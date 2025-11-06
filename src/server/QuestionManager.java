package server;

import model.*;
import common.Protocol;
import java.io.*;
import java.util.*;

/**
 * QuestionManager handles quiz question management
 * Demonstrates data management and business logic
 */
public class QuestionManager {
    private final List<Question> questionBank;
    private int nextQuestionId;

    public QuestionManager() {
        this.questionBank = new ArrayList<>();
        this.nextQuestionId = 1;
        loadDefaultQuestions();
    }

    /**
     * Adds a new question to the bank
     */
    public synchronized Question addQuestion(String questionText, String[] options,
                                             int correctAnswer, int timeLimit, int points) {
        Question question = new Question(nextQuestionId++, questionText, options,
                                        correctAnswer, timeLimit, points);
        if (!validateQuestion(question)) {
            throw new IllegalArgumentException("Invalid question");
        }
        questionBank.add(question);
        return question;
    }

    /**
     * Removes a question from the bank
     */
    public synchronized boolean removeQuestion(int questionId) {
        return questionBank.removeIf(q -> q.getId() == questionId);
    }

    /**
     * Gets a question by ID
     */
    public synchronized Question getQuestion(int questionId) {
        return questionBank.stream()
                .filter(q -> q.getId() == questionId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all questions
     */
    public synchronized List<Question> getAllQuestions() {
        return new ArrayList<>(questionBank);
    }

    /**
     * Gets a random subset of questions for a quiz
     */
    public synchronized List<Question> getRandomQuestions(int count) {
        List<Question> shuffled = new ArrayList<>(questionBank);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    /**
     * Clears all questions
     */
    public synchronized void clearQuestions() {
        questionBank.clear();
        nextQuestionId = 1;
    }

    /**
     * Loads default questions for demo
     */
    private void loadDefaultQuestions() {
        addQuestion(
            "What is the default port for HTTP?",
            new String[]{"80", "443", "8080", "3000"},
            0, 30, 10
        );

        addQuestion(
            "Which Java class is used for TCP socket communication?",
            new String[]{"DatagramSocket", "Socket", "ServerSocket", "URLConnection"},
            1, 30, 10
        );

        addQuestion(
            "What does NIO stand for in Java?",
            new String[]{"Network Input Output", "New Input Output", "Non-blocking IO", "Native IO"},
            1, 30, 10
        );

        addQuestion(
            "Which protocol is connection-oriented?",
            new String[]{"UDP", "TCP", "ICMP", "DNS"},
            1, 30, 10
        );

        addQuestion(
            "What is the maximum value of a port number?",
            new String[]{"1024", "32767", "65535", "99999"},
            2, 30, 10
        );

        addQuestion(
            "Which layer of OSI model does socket programming operate at?",
            new String[]{"Physical", "Data Link", "Network", "Transport"},
            3, 30, 10
        );

        addQuestion(
            "What is the loopback IP address?",
            new String[]{"192.168.0.1", "127.0.0.1", "0.0.0.0", "255.255.255.255"},
            1, 30, 10
        );

        addQuestion(
            "Which method is used to accept client connections in ServerSocket?",
            new String[]{"connect()", "accept()", "listen()", "bind()"},
            1, 30, 10
        );
    }

    /**
     * Validates a question
     */
    public boolean validateQuestion(Question question) {
        if (question == null) return false;
        if (question.getQuestionText() == null || question.getQuestionText().trim().isEmpty()) return false;
        String[] opts = question.getOptions();
        if (opts.length != 4) return false;
        for (String o : opts) {
            if (o == null || o.trim().isEmpty()) return false;
        }
        if (question.getCorrectAnswer() < 0 || question.getCorrectAnswer() > 3) return false;
        if (question.getTimeLimit() <= 0) return false;
        if (question.getPoints() <= 0) return false;
        return true;
    }

    // ------------------------------
    // Load questions from CSV file
    // Format: questionText,opt0,opt1,opt2,opt3,correctIndex,timeLimit,points
    // ------------------------------
    public synchronized int loadFromCsv(String path) throws IOException {
        int added = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = splitCsv(line);
                if (parts.length < 8) continue;
                String qText = parts[0];
                String[] options = new String[]{parts[1], parts[2], parts[3], parts[4]};
                int correct = Integer.parseInt(parts[5]);
                int time = Integer.parseInt(parts[6]);
                int pts = Integer.parseInt(parts[7]);
                try {
                    addQuestion(qText, options, correct, time, pts);
                    added++;
                } catch (IllegalArgumentException ignore) { }
            }
        }
        return added;
    }

    private static String[] splitCsv(String line) {
        // simple CSV splitter;
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '"' ) { inQuotes = !inQuotes; }
            else if (c == ',' && !inQuotes) { out.add(cur.toString().trim()); cur.setLength(0); }
            else { cur.append(c); }
        }
        out.add(cur.toString().trim());
        return out.toArray(new String[0]);
    }

    // ------------------------------
    // Message builder (with messageId) for QUESTION packets
    // when sending to a client; client returns ACK|<messageId>|OK
    // ------------------------------
    public static String buildQuestionMessage(Question q) {
        String messageId = Protocol.newMessageId();
        String payload = q.toProtocolString();
        return Protocol.createMessage(Protocol.QUESTION, messageId, payload);
    }
}
