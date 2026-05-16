# SheepChat

https://chatapp-ks5w.onrender.com/login

SheepChat is a simple secure real-time chat application built with **Java** and **Spring Boot**, featuring multi-room messaging, persistent chat history, and authenticated user accounts. The application uses **WebSockets** for low-latency real-time communication and **PostgreSQL** for data persistence, allowing users to create accounts, join chat rooms, and communicate instantly across concurrent sessions.

The project was designed to simulate the architecture of modern chat platforms by combining traditional REST-based navigation and authentication with persistent WebSocket connections for live messaging. Users can dynamically create and delete chat rooms, with server-side synchronization ensuring all connected clients stay updated in real time.

## Features

* Secure user authentication with Spring Security
* BCrypt password hashing
* Real-time messaging using WebSockets (`/ws/chat`)
* Dynamic multi-room chat system
* Room creation and deletion with live client updates
* Persistent message history stored in PostgreSQL
* Responsive Discord-style UI using HTML, CSS, Thymeleaf, and JavaScript
* Cloud deployment on Render with HTTPS/WSS support

## Tech Stack

* **Backend:** Java, Spring Boot, Spring WebSocket, Spring Security, Spring Data JPA, Hibernate
* **Frontend:** HTML, CSS, JavaScript, Thymeleaf
* **Database:** PostgreSQL
* **Deployment:** Render
* **Tools:** Maven, Git, GitHub

## Architecture

* REST-style endpoints handle authentication, navigation, and room management
* WebSockets manage real-time bi-directional communication
* Spring Security manages authentication and session-based authorization
* PostgreSQL persists users, chat rooms, and message history

## What I learned?

This project strengthened my understanding of:

* Real-time web application architecture
* WebSocket lifecycle management
* Backend security and authentication
* Database persistence and ORM design
* Cloud deployment and production debugging
* Multi-user synchronization and state management

## Future additions

While the frontend does currently look bland as I mainly focused on understanding the backend architecutre, REST endpoints and WebSockets, I do plan on improving the frontend to make it look a bit more polished
