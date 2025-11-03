# QuizHub - Development Guide

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture & Design](#architecture--design)
3. [Java Network Programming Concepts](#java-network-programming-concepts)
4. [Setup & Installation](#setup--installation)
5. [How to Run](#how-to-run)
6. [Team Split Guide](#team-split-guide)
7. [Features Implementation](#features-implementation)
8. [Testing Guide](#testing-guide)
9. [Troubleshooting](#troubleshooting)

---

## Project Overview

QuizHub is a comprehensive Online Quiz/Exam System demonstrating Java Network Programming concepts including:
- **Socket Communication**: TCP/IP client-server architecture
- **Multithreading**: Concurrent client handling
- **NIO (Non-blocking I/O)**: Efficient data transfer
- **Synchronization**: Thread-safe operations
- **Real-time Communication**: Live updates and broadcasting

### Application Flow
```
1. Teacher starts the server
2. Students connect as clients
3. Teacher loads questions and starts quiz
4. Server broadcasts questions to all clients
5. Students answer with timer countdown
6. Server scores answers and updates leaderboard in real-time
7. Quiz ends and final results are displayed
```

---

## Architecture & Design

### Component Diagram
```
┌─────────────────────────────────────────────────────────────┐
│                         SERVER SIDE                          │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌───────────────┐  ┌─────────────────┐  │
│  │ QuizServer   │──│ ClientHandler │──│ Socket (Client) │  │
│  │ (Main)       │  │ (Thread)      │  │                 │  │
│  └──────┬───────┘  └───────────────┘  └─────────────────┘  │
│         │                                                    │
│  ┌──────▼──────────┐  ┌──────────────┐  ┌───────────────┐  │
│  │ QuestionManager │  │ QuizSession  │  │ ScoringSystem │  │
│  └─────────────────┘  └──────────────┘  └───────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                    ServerUI                          │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                         CLIENT SIDE                          │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────┐                                            │
│  │ QuizClient   │──────► Socket (Server)                     │
│  └──────┬───────┘                                            │
│         │                                                    │
│  ┌──────▼──────────────────────────────────────────────┐    │
│  │                   ClientUI                          │    │
│  │  (Login, Quiz Interface, Leaderboard, Chat)         │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                      SHARED COMPONENTS                       │
├─────────────────────────────────────────────────────────────┤
│  Protocol | Question | Student | QuizSession (models)       │
└─────────────────────────────────────────────────────────────┘
```

### Class Responsibilities

#### Server Components
1. **QuizServer.java**
   - Manages ServerSocket
   - Accepts client connections
   - Manages quiz lifecycle
   - Broadcasts messages to all clients
   - Thread pool management

2. **ClientHandler.java**
   - Handles individual client communication
   - Runs in separate thread
   - Processes client messages
   - Sends responses

3. **QuestionManager.java**
   - CRUD operations for questions
   - Question validation
   - Question bank management

4. **ScoringSystem.java**
   - Answer validation
   - Score calculation with time bonus
   - Leaderboard generation
   - Answer history tracking

5. **ServerUI.java**
   - Teacher dashboard
   - Quiz control panel
   - Live monitoring
   - Question management

#### Client Components
1. **QuizClient.java**
   - Socket connection to server
   - Message sending/receiving
   - Background message listener

2. **ClientUI.java**
   - Student login interface
   - Quiz interface with timer
   - Answer submission
   - Leaderboard display
   - Chat functionality

#### Common Components
1. **Protocol.java**
   - Message types and formats
   - Protocol constants
   - Message parsing utilities

2. **Question.java**
   - Question data model
   - Serialization methods

3. **Student.java**
   - Student data model
   - Score tracking
   - Performance metrics

4. **QuizSession.java**
   - Session state management
   - Student collection
   - Question progression

---

## Java Network Programming Concepts

### 1. Socket Communication
**Location**: `QuizServer.java`, `QuizClient.java`

```java
// Server Side
ServerSocket serverSocket = new ServerSocket(port);
Socket clientSocket = serverSocket.accept();

// Client Side
Socket socket = new Socket(serverHost, serverPort);
```

**Demonstrates**: TCP/IP connection establishment, client-server model

### 2. Multithreading
**Location**: `QuizServer.java`, `ClientHandler.java`

```java
// Thread pool for handling multiple clients
ExecutorService threadPool = Executors.newCachedThreadPool();
threadPool.execute(new ClientHandler(socket, server));
```

**Demonstrates**: Concurrent client handling, thread pooling

### 3. I/O Streams
**Location**: `ClientHandler.java`, `QuizClient.java`

```java
// Reading from socket
BufferedReader in = new BufferedReader(
    new InputStreamReader(socket.getInputStream()));

// Writing to socket
PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
```

**Demonstrates**: Network I/O, buffered streams

### 4. Synchronization
**Location**: `QuizSession.java`, `ScoringSystem.java`

```java
public synchronized void processAnswer(...) {
    // Thread-safe operations
}

// Thread-safe collections
private Map<String, Student> students = new ConcurrentHashMap<>();
```

**Demonstrates**: Thread safety, synchronized methods, concurrent collections

### 5. Protocol Design
**Location**: `Protocol.java`

```java
// Custom protocol for client-server communication
String message = Protocol.createMessage(type, data);
String[] parts = Protocol.parseMessage(message);
```

**Demonstrates**: Application-layer protocol design

### 6. Real-time Updates
**Location**: `QuizServer.java` - broadcast methods

```java
public void broadcast(String type, String data) {
    for (ClientHandler client : clients) {
        client.sendMessage(type, data);
    }
}
```

**Demonstrates**: Broadcasting, real-time synchronization

---

## Setup & Installation

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- No external libraries required (uses only Java SE)

### Installation Steps

1. **Extract/Clone the project**
   ```
   QuizHub/
   ├── src/
   ├── build.bat / build.sh
   ├── run-server.bat / run-server.sh
   └── run-client.bat / run-client.sh
   ```

2. **Verify Java Installation**
   ```bash
   java -version
   javac -version
   ```

3. **Set permissions (Linux/Mac only)**
   ```bash
   chmod +x build.sh run-server.sh run-client.sh
   ```

---

## How to Run

### Method 1: Using Build Scripts (Recommended)

#### Windows:
```cmd
# Step 1: Build the project
build.bat

# Step 2: Start the server (in one terminal)
run-server.bat

# Step 3: Start client(s) (in another terminal)
run-client.bat
```

#### Linux/Mac:
```bash
# Step 1: Build the project
./build.sh

# Step 2: Start the server (in one terminal)
./run-server.sh

# Step 3: Start client(s) (in another terminal)
./run-client.sh
```

### Method 2: Manual Compilation

```bash
# Create bin directory
mkdir bin

# Compile all source files
javac -d bin -sourcepath src src/**/*.java

# Run server
java -cp bin server.QuizServer

# Run client (in another terminal)
java -cp bin client.QuizClient
```

### Method 3: With Custom Port

```bash
# Server with custom port
java -cp bin server.QuizServer 9999

# Client connecting to custom port
java -cp bin client.QuizClient localhost 9999
```

---

## Team Split Guide

### Team Member 1: Server (Host)
**Files**: `QuizServer.java`, `ClientHandler.java`

**Responsibilities**:
- Implement ServerSocket
- Handle client connections
- Manage thread pool
- Implement broadcast mechanism
- Handle quiz lifecycle (start/stop)

**Key Concepts**: ServerSocket, multithreading, socket communication

---

### Team Member 2: Client
**Files**: `QuizClient.java`

**Responsibilities**:
- Implement Socket connection
- Handle message sending
- Implement message listener thread
- Handle disconnection/reconnection
- Process server responses

**Key Concepts**: Socket, I/O streams, background threading

---

### Team Member 3: Question Manager
**Files**: `QuestionManager.java`, `Question.java`

**Responsibilities**:
- Design Question model
- Implement CRUD operations
- Question validation
- Load default questions
- Question serialization/deserialization

**Key Concepts**: Data management, serialization

---

### Team Member 4: Scoring System
**Files**: `ScoringSystem.java`, `Student.java`, `QuizSession.java`

**Responsibilities**:
- Answer validation
- Score calculation algorithm
- Time-based bonus points
- Leaderboard generation
- Answer history tracking
- Thread-safe score updates

**Key Concepts**: Synchronization, concurrent collections, algorithms

---

### Team Member 5: UI
**Files**: `ServerUI.java`, `ClientUI.java`

**Responsibilities**:
- Design server dashboard
- Design client interface
- Timer implementation
- Real-time updates
- User interaction handling

**Key Concepts**: Swing/JavaFX, event handling, timers

---

## Features Implementation

### Core Features

#### 1. Multi-client Support
- **Implementation**: Thread pool in `QuizServer.java`
- **Thread Safety**: ConcurrentHashMap for client storage
- **Testing**: Connect 5+ clients simultaneously

#### 2. Real-time Question Broadcasting
- **Implementation**: `broadcast()` method in QuizServer
- **Protocol**: QUESTION message type
- **Flow**: Server → All connected clients

#### 3. Timer System
- **Client Side**: `javax.swing.Timer` in ClientUI
- **Visual Feedback**: Color changes (blue → red)
- **Auto-submit**: When time expires

#### 4. Scoring with Time Bonus
- **Algorithm**:
  ```
  If time < 50% of limit: points × 1.5
  If time < 75% of limit: points × 1.25
  Otherwise: base points
  ```

#### 5. Live Leaderboard
- **Update Trigger**: After each answer submission
- **Broadcast**: To all clients
- **Sorting**: By score, then correct answers, then name

#### 6. Result Broadcasting
- **Timing**: When quiz ends
- **Content**: Final leaderboard, individual scores
- **Display**: Pop-up dialog + leaderboard panel

### Extension Features

#### 7. Chat System
- **Implementation**: MESSAGE protocol type
- **Broadcasting**: Server relays to all clients
- **Display**: Scrollable text area

#### 8. Student Monitoring
- **Server View**: Connected students, scores, progress
- **Real-time**: Updates on each answer
- **Display**: Server dashboard

---

## Testing Guide

### Unit Testing Checklist

#### Server Testing
- [ ] Server starts on specified port
- [ ] Accepts multiple client connections
- [ ] Handles client disconnection gracefully
- [ ] Broadcasts messages to all clients
- [ ] Quiz lifecycle works correctly

#### Client Testing
- [ ] Connects to server successfully
- [ ] Receives and displays questions
- [ ] Timer works correctly
- [ ] Answer submission works
- [ ] Handles disconnection

#### Question Manager Testing
- [ ] Add question
- [ ] Remove question
- [ ] Get random questions
- [ ] Question validation

#### Scoring System Testing
- [ ] Correct answer scoring
- [ ] Time bonus calculation
- [ ] Leaderboard sorting
- [ ] Thread safety with concurrent answers

### Integration Testing

1. **Single Client Test**
   - Start server
   - Connect one client
   - Start quiz
   - Answer all questions
   - Verify final score

2. **Multiple Client Test**
   - Start server
   - Connect 3+ clients
   - Start quiz
   - Have clients answer at different speeds
   - Verify leaderboard accuracy

3. **Stress Test**
   - Connect 10+ clients
   - Start quiz
   - Submit answers rapidly
   - Monitor for race conditions

4. **Disconnect Test**
   - Connect multiple clients
   - Disconnect one during quiz
   - Verify server continues normally
   - Verify leaderboard updates

### Manual Testing Scenarios

**Scenario 1: Normal Quiz Flow**
```
1. Start server
2. Connect 3 students
3. Load questions (use default)
4. Start quiz
5. Students answer questions
6. Complete quiz
7. Verify results
```

**Scenario 2: Late Join**
```
1. Start server
2. Connect student A
3. Start quiz
4. Connect student B (during quiz)
5. Verify student B receives current question
```

**Scenario 3: Timeout**
```
1. Start quiz
2. Don't answer a question
3. Wait for timer to expire
4. Verify auto-submission
```

---

## Troubleshooting

### Common Issues

#### 1. "Address already in use" Error
**Problem**: Port 8888 is already in use
**Solution**: 
```bash
# Find process using port
netstat -ano | findstr :8888

# Kill the process or use different port
java -cp bin server.QuizServer 9999
```

#### 2. Client Cannot Connect
**Problem**: Connection refused
**Solutions**:
- Ensure server is running
- Check firewall settings
- Verify correct host/port
- Try localhost instead of IP address

#### 3. "Class not found" Error
**Problem**: Classpath issue
**Solution**:
```bash
# Make sure you're in the QuizHub directory
# Re-compile with correct classpath
javac -d bin -sourcepath src src/**/*.java
```

#### 4. UI Not Displaying
**Problem**: Swing UI issues
**Solution**:
- Ensure Java version supports Swing
- Check display settings
- Run with: `java -Djava.awt.headless=false -cp bin server.QuizServer`

#### 5. Questions Not Loading
**Problem**: QuestionManager initialization
**Solution**:
- Check QuestionManager constructor
- Verify loadDefaultQuestions() is called
- Add questions manually via UI

#### 6. Score Not Updating
**Problem**: Synchronization issue
**Solution**:
- Check network connection
- Verify answer submission
- Check server logs
- Ensure LEADERBOARD broadcast is working

### Debug Mode

Add logging to track issues:

```java
// In QuizServer.java
public void log(String message) {
    String logMessage = "[" + new Date() + "] " + message;
    System.out.println(logMessage);
    // Also writes to UI log area
}
```

Monitor server logs for:
- Client connections/disconnections
- Message exchanges
- Answer processing
- Errors

---

## Advanced Features (Future Enhancements)

1. **Database Integration**
   - Store questions in database
   - Save quiz results
   - Student history tracking

2. **Question Categories**
   - Subject-based organization
   - Difficulty levels
   - Random selection by category

3. **Time Limits per Quiz**
   - Overall quiz timer
   - Pause/resume functionality

4. **Authentication**
   - Student login with password
   - Teacher admin panel
   - Role-based access

5. **Result Analytics**
   - Performance graphs
   - Question difficulty analysis
   - Student progress tracking

6. **File Import/Export**
   - Import questions from CSV/JSON
   - Export results to file
   - Question bank sharing

7. **NIO Implementation**
   - Replace Socket with SocketChannel
   - Use Selector for efficient I/O
   - Non-blocking operations

8. **Encrypted Communication**
   - SSL/TLS sockets
   - Secure data transmission
   - Answer encryption

---

## Learning Outcomes

After completing this project, you will understand:

1. **Socket Programming**
   - TCP/IP client-server architecture
   - ServerSocket and Socket classes
   - Network I/O streams

2. **Multithreading**
   - Thread creation and management
   - Thread pools (ExecutorService)
   - Concurrent programming

3. **Synchronization**
   - Synchronized methods and blocks
   - Thread-safe collections
   - Race condition prevention

4. **Protocol Design**
   - Application-layer protocols
   - Message formats
   - Request-response patterns

5. **Real-time Systems**
   - Broadcasting
   - Live updates
   - Timer management

6. **GUI Programming**
   - Swing components
   - Event handling
   - Multi-panel layouts

---

## Conclusion

QuizHub demonstrates comprehensive Java Network Programming concepts in a practical, real-world application. The modular design allows for easy team collaboration and future enhancements.

**Key Takeaways**:
- Client-server architecture patterns
- Thread-safe concurrent programming
- Real-time communication protocols
- User interface integration with networking

**Next Steps**:
1. Build and run the application
2. Test with multiple clients
3. Customize questions for your domain
4. Implement additional features
5. Deploy on network for actual use

For questions or issues, refer to the troubleshooting section or review the inline code comments.

---

**Happy Coding! 🚀**

