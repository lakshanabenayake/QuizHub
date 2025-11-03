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

    public QuizServer(int port) {
        this.port = port;
        this.clients = new CopyOnWriteArrayList<>();
        this.questionManager = new QuestionManager();
        this.session = new QuizSession("SESSION-" + System.currentTimeMillis());
        this.scoringSystem = new ScoringSystem(session);
        this.threadPool = Executors.newCachedThreadPool();
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
            updateUI();

            // Auto-advance after time limit (optional)
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
    public synchronized void endQuiz() {
        if (!session.isActive()) return;

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
     * Processes student answer
     */
    public ScoringSystem.AnswerResult processAnswer(String studentId, int questionId,
                                                    int answer, long timeTaken) {
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
     * Stops the server
     */
    public void stop() {
        running = false;

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

        // Start server with UI
        javax.swing.SwingUtilities.invokeLater(() -> {
            QuizServer server = new QuizServer(port);
            ServerUI ui = new ServerUI(server);
            server.setUI(ui);
            ui.setVisible(true);
        });
    }
}

