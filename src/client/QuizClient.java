package client;

import model.*;
import common.Protocol;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * QuizClient - Client application for students
 * Demonstrates socket client and network communication
 */
public class QuizClient {
    private String serverHost;
    private int serverPort;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected;
    private ClientUI ui;
    private String studentId;
    private String studentName;
    private ExecutorService executor;
    private MessageListener messageListener;

    public QuizClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.connected = false;
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Connects to the quiz server
     */
    public boolean connect(String studentId, String studentName) {
        try {
            socket = new Socket(serverHost, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            connected = true;

            this.studentId = studentId;
            this.studentName = studentName;

            // Send join message. Used custom protocol format
            sendMessage(Protocol.STUDENT_JOIN, studentId + "~" + studentName);

            // Start listening for messages
            startMessageListener();

            log("Connected to server at " + serverHost + ":" + serverPort);
            return true;

        } catch (IOException e) {
            log("Connection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Starts listening for server messages
     */
    private void startMessageListener() {
        messageListener = new MessageListener();
        executor.execute(messageListener);
    }

    /**
     * Sends a message to the server
     */
    public void sendMessage(String type, String data) {
        if (out != null && connected) {
            String message = Protocol.createMessage(type, data);
            out.println(message);
        }
    }

    /**
     * Submits an answer to the server
     */
    public void submitAnswer(int questionId, int answer, long timeTaken) {
        String data = questionId + "~" + answer + "~" + timeTaken;
        sendMessage(Protocol.ANSWER, data);
    }

    /**
     * Sends a chat message
     */
    public void sendChatMessage(String message) {
        sendMessage(Protocol.MESSAGE, message);
    }

    /**
     * Disconnects from the server
     */
    public void disconnect() {
        connected = false;

        try {
            if (out != null) {
                sendMessage(Protocol.DISCONNECT, "");
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            log("Error disconnecting: " + e.getMessage());
        }

        executor.shutdown();
        log("Disconnected from server");
    }

    /**
     * Sets the UI reference
     */
    public void setUI(ClientUI ui) {
        this.ui = ui;
    }

    /**
     * Logs a message
     */
    private void log(String message) {
        System.out.println("[Client] " + message);
        if (ui != null) {
            ui.appendLog(message);
        }
    }

    /**
     * Checks if connected
     */
    public boolean isConnected() {
        return connected;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentId() {
        return studentId;
    }

    /**
     * Inner class to listen for server messages
     */
    private class MessageListener implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while (connected && (message = in.readLine()) != null) {
                    handleMessage(message);
                }
            } catch (IOException e) {
                if (connected) {
                    log("Connection lost: " + e.getMessage());
                    connected = false;
                    if (ui != null) {
                        ui.handleDisconnection();
                    }
                }
            }
        }

        /**
         * Handles incoming messages from server
         */
        private void handleMessage(String message) {
            String[] parts = Protocol.parseMessage(message);
            String type = parts[0];
            String data = parts[1];

            if (ui == null) return;

            switch (type) {
                case Protocol.ACK:
                    ui.showMessage("Server", data);
                    break;

                case Protocol.QUIZ_START:
                    int totalQuestions = Integer.parseInt(data);
                    ui.handleQuizStart(totalQuestions);
                    break;

                case Protocol.QUESTION:
                    Question question = Question.fromProtocolString(data);
                    ui.handleQuestion(question);
                    break;

                case Protocol.RESULT:
                    handleResult(data);
                    break;

                case Protocol.LEADERBOARD:
                    ui.handleLeaderboard(data);
                    break;

                case Protocol.QUIZ_END:
                    ui.handleQuizEnd(data);
                    break;

                case Protocol.MESSAGE:
                    ui.showMessage("Server", data);
                    break;

                case Protocol.ERROR:
                    ui.showError(data);
                    break;

                default:
                    log("Unknown message type: " + type);
            }
        }

        private void handleResult(String data) {
            String[] parts = data.split("~");
            boolean correct = parts[0].equals("1");
            int pointsEarned = Integer.parseInt(parts[1]);
            String message = parts[2];
            int totalScore = Integer.parseInt(parts[3]);

            ui.handleAnswerResult(correct, pointsEarned, message, totalScore);
        }
    }

    /**
     * Main method to start client
     */
    public static void main(String[] args) {
        String host = "localhost";
        int port = Protocol.DEFAULT_PORT;

        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port, using default: " + port);
            }
        }

        final String serverHost = host;
        final int serverPort = port;

        // Start client with UI
        javax.swing.SwingUtilities.invokeLater(() -> {
            QuizClient client = new QuizClient(serverHost, serverPort);
            ClientUI ui = new ClientUI(client);
            client.setUI(ui);
            ui.setVisible(true);
        });
    }
}

