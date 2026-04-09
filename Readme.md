# sunny-sally spring minimini project

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

## 이 학습 프로젝트와 segye의 관계
- `front/segye`
  - 서비스 화면과 사용자 흐름이 있는 프론트엔드 프로젝트
- `sunny-sally_spring-minimini_project`
  - 그 프론트의 백엔드를 만들기 전에 Spring을 연습하는 학습 프로젝트

## segye 백엔드

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
