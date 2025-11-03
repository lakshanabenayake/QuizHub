package server;

import model.*;
import common.Protocol;
import java.util.*;

/**
 * QuestionManager handles quiz question management
 * Demonstrates data management and business logic
 */
public class QuestionManager {
    private List<Question> questionBank;
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
    public Question getQuestion(int questionId) {
        return questionBank.stream()
                .filter(q -> q.getId() == questionId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all questions
     */
    public List<Question> getAllQuestions() {
        return new ArrayList<>(questionBank);
    }

    /**
     * Gets a random subset of questions for a quiz
     */
    public List<Question> getRandomQuestions(int count) {
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
        if (question.getOptions() == null || question.getOptions().length != 4) return false;
        if (question.getCorrectAnswer() < 0 || question.getCorrectAnswer() > 3) return false;
        if (question.getTimeLimit() <= 0) return false;
        if (question.getPoints() <= 0) return false;
        return true;
    }
}

