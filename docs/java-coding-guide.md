# Java 코딩 가이드 — honeysalt-server

개발자와 AI 가 함께 참고하는 프로젝트 코딩 가이드.
AI 는 기능을 구현하거나 수정하기 전에 이 문서의 규칙과 작업 절차를 확인한다.

설계 문서(테이블 · 엔티티 · 진행표): [design-doc.html](design-doc.html)

---

## 1부. 공통

### AI 작업 원칙

1. **커밋 · push 는 사용자가 확인 창에서 번호를 눌러야 실행된다.**
   - "커밋해" · "푸시해" 라는 말은 준비해도 된다는 뜻이다. 실행 허락은 확인 창에서 받는다.
   - 실행 전에 무엇을 올리는지(파일 목록 · 커밋 메시지 · 저장소)를 먼저 보여 준다.
   - 확인 창이 안 뜨면 실행하지 않고 멈춘다. 설정(`settings.json` 의 `ask`)에서 빠진 것이니 사용자에게 알린다.
   - 확인 창을 피하려고 명령을 바꿔 쓰지 않는다(`git -C` · 스크립트로 감싸기 등).
2. **이 저장소(`honeysalt-server`)만 고친다.** 타 저장소 작업 시 사용자의 승인 과정을 반드시 거친다.
3. **파일 편집은 `Edit` 도구로.** 고치기 전에 어느 파일을 고칠지 알린다.
   - `Bash` · `PowerShell` 명령(`sed` · `Set-Content` · 스크립트)으로 소스를 고치지 않는다.
     파일을 통째로 다시 써서 사용자가 쓴 글을 지우거나, 기호가 풀려 엉뚱한 글자가 섞여 들어갈 수 있다.
   - `Edit` 는 찾은 문장만 바꾸고, 읽은 뒤 파일이 바뀌었으면 고치지 않고 멈춘다 — 저장된 사용자 글은 지켜진다.
   - IDE 에서 **저장하지 않은 글은 AI 가 볼 수 없다.** 그래서 고칠 파일을 먼저 알리고,
     사용자가 그 파일을 저장해 두고 손을 뗀 뒤에 고친다.
4. **질문을 명령으로 이해하지 않는다.** 질문 시 바로 작업하지 않는다.
5. **요청한 것이 아닌 것은 반드시 물어보고,** 사용자가 승인하면 고친다.
6. **공통 코드는 사용자에게 영향도를 설명하고 승인 후 작업한다.**
7. **되돌릴 때는 AI 가 만든 변경만 되돌린다.** `git checkout --` 처럼 파일째 되돌리면 사용자의 작업도 사라진다.
8. **작업 후 확인한다.** `gradlew test` · `gradlew bootJar` 결과를 보고하고, 실패하면 오류 출력을 함께 보고한다.
9. **기능 하나씩 작게.** 엔티티 + 마이그레이션 + 테스트를 한 묶음으로 내고 검토받는다.
10. **주변 코드처럼 쓴다.** 아래 관례를 따른다.
11. **같은 코드를 두 번 만들지 않는다.** 먼저 있는 구현을 찾고, 공통으로 뽑아야 하면 사용자와 협의한다.

---

### 보안

1. **비밀값은 환경변수나 jasypt `ENC(...)` 로 관리하고 저장소에 커밋하지 않는다.** (OAuth client secret · DB 비밀번호)
   - 로그에 개인정보(소셜 회원번호 · 생년월일 · 글 내용)를 남기지 않는다.
2. **입력값은 DTO 의 `@Valid` 로 길이 · 형식을 검사한다.** 링크는 `http` · `https` 만 받는다.
3. **XSS 필터(`<` → `&lt;` 저장)는 쓰지 않는다.** 화면은 Vue `{{ }}` 로 출력한다.
4. **수정 · 삭제 · 숨김과 내 글 조회는 서비스에서 본인인지(`member_id`) 확인한다.** 주소의 id 만 믿지 않는다.
5. **쿼리는 JPA 파라미터로만 만든다.** 문자열을 이어 붙여 쿼리를 만들지 않는다.
6. **앱 인증은 `app_key` 와 앱이 만든 값을 더한 해시(`app_hash`)로 확인한다.**
   - 앱이 만든 값은 저장하지 않는다. 헤더(`Authorization`)로만 받고 주소 · 로그에 남기지 않는다.
