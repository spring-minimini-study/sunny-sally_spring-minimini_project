# segye 백엔드 구현 계획

프론트엔드(`front/segye_world`)와 현재 학습 프로젝트를 분석해서 정리한 구현 계획.

---

## 현재 상태 파악

### 프론트엔드가 기대하는 것

| 항목 | 내용 |
|------|------|
| Base URL | `http://10.0.2.2:8080` (Android 에뮬레이터 → localhost) |
| 인증 방식 | JWT Bearer 토큰 (`Authorization: Bearer <token>`) |
| 토큰 저장 | `flutter_secure_storage` |
| 공통 응답 형식 | `{ "data": { ... } }` |

**프론트가 호출하는 API (auth_api.dart 기준)**

```
POST /api/v1/auth/signup   → { email, password }
POST /api/v1/auth/login    → { email, password } → 응답: { data: { token: "..." } }
```

**메인 화면 3개 탭 (main_screen.dart)**

| 탭 | 표시 데이터 | 필드 |
|----|------------|------|
| 일정 | schedule | label, timeRange, color |
| 할 일 | todo | label, done(체크박스) |
| 소비 | account | title, category(수입/지출), amount |

---

## 백엔드 구현 순서

복잡한 것부터 하면 막히니까, 프론트와 연결되는 순서로 한 단계씩 완성한다.

---

### 1단계: 공통 응답 형식 만들기 (`common`)

프론트가 `response.data['data']['token']` 형태로 꺼낸다.  
모든 API가 같은 형식으로 응답해야 한다.

**만들 것:**

```java
// ApiResponse<T>
{
  "data": { ... }      // 성공
}

// ErrorResponse
{
  "message": "이미 사용 중인 이메일입니다."   // 실패
}
```

**파일 위치:**
```
common/
  response/
    ApiResponse.java
  exception/
    GlobalExceptionHandler.java   // @RestControllerAdvice
```

**배울 것:**
- 제네릭 `ApiResponse<T>`
- `@RestControllerAdvice`로 예외 전역 처리
- `@ExceptionHandler`로 예외 종류별 응답 분기

---

### 2단계: Auth 구현 (`auth`)

프론트에서 회원가입/로그인 API를 제일 먼저 쓴다. JWT가 없으면 나머지를 테스트할 수 없다.

**만들 API:**

```
POST /api/v1/auth/signup   회원가입
POST /api/v1/auth/login    로그인 → JWT 토큰 반환
```

**응답 형식 (login):**
```json
{
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

**파일 위치:**
```
auth/
  controller/AuthController.java
  service/AuthService.java
  dto/
    SignupRequest.java
    LoginRequest.java
    LoginResponse.java   // token 필드
  jwt/
    JwtProvider.java     // 토큰 생성/검증
    JwtFilter.java       // 요청마다 토큰 확인
