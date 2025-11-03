package model;

import java.io.Serializable;

/**
 * Student model representing a quiz participant
 */
public class Student implements Serializable, Comparable<Student> {
    private static final long serialVersionUID = 1L;

    private String studentId;
    private String name;
    private int score;
    private int answeredQuestions;
    private int correctAnswers;
    private long connectionTime;

    public Student(String studentId, String name) {
        this.studentId = studentId;
        this.name = name;
        this.score = 0;
        this.answeredQuestions = 0;
        this.correctAnswers = 0;
        this.connectionTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public int getAnsweredQuestions() {
        return answeredQuestions;
    }

    public void incrementAnsweredQuestions() {
        this.answeredQuestions++;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void incrementCorrectAnswers() {
        this.correctAnswers++;
    }

    public long getConnectionTime() {
        return connectionTime;
    }

    public double getAccuracy() {
        if (answeredQuestions == 0) return 0.0;
        return (double) correctAnswers / answeredQuestions * 100;
    }

    @Override
    public int compareTo(Student other) {
        // Sort by score descending, then by correct answers, then by name
        if (this.score != other.score) {
            return Integer.compare(other.score, this.score);
        }
        if (this.correctAnswers != other.correctAnswers) {
            return Integer.compare(other.correctAnswers, this.correctAnswers);
        }
        return this.name.compareTo(other.name);
    }

    public String toProtocolString() {
        return studentId + "~" + name + "~" + score + "~" + correctAnswers + "~" + answeredQuestions;
    }

    @Override
    public String toString() {
        return name + " (ID: " + studentId + ") - Score: " + score;
    }
}

