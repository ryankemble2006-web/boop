package com.boop.shieldhome;

import java.io.IOException;
import java.util.List;

/** Only explicit supported media targets, checked again before each local mutation. */
public final class LocalMediaClosePolicy {
    public interface Current { boolean valid(); }
    public interface Bridge {
        boolean installed(String packageName) throws Exception;
        void stop(String packageName) throws Exception;
    }
    private static final List<String> NATIVE = List.of("deezer.android.app", "com.google.android.youtube.tv");
    private LocalMediaClosePolicy() { }
    public static int execute(boolean all, long session, String selected, Current current, Bridge bridge) throws Exception {
        if (!all && (session <= 0 || !NATIVE.contains(selected == null ? "" : selected)))
            throw new IllegalArgumentException("This player cannot be closed by the local tools.");
        List<String> targets = all ? NATIVE : List.of(selected);
        int stopped = 0;
        for (String target : targets) {
            check(current);
            if (!bridge.installed(target)) continue;
            check(current);
            bridge.stop(target);
            stopped++;
        }
        return stopped;
    }
    private static void check(Current current) throws IOException {
        if (Thread.currentThread().isInterrupted() || current == null || !current.valid())
            throw new IOException("Player close cancelled.");
    }
}