7. **오류 응답에 스택 · SQL · 내부 메시지를 보내지 않는다.** `ResultCode` 로만 답한다.
8. **CORS 와 CSRF**
   - CORS 허용 출처는 운영 웹 주소(`https://honeysaltlove.com`)로 제한한다.
   - 웹은 세션 쿠키 인증을 쓰므로 CSRF 보호를 적용한다.
   - 앱 API(`/app/**`)는 세션 쿠키 대신 `Authorization` 헤더로 인증하므로 CSRF 검증에서 제외한다.

---

### 패키지

```
com.honeysaltlove
  domain/<영역>/          member · worry · tip · activity · schedule · reaction · cart · tag · feedback
    controller/
    entity/               엔티티 · enum
    repository/
    service/
    dto/                  요청 · 응답 (record)
  global/                 모든 기능이 함께 쓰는 것. 업무 규칙(서비스)은 두지 않는다
    common/               ApiResponse · ResultCode · BaseTimeEntity
    config/               SecurityConfig · JasyptConfig · JpaConfig
    util/
```

global 에 두는 것:

| 위치 | 클래스 | 하는 일 | 적용 단계 |
|---|---|---|---|
| `config/` | `JasyptConfig` | 비밀값 암호화 설정 | 현재 구현 |
| `common/` | `ApiResponse` | 모든 API 응답 모양 (status · code · message · data) | 1차 구현 예정 |
| `common/` | `ResultCode` | 결과 코드 목록 | 1차 구현 예정 |
| `common/` | `BusinessException` | 규칙 위반 예외 하나로 통일 | 1차 구현 예정 |
| `common/` | `GlobalExceptionHandler` | 예외 → `ApiResponse` 오류 응답 (`@RestControllerAdvice`) | 1차 구현 예정 |
| `common/` | `BaseTimeEntity` | 모든 엔티티의 생성 · 수정 시각 | 1차 구현 예정 |
| `config/` | `JpaConfig` | JPA Auditing 켜기 | 1차 구현 예정 |
| `config/` | `SecurityConfig` | 시큐리티 · CORS · CSRF | 로그인 구현 때 |
| `common/` | `PageResponse` | 목록 API 페이지 정보 | 2차 구현 예정 |

회원을 찾아야 하는 인증 코드(로그인 회원 꺼내기 · 앱 키 필터)는 `global` 이 아니라 `domain/member/auth/` 에 둔다.

#### 패키지 이름과 클래스 이름

- **패키지 이름과 클래스 이름은 따로다.** `global`(패키지) 와 `GlobalExceptionHandler`(클래스) 의
  "Global" 은 둘 다 "전체에 걸친다"는 뜻일 뿐, 이름 규칙으로 묶이지 않는다

  ```
  com.honeysaltlove.global.common.GlobalExceptionHandler
                    ──────        ──────────────────────
                    패키지(폴더)     클래스
  ```

- 클래스 이름은 **스프링에서 흔히 쓰는 이름**을 쓴다(`GlobalExceptionHandler` · `BusinessException` ·
  `BaseTimeEntity`). 길어도 보는 사람이 이름만 보고 알아본다. 줄인 이름(`AppException` · `PageRes`)은 쓰지 않는다

---

### 이름

| 대상 | 규칙 | 예 |
|---|---|---|
| 클래스 · enum · record | 명사, PascalCase | `Worry` · `Status` · `WorryCreateRequest` |
| 메서드 | 동사로 시작, camelCase | `hide()` · `findWaiting()` · `countByMember()` |
| boolean | `is` · `has` · `can` | `isFixed()` · `isAnonymous()` · `canEdit(member)` |
| 상수 | UPPER_SNAKE | `MAX_TEXT_LENGTH` |
| 패키지 | 소문자 한 단어 | `domain.worry.service` |
| 테이블 · 컬럼 | snake_case (엔티티 필드는 camelCase, 이름 변환은 기본 전략에 맡김) | `schedule_activity` |

