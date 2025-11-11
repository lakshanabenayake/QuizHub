package client;

import common.Protocol;
import model.Question;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;
import java.util.Map;
import java.util.HashMap;

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

    // Member 5 - UI & Event-Driven Programming: Synchronized timer state
    private int serverRemainingTime = 0;       // Time from server (synchronized)
    private boolean useServerTimer = false;     // Whether to use server-synchronized timer

    // 3️⃣ Connection Status & Network Monitoring
    private JLabel connectionStatusLabel;      // Connection status indicator
    private JLabel latencyLabel;               // Network latency display
    private long lastPingTime;                 // Track ping timing
    private ScheduledFuture<?> pingTask;       // Periodic ping task

    // 4️⃣ Enhanced Leaderboard - Track rank changes
    private Map<String, Integer> previousRanks;  // Previous rankings
    private JPanel miniLeaderboardPanel;         // Compact top 5 view

    // 5️⃣ Enhanced Question Display
    private JPanel[] optionPanels;             // Card-style option containers
    private int selectedOptionIndex = -1;      // Track keyboard selection

    // 6️⃣ Progress Indicators
    private JProgressBar quizProgressBar;      // Overall quiz progress
    private JLabel progressLabel;              // Question X of Y
    private int totalQuestions = 0;
    private int currentQuestionNumber = 0;

    // Card layout components
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public ClientUI(QuizClient client) {
        this.client = client;
        this.currentScore = 0;
        this.timerExecutor = Executors.newScheduledThreadPool(2);
        this.previousRanks = new HashMap<>();
        initComponents();
        setupKeyboardShortcuts();
    }

    private void initComponents() {
        setTitle("QuizHub Client - Student Interface");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create card layout for switching between login and quiz
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

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
                if (pingTask != null) {
                    pingTask.cancel(false);
                }
            }
        });
    }

    /**
     * 5️⃣ Keyboard Shortcuts: Event-Driven Programming with KeyEventDispatcher
     * Demonstrates accessibility and enhanced user interaction
     * Keys: 1/2/3/4 to select options, Enter to submit
     */
    private void setupKeyboardShortcuts() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && currentQuestion != null && !answerSubmitted) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_1:
                        selectOption(0);
                        return true;
                    case KeyEvent.VK_2:
                        selectOption(1);
                        return true;
                    case KeyEvent.VK_3:
                        selectOption(2);
                        return true;
                    case KeyEvent.VK_4:
                        selectOption(3);
                        return true;
                    case KeyEvent.VK_ENTER:
                        if (submitBtn.isEnabled()) {
                            submitAnswer();
                        }
                        return true;
                }
            }
            return false;
        });
    }

    /**
     * 5️⃣ Helper method for keyboard selection
     */
    private void selectOption(int index) {
        if (index >= 0 && index < 4 && optionButtons[index].isEnabled()) {
            optionButtons[index].setSelected(true);
            selectedOptionIndex = index;
            updateOptionStyles();
        }
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("🎓 QuizHub - Student Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 100, 180));
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
        connectBtn = new JButton("🔗 Connect to Quiz");
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
        rightSplit.setDividerLocation(300);

        panel.add(rightSplit, BorderLayout.EAST);

        // Bottom panel - Log
        JPanel bottomPanel = createLogPanel();
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(230, 240, 255));

        // Left side - Question info and progress
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(new Color(230, 240, 255));

        questionNumberLabel = new JLabel("Waiting for quiz to start...");
        questionNumberLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // 6️⃣ Quiz Progress Bar
        progressLabel = new JLabel("Progress: 0/0");
        progressLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        quizProgressBar = new JProgressBar(0, 100);
        quizProgressBar.setStringPainted(true);
        quizProgressBar.setPreferredSize(new Dimension(150, 20));
        quizProgressBar.setString("0%");

        leftPanel.add(questionNumberLabel);
        leftPanel.add(Box.createHorizontalStrut(20));
        leftPanel.add(progressLabel);
        leftPanel.add(quizProgressBar);

        // Right side - Timer, Score, Connection Status
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(new Color(230, 240, 255));

        timerLabel = new JLabel("Time: --:--");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setForeground(Color.BLUE);

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        scoreLabel.setForeground(new Color(0, 150, 0));

        // 3️⃣ Connection Status Indicator
        connectionStatusLabel = new JLabel("● ");
        connectionStatusLabel.setFont(new Font("Arial", Font.BOLD, 20));
        connectionStatusLabel.setForeground(Color.GRAY);
        connectionStatusLabel.setToolTipText("Disconnected");

        latencyLabel = new JLabel("Ping: --ms");
        latencyLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        latencyLabel.setForeground(Color.GRAY);

        rightPanel.add(connectionStatusLabel);
        rightPanel.add(latencyLabel);
        rightPanel.add(Box.createHorizontalStrut(20));
        rightPanel.add(timerLabel);
        rightPanel.add(Box.createHorizontalStrut(20));
        rightPanel.add(scoreLabel);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Question"));

        // Question text
        questionLabel = new JLabel("<html><h2>Waiting for question...</h2></html>");
        questionLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(questionLabel, BorderLayout.NORTH);

        // 5️⃣ Enhanced Options - Card-style with hover effects
        JPanel optionsContainer = new JPanel(new GridLayout(4, 1, 10, 10));
        optionsContainer.setBorder(new EmptyBorder(10, 20, 10, 20));
        optionsGroup = new ButtonGroup();
        optionButtons = new JRadioButton[4];
        optionPanels = new JPanel[4];

        for (int i = 0; i < 4; i++) {
            final int index = i;

            // Card-style container for each option
            optionPanels[i] = new JPanel(new BorderLayout());
            optionPanels[i].setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));
            optionPanels[i].setBackground(Color.WHITE);

            optionButtons[i] = new JRadioButton();
            optionButtons[i].setFont(new Font("Arial", Font.PLAIN, 14));
            optionButtons[i].setEnabled(false);
            optionButtons[i].setBackground(Color.WHITE);
            optionButtons[i].setOpaque(false);

            optionsGroup.add(optionButtons[i]);
            optionPanels[i].add(optionButtons[i], BorderLayout.CENTER);

            // 5️⃣ Mouse hover effects (Event-Driven Visual Feedback)
            optionPanels[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (optionButtons[index].isEnabled()) {
                        optionPanels[index].setBackground(new Color(230, 240, 255));
                        optionPanels[index].setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(0, 120, 215), 2, true),
                            BorderFactory.createEmptyBorder(10, 15, 10, 15)
                        ));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!optionButtons[index].isSelected()) {
                        optionPanels[index].setBackground(Color.WHITE);
                        optionPanels[index].setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true),
                            BorderFactory.createEmptyBorder(10, 15, 10, 15)
                        ));
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (optionButtons[index].isEnabled()) {
                        optionButtons[index].setSelected(true);
                        updateOptionStyles();
                    }
                }
            });

            // Update styles when selection changes
            optionButtons[i].addActionListener(e -> updateOptionStyles());

            optionsContainer.add(optionPanels[i]);
        }

        panel.add(optionsContainer, BorderLayout.CENTER);

        // Submit button with hint
        JPanel submitPanel = new JPanel(new BorderLayout());
        submitBtn = new JButton("✓ Submit Answer");
        submitBtn.setFont(new Font("Arial", Font.BOLD, 16));
        submitBtn.setEnabled(false);
        submitBtn.addActionListener(e -> submitAnswer());

        JLabel hintLabel = new JLabel("Tip: Use keys 1-4 to select, Enter to submit");
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);

        submitPanel.add(submitBtn, BorderLayout.CENTER);
        submitPanel.add(hintLabel, BorderLayout.SOUTH);
        panel.add(submitPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 5️⃣ Updates visual styles based on selection state
     */
    private void updateOptionStyles() {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < 4; i++) {
                if (optionButtons[i].isSelected()) {
                    optionPanels[i].setBackground(new Color(220, 245, 220));
                    optionPanels[i].setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 150, 0), 3, true),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)
                    ));
                } else if (optionButtons[i].isEnabled()) {
                    optionPanels[i].setBackground(Color.WHITE);
                    optionPanels[i].setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)
                    ));
                }
            }
        });
    }

    private JPanel createLeaderboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("🏆 Live Leaderboard"));

        // 4️⃣ Mini leaderboard - compact view
        miniLeaderboardPanel = new JPanel();
        miniLeaderboardPanel.setLayout(new BoxLayout(miniLeaderboardPanel, BoxLayout.Y_AXIS));
        miniLeaderboardPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JScrollPane miniScroll = new JScrollPane(miniLeaderboardPanel);
        miniScroll.setPreferredSize(new Dimension(280, 150));
        panel.add(miniScroll, BorderLayout.NORTH);

        leaderboardArea = new JTextArea(8, 25);
        leaderboardArea.setEditable(false);
        leaderboardArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        JScrollPane scrollPane = new JScrollPane(leaderboardArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Chat & Notifications"));

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
                        cardLayout.show(cardPanel, "QUIZ");
                        setTitle("QuizHub - " + studentName);

                        // 3️⃣ Update connection status
                        updateConnectionStatus("connected");

                        // Start ping monitoring
                        startPingMonitoring();
                    } else {
                        JOptionPane.showMessageDialog(ClientUI.this,
                            "Failed to connect to server", "Error", JOptionPane.ERROR_MESSAGE);
                        connectBtn.setEnabled(true);
                        connectBtn.setText("🔗 Connect to Quiz");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * 3️⃣ Network Monitoring: Starts periodic ping to measure latency
     * Demonstrates asynchronous network monitoring
     */
    private void startPingMonitoring() {
        pingTask = timerExecutor.scheduleAtFixedRate(() -> {
            lastPingTime = System.currentTimeMillis();
            client.sendMessage(Protocol.PING, String.valueOf(lastPingTime));
        }, 2, 5, TimeUnit.SECONDS);
    }

    /**
     * 3️⃣ Handles PONG response to calculate latency
     */
    public void handlePong(long sentTime) {
        long latency = System.currentTimeMillis() - sentTime;
        SwingUtilities.invokeLater(() -> {
            latencyLabel.setText("Ping: " + latency + "ms");

            // Update connection quality indicator
            if (latency < 100) {
                connectionStatusLabel.setForeground(new Color(0, 200, 0)); // Green - good
                connectionStatusLabel.setToolTipText("Connected - Good (" + latency + "ms)");
            } else if (latency < 300) {
                connectionStatusLabel.setForeground(new Color(255, 165, 0)); // Orange - slow
                connectionStatusLabel.setToolTipText("Connected - Slow (" + latency + "ms)");
            } else {
                connectionStatusLabel.setForeground(new Color(255, 100, 0)); // Red-orange - poor
                connectionStatusLabel.setToolTipText("Connected - Poor (" + latency + "ms)");
            }
        });
    }

    /**
     * 3️⃣ Updates connection status indicator
     */
    private void updateConnectionStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            switch (status) {
                case "connected":
                    connectionStatusLabel.setForeground(new Color(0, 200, 0));
                    connectionStatusLabel.setToolTipText("Connected");
                    break;
                case "disconnected":
                    connectionStatusLabel.setForeground(Color.RED);
                    connectionStatusLabel.setToolTipText("Disconnected");
                    latencyLabel.setText("Ping: --ms");
                    break;
                case "offline":
                    connectionStatusLabel.setForeground(Color.GRAY);
                    connectionStatusLabel.setToolTipText("Offline");
                    latencyLabel.setText("Ping: --ms");
                    break;
            }
        });
    }

    public void handleQuizStart(int totalQuestions) {
        this.totalQuestions = totalQuestions;
        this.currentQuestionNumber = 0;

        SwingUtilities.invokeLater(() -> {
            showToast("📢 Quiz Started!", "Total questions: " + totalQuestions, new Color(0, 120, 215));
            questionNumberLabel.setText("Quiz Started - Total Questions: " + totalQuestions);
            updateQuizProgress();
        });
    }

    public void handleQuestion(Question question) {
        SwingUtilities.invokeLater(() -> {
            currentQuestion = question;
            questionStartTime = System.currentTimeMillis();
            answerSubmitted = false;
            currentQuestionNumber++;

            // Update UI
            questionLabel.setText("<html><h2>" + question.getQuestionText() + "</h2></html>");
            questionNumberLabel.setText("Question " + question.getId());

            // Set options with card styling
            String[] options = question.getOptions();
            for (int i = 0; i < 4; i++) {
                optionButtons[i].setText((char)('A' + i) + ". " + options[i]);
                optionButtons[i].setEnabled(true);
                optionPanels[i].setBackground(Color.WHITE);
            }
            optionsGroup.clearSelection();
            submitBtn.setEnabled(true);

            // Update progress
            updateQuizProgress();

            // Show toast notification
            showToast("📝 New Question", "Question " + currentQuestionNumber + " of " + totalQuestions,
                     new Color(0, 150, 0));

            // DO NOT start local timer - rely on server synchronization only
            // The server will broadcast timer updates via handleTimerSync()
            // This ensures pause/resume commands work correctly
        });
    }

    /**
     * 6️⃣ Updates quiz progress indicators
     */
    private void updateQuizProgress() {
        SwingUtilities.invokeLater(() -> {
            if (totalQuestions > 0) {
                int percentage = (currentQuestionNumber * 100) / totalQuestions;
                progressLabel.setText(String.format("Progress: %d/%d", currentQuestionNumber, totalQuestions));
                quizProgressBar.setValue(percentage);
                quizProgressBar.setString(percentage + "%");
            }
        });
    }

    /**
     * 3️⃣ Toast Notification System - Non-blocking visual feedback
     * Demonstrates event-driven UI updates without interrupting user
     */
    private void showToast(String title, String message, Color bgColor) {
        JWindow toast = new JWindow(this);
        toast.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel msgLabel = new JLabel(message);
        msgLabel.setForeground(Color.WHITE);
        msgLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(msgLabel, BorderLayout.CENTER);

        toast.add(panel);
        toast.pack();

        // Position in top-right corner
        Point location = getLocation();
        Dimension size = getSize();
        toast.setLocation(location.x + size.width - toast.getWidth() - 20, location.y + 80);
        toast.setVisible(true);

        // Auto-hide after 3 seconds (non-blocking Swing Timer)
        Timer timer = new Timer(3000, e -> toast.dispose());
        timer.setRepeats(false);
        timer.start();
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

        // Disable controls and update styles
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setEnabled(false);
            optionPanels[i].setBackground(new Color(240, 240, 240));
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
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setEnabled(false);
            optionPanels[i].setBackground(new Color(240, 240, 240));
        }
        submitBtn.setEnabled(false);

        showMessage("System", "⏰ Time's up! Auto-submitting...");
    }

    public void handleAnswerResult(boolean correct, int pointsEarned, String message, int totalScore) {
        SwingUtilities.invokeLater(() -> {
            currentScore = totalScore;
            scoreLabel.setText("Score: " + currentScore);

            // 3️⃣ Toast notification for result
            String title = correct ? "✓ Correct!" : "✗ Incorrect";
            Color color = correct ? new Color(0, 150, 0) : new Color(200, 0, 0);
            showToast(title, message, color);

            // 4️⃣ Show score animation if points earned
            if (pointsEarned > 0) {
                animateScoreChange("+" + pointsEarned);
            }
        });
    }

    /**
     * 4️⃣ Animates score changes with floating text
     */
    private void animateScoreChange(String changeText) {
        JLabel scoreChange = new JLabel(changeText);
        scoreChange.setFont(new Font("Arial", Font.BOLD, 20));
        scoreChange.setForeground(new Color(0, 180, 0));

        JWindow floater = new JWindow(this);
        floater.add(scoreChange);
        floater.pack();

        Point scoreLoc = scoreLabel.getLocationOnScreen();
        floater.setLocation(scoreLoc.x + scoreLabel.getWidth() + 10, scoreLoc.y);
        floater.setVisible(true);

        // Animate upward and fade out
        Timer animator = new Timer(50, null);
        final int[] step = {0};
        animator.addActionListener(e -> {
            step[0]++;
            floater.setLocation(floater.getX(), floater.getY() - 3);
            if (step[0] > 20) {
                floater.dispose();
                ((Timer)e.getSource()).stop();
            }
        });
        animator.start();
    }

    public void handleLeaderboard(String data) {
        SwingUtilities.invokeLater(() -> {
            if (data.isEmpty()) {
                leaderboardArea.setText("No data yet");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-4s  %-15s  %-5s  %-3s\n", "Rank", "Name", "Score", "Δ"));
            sb.append("-".repeat(35)).append("\n");

            // 4️⃣ Clear mini leaderboard
            miniLeaderboardPanel.removeAll();

            String[] entries = data.split("\\|\\|");
            int position = 0;
            for (String entry : entries) {
                String[] parts = entry.split("~");
                if (parts.length >= 3) {
                    position++;
                    String studentName = parts[1];
                    int score = Integer.parseInt(parts[2]);

                    // Detect rank change
                    String rankChange = "–";
                    if (previousRanks.containsKey(studentName)) {
                        int prevRank = previousRanks.get(studentName);
                        if (prevRank > position) rankChange = "↑";
                        else if (prevRank < position) rankChange = "↓";
                    }
                    previousRanks.put(studentName, position);

                    // Medal icons for top 3
                    String medal = "";
                    if (position == 1) medal = "🥇 ";
                    else if (position == 2) medal = "🥈 ";
                    else if (position == 3) medal = "🥉 ";

                    sb.append(String.format("%-4s  %s%-15s  %-5s  %-3s\n",
                        parts[0], medal, studentName, parts[2], rankChange));

                    // 4️⃣ Add to mini leaderboard (top 5 only)
                    if (position <= 5) {
                        JPanel miniRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        miniRow.setBackground(position <= 3 ? new Color(255, 250, 205) : Color.WHITE);
                        JLabel miniLabel = new JLabel(String.format("%s#%d %s - %d pts",
                            medal, position, studentName, score));
                        miniLabel.setFont(new Font("Arial", Font.BOLD, 12));
                        miniRow.add(miniLabel);
                        miniLeaderboardPanel.add(miniRow);
                    }
                }
            }

            // Highlight current user's row in full leaderboard
            String currentUser = client.getStudentName();
            String leaderboardText = sb.toString();
            leaderboardArea.setText(leaderboardText);

            miniLeaderboardPanel.revalidate();
            miniLeaderboardPanel.repaint();
        });
    }

    public void handleQuizEnd(String leaderboardData) {
        SwingUtilities.invokeLater(() -> {
            handleLeaderboard(leaderboardData);

            showToast("🏁 Quiz Completed!", "Final Score: " + currentScore, new Color(100, 100, 200));

            JOptionPane.showMessageDialog(this,
                "Quiz has ended!\nYour final score: " + currentScore,
                "Quiz Completed", JOptionPane.INFORMATION_MESSAGE);

            questionLabel.setText("<html><h2>🏁 Quiz Completed! Check the leaderboard for final results.</h2></html>");
            questionNumberLabel.setText("Quiz Ended");
            timerLabel.setText("--:--");
            timerLabel.setForeground(Color.BLUE);
            submitBtn.setEnabled(false);

            // Clear all answer options from the last question
            if (optionsGroup != null) {
                optionsGroup.clearSelection();
            }
            if (optionButtons != null) {
                for (JRadioButton btn : optionButtons) {
                    if (btn != null) {
                        btn.setVisible(false);
                        btn.setEnabled(false);
                    }
                }
            }
            if (optionPanels != null) {
                for (JPanel panel : optionPanels) {
                    if (panel != null) {
                        panel.setVisible(false);
                    }
                }
            }

            // Reset quiz progress
            if (quizProgressBar != null) {
                quizProgressBar.setValue(100);
            }
            if (progressLabel != null) {
                progressLabel.setText("Quiz Completed");
            }
        });
    }

    /**
     * Member 5 - Network-Synchronized Timer Update (PRIMARY TIMER)
     * Thread-Safe UI Update: Handles timer synchronization messages from server
     * This demonstrates network-driven event updates with thread safety
     *
     * @param remainingSeconds Time remaining from server
     * @param state Timer state: "normal", "warning", or "critical"
     */
    public void handleTimerSync(int remainingSeconds, String state) {
        // Update synchronized timer state (network-driven data)
        this.serverRemainingTime = remainingSeconds;
        this.useServerTimer = true;

        // Thread-safe UI update using SwingUtilities.invokeLater()
        // This ensures UI updates happen on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // Format timer as MM:SS
            int minutes = remainingSeconds / 60;
            int seconds = remainingSeconds % 60;
            timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));

            // Event-Driven State Changes: Color changes based on urgency
            switch (state) {
                case "critical":
                    // Last 10 seconds - RED (urgent)
                    timerLabel.setForeground(Color.RED);
                    break;
                case "warning":
                    // Last 30 seconds - ORANGE (caution)
                    timerLabel.setForeground(Color.ORANGE);
                    break;
                default:
                    // Normal state - BLUE
                    timerLabel.setForeground(Color.BLUE);
                    break;
            }

            // Auto-submit when time reaches zero (only if not paused)
            if (remainingSeconds <= 0 && !answerSubmitted && currentQuestion != null) {
                autoSubmitAnswer();
            }
        });
    }

    /**
     * Handles timer control messages from server (pause/resume/extend)
     * Demonstrates server-side administrative control affecting client UI
     */
    public void handleTimerControl(String control, String data) {
        SwingUtilities.invokeLater(() -> {
            switch (control) {
                case "pause":
                    showToast("⏸ Timer Paused", "Instructor paused the quiz", new Color(255, 165, 0));
                    timerLabel.setForeground(Color.GRAY);
                    break;
                case "resume":
                    showToast("▶ Timer Resumed", "Quiz continues", new Color(0, 150, 0));
                    break;
                case "extend":
                    int additionalTime = Integer.parseInt(data);
                    showToast("⏱ Time Extended", "+" + additionalTime + " seconds added!",
                             new Color(0, 120, 215));
                    break;
            }
        });
    }

    public void handleDisconnection() {
        SwingUtilities.invokeLater(() -> {
            updateConnectionStatus("disconnected");

            // 3️⃣ Auto-reconnect countdown dialog
            showReconnectDialog();
        });
    }

    /**
     * 3️⃣ Shows auto-reconnect countdown dialog
     */
    private void showReconnectDialog() {
        JDialog dialog = new JDialog(this, "Connection Lost", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JLabel messageLabel = new JLabel("<html><center>Connection to server lost!<br>Attempting to reconnect...</center></html>");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 14));
        messageLabel.setBorder(new EmptyBorder(20, 20, 10, 20));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBorder(new EmptyBorder(10, 20, 20, 20));

        dialog.add(messageLabel, BorderLayout.CENTER);
        dialog.add(progressBar, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // Auto-close after showing (this is just visual feedback)
        Timer closeTimer = new Timer(5000, e -> dialog.dispose());
        closeTimer.setRepeats(false);
        closeTimer.start();

        dialog.setVisible(true);
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
