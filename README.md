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

## 3. Preview
3.1 Login

<img width="1097" height="577" alt="login" src="https://github.com/user-attachments/assets/5ae6b369-1ffc-47a5-a484-9d0e04c993b0" />

3.2 Dashborad

<img width="1858" height="831" alt="Dash" src="https://github.com/user-attachments/assets/c66a265e-88fb-4378-b1cd-17352535dc95" />

3.3 Todo

<img width="1856" height="832" alt="todoo" src="https://github.com/user-attachments/assets/07ce03b7-5af3-4b00-a665-eba9dc9aff66" />

3.4 Grade predict

<img width="1882" height="747" alt="predict" src="https://github.com/user-attachments/assets/cc321cb6-6212-4d59-8d64-e1556c496cce" />

3.5 mypage

<img width="1868" height="837" alt="mypage" src="https://github.com/user-attachments/assets/d1fae3f6-5101-4c52-b8f9-eb3688fbb3ce" />