- 줄임말 쓰지 않기: `mbr` ✕ → `member`. 단, 널리 쓰는 것은 괜찮다(`id` · `url` · `dto`)
- 줄임말도 PascalCase 규칙대로: `HttpUrl` ✕`HTTPURL`, `OAuth2User` 는 라이브러리 이름이라 그대로
- DTO 이름은 쓰임을 붙인다: `…Request` · `…Response` · `…Summary`(목록용)
- 리포지토리 메서드는 Spring Data 규칙(`findBy…` · `existsBy…` · `countBy…`), 길어지면 `@Query` 로 짧은 이름
- 서비스는 클래스 하나로 만든다(`ServiceImpl` 없음).
  - 인터페이스는 구현이 둘 이상이거나 외부 연동(AI 검사 · 소셜 로그인)일 때만 만든다.
    구현 클래스는 특별한 이름이 없을 때 앞에 `Default` 를 붙인다(`DefaultXxx`).

---

### 모양

- 들여쓰기 **공백 4칸**(기존 파일 기준), 한 줄 120자
- 중괄호는 한 줄짜리 `if` 에도 쓴다
- import 는 `*` 쓰지 않는다
- 클래스 안 순서: 상수 → 필드 → 생성자 → 정적 팩토리 → public 메서드 → private 메서드
- `.editorconfig` 와 IntelliJ 포맷터를 맞춰 두고, 커밋 전에 `Ctrl+Alt+L`

---

### 주석

**원칙: 코드는 "무엇을", 주석은 "왜"를 말한다. 쉽고 짧게 쓴다**

```java
// ✕ 무엇을 — 코드를 다시 읽어 주는 주석
// 좋아요를 저장한다
likeRepository.save(like);

// ○ 왜 — 코드만 봐서는 모르는 이유
// 수(like_count)는 여기서 올리지 않는다. 호출마다 수정하면 인기 글에서 로우 락(row lock)으로 기다린다. 스케줄러가 1분마다 다시 세어 맞춘다
likeRepository.save(like);
```

| 언제 | 무엇을 |
|---|---|
| 파일 맨 위 | 머리 주석(아래 양식) |
| public 클래스 · 서비스 메서드 | Javadoc 한두 줄: 하는 일 · 규칙 · 던지는 예외 |
| 규칙 · 예외 처리 · 우회 | 왜 그렇게 했는지 |
| 정한 값 | 그 숫자의 출처 (`// 시안의 닉네임 규칙: 2~12자`) |
| 쓰지 말 것 | 지운 코드 주석 처리(git 에 있다), 코드 사이의 변경 이력 주석(git log), 뻔한 getter 설명 |

머리 주석 양식 (파일 맨 위):

```java
/**
 * 고민 등록 · 수정 · 숨김
 *
 * @author shinm
 * @since 2026-10-30
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-10-30        shinm              최초 생성
 */
```

이력 표는 파일을 열자마자 흐름을 보려고 둔다 (git 을 매번 찾지 않게).

- 동작이 바뀔 때만 한 줄 — 하는 일 · 규칙 · 연결이 달라졌을 때
- 주석 · 이름 · 줄 맞춤처럼 동작이 그대로인 수정은 적지 않는다
- NOTE 는 무엇이 달라졌는지 짧게 (`OAuth 로그인 주소 열기`)
- 동작을 바꾼 커밋이면 커밋 전에 이력 줄이 들어갔는지 같이 본다

Javadoc 예:

```java
/**
 * 고민을 숨긴다. 지우지 않고 HIDDEN 으로 둔다.
 * @author shinm
 * @since 2026-10-30
 * @throws BusinessException FORBIDDEN — 본인 글이 아닐 때
 */
public void hide(Long worryId, Long memberId) { … }
```

남겨 둘 일은 형식을 맞춘다: `// TODO(shinm): 3차 다국어 때 번역 테이블로 옮긴다`

#### 주석 예시 — 이 프로젝트에서

**엔티티 클래스** — 이 테이블이 무엇이고, 한눈에 안 보이는 규칙

```java
/**
 * 고민. 등록하면 바로 공개하고, 지우지 않고 숨김(HIDDEN)으로만 내린다.
 * @author shinm
 * @since 2026-10-30
 */
@Entity
public class Worry extends BaseTimeEntity {

    // 목록 정렬(좋아요순)에 바로 쓰려고 미리 센다. 원본은 honey_like, 스케줄러가 1분마다 다시 세어 맞춘다
    private int likeCount;

    // 0 이면 '답을 기다리는 고민'. 생각꿀팁 · 활동꿀팁을 쓸 때 1 올린다
    private int answerCount;
```

