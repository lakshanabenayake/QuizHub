package common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Protocol class defines message types and communication protocol
 * between server and clients
 */
public class Protocol {

    // Message Types
    public static final String CONNECT = "CONNECT";
    public static final String DISCONNECT = "DISCONNECT";
    public static final String STUDENT_JOIN = "STUDENT_JOIN";
    public static final String QUIZ_START = "QUIZ_START";
    public static final String QUIZ_END = "QUIZ_END";
    public static final String QUESTION = "QUESTION";
    public static final String ANSWER = "ANSWER";
    public static final String SCORE_UPDATE = "SCORE_UPDATE";
    public static final String LEADERBOARD = "LEADERBOARD";
    public static final String TIME_UPDATE = "TIME_UPDATE";
    public static final String RESULT = "RESULT";
    public static final String MESSAGE = "MESSAGE";
    public static final String ERROR = "ERROR";
    public static final String ACK = "ACK";

    // Message Delimiters
    public static final String DELIMITER = "|";
    public static final String FIELD_SEPARATOR = "~";

    // Server Configuration
    public static final int DEFAULT_PORT = 8888;
    public static final int BUFFER_SIZE = 4096;

    // Retransmission config (Member 3 concept coverage)
    public static final int ACK_TIMEOUT_MS = 2000; // wait for ACK before retry
    public static final int MAX_RETRIES = 3;

    // --- Message helpers ---

    /**
     * Creates a protocol message: TYPE|messageId|payload
     */
    public static String createMessage(String type, String messageId, String data) {
        return safeJoin(DELIMITER, escape(type), escape(messageId), escape(data));
    }

    /**
     * Legacy: TYPE|payload (kept for backward-compat)
     */
    public static String createMessage(String type, String data) {
        return safeJoin(DELIMITER, escape(type), escape(data));
    }

    /**
     * Parses message into parts:
     * - 3-part form: [type, messageId, data]
     * - 2-part form: [type, data]
     */
    public static String[] parseMessage(String message) {
        if (message == null || message.isEmpty()) {
            return new String[]{"", ""};
        }
        // split only on top-level DELIMITER, respecting escapes
        String[] parts = splitTop(message, DELIMITER);
        if (parts.length == 3) {
            return new String[]{unescape(parts[0]), unescape(parts[1]), unescape(parts[2])};
        } else if (parts.length == 2) {
            return new String[]{unescape(parts[0]), unescape(parts[1])};
        } else if (parts.length == 1) {
            return new String[]{unescape(parts[0]), ""};
        }
        return new String[]{"", ""};
    }

    /** Generate a message id for ACK tracking */
    public static String newMessageId() {
        return UUID.randomUUID().toString();
    }

    // --- Escaping / splitting helpers (support ~ and | inside fields) ---

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace(DELIMITER, "\\" + DELIMITER).replace(FIELD_SEPARATOR, "\\" + FIELD_SEPARATOR);
    }

    public static String unescape(String s) {
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

    /** Split by top-level delimiter (|) respecting backslash escapes */
    public static String[] splitTop(String s, String delim) {
        List<String> parts = new ArrayList<>();
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

    /** Join helper that won’t NPE */
    private static String safeJoin(String delim, String... fields) {
        if (fields == null || fields.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(delim);
            sb.append(fields[i] == null ? "" : fields[i]);
        }
        return sb.toString();
    }
}
