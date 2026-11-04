package com.boop.shieldhome;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StartupPreventionPolicy {
    private static final List<String> ALLOWED_MODES = List.of("allow", "ignore", "deny", "default");
    private static final List<String> WARM_PATH = List.of("com.netflix.ninja", "com.plexapp.android", "deezer.android.app");

    private StartupPreventionPolicy() { }

    public static boolean eligible(String packageName, boolean systemApp) {
        if (systemApp || !StartupCleanupPolicy.validPackage(packageName)) return false;
        String lower = packageName.toLowerCase(Locale.ROOT);
        return WARM_PATH.stream().noneMatch(lower::equals);
    }

    public static String queryCommand(String packageName, String op) {
        requirePackage(packageName);
        if (!op.equals("RUN_IN_BACKGROUND") && !op.equals("RUN_ANY_IN_BACKGROUND"))
            throw new IllegalArgumentException("Unsupported app-op");
        return "cmd appops get " + packageName + " " + op;
    }

    public static List<String> blockCommands(String packageName) {
        requirePackage(packageName);
        return List.of("cmd appops set " + packageName + " RUN_IN_BACKGROUND ignore",
                "cmd appops set " + packageName + " RUN_ANY_IN_BACKGROUND ignore");
    }
    public static List<String> restoreCommands(String packageName, String runInBackground, String runAnyInBackground) {
        requirePackage(packageName);
        String first = requireMode(runInBackground);
        String second = requireMode(runAnyInBackground);
        return List.of("cmd appops set " + packageName + " RUN_IN_BACKGROUND " + first,
                "cmd appops set " + packageName + " RUN_ANY_IN_BACKGROUND " + second);
    }

    public static String parseMode(String output) {
        if (output == null) return null;
        Matcher explicit = Pattern.compile("(?:RUN_IN_BACKGROUND|RUN_ANY_IN_BACKGROUND):\\s*(allow|ignore|deny|default|foreground)\\b")
                .matcher(output);
        if (explicit.find()) return explicit.group(1);
        if (output.contains("No operations.")) return "default";
        Matcher fallback = Pattern.compile("Default mode:\\s*(allow|ignore|deny|default|foreground)\\b").matcher(output);
        return fallback.find() ? fallback.group(1) : null;
    }

    private static String requireMode(String mode) {
        if (mode == null) throw new IllegalArgumentException("Missing app-op mode");
        String lower = mode.toLowerCase(Locale.ROOT);
        if (!ALLOWED_MODES.contains(lower)) throw new IllegalArgumentException("Unsupported app-op mode");
        return lower;
    }

    private static void requirePackage(String packageName) {
        if (!StartupCleanupPolicy.validPackage(packageName))
            throw new IllegalArgumentException("Invalid or protected package");
    }
}
