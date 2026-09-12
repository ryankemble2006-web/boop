package com.boop.shieldhome;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record StartupRestoreRecord(
        String packageName,
        String originalEnabledState,
        String originalRunInBackground,
        String originalRunAnyInBackground,
        Set<StartupRecoveryPolicy.ManagedAction> managedActions,
        String lastAppliedEnabledState,
        String lastAppliedRunInBackground,
        String lastAppliedRunAnyInBackground,
        long firstChangedAtMillis) {

    private static final Pattern PACKAGE = Pattern.compile("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+");

    public StartupRestoreRecord {
        if (packageName == null || !PACKAGE.matcher(packageName).matches()) throw new IllegalArgumentException("packageName");
        originalEnabledState = safe(originalEnabledState, "unknown");
        originalRunInBackground = safe(originalRunInBackground, "unknown");
        originalRunAnyInBackground = safe(originalRunAnyInBackground, "unknown");
        lastAppliedEnabledState = safe(lastAppliedEnabledState, originalEnabledState);
        lastAppliedRunInBackground = safe(lastAppliedRunInBackground, originalRunInBackground);
        lastAppliedRunAnyInBackground = safe(lastAppliedRunAnyInBackground, originalRunAnyInBackground);
        managedActions = managedActions == null ? Set.of() : Set.copyOf(managedActions);
        if (firstChangedAtMillis < 0) throw new IllegalArgumentException("firstChangedAtMillis");
    }
    public static StartupRestoreRecord fromBaseline(StartupPackageState baseline, long now) {
        return new StartupRestoreRecord(
                baseline.packageName(), baseline.enabledState(), baseline.runInBackgroundMode(),
                baseline.runAnyInBackgroundMode(), Set.of(), baseline.enabledState(),
                baseline.runInBackgroundMode(), baseline.runAnyInBackgroundMode(), now);
    }

    public StartupRestoreRecord withManagedActions(
            Set<StartupRecoveryPolicy.ManagedAction> actions, StartupPackageState applied) {
        return new StartupRestoreRecord(
                packageName, originalEnabledState, originalRunInBackground, originalRunAnyInBackground,
                actions, applied.enabledState(), applied.runInBackgroundMode(),
                applied.runAnyInBackgroundMode(), firstChangedAtMillis);
    }

    public boolean drifted(StartupPackageState current) {
        return !lastAppliedEnabledState.equals(current.enabledState())
                || !lastAppliedRunInBackground.equals(current.runInBackgroundMode())
                || !lastAppliedRunAnyInBackground.equals(current.runAnyInBackgroundMode());
    }

    public String encode() {
        return "{\"v\":1,\"package\":\"" + esc(packageName)
                + "\",\"originalEnabled\":\"" + esc(originalEnabledState)
                + "\",\"originalRun\":\"" + esc(originalRunInBackground)
                + "\",\"originalRunAny\":\"" + esc(originalRunAnyInBackground)
                + "\",\"actions\":\"" + esc(actionString(managedActions))
                + "\",\"lastEnabled\":\"" + esc(lastAppliedEnabledState)
                + "\",\"lastRun\":\"" + esc(lastAppliedRunInBackground)
                + "\",\"lastRunAny\":\"" + esc(lastAppliedRunAnyInBackground)
                + "\",\"firstChanged\":" + firstChangedAtMillis + "}";
    }
    public static StartupRestoreRecord decode(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            if (!raw.contains("\"v\":1")) return null;
            String pkg = extract(raw, "package");
            String originalEnabled = extract(raw, "originalEnabled");
            String originalRun = extract(raw, "originalRun");
            String originalRunAny = extract(raw, "originalRunAny");
            String actionsRaw = extract(raw, "actions");
            String lastEnabled = extract(raw, "lastEnabled");
            String lastRun = extract(raw, "lastRun");
            String lastRunAny = extract(raw, "lastRunAny");
            long firstChanged = Long.parseLong(extractNumber(raw, "firstChanged"));
            return new StartupRestoreRecord(pkg, originalEnabled, originalRun, originalRunAny,
                    parseActions(actionsRaw), lastEnabled, lastRun, lastRunAny, firstChanged);
        } catch (RuntimeException failure) {
            return null;
        }
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String actionString(Set<StartupRecoveryPolicy.ManagedAction> actions) {
        return actions.stream().map(Enum::name).sorted().reduce((a,b) -> a + "," + b).orElse("");
    }

    private static Set<StartupRecoveryPolicy.ManagedAction> parseActions(String raw) {
        EnumSet<StartupRecoveryPolicy.ManagedAction> out = EnumSet.noneOf(StartupRecoveryPolicy.ManagedAction.class);
        if (raw == null || raw.isBlank()) return out;
        for (String item : raw.split(",")) out.add(StartupRecoveryPolicy.ManagedAction.valueOf(item));
        return out;
    }
    private static String extract(String raw, String key) {
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\":\\\"((?:\\\\.|[^\\\"])*)\\\"");
        Matcher m = p.matcher(raw);
        if (!m.find()) throw new IllegalArgumentException("missing " + key);
        return unesc(m.group(1));
    }

    private static String extractNumber(String raw, String key) {
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\":([0-9]+)");
        Matcher m = p.matcher(raw);
        if (!m.find()) throw new IllegalArgumentException("missing " + key);
        return m.group(1);
    }

    private static String esc(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String unesc(String value) {
        StringBuilder out = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (escaped) {
                out.append(c == 'n' ? '\n' : c == 'r' ? '\r' : c);
                escaped = false;
            } else if (c == '\\') escaped = true;
            else out.append(c);
        }
        if (escaped) out.append('\\');
        return out.toString();
    }
}
