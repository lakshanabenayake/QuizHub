package server;

import common.Protocol;

/**
 * Helper to format chat/announce messages using the existing Protocol.MESSAGE type.
 * You can keep using QuizServer#broadcast(Protocol.MESSAGE, data).
 */
public class ChatHub {

    /** General chat line: "name: text" (client shows it in its MESSAGE area). */
    public static String chatLine(String name, String text) {
        String safeName = name == null ? "Player" : name.replace(Protocol.DELIMITER, ":").replace(",", " ");
        String safeText = text == null ? "" : text.replace(Protocol.DELIMITER, " ").replace("\n", " ");
        return safeName + ": " + safeText;
    }

    /** Join/Leave/Info announcements, e.g., "[JOIN] Alice joined" */
    public static String announce(String tag, String who, String extra) {
        String safeTag = tag == null ? "INFO" : tag.replace("[", "").replace("]", "");
        String safeWho = who == null ? "Player" : who.replace(Protocol.DELIMITER, " ").replace(",", " ");
        String suffix = (extra == null || extra.isEmpty()) ? "" : " " + extra.replace("\n", " ");
        return "[" + safeTag.toUpperCase() + "] " + safeWho + suffix;
    }

    private ChatHub() {}
}
