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
- **NIO (Non-blocking I/O)**: Efficient data transfer
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
- Command Prompt/PowerShell/Terminal

### 🚀 Quick Start (Simplest Method)

#### Step 1: Compile the Project
Open Command Prompt/PowerShell in the QuizHub folder:

```cmd
javac -d bin src\server\*.java src\client\*.java src\model\*.java src\common\*.java
```

#### Step 2: Run Server (Terminal 1)
```cmd
java -cp bin server.QuizServer
```
✅ Server UI opens → Click **"Start Server"** button

#### Step 3: Run Clients (Terminal 2, 3, 4...)
Open **new** Command Prompt windows for each student:

```cmd
java -cp bin client.QuizClient
```
✅ Enter: `localhost`, Student ID (S001, S002, etc.), Name → Click **"Connect to Quiz"**

**Repeat Step 3** in multiple terminal windows to simulate multiple students.

---

### 🎯 One-Command Solution (PowerShell)

Copy and paste this into PowerShell to start everything automatically:

```powershell
# Start Server in new window
Start-Process cmd -ArgumentList "/k java -cp bin server.QuizServer"

# Wait 2 seconds
Start-Sleep -Seconds 2

# Start 3 Clients in new windows
Start-Process cmd -ArgumentList "/k java -cp bin client.QuizClient"
Start-Process cmd -ArgumentList "/k java -cp bin client.QuizClient"
Start-Process cmd -ArgumentList "/k java -cp bin client.QuizClient"
```

This opens 4 windows automatically (1 server + 3 clients)! 🎉

---

### 💻 Alternative: Run from IDE

If you're using IntelliJ IDEA, Eclipse, VS Code, or NetBeans:

**Run Server:**
1. Open `src/server/QuizServer.java`
2. Right-click → **Run 'QuizServer.main()'** or press `Ctrl+Shift+F10`
3. Server UI opens → Click **"Start Server"**

**Run Clients:**
1. Open `src/client/QuizClient.java`
2. Right-click → **Run 'QuizClient.main()'** (Allow multiple instances in run configuration)
3. Enter connection details → Click **"Connect to Quiz"**
4. Repeat for multiple students

**IDE Configuration:**
- Server: Main class = `server.QuizServer`, Args (optional) = `8888`
- Client: Main class = `client.QuizClient`, Args (optional) = `localhost 8888`
- Enable "Allow parallel run" / "Allow multiple instances" for clients

---

### 🌐 Running on Network (Different Computers)

**On Server Machine:**
1. Find your IP address:
   - Windows: `ipconfig` (look for IPv4 Address, e.g., 192.168.1.100)
   - Linux/Mac: `ifconfig` or `ip addr`
2. Run server: `java -cp bin server.QuizServer`
3. Allow port 8888 in firewall

**On Client Machines:**
```cmd
java -cp bin client.QuizClient 192.168.1.100 8888
```
Or enter the server's IP address in the client login dialog.

---

### 🔧 Custom Port

**Run server on different port:**
```cmd
java -cp bin server.QuizServer 9999
```

**Connect clients to custom port:**
```cmd
java -cp bin client.QuizClient localhost 9999
```

---

### ⚡ Using PowerShell Scripts (If Available)

If build scripts exist in your project:

```powershell
.\build.ps1         # Compile all files
.\run-server.ps1    # Start server
.\run-client.ps1    # Start client (run in separate windows)
```

## Project Structure
```
QuizHub/
├── src/
│   ├── server/          # Server-side components
│   │   ├── QuizServer.java       # Main server (ServerSocket, multithreading)
│   │   ├── ClientHandler.java    # Individual client handler (threads)
│   │   ├── QuestionManager.java  # Question CRUD operations
│   │   ├── ScoringSystem.java    # Score calculation, leaderboard
│   │   └── ServerUI.java         # Teacher dashboard GUI
│   ├── client/          # Client-side components
│   │   ├── QuizClient.java       # Client socket connection
│   │   └── ClientUI.java         # Student quiz interface GUI
│   ├── common/          # Shared protocol
│   │   └── Protocol.java         # Message types and formats
│   └── model/           # Data models
│       ├── Question.java         # Question data model
│       ├── Student.java          # Student data model
│       └── QuizSession.java      # Quiz session management
├── bin/                 # Compiled .class files
└── README.md
```

## Team Split Responsibilities

### Overview:
This project is designed for **5 team members**, each focusing on specific Java Network Programming concepts:

1. **Member 1 - Server Architecture & Socket Programming**
   - **Java Concepts**: ServerSocket, TCP/IP, Multi-threading, Thread Pools, ExecutorService
   - **Files**: `QuizServer.java`, `ClientHandler.java`
   - **Tasks**: Set up server socket, accept connections, thread pool management, broadcast messages

