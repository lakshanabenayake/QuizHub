# How to Run QuizHub - Complete Guide

## 🎯 Multiple Ways to Run the Application

---

## ✅ Method 1: Using the New Launcher Classes (EASIEST!)

I've created two simple launcher files in the `src/` folder:
- `ServerLauncher.java` - Starts the server
- `ClientLauncher.java` - Starts the client

### In Your IDE (IntelliJ IDEA, Eclipse, VS Code, NetBeans):

**To Start Server:**
1. Open `src/ServerLauncher.java`
2. Right-click → **Run 'ServerLauncher.main()'** or **Run Java**
3. Server UI opens → Click "Start Server"

**To Start Client(s):**
1. Open `src/ClientLauncher.java`
2. Right-click → **Run 'ClientLauncher.main()'** or **Run Java**
3. Client UI opens → Enter details and connect
4. Repeat for multiple students

---

## ✅ Method 2: Command Line (No IDE Required)

Open Command Prompt or PowerShell in the QuizHub folder:

### Step 1: Compile Everything
```cmd
javac -d bin src\server\*.java src\client\*.java src\model\*.java src\common\*.java src\*.java
```

### Step 2: Run Server (Terminal 1)
```cmd
java -cp bin server.QuizServer
```
OR using the launcher:
```cmd
java -cp bin ServerLauncher
```

### Step 3: Run Client(s) (Terminal 2, 3, 4...)
```cmd
java -cp bin client.QuizClient
```
OR using the launcher:
```cmd
java -cp bin ClientLauncher
```

**To run multiple clients**: Open multiple Command Prompt windows and run the client command in each.

---

## ✅ Method 3: Using PowerShell Scripts

If the scripts exist, use them:

```powershell
.\build.ps1         # Compile everything
.\run-server.ps1    # Start server
.\run-client.ps1    # Start client (in another window)
```

---

## ✅ Method 4: Direct Java Execution (Specific Class)

### For Server:
```cmd
cd "D:\Network programming\Online Qui System (Client-Server Based)\QuizHub"
javac -d bin src\server\*.java src\client\*.java src\model\*.java src\common\*.java
java -cp bin server.QuizServer
```

### For Client:
```cmd
cd "D:\Network programming\Online Qui System (Client-Server Based)\QuizHub"
java -cp bin client.QuizClient
```

---

## 🔧 Troubleshooting IDE Issues

### If "Run" option doesn't appear in your IDE:

#### For IntelliJ IDEA:
1. Right-click on the project root → **Mark Directory as** → **Sources Root**
2. Go to **File** → **Project Structure** → **Modules**
3. Ensure `src` is marked as "Sources"
4. Click **Apply** and **OK**
5. Try right-clicking `ServerLauncher.java` again

#### For Eclipse:
1. Right-click project → **Properties**
2. Go to **Java Build Path** → **Source** tab
3. Add the `src` folder if not present
4. Clean and rebuild: **Project** → **Clean**
5. Try running `ServerLauncher.java`

#### For VS Code:
1. Install "Extension Pack for Java" if not installed
2. Press `F5` or click **Run** → **Run Without Debugging**
3. Select `ServerLauncher` or `ClientLauncher`

#### For NetBeans:
1. Right-click project → **Properties**
2. Go to **Sources**
3. Ensure source package folder is set to `src`
4. Right-click `ServerLauncher.java` → **Run File**

---

## 🚀 Quick Start (Command Line - No IDE)

### All-in-One Commands:

**Windows Command Prompt:**
```cmd
cd "D:\Network programming\Online Qui System (Client-Server Based)\QuizHub"

REM Compile all
javac -d bin src\server\*.java src\client\*.java src\model\*.java src\common\*.java src\*.java

REM Open first terminal - Run Server
start cmd /k "java -cp bin server.QuizServer"

REM Wait 3 seconds then open clients
timeout /t 3
start cmd /k "java -cp bin client.QuizClient"
start cmd /k "java -cp bin client.QuizClient"
start cmd /k "java -cp bin client.QuizClient"
```

**Windows PowerShell:**
```powershell
cd "D:\Network programming\Online Qui System (Client-Server Based)\QuizHub"

# Compile all
javac -d bin src\server\*.java src\client\*.java src\model\*.java src\common\*.java src\*.java

# Run Server in new window
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp bin server.QuizServer"

# Wait 3 seconds
Start-Sleep -Seconds 3

# Run 3 clients in new windows
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp bin client.QuizClient"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp bin client.QuizClient"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp bin client.QuizClient"
```

---

## 📦 Alternative: Create BAT Files

Create these files in the QuizHub root folder:

### `run-server-simple.bat`
```batch
@echo off
echo Starting QuizHub Server...
cd /d "%~dp0"
javac -d bin src\server\*.java src\client\*.java src\model\*.java src\common\*.java 2>nul
java -cp bin server.QuizServer
pause
```

### `run-client-simple.bat`
```batch
@echo off
echo Starting QuizHub Client...
cd /d "%~dp0"
java -cp bin client.QuizClient
pause
```

Then just double-click these files to run!

---

## ✅ Verification Checklist

After starting the server, you should see:
- [ ] Server UI window opens
- [ ] "Start Server" button is visible
- [ ] After clicking "Start Server", log shows "Server started on port 8888"

After starting a client, you should see:
- [ ] Client UI window opens with login form
- [ ] Fields for Server Host, Student ID, and Name
- [ ] "Connect to Quiz" button

---

## 🎯 Recommended Workflow

1. **First Time Setup:**
   ```cmd
   cd "D:\Network programming\Online Qui System (Client-Server Based)\QuizHub"
   javac -d bin src\server\*.java src\client\*.java src\model\*.java src\common\*.java src\*.java
   ```

2. **Every Time You Run:**
   - Terminal 1: `java -cp bin server.QuizServer`
   - Terminal 2: `java -cp bin client.QuizClient`
   - Terminal 3: `java -cp bin client.QuizClient`
   - Terminal 4: `java -cp bin client.QuizClient`

3. **Or use the launchers:**
   - Right-click `src/ServerLauncher.java` → Run
   - Right-click `src/ClientLauncher.java` → Run (multiple times)

---

## 💡 Pro Tips

1. **Keep a terminal open** with the server running so you can see the logs
2. **Use multiple monitors** if available - server on one, clients on another
3. **Test with 2-3 clients first** before adding more
4. **Check the bin folder exists** - if not, the javac command will create it
5. **Make sure Java is in your PATH** - run `java -version` to verify

---

## 🆘 Still Having Issues?

If none of the above works, try this minimal test:

```cmd
cd "D:\Network programming\Online Qui System (Client-Server Based)\QuizHub\src\server"
javac QuizServer.java
java QuizServer
```

If this works, the issue is with classpath. If this doesn't work, there might be a Java installation issue.

---

**Need more help? Check the server logs for error messages!**