**상태를 바꾸는 메서드** — 규칙과 실패 조건

```java
    /**
     * 숨긴다. 본인 · 관리자 · AI 검사가 숨길 수 있고, 누가 왜 숨겼는지 남긴다.
     * @author shinm
     * @since 2026-10-30
     * @param reason 예: "AI: 광고" · "관리자: 욕설"
     */
    public void hide(String reason) {
```

**쿼리** — 왜 이렇게 짰는지

```java
    // fetch join 으로 작성자를 같이 가져온다. 목록마다 member 를 따로 부르면 N+1
    // (status, answer_count, created_at) 색인을 탄다 — '답을 기다리는 고민' 목록
    @Query("""
        select w from Worry w join fetch w.member
        where w.status = 'PUBLIC' and w.answerCount = 0
        order by w.createdAt desc
        """)
    List<Worry> findWaiting(Pageable pageable);
```

**설정** — 빼면 무엇이 깨지는지

```yaml
server:
  # nginx 뒤에서 돈다. 없으면 스프링이 http://localhost 로 알아서
  # OAuth 콜백을 http:// 로 만들고 구글 · 카카오가 거절한다
  forward-headers-strategy: framework
```

**검증** — 숫자의 출처

```java
public record NicknameRequest(
        // 시안의 닉네임 규칙: 2~12자. 비우면 서버가 자동으로 만든다
        @Size(min = 2, max = 12) String nickname
) {}
```

**보안상 막은 것** — 다음 사람이 풀지 않게

```java
    // http · https 만 받는다. javascript: 링크를 글에 심으면 누르는 순간 스크립트가 돈다
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
```

**쓰지 말 것**

```java
// ✕ 코드를 다시 읽어 주는 주석
// 회원을 저장한다
memberRepository.save(member);

// ✕ 지운 코드를 주석으로 남기기 — git 에 있다
// worry.setStatus(Status.HIDDEN);

// ✕ 변경 이력 — git log 에 있다
// 2026-11-02 shinm 사유 칸 추가
// 2026-11-05 shinm 길이 200 → 300

// ✕ 거짓말이 된 주석 — 코드를 고치면 주석도 같이 고친다
// 10개까지 가져온다
.limit(20)
```

---

### 중복 금지 — 공통을 만들어 쓴다

**같은 코드를 두 번 쓰지 않는다.** 두 번째로 쓰게 되는 순간 공통으로 뽑고, 첫 번째 자리도 그걸 쓰게 바꾼다.

- 쓰기 전에 **있는지 먼저 찾는다**: `global/` · 같은 도메인의 `service` · `dto`
- 놓는 자리
  - 한 도메인 안에서만 쓰면 → 그 도메인 안(`domain/worry/…`)
  - 두 도메인 이상이 쓰면 → `global/`
- 이 프로젝트에서 공통으로 둘 것

| 공통 | 대신하는 중복 |
|---|---|
| `ApiResponse` · `ResultCode` | 컨트롤러마다 응답 모양 만들기 |
| `BusinessException` + `@RestControllerAdvice` | 서비스마다 try-catch · 오류 응답 |
| `BaseTimeEntity` | 엔티티마다 `createdAt` · `updatedAt` |
| `@LoginMember` (ArgumentResolver) | 컨트롤러마다 세션에서 회원 꺼내기 |
| `PageResponse<T>` | 목록 API 마다 페이지 정보 담기 |
| `@SafeUrl` (검증 애너테이션) | 링크 받는 DTO 마다 http · https 검사 |
| 주인 확인 (`assertOwner(member)`) | 수정 · 삭제마다 "내 글인가" 비교 |

- 비슷하지만 조금 다른 코드를 **복사해서 고치지 않는다.** 다른 부분을 매개변수로 받는 공통으로 만든다
- 공통이 쓰는 쪽 사정으로 `if` 가 늘어나기 시작하면 공통이 잘못 나뉜 것이다. 다시 나눈다
- `global/` 의 **있는 공통을 바꿀 때는 승인 후** — 쓰는 곳 전부에 영향이 간다(위 「AI 작업 원칙」 6번)

---

### 클래스 · 메서드

