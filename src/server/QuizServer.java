package server;

import model.*;
import common.Protocol;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * QuizServer - Main server class
 * Demonstrates: ServerSocket, Multithreading, Synchronization
 */
public class QuizServer {
    private int port;
    private ServerSocket serverSocket;
    private boolean running;
    private List<ClientHandler> clients;
    private QuizSession session;
    private QuestionManager questionManager;
    private ScoringSystem scoringSystem;
    private ExecutorService threadPool;
    private ServerUI ui;

    // Track answers for current question
    private Map<Integer, Set<String>> questionAnswers; // questionId -> Set of studentIds who answered
    private ScheduledFuture<?> currentQuestionTimer;
    private ScheduledExecutorService scheduler;

    // Master Timer Management
    private ScheduledFuture<?> timerBroadcastTask;
    private int currentQuestionTimeLimit = 0;
    private long questionStartTime = 0;
    private boolean timerPaused = false;
    private int pausedTimeRemaining = 0;

    public QuizServer(int port) {
        this.port = port;
        this.clients = new CopyOnWriteArrayList<>();
        this.questionManager = new QuestionManager();
        this.session = new QuizSession("SESSION-" + System.currentTimeMillis());
        this.scoringSystem = new ScoringSystem(session);
        this.threadPool = Executors.newCachedThreadPool();
        this.questionAnswers = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2); // Increased for timer broadcasts
        this.running = false;
    }

    /**
     * Starts the server
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            log("Server started on port " + port);

            // Accept client connections in a separate thread
            threadPool.execute(() -> acceptClients());

        } catch (IOException e) {
            log("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Accepts incoming client connections
     */
    private void acceptClients() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                threadPool.execute(handler);

                log("New client connection from: " + clientSocket.getInetAddress());
                updateUI();

            } catch (IOException e) {
                if (running) {
                    log("Error accepting client: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Starts a quiz with selected questions
     */
    public synchronized void startQuiz(List<Question> questions) {
        if (session.isActive()) {
            log("Quiz already in progress!");
            return;
        }

        session = new QuizSession("SESSION-" + System.currentTimeMillis());
        scoringSystem = new ScoringSystem(session);

        // Add questions to session
        for (Question q : questions) {
            session.addQuestion(q);
        }

        // Re-add connected students
        for (ClientHandler handler : clients) {
            if (handler.getStudent() != null) {
                session.addStudent(handler.getStudent());
            }
        }

        session.start();
        log("Quiz started with " + questions.size() + " questions");

        // Notify all clients
        broadcast(Protocol.QUIZ_START, String.valueOf(questions.size()));

        // Start first question after a short delay
        threadPool.execute(() -> {
            try {
                Thread.sleep(3000); // 3 second countdown
                sendNextQuestion();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Sends the next question to all clients
     */
    public synchronized void sendNextQuestion() {
        if (!session.isActive()) return;

        Question question = session.nextQuestion();
        if (question != null) {
            log("Sending question " + (session.getCurrentQuestionIndex() + 1) +
                " of " + session.getTotalQuestions());

            broadcast(Protocol.QUESTION, question.toProtocolString());

            // Member 5 - Start master timer synchronization for this question
            startMasterTimer(question.getTimeLimit());

            // Enable timer controls in UI
            if (ui != null) {
                ui.setTimerControlsEnabled(true);
                // 6️⃣ Update quiz progress in UI
                ui.updateQuizProgress(session.getCurrentQuestionIndex() + 1, session.getTotalQuestions());
            }

            updateUI();

            // Auto-advance after time limit (optional)
            scheduleNextQuestion(question.getTimeLimit());
        } else {
            endQuiz();
        }
    }

    /**
     * Member 5 - Starts master timer and broadcasts sync messages to all clients
     * This demonstrates network synchronization and event-driven updates
     */
    private void startMasterTimer(int timeLimit) {
        // Stop any existing timer
        stopMasterTimer();

        // Initialize timer state
        currentQuestionTimeLimit = timeLimit;
        questionStartTime = System.currentTimeMillis();
        timerPaused = false;
        pausedTimeRemaining = 0;

        // Reset answer tracking for new question
        if (session.getCurrentQuestion() != null) {
            questionAnswers.put(session.getCurrentQuestion().getId(), ConcurrentHashMap.newKeySet());
        }

        // Schedule periodic timer broadcasts (every 1 second)
        timerBroadcastTask = scheduler.scheduleAtFixedRate(() -> {
            int remaining = getRemainingTime();
            String state = getTimerState(remaining);

            // Broadcast to all clients (network synchronization)
            broadcast(Protocol.TIMER_SYNC, remaining + "~" + state);

            // Update server UI (thread-safe update)
            if (ui != null) {
                ui.updateMasterTimer(remaining, state);

                // Update answer count display
                if (session.getCurrentQuestion() != null) {
                    int questionId = session.getCurrentQuestion().getId();
                    Set<String> answered = questionAnswers.get(questionId);
                    int answeredCount = answered != null ? answered.size() : 0;
                    ui.updateAnswerCount(answeredCount, getConnectedClientsCount());
                }
            }
        }, 0, 1, TimeUnit.SECONDS);

        log("Master timer started: " + timeLimit + " seconds");
    }

    /**
     * Stops master timer broadcasts
     */
    private void stopMasterTimer() {
        if (timerBroadcastTask != null) {
            timerBroadcastTask.cancel(false);
            timerBroadcastTask = null;
        }
        timerPaused = false;
        pausedTimeRemaining = 0;
    }

    /**
     * Calculates remaining time based on current state
     */
    private int getRemainingTime() {
        if (timerPaused) {
            // Return the frozen time when paused
            return pausedTimeRemaining;
        }

        // Calculate time elapsed since question started
        long elapsedMs = System.currentTimeMillis() - questionStartTime;
        int elapsedSeconds = (int)(elapsedMs / 1000);
        int remaining = currentQuestionTimeLimit - elapsedSeconds;
        return Math.max(0, remaining);
    }

    /**
     * Determines timer state based on remaining time
     * @return "normal", "warning", or "critical"
     */
    private String getTimerState(int remainingSeconds) {
        if (remainingSeconds <= 10) {
            return "critical";  // Last 10 seconds - RED
        } else if (remainingSeconds <= 30) {
            return "warning";   // Last 30 seconds - ORANGE
        } else {
            return "normal";    // Normal - BLUE
        }
    }

    /**
     * Member 5 - Pauses the quiz timer (administrative control)
     * Event-driven: UI button triggers network event affecting all clients
     */
    public synchronized void pauseTimer() {
        if (!timerPaused && timerBroadcastTask != null) {
            // Get current remaining time before pausing
            pausedTimeRemaining = getRemainingTime();
            timerPaused = true;

            // Broadcast pause control to all clients
            broadcast(Protocol.TIMER_CONTROL, "pause~" + pausedTimeRemaining);
            log("⏸ Timer PAUSED at " + pausedTimeRemaining + " seconds remaining");
        }
    }

    /**
     * Member 5 - Resumes the quiz timer
     */
    public synchronized void resumeTimer() {
        if (timerPaused && timerBroadcastTask != null) {
            // Calculate new start time based on paused remaining time
            // If we have 45 seconds remaining and total was 60:
            // Time already used = 60 - 45 = 15 seconds
            // New start time = now - 15 seconds (so timer continues from 45s remaining)
            int timeAlreadyUsed = currentQuestionTimeLimit - pausedTimeRemaining;
            questionStartTime = System.currentTimeMillis() - (timeAlreadyUsed * 1000L);

            // Unpause
            timerPaused = false;

            // Broadcast resume control to all clients with the remaining time
            broadcast(Protocol.TIMER_CONTROL, "resume~" + pausedTimeRemaining);
            log("▶ Timer RESUMED - continuing from " + pausedTimeRemaining + " seconds");
        }
    }

    /**
     * Member 5 - Extends current question time by specified seconds
     */
    public synchronized void extendTimer(int additionalSeconds) {
        if (timerBroadcastTask != null) {
            // Always increase the total time limit
            currentQuestionTimeLimit += additionalSeconds;

            if (timerPaused) {
                // If paused, add to the frozen time
                pausedTimeRemaining += additionalSeconds;
                log("⏱ Timer extended by " + additionalSeconds + "s while PAUSED (now at: " + pausedTimeRemaining + "s)");
            } else {
                // If running, the increased time limit will automatically give more time
                log("⏱ Timer extended by " + additionalSeconds + "s while RUNNING (new remaining: " + getRemainingTime() + "s)");
            }

            // Broadcast extend control to all clients
            broadcast(Protocol.TIMER_CONTROL, "extend~" + additionalSeconds);
        }
    }

    /**
     * Member 5 - Skips current question (no scores recorded)
     */
    public synchronized void skipCurrentQuestion() {
        log("Skipping current question");
        stopMasterTimer();

        if (session.isActive() && session.hasMoreQuestions()) {
            sendNextQuestion();
        } else if (session.isActive()) {
            endQuiz();
        }
    }

    /**
     * Member 5 - Forces next question immediately
     */
    public synchronized void forceNextQuestion() {
        log("Forcing next question");
        stopMasterTimer();

        if (session.isActive() && session.hasMoreQuestions()) {
            sendNextQuestion();
        } else if (session.isActive()) {
            endQuiz();
        }
    }

    /**
     * Ends the quiz and broadcasts results
     */
    public synchronized void endQuiz() {
        if (!session.isActive()) return;

        // Stop master timer
        stopMasterTimer();

        // Disable timer controls in UI
        if (ui != null) {
            ui.setTimerControlsEnabled(false);
        }

        session.end();
        log("Quiz ended");

        // Generate results
        String results = scoringSystem.generateResultsSummary();
        log(results);

        // Broadcast results to all clients
        broadcast(Protocol.QUIZ_END, scoringSystem.getLeaderboardProtocolString());
        updateUI();
    }

    /**
     * Schedules next question after time limit
     */
    private void scheduleNextQuestion(int timeLimit) {
        threadPool.execute(() -> {
            try {
                Thread.sleep((timeLimit + 5) * 1000L); // Add 5 seconds buffer
                if (session.isActive() && session.hasMoreQuestions()) {
                    sendNextQuestion();
                } else if (session.isActive()) {
                    endQuiz();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Gets count of students who answered current question
     */
    public int getAnsweredCount(int questionId) {
        Set<String> answered = questionAnswers.get(questionId);
        return answered != null ? answered.size() : 0;
    }

    /**
     * Checks if a specific student has answered the current question
     */
    public boolean hasStudentAnswered(String studentId) {
        if (!session.isActive() || session.getCurrentQuestion() == null) {
            return false;
        }
        int questionId = session.getCurrentQuestion().getId();
        Set<String> answered = questionAnswers.get(questionId);
        return answered != null && answered.contains(studentId);
    }

    /**
     * Processes student answer
     */
    public ScoringSystem.AnswerResult processAnswer(String studentId, int questionId,
                                                    int answer, long timeTaken) {
        // Track that this student has answered the question
        questionAnswers.computeIfAbsent(questionId, k -> ConcurrentHashMap.newKeySet()).add(studentId);

        return scoringSystem.recordAnswer(studentId, questionId, answer, timeTaken);
    }

    /**
     * Adds a student to the session
     */
    public synchronized void addStudent(ClientHandler handler, Student student) {
        if (session.isActive()) {
            session.addStudent(student);
        }
        updateUI();
    }

    /**
     * Removes a student from the session
     */
    public synchronized void removeStudent(ClientHandler handler) {
        clients.remove(handler);
        if (handler.getStudent() != null) {
            session.removeStudent(handler.getStudent().getStudentId());
        }
        updateUI();
    }

    /**
     * Broadcasts a message to all connected clients
     */
    public void broadcast(String type, String data) {
        for (ClientHandler client : clients) {
            if (client.isRunning()) {
                client.sendMessage(type, data);
            }
        }
    }

    /**
     * Broadcasts leaderboard to all clients
     */
    public void broadcastLeaderboard() {
        String leaderboardData = scoringSystem.getLeaderboardProtocolString();
        broadcast(Protocol.LEADERBOARD, leaderboardData);
    }

    /**
     * Broadcasts student list
     */
    public void broadcastStudentList() {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (ClientHandler handler : clients) {
            if (handler.getStudent() != null) {
                if (count > 0) sb.append("||");
                sb.append(handler.getStudent().getName());
                count++;
            }
        }
        broadcast(Protocol.MESSAGE, "Students online: " + count);
    }

    /**
     * Broadcasts a chat message
     */
    public void broadcastMessage(String message) {
        broadcast(Protocol.MESSAGE, message);
        log(message);
    }

    /**
     * Called when a student submits an answer
     * Checks if we should auto-advance to next question
     */
    public synchronized void onStudentAnswered(int questionId) {
        if (!session.isActive()) return;

        Question currentQuestion = session.getCurrentQuestion();
        if (currentQuestion == null || currentQuestion.getId() != questionId) return;

        // Get number of students who have answered this question
        Set<String> answeredStudents = questionAnswers.get(questionId);
        if (answeredStudents == null) return;

        int answeredCount = answeredStudents.size();
        int totalStudents = session.getStudentCount();

        log("Question " + questionId + ": " + answeredCount + "/" + totalStudents + " students answered");

        // Auto-advance after a short delay (3 seconds) to show results
        // This gives students time to see their result before next question
        if (currentQuestionTimer != null) {
            currentQuestionTimer.cancel(false);
        }

        currentQuestionTimer = scheduler.schedule(() -> {
            if (session.isActive() && session.hasMoreQuestions()) {
                sendNextQuestion();
            } else if (session.isActive()) {
                endQuiz();
            }
        }, 3, TimeUnit.SECONDS);
    }

    /**
     * Stops the server
     */
    public void stop() {
        running = false;

        // Cancel any pending question timer
        if (currentQuestionTimer != null) {
            currentQuestionTimer.cancel(false);
        }

        // Shutdown scheduler
        scheduler.shutdown();

        // Close all client connections
        for (ClientHandler client : clients) {
            client.close();
        }
        clients.clear();

        // Shutdown thread pool
        threadPool.shutdown();

        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log("Error closing server socket: " + e.getMessage());
        }

        log("Server stopped");
    }

    /**
     * Logs a message
     */
    public void log(String message) {
        String logMessage = "[" + new Date() + "] " + message;
        System.out.println(logMessage);
        if (ui != null) {
            ui.appendLog(logMessage);
        }
    }

    /**
     * Updates UI
     */
    private void updateUI() {
        if (ui != null) {
            ui.updateStatus();
        }
    }

    /**
     * Sets the UI reference
     */
    public void setUI(ServerUI ui) {
        this.ui = ui;
    }

    // Getters
    public QuizSession getSession() { return session; }
    public QuestionManager getQuestionManager() { return questionManager; }
    public ScoringSystem getScoringSystem() { return scoringSystem; }
    public int getConnectedClientsCount() { return clients.size(); }
    public boolean isRunning() { return running; }
    public List<ClientHandler> getClients() { return new ArrayList<>(clients); }

    /**
     * Main method to start server
     */
    public static void main(String[] args) {
        int port = Protocol.DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number, using default: " + port);
            }
        }

        // Make port effectively final for lambda
        final int serverPort = port;

        // Start server with UI
        javax.swing.SwingUtilities.invokeLater(() -> {
            QuizServer server = new QuizServer(serverPort);
            ServerUI ui = new ServerUI(server);
            server.setUI(ui);
            ui.setVisible(true);
        });
    }

    public synchronized void stopServer() {
    try {
        log("Stopping server...");

        running = false;

        // 1. Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }

        // 2. Disconnect all clients
        for (ClientHandler handler : clients) {
            try {
                handler.disconnect();
            } catch (Exception ignored) {}
        }
        clients.clear();

        // 3. Stop timers/executors
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdownNow();
        }

        // 4. Notify UI
        if (ui != null) {
            ui.setServerStopped();
        }

        log("Server stopped successfully.");
        
    } catch (Exception e) {
        log("Error stopping server: " + e.getMessage());
    }
}


}