2. **Member 2 - Client & Network Communication**
   - **Java Concepts**: Client Socket, I/O Streams (ObjectInputStream/ObjectOutputStream), Asynchronous Communication
   - **Files**: `QuizClient.java`, `ClientUI.java`
   - **Tasks**: Socket connection, message listener thread, send/receive data, reconnection logic

3. **Member 3 - Question Manager & Data Serialization**
   - **Java Concepts**: Object Serialization, Protocol Design, Data Integrity
   - **Files**: `QuestionManager.java`, `Question.java`, `QuizSession.java`, `Protocol.java`
   - **Tasks**: Implement Serializable, question CRUD, protocol message format, data validation

4. **Member 4 - Scoring System & Real-time Updates**
   - **Java Concepts**: Thread Synchronization, ConcurrentHashMap, Atomic Operations, Real-time Broadcasting
   - **Files**: `ScoringSystem.java`, integration with server
   - **Tasks**: Thread-safe scoring, time-based bonuses, leaderboard generation, concurrent answer handling

5. **Member 5 - UI & Event-Driven Programming**
   - **Java Concepts**: Event-Driven Programming, Callbacks, Thread-Safe UI Updates (SwingUtilities)
   - **Files**: `ServerUI.java`, `ClientUI.java`
   - **Tasks**: Server/client GUIs, timer display, event listeners, thread-safe UI updates

📋 **For detailed work division, tasks, deliverables, and weekly milestones, see [TEAM_WORK_DIVISION.md](TEAM_WORK_DIVISION.md)**

## Key Implementation Details

### Socket Communication
```java
// Server: Accept connections
ServerSocket serverSocket = new ServerSocket(8888);
Socket clientSocket = serverSocket.accept();

// Client: Connect to server
Socket socket = new Socket("localhost", 8888);
```

### Multithreading
```java
// Thread pool for concurrent clients
ExecutorService threadPool = Executors.newCachedThreadPool();
threadPool.execute(new ClientHandler(socket, server));
```

### Thread Synchronization
```java
// Thread-safe collections
private Map<String, Student> students = new ConcurrentHashMap<>();

// Synchronized methods
public synchronized void processAnswer(String studentId, String answer) {
    // Thread-safe operations
}
```

### Real-time Broadcasting
```java
// Broadcast to all connected clients
public void broadcast(String type, String data) {
    for (ClientHandler client : clients) {
        client.sendMessage(type, data);
    }
}
```

## Troubleshooting

### Common Issues

**"Address already in use" Error**
- Port 8888 is occupied. Use different port: `java -cp bin server.QuizServer 9999`
- Or kill existing Java processes: `taskkill /F /IM java.exe`

**"Could not find or load main class"**
- Ensure you're in QuizHub directory
- Recompile: `javac -d bin src\server\*.java src\client\*.java src\model\*.java src\common\*.java`
- Check classpath: `-cp bin`

**Client Cannot Connect**
- Ensure server is running first
- Check firewall allows port 8888
- Verify correct host/port (use `localhost` for local testing)
- Check server logs for errors

**UI Not Displaying**
- Ensure Java version supports Swing (JDK 8+)
- Try: `java -Djava.awt.headless=false -cp bin server.QuizServer`

**Multiple Clients in IDE**
- Enable "Allow parallel run" or "Allow multiple instances" in run configuration
- Or run clients from command line

## Testing Checklist

**Server Testing:**
- [ ] Server starts on port 8888
- [ ] Accepts multiple client connections
- [ ] Broadcasts questions to all clients
- [ ] Handles client disconnection gracefully
- [ ] Quiz start/stop works correctly

**Client Testing:**
- [ ] Connects to server successfully
- [ ] Receives and displays questions
- [ ] Timer countdown works
- [ ] Answer submission works
- [ ] Leaderboard updates in real-time

**Integration Testing:**
- [ ] 3+ clients connect simultaneously
- [ ] All clients receive questions at same time
- [ ] Scores update correctly for all clients
- [ ] Leaderboard ranks students properly
- [ ] Quiz completes and shows final results

## Dependencies
- Java SE 8 or higher
- Java Swing (included in JDK)
- No external libraries required

## Quick Reference

**Compile:** `javac -d bin src\server\*.java src\client\*.java src\model\*.java src\common\*.java`

**Run Server:** `java -cp bin server.QuizServer`

**Run Client:** `java -cp bin client.QuizClient`

**Default Port:** 8888

**Time per Question:** 30 seconds

**Base Points:** 10 per question (with time bonus up to 50%)

---

## 📚 Additional Documentation

- **[QUICKSTART.md](QUICKSTART.md)** - 5-minute quick start guide
- **[TEAM_WORK_DIVISION.md](TEAM_WORK_DIVISION.md)** - Detailed team responsibilities and tasks
- **[DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)** - Comprehensive development guide
- **[NETWORK_CONCEPTS.md](NETWORK_CONCEPTS.md)** - Java network programming concepts explained
- **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** - Project overview and summary

---

**Ready to start? Open 4 terminals and run the Quick Start commands above! 🚀**
