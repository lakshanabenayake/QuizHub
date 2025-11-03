package model;

import java.io.Serializable;

/**
 * Question model representing a quiz question
 */
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String questionText;
    private String[] options;
    private int correctAnswer; // Index of correct option (0-3)
    private int timeLimit; // Time limit in seconds
    private int points;

    public Question(int id, String questionText, String[] options, int correctAnswer, int timeLimit, int points) {
        this.id = id;
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.timeLimit = timeLimit;
        this.points = points;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(int correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    /**
     * Converts question to protocol string format
     */
    public String toProtocolString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append("~");
        sb.append(questionText).append("~");
        sb.append(timeLimit).append("~");
        sb.append(points).append("~");
        for (int i = 0; i < options.length; i++) {
            sb.append(options[i]);
            if (i < options.length - 1) {
                sb.append("~");
            }
        }
        return sb.toString();
    }

    /**
     * Creates Question from protocol string
     */
    public static Question fromProtocolString(String data) {
        String[] parts = data.split("~");
        int id = Integer.parseInt(parts[0]);
        String questionText = parts[1];
        int timeLimit = Integer.parseInt(parts[2]);
        int points = Integer.parseInt(parts[3]);
        String[] options = new String[4];
        System.arraycopy(parts, 4, options, 0, 4);
        return new Question(id, questionText, options, -1, timeLimit, points);
    }
}

