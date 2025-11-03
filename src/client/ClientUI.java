package client;

import model.Question;
import common.Protocol;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;

/**
 * ClientUI - Graphical interface for the quiz client
 * Demonstrates UI integration and timer functionality
 */
public class ClientUI extends JFrame {
    private QuizClient client;

    // Login components
    private JPanel loginPanel;
    private JTextField studentIdField;
    private JTextField studentNameField;
    private JTextField serverHostField;
    private JButton connectBtn;

    // Quiz components
    private JPanel quizPanel;
    private JLabel questionLabel;
    private JLabel questionNumberLabel;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private ButtonGroup optionsGroup;
    private JRadioButton[] optionButtons;
    private JButton submitBtn;
    private JTextArea leaderboardArea;
    private JTextArea logArea;
    private JTextArea chatArea;
    private JTextField chatInputField;

    // Quiz state
    private Question currentQuestion;
    private long questionStartTime;
    private Timer questionTimer;
    private ScheduledExecutorService timerExecutor;
    private int currentScore;
    private boolean answerSubmitted;

    public ClientUI(QuizClient client) {
        this.client = client;
        this.currentScore = 0;
        this.timerExecutor = Executors.newScheduledThreadPool(1);
        initComponents();
    }

    private void initComponents() {
        setTitle("QuizHub Client - Student Interface");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create card layout for switching between login and quiz
        JPanel cardPanel = new JPanel(new CardLayout());

        loginPanel = createLoginPanel();
        quizPanel = createQuizPanel();

        cardPanel.add(loginPanel, "LOGIN");
        cardPanel.add(quizPanel, "QUIZ");

        add(cardPanel);

        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client.isConnected()) {
                    client.disconnect();
                }
                timerExecutor.shutdown();
            }
        });
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("QuizHub - Student Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        // Server host
        panel.add(new JLabel("Server Host:"), gbc);
        gbc.gridx = 1;
        serverHostField = new JTextField("localhost", 20);
        panel.add(serverHostField, gbc);

        // Student ID
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Student ID:"), gbc);
        gbc.gridx = 1;
        studentIdField = new JTextField(20);
        panel.add(studentIdField, gbc);

        // Student Name
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Your Name:"), gbc);
        gbc.gridx = 1;
        studentNameField = new JTextField(20);
        panel.add(studentNameField, gbc);

        // Connect button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        connectBtn = new JButton("Connect to Quiz");
        connectBtn.setFont(new Font("Arial", Font.BOLD, 16));
        connectBtn.setBackground(new Color(0, 150, 0));
        connectBtn.setForeground(Color.WHITE);
        connectBtn.addActionListener(e -> connectToServer());
        panel.add(connectBtn, gbc);

        return panel;
    }

    private JPanel createQuizPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top panel - Status
        JPanel topPanel = createStatusPanel();
        panel.add(topPanel, BorderLayout.NORTH);

        // Center panel - Question and options
        JPanel centerPanel = createQuestionPanel();
        panel.add(centerPanel, BorderLayout.CENTER);

        // Right panel - Leaderboard and chat
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplit.setTopComponent(createLeaderboardPanel());
        rightSplit.setBottomComponent(createChatPanel());
        rightSplit.setDividerLocation(250);

        panel.add(rightSplit, BorderLayout.EAST);

        // Bottom panel - Log
        JPanel bottomPanel = createLogPanel();
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(230, 240, 255));

        questionNumberLabel = new JLabel("Waiting for quiz to start...");
        questionNumberLabel.setFont(new Font("Arial", Font.BOLD, 16));

        timerLabel = new JLabel("Time: --");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setForeground(Color.BLUE);

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scoreLabel.setForeground(new Color(0, 150, 0));

        panel.add(questionNumberLabel);
        panel.add(Box.createHorizontalStrut(50));
        panel.add(timerLabel);
        panel.add(Box.createHorizontalStrut(50));
        panel.add(scoreLabel);

        return panel;
    }

    private JPanel createQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Question"));

        // Question text
        questionLabel = new JLabel("<html><h2>Waiting for question...</h2></html>");
        questionLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(questionLabel, BorderLayout.NORTH);

        // Options
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        optionsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        optionsGroup = new ButtonGroup();
        optionButtons = new JRadioButton[4];

        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setFont(new Font("Arial", Font.PLAIN, 14));
            optionButtons[i].setEnabled(false);
            optionsGroup.add(optionButtons[i]);
            optionsPanel.add(optionButtons[i]);
        }

        panel.add(optionsPanel, BorderLayout.CENTER);

        // Submit button
        submitBtn = new JButton("Submit Answer");
        submitBtn.setFont(new Font("Arial", Font.BOLD, 16));
        submitBtn.setEnabled(false);
        submitBtn.addActionListener(e -> submitAnswer());

        JPanel submitPanel = new JPanel();
        submitPanel.add(submitBtn);
        panel.add(submitPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLeaderboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Leaderboard"));

        leaderboardArea = new JTextArea(12, 25);
        leaderboardArea.setEditable(false);
        leaderboardArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        JScrollPane scrollPane = new JScrollPane(leaderboardArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Chat"));

        chatArea = new JTextArea(8, 25);
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Chat input
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        chatInputField = new JTextField();
        JButton sendBtn = new JButton("Send");

        chatInputField.addActionListener(e -> sendChatMessage());
        sendBtn.addActionListener(e -> sendChatMessage());

        inputPanel.add(chatInputField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Log"));

        logArea = new JTextArea(5, 80);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 10));

        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void connectToServer() {
        String studentId = studentIdField.getText().trim();
        String studentName = studentNameField.getText().trim();
        String serverHost = serverHostField.getText().trim();

        if (studentId.isEmpty() || studentName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Student ID and Name",
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        connectBtn.setEnabled(false);
        connectBtn.setText("Connecting...");

        // Connect in background thread
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return client.connect(studentId, studentName);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        // Switch to quiz panel
                        CardLayout cl = (CardLayout) getContentPane().getLayout();
                        cl.show(getContentPane(), "QUIZ");
                        setTitle("QuizHub - " + studentName);
                    } else {
                        JOptionPane.showMessageDialog(ClientUI.this,
                            "Failed to connect to server", "Error", JOptionPane.ERROR_MESSAGE);
                        connectBtn.setEnabled(true);
                        connectBtn.setText("Connect to Quiz");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public void handleQuizStart(int totalQuestions) {
        SwingUtilities.invokeLater(() -> {
            showMessage("Quiz", "Quiz started! Total questions: " + totalQuestions);
            questionNumberLabel.setText("Quiz Started - Total Questions: " + totalQuestions);
        });
    }

    public void handleQuestion(Question question) {
        SwingUtilities.invokeLater(() -> {
            currentQuestion = question;
            questionStartTime = System.currentTimeMillis();
            answerSubmitted = false;

            // Update UI
            questionLabel.setText("<html><h2>" + question.getQuestionText() + "</h2></html>");
            questionNumberLabel.setText("Question " + question.getId());

            // Set options
            String[] options = question.getOptions();
            for (int i = 0; i < 4; i++) {
                optionButtons[i].setText((char)('A' + i) + ". " + options[i]);
                optionButtons[i].setEnabled(true);
            }
            optionsGroup.clearSelection();

            submitBtn.setEnabled(true);

            // Start timer
            startQuestionTimer(question.getTimeLimit());
        });
    }

    private void startQuestionTimer(int timeLimit) {
        if (questionTimer != null) {
            questionTimer.stop();
        }

        questionTimer = new Timer(1000, null);
        questionTimer.addActionListener(e -> {
            long elapsed = (System.currentTimeMillis() - questionStartTime) / 1000;
            long remaining = timeLimit - elapsed;

            if (remaining > 0) {
                timerLabel.setText("Time: " + remaining + "s");
                if (remaining <= 10) {
                    timerLabel.setForeground(Color.RED);
                } else {
                    timerLabel.setForeground(Color.BLUE);
                }
            } else {
                timerLabel.setText("Time: 0s");
                questionTimer.stop();
                if (!answerSubmitted) {
                    autoSubmitAnswer();
                }
            }
        });
        questionTimer.start();
    }

    private void submitAnswer() {
        if (currentQuestion == null || answerSubmitted) return;

        // Get selected option
        int selectedOption = -1;
        for (int i = 0; i < 4; i++) {
            if (optionButtons[i].isSelected()) {
                selectedOption = i;
                break;
            }
        }

        if (selectedOption == -1) {
            JOptionPane.showMessageDialog(this, "Please select an answer",
                                        "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        answerSubmitted = true;
        long timeTaken = System.currentTimeMillis() - questionStartTime;

        // Disable controls
        for (JRadioButton btn : optionButtons) {
            btn.setEnabled(false);
        }
        submitBtn.setEnabled(false);

        if (questionTimer != null) {
            questionTimer.stop();
        }

        // Submit to server
        client.submitAnswer(currentQuestion.getId(), selectedOption, timeTaken);
    }

    private void autoSubmitAnswer() {
        if (answerSubmitted) return;
        answerSubmitted = true;

        // Disable controls
        for (JRadioButton btn : optionButtons) {
            btn.setEnabled(false);
        }
        submitBtn.setEnabled(false);

        showMessage("System", "Time's up! Auto-submitting...");
    }

    public void handleAnswerResult(boolean correct, int pointsEarned, String message, int totalScore) {
        SwingUtilities.invokeLater(() -> {
            currentScore = totalScore;
            scoreLabel.setText("Score: " + currentScore);

            String title = correct ? "Correct!" : "Incorrect";
            Color color = correct ? new Color(0, 150, 0) : Color.RED;

            JOptionPane optionPane = new JOptionPane(
                message,
                correct ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
            );
            JDialog dialog = optionPane.createDialog(this, title);
            dialog.setModal(false);
            dialog.setVisible(true);

            // Auto-close after 2 seconds
            timerExecutor.schedule(() -> SwingUtilities.invokeLater(() -> dialog.dispose()),
                                  2, TimeUnit.SECONDS);
        });
    }

    public void handleLeaderboard(String data) {
        SwingUtilities.invokeLater(() -> {
            if (data.isEmpty()) {
                leaderboardArea.setText("No data yet");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-4s | %-15s | %-5s\n", "Rank", "Name", "Score"));
            sb.append("-".repeat(35)).append("\n");

            String[] entries = data.split("\\|\\|");
            for (String entry : entries) {
                String[] parts = entry.split("~");
                if (parts.length >= 3) {
                    sb.append(String.format("%-4s | %-15s | %-5s\n",
                        parts[0], parts[1], parts[2]));
                }
            }

            leaderboardArea.setText(sb.toString());
        });
    }

    public void handleQuizEnd(String leaderboardData) {
        SwingUtilities.invokeLater(() -> {
            if (questionTimer != null) {
                questionTimer.stop();
            }

            handleLeaderboard(leaderboardData);

            JOptionPane.showMessageDialog(this,
                "Quiz has ended!\nYour final score: " + currentScore,
                "Quiz Completed", JOptionPane.INFORMATION_MESSAGE);

            questionLabel.setText("<html><h2>Quiz Completed! Check the leaderboard for final results.</h2></html>");
            questionNumberLabel.setText("Quiz Ended");
            timerLabel.setText("--");
            submitBtn.setEnabled(false);
        });
    }

    public void handleDisconnection() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                "Disconnected from server", "Connection Lost", JOptionPane.ERROR_MESSAGE);
        });
    }

    private void sendChatMessage() {
        String message = chatInputField.getText().trim();
        if (!message.isEmpty()) {
            client.sendChatMessage(message);
            chatInputField.setText("");
        }
    }

    public void showMessage(String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append("[" + sender + "] " + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public void showError(String error) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}
