package server;

import model.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ScoringSystem manages student scores and leaderboard
 * Demonstrates synchronization for thread-safe operations
 */
public class ScoringSystem {
    private QuizSession session;
    private Map<String, List<AnswerRecord>> answerHistory;

    public ScoringSystem(QuizSession session) {
        this.session = session;
        this.answerHistory = new ConcurrentHashMap<>();
    }

    /**
     * Records a student's answer
     */
    public synchronized AnswerResult recordAnswer(String studentId, int questionId,
                                                  int answer, long timeTaken) {
        Student student = session.getStudent(studentId);
        if (student == null) {
            return new AnswerResult(false, 0, "Student not found");
        }

        Question question = session.getCurrentQuestion();
        if (question == null || question.getId() != questionId) {
            return new AnswerResult(false, 0, "Invalid question");
        }

        boolean isCorrect = session.processAnswer(studentId, questionId, answer);
        int pointsEarned = 0;

        if (isCorrect) {
            // Award points based on time (bonus for faster answers)
            pointsEarned = calculatePoints(question, timeTaken);
        }

        // Record answer in history
        AnswerRecord record = new AnswerRecord(questionId, answer, isCorrect,
                                               pointsEarned, timeTaken);
        answerHistory.computeIfAbsent(studentId, k -> new ArrayList<>()).add(record);

        String message = isCorrect ? "Correct! +" + pointsEarned + " points" : "Incorrect";
        return new AnswerResult(isCorrect, pointsEarned, message);
    }

    /**
     * Calculates points based on question value and time taken
     */
    private int calculatePoints(Question question, long timeTaken) {
        int basePoints = question.getPoints();
        long timeLimit = question.getTimeLimit() * 1000L; // Convert to milliseconds

        // Award bonus points for fast answers (up to 50% bonus)
        if (timeTaken < timeLimit / 2) {
            return (int) (basePoints * 1.5);
        } else if (timeTaken < timeLimit * 0.75) {
            return (int) (basePoints * 1.25);
        }
        return basePoints;
    }

    /**
     * Gets current leaderboard
     */
    public List<Student> getLeaderboard() {
        return session.getLeaderboard();
    }

    /**
     * Gets top N students
     */
    public List<Student> getTopStudents(int count) {
        List<Student> leaderboard = getLeaderboard();
        return leaderboard.subList(0, Math.min(count, leaderboard.size()));
    }

    /**
     * Gets student rank
     */
    public int getStudentRank(String studentId) {
        List<Student> leaderboard = getLeaderboard();
        for (int i = 0; i < leaderboard.size(); i++) {
            if (leaderboard.get(i).getStudentId().equals(studentId)) {
                return i + 1;
            }
        }
        return -1;
    }

    /**
     * Gets answer history for a student
     */
    public List<AnswerRecord> getAnswerHistory(String studentId) {
        return answerHistory.getOrDefault(studentId, new ArrayList<>());
    }

    /**
     * Generates final results summary
     */
    public String generateResultsSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== QUIZ RESULTS ===\n\n");

        List<Student> leaderboard = getLeaderboard();
        sb.append("Total Participants: ").append(leaderboard.size()).append("\n\n");

        sb.append("LEADERBOARD:\n");
        sb.append("Rank | Name | Score | Correct | Accuracy\n");
        sb.append("-----|------|-------|---------|----------\n");

        for (int i = 0; i < leaderboard.size(); i++) {
            Student student = leaderboard.get(i);
            sb.append(String.format("%-4d | %-20s | %-5d | %-7d | %.1f%%\n",
                    i + 1,
                    student.getName(),
                    student.getScore(),
                    student.getCorrectAnswers(),
                    student.getAccuracy()));
        }

