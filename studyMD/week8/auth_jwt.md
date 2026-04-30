# 2단계: Auth 구현 (회원가입 / 로그인 / JWT)

## 왜 필요한가

1단계에서 만든 공통 응답 형식을 바탕으로, 프론트가 제일 먼저 호출하는 회원가입·로그인 API를 만든다.  
로그인에 성공하면 JWT 토큰을 발급해서 내려주고, 이후 요청마다 그 토큰으로 "누가 보낸 요청인지" 확인한다.

```
// 프론트(auth_api.dart)가 기대하는 흐름

POST /api/v1/auth/signup   → { email, password }
POST /api/v1/auth/login    → { email, password }
                              응답: { "data": { "token": "eyJ..." } }

이후 모든 요청 헤더:
Authorization: Bearer eyJ...
```

JWT가 없으면 Schedule, Todo, Account 탭을 테스트할 수 없으므로 Auth를 먼저 완성한다.

---

## 만들 파일 목록

```
auth/
  controller/
    AuthController.java
  service/
    AuthService.java
  dto/
    SignupRequest.java
    LoginRequest.java
    LoginResponse.java
  jwt/
    JwtProvider.java       ← 토큰 생성·검증
    JwtFilter.java         ← 요청마다 토큰 확인 (OncePerRequestFilter)

config/
  SecurityConfig.java      ← Spring Security 기본 설정
```

---

## 의존성 추가 (build.gradle.kts)

```kotlin
// JWT
implementation("io.jsonwebtoken:jjwt-api:0.12.6")
runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

// Spring Security
implementation("org.springframework.boot:spring-boot-starter-security")
```

- `jjwt-api` : JWT를 만들고 파싱하는 API.
- `jjwt-impl` / `jjwt-jackson` : 런타임에만 필요한 구현체. `runtimeOnly`로 충분하다.
- `spring-boot-starter-security` : Spring Security 전체를 프로젝트에 추가한다.  
  추가하는 순간 모든 URL이 자동으로 인증 필요 상태가 되므로, `SecurityConfig`에서 허용 경로를 직접 열어줘야 한다.

---

## application.yml에 시크릿 키 추가

```yaml
jwt:
  secret: my-super-secret-key-that-is-at-least-32-characters-long
  expiration-ms: 86400000   # 24시간
```

- HS256 알고리즘은 시크릿 키가 **최소 32바이트**여야 한다.
- 실제 프로젝트에서는 환경 변수로 주입하고 코드에 절대 커밋하지 않는다.

---

## DTO

### SignupRequest.java

```java
package com.example.mini_spring.auth.dto;

public class SignupRequest {
    private String email;
    private String password;

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
```

### LoginRequest.java

```java
package com.example.mini_spring.auth.dto;

public class LoginRequest {
    private String email;
    private String password;

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
```

### LoginResponse.java

```java
package com.example.mini_spring.auth.dto;

public class LoginResponse {
    private final String token;

    public LoginResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
```

프론트가 `response['data']['token']` 으로 토큰을 꺼내므로,  
`LoginResponse`를 `ApiResponse.of()`로 감싸면 `{ "data": { "token": "..." } }` 형태가 된다.

---

## JwtProvider.java

### 역할
JWT 토큰을 **생성**하고, 들어온 토큰에서 **사용자 이메일을 꺼내거나 유효성을 검증**하는 유틸 클래스.

### 코드

```java
package com.example.mini_spring.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    // 이메일을 claim에 담아서 토큰 생성
    public String generate(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    // 토큰에서 이메일 꺼내기
    public String getEmail(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // 토큰이 유효한지 확인 (만료·변조 포함)
    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 포인트

**JWT 구조 (Base64로 인코딩된 3덩어리)**
```
eyJhbGciOiJIUzI1NiJ9          ← Header  (알고리즘 정보)
.eyJzdWIiOiJob25nQGV4LmNvbSJ9  ← Payload (claim = 저장한 데이터)
.SflKxwRJSMeKKF2QT4fwpMeJf36P  ← Signature (변조 감지용 서명)
```

**`.subject(email)`**  
Payload의 `sub` claim에 이메일을 넣는다. `getSubject()`로 꺼낼 수 있다.

**`Keys.hmacShaKeyFor(secret.getBytes())`**  
문자열 시크릿을 HMAC-SHA 알고리즘에 맞는 키 객체로 변환한다.  
이 키로 서명하고, 같은 키로 검증한다.

**`isValid()`에서 try-catch를 쓰는 이유**  
만료됐거나 서명이 틀리면 jjwt가 예외를 던진다. 그 예외를 잡아서 `false`로 변환하는 것이다.

---

## AuthService.java

### 역할
회원가입 시 비밀번호를 BCrypt로 암호화해서 저장하고, 로그인 시 비밀번호를 검증한 뒤 JWT를 발급한다.

### 코드

```java
package com.example.mini_spring.auth.service;

