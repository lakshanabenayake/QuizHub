# QuizHub - Quick Start Guide

## 🚀 Getting Started in 5 Minutes

### Step 1: Build the Application

**Windows:**
```cmd
build.bat
```

**Linux/Mac:**
```bash
chmod +x build.sh
./build.sh
```

### Step 2: Start the Server (Teacher)

**Windows:**
```cmd
run-server.bat
```

**Linux/Mac:**
```bash
./run-server.sh
```

You'll see the Teacher Dashboard window:
- Click **"Start Server"** button
- Questions are loaded automatically
- Wait for students to connect

### Step 3: Start Clients (Students)

**Windows:** Run `run-client.bat` (open multiple terminals for multiple students)

**Linux/Mac:** Run `./run-client.sh`

On each client:
1. Enter Server Host: `localhost`
2. Enter Student ID: e.g., `S001`
3. Enter Your Name: e.g., `John Doe`
4. Click **"Connect to Quiz"**

### Step 4: Conduct the Quiz

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

### Keyboard Shortcuts
- Students can press Enter to submit answer
- Chat messages can be sent with Enter key

### Features Available
✅ Multiple concurrent students
✅ Real-time timer countdown
✅ Automatic answer submission on timeout
✅ Live leaderboard updates
✅ Time-based scoring bonus
✅ Chat functionality
✅ Question management (add/remove)
✅ Final results summary

---

## 🎯 Sample Quiz Flow

1. **Teacher**: Start Server → Load Questions → Start Quiz
2. **Students**: Connect → Wait for quiz start
3. **Question 1**: 30 seconds → Students answer → See results
4. **Question 2-8**: Repeat
5. **End**: View final leaderboard and scores

---

## ⚠️ Common Issues

**Can't connect?**
- Ensure server is running first
- Use `localhost` if on same machine
- Check firewall settings

**Build errors?**
- Ensure JDK 8+ is installed
- Run `java -version` to verify

**Port conflict?**
- Edit the port in Protocol.java
- Or start with: `java -cp bin server.QuizServer 9999`

---

## 📞 Need Help?

See **DEVELOPMENT_GUIDE.md** for:
- Detailed architecture explanation
- Team collaboration guide
- Advanced features
- Troubleshooting steps
- Customization options

