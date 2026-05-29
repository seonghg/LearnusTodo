<img width="228" height="228" alt="yon_chat_hover" src="https://github.com/user-attachments/assets/30868643-04d2-47be-bea7-43b96d5fa0dc" />

<img width="557" height="228" alt="1425-Photoroom" src="https://github.com/user-attachments/assets/39e1cf37-0ba6-485d-8edd-50f2fd46d042" />

# LearnTodo ?
Spring Boot service for Yonsei LearnUs Calendar

## 1. Quick Start
```text
[1] # You must configure "application.properties"
      1. Supabase PostgreSQL Pooler Connection Setting
      2. Mistral API key Setting
[2] .\gradlew.bat clean bootRun
```

## 2. Architecture

```text
todo/
├─ build.gradle                         # Gradle build setup and Spring Boot dependencies
├─ settings.gradle                      # Gradle project name
├─ gradlew / gradlew.bat                # Gradle Wrapper scripts
├─ README.md
│
├─ gradle/
│  └─ wrapper/                          # Gradle Wrapper files
│
└─ src/
   └─ main/
      ├─ java/
      │  └─ RunA2Do/
      │     └─ todo/
      │        ├─ TodoApplication.java  # Spring Boot entry point
      │        ├─ config/               # Spring Security configuration
      │        ├─ controller/           # Auth, dashboard, LearnUs sync, ToDo, grades, my page, chatbot APIs
      │        ├─ dto/                  # API request and response DTOs
      │        ├─ repository/           # Database access for users, courses, calendar, LearnUs data
      │        ├─ security/             # Authenticated user helper
      │        └─ service/              # Business logic for auth, dashboard, ToDo, grades, my page
      │
      └─ resources/
         ├─ application-example.properties  # Example environment configuration
         ├─ application.properties          # Local/server-only configuration and secrets
         └─ static/                         # HTML/CSS/JavaScript frontend
            ├─ dashboard.html               # LearnUs sync, summary cards, calendar, courses, upcoming events
            ├─ todo.html                    # Personal ToDo creation and recent schedule list
            ├─ grades.html                  # Grade prediction screen
            ├─ guide.html                   # User guide
            ├─ mypage.html                  # Profile, schedule summary, account management
            ├─ login.html / register.html   # Login and registration pages
            ├─ images/                      # Chatbot launcher images
            ├─ css/
            │  └─ style.css                 # Shared frontend styles
            └─ js/                          # Page scripts and shared API helpers
               ├─ api.js                    # Common fetch and utility functions
               ├─ chatbot.js                # Floating chatbot UI
               ├─ dashboard.js              # Dashboard calendar and LearnUs sync logic
               ├─ todo.js                   # ToDo form and recent schedule rendering
               ├─ grades.js                 # Grade prediction interactions
               ├─ mypage.js                 # Profile and schedule summary logic
               ├─ login.js                  # Login page behavior
               └─ register.js               # Registration page behavior
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

<img width="968" height="537" alt="mypage" src="https://github.com/user-attachments/assets/d1fae3f6-5101-4c52-b8f9-eb3688fbb3ce" />

3.7 chatbot

<img width="300" height="400" alt="image" src="https://github.com/user-attachments/assets/fd2be8ca-f0c2-4421-b00b-67f415e3032a" />

