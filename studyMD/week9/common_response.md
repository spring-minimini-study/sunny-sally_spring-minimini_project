# 1단계: 공통 응답 형식 만들기

## 왜 필요한가

지금 `MemberController`는 성공하면 `MemberResponse` 객체를 그냥 반환하고,  
실패하면 Spring이 알아서 에러 응답을 만들어서 내려준다.

```json
// 지금 성공 응답
{ "id": 1, "name": "홍길동", "email": "hong@example.com" }

// 지금 실패 응답 (Spring 기본값 - 형식이 제멋대로)
{ "timestamp": "...", "status": 400, "error": "Bad Request", "path": "/members" }
```

프론트(`auth_api.dart`)는 성공 응답을 이렇게 꺼낸다:
```dart
final data = payload['data'];         // 'data' 키를 먼저 꺼내고
final token = data['token'];          // 그 안에서 token을 꺼냄
```

즉, 백엔드가 항상 `{ "data": { ... } }` 형태로 내려줘야 프론트가 제대로 동작한다.  
실패할 때도 `{ "message": "..." }` 형태로 통일해야 프론트에서 에러 처리가 가능하다.

**목표 응답 형식:**
```json
// 성공
{ "data": { "id": 1, "name": "홍길동", "email": "hong@example.com" } }

// 실패
{ "message": "이미 사용 중인 이메일입니다." }
```

---

## 만들 파일 목록

```
common/
  response/
    ApiResponse.java          ← 성공 응답 래퍼
  exception/
    GlobalExceptionHandler.java   ← 실패 응답 전역 처리
```

---

## ApiResponse.java

### 역할
서비스에서 반환한 데이터를 `{ "data": ... }` 형태로 감싸주는 클래스.

### 코드
```java
package com.example.mini_spring.common.response;

public class ApiResponse<T> {

    private final T data;

    private ApiResponse(T data) {
        this.data = data;
    }

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data);
    }

    public T getData() {
        return data;
    }
}
```

### 포인트
- `<T>`가 제네릭 타입 파라미터다. `T` 자리에 `MemberResponse`, `LoginResponse` 등 어떤 타입이든 들어올 수 있다.
- 생성자를 `private`으로 막고 `of()`라는 정적 팩토리 메서드로만 만들 수 있게 했다.
  - `new ApiResponse<>(data)` 대신 `ApiResponse.of(data)` 형태로 쓰게 만드는 패턴.
- Jackson이 직렬화할 때 `getData()`를 보고 `"data"` 키를 만들어준다. (`getXxx()` → `"xxx"` 키)

### 사용 방법
컨트롤러에서 반환 타입만 바꿔주면 된다:
```java
// 전
public MemberResponse createMember(@RequestBody MemberCreateRequest request) {
    return memberService.createMember(request);
}

// 후
public ApiResponse<MemberResponse> createMember(@RequestBody MemberCreateRequest request) {
    return ApiResponse.of(memberService.createMember(request));
}
```

---

## GlobalExceptionHandler.java

### 역할
컨트롤러에서 예외가 터지면 Spring이 이 클래스를 찾아서 적절한 응답을 만들어준다.  
`@RestControllerAdvice`를 붙이면 스프링이 "전역 예외 처리기"로 인식한다.

### 코드
```java
package com.example.mini_spring.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // IllegalArgumentException이 발생하면 이 메서드가 실행됨
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
    }

    // 처리 못 한 나머지 예외는 500으로 내려줌
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "서버 오류가 발생했습니다."));
    }
}
```

### 포인트

**`@RestControllerAdvice`란?**
- `@ControllerAdvice` + `@ResponseBody`를 합친 것.
- 모든 `@RestController`에서 발생하는 예외를 이 클래스 하나에서 잡아서 처리한다.
- 컨트롤러마다 try-catch를 쓰지 않아도 된다.

**`@ExceptionHandler`란?**
- 괄호 안에 적은 예외 클래스가 발생하면 이 메서드를 실행하라는 뜻.
- `IllegalArgumentException.class`를 적으면, 어디서든 `IllegalArgumentException`이 터지면 이 메서드로 온다.

**`ResponseEntity`란?**
- HTTP 응답 전체(상태 코드 + body + 헤더)를 직접 만들 수 있는 클래스.
- `.status(HttpStatus.BAD_REQUEST)` → HTTP 400
- `.body(Map.of(...))` → JSON body 설정

**왜 `Map.of("message", e.getMessage())`를 쓰나?**
- `{ "message": "이미 사용 중인 이메일입니다." }` 형태의 JSON을 만들기 위해.
- `Map.of()`는 Java 9부터 쓸 수 있는 불변 Map 생성 방법.
- 지금은 간단하게 Map으로 쓰지만, 나중에 `ErrorResponse` 클래스를 따로 만들어도 된다.

---

## MemberController 수정

두 파일을 만든 뒤, 기존 컨트롤러 반환 타입을 `ApiResponse`로 감싸준다.

```java
package com.example.mini_spring.member.controller;

import com.example.mini_spring.common.response.ApiResponse;
import com.example.mini_spring.member.dto.MemberCreateRequest;
import com.example.mini_spring.member.dto.MemberResponse;
import com.example.mini_spring.member.service.MemberService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ApiResponse<MemberResponse> createMember(@RequestBody MemberCreateRequest request) {
        return ApiResponse.of(memberService.createMember(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<MemberResponse> getMember(@PathVariable Long id) {
        return ApiResponse.of(memberService.getMember(id));
    }
}
```

서비스 코드는 바꾸지 않아도 된다. 컨트롤러 반환 타입만 바뀐다.

---

## 완성 후 확인

### 회원 생성 성공 (POST /members)
```json
// 요청
{ "name": "홍길동", "email": "hong@example.com", "password": "1234" }

// 응답 (전)
{ "id": 1, "name": "홍길동", "email": "hong@example.com" }

// 응답 (후)
{ "data": { "id": 1, "name": "홍길동", "email": "hong@example.com" } }
```

### 이메일 중복 실패 (POST /members)
```json
// 응답 (전) - Spring 기본 에러
{ "timestamp": "2026-04-29T...", "status": 500, "error": "Internal Server Error" }

// 응답 (후)
{ "message": "이미 사용 중인 이메일입니다." }
```

Swagger UI (`http://localhost:8080/swagger-ui/index.html`)나 curl로 확인하면 된다.

---

## 흐름 정리

```
요청 들어옴
    ↓
Controller
    ↓ 성공
Service → 데이터 반환
    ↓
ApiResponse.of(데이터) → { "data": { ... } }

    ↓ 실패 (예외 발생)
GlobalExceptionHandler가 잡아서
    ↓
{ "message": "에러 메시지" }
```

---

## 다음 단계에서 쓰는 방법

앞으로 새 도메인을 만들 때마다 컨트롤러에서 `ApiResponse.of()`로 감싸기만 하면 된다.  
예외는 `GlobalExceptionHandler`가 자동으로 처리한다.

```java
// auth 컨트롤러 예시 (2단계에서 만들 것)
@PostMapping("/login")
public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
    return ApiResponse.of(authService.login(request));
}
```