import com.example.mini_spring.auth.dto.LoginRequest;
import com.example.mini_spring.auth.dto.LoginResponse;
import com.example.mini_spring.auth.dto.SignupRequest;
import com.example.mini_spring.auth.jwt.JwtProvider;
import com.example.mini_spring.member.domain.Member;
import com.example.mini_spring.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(MemberRepository memberRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public void signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        Member member = new Member(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())  // 암호화 저장
        );
        memberRepository.save(member);
    }

    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtProvider.generate(member.getEmail());
        return new LoginResponse(token);
    }
}
```

### 포인트

**`passwordEncoder.encode()` vs `passwordEncoder.matches()`**  
- `encode("1234")` → `"$2a$10$..."`  (BCrypt 해시, 매번 달라짐)  
- `matches("1234", "$2a$10$...")` → `true`  
  평문을 다시 해시해서 비교하는 것이 아니라, BCrypt 알고리즘 특성을 이용해 내부적으로 비교한다.  
  그래서 같은 비밀번호라도 `encode` 결과가 달라도 `matches`는 `true`를 반환한다.

**`memberRepository.existsByEmail()`**  
이 메서드는 직접 만들어야 한다. `MemberRepository`에 아래 한 줄만 추가하면 Spring Data JPA가 자동으로 만들어준다:
```java
boolean existsByEmail(String email);
```

**`memberRepository.findByEmail()`도 마찬가지:**
```java
Optional<Member> findByEmail(String email);
```

---

## AuthController.java

```java
package com.example.mini_spring.auth.controller;

import com.example.mini_spring.auth.dto.LoginRequest;
import com.example.mini_spring.auth.dto.LoginResponse;
import com.example.mini_spring.auth.dto.SignupRequest;
import com.example.mini_spring.auth.service.AuthService;
import com.example.mini_spring.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.of(authService.login(request));
    }
}
```

- `signup`은 반환할 데이터가 없으므로 `ResponseEntity<Void>`로 201 Created만 내려준다.
- `login`은 토큰을 담은 `LoginResponse`를 `ApiResponse`로 감싸서 `{ "data": { "token": "..." } }` 형태로 반환한다.

---

## SecurityConfig.java

### 역할
Spring Security가 기본으로 모든 URL을 막아버리므로,  
회원가입·로그인 URL은 열어두고 나머지는 JWT가 있어야만 접근할 수 있게 설정한다.  
`JwtFilter`도 여기서 Security 필터 체인에 등록한다.

### 코드

```java
package com.example.mini_spring.config;

import com.example.mini_spring.auth.jwt.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                       // REST API는 CSRF 불필요
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 미사용
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()  // 회원가입·로그인은 인증 없이 허용
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger
                .anyRequest().authenticated()                   // 나머지는 인증 필요
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // JWT 필터 등록

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 포인트

**왜 `csrf.disable()`?**  
CSRF 공격은 브라우저가 쿠키를 자동으로 붙여주는 특성을 이용한다.  
JWT를 Authorization 헤더로 전달하는 방식에서는 브라우저가 자동으로 헤더를 붙이지 않으므로 CSRF 공격이 불가능하다. 따라서 불필요한 보호를 꺼둔다.

**`SessionCreationPolicy.STATELESS`**  
서버가 세션을 만들지 않겠다는 설정. JWT 방식에서는 서버가 상태를 기억하지 않고 토큰으로만 인증하므로 세션이 필요 없다.

**`addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)`**  
Spring Security의 필터 체인에 우리가 만든 `JwtFilter`를 끼워 넣는다.  
`UsernamePasswordAuthenticationFilter` 앞에 넣으면 요청이 Spring Security의 기본 인증 처리에 닿기 전에 JWT를 먼저 확인한다.

---

## JwtFilter.java

