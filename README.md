### TaskTrek - a Gamified Task Tracker (Java Spring Boot)

## Overview

A gamified productivity tracker for students inspired by Google Tasks & Duolingo, with course and task management — gamified with XP and levels.
Built with Spring Boot in Java.

Users can:-

- Register / Log in (via /auth)

- Create and manage courses (via /courses)

- Add and complete tasks (via /tasks), earning XP and levelling up

## Tech Stack

- Language: Java (JDK 21)

- Framework: Spring Boot

- Database: MongoDB

- Tools: Maven, Postman

## Project Structure (as of date)

src/

├── controller/

│   └── AuthController.java

│   └── CourseController.java

│   └── TaskController.java

│

├── service/

│   ├── AuthService.java

│   └── CourseService.java

│   └── TaskService.java

│   └── SkillProgressService.java

│

├── model/

│   └── User.java

│   └── Course.java

│   └── Task.java

│   └── QuizPrepTask.java

│   └── ProjectTask.java

│   └── ExamPrepTask.java

│   └── AssignmentTask.java

│   └── Achievement.java

│   └── SkillProgress.java

│

├── repository/

│   └── UsersRepository.java

└── request/

│   └── LoginRequest.java

│   └── SignupRequest.java

│   └── NewCourseRequest.java

│   └── NewTaskRequest.java

│   └── EditedCourseRequest.java

## Features

- Auth system (register + login) with state maintained and Passwords hashed using BCryptPasswordEncoder
 
- Course CRUD (create, rename, delete, view)

- Task CRUD + Complete, with gamification through XP, levels, and day streaks

- Controller–Service separation with database storage through MongoDB


## API Endpoints Summary

| Method | Endpoint	| Description |
| :---: | :---: | :---: |
| POST | /auth/register	| Register a new user |
| POST | /auth/login | Login existing user |
| GET | /api/courses | Show all courses of the logged-in user |
| POST | /api/courses | Create a new course under the user |
| PUT | /api/courses/{id} | Rename course of ID in path |
| DELETE | /api/courses/{id} | Delete course of ID in path |
| GET | /api/courses/{id} | Shows all tasks of a course |
| POST | /api/courses/{id}/tasks | Create a task |
| PATCH | /api/courses/{courseid}/tasks/{taskid} | Edit details of a task |
| DELETE | /api/courses/{courseid}/tasks/{taskid} | Delete a task |
| POST | /api/courses/{courseid}/tasks/{taskid}/complete | Mark a task as complete and award XP |