        return sb.toString();
    }

    /**
     * Converts leaderboard to protocol string
     */
    public String getLeaderboardProtocolString() {
        List<Student> leaderboard = getLeaderboard();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leaderboard.size(); i++) {
            if (i > 0) sb.append("||");
            Student s = leaderboard.get(i);
            sb.append((i + 1)).append("~")
              .append(s.getName()).append("~")
              .append(s.getScore()).append("~")
              .append(s.getCorrectAnswers()).append("~")
              .append(s.getAnsweredQuestions());
        }
        return sb.toString();
    }

    // Inner classes for result tracking
    public static class AnswerResult {
        private boolean correct;
        private int pointsEarned;
        private String message;

        public AnswerResult(boolean correct, int pointsEarned, String message) {
            this.correct = correct;
            this.pointsEarned = pointsEarned;
            this.message = message;
        }

        public boolean isCorrect() { return correct; }
        public int getPointsEarned() { return pointsEarned; }
        public String getMessage() { return message; }
    }

    public static class AnswerRecord {
        private int questionId;
        private int answer;
        private boolean correct;
        private int pointsEarned;
        private long timeTaken;
        private long timestamp;

        public AnswerRecord(int questionId, int answer, boolean correct,
                          int pointsEarned, long timeTaken) {
            this.questionId = questionId;
            this.answer = answer;
            this.correct = correct;
            this.pointsEarned = pointsEarned;
            this.timeTaken = timeTaken;
            this.timestamp = System.currentTimeMillis();
        }

        public int getQuestionId() { return questionId; }
        public int getAnswer() { return answer; }
        public boolean isCorrect() { return correct; }
        public int getPointsEarned() { return pointsEarned; }
        public long getTimeTaken() { return timeTaken; }
        public long getTimestamp() { return timestamp; }
    }

    // ───────────────────────────────────────────────
// NEW ANALYTICS METHOD (your clear contribution)
// ───────────────────────────────────────────────

/**
 * Generates detailed analytics for each question using recorded answers.
 *
 * For each question:
 * - Attempts
 * - Correct answers
 * - Accuracy %
 * - Average response time
 * - Fastest & slowest response
 *
 * This uses data that comes from many clients over the network,
 * but is aggregated here in a thread-safe server-side module.
 */
public String generateDetailedAnalytics() {
    StringBuilder sb = new StringBuilder();
    sb.append("=== QUESTION ANALYTICS ===\n\n");

    // Build questionId -> list of AnswerRecord (from all students)
    Map<Integer, List<AnswerRecord>> byQuestion = new HashMap<>();
    for (Map.Entry<String, List<AnswerRecord>> entry : answerHistory.entrySet()) {
        for (AnswerRecord record : entry.getValue()) {
            byQuestion.computeIfAbsent(record.getQuestionId(), k -> new ArrayList<>()).add(record);
        }
    }

    if (byQuestion.isEmpty()) {
        sb.append("No answers recorded yet.\n");
        return sb.toString();
    }

    // Sort questions by ID for clean display
    List<Integer> questionIds = new ArrayList<>(byQuestion.keySet());
    Collections.sort(questionIds);

    sb.append(String.format("%-5s | %-8s | %-8s | %-9s | %-11s | %-11s | %-11s%n",
            "QID", "Attempts", "Correct", "Accuracy", "Avg Time", "Fastest", "Slowest"));
    sb.append("---------------------------------------------------------------------\n");

    for (Integer qid : questionIds) {
        List<AnswerRecord> records = byQuestion.get(qid);
        int attempts = records.size();
        int correct = 0;
        long totalTime = 0L;
        long fastest = Long.MAX_VALUE;
        long slowest = 0L;

        for (AnswerRecord r : records) {
            if (r.isCorrect()) correct++;
            long t = r.getTimeTaken();
            totalTime += t;
            if (t < fastest) fastest = t;
            if (t > slowest) slowest = t;
        }

        double accuracy = attempts > 0 ? (correct * 100.0 / attempts) : 0.0;
        double avgSeconds = attempts > 0 ? (totalTime / (double) attempts) / 1000.0 : 0.0;
        double fastestSec = (fastest == Long.MAX_VALUE) ? 0.0 : fastest / 1000.0;
        double slowestSec = slowest / 1000.0;

        sb.append(String.format("%-5d | %-8d | %-8d | %7.2f%% | %8.2fs | %8.2fs | %8.2fs%n",
                qid, attempts, correct, accuracy, avgSeconds, fastestSec, slowestSec));
    }

    return sb.toString();
}

}

