# honeysalt-server

[HoneySaltLove.com](https://honeysaltlove.com/)의 **지혜나눔 API 서버**입니다.

사용자가 고민을 올리면 다른 회원이 도움이 되는 생각과 활동을 답변으로 남깁니다. 마음에 드는 활동은 스케줄로 묶어 [꿀방울 앱](https://honeysaltlove.com/ko/app/honey-drops/)으로 보냅니다. 앱은 로그인 없이 앱 키로 활동 완료와 ‘도움돼요’ 기록을 전송합니다.

> 2차 개발 진행 중 (2026.10~)

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
| 데이터베이스 | PostgreSQL 15 · Flyway |
| 설정 암호화 | Jasypt |
| 빌드·배포 | Gradle · Docker · nginx |

## 패키지 구조

```text
com.honeysaltlove
├── domain/          기능별로 나눈다 — 영역마다 entity · repository …
│   ├── member/      회원 · 약관
│   ├── worry/       고민
│   ├── tip/         생각꿀팁 · 활동꿀팁
│   ├── activity/    활동
│   ├── schedule/    꿀방울 스케줄
│   ├── reaction/    댓글 · 좋아요 · 신고
│   └── tag/         태그 · 사용이 차단된 태그
└── global/          모든 기능이 함께 쓰는 것 (업무 규칙은 두지 않는다)
    ├── common/      공통 엔티티 · 언어 · 공개/숨김
    └── config/      보안 · JPA · 암호화 설정
```

## 문서

| 문서 | 내용 |
|---|---|
| [설계서](docs/design-doc.html) | 기능 범위 · ERD · API · 처리 규칙 · 진행표 |
| [Java 코딩 가이드](docs/java-coding-guide.md) | 코딩 규칙 · 작업 방식 · Claude 협업 및 결과 검토 기준 |

## AI 활용

Claude를 설계 검토와 코드 작성에 활용했으며, 결과를 직접 검토·수정해 반영했습니다.
