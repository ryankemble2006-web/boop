package com.boop.shieldhome;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StartupPackageCommands {
    private static final Pattern ENABLED = Pattern.compile("\\benabled=([0-4])\\b");
    private static final Pattern STOPPED = Pattern.compile("\\bstopped=(true|false)\\b");
    private static final Set<String> APP_OPS = Set.of("RUN_IN_BACKGROUND", "RUN_ANY_IN_BACKGROUND");
    private static final Set<String> APP_OP_MODES = Set.of("allow", "ignore", "deny", "default");

    private StartupPackageCommands() { }

    public static String disable(String packageName) {
        return "pm disable-user --user current '" + requirePackage(packageName) + "'";
    }

    public static String disablePlain(String packageName) {
        return "pm disable --user current '" + requirePackage(packageName) + "'";
    }

    public static String enable(String packageName) {
        return "pm enable --user current '" + requirePackage(packageName) + "'";
    }

    public static String resetEnabled(String packageName) {
        return "pm default-state --user current '" + requirePackage(packageName) + "'";
    }

    public static String disableUntilUsed(String packageName) {
        return "pm disable-until-used --user current '" + requirePackage(packageName) + "'";
    }

    public static String forceStop(String packageName) {
        return "am force-stop --user current '" + requirePackage(packageName) + "'";
    }

    public static String userState(String packageName, int userId) {
        if (userId < 0) throw new IllegalArgumentException("Invalid user id");
        return "dumpsys package '" + requirePackage(packageName)
                + "' | grep -A1 -m1 'User " + userId + ":'";
    }

    public static String processSnapshot() {
        return "ps -A -o PID,NAME";
    }

    public static String currentUser() {
        return "cmd activity get-current-user";
    }

    public static String queryAppOp(String packageName, String op) {
        if (!APP_OPS.contains(op)) throw new IllegalArgumentException("Unsupported app-op");
        return "cmd appops get " + requirePackage(packageName) + " " + op;
    }

    public static String setAppOp(String packageName, String op, String mode) {
        if (!APP_OPS.contains(op)) throw new IllegalArgumentException("Unsupported app-op");
        if (!APP_OP_MODES.contains(mode)) throw new IllegalArgumentException("Unsupported app-op mode");
        return "cmd appops set " + requirePackage(packageName) + " " + op + " " + mode;
    }

    public static String parseEnabled(String output) {
        Matcher matcher = ENABLED.matcher(output == null ? "" : output);
        if (!matcher.find()) return null;
        return switch (matcher.group(1)) {
            case "0" -> "default";
            case "1" -> "enabled";
            case "2" -> "disabled";
            case "3" -> "disabled-user";
            case "4" -> "disabled-until-used";
            default -> null;
        };
    }

    public static String parseResumedPackage(String output) {
        if (output == null) return null;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(
                "(?:mResumedActivity:|topResumedActivity=)[^\\n]*?\\bu\\d+\\s+([A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z0-9_]+)+)/")
                .matcher(output);
        return matcher.find() ? matcher.group(1) : null;
    }
    public static Boolean parseStopped(String output) {
        Matcher matcher = STOPPED.matcher(output == null ? "" : output);
        return matcher.find() ? Boolean.valueOf(matcher.group(1)) : null;
    }

    public static List<String> packageProcesses(String packageName, String snapshot) {
        requirePackage(packageName);
        ArrayList<String> out = new ArrayList<>();
        if (snapshot == null) return out;
        for (String line : snapshot.lines().toList()) {
            String value = line.trim();
            if (value.isEmpty()) continue;
            String[] fields = value.split("\\s+");
            if (fields.length < 2 || !fields[0].matches("\\d+")) continue;
            String name = fields[fields.length - 1];
            if (name.equals(packageName) || name.startsWith(packageName + ":")) out.add(name);
        }
        return List.copyOf(out);
    }

    public static Integer parseCurrentUser(String output) {
        try {
            int value = Integer.parseInt(output == null ? "" : output.trim());
            return value >= 0 ? value : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static boolean verifiedStopped(String packageName, String processSnapshot, String userState) {
        Boolean stopped = parseStopped(userState);
        return packageProcesses(packageName, processSnapshot).isEmpty() && Boolean.TRUE.equals(stopped);
    }

    private static String requirePackage(String packageName) {
        if (!StartupPackageController.validPackageName(packageName))
            throw new IllegalArgumentException("Invalid package name");
        return packageName;
    }
}