### 역할
모든 요청이 컨트롤러에 도달하기 전에 실행된다.  
`Authorization: Bearer <token>` 헤더에서 토큰을 꺼내 유효성을 확인하고,  
유효하면 `SecurityContextHolder`에 인증 정보를 저장해서 이후 처리가 "인증된 요청"으로 진행되게 한다.

### 코드

```java
package com.example.mini_spring.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);  // "Bearer " 이후 토큰 부분만 잘라냄

            if (jwtProvider.isValid(token)) {
                String email = jwtProvider.getEmail(token);

                // Spring Security에 "이 요청은 email을 가진 사람이 보냈다"고 알려줌
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);  // 다음 필터(또는 컨트롤러)로 넘김
    }
}
```

### 포인트

**`OncePerRequestFilter`란?**  
요청 하나당 딱 한 번만 실행되는 필터 추상 클래스. 상속받아서 `doFilterInternal()`만 구현하면 된다.

**`SecurityContextHolder.getContext().setAuthentication(...)`**  
Spring Security가 "현재 요청의 인증 정보"를 보관하는 공간.  
여기에 인증 객체를 넣으면, 이후 컨트롤러에서 `@AuthenticationPrincipal`로 꺼낼 수 있다.

**`UsernamePasswordAuthenticationToken(email, null, List.of())`**  
- 첫 번째 인자 : principal (이메일 문자열)  
- 두 번째 인자 : credentials (비밀번호, 여기선 불필요하므로 null)  
- 세 번째 인자 : authorities (권한 목록, 아직 역할 구분 없으므로 빈 리스트)  
**세 번째 인자를 넣어야** `isAuthenticated()`가 `true`가 된다. 빠트리면 인증된 것으로 처리되지 않는다.

**`filterChain.doFilter(request, response)` 반드시 호출**  
이 줄이 없으면 요청이 컨트롤러까지 전달되지 않고 여기서 멈춘다.  
토큰이 없거나 유효하지 않아도 호출해야 한다(Security가 뒤에서 인증 여부를 보고 403을 낸다).

---

## Member 도메인 수정

`AuthService`에서 이메일과 암호화된 비밀번호로 `Member`를 만들 수 있어야 한다.  
기존 `Member` 생성자가 이메일·이름·비밀번호를 받는 구조라면 아래처럼 수정하거나 생성자를 추가한다.

```java
// Member.java에 생성자 추가
public Member(String email, String password) {
    this.email = email;
    this.password = password;
}
```

`MemberRepository.java`에 쿼리 메서드 2개 추가:

```java
Optional<Member> findByEmail(String email);
boolean existsByEmail(String email);
```

---

## 완성 후 확인

### 회원가입 (POST /api/v1/auth/signup)

```json
// 요청
{ "email": "hong@example.com", "password": "1234" }

// 응답 (201 Created, body 없음)
```

### 로그인 (POST /api/v1/auth/login)

```json
// 요청
{ "email": "hong@example.com", "password": "1234" }

// 응답
{
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJob25nQ..."
  }
}
```

### 인증 없이 보호된 URL 접근

```json
// GET /api/v1/members (토큰 없이)
// 응답 (403 Forbidden)
```

---

## 흐름 정리

```
[회원가입]
요청 (email, password)
    ↓
AuthController.signup()
    ↓
AuthService.signup()
    → 이메일 중복 확인
    → BCrypt로 비밀번호 암호화
    → DB 저장
    ↓
201 응답

[로그인]
요청 (email, password)
    ↓
AuthController.login()
    ↓
AuthService.login()
    → DB에서 이메일로 회원 조회
    → BCrypt 비밀번호 검증
    → JwtProvider.generate(email)
    ↓
{ "data": { "token": "eyJ..." } }

[이후 모든 요청]
요청 헤더: Authorization: Bearer eyJ...
    ↓
JwtFilter.doFilterInternal()
    → 헤더에서 토큰 추출
    → JwtProvider.isValid() 검증
    → SecurityContextHolder에 인증 정보 저장
    ↓
컨트롤러 (인증된 요청으로 처리)
```

---

## 다음 단계 예고

3단계에서는 `SecurityContextHolder`에 저장한 인증 정보를 컨트롤러에서 꺼내서  
"현재 로그인한 사람의 정보"를 조회하는 `/api/v1/members/me` API를 만든다.

```java
// 3단계 예시
@GetMapping("/me")
public ApiResponse<MemberResponse> getMe(@AuthenticationPrincipal String email) {
    return ApiResponse.of(memberService.getMemberByEmail(email));
}
```
