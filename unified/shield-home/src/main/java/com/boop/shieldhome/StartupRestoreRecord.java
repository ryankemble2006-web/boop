package com.boop.shieldhome;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Private, versioned receipt. Only exact known state values can become a baseline. */
public record StartupRestoreRecord(
        String packageName, String originalEnabledState,
        String originalRunInBackground, String originalRunAnyInBackground,
        Set<StartupRecoveryPolicy.ManagedAction> managedActions,
        String lastAppliedEnabledState, String lastAppliedRunInBackground,
        String lastAppliedRunAnyInBackground, long firstChangedAtMillis,
        boolean originalBootClean, boolean lastAppliedBootClean) {

    public StartupRestoreRecord(String pkg, String enabled, String run, String any,
            Set<StartupRecoveryPolicy.ManagedAction> actions, String lastEnabled,
            String lastRun, String lastAny, long when) {
        this(pkg, enabled, run, any, actions, lastEnabled, lastRun, lastAny, when,
                false, actions != null && actions.contains(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN));
    }

    public StartupRestoreRecord {
        if (packageName == null || packageName.length() >= 180
                || !packageName.matches("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+"))
            throw new IllegalArgumentException("Invalid restore package");
        if (!validEnabled(originalEnabledState) || !validEnabled(lastAppliedEnabledState)
                || !validMode(originalRunInBackground) || !validMode(originalRunAnyInBackground)
                || !validMode(lastAppliedRunInBackground) || !validMode(lastAppliedRunAnyInBackground))
            throw new IllegalArgumentException("Cannot save an unknown package state");
        if (firstChangedAtMillis < 0) throw new IllegalArgumentException("Invalid restore time");
        managedActions = managedActions == null ? Set.of() : Set.copyOf(managedActions);
    }

    public static boolean validEnabled(String value) {
        return value != null && Set.of("default", "enabled", "disabled", "disabled-user",
                "disabled-until-used", "manifest-disabled").contains(value);
    }
    public static boolean validMode(String value) {
        return value != null && Set.of("allow", "ignore", "deny", "default", "foreground").contains(value);
    }
    public static StartupRestoreRecord fromBaseline(StartupPackageState state, long now) {
        boolean boot = state.managedActions().contains(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN);
        return new StartupRestoreRecord(state.packageName(), state.enabledState(),
                state.runInBackgroundMode(), state.runAnyInBackgroundMode(), Set.of(),
                state.enabledState(), state.runInBackgroundMode(), state.runAnyInBackgroundMode(), now, boot, boot);
    }
    public StartupRestoreRecord withManagedActions(Set<StartupRecoveryPolicy.ManagedAction> actions,
                                                  StartupPackageState applied) {
        if (!packageName.equals(applied.packageName())) throw new IllegalArgumentException("Restore target changed");
        return new StartupRestoreRecord(packageName, originalEnabledState, originalRunInBackground,
                originalRunAnyInBackground, actions, applied.enabledState(), applied.runInBackgroundMode(),
                applied.runAnyInBackgroundMode(), firstChangedAtMillis, originalBootClean,
                applied.managedActions().contains(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN));
    }
    public boolean drifted(StartupPackageState current) {
        return !packageName.equals(current.packageName())
                || !lastAppliedEnabledState.equals(current.enabledState())
                || !lastAppliedRunInBackground.equals(current.runInBackgroundMode())
                || !lastAppliedRunAnyInBackground.equals(current.runAnyInBackgroundMode())
                || lastAppliedBootClean != current.managedActions().contains(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN);
    }
    public String encode() { return encode(false); }
    private String encode(boolean legacy) {
        String actions = managedActions.stream().map(Enum::name).sorted().reduce((a,b) -> a+","+b).orElse("");
        return "{\"v\":" + (legacy ? 1 : 2) + ",\"package\":\"" + packageName
                + "\",\"originalEnabled\":\"" + originalEnabledState
                + "\",\"originalRun\":\"" + originalRunInBackground
                + "\",\"originalRunAny\":\"" + originalRunAnyInBackground
                + "\",\"actions\":\"" + actions + "\",\"lastEnabled\":\"" + lastAppliedEnabledState
                + "\",\"lastRun\":\"" + lastAppliedRunInBackground
                + "\",\"lastRunAny\":\"" + lastAppliedRunAnyInBackground
                + "\",\"firstChanged\":" + firstChangedAtMillis
                + (legacy ? "" : ",\"originalBoot\":" + originalBootClean + ",\"lastBoot\":" + lastAppliedBootClean) + "}";
    }
    public static StartupRestoreRecord decode(String raw) {
        if (raw == null) return null;
        try {
            boolean legacy = raw.startsWith("{\"v\":1,");
            if (!legacy && !raw.startsWith("{\"v\":2,")) return null;
            EnumSet<StartupRecoveryPolicy.ManagedAction> actions = EnumSet.noneOf(StartupRecoveryPolicy.ManagedAction.class);
            String actionText = field(raw, "actions");
            if (!actionText.isEmpty()) for (String action : actionText.split(","))
                actions.add(StartupRecoveryPolicy.ManagedAction.valueOf(action));
            StartupRestoreRecord result = new StartupRestoreRecord(field(raw,"package"),
                    field(raw,"originalEnabled"), field(raw,"originalRun"), field(raw,"originalRunAny"),
                    actions, field(raw,"lastEnabled"), field(raw,"lastRun"), field(raw,"lastRunAny"),
                    Long.parseLong(scalar(raw,"firstChanged","[0-9]+")),
                    !legacy && Boolean.parseBoolean(scalar(raw,"originalBoot","true|false")),
                    legacy ? actions.contains(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN)
                           : Boolean.parseBoolean(scalar(raw,"lastBoot","true|false")));
            // Deliberately accept only the canonical private receipt format, not arbitrary JSON.
            return raw.equals(result.encode(legacy)) ? result : null;
        } catch (RuntimeException invalid) { return null; }
    }
    private static String field(String raw, String key) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\":\"([^\"]*)\"").matcher(raw);
        if (!m.find()) throw new IllegalArgumentException("Missing receipt field");
        return m.group(1);
    }
    private static String scalar(String raw, String key, String expression) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\":(" + expression + ")(?=[,}])").matcher(raw);
        if (!m.find()) throw new IllegalArgumentException("Missing receipt field");
        return m.group(1);
    }
}
