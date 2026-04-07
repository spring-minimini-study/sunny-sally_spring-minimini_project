# member 폴더 정리

## 1. 목표
member 폴더는 사용자(Member) 관련 기능을 구현하는 공간이다.  
이 프로젝트에서는 Spring Boot 기본 구조를 익히는 것이 목적이므로,  
member 도메인부터 아래 흐름을 연습하는 것이 좋다.

클라이언트 요청  
-> Controller  
-> Service  
-> Repository  
-> Database(H2)

즉, member 폴더는 단순히 회원 기능을 넣는 곳이 아니라,  
Spring의 계층형 구조를 직접 연습하는 첫 번째 도메인이라고 생각하면 된다.

---

## 2. member 폴더 구조

    domain/member/
      controller/
        MemberController.java
      service/
        MemberService.java
      repository/
        MemberRepository.java
      domain/
        Member.java
      dto/
        MemberCreateRequest.java
        MemberResponse.java

구조 의미:
- controller: HTTP 요청/응답 처리
- service: 비즈니스 로직 처리
- repository: DB 접근
- domain: 엔티티 정의
- dto: 요청/응답 데이터 정의

---

## 3. 각 파일 역할

### (1) Member.java
**위치:**  
`domain/member/domain/Member.java`

**역할:**
- Member 엔티티를 정의하는 파일
- DB의 member 테이블과 연결되는 객체
- 회원 한 명의 데이터를 표현

**처음에는 아래 정도의 필드를 두면 된다.**
- id
- name
- email
- password

**핵심 포인트:**
- id는 PK
- email은 나중에 중복 검사에 사용 가능
- JPA 엔티티이므로 기본 생성자가 필요
- 실제 DB에 저장되는 데이터의 기준이 되는 클래스

---

### (2) MemberRepository.java
**위치:**  
`domain/member/repository/MemberRepository.java`

**역할:**
- Member 엔티티를 DB에 저장하거나 조회하는 역할
- JPA를 통해 DB 접근

처음에는 `JpaRepository<Member, Long>` 상속만으로도 충분하다.

**처음 사용할 기능:**
- `save()`
- `findById()`

**나중에 추가하면 좋은 것:**
- `existsByEmail(String email)`

**핵심 포인트:**
- 직접 SQL을 작성하지 않아도 기본 CRUD 가능
- DB 접근 책임은 Repository에 둔다

---

### (3) MemberCreateRequest.java
**위치:**  
`domain/member/dto/MemberCreateRequest.java`

**역할:**
- 회원 생성 요청을 받을 때 사용하는 DTO
- 클라이언트가 보내는 JSON 데이터를 담는 객체

**예시 필드:**
- name
- email
- password

**핵심 포인트:**
- 요청용 DTO
- 엔티티와 분리해서 사용
- 외부 입력값을 받는 객체

---

### (4) MemberResponse.java
**위치:**  
`domain/member/dto/MemberResponse.java`

**역할:**
- 회원 조회/생성 후 응답할 때 사용하는 DTO
- 클라이언트에게 반환할 데이터를 담는 객체

**예시 필드:**
- id
- name
- email

**핵심 포인트:**
- 응답용 DTO
- 보통 password는 응답에 포함하지 않음
- 엔티티를 그대로 반환하지 않기 위한 용도

---

### (5) MemberService.java
**위치:**  
`domain/member/service/MemberService.java`

**역할:**
- 실제 비즈니스 로직 처리
- 컨트롤러와 레포지토리 사이를 연결
- 요청 데이터를 엔티티로 바꾸고 저장/조회한 뒤 응답 DTO로 변환

**처음 구현할 메서드 예시:**
- `createMember(MemberCreateRequest request)`
- `getMember(Long memberId)`

**여기서 하는 일:**
- DTO -> Entity 변환
- 회원 저장
- 회원 조회
- Entity -> Response DTO 변환

**핵심 포인트:**
- 비즈니스 로직은 Service에 둔다
- Controller는 얇게 유지하고, 실제 처리 중심은 Service가 맡는다

---

### (6) MemberController.java
**위치:**  
`domain/member/controller/MemberController.java`

**역할:**
- HTTP 요청을 받는 입구
- 클라이언트 요청을 Service로 전달
- 처리 결과를 응답으로 반환

**처음 만들 API:**
- `POST /members`
- `GET /members/{id}`

**핵심 포인트:**
- 요청 받기
- Service 호출하기
- 응답 반환하기
- 복잡한 로직은 넣지 않기

---

## 4. 처음 구현할 기능

처음부터 수정/삭제까지 다 하지 말고,  
아래 2개부터 먼저 완성하는 것이 좋다.

### (1) 회원 생성
- `POST /members`
- 이름, 이메일, 비밀번호를 입력받아 저장

### (2) 회원 단건 조회
- `GET /members/{id}`
- 회원 id로 한 명 조회

**이유:**
- Spring의 기본 흐름을 익히기에 가장 적절함
- DTO, Entity, Service, Repository 흐름을 한 번에 연습 가능
- 너무 많은 기능을 한 번에 넣지 않아도 됨

---

## 5. 추천 구현 순서

1. Member 엔티티 만들기
2. MemberRepository 만들기
3. MemberCreateRequest 만들기
4. MemberResponse 만들기
5. MemberService 만들기
6. MemberController 만들기
7. API 테스트하기

이 순서로 가면 가장 덜 헷갈린다.

---

## 6. 구현하면서 익혀야 하는 핵심

### (1) Entity와 DTO를 구분하기
- Entity는 DB 저장용
- DTO는 요청/응답용

### (2) Controller와 Service를 구분하기
- Controller는 요청/응답 처리
- Service는 실제 로직 처리

### (3) Repository 책임 분리하기
- DB 접근만 담당
- 저장, 조회 책임 담당

### (4) 작은 기능부터 끝까지 구현하기
- member 생성
- member 조회

이 2개를 먼저 완성하면 이후 다른 도메인도 같은 패턴으로 확장 가능하다.

---

## 7. member 구현 후 다음 단계

member 기능이 어느 정도 끝나면,  
그 다음에는 아래 순서로 확장하면 좋다.

1. 이메일 중복 체크
2. 예외 처리
3. Validation 추가
4. 공통 응답 형식 만들기
5. 로그인/인증 구조 이해하기
6. 이후 schedule, todo, account 등으로 확장

즉, 지금 member는 연습용 첫 도메인이고,  
이 구조를 잘 잡아두면 뒤의 모든 기능 구현이 쉬워진다.

---

## 8. 한 줄 정리

member 폴더는  
사용자 정보를 다루는 기능을 구현하면서  
Spring Boot의 Controller / Service / Repository / Entity / DTO 구조를  
직접 연습하는 가장 기본적인 시작 도메인이다.