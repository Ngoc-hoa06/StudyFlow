package com.example.studyflow.focus;
/** Clock arithmetic separated from UI and Android lifecycle. All values are milliseconds. */
public final class FocusClock {
    public static long remaining(long storedRemaining, long resumedAt, long now, boolean running) {
        return Math.max(0, storedRemaining - (running ? Math.max(0, now - resumedAt) : 0));
    }
    public static int creditedMinutes(long total, long remaining) {
        return (int) (Math.max(0, total - remaining) / 60000L);
    }
}
