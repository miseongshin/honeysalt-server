# honeysalt-server

[HoneySaltLove.com](https://honeysaltlove.com)의 **지혜나눔 API 서버**입니다.

사용자가 고민을 올리면 다른 회원이 도움이 된 생각과 활동으로 답합니다. 마음에 드는 활동은 스케줄로 묶어 [꿀방울 앱](https://honeysaltlove.com/ko/app/honey-drops/)으로 보냅니다. 앱은 로그인 없이 앱 키를 사용해 활동 완료와 도움돼요 기록을 전송합니다.

> 2차 개발 진행 중 (2026.10~). 현재 서버 기본 구성(헬스 체크 · 환경별 설정 분리 · 비밀값 암호화)

## 주소

| 구분 | 주소 |
|---|---|
| 지혜나눔 웹 (개발 중) | [honeysaltlove.com/ko/wisdom](https://honeysaltlove.com/ko/wisdom) |
| API 서버 (개발 중) | `https://api.honeysaltlove.com/api/v1/…` |
| 포트폴리오 설계서 | [지혜나눔 2차 설계서](https://shinmiseong.com/portfolio/honeysaltlove/v2/) |

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 3.5 · Spring Security · Spring Data JPA |
| 데이터베이스 | PostgreSQL |
| 설정 암호화 | Jasypt (`ENC(...)`) |
| 빌드 | Gradle Wrapper |

## 문서

| 문서 | 내용 |
|---|---|
| [설계서](docs/design-doc.html) | 기능 범위 · ERD · API · 처리 규칙 · 진행표 |
| [Java 코딩 가이드](docs/java-coding-guide.md) | 코딩 규칙 · 작업 방식 · AI 협업 규칙 |

## 패키지 구조

```text
com.honeysaltlove
├── domain/<영역>/   도메인별 코드 (member · worry · activity 등)
└── global/          공통 코드
    └── config/      Jasypt 설정
```