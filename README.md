<img width="557" height="228" alt="LearnToDo" src="https://github.com/user-attachments/assets/5499da11-d1e3-47b2-8235-1ff3a7534abd" />

# LearnTodo ?
Spring Boot service for Yonsei LearnUs Calendar

## 1. Quick Start
```text
[1] # You must configure application.properties !
[2] .\gradlew.bat clean bootRun
```

## 2. Architecture

```text
todo/
├─ build.gradle                         # Gradle 빌드 설정 및 Spring Boot 의존성 관리
├─ settings.gradle                      # Gradle 프로젝트 이름 설정
├─ gradlew / gradlew.bat                # Gradle Wrapper 실행 파일
├─ README.md
│
├─ gradle/
│  └─ wrapper/                          # Gradle Wrapper 설정 파일 및 jar
│
└─ src/
   └─ main/
      ├─ java/
      │  └─ RunA2Do/
      │     └─ todo/
      │        ├─ TodoApplication.java  # Spring Boot Start Point
      │        ├─ config/               # Spring Security 설정
      │        ├─ controller/           # 인증, 대시보드, LearnUs, ToDo, 성적, 마이페이지 API
      │        ├─ dto/                  # API 요청/응답 DTO
      │        ├─ repository/           # 사용자, 강의, 일정, LearnUs DB 접근 계층
      │        ├─ security/             # 인증 사용자 조회 유틸
      │        └─ service/              # 회원, 동기화, 대시보드, ToDo, 성적, 마이페이지 비즈니스 로직
      │
      └─ resources/
         ├─ application-example.properties  # 환경설정 예시 파일
         ├─ application.properties          # 로컬 실행용 환경설정 파일
         └─ static/                         # HTML/CSS/JavaScript 프론트엔드 파일
            ├─ dashboard.html               # LearnUs 동기화, 요약 카드, 달력, 강의/일정 목록
            ├─ todo.html                    # 개인 일정 추가 및 최근 일정 조회
            ├─ grades.html                  # 성적 예측 계산 화면
            ├─ guide.html                   # 서비스 사용 가이드
            ├─ mypage.html                  # 내 정보, 일정 요약, 계정 관리
            ├─ login.html / register.html   # 로그인 및 회원가입
            ├─ css/
            │  └─ style.css                 # 전체 화면 공통 스타일
            └─ js/                          # 화면별 동작 및 공통 API 유틸
```

## 3. Preview
3.1 Login

<img width="1097" height="577" alt="login" src="https://github.com/user-attachments/assets/5ae6b369-1ffc-47a5-a484-9d0e04c993b0" />

3.2 Dashboard

<img width="1858" height="831" alt="Dash" src="https://github.com/user-attachments/assets/c66a265e-88fb-4378-b1cd-17352535dc95" />

3.3 Todo

<img width="1856" height="832" alt="todoo" src="https://github.com/user-attachments/assets/07ce03b7-5af3-4b00-a665-eba9dc9aff66" />

3.4 Grade predict

<img width="1882" height="747" alt="predict" src="https://github.com/user-attachments/assets/cc321cb6-6212-4d59-8d64-e1556c496cce" />

3.5 Guide

<img width="1881" height="848" alt="image" src="https://github.com/user-attachments/assets/48e98cb7-0069-4dec-bfb8-d5f49b2afcf0" />

3.6 mypage

<img width="1868" height="837" alt="mypage" src="https://github.com/user-attachments/assets/d1fae3f6-5101-4c52-b8f9-eb3688fbb3ce" />
