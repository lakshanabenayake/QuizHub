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

    public QuizServer(int port) {
        this.port = port;
        this.clients = new CopyOnWriteArrayList<>();
        this.questionManager = new QuestionManager();
        this.session = new QuizSession("SESSION-" + System.currentTimeMillis());
        this.scoringSystem = new ScoringSystem(session);
        this.threadPool = Executors.newCachedThreadPool();
        this.questionAnswers = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
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
    /**
 * Accepts incoming client connections
 */
private void acceptClients() {
    while (running) {
        try {
            Socket clientSocket = serverSocket.accept();   // blocks here
            ClientHandler handler = new ClientHandler(clientSocket, this);
            clients.add(handler);
            threadPool.execute(handler);

            log("New client connection from: " + clientSocket.getInetAddress());
            updateUI();

        } catch (IOException e) {
            // When stopping, ServerSocket.close() will make accept() throw.
            // Break out cleanly instead of logging it as an error.
            if (!running || serverSocket == null || serverSocket.isClosed()) {
                log("Server socket closed. Stopping accept loop.");
                break;
            } else {
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

    // NEW: cancel any pending timer from the previous question
    if (currentQuestionTimer != null) {
        currentQuestionTimer.cancel(false);
        currentQuestionTimer = null;
    }

    Question question = session.nextQuestion();
    if (question != null) {
        // NEW: reset per-question answered set
        questionAnswers.put(question.getId(), ConcurrentHashMap.newKeySet());

        log("Sending question " + (session.getCurrentQuestionIndex() + 1) +
            " of " + session.getTotalQuestions());

        broadcast(Protocol.QUESTION, question.toProtocolString());
        updateUI();

        // keep your existing time-limit auto advance
        scheduleNextQuestion(question.getTimeLimit());
    } else {
        endQuiz();
    }
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
     * Ends the quiz and broadcasts results
     */
    /**
 * Ends the quiz and broadcasts results
 */
public synchronized void endQuiz() {
    if (!session.isActive()) return;

    session.end();
    log("Quiz ended");

    // Generate results (console/UI only)
    String results = scoringSystem.generateResultsSummary();
    log(results);

    // Send results to all clients
    broadcast(Protocol.QUIZ_END, scoringSystem.getLeaderboardProtocolString());

// --- Save final clean leaderboard file (ONE CSV) ---
try {
    File outFile = new File("quiz_final_clean.csv");
    PrintWriter writer = new PrintWriter(new FileWriter(outFile, false));
    writer.println("rank,name,score,correct,answered,accuracy_pct,avg_response_ms,total_time_ms");

    int rank = 1;
    // Use the existing leaderboard (List<Student>)
    java.util.List<model.Student> board = scoringSystem.getLeaderboard();
    for (model.Student s : board) {
        // base stats from Student object
        int score    = s.getScore();
        int correct  = s.getCorrectAnswers();
        int answered = s.getAnsweredQuestions();
        double acc   = s.getAccuracy(); // already in percent (e.g., 83.3)

        // compute timing from answer history
        java.util.List<server.ScoringSystem.AnswerRecord> hist =
                scoringSystem.getAnswerHistory(s.getStudentId());
        long totalMs = 0L;
        int  count   = 0;
        if (hist != null) {
            for (server.ScoringSystem.AnswerRecord r : hist) {
                long t = Math.max(0L, r.getTimeTaken());
                totalMs += t;
                count++;
            }
        }
        long avgMs = (count > 0) ? (totalMs / count) : 0L;

        writer.printf(java.util.Locale.US,
            "%d,%s,%d,%d,%d,%.1f,%d,%d%n",
            rank++,
            s.getName(),
            score,
            correct,
            answered,
            acc,
            avgMs,
            totalMs
        );
    }

    writer.close();
    log("✅ Saved final clean leaderboard to: " + outFile.getAbsolutePath());
} catch (Exception ex) {
    log("⚠️ CSV save failed: " + ex.getMessage());
}

    updateUI();
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
   /**
 * Adds a student to the session and announces the join
 */
public synchronized void addStudent(ClientHandler handler, Student student) {
    if (student == null) {
        updateUI();
        return;
    }

    // Keep existing behavior: only register into the active session
    if (session.isActive()) {
        session.addStudent(student);
    }

    // NEW: broadcast a JOIN announcement to all clients
    String joinMsg = ChatHub.announce("JOIN", student.getName(), "joined");
    broadcast(Protocol.MESSAGE, joinMsg);
    log(joinMsg);   // also into server log

    updateUI();
}

    /**
     * Removes a student from the session
     */
    public synchronized void removeStudent(ClientHandler handler) {
    // Keep a copy of the student before removing
    Student s = (handler != null) ? handler.getStudent() : null;

    // Remove handler from active clients list
    clients.remove(handler);

    // Remove from session if active
    if (s != null) {
        session.removeStudent(s.getStudentId());

        // Broadcast leave message before clearing reference
        String leaveMsg = server.ChatHub.announce("LEAVE", s.getName(), "left");
        broadcast(Protocol.MESSAGE, leaveMsg);
        log(leaveMsg); // also log to server console
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
    /**
 * Called when a student submits an answer.
 * Only auto-advance when ALL students answered this question.
 */
public synchronized void onStudentAnswered(int questionId) {
    if (!session.isActive()) return;

    Question currentQuestion = session.getCurrentQuestion();
    if (currentQuestion == null || currentQuestion.getId() != questionId) return;

    // How many answered this specific question?
    Set<String> answeredStudents = questionAnswers.get(questionId);
    if (answeredStudents == null) return;

    int answeredCount = answeredStudents.size();
    int totalStudents = session.getStudentCount();

    log("Question " + questionId + ": " + answeredCount + "/" + totalStudents + " students answered");

    // Only schedule auto-advance when all students have answered
    if (answeredCount >= Math.max(1, totalStudents)) {
        // Small delay to let clients see their result before advancing
        if (currentQuestionTimer != null) currentQuestionTimer.cancel(false);
        currentQuestionTimer = scheduler.schedule(() -> {
            if (session.isActive() && session.hasMoreQuestions()) {
                sendNextQuestion();
            } else if (session.isActive()) {
                endQuiz();
            }
        }, 3, TimeUnit.SECONDS);   // change 3 -> 6/8 if you want a longer pause
    }
}


    /**
     * Stops the server
     */
    /**
 * Stops the server
 */
public void stop() {
    if (!running) return;
    running = false;

    // Unblock accept() immediately
    try {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    } catch (IOException ignore) {}

    // Cancel any pending question timer
    if (currentQuestionTimer != null) {
        currentQuestionTimer.cancel(false);
        currentQuestionTimer = null;
    }

    // Stop schedulers and worker threads
    try { scheduler.shutdownNow(); } catch (Exception ignore) {}
    try { threadPool.shutdownNow(); } catch (Exception ignore) {}

    // Close all client connections
    for (ClientHandler client : new ArrayList<>(clients)) {
        try { client.close(); } catch (Exception ignore) {}
    }
    clients.clear();

    log("Server stopped");
    updateUI();
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
}
