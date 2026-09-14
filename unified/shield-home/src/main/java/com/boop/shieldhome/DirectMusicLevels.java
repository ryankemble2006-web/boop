package com.boop.shieldhome;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Read only fresh output power, never record threads or retained historical threads. */
final class DirectMusicLevels {
    private static final Pattern ROW = Pattern.compile("^\\s*(\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}):\\s*(.*)$");
    static float parse(String dump, long wallNowMs) {
        if (dump == null || dump.length() > 256 * 1024) return -1f;
        boolean output = false, active = false, history = false;
        float result = -1f;
        String stamp = null, values = null;
        for (String line : dump.split("\\r?\\n")) {
            if (line.startsWith("Historical Thread Log") || line.startsWith("Input thread")) break;
            if (line.startsWith("Output thread")) {
                if (output && active) result = Math.max(result, level(stamp, values, wallNowMs));
                output = true; active = false; history = false; stamp = null; values = null;
                continue;
            }
            if (!output) continue;
            if (line.trim().equals("Standby: no")) active = true;
            if (line.trim().startsWith("Signal power history:")) { history = true; continue; }
            if (!history) continue;
            Matcher row = ROW.matcher(line);
            if (!row.matches()) { if (!line.trim().isEmpty()) history = false; continue; }
            stamp = row.group(1);
            values = row.group(2);
        }
        if (output && active) result = Math.max(result, level(stamp, values, wallNowMs));
        return result;
    }

    private static float level(String stamp, String row, long wallNowMs) {
        // Historical rows can be numerous. Parse dates only for each active output's final row.
        if (stamp == null || row == null || !fresh(stamp, wallNowMs)) return -1f;
        String values = row.split("\\]", 2)[0].replace("[", "").trim();
        if (values.isEmpty()) return -1f;
        String[] tokens = values.split("\\s+");
        try {
            float db = Float.parseFloat(tokens[tokens.length - 1]);
            if (!Float.isFinite(db) || db < -120f || db > 0f) return -1f;
            return db <= -60f ? 0f
                    : (float) Math.min(1.0, Math.sqrt(Math.pow(10.0, db / 20.0) * 1.5));
        } catch (NumberFormatException ignored) { return -1f; }
    }

    private static boolean fresh(String timestamp, long now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        int year = calendar.get(Calendar.YEAR);
        SimpleDateFormat format = new SimpleDateFormat("yyyy MM-dd HH:mm:ss.SSS", Locale.US);
        format.setLenient(false);
        for (int candidate = year - 1; candidate <= year + 1; candidate++) {
            try {
                long age = now - format.parse(candidate + " " + timestamp).getTime();
                if (age >= -100L && age <= 1000L) return true;
            } catch (java.text.ParseException ignored) { }
        }
        return false;
    }
    private DirectMusicLevels() { }
}
