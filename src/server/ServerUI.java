package server;

import common.Protocol;
import model.Question;
import model.Student;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ServerUI - Graphical interface for the quiz server
 * Demonstrates UI integration with network programming
 */
public class ServerUI extends JFrame {
    private QuizServer server;
    private JTextArea logArea;
    private JTextArea studentsArea;
    private JTextArea leaderboardArea;
    private JButton startServerBtn;
    private JButton stopServerBtn;
    private JButton startQuizBtn;
    private JButton nextQuestionBtn;
    private JButton endQuizBtn;
    private JLabel statusLabel;
    private JLabel studentsCountLabel;
    private DefaultListModel<String> questionListModel;
    private JList<String> questionList;

    // Master Timer Components
    private JLabel masterTimerLabel;
    private JButton pauseResumeBtn;
    private JButton extendTimeBtn;
    private JButton skipQuestionBtn;
    private JButton forceNextBtn;
    private JLabel answerCountLabel;
    private boolean timerPaused = false;

    // Real-Time Answer Progress Tracking
    private JProgressBar answerProgressBar;
    private Map<String, JLabel> studentStatusLabels;
    private JPanel studentStatusPanel;

    // Connection Status Indicator
    private JLabel connectionStatusLabel;
    private JPanel connectionIndicatorPanel;

    // Progress Indicators & Quiz Flow
    private JProgressBar quizProgressBar;
    private JLabel quizProgressLabel;
    private JButton exportResultsBtn;

    // Enhanced Leaderboard
    private Map<String, Integer> previousRanks;
    private Map<String, Integer> previousScores;

    public ServerUI(QuizServer server) {
        this.server = server;
        this.studentStatusLabels = new ConcurrentHashMap<>();
        this.previousRanks = new HashMap<>();
        this.previousScores = new HashMap<>();
        initComponents();
    }

