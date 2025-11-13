package server;

import model.*;
import common.Protocol;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * ClientHandler manages individual client connections
 * Demonstrates multithreading and socket I/O
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private QuizServer server;
    private Student student;
    private boolean running;
    private String clientId;

    public ClientHandler(Socket socket, QuizServer server) {
        this.socket = socket;
        this.server = server;
        this.running = true;
        this.clientId = UUID.randomUUID().toString();
    }

    @Override
    public void run() {
        try {
            // Setup I/O streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            server.log("Client connected: " + socket.getInetAddress());

            // Handle client messages
            String message;
            while (running && (message = in.readLine()) != null) {
                handleMessage(message);
            }

        } catch (IOException e) {
            if (running) {
                server.log("Client connection error: " + e.getMessage());
            }
        } finally {
            cleanup();
        }
    }

    /**
     * Handles incoming messages from client
     */
    private void handleMessage(String message) {
        String[] parts = Protocol.parseMessage(message);
        String type = parts[0];
        String data = parts[1];

        switch (type) {
            case Protocol.STUDENT_JOIN:
                handleStudentJoin(data);
                break;

            case Protocol.ANSWER:
                handleAnswer(data);
                break;

            case Protocol.DISCONNECT:
                handleDisconnect();
                break;

            case Protocol.MESSAGE:
                handleChatMessage(data);
                break;

            default:
                server.log("Unknown message type: " + type);
        }
    }

    /**
     * Handles student joining the quiz
     */
    private void handleStudentJoin(String data) {
        String[] parts = data.split(Protocol.FIELD_SEPARATOR);
        String studentId = parts[0];
        String name = parts[1];

        student = new Student(studentId, name);
        server.addStudent(this, student);

        // Send acknowledgment
        sendMessage(Protocol.ACK, "Welcome " + name + "!");
        server.log("Student joined: " + name + " (ID: " + studentId + ")");

        // Broadcast updated student list to all clients
        server.broadcastStudentList();
    }

    /**
     * Handles student answer submission
     */
    private void handleAnswer(String data) {
        if (student == null) return;

        String[] parts = data.split(Protocol.FIELD_SEPARATOR);
        int questionId = Integer.parseInt(parts[0]);
        int answer = Integer.parseInt(parts[1]);
        long timeTaken = Long.parseLong(parts[2]);

        // Process answer through scoring system
        ScoringSystem.AnswerResult result = server.processAnswer(
            student.getStudentId(), questionId, answer, timeTaken
        );

        // Send result back to student
        String resultData = (result.isCorrect() ? "1" : "0") + "~" +
                           result.getPointsEarned() + "~" +
                           result.getMessage() + "~" +
                           student.getScore();
        sendMessage(Protocol.RESULT, resultData);

        server.log(student.getName() + " answered Q" + questionId + ": " +
                  (result.isCorrect() ? "Correct" : "Incorrect"));

        // Broadcast updated leaderboard
        server.broadcastLeaderboard();

        // Notify server that a student answered (for potential auto-advance)
        server.onStudentAnswered(questionId);
    }

    /**
     * Handles chat messages
     */
    private void handleChatMessage(String data) {
        if (student != null) {
            server.broadcastMessage(student.getName() + ": " + data);
        }
    }

    /**
     * Handles client disconnect
     */
    private void handleDisconnect() {
        running = false;
        if (student != null) {
            server.log("Student disconnected: " + student.getName());
            server.removeStudent(this);
            server.broadcastStudentList();
        }
    }

    /**
     * Sends a message to this client
     */
    public void sendMessage(String type, String data) {
        if (out != null && !socket.isClosed()) {
            String message = Protocol.createMessage(type, data);
            out.println(message);
        }
    }

    /**
     * Gets the student associated with this handler
     */
    public Student getStudent() {
        return student;
    }

    /**
     * Gets the client ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Checks if handler is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Cleanup resources
     */
    private void cleanup() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            server.log("Error closing client connection: " + e.getMessage());
        }

        if (student != null) {
            server.removeStudent(this);
            server.broadcastStudentList();
        }
    }

    /**
     * Closes this client connection
     */
    public void close() {
        cleanup();
    }

    public void disconnect() {
    try {
        running = false;
        socket.close();
    } catch (Exception ignored) {}
}

}


