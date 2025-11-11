package server;

import model.*;
import common.Protocol;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

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

    public ServerUI(QuizServer server) {
        this.server = server;
        initComponents();
    }

    private void initComponents() {
        setTitle("QuizHub Server - Teacher Dashboard");
        setSize(1200, 800);
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
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Server Controls"));

        startServerBtn = new JButton("Start Server");
        stopServerBtn = new JButton("Stop Server");
        startQuizBtn = new JButton("Start Quiz");
        nextQuestionBtn = new JButton("Next Question");
        endQuizBtn = new JButton("End Quiz");

        stopServerBtn.setEnabled(false);
        startQuizBtn.setEnabled(false);
        nextQuestionBtn.setEnabled(false);
        endQuizBtn.setEnabled(false);

        statusLabel = new JLabel("Server: Stopped");
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));

        studentsCountLabel = new JLabel("Students: 0");
        studentsCountLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Action listeners
        startServerBtn.addActionListener(e -> startServer());
        stopServerBtn.addActionListener(e -> stopServer());
        startQuizBtn.addActionListener(e -> startQuiz());
        nextQuestionBtn.addActionListener(e -> nextQuestion());
        endQuizBtn.addActionListener(e -> endQuiz());

        panel.add(startServerBtn);
        panel.add(stopServerBtn);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        panel.add(startQuizBtn);
        panel.add(nextQuestionBtn);
        panel.add(endQuizBtn);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(statusLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(studentsCountLabel);

        return panel;
    }

    private JPanel createQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Question Bank"));

        questionListModel = new DefaultListModel<>();
        questionList = new JList<>(questionListModel);
        questionList.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(questionList);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadQuestionsBtn = new JButton("Load Questions");
        JButton addQuestionBtn = new JButton("Add Question");
        JButton removeQuestionBtn = new JButton("Remove Selected");

        loadQuestionsBtn.addActionListener(e -> loadQuestions());
        addQuestionBtn.addActionListener(e -> addQuestion());
        removeQuestionBtn.addActionListener(e -> removeQuestion());

        buttonPanel.add(loadQuestionsBtn);
        buttonPanel.add(addQuestionBtn);
        buttonPanel.add(removeQuestionBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMonitorPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));

        // Students panel
        JPanel studentsPanel = new JPanel(new BorderLayout());
        studentsPanel.setBorder(BorderFactory.createTitledBorder("Connected Students"));
        studentsArea = new JTextArea(10, 30);
        studentsArea.setEditable(false);
        studentsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        studentsPanel.add(new JScrollPane(studentsArea), BorderLayout.CENTER);

        // Leaderboard panel
        JPanel leaderboardPanel = new JPanel(new BorderLayout());
        leaderboardPanel.setBorder(BorderFactory.createTitledBorder("Live Leaderboard"));
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
        loadQuestions();
    }

    private void stopServer() {
    // Disable buttons immediately (prevent double-clicks)
    startServerBtn.setEnabled(false);
    stopServerBtn.setEnabled(false);
    startQuizBtn.setEnabled(false);
    nextQuestionBtn.setEnabled(false);
    endQuizBtn.setEnabled(false);
    statusLabel.setText("Server: Stopping...");

    // Stop on a background thread so the UI stays responsive
    new Thread(() -> {
        server.stop();
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Server: Stopped");
            statusLabel.setForeground(Color.RED);
            startServerBtn.setEnabled(true);
            stopServerBtn.setEnabled(false);
        });
    }, "StopServerThread").start();
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
    }

    private void nextQuestion() {
        server.sendNextQuestion();
    }

    private void endQuiz() {
        server.endQuiz();
        startQuizBtn.setEnabled(true);
        nextQuestionBtn.setEnabled(false);
        endQuizBtn.setEnabled(false);
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

    public void updateStatus() {
        SwingUtilities.invokeLater(() -> {
            // Update students count
            int count = server.getConnectedClientsCount();
            studentsCountLabel.setText("Students: " + count);

            // Update students list
            StringBuilder sb = new StringBuilder();
            for (ClientHandler handler : server.getClients()) {
                if (handler.getStudent() != null) {
                    Student s = handler.getStudent();
                    sb.append(String.format("%-20s | Score: %-4d | Answered: %d\n",
                        s.getName(), s.getScore(), s.getAnsweredQuestions()));
                }
            }
            studentsArea.setText(sb.toString());

            // Update leaderboard
            List<Student> leaderboard = server.getScoringSystem().getLeaderboard();
            StringBuilder lb = new StringBuilder();
            lb.append(String.format("%-4s | %-20s | %-6s | %-8s | %-8s\n",
                "Rank", "Name", "Score", "Correct", "Accuracy"));
            lb.append("-".repeat(65)).append("\n");
            for (int i = 0; i < leaderboard.size(); i++) {
                Student s = leaderboard.get(i);
                lb.append(String.format("%-4d | %-20s | %-6d | %-8d | %.1f%%\n",
                    i+1, s.getName(), s.getScore(), s.getCorrectAnswers(), s.getAccuracy()));
            }
            leaderboardArea.setText(lb.toString());
        });
    }
}

