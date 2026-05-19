<img width="557" height="228" alt="LearnToDo" src="https://github.com/user-attachments/assets/5499da11-d1e3-47b2-8235-1ff3a7534abd" />

# LearnTodo ?
Spring Boot service for Yonsei LearnUs Calendar

## 1. Quick Start
```text
[1] #application.properties Setting
[2] .\gradlew.bat clean bootRun
```

## 2. Architecture

```text
todo/
├─ build.gradle                         # Gradle 빌드 설정, 의존성 관리
├─ settings.gradle                      # Gradle 프로젝트 이름 설정
├─ gradlew / gradlew.bat                # Gradle Wrapper 실행 파일
├─ README.md
│
├─ gradle/
│  └─ wrapper/                          # Gradle Wrapper 설정 및 jar
│
└─ src/
   └─ main/
      ├─ java/
      │  └─ RunA2Do/
      │     └─ todo/
      │        ├─ TodoApplication.java  # Spring Boot Start Point
      │        ├─ config/               # Spring Security 및 설정
      │        ├─ controller/           # API 요청/응답 처리
      │        ├─ service/              # 비즈니스 로직
      │        ├─ repository/           # DB 접근 계층
      │        └─ security/             # 인증/인가 관련 코드
      │
      └─ resources/
         ├─ application-example.properties  # 환경설정 예시 파일
         └─ static/                         # 프론트엔드 파일
```
