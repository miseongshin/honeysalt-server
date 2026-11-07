# 요건 정리 — 지혜나눔 (honeysalt-server)

개발하면서 더블체크하는 목록. [설계서](design-doc.html) 7절 진행표 순서대로 둔다.
단계를 시작할 때 그 절을 보고, 끝낼 때 체크한다. 코드 규칙은 [Java 코딩 가이드](java-coding-guide.md).

**범위**: honeysaltlove.com 대메뉴 지혜나눔 — 고민에 생각 · 활동으로 답하고, 활동을 스케줄로 묶어 꿀방울 앱으로 보낸다.
화면 `honeysaltlove.com/{lang}/wisdom` · 서버 `api.honeysaltlove.com/api/web/v1/…` (웹) · `/api/app/v1/…` (앱) · 2차는 한국어만.

---

## 0. 공통

- 비밀값은 환경변수 · jasypt `ENC(...)` 로만. 주석 · 로그 · 커밋에 남기지 않는다
- 설정 파일(`application*.yml`) · 개발용 README 는 `.gitignore` — 커밋 전에 확인
- 로그에 개인정보(소셜 회원번호 · 생년월일 · 글 내용)를 로그, 코드로 남기지 않는다.
- 엔티티 + 마이그레이션 + 테스트를 한 묶음으로 진행한다.

---

## 2. 서버 구성 (11/3~)

