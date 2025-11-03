package model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * QuizSession manages the state of an active quiz
 */
public class QuizSession {
    private String sessionId;
    private List<Question> questions;
    private int currentQuestionIndex;
    private Map<String, Student> students; // studentId -> Student
    private boolean isActive;
    private long sessionStartTime;
    private long currentQuestionStartTime;

    public QuizSession(String sessionId) {
        this.sessionId = sessionId;
        this.questions = new ArrayList<>();
        this.currentQuestionIndex = -1;
        this.students = new ConcurrentHashMap<>();
        this.isActive = false;
    }

    public synchronized void addQuestion(Question question) {
        questions.add(question);
    }

    public synchronized void addStudent(Student student) {
        students.put(student.getStudentId(), student);
    }

    public synchronized void removeStudent(String studentId) {
        students.remove(studentId);
    }

    public Student getStudent(String studentId) {
        return students.get(studentId);
    }

    public Collection<Student> getAllStudents() {
        return students.values();
    }

    public int getStudentCount() {
        return students.size();
    }

    public synchronized Question getCurrentQuestion() {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < questions.size()) {
            return questions.get(currentQuestionIndex);
        }
        return null;
    }

    public synchronized Question nextQuestion() {
        currentQuestionIndex++;
        currentQuestionStartTime = System.currentTimeMillis();
        if (currentQuestionIndex < questions.size()) {
            return questions.get(currentQuestionIndex);
        }
        return null;
    }

    public synchronized boolean hasMoreQuestions() {
        return currentQuestionIndex < questions.size() - 1;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public int getTotalQuestions() {
        return questions.size();
    }

    public void start() {
        this.isActive = true;
        this.sessionStartTime = System.currentTimeMillis();
        this.currentQuestionIndex = -1;
    }

    public void end() {
        this.isActive = false;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getSessionStartTime() {
        return sessionStartTime;
    }

    public long getCurrentQuestionStartTime() {
        return currentQuestionStartTime;
    }

    /**
     * Gets sorted leaderboard
     */
    public List<Student> getLeaderboard() {
        List<Student> leaderboard = new ArrayList<>(students.values());
        Collections.sort(leaderboard);
        return leaderboard;
    }

    /**
     * Processes student answer and updates score
     */
    public synchronized boolean processAnswer(String studentId, int questionId, int answer) {
        Student student = students.get(studentId);
        if (student == null) return false;

        Question question = getCurrentQuestion();
        if (question == null || question.getId() != questionId) return false;

        student.incrementAnsweredQuestions();

        boolean isCorrect = (answer == question.getCorrectAnswer());
        if (isCorrect) {
            student.incrementCorrectAnswers();
            student.addScore(question.getPoints());
        }

        return isCorrect;
    }
}

