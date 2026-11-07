# 요건 정리 — 지혜나눔 (honeysalt-server)

개발하면서 더블체크하는 목록. [설계서](design-doc.html) 7절 진행표 순서대로 둔다.
단계를 시작할 때 그 절을 보고, 끝낼 때 체크한다. 코드 규칙은 [Java 코딩 가이드](java-coding-guide.md).

**범위**: honeysaltlove.com 대메뉴 지혜나눔 — 고민에 생각 · 활동으로 답하고, 활동을 스케줄로 묶어 꿀방울 앱으로 보낸다.
화면 `honeysaltlove.com/{lang}/wisdom` · 서버 `api.honeysaltlove.com/api/v1/…` · 2차는 한국어만.

---

## 0. 공통

- 비밀값은 환경변수 · jasypt `ENC(...)` 로만. 주석 · 로그 · 커밋에 남기지 않는다
- 설정 파일(`application*.yml`) · 개발용 README 는 `.gitignore` — 커밋 전에 확인
- 로그에 개인정보(소셜 회원번호 · 생년월일 · 글 내용)를 로그, 코드로 남기지 않는다.
- 엔티티 + 마이그레이션 + 테스트를 한 묶음으로 진행한다.

---

## 1. 서버 설정 (11/3~)

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

## 2. 엔티티 · Flyway (11/5~)

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

## 서버 배포
1. [ ] CI/CD (Jenkins)
    - Jenkins: 서버에 Docker 컨테이너로 · 화면은 밖에 열지 않고 SSH 터널(`localhost:8081`)로 본다
    - 테스트: 전체 테스트 — DB 테스트는 Testcontainers(PostgreSQL)로 (터널 없이 · 테스트 설정은 `application-test.yml`)
    - 배포: `Jenkinsfile` — Build Now → 테스트 → docker build → rm · run → health `UP` 확인

## 3. API 구현

1. [ ] 기본 데이터: `tag` · `terms` — 태그 목록 · 약관 본문이 정해진 뒤 새 V 파일로
2. [ ] 엔티티 규칙 확인 (엔티티를 다 만든 뒤 · 맨 끝): 연관 모두 `LAZY` · enum `@Enumerated(STRING)` · setter 없이 메서드로만 (`hide("AI: 광고")`)