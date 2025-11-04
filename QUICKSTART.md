# QuizHub - Quick Start Guide

## 🚀 Getting Started in 5 Minutes

### Method 1: Run from IDE (Easiest - No Scripts Required!)

#### Step 1: Open Project in Your IDE
- IntelliJ IDEA, Eclipse, VS Code, or NetBeans
- Import the QuizHub project

#### Step 2: Start the Server
1. Navigate to: `src/server/QuizServer.java`
2. **Right-click** on the file → **Run 'QuizServer.main()'**
3. Server UI window opens automatically
4. Click **"Start Server"** button
5. ✅ Server is now running on port 8888

#### Step 3: Start Client(s) (Students)
1. Navigate to: `src/client/QuizClient.java`
2. **Right-click** on the file → **Run 'QuizClient.main()'**
3. Client UI opens with login screen
4. Enter:
   - Server Host: `localhost`
   - Student ID: `S001` (or any ID)
   - Your Name: `John Doe`
5. Click **"Connect to Quiz"**
6. ✅ Student connected!

**To add more students**: Repeat Step 3 in new run windows (enable multiple instances in your IDE)

---

### Method 2: Run from Command Line (No Scripts)

#### Step 1: Compile the Project
Open terminal/command prompt in the QuizHub folder:

```cmd
javac -d bin src\server\*.java src\client\*.java src\model\*.java src\common\*.java
```

#### Step 2: Start the Server
Open **Terminal 1**:

```cmd
java -cp bin server.QuizServer
```

The Server UI opens → Click **"Start Server"**

#### Step 3: Start Clients (Students)
Open **Terminal 2** (for Student 1):

```cmd
java -cp bin client.QuizClient
```

Open **Terminal 3** (for Student 2):

```cmd
java -cp bin client.QuizClient
```

Open **Terminal 4** (for Student 3):

```cmd
java -cp bin client.QuizClient
```

Each client UI opens → Enter details and connect

---

### Method 3: Using Build Scripts (Optional)

**Windows (PowerShell):**
```powershell
.\build.ps1         # Build the project
.\run-server.ps1    # Start the server
.\run-client.ps1    # Start a client
```

**Linux/Mac:**
```bash
chmod +x build.sh
./build.sh          # Build the project
./run-server.sh     # Start the server
./run-client.sh     # Start a client
```

---

## Step 4: Conduct the Quiz

On the **Server Dashboard**:
1. Verify students are connected (check "Students" count)
2. Click **"Start Quiz"** button
3. Click **"Next Question"** to send questions (or wait for auto-advance)
4. Monitor live leaderboard
5. Click **"End Quiz"** when finished

On **Client** (Students):
- Read the question
- Select your answer (A, B, C, or D)
- Click **"Submit Answer"** before time runs out
- See your score update in real-time
- Check leaderboard to see your ranking

---

## 📋 Quick Reference

### Default Configuration
- **Port:** 8888
- **Time per question:** 30 seconds
- **Points per question:** 10 (with time bonus up to 50%)

### Main Classes to Run

**Server (Teacher):**
- Main class: `server.QuizServer`
- Optional args: `8888` (port)

**Client (Students):**
- Main class: `client.QuizClient`
- Optional args: `localhost 8888` (host port)

### Command Line Quick Commands

```bash
# Compile all
javac -d bin src/**/*.java

# Run server
java -cp bin server.QuizServer

# Run client
java -cp bin client.QuizClient

# Run with custom port
java -cp bin server.QuizServer 9999
java -cp bin client.QuizClient localhost 9999
```

### Running Across Network

**On Server Computer:**
1. Find your IP address:
   - Windows: `ipconfig` → Look for IPv4 Address
   - Linux/Mac: `ifconfig` or `ip addr`
2. Example IP: `192.168.1.100`
3. Start server: `java -cp bin server.QuizServer`

**On Client Computers:**
```cmd
java -cp bin client.QuizClient 192.168.1.100 8888
```
Or enter IP in the login dialog

**Firewall:** Allow port 8888 through firewall

---

## 🎯 Sample Quiz Flow

1. **Teacher**: Run `QuizServer.main()` → Start Server → Load Questions
2. **Students**: Run `QuizClient.main()` multiple times → Connect
3. **Teacher**: Click "Start Quiz"
4. **Question 1**: 30 seconds → Students answer → See results
5. **Questions 2-8**: Teacher clicks "Next Question" → Repeat
6. **End**: Teacher clicks "End Quiz" → View final leaderboard

---

## ⚠️ Common Issues

**Can't connect?**
- Ensure server is running first
- Use `localhost` if on same machine
- Check firewall settings

**Build errors?**
- Ensure JDK 8+ is installed
- Run `java -version` to verify
- Compile from project root directory

**Port conflict?**
- Change port: `java -cp bin server.QuizServer 9999`
- Update clients: `java -cp bin client.QuizClient localhost 9999`

**Class not found?**
- Ensure you're in QuizHub directory
- Check classpath: `-cp bin`
- Recompile all files

**Multiple clients in IDE?**
- Enable "Allow parallel run" in run configuration
- Or run client from command line multiple times

---

## 💡 Pro Tips

1. **IDE Users**: Set up run configurations once, then just click "Run" each time
2. **Command Line**: Open multiple terminal windows before starting
3. **Testing**: Start with 2-3 clients first, then add more
4. **Debugging**: Check server logs in the UI for connection issues
5. **Network Mode**: Test on localhost first, then try network mode

---

## 📞 Need More Help?

- **Full Guide**: See [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)
- **Team Work**: See [TEAM_WORK_DIVISION.md](TEAM_WORK_DIVISION.md)
- **Architecture**: See [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)

---

**That's it! You're ready to run QuizHub! 🎉**