1. [x] 키 파일 권한 설정 (PC 처음 한 번)
2. [x] 서버 접속: `ssh -i <키 파일 경로> <계정>@<서버 IP>`
3. [x] 서버 상태 확인: 메모리 · CPU · Docker · nginx · PostgreSQL(Docker 로 설치했던 것 — 볼륨 확인) · 열린 포트
4. [x] 서버 방화벽 80 · 443 열기 (firewalld)
5. [x] nginx 설치 · 실행 (서버에 직접)
6. [x] 도메인: `api.honeysaltlove.com` → 서버 (DNS A 레코드)
7. [x] OCI 콘솔 포트 80 · 443 열기 (NSG)
8. [x] HTTPS: 서버에서 `certbot --nginx` (Let's Encrypt, 무료 · 자동 갱신)
9. [x] Dockerfile · 서버에서 이미지 만들기 (`docker build`)
10. [x] DB 연결: 앱과 DB 컨테이너를 같은 Docker 네트워크로. 운영 DB `love` (개발은 `love_local` — DB 로 나눈다)
11. [x] 운영 설정(`application-prod.yml`)
    - [x] datasource 주소 `honeysalt-postgres:5432`
    - [x] `server.forward-headers-strategy: framework` (없으면 OAuth 콜백이 `http://`)
    - [x] Actuator 는 `/actuator/health` 만 연다 (`SecurityConfig` 에서 health 만 허용)
12. [x] 서버 앱 실행: Docker 컨테이너로. 설정 파일은 마운트, 마스터 키 `JASYPT_ENCRYPTOR_PASSWORD` 는 서버 `.env`
13. [x] nginx: `api.honeysaltlove.com` → 앱 `localhost:8080`
14. [x] **완료 기준**: `https://api.honeysaltlove.com/actuator/health` → `UP`
15. [x] (나중) DB 포트 `0.0.0.0:5432` → `127.0.0.1` · firewalld 5432 규칙 확인

---

## 3. 엔티티 · Flyway (11/5~)

설계서 3절(ERD · 테이블 정의 · 제약조건 · 참고사항)을 보고 만든다. 묶음마다 엔티티 + 마이그레이션 + 테스트.

1. [x] Flyway 붙이기: `flyway-core` · `flyway-database-postgresql` · `ddl-auto: validate` (local · prod) — 테이블은 Flyway 만 만든다
2. [x] 공통: `BaseTimeEntity`(등록일 · 수정일) · `BaseCreatedEntity`(등록일) · JPA Auditing
3. [x] 회원: `member` · `member_nickname_history` · `terms` · `member_terms_history`
4. [x] 주요기능: `honey_worry` · `honey_thought_tip` · `honey_thought_tip_url` · `honey_activity_tip` · `honey_activity` · `honey_activity_url` · `honey_schedule` · `honey_schedule_activity` — 미션은 테이블 없이 enum
5. [x] 태그: `tag` · `member_tag` · `honey_worry_tag` · `honey_thought_tip_tag` · `honey_schedule_tag` · `honey_activity_tag` · `tag_blocklist`(사용이 차단된 태그) — 연결 엔티티 없이 `@JoinTable`
6. [x] 반응: `honey_comment` · `honey_like` · `honey_complaint`(신고 — 저장하고, 같은 글에 3명 이상 신고하면 관리자에게 메일 · 메일 발송 설정은 그때)
7. [x] 앱 · 기타: `member_app_key` · `cart` · `app_done_log` · `app_helped_log` · `feedback` — 로그는 `app_key_id` 숫자만(연관 없이), 추가만
8. [x] 로컬 `love_local` 에 적용 → 앱이 뜨고 `validate` 통과
9. [x] 서버 `love` 에 적용 (배포하면 Flyway 가 올린다) → `https://api.honeysaltlove.com/actuator/health` → `UP`

## 4. 서버 배포
1. [x] CI/CD (Jenkins)
    - Jenkins: 서버에 Docker 컨테이너로 · 화면은 밖에 열지 않고 SSH 터널(`localhost:8081`)로 본다
    - 테스트: 전체 테스트 — DB 테스트는 Testcontainers(PostgreSQL)로 (터널 없이 · 테스트 설정은 `application-test.yml`)
    - 배포: `Jenkinsfile` (작업 `honeysalt-server`) — Build Now → 테스트 → docker build → rm · run → health `UP` 확인
    - 테스트만: `Jenkinsfile.test` (작업 `honeysalt-test`) — 배포하지 않는다

## 5. API 명세

1. [x] API 명세를 정했다 (설계서 4절, 2026-11-07). 주소 · 요청과 응답 항목 · 응답 형식 · 오류 형식 · Swagger 를 다룬다. 정한 내용을 실제로 만드는 일은 9. API 구현에서 한다.
    - [x] 주소: 웹 API 는 `/api/web/v1/…` 로 시작하고 세션 쿠키로 인증한다. 앱 API 는 `/api/app/v1/…` 로 시작하고 앱 키로 인증한다. `/actuator/health` 는 그대로 둔다.
    - [x] 좋아요를 두 번 눌렀을 때: 사용자가 다시 누르면 화면이 취소 요청(DELETE)을 보낸다. 서버는 같은 POST 가 다시 와도 오류(409) 없이 기존 상태를 유지한다.
    - [x] 목록: 모든 목록은 페이지 번호(`page` · `size`)로 가져온다. 커서(키셋) 방식은 쓰지 않는다.
    - [x] 댓글 목록: 첫 페이지에 좋아요가 많은 댓글 3개(좋아요 1개 이상)를 먼저 보여 주고, 그 아래에 최신 댓글을 보여 준다.
    - [x] 명세를 둘 곳: 설계서에는 주소 목록과 규칙을 적고, 요청 · 응답 항목은 Swagger(springdoc)가 코드에서 보여 준다. Swagger 는 local 에서만 켜고 운영에서는 끈다.
    - [x] 빠진 API: 로그인 · 앱 연결 · 신고 · 댓글 수정과 숨김 · 관리자 API 를 더했다. 설계서 4절 표를 기능별로 나눴고, 내 정보 주소는 `/member` 로 정했다 (로그인한 회원은 한 명이라 단수형).
    - [x] 검색: 검색 색인 테이블 `search_index` 를 따로 둔다 (만들 때 새 V 파일, `pg_trgm` + GIN 색인). 글을 등록하면 색인 행을 추가하고, 숨기면 삭제하고, 수정하면 삭제하고 `flush()` 한 뒤 다시 등록한다. 모든 글을 색인에 넣고, 검색어가 3글자 이상일 때만 색인을 사용한다 (3글자 미만은 전체를 확인한다). 정렬은 찾은 키로 원본 글과 이어서 한다.
    - [x] 응답 형식: 성공과 실패 모두 `{ status, code, message, data }` 로 응답한다 (`ApiResponse<T>`). `status` 는 HTTP 상태와 같은 값이고, 성공은 모두 200 과 `OK` 코드를 쓴다.
    - [x] 오류 응답: `status` 는 숫자, `code` 는 오류 이름이다. 입력 오류는 어느 입력 항목이 틀렸는지 알려 주지 않는다. `message` 는 영어로 쓰고, 사용자에게 보이는 문구는 화면과 앱이 `code` 를 보고 정한다.
    - [x] 요청 제한(429): nginx 에서 막는다 (서버 구성 13e). nginx 가 직접 응답하는 오류도 같은 JSON 형식으로 보낸다. 앱 키로 세는 방식 등은 문제가 생기면 더한다.

## 6. 로그인

1. [ ] 로그인을 만든다 (진행표 6. 로그인). 로그인 화면은 honeysalt-vue 의 7. 로그인 화면에서 따로 만든다.
    - [ ] 미리 할 것: Google Cloud 에서 OAuth 클라이언트를 만들고 리디렉션 주소(운영 · 로컬)를 등록한다. client-id 와 secret 은 `ENC(...)` 로 바꿔 yml 에 넣는다 (git 밖). 약관 첫 버전(`terms` 기본 데이터)을 준비한다.
    - [ ] 구글 로그인: `spring-boot-starter-oauth2-client` 를 붙인다. `provider` 와 `provider_uid` 로 회원을 찾고, 있으면 `last_login_at` 을 갱신한다. 없으면 가입 대기로 두고 약관 · 닉네임 화면으로 보낸다.
    - [ ] Security: 웹 API(`/api/web/**`)는 세션으로, 앱 API(`/api/app/**`)는 앱 키로 인증한다. 조회(GET)는 누구나, 쓰기는 USER, 관리자 API(`/admin/**`)는 ADMIN 만 쓸 수 있다. 가입을 덜 마친 회원에게는 `TERMS_REQUIRED` 를 보낸다. 401 · 403 도 응답 형식에 맞춰 보낸다.
    - [ ] CORS: `https://honeysaltlove.com` 과 `http://localhost:3000` 을 허용하고, 쿠키를 실어 보낼 수 있게 `allowCredentials` 를 켠다.
    - [ ] CSRF: 토큰을 쿠키로 주고(`CookieCsrfTokenRepository`, 쿠키 도메인 `.honeysaltlove.com`) 화면이 `X-XSRF-TOKEN` 헤더로 다시 보낸다. 앱 API(`/api/app/**`)는 쿠키를 쓰지 않으므로 끈다.
    - [ ] 세션 쿠키: `HttpOnly` · `Secure` · `SameSite=Lax` 로 둔다.
    - [ ] API: 설계서 4.1 의 1) 로그인 · 회원 표를 만든다 (`GET /api/web/v1/terms` · `POST /api/web/v1/member/terms` · `GET · PATCH · DELETE /api/web/v1/member` · `GET /api/web/v1/member/posts` · `POST /api/web/v1/logout`).
    - [ ] 닉네임: 2~12자이고 띄어쓰기를 넣을 수 없다. 비워 두면 자동으로 만든다. 바꾸면 전 이름을 `member_nickname_history` 에 남긴다.
    - [ ] 앱 키 발급(`POST /api/app/v1/keys`): 비밀값은 처음 발급할 때 한 번만 보여 주고, 서버에는 해시만 저장한다.
    - [ ] 테스트: 로그인, 가입 마치기, 권한(남의 글 수정 · 일반 회원의 관리자 API 는 403), CSRF 토큰 없는 POST(403), 탈퇴를 확인한다.
    - [ ] 정할 것: 탈퇴할 때 내가 만든 활동을 어떻게 할지, 세션을 메모리와 DB 중 어디에 둘지, 관리자를 어떻게 지정할지(추천: DB 에서 role 을 직접 바꾼다), 닉네임 중복을 허용할지, 로그인 뒤 어느 화면으로 돌아갈지 정한다.

