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
10. [ ] DB 연결: 앱과 DB 컨테이너를 같은 Docker 네트워크로. 운영 DB `love` (개발은 `love_local` — DB 로 나눈다)
11. [ ] 운영 설정(`application-prod.yml`)
    - [ ] datasource 주소 `honeysalt-postgres:5432`
    - [ ] `server.forward-headers-strategy: framework` (없으면 OAuth 콜백이 `http://`)
    - [ ] Actuator 는 `/actuator/health` 만 연다 (`SecurityConfig` 에서 health 만 허용)
12. [ ] 앱 실행: Docker 컨테이너로. 설정 파일은 마운트, 마스터 키 `JASYPT_ENCRYPTOR_PASSWORD` 는 서버 `.env`
13. [ ] nginx: `api.honeysaltlove.com` → 앱 `localhost:8080`
14. [ ] **완료 기준**: `https://api.honeysaltlove.com/actuator/health` → `UP`
15. [ ] (나중) DB 포트 `0.0.0.0:5432` → `127.0.0.1` · firewalld 5432 규칙 확인
