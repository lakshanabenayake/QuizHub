package server;

import model.Student;
import server.ScoringSystem.AnswerRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Writes ALL useful data into ONE CSV file.
 * It uses a "record_type" column to distinguish sections:
 *   - student_summary
 *   - answer_detail
 *   - question_stat
 */
public class ReportExporterSingleCSV {

    public static void exportAll(ScoringSystem scoring, File file) throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file, false), StandardCharsets.UTF_8))) {

            // CSV header (superset of all fields)
            pw.println("record_type,rank,name,score,correct,answered,accuracy_pct,avg_response_ms,fastest_ms,slowest_ms,total_time_ms,max_streak,student,question_id,answer,ans_correct,points,time_ms,timestamp,attempts,q_correct,q_accuracy_pct,q_avg_time_ms,q_fastest_ms,q_slowest_ms");

            // ---------- student_summary ----------
            List<Student> students = scoring.getLeaderboard();
            int rank = 1;
            for (Student s : students) {
                List<AnswerRecord> hist = scoring.getAnswerHistory(s.getStudentId());

                long totalTime = 0L, fastest = Long.MAX_VALUE, slowest = 0L;
                int count = 0, maxStreak = 0, curStreak = 0;

                if (hist != null) {
                    for (AnswerRecord r : hist) {
                        long t = Math.max(0L, r.getTimeTaken());
                        totalTime += t;
                        fastest = Math.min(fastest, t);
                        slowest = Math.max(slowest, t);
                        count++;

                        if (r.isCorrect()) { curStreak++; if (curStreak > maxStreak) maxStreak = curStreak; }
                        else curStreak = 0;
                    }
                }
                long avg = (count > 0) ? (totalTime / count) : 0L;
                if (fastest == Long.MAX_VALUE) fastest = 0L;

                pw.printf(Locale.US,
                        "student_summary,%d,%s,%d,%d,%d,%.1f,%d,%d,%d,%d,%d,,,,,,,,,,,,,%n",
                        rank++,
                        csv(s.getName()),
                        s.getScore(),
                        s.getCorrectAnswers(),
                        s.getAnsweredQuestions(),
                        s.getAccuracy(),
                        avg, fastest, slowest, totalTime, maxStreak
                );
            }

            // ---------- answer_detail ----------
            for (Student s : students) {
                List<AnswerRecord> hist = scoring.getAnswerHistory(s.getStudentId());
                if (hist == null) continue;

                for (AnswerRecord r : hist) {
                    pw.printf(Locale.US,
                            "answer_detail,,,,,,,,,,,,%s,%d,%d,%s,%d,%d,%d,,,,,,%n",
                            csv(s.getName()),
                            r.getQuestionId(),
                            r.getAnswer(),
                            r.isCorrect() ? "true" : "false",
                            r.getPointsEarned(),
                            Math.max(0L, r.getTimeTaken()),
                            r.getTimestamp()
                    );
                }
            }

            // ---------- question_stat ----------
            Map<Integer, Agg> agg = new LinkedHashMap<>();
            for (Student s : students) {
                List<AnswerRecord> hist = scoring.getAnswerHistory(s.getStudentId());
                if (hist == null) continue;
                for (AnswerRecord r : hist) {
                    Agg a = agg.computeIfAbsent(r.getQuestionId(), k -> new Agg());
                    a.attempts++;
                    if (r.isCorrect()) a.correct++;
                    long t = Math.max(0L, r.getTimeTaken());
                    a.totalTime += t;
                    a.fastest = Math.min(a.fastest, t);
                    a.slowest = Math.max(a.slowest, t);
                }
            }
            for (Map.Entry<Integer, Agg> e : agg.entrySet()) {
                int q = e.getKey();
                Agg a = e.getValue();
                double acc = (a.attempts > 0) ? (100.0 * a.correct / a.attempts) : 0.0;
                long avg = (a.attempts > 0) ? (a.totalTime / a.attempts) : 0L;
                long fastest = (a.fastest == Long.MAX_VALUE) ? 0L : a.fastest;

                pw.printf(Locale.US,
                        "question_stat,,,,,,,,,,,,,,,,,,,%d,%d,%.1f,%d,%d,%d%n",
                        a.attempts, a.correct, acc, avg, fastest, a.slowest
                );
            }
        }
    }

    // helpers
    private static String csv(String s) {
        if (s == null) return "";
        String v = s.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"")) return "\"" + v + "\"";
        return v;
    }

    private static class Agg {
        int attempts = 0;
        int correct = 0;
        long totalTime = 0L;
        long fastest = Long.MAX_VALUE;
        long slowest = 0L;
    }

    private ReportExporterSingleCSV() {}
}