## 9. API 구현

1. [ ] 기본 데이터: 태그 목록과 약관 본문이 정해지면 `tag` · `terms` 기본 데이터를 새 V 파일로 넣는다.
2. [ ] 엔티티 규칙 확인: 엔티티를 다 만든 뒤 맨 끝에 한 번 더 확인한다. 연관은 모두 `LAZY`, enum 은 `@Enumerated(STRING)` 이고, 값은 setter 없이 이름 있는 메서드로만 바꾼다 (예: `hide("AI: 광고")`).
3. [ ] 공통 응답 · 오류: 5절에서 정한 응답 형식과 오류 code 목록을 코드로 만든다. 클래스 이름은 java-coding-guide 를 따른다 (`ApiResponse<T>`, `ResultCode`, `BusinessException`, `@RestControllerAdvice`).
4. [ ] Swagger: `springdoc-openapi` 를 붙인다. local 에서만 켜고, `application-prod.yml` 에서는 끈다.
5. [ ] 검색 색인 테이블: 새 V 파일로 `pg_trgm` 을 켜고 `search_index` 테이블과 GIN 색인을 만든다. 글을 등록 · 숨김 · 수정할 때 같은 트랜잭션에서 색인을 맞춘다 (수정은 삭제 → `flush()` → 다시 등록).
6. [ ] 기능별 API: 설계서 4.1 의 3)~7) 표와 4.2 앱 API 를 만든다. 로그인과 앱 연결은 6절과 12절에서 만든다.
