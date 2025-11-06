package model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Question model representing a quiz question
 */
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String questionText;
    private String[] options;   // immutable defensive copy
    private int correctAnswer;  // Index of correct option (0-3)
    private int timeLimit;      // seconds
    private int points;

    public Question(int id, String questionText, String[] options, int correctAnswer, int timeLimit, int points) {
        // Basic validation (kept lightweight)
        if (questionText == null || questionText.trim().isEmpty()) {
            throw new IllegalArgumentException("questionText is required");
        }
        if (options == null || options.length != 4) {
            throw new IllegalArgumentException("options must be length 4");
        }
        if (correctAnswer < -1 || correctAnswer > 3) {
            throw new IllegalArgumentException("correctAnswer must be -1..3");
        }
        if (timeLimit <= 0) {
            throw new IllegalArgumentException("timeLimit must be > 0");
        }
        if (points <= 0) {
            throw new IllegalArgumentException("points must be > 0");
        }

        this.id = id;
        this.questionText = questionText;
        // defensive copy
        this.options = Arrays.copyOf(options, options.length);
        this.correctAnswer = correctAnswer;
        this.timeLimit = timeLimit;
        this.points = points;
    }

    // Getters and Setters (with defensive copy on options)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String[] getOptions() { return Arrays.copyOf(options, options.length); }
    public void setOptions(String[] options) {
        if (options == null || options.length != 4) {
            throw new IllegalArgumentException("options must be length 4");
        }
        this.options = Arrays.copyOf(options, options.length);
    }

    public int getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(int correctAnswer) {
        if (correctAnswer < -1 || correctAnswer > 3) {
            throw new IllegalArgumentException("correctAnswer must be -1..3");
        }
        this.correctAnswer = correctAnswer;
    }

    public int getTimeLimit() { return timeLimit; }
    public void setTimeLimit(int timeLimit) {
        if (timeLimit <= 0) throw new IllegalArgumentException("timeLimit must be > 0");
        this.timeLimit = timeLimit;
    }

    public int getPoints() { return points; }
    public void setPoints(int points) {
        if (points <= 0) throw new IllegalArgumentException("points must be > 0");
        this.points = points;
    }

    /**
     * Converts question to protocol string format (Without leaking correct answer).
     * Format: id~questionText~timeLimit~points~opt0~opt1~opt2~opt3
     * Uses Protocol-style escaping for '~'
     */
    public String toProtocolString() {
        String[] fields = new String[] {
            String.valueOf(id),
            ProtocolSafe.escape(questionText),
            String.valueOf(timeLimit),
            String.valueOf(points),
            ProtocolSafe.escape(options[0]),
            ProtocolSafe.escape(options[1]),
            ProtocolSafe.escape(options[2]),
            ProtocolSafe.escape(options[3])
        };
        return String.join("~", fields);
    }

    /**
     * Creates Question from protocol string (correct answer set to -1)
     */
    public static Question fromProtocolString(String data) {
        if (data == null) throw new IllegalArgumentException("data is null");
        String[] parts = ProtocolSafe.splitFields(data, "~", 8);
        if (parts.length < 8) throw new IllegalArgumentException("invalid question payload");

        int id = Integer.parseInt(parts[0]);
        String questionText = ProtocolSafe.unescape(parts[1]);
        int timeLimit = Integer.parseInt(parts[2]);
        int points = Integer.parseInt(parts[3]);
        String[] options = new String[] {
            ProtocolSafe.unescape(parts[4]),
            ProtocolSafe.unescape(parts[5]),
            ProtocolSafe.unescape(parts[6]),
            ProtocolSafe.unescape(parts[7])
        };
        return new Question(id, questionText, options, -1, timeLimit, points);
    }

    // Small internal helper so we can reuse in Protocol without circular dependency
    private static final class ProtocolSafe {
        static String escape(String s) {
            if (s == null) return "";
            // escape backslash first, then delimiter
            return s.replace("\\", "\\\\").replace("~", "\\~");
        }
        static String unescape(String s) {
            if (s == null) return "";
            StringBuilder out = new StringBuilder();
            boolean esc = false;
            for (char c : s.toCharArray()) {
                if (esc) { out.append(c); esc = false; }
                else if (c == '\\') { esc = true; }
                else { out.append(c); }
            }
            return out.toString();
        }
        static String[] splitFields(String s, String delim, int expected) {
            // split respecting backslash escapes
            java.util.List<String> parts = new java.util.ArrayList<>();
            StringBuilder cur = new StringBuilder();
            boolean esc = false;
            for (char c : s.toCharArray()) {
                if (esc) { cur.append(c); esc = false; }
                else if (c == '\\') { esc = true; }
                else if (String.valueOf(c).equals(delim)) { parts.add(cur.toString()); cur.setLength(0); }
                else { cur.append(c); }
            }
            parts.add(cur.toString());
            return parts.toArray(new String[0]);
        }
    }
}
