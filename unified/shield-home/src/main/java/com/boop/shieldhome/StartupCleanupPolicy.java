package com.boop.shieldhome;

import java.util.List;
import java.util.Locale;

/** Pure safety policy for BOOP's bounded post-boot cleanup. */
public final class StartupCleanupPolicy {
    public static final int MAX_TARGETS = 5;
    public static final String STOCK_LAUNCHER = "com.google.android.tvlauncher";
    private static final List<String> PROTECTED_PREFIXES = List.of(
            "com.boop.", "com.android.", "com.nvidia.",
            "com.google.android.gms", "com.google.android.gsf");
    private static final List<String> WARM_PATH = List.of(
            "com.netflix.ninja", "com.plexapp.android", "deezer.android.app");

    private StartupCleanupPolicy() { }

    public static boolean validPackage(String value) {
        if (value == null || value.length() >= 180
                || !value.matches("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+")) return false;
        String lower = value.toLowerCase(Locale.ROOT);
        for (String prefix : PROTECTED_PREFIXES) {
            if (lower.startsWith(prefix)) return false;
        }
        return true;
    }

    public static boolean eligible(String packageName, boolean systemApp) {
        return eligibleForPrevention(packageName, systemApp);
    }

    public static boolean eligibleForPrevention(String packageName, boolean systemApp) {
        if (systemApp || !validPackage(packageName)) return false;
        String lower = packageName.toLowerCase(Locale.ROOT);
        return WARM_PATH.stream().noneMatch(lower::equals);
    }

    public static boolean eligibleForCleanStart(String packageName, boolean systemApp) {
        if (STOCK_LAUNCHER.equals(packageName)) return true;
        return eligibleForPrevention(packageName, systemApp);
    }

    public static boolean canAdd(int currentCount) {
        return currentCount >= 0 && currentCount < MAX_TARGETS;
    }

    public static String forceStopCommand(String packageName) {
        if (!validPackage(packageName)) throw new IllegalArgumentException("Invalid or protected package");
        return "am force-stop --user current '" + packageName + "'";
    }

    public static String currentUserCommand() { return "cmd activity get-current-user"; }
    public static String processSnapshotCommand() { return "ps -A -o PID,NAME"; }
    public static String resumedActivityCommand() {
        return "dumpsys activity activities | grep -m2 -E 'mResumedActivity:|topResumedActivity='";
    }
    public static String userStateCommand(String packageName, int userId) {
        if (userId < 0) throw new IllegalArgumentException("Invalid user id");
        if (!validPackage(packageName)) throw new IllegalArgumentException("Invalid or protected package");
        return "dumpsys package '" + packageName + "' | grep -A1 -m1 'User " + userId + ":'";
    }

    public static Integer parseCurrentUser(String output) {
        try { int value = Integer.parseInt(output.trim()); return value >= 0 ? value : null; }
        catch (Exception ignored) { return null; }
    }

    public static List<String> packageProcesses(String packageName, String snapshot) {
        if (!validPackage(packageName)) throw new IllegalArgumentException("Invalid or protected package");
        return snapshot.lines().map(String::trim).filter(s -> !s.isEmpty()).map(s -> s.split("\\s+"))
                .filter(parts -> parts.length >= 2 && parts[0].matches("\\d+"))
                .map(parts -> parts[parts.length - 1])
                .filter(name -> name.equals(packageName) || name.startsWith(packageName + ":")).toList();
    }

    public static Boolean parseStopped(String output) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\bstopped=(true|false)\\b").matcher(output);
        return m.find() ? Boolean.valueOf(m.group(1)) : null;
    }

    public static String parseEnabled(String output) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\benabled=([0-4])\\b").matcher(output);
        if (!m.find()) return null;
        return switch (m.group(1)) {
            case "0" -> "default"; case "1" -> "enabled"; case "2" -> "disabled";
            case "3" -> "disabled-user"; case "4" -> "disabled-until-used"; default -> null;
        };
    }

    public static String parseResumedPackage(String output) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "(?:mResumedActivity:|topResumedActivity=)[^\\n]*?\\bu\\d+\\s+([A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z0-9_]+)+)/")
                .matcher(output);
        if (!m.find()) return null;
        String value = m.group(1);
        return validPackage(value) ? value : null;
    }

    public static boolean verifiedStopped(String packageName, String processSnapshot, String userState) {
        Boolean stopped = parseStopped(userState);
        String enabled = parseEnabled(userState);
        return packageProcesses(packageName, processSnapshot).isEmpty()
                && Boolean.TRUE.equals(stopped)
                && enabled != null
                && !List.of("disabled", "disabled-user", "disabled-until-used").contains(enabled);
    }
}
