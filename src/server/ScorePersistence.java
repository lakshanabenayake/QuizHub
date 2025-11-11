package server;

import model.Student;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Save leaderboard/results to a CSV file (for evidence & analytics). */
public class ScorePersistence {

    /**
     * Save students as a CSV leaderboard.
     * Columns: rank, name, score, correct, answered, accuracy_pct
     */
    public static void saveCsvFromStudents(List<Student> students, File file) throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file, false), StandardCharsets.UTF_8))) {

            pw.println("rank,name,score,correct,answered,accuracy_pct");

            int rank = 1;
            for (Student s : students) {
                String name = s.getName() == null ? "" : s.getName().replace(",", " ");
                int score = s.getScore();
                int correct = s.getCorrectAnswers();
                int answered = s.getAnsweredQuestions();
                double accuracyPct = s.getAccuracy();

                pw.printf("%d,%s,%d,%d,%d,%.1f%n",
                        rank++, name, score, correct, answered, accuracyPct);
            }
        }
    }

    private ScorePersistence() {}
}
