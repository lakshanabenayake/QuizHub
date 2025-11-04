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

### Prerequisites
- Java JDK 8 or higher
- PowerShell (Windows) or Bash (Linux/Mac)

### Build the Application:

**Windows (PowerShell):**
```powershell
.\build.ps1
```

**Linux/Mac:**
```bash
chmod +x build.sh
./build.sh
```

### Start the Server (Teacher):

**Windows (PowerShell):**
```powershell
.\run-server.ps1
```

**Linux/Mac:**
```bash
./run-server.sh
```

### Start the Client (Students):

**Windows (PowerShell):**
```powershell
.\run-client.ps1
```

**Linux/Mac:**
```bash
./run-client.sh
```

**Note:** Open multiple terminals/PowerShell windows to simulate multiple students.

## Project Structure
```
QuizHub/
├── src/
│   ├── server/          # Server-side components
│   ├── client/          # Client-side components
│   ├── common/          # Shared classes/interfaces
│   └── model/           # Data models
├── bin/                 # Compiled classes
├── build.ps1           # Build script (Windows)
├── build.sh            # Build script (Linux/Mac)
├── run-server.ps1      # Server launcher (Windows)
├── run-server.sh       # Server launcher (Linux/Mac)
├── run-client.ps1      # Client launcher (Windows)
├── run-client.sh       # Client launcher (Linux/Mac)
└── README.md
```

## Team Split Responsibilities

### Overview:
This project is designed for **5 team members**, each focusing on specific Java Network Programming concepts:

1. **Member 1 - Server Architecture & Socket Programming**
   - ServerSocket, TCP/IP, Multi-threading, Thread Pools
   - Files: `QuizServer.java`, `ClientHandler.java`

2. **Member 2 - Client & Network Communication**
   - Client Socket, I/O Streams, Asynchronous Communication
   - Files: `QuizClient.java`, `ClientUI.java`

3. **Member 3 - Question Manager & Data Serialization**
   - Object Serialization, Protocol Design, Data Integrity
   - Files: `QuestionManager.java`, `Question.java`, `QuizSession.java`

4. **Member 4 - Scoring System & Real-time Updates**
   - Thread Synchronization, Real-time Broadcasting, Atomic Operations
   - Files: `ScoringSystem.java`, scoring integration

5. **Member 5 - UI & Event-Driven Programming**
   - Event-Driven Programming, Callbacks, Thread-Safe UI Updates
   - Files: `ServerUI.java`, `ClientUI.java`

📋 **For detailed work division, Java concepts, tasks, and deliverables for each member, see [TEAM_WORK_DIVISION.md](TEAM_WORK_DIVISION.md)**

## Dependencies
- Java SE 8 or higher
- Java Swing (included in JDK)
- Java NIO (included in JDK)

## Quick Start
See **QUICKSTART.md** for a detailed 5-minute setup guide.
