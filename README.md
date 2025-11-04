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
- Any Java IDE (IntelliJ IDEA, Eclipse, VS Code, NetBeans) OR Command Line

### Method 1: Run from IDE (Simplest - No Scripts Needed)

Both server and client have main methods that can be executed directly.

#### Run Server:
1. Open `src/server/QuizServer.java` in your IDE
2. Right-click → **Run 'QuizServer.main()'**
3. Server UI opens → Click **"Start Server"** button
4. Server ready on port 8888

#### Run Client(s):
1. Open `src/client/QuizClient.java` in your IDE
2. Right-click → **Run 'QuizClient.main()'** (allow multiple instances)
3. Enter: `localhost`, Student ID, Name → Click **"Connect to Quiz"**
4. Repeat for multiple students

**IDE Configuration (Optional)**:
- Server: Main class = `server.QuizServer`, Args = `8888`
- Client: Main class = `client.QuizClient`, Args = `localhost 8888`

---

### Method 2: Command Line (No Scripts)

#### Compile:
```cmd
javac -d bin src\server\*.java src\client\*.java src\model\*.java src\common\*.java
```

#### Run Server (Terminal 1):
```cmd
java -cp bin server.QuizServer
```

#### Run Clients (Terminal 2, 3, 4...):
```cmd
java -cp bin client.QuizClient
```

**With Custom Port**:
```cmd
java -cp bin server.QuizServer 9999
java -cp bin client.QuizClient localhost 9999
```

---

### Method 3: Using PowerShell Scripts (Windows)

**Windows (PowerShell):**
```powershell
.\build.ps1         # Build
.\run-server.ps1    # Start server
.\run-client.ps1    # Start client
```

**Linux/Mac:**
```bash
chmod +x build.sh
./build.sh          # Build
./run-server.sh     # Start server
./run-client.sh     # Start client
```

---

### Running on Network (Different Computers)

**Server Machine:**
1. Find IP: `ipconfig` (Windows) or `ifconfig` (Linux/Mac)
2. Run: `java -cp bin server.QuizServer`

**Client Machines:**
```cmd
java -cp bin client.QuizClient 192.168.1.100 8888
```

**Note:** Allow port 8888 in firewall settings

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
