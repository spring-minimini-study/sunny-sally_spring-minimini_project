# sunny-sally spring minimini project

## 프로젝트 소개

이 프로젝트는 Spring Boot를 공부하기 위한 개인 학습용 프로젝트입니다.  
작은 CRUD와 기본적인 Spring 구조를 직접 구현해 보면서, 이후 `segye` 프로젝트의 백엔드를 차근차근 만들기 위한 기반을 다지는 것이 목적입니다.

즉, 이 저장소는 완성형 서비스 프로젝트라기보다 다음 단계를 준비하는 연습용 백엔드 저장소입니다.

## 학습 목표

- Spring Boot 기본 구조 익히기
- Controller / Service / Repository 계층 분리 연습
- JPA 엔티티와 H2 데이터베이스 사용 경험 쌓기
- REST API 설계와 구현 흐름 익히기
- 이후 `segye` 백엔드 구현을 위한 기초 다지기

## 현재 프로젝트 성격

현재는 아주 단순한 예제로 구성되어 있습니다.

- 서버 실행 테스트
- 간단한 `User` 생성
- 간단한 `User` 조회
- H2 인메모리 데이터베이스 연동

아직은 학습용 최소 구조이며, 점차 기능을 늘려 갈 예정입니다.

## 간단한 구조

현재 구조는 아래와 같은 흐름으로 이해할 수 있습니다.

```text
클라이언트 요청
  -> Controller
  -> Service
  -> Repository
  -> Database(H2)
```

프로젝트 디렉터리도 이 구조에 맞춰 나뉘어 있습니다.

```text
src/main/java/com/example/mini_spring/
  MiniSpringApplication.java
  HelloController.java
  domain/user/
    TestController.java
    controller/
    service/
    repository/
    domain/
    dto/
```

각 계층의 역할:

- `controller`
  - HTTP 요청을 받고 응답을 반환
- `service`
  - 비즈니스 로직 처리
- `repository`
  - DB 접근
- `domain`
  - 엔티티 정의
- `dto`
  - 요청/응답 데이터 정의

## 앞으로의 확장 방향

이 프로젝트는 단순 CRUD 연습에서 끝나지 않고, 점차 `segye`의 백엔드 구조를 반영하는 방향으로 확장할 예정입니다.

예상 순서:

1. 기본 CRUD 더 익히기
2. 예외 처리 추가
3. Validation 추가
4. 공통 응답 형식 만들기
5. 인증/인가 구조 이해하기
6. `segye` 도메인에 맞는 API 설계 시작

## segye 프로젝트 소개

`segye`는 Flutter로 만들어진 프론트엔드 프로젝트입니다.  
일정, 할 일, 소비 내역을 날짜 중심으로 관리하는 개인 다이어리형 애플리케이션입니다.

핵심 기능 방향:

- 로그인 / 회원가입
- 캘린더 기반 메인 화면
- 날짜별 일정 관리
- 날짜별 할 일 관리
- 소비 내역 기록 및 조회
- 마이페이지 확장 가능 구조

현재 `front` 프로젝트에서 구현된 특징:

- Flutter UI 중심으로 주요 화면이 이미 구성되어 있음
- 로그인, 회원가입, 메인 화면, 날짜 상세 화면이 존재함
- 일정 / todo / consumption 흐름이 프론트에 표현되어 있음
- 아직 실제 백엔드 연동은 거의 없는 상태임

## 이 학습 프로젝트와 segye의 관계

이 저장소는 `segye`의 백엔드를 바로 만드는 메인 저장소는 아니지만, 그 전에 필요한 Spring 개념을 실습하는 준비 단계 프로젝트입니다.

정리하면:

- `front/segye`
  - 서비스 화면과 사용자 흐름이 있는 프론트엔드 프로젝트
- `sunny-sally_spring-minimini_project`
  - 그 프론트의 백엔드를 만들기 전에 Spring을 연습하는 학습 프로젝트

## 나중에 구현하고 싶은 segye 백엔드 예시

추후에는 이런 식의 구조로 확장할 수 있습니다.

- `auth`
  - 회원가입, 로그인, JWT
- `member`
  - 사용자 정보
- `schedule`
  - 일정 관리
- `todo`
  - 할 일 관리
- `account`
  - 수입/지출 기록
- `category`
  - 소비 카테고리 관리
- `common`
  - 예외 처리, 공통 응답

## 요약

이 프로젝트는 Spring Boot를 공부하기 위한 작은 연습용 백엔드 프로젝트입니다.  
최종적으로는 `segye` 프론트 프로젝트와 연결될 수 있는 백엔드를 구현하고 싶고, 그 목표를 위해 지금은 작은 기능부터 차근차근 연습하는 단계입니다.