- **메서드는 한 가지 일.** 20~30줄을 넘으면 나눌 곳을 찾는다
- **일찍 돌아간다.** 조건이 겹겹이 쌓이면 뒤집어서 먼저 `return` · `throw`

  ```java
  // ✕
  if (worry != null) { if (worry.isOwnedBy(memberId)) { … } }
  // ○
  if (!worry.isOwnedBy(memberId)) throw new BusinessException(ResultCode.FORBIDDEN);
  …
  ```

- **매개변수는 3개까지.** 넘으면 객체(record)로 묶는다
- **매직 넘버 금지.** 상수나 enum 으로: `if (text.length() > 50)` ✕ → `MAX_TEXT_LENGTH`
- **의존성은 생성자 주입.** `@RequiredArgsConstructor` + `private final`. `@Autowired` 필드 주입 금지
- 컨트롤러는 얇게: 받기 → 검증(`@Valid`) → 서비스 호출 → 응답. 규칙은 서비스 · 엔티티에

---

### 불변 · null

- 바뀌지 않는 값은 `final`. DTO 는 **record**
- 컬렉션을 밖에 내줄 때는 고칠 수 없게: `List.copyOf(...)` · `Collections.unmodifiableList(...)`
- **`Optional` 은 반환에만.** 필드 · 매개변수 · 컬렉션 안에 쓰지 않는다
- `Optional.get()` 대신 `orElseThrow(() -> new BusinessException(...))`
- 빈 목록은 `null` 이 아니라 `List.of()` 로 돌려준다

---

### 예외

- 업무 규칙 위반은 `BusinessException(ResultCode)` 하나로 던지고, `@RestControllerAdvice` 가 응답으로 바꾼다
- `catch (Exception e)` 로 삼키지 않는다. 잡았으면 처리하거나, 원인을 붙여 다시 던진다
  (`throw new BusinessException(code, e)`)
- 검사 예외(checked)를 위로 끌고 다니지 않는다. 경계에서 바꾼다
- 오류 문구는 `ResultCode` 에 모은다. 서비스 · 컨트롤러 곳곳에 문구를 흩어 쓰지 않는다

---

### 로그

- `@Slf4j` · 자리표시자 사용: `log.info("worry hidden: id={}", worryId)` (문자열 더하기 ✕)
- 수준: `error` 사람이 봐야 할 실패 · `warn` 이상하지만 계속 감 · `info` 중요한 흐름(숨김 · 탈퇴) · `debug` 개발용
- **개인정보 · 비밀값은 찍지 않는다.** 소셜 회원번호 · 생년월일 · 글 내용 · 토큰
- 예외는 마지막 인자로 넘겨 스택을 남긴다: `log.error("translate failed: id={}", id, e)`

---

### Lombok

| 쓴다 | 안 쓴다 |
|---|---|
| `@Getter` · `@RequiredArgsConstructor` · `@Builder` · `@Slf4j` | `@Data` · `@Setter`(엔티티) · `@AllArgsConstructor`(엔티티) · `@ToString`(연관 있는 엔티티) |
| 엔티티: `@NoArgsConstructor(access = PROTECTED)` | `@EqualsAndHashCode`(엔티티) |

- 엔티티의 `@Builder` 는 클래스가 아니라 **생성자**에 붙인다 — id · 카운트 같은 칸을 밖에서 못 넣게

---

### 스트림 · 컬렉션

- 스트림은 읽기 쉬울 때만. 단계가 4~5개를 넘거나 안에서 예외를 던지면 for 문이 낫다
- 스트림 안에서 바깥 변수를 바꾸지 않는다
- 리포지토리 호출을 반복문 안에서 하지 않는다(N+1). 한 번에 가져와 `Map` 으로 묶는다

---

### 응답 · 오류

- 모든 응답은 `ApiResponse` 로 감싼다. 성공은 `ApiResponse.success(data)`,
  코드가 필요하면 `ApiResponse.toResponseEntity(ResultCode, data)`
- 새 결과 코드는 `ResultCode` 에 문구와 함께 더한다(2차는 한국어만. 다국어 문구는 3차에)
- 컨트롤러가 **엔티티를 그대로 내보내지 않는다.** 응답 DTO(record)로 바꿔서 낸다
  — 지연 로딩 예외 · 무한 순환 · 내부 칸 노출을 막는다
- 예외는 한곳(`@RestControllerAdvice`)에서 `ResultCode` 로 바꾼다

