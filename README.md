# Online Quiz/Exam System (Client-Server Based)

## Overview
A Java-based network programming application demonstrating socket communication, multithreading, NIO, and client-server architecture for conducting live quizzes.

## Architecture

### Components:
1. **Server (Host)** - Teacher's interface to manage quiz sessions
2. **Client** - Student's interface to participate in quizzes
3. **Question Manager** - Handles quiz questions and answers
4. **Scoring System** - Tracks and calculates student scores
5. **UI** - Graphical interfaces for both server and client

## Java Network Programming Concepts Demonstrated

- **Socket Communication**: TCP/IP sockets for client-server connection
- **Multithreading**: Handles multiple concurrent client connections
- **NIO (Non-blocking I/O)**: Efficient I/O operations
- **Synchronization**: Thread-safe operations for real-time updates
- **Client-Server Architecture**: Centralized quiz management

## Features

### Server Features:
- Create and manage quiz questions
- Start/stop quiz sessions
- Monitor connected students in real-time
- Broadcast questions to all clients
- Display live leaderboard
- Result broadcasting

### Client Features:
- Connect to quiz server
- Receive questions in real-time
- Submit answers with timer
- View scores and rankings
- Real-time feedback

### Extensions:
- Timer system for each question
- Automatic scoring
- Result broadcasting
- Leaderboard updates
- Chat functionality (bonus)

## How to Run

### Start the Server:
```bash
javac -d bin src/server/*.java src/common/*.java src/model/*.java
java -cp bin server.QuizServer
```

### Start the Client:
```bash
javac -d bin src/client/*.java src/common/*.java src/model/*.java
java -cp bin client.QuizClient
```

## Project Structure
```
QuizHub/
├── src/
│   ├── server/          # Server-side components
│   ├── client/          # Client-side components
│   ├── common/          # Shared classes/interfaces
│   ├── model/           # Data models
│   └── ui/              # User interface components
├── resources/           # Configuration files
└── README.md
```

## Team Split Responsibilities

1. **Server (Host)**: Socket server, client handler, session management
2. **Client**: Socket client, UI interaction, answer submission
3. **Question Manager**: Question CRUD, quiz logic, question broadcasting
4. **Scoring**: Score calculation, leaderboard, result generation
5. **UI**: Server GUI, Client GUI, real-time updates

## Dependencies
- Java SE 8 or higher
- Java Swing (included in JDK)
- Java NIO (included in JDK)

