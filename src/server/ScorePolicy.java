package server;

/** Stateless scoring policy with time bonus and streak bonus. */
public class ScorePolicy {

    /**
     * @param correct       true if answer is correct
     * @param responseMs    time from question shown to answer (ms); 0 if unknown
     * @param currentStreak number of consecutive correct answers BEFORE this one
     * @return delta points to add for this answer
     */
    public static int points(boolean correct, long responseMs, int currentStreak) {
        if (!correct) return 0;
        int base = 10; // base points for correct

        // Time bonus: up to +5 if answered quickly (<10s). Scales down every 2s.
        int timeBonus = 0;
        if (responseMs > 0) {
            timeBonus = Math.max(0, 5 - (int)(responseMs / 2000L)); // 0..5
        }

        // Streak bonus: +1 per consecutive correct, capped at +5
        int streakBonus = Math.min(5, Math.max(0, currentStreak));

        return base + timeBonus + streakBonus;
    }

    private ScorePolicy() {}
}