---

### 엔티티 (JPA)

| 규칙 | 이유 |
|---|---|
| `@NoArgsConstructor(access = PROTECTED)` | JPA 용 기본 생성자, 밖에서 빈 객체 못 만들게 |
| `@Setter` · `@Data` 금지 | 아무 데서나 상태를 바꾸지 못하게. `@Data` 는 equals/hashCode 가 연관을 타고 돈다 |
| 생성은 `@Builder`(생성자에) 또는 정적 팩토리 | |
| 상태 변경은 이름 있는 메서드 | `worry.hide("AI: 광고")` · `worry.show()` — 규칙이 엔티티 안에 모인다 |
| 연관은 모두 `fetch = LAZY` | `@ManyToOne` 기본값(EAGER)을 반드시 바꾼다 |
| enum 은 `@Enumerated(EnumType.STRING)` | ORDINAL 은 순서를 바꾸면 데이터가 뒤섞인다 |
| 시각은 `BaseTimeEntity` 상속 | `@CreatedDate` · `@LastModifiedDate`, `@EnableJpaAuditing` |
| 양방향은 필요할 때만 | 부모가 자식을 함께 저장 · 삭제할 때(`cascade = ALL`, `orphanRemoval = true`). 편의 메서드로 양쪽을 맞춘다 |
| 컬렉션은 선언할 때 초기화 | `private List<ActivityUrl> urls = new ArrayList<>();` |
| 복합키는 `@EmbeddedId` + `@MapsId` | `ScheduleActivity` (schedule_id + activity_id) |
| "한 사람 한 번"은 유니크 제약으로 | 코드로 먼저 조회하고 넣으면 동시 요청에 뚫린다 |

#### 이름 짓기

- **엔티티 클래스**: 단수형, 대문자로 시작하는 낙타 표기(PascalCase). 예: `ThoughtTip`
- **테이블 이름**: Spring Boot 가 엔티티 이름을 스네이크 표기(snake_case)로 바꾼다. 예: `thought_tip`.
  그래서 `@Table` 은 거의 쓰지 않는다
- **연결 테이블**: 칸이 두 키뿐이면 `@ManyToMany` 로 처리하고 `부모_자식` 꼴로 짓는다.
  칸이 더 있으면 따로 엔티티를 만들고 역할이 드러나는 이름을 붙인다

---

### 조회 · 트랜잭션

- 서비스 클래스에 `@Transactional(readOnly = true)`, 쓰는 메서드에만 `@Transactional`
- `spring.jpa.open-in-view: false` — 트랜잭션 밖에서 지연 로딩하지 않는다
- 목록은 N+1 을 막는다: fetch join · `@EntityGraph` · `default_batch_fetch_size: 100`
- 숫자(`like_count` · `use_count` · `done_count`)는 호출마다 수정하지 않는다 — 인기 글에 몰려도 로우 락(row lock)으로 기다리지 않게
  - 활동 수(`use_count` · `done_count` · `helped_count`): 원본(`app_done_log` · `app_helped_log`)에 쌓고, `@Scheduled` 가 1시간마다 활동별로 다시 센다(사용자 수는 앱 키를 겹치지 않게) — 집계 기준 시점을 따로 저장하지 않아 누락되거나 중복 집계될 가능성이 없다
  - 좋아요(`like_count`): 취소하면 `honey_like` 행을 지워서 더하기로는 줄어든 걸 모른다 — `@Scheduled` 가 1분마다 다시 세어 맞춘다(1분 동안은 실제 좋아요 수와 다를 수 있다) · 글의 `like_count` 는 `UPDATE` 쿼리로 갱신한다 — 엔티티로 고치면 글의 수정일까지 바뀐다
- 지운 글은 `deleted_at` 으로 숨긴다(`@SQLRestriction("deleted_at is null")`)

---

### DB 변경 — Flyway

- 테이블은 Flyway 가 만든다. `spring.jpa.hibernate.ddl-auto: validate`
- 파일: `src/main/resources/db/migration/V<번호>__<설명>.sql` (밑줄 두 개)
- **한 번 돌린 파일은 고치지 않는다.** 바꿀 게 있으면 다음 번호로 새 파일
- 엔티티를 고치면 같은 묶음에 SQL 파일도 낸다. 둘이 짝
- 운영 DB 에 손으로 `ALTER` 하지 않는다

