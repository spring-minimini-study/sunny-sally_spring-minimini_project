# sunny-sally Spring Minimini Project

Spring Boot를 처음 배우면서 직접 만들어보는 학습용 백엔드 프로젝트입니다.  
Flutter 앱 `segye`의 백엔드를 만들기 전, Spring의 계층형 구조를 익히는 것이 목적입니다.

---

## segye 프로젝트 소개

`segye`는 Flutter로 만들어진 개인 다이어리형 애플리케이션입니다.  
일정, 할 일, 소비 내역을 날짜 중심으로 관리합니다.

| 기능 | 설명 |
|------|------|
| 회원가입 / 로그인 | 이메일 기반 인증 |
| 캘린더 기반 메인 화면 | 날짜 중심 UI |
| 일정 관리 | 날짜별 schedule 등록/조회 |
| 할 일 관리 | 날짜별 todo 등록/조회 |
| 소비 내역 | 수입/지출 기록 및 카테고리 분류 |
| 마이페이지 | 사용자 정보 확인 |

---

## 이 프로젝트의 위치

```
front/segye                        ← Flutter 프론트엔드
sunny-sally_spring-minimini_project ← Spring 학습용 백엔드 (이 프로젝트)
```

실제 `segye` 백엔드를 만들기 전에, Spring의 기본 흐름을 익히는 연습 공간입니다.

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.3 |
| ORM | Spring Data JPA (Hibernate) |
| Database | H2 (In-Memory) |
| Build | Gradle (Kotlin DSL) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Utility | Lombok |

---

## 프로젝트 구조

```
src/main/java/com/example/mini_spring/
└── member/
    ├── controller/
    │   └── MemberController.java    # HTTP 요청/응답 처리
    ├── service/
    │   └── MemberService.java       # 비즈니스 로직
    ├── repository/
    │   └── MemberRepository.java    # DB 접근
    ├── domain/
    │   └── Member.java              # JPA 엔티티
    └── dto/
        ├── MemberCreateRequest.java # 회원 생성 요청 DTO
        └── MemberResponse.java      # 응답 DTO
```

Spring의 계층형 아키텍처를 직접 연습하는 구조입니다.

```
클라이언트 요청 → Controller → Service → Repository → H2 DB
```

---

## API

### Member

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/members` | 회원 생성 |
| GET | `/members/{id}` | 회원 단건 조회 |

**POST /members 요청 예시**
```json
{
  "name": "홍길동",
  "email": "hong@example.com",
  "password": "1234"
}
```

**응답 예시**
```json
{
  "id": 1,
  "name": "홍길동",
  "email": "hong@example.com"
}
```

> 이메일 중복 시 예외가 발생합니다. (비밀번호는 응답에 포함되지 않습니다.)

---

## 실행 방법

```bash
# 프로젝트 빌드 및 실행
./gradlew bootRun
```

- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa` / Password: _(없음)_
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html

---

## 학습 목표

이 프로젝트를 통해 익히는 것들:

- Spring Boot의 Controller / Service / Repository 계층 구조
- JPA Entity와 DTO의 역할 분리
- Spring Data JPA (`JpaRepository`) 사용법
- Query Method (`existsByEmail`) 동작 원리
- H2 인메모리 DB를 활용한 빠른 개발/테스트

---

## segye 백엔드 확장 계획

member 기능을 기반으로 아래 도메인으로 확장할 예정입니다.

| 도메인 | 설명 |
|--------|------|
| `auth` | 회원가입, 로그인, JWT |
| `member` | 사용자 정보 (현재 구현 중) |
| `schedule` | 일정 관리 |
| `todo` | 할 일 관리 |
| `account` | 수입/지출 기록 |
| `category` | 소비 카테고리 관리 |
| `common` | 예외 처리, 공통 응답 |

---

## 학습 기록

주차별 학습 내용은 `studyMD/` 폴더에 정리되어 있습니다.

| 주차 | 주요 내용 |
|------|-----------|
| Week 3 | Spring Boot 기초 |
| Week 4 | Git 명령어, 커밋 규칙, 설정 |
| Week 6 | SOLID 원칙 |
| Week 7 | JPA 구조 탐구, Docker 개념 |