    private void initComponents() {
        setTitle("QuizHub Server - Teacher Dashboard");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top panel - Controls
        JPanel topPanel = createControlPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center panel - Split into sections
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplit.setDividerLocation(400);

        // Left side - Questions and controls
        JPanel leftPanel = createQuestionPanel();
        centerSplit.setLeftComponent(leftPanel);

        // Right side - Students and leaderboard
        JPanel rightPanel = createMonitorPanel();
        centerSplit.setRightComponent(rightPanel);

        mainPanel.add(centerSplit, BorderLayout.CENTER);

        // Bottom panel - Logs
        JPanel bottomPanel = createLogPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (server.isRunning()) {
                    server.stop();
                }
            }
        });
    }

    private JPanel createControlPanel() {
        // Main container with vertical layout for multiple rows
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Server Controls"));

        // Top row - Server and Quiz controls
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT));

        startServerBtn = new JButton("Start Server");
        stopServerBtn = new JButton("Stop Server");
        startQuizBtn = new JButton("Start Quiz");
        nextQuestionBtn = new JButton("Next Question");
        endQuizBtn = new JButton("End Quiz");

        stopServerBtn.setEnabled(false);
        startQuizBtn.setEnabled(false);
        nextQuestionBtn.setEnabled(false);
        endQuizBtn.setEnabled(false);

        // Connection Status Indicator (Event-Driven Visual Feedback)
        connectionStatusLabel = new JLabel("●");
        connectionStatusLabel.setFont(new Font("Arial", Font.BOLD, 24));
        connectionStatusLabel.setForeground(Color.GRAY);
        connectionStatusLabel.setToolTipText("Server Offline");

        statusLabel = new JLabel("Server: Stopped");
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));

        studentsCountLabel = new JLabel("Students: 0");
        studentsCountLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Event-Driven Programming: ActionListeners respond to user button clicks
        startServerBtn.addActionListener(e -> startServer());
        stopServerBtn.addActionListener(e -> stopServer());
        startQuizBtn.addActionListener(e -> startQuiz());
        nextQuestionBtn.addActionListener(e -> nextQuestion());
        endQuizBtn.addActionListener(e -> endQuiz());

        topRow.add(connectionStatusLabel);
        topRow.add(startServerBtn);
        topRow.add(stopServerBtn);
        topRow.add(new JSeparator(SwingConstants.VERTICAL));
        topRow.add(startQuizBtn);
        topRow.add(nextQuestionBtn);
        topRow.add(endQuizBtn);
        topRow.add(Box.createHorizontalStrut(20));
        topRow.add(statusLabel);
        topRow.add(Box.createHorizontalStrut(20));
        topRow.add(studentsCountLabel);

        // Bottom row - Master Timer Display and Timer Controls (Member 5 Enhancement)
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Master Timer Display - shows synchronized time in MM:SS format
        masterTimerLabel = new JLabel("Timer: --:--");
        masterTimerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        masterTimerLabel.setForeground(Color.BLUE);
        masterTimerLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Real-time Answer Tracking Counter with Progress Bar
        answerCountLabel = new JLabel("Answered: 0/0");
        answerCountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        answerCountLabel.setForeground(new Color(0, 100, 180));

        answerProgressBar = new JProgressBar(0, 100);
        answerProgressBar.setStringPainted(true);
        answerProgressBar.setPreferredSize(new Dimension(150, 25));
        answerProgressBar.setString("0%");

        // Timer Control Buttons (Event-Driven Administrative Controls)
        pauseResumeBtn = new JButton("⏸ Pause");
        extendTimeBtn = new JButton("⏱ +30s");
        skipQuestionBtn = new JButton("⏭ Skip");
        forceNextBtn = new JButton("⏩ Force Next");

        // Initially disabled until quiz starts
        pauseResumeBtn.setEnabled(false);
        extendTimeBtn.setEnabled(false);
        skipQuestionBtn.setEnabled(false);
        forceNextBtn.setEnabled(false);

        // Event listeners for timer controls - trigger network events
        pauseResumeBtn.addActionListener(e -> handlePauseResume());
        extendTimeBtn.addActionListener(e -> handleExtendTime());
        skipQuestionBtn.addActionListener(e -> handleSkipQuestion());
        forceNextBtn.addActionListener(e -> handleForceNext());

        bottomRow.add(masterTimerLabel);
        bottomRow.add(Box.createHorizontalStrut(15));
        bottomRow.add(answerCountLabel);
        bottomRow.add(answerProgressBar);
        bottomRow.add(Box.createHorizontalStrut(20));
        bottomRow.add(new JSeparator(SwingConstants.VERTICAL));
        bottomRow.add(pauseResumeBtn);
        bottomRow.add(extendTimeBtn);
        bottomRow.add(skipQuestionBtn);
        bottomRow.add(forceNextBtn);

        // Combine rows
        JPanel combined = new JPanel(new GridLayout(2, 1, 5, 5));
        combined.add(topRow);
        combined.add(bottomRow);
        mainPanel.add(combined, BorderLayout.CENTER);

        return mainPanel;
    }

    /**
     * Handles pause/resume timer control
     */
    private void handlePauseResume() {
        timerPaused = !timerPaused;
        if (timerPaused) {
            server.pauseTimer();
            pauseResumeBtn.setText("▶ Resume");
            pauseResumeBtn.setBackground(new Color(0, 150, 0));
            appendLog("⏸ Quiz timer PAUSED by administrator");
        } else {
            server.resumeTimer();
            pauseResumeBtn.setText("⏸ Pause");
            pauseResumeBtn.setBackground(null);
            appendLog("▶ Quiz timer RESUMED by administrator");
        }
    }

    /**
     * Event-Driven Programming: Handles extend time control
     * Adds 30 seconds to current question timer
     */
    private void handleExtendTime() {
        server.extendTimer(30);
        // Thread-safe UI update showing confirmation
        SwingUtilities.invokeLater(() -> {
            showToast("⏱ Added 30 seconds to timer", new Color(0, 150, 0));
        });
        appendLog("⏱ Administrator extended time by 30 seconds");
    }

    /**
     * Event-Driven Programming: Handles skip question with confirmation
     * Shows dialog before skipping to prevent accidental clicks
     */
    private void handleSkipQuestion() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Skip current question and move to next?\nNo answers will be recorded.",
            "Confirm Skip", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            server.skipCurrentQuestion();
            appendLog("⏭ Administrator skipped current question");
        }
    }

    /**
     * Handles force next with confirmation
     */
    private void handleForceNext() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Force advance to next question?\nStudents who haven't answered will get 0 points.",
            "Confirm Force Next", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            server.forceNextQuestion();
            appendLog("⏩ Administrator forced next question");
        }
    }

    /**
     * 3️⃣ Thread-Safe UI Update: Toast notification for events
     * Demonstrates non-blocking visual feedback
     */
    private void showToast(String message, Color bgColor) {
        JWindow toast = new JWindow(this);
        toast.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel();
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        JLabel label = new JLabel(message);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(label);

        toast.add(panel);
        toast.pack();
        toast.setLocationRelativeTo(this);
        toast.setVisible(true);

        // Auto-hide after 2 seconds (non-blocking timer)
        Timer timer = new Timer(2000, e -> toast.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Updates master timer display (thread-safe)
     * @param remainingSeconds Time remaining in seconds
     * @param state Timer state: "normal", "warning", or "critical"
     */
    public void updateMasterTimer(int remainingSeconds, String state) {
        SwingUtilities.invokeLater(() -> {
            // Format as MM:SS
            int minutes = remainingSeconds / 60;
            int seconds = remainingSeconds % 60;
            masterTimerLabel.setText(String.format("Timer: %02d:%02d", minutes, seconds));

            // Change color based on urgency (demonstrates event-driven state changes)
            switch (state) {
                case "critical":
                    masterTimerLabel.setForeground(Color.RED);
                    masterTimerLabel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.RED, 3),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                    break;
                case "warning":
                    masterTimerLabel.setForeground(Color.ORANGE);
                    masterTimerLabel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.ORANGE, 2),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                    break;
                default:
                    masterTimerLabel.setForeground(Color.BLUE);
                    masterTimerLabel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.GRAY, 2),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                    break;
            }
        });
    }

    /**
     * 2️⃣ Thread-Safe UI Update: Updates answer tracking counter and progress bar
     * Shows real-time count of students who have answered current question
     * Demonstrates concurrency control and event-driven state updates
     */
    public void updateAnswerCount(int answeredCount, int totalStudents) {
        SwingUtilities.invokeLater(() -> {
            answerCountLabel.setText(String.format("Answered: %d/%d", answeredCount, totalStudents));

            // Update progress bar
            int percentage = totalStudents > 0 ? (answeredCount * 100) / totalStudents : 0;
            answerProgressBar.setValue(percentage);
            answerProgressBar.setString(percentage + "%");

            // Visual feedback when all students have answered
            if (answeredCount == totalStudents && totalStudents > 0) {
                answerCountLabel.setForeground(new Color(0, 150, 0)); // Green
                answerProgressBar.setForeground(new Color(0, 180, 0));
            } else if (percentage >= 75) {
                answerProgressBar.setForeground(new Color(100, 200, 100));
            } else if (percentage >= 50) {
                answerProgressBar.setForeground(new Color(255, 200, 0));
            } else {
                answerCountLabel.setForeground(new Color(0, 100, 180)); // Blue
                answerProgressBar.setForeground(new Color(70, 130, 180));
            }
        });
    }

    /**
     * Enables/disables timer control buttons based on quiz state
     */
    public void setTimerControlsEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            pauseResumeBtn.setEnabled(enabled);
            extendTimeBtn.setEnabled(enabled);
            skipQuestionBtn.setEnabled(enabled);
            forceNextBtn.setEnabled(enabled);

            if (!enabled) {
                // Reset timer display when quiz not active
                masterTimerLabel.setText("Timer: --:--");
                masterTimerLabel.setForeground(Color.BLUE);
                answerCountLabel.setText("Answered: 0/0");
                answerProgressBar.setValue(0);
                answerProgressBar.setString("0%");
                timerPaused = false;
                pauseResumeBtn.setText("⏸ Pause");
                pauseResumeBtn.setBackground(null);
            }
        });
    }

    private JPanel createQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Question Bank"));

        // Quiz Progress Display at top
        JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
        quizProgressLabel = new JLabel("No active quiz");
        quizProgressLabel.setFont(new Font("Arial", Font.BOLD, 12));
        quizProgressBar = new JProgressBar(0, 100);
        quizProgressBar.setStringPainted(true);
        quizProgressBar.setString("0%");
        progressPanel.add(quizProgressLabel, BorderLayout.NORTH);
        progressPanel.add(quizProgressBar, BorderLayout.CENTER);
        panel.add(progressPanel, BorderLayout.NORTH);

        questionListModel = new DefaultListModel<>();
        questionList = new JList<>(questionListModel);
        questionList.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(questionList);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JButton loadQuestionsBtn = new JButton("📋 Load Questions");
        JButton addQuestionBtn = new JButton("➕ Add Question");
        JButton removeQuestionBtn = new JButton("➖ Remove Selected");
        exportResultsBtn = new JButton("💾 Export Results");
        exportResultsBtn.setEnabled(false);

        loadQuestionsBtn.addActionListener(e -> loadQuestions());
        addQuestionBtn.addActionListener(e -> addQuestion());
        removeQuestionBtn.addActionListener(e -> removeQuestion());
        exportResultsBtn.addActionListener(e -> exportResults());

        buttonPanel.add(loadQuestionsBtn);
        buttonPanel.add(addQuestionBtn);
        buttonPanel.add(removeQuestionBtn);
        buttonPanel.add(exportResultsBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 6️⃣ Updates quiz progress indicators
     */
    public void updateQuizProgress(int currentQuestion, int totalQuestions) {
        SwingUtilities.invokeLater(() -> {
            if (totalQuestions > 0) {
                int percentage = (currentQuestion * 100) / totalQuestions;
                quizProgressLabel.setText(String.format("Question %d of %d", currentQuestion, totalQuestions));
                quizProgressBar.setValue(percentage);
                quizProgressBar.setString(percentage + "%");
            } else {
                quizProgressLabel.setText("No active quiz");
                quizProgressBar.setValue(0);
                quizProgressBar.setString("0%");
            }
        });
    }

    private JPanel createMonitorPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));

        // Students panel with status indicators
        JPanel studentsPanel = new JPanel(new BorderLayout());
        studentsPanel.setBorder(BorderFactory.createTitledBorder("Connected Students - Real-Time Status"));

        studentStatusPanel = new JPanel();
        studentStatusPanel.setLayout(new BoxLayout(studentStatusPanel, BoxLayout.Y_AXIS));
        JScrollPane studentScroll = new JScrollPane(studentStatusPanel);
        studentsPanel.add(studentScroll, BorderLayout.CENTER);

        // Enhanced Leaderboard panel
        JPanel leaderboardPanel = new JPanel(new BorderLayout());
        leaderboardPanel.setBorder(BorderFactory.createTitledBorder("🏆 Live Leaderboard - Animated Rankings"));
        leaderboardArea = new JTextArea(10, 30);
        leaderboardArea.setEditable(false);
        leaderboardArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        leaderboardPanel.add(new JScrollPane(leaderboardArea), BorderLayout.CENTER);

        panel.add(studentsPanel);
        panel.add(leaderboardPanel);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Server Log"));

        logArea = new JTextArea(8, 80);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void startServer() {
        server.start();
        startServerBtn.setEnabled(false);
        stopServerBtn.setEnabled(true);
        startQuizBtn.setEnabled(true);
        statusLabel.setText("Server: Running on port " + Protocol.DEFAULT_PORT);
        statusLabel.setForeground(new Color(0, 150, 0));

        // 3️⃣ Update connection status indicator
        connectionStatusLabel.setForeground(new Color(0, 200, 0));
        connectionStatusLabel.setToolTipText("Server Online");

        loadQuestions();
    }

    private void stopServer() {
        server.stop();
        startServerBtn.setEnabled(true);
        stopServerBtn.setEnabled(false);
        startQuizBtn.setEnabled(false);
        nextQuestionBtn.setEnabled(false);
        endQuizBtn.setEnabled(false);
        statusLabel.setText("Server: Stopped");
        statusLabel.setForeground(Color.RED);

        // 3️⃣ Update connection status indicator
        connectionStatusLabel.setForeground(Color.GRAY);
        connectionStatusLabel.setToolTipText("Server Offline");
    }

    private void startQuiz() {
        List<Question> questions = server.getQuestionManager().getAllQuestions();
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No questions available!",
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        server.startQuiz(questions);
        startQuizBtn.setEnabled(false);
        nextQuestionBtn.setEnabled(true);
        endQuizBtn.setEnabled(true);
        exportResultsBtn.setEnabled(false);
    }

    private void nextQuestion() {
        server.sendNextQuestion();
    }

    private void endQuiz() {
        server.endQuiz();
        startQuizBtn.setEnabled(true);
        nextQuestionBtn.setEnabled(false);
        endQuizBtn.setEnabled(false);
        exportResultsBtn.setEnabled(true);
    }

    /**
     * 6️⃣ Export quiz results to CSV format
     */
    private void exportResults() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Quiz Results");
        fileChooser.setSelectedFile(new java.io.File("quiz_results_" + System.currentTimeMillis() + ".csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                java.io.PrintWriter writer = new java.io.PrintWriter(file);

                // Write header
                writer.println("Rank,Student Name,Score,Correct Answers,Accuracy");

                // Write data
                List<Student> leaderboard = server.getScoringSystem().getLeaderboard();
                for (int i = 0; i < leaderboard.size(); i++) {
                    Student s = leaderboard.get(i);
                    writer.printf("%d,%s,%d,%d,%.1f%%\n",
                        i + 1, s.getName(), s.getScore(), s.getCorrectAnswers(), s.getAccuracy());
                }

                writer.close();
                showToast("✅ Results exported successfully!", new Color(0, 150, 0));
                appendLog("💾 Results exported to: " + file.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting results: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadQuestions() {
        questionListModel.clear();
        List<Question> questions = server.getQuestionManager().getAllQuestions();
        for (Question q : questions) {
            questionListModel.addElement(String.format("Q%d: %s (%ds, %dpts)",
                q.getId(), q.getQuestionText(), q.getTimeLimit(), q.getPoints()));
        }
    }

    private void addQuestion() {
        JTextField questionField = new JTextField(40);
        JTextField option1Field = new JTextField(30);
        JTextField option2Field = new JTextField(30);
        JTextField option3Field = new JTextField(30);
        JTextField option4Field = new JTextField(30);
        JComboBox<String> correctBox = new JComboBox<>(new String[]{"Option 1", "Option 2", "Option 3", "Option 4"});
        JSpinner timeLimitSpinner = new JSpinner(new SpinnerNumberModel(30, 10, 300, 5));
        JSpinner pointsSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 5));

        JPanel panel = new JPanel(new GridLayout(8, 2, 5, 5));
        panel.add(new JLabel("Question:"));
        panel.add(questionField);
        panel.add(new JLabel("Option 1:"));
        panel.add(option1Field);
        panel.add(new JLabel("Option 2:"));
        panel.add(option2Field);
        panel.add(new JLabel("Option 3:"));
        panel.add(option3Field);
        panel.add(new JLabel("Option 4:"));
        panel.add(option4Field);
        panel.add(new JLabel("Correct Answer:"));
        panel.add(correctBox);
        panel.add(new JLabel("Time Limit (seconds):"));
        panel.add(timeLimitSpinner);
        panel.add(new JLabel("Points:"));
        panel.add(pointsSpinner);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Question",
                                                   JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String[] options = {
                option1Field.getText(), option2Field.getText(),
                option3Field.getText(), option4Field.getText()
            };
            server.getQuestionManager().addQuestion(
                questionField.getText(), options, correctBox.getSelectedIndex(),
                (int)timeLimitSpinner.getValue(), (int)pointsSpinner.getValue()
            );
            loadQuestions();
        }
    }

    private void removeQuestion() {
        int selectedIndex = questionList.getSelectedIndex();
        if (selectedIndex != -1) {
            String selected = questionListModel.get(selectedIndex);
            int questionId = Integer.parseInt(selected.substring(1, selected.indexOf(':')));
            server.getQuestionManager().removeQuestion(questionId);
            loadQuestions();
        }
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * 2️⃣ & 4️⃣ Enhanced updateStatus with student status tracking and animated leaderboard
     */
    public void updateStatus() {
        SwingUtilities.invokeLater(() -> {
            // Update students count
            int count = server.getConnectedClientsCount();
            studentsCountLabel.setText("Students: " + count);

            // 2️⃣ Update student status panel with color-coded states
            studentStatusPanel.removeAll();
            for (ClientHandler handler : server.getClients()) {
                if (handler.getStudent() != null) {
                    Student s = handler.getStudent();
                    JPanel studentRow = new JPanel(new FlowLayout(FlowLayout.LEFT));

                    // Status indicator (green = answered, yellow = viewing, red = not answered)
                    JLabel statusDot = new JLabel("●");
                    statusDot.setFont(new Font("Arial", Font.BOLD, 16));

                    // Check if student answered current question
                    boolean answered = server.hasStudentAnswered(s.getStudentId());
                    if (answered) {
                        statusDot.setForeground(new Color(0, 180, 0)); // Green
                        statusDot.setToolTipText("Answered");
                    } else if (server.getSession().isActive()) {
                        statusDot.setForeground(new Color(255, 165, 0)); // Orange
                        statusDot.setToolTipText("Viewing question");
                    } else {
                        statusDot.setForeground(Color.GRAY);
                        statusDot.setToolTipText("Waiting");
                    }

                    JLabel nameLabel = new JLabel(String.format("%-20s Score: %-4d",
                        s.getName(), s.getScore()));
                    nameLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));

                    studentRow.add(statusDot);
                    studentRow.add(nameLabel);
                    studentStatusPanel.add(studentRow);
                }
            }
            studentStatusPanel.revalidate();
            studentStatusPanel.repaint();

            // 4️⃣ Enhanced leaderboard with rank changes and podium display
            List<Student> leaderboard = server.getScoringSystem().getLeaderboard();
            StringBuilder lb = new StringBuilder();

            // Podium display for top 3
            lb.append("═══════════════════════════════════════════════════\n");
            lb.append("                    🏆 PODIUM 🏆\n");
            lb.append("═══════════════════════════════════════════════════\n");

            if (leaderboard.size() > 0) {
                Student first = leaderboard.get(0);
                lb.append(String.format("        🥇 1st: %-15s %d pts\n", first.getName(), first.getScore()));
            }
            if (leaderboard.size() > 1) {
                Student second = leaderboard.get(1);
                lb.append(String.format("    🥈 2nd: %-15s %d pts\n", second.getName(), second.getScore()));
            }
            if (leaderboard.size() > 2) {
                Student third = leaderboard.get(2);
                lb.append(String.format("🥉 3rd: %-15s %d pts\n", third.getName(), third.getScore()));
            }

            lb.append("═══════════════════════════════════════════════════\n\n");
            lb.append(String.format("%-4s  %-20s  %-6s  %-8s  %-8s  %-4s\n",
                "Rank", "Name", "Score", "Correct", "Accuracy", "Δ"));
            lb.append("-".repeat(70)).append("\n");

            for (int i = 0; i < leaderboard.size(); i++) {
                Student s = leaderboard.get(i);
                int currentRank = i + 1;

                // Detect rank changes
                String rankChange = "–";
                if (previousRanks.containsKey(s.getStudentId())) {
                    int prevRank = previousRanks.get(s.getStudentId());
                    if (prevRank > currentRank) rankChange = "↑";
                    else if (prevRank < currentRank) rankChange = "↓";
                }

                // Detect score changes
                String scoreIndicator = "";
                if (previousScores.containsKey(s.getStudentId())) {
                    int prevScore = previousScores.get(s.getStudentId());
                    int scoreDiff = s.getScore() - prevScore;
                    if (scoreDiff > 0) scoreIndicator = " (+" + scoreDiff + ")";
                }

                lb.append(String.format("%-4d  %-20s  %-6d%-10s  %-8d  %-8.1f%%  %-4s\n",
                    currentRank, s.getName(), s.getScore(), scoreIndicator,
                    s.getCorrectAnswers(), s.getAccuracy(), rankChange));

                // Update tracking maps
                previousRanks.put(s.getStudentId(), currentRank);
                previousScores.put(s.getStudentId(), s.getScore());
            }

            leaderboardArea.setText(lb.toString());
        });
    }
}