---

### 테스트 코드

- 이름은 규칙 문장으로: `@DisplayName("본인 글만 숨길 수 있다")`
- 본문은 **given / when / then** 세 덩어리로 나눈다
- 테스트 하나에 확인 하나(관련된 assert 여러 개는 괜찮다)
- 단언은 AssertJ: `assertThat(worry.getStatus()).isEqualTo(Status.HIDDEN)`
- 예외는 `assertThatThrownBy(() -> worryService.hide(worryId, otherId)).isInstanceOf(BusinessException.class)`

---

### 테스트 — 무엇을 얼마나

- **규칙이 있는 곳만.** 20~30개면 된다

| 종류 | 대상 |
|---|---|
| 단위 | 엔티티 · 서비스 규칙 (숨김, 본인 확인, 한 번만 누르기, 탈퇴) |
| `@DataJpaTest` + Testcontainers(PostgreSQL) | 직접 짠 쿼리, 유니크 제약, N+1 |
| `MockMvc` | 권한 (비로그인 401, 남의 글 수정 403, 앱 키 틀림 401) |

- 기능과 **같은 묶음**으로 낸다. 끝에 몰아서 하지 않는다
- getter · 단순 저장/조회 · 화면은 테스트하지 않는다

---

### 커밋 메시지

- 한 커밋에 한 가지. 엔티티 + 마이그레이션 + 테스트는 한 묶음
- 첫 줄은 무엇을 했는지 한국어로 짧게: `회원 엔티티와 V1 마이그레이션, 소셜 계정 중복 테스트를 더한다`
- 이유가 필요하면 한 줄 띄우고 본문에

---

### 도구 (선택)

- **.editorconfig**: 들여쓰기 · 줄 끝 · 인코딩을 맞춘다
- **Spotless** 또는 **Checkstyle**: 위 모양 규칙을 빌드에서 검사한다. 처음엔 없이 시작하고, 규칙이 자리 잡으면 붙인다

---

## 2부. 이번 프로젝트 — honeysalt-server

### 설정값 · 포트폴리오 관련 설정

Java 21 · Spring Boot 3.5 · JPA.

1. **포트폴리오로 공개하면서 작업한다.**
   - git 저장소는 공개(public)로 쓴다.
   - 화면 주소는 `/{lang}/wisdom` 이다. 기능이 완성될 때까지 메뉴에 걸지 않는다.
   - 설정 파일(`application*.yml`)은 `.gitignore` 로 저장소에서 뺀다.
   - 비밀값은 환경변수나 jasypt `ENC(...)` 로 관리하고 저장소에 커밋하지 않는다.
   - 서버 주소 · 계정처럼 보안에 위험이 되는 내용은 주석에도 남기지 않는다.

---

### 서버 설정

- CSRF 토큰은 `CookieCsrfTokenRepository` 를 쓰고, 쿠키 도메인은 `.honeysaltlove.com` 으로 둔다.
- nginx 뒤에서 돌므로 `server.forward-headers-strategy: framework` — 없으면 OAuth 콜백이 `http://` 로 나간다.

---

### 시간 · 언어

- 시각은 **UTC 로 저장하고, 화면이 보는 사람 시간대로 바꿔 보여 준다**
  - 엔티티는 `Instant`(`LocalDateTime` ✕), `hibernate.jdbc.time_zone: UTC`, API 는 ISO 8601(`2026-11-02T09:00:00Z`)
- "그날"을 뜻하는 칸(`done_on` 등)은 `LocalDate` — 앱이 보낸 그 사람의 날짜를 그대로 저장한다
- 지금은 **한국어만** 연다. 그래도 `lang` 칸은 채운다(`ko`) — 3차 다국어 때 번역 테이블만 더하면 되게
- 사용자가 쓴 글은 원문 그대로 저장하고, 번역은 따로 둔다(3차)

---

### 문서

- 테이블 · 엔티티를 바꾸면 [설계 문서](design-doc.html)도 같이 고친다. 원본은 포트폴리오 저장소의 `portfolio/honeysaltlove/v2/index.html` 이고, 수정한 뒤 이 문서에 복사해 반영한다
- 주요 기술 선택과 그 이유는 README 에 기록한다
