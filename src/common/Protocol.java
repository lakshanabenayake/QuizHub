package common;

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

    /**
     * Creates a protocol message
     * @param type Message type
     * @param data Message data
     * @return Formatted protocol message
     */
    public static String createMessage(String type, String data) {
        return type + DELIMITER + data;
    }

    /**
     * Parses a protocol message
     * @param message Raw message
     * @return Array [type, data]
     */
    public static String[] parseMessage(String message) {
        if (message == null || message.isEmpty()) {
            return new String[]{"", ""};
        }
        String[] parts = message.split("\\" + DELIMITER, 2);
        if (parts.length == 2) {
            return parts;
        } else if (parts.length == 1) {
            return new String[]{parts[0], ""};
        }
        return new String[]{"", ""};
    }
}