```

**의존성 추가 필요 (build.gradle.kts):**
```kotlin
implementation("io.jsonwebtoken:jjwt-api:0.12.6")
runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
implementation("org.springframework.boot:spring-boot-starter-security")
```

**배울 것:**
- Spring Security 기본 설정
- JWT 생성 (`HS256` 알고리즘, 만료 시간)
- `OncePerRequestFilter`로 토큰 검증 필터 만들기
- `SecurityContextHolder`에 인증 정보 저장

---

### 3단계: Member 확장 (`member`)

현재 `POST /members`, `GET /members/{id}`만 있다.  
Auth와 연결해서 "현재 로그인한 사람의 정보 조회" 형태로 바꾼다.

**바꿀 것:**

```
GET /api/v1/members/me   → JWT에서 사용자 꺼내서 내 정보 반환
```

**배울 것:**
- `@AuthenticationPrincipal`로 현재 사용자 꺼내기
- 비밀번호 암호화 (`BCryptPasswordEncoder`)

---

### 4단계: Schedule 구현 (`schedule`)

메인 화면 "일정" 탭에 표시되는 데이터.

**만들 API:**

```
POST   /api/v1/schedules              일정 생성
GET    /api/v1/schedules?date=2025-09-09   날짜별 일정 조회
DELETE /api/v1/schedules/{id}         일정 삭제
```

**Entity 필드:**
```java
Long id
Member member       // 누구의 일정인지
String label        // 일정 이름
LocalDate date      // 날짜
LocalTime startTime // 시작 시간
LocalTime endTime   // 종료 시간
String color        // 색상 (hex 코드 등)
```

**배울 것:**
- `@ManyToOne`으로 Member와 연관관계 설정
- `LocalDate`, `LocalTime` JPA 매핑
- 날짜 파라미터로 조회하는 Query Method

---

### 5단계: Todo 구현 (`todo`)

메인 화면 "할 일" 탭에 표시되는 데이터.

**만들 API:**

```
POST   /api/v1/todos              할 일 생성
GET    /api/v1/todos?date=2025-09-09   날짜별 할 일 조회
PATCH  /api/v1/todos/{id}/done    완료 처리
DELETE /api/v1/todos/{id}         삭제
```

**Entity 필드:**
```java
Long id
Member member
String label
LocalDate date
boolean done    // 체크박스 상태
```

**배울 것:**
- `PATCH`로 부분 업데이트 처리
- boolean 필드 토글

---

### 6단계: Account 구현 (`account`)

메인 화면 "소비" 탭 + CASH 페이지에 표시되는 데이터.

**만들 API:**

```
POST   /api/v1/accounts              내역 추가
GET    /api/v1/accounts?date=2025-09-09   날짜별 내역 조회
DELETE /api/v1/accounts/{id}          삭제
```

**Entity 필드:**
```java
Long id
Member member
String title      // 내역 이름 (예: 월급, 점심)
String category   // 수입 / 지출
int amount        // 양수 = 수입, 음수 = 지출
LocalDate date
```

**배울 것:**
- 수입/지출 구분 처리 방식 (Enum vs String)
- 날짜별 합계 계산 (JPQL 또는 스트림)

---

## URL 구조 정리

```
/api/v1/auth/signup       POST  회원가입
/api/v1/auth/login        POST  로그인

/api/v1/members/me        GET   내 정보 조회

/api/v1/schedules         POST  일정 생성
/api/v1/schedules?date=   GET   날짜별 일정 조회
/api/v1/schedules/{id}    DELETE 삭제

/api/v1/todos             POST  할 일 생성
/api/v1/todos?date=       GET   날짜별 할 일 조회
/api/v1/todos/{id}/done   PATCH 완료 처리
/api/v1/todos/{id}        DELETE 삭제

/api/v1/accounts          POST  내역 추가
/api/v1/accounts?date=    GET   날짜별 조회
/api/v1/accounts/{id}     DELETE 삭제
```

---

## 프로젝트 구조 목표

```
src/main/java/com/example/mini_spring/
├── common/
│   ├── response/
│   │   └── ApiResponse.java
│   └── exception/
│       └── GlobalExceptionHandler.java
├── auth/
│   ├── controller/AuthController.java
│   ├── service/AuthService.java
│   ├── dto/
│   └── jwt/
├── member/
│   ├── controller/MemberController.java   ← 기존 + /me 추가
│   ├── service/MemberService.java
│   ├── repository/MemberRepository.java
│   ├── domain/Member.java
│   └── dto/
├── schedule/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── domain/
│   └── dto/
├── todo/
│   └── (동일 구조)
└── account/
    └── (동일 구조)
```

---

## 프론트 코드에서 발견한 주의사항

- `auth_api.dart`의 `ApiConfig.apiPrefix`가 `/api/v1/auth/login`으로 설정되어 있고 여기에 또 `/auth/login`을 붙이고 있어서 경로가 이상하다. 백엔드를 만들면서 프론트의 실제 요청 경로를 확인하고 맞춰야 한다.
- 로그인 응답에서 토큰을 꺼내는 경로가 `response['data']['token']`이므로, 공통 응답 형식에서 `data` 키 안에 실제 payload가 있어야 한다.
- 프론트는 모든 인증 필요 요청에 `Authorization: Bearer <token>` 헤더를 자동으로 붙인다 (`auth_interceptor.dart`).

---

## 단계별 체크리스트

- [ ] 1단계: `ApiResponse<T>` + `GlobalExceptionHandler`
- [ ] 2단계: 회원가입 + 로그인 + JWT
- [ ] 3단계: `/members/me` + 비밀번호 암호화
- [ ] 4단계: Schedule CRUD
- [ ] 5단계: Todo CRUD
- [ ] 6단계: Account CRUD
