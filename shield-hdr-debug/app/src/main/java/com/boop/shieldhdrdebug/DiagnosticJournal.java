package com.boop.shieldhdrdebug;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

final class DiagnosticJournal {
    static final int MAX_EVENTS = 80;

    private static final String PREFS = "hdr_debug_journal";
    private static final String KEY_SESSION_COUNTER = "session_counter";
    private static final String KEY_CURRENT_SESSION = "current_session";
    private static final String KEY_CURRENT_PID = "current_pid";
    private static final String KEY_SESSION_START_WALL = "session_start_wall";
    private static final String KEY_SESSION_START_ELAPSED = "session_start_elapsed";
    private static final String KEY_LAST_HEARTBEAT_WALL = "last_heartbeat_wall";
    private static final String KEY_LAST_HEARTBEAT_ELAPSED = "last_heartbeat_elapsed";
    private static final String KEY_PREVIOUS_SESSION = "previous_session";
    private static final String KEY_EVENTS = "events";

    private final SharedPreferences preferences;
    private int session;
    private int pid;

    DiagnosticJournal(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    synchronized void beginSession(int newPid) {
        int oldSession = preferences.getInt(KEY_CURRENT_SESSION, 0);
        int oldPid = preferences.getInt(KEY_CURRENT_PID, 0);
        long oldHeartbeatWall = preferences.getLong(KEY_LAST_HEARTBEAT_WALL, 0L);
        String previous = oldSession == 0
                ? "none"
                : String.format(Locale.US, "#%d pid=%d lastHB=%s", oldSession, oldPid,
                        wallTime(oldHeartbeatWall));

        session = preferences.getInt(KEY_SESSION_COUNTER, 0) + 1;
        pid = newPid;
        long nowWall = System.currentTimeMillis();
        long nowElapsed = SystemClock.elapsedRealtime();
        preferences.edit()
                .putInt(KEY_SESSION_COUNTER, session)
                .putInt(KEY_CURRENT_SESSION, session)
                .putInt(KEY_CURRENT_PID, pid)
                .putLong(KEY_SESSION_START_WALL, nowWall)
                .putLong(KEY_SESSION_START_ELAPSED, nowElapsed)
                .putLong(KEY_LAST_HEARTBEAT_WALL, nowWall)
                .putLong(KEY_LAST_HEARTBEAT_ELAPSED, nowElapsed)
                .putString(KEY_PREVIOUS_SESSION, previous)
                .commit();
        appendEvent("SESSION START pid=" + pid + " previous=" + previous);
    }

    synchronized void heartbeat() {
        preferences.edit()
                .putLong(KEY_LAST_HEARTBEAT_WALL, System.currentTimeMillis())
                .putLong(KEY_LAST_HEARTBEAT_ELAPSED, SystemClock.elapsedRealtime())
                .commit();
    }

    synchronized void appendEvent(String event) {
        String line = wallTime(System.currentTimeMillis()) + " S" + session + " " + event;
        String rolled = EventRing.append(preferences.getString(KEY_EVENTS, ""), line, MAX_EVENTS);
        preferences.edit().putString(KEY_EVENTS, rolled).commit();
    }

    synchronized Snapshot snapshot() {
        String events = preferences.getString(KEY_EVENTS, "");
        List<String> eventLines = events.isEmpty()
                ? new ArrayList<>()
                : new ArrayList<>(Arrays.asList(events.split("\\n")));
        return new Snapshot(
                preferences.getInt(KEY_CURRENT_SESSION, session),
                preferences.getInt(KEY_CURRENT_PID, pid),
                preferences.getLong(KEY_SESSION_START_WALL, 0L),
                preferences.getLong(KEY_LAST_HEARTBEAT_WALL, 0L),
                preferences.getString(KEY_PREVIOUS_SESSION, "none"),
                eventLines);
    }

    private static String wallTime(long value) {
        if (value <= 0L) {
            return "never";
        }
        return new SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(new Date(value));
    }

    static final class Snapshot {
        final int session;
        final int pid;
        final long startWall;
        final long lastHeartbeatWall;
        final String previousSession;
        final List<String> events;

        Snapshot(int session, int pid, long startWall, long lastHeartbeatWall,
                 String previousSession, List<String> events) {
            this.session = session;
            this.pid = pid;
            this.startWall = startWall;
            this.lastHeartbeatWall = lastHeartbeatWall;
            this.previousSession = previousSession;
            this.events = events;
        }

        long heartbeatAgeMs() {
            return lastHeartbeatWall <= 0L ? -1L : Math.max(0L, System.currentTimeMillis() - lastHeartbeatWall);
        }

        List<String> recentEvents(int count) {
            int from = Math.max(0, events.size() - Math.max(0, count));
            return new ArrayList<>(events.subList(from, events.size()));
        }
    }
}
