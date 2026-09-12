package com.boop.shieldhome;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

public final class StartupPackageController {
    private static final Pattern PACKAGE = Pattern.compile("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+");

    public interface Bridge {
        StartupPackageState probe(String packageName) throws Exception;
        void setEnabledState(String packageName, String enabledState) throws Exception;
        void forceStop(String packageName) throws Exception;
        void setBackgroundModes(String packageName, String runInBackground, String runAnyInBackground) throws Exception;
    }

    public interface BootStore {
        boolean setTarget(String packageName, boolean enabled);
        boolean contains(String packageName);
    }

    public record Result(boolean success, String summary, StartupPackageState current) { }

    private final Bridge bridge;
    private final BootStore bootStore;
    private final StartupRestoreStore restoreStore;
    private final StartupRecoveryPolicy.RecoveryCapabilities capabilities;

    public StartupPackageController(Bridge bridge, BootStore bootStore,
                                    StartupRestoreStore restoreStore,
                                    StartupRecoveryPolicy.RecoveryCapabilities capabilities) {
        this.bridge = bridge;
        this.bootStore = bootStore;
        this.restoreStore = restoreStore;
        this.capabilities = capabilities;
    }

    public static boolean validPackageName(String packageName) {
        return packageName != null && packageName.length() < 180 && PACKAGE.matcher(packageName).matches();
    }
    public Result disable(String packageName) {
        return persistent(packageName, StartupRecoveryPolicy.ManagedAction.DISABLED, current -> {
            bridge.setEnabledState(packageName, "disabled-user");
            StartupPackageState after = bridge.probe(packageName);
            if (!after.enabledState().equals("disabled-user") && !after.enabledState().equals("disabled")) {
                throw new IllegalStateException("Disable could not be verified.");
            }
            return after;
        }, true);
    }

    public Result reenable(String packageName) {
        if (!validPackageName(packageName)) return fail("Invalid package name.", null);
        try {
            StartupPackageState before = bridge.probe(packageName);
            StartupRecoveryPolicy.Assessment assessment = StartupRecoveryPolicy.assess(before, capabilities);
            if (assessment.protectedPackage()) return fail(assessment.protectionReason(), before);
            StartupRestoreRecord existing = restoreStore.record(packageName);
            if (existing == null) restoreStore.captureIfAbsent(packageName, before);
            bridge.setEnabledState(packageName, "enabled");
            StartupPackageState after = bridge.probe(packageName);
            if (!after.enabledState().equals("enabled")) throw new IllegalStateException("Re-enable could not be verified.");
            Set<StartupRecoveryPolicy.ManagedAction> actions = actionsWithout(packageName, StartupRecoveryPolicy.ManagedAction.DISABLED);
            restoreStore.updateManagedActions(packageName, actions, after);
            return ok("App enabled.", withActions(after, actions));
        } catch (Exception failure) {
            return fail(message(failure), safeProbe(packageName));
        }
    }

    public Result forceStop(String packageName) {
        if (!validPackageName(packageName)) return fail("Invalid package name.", null);
        try {
            StartupPackageState current = bridge.probe(packageName);
            StartupRecoveryPolicy.Assessment assessment = StartupRecoveryPolicy.assess(current, capabilities);
            if (assessment.protectedPackage()) return fail(assessment.protectionReason(), current);
            bridge.forceStop(packageName);
            return ok("App stopped.", current);
        } catch (Exception failure) {
            return fail(message(failure), safeProbe(packageName));
        }
    }
    public Result setBootClean(String packageName, boolean enabled) {
        if (!validPackageName(packageName)) return fail("Invalid package name.", null);
        try {
            StartupPackageState before = bridge.probe(packageName);
            StartupRecoveryPolicy.Assessment assessment = StartupRecoveryPolicy.assess(before, capabilities);
            if (assessment.protectedPackage()) return fail(assessment.protectionReason(), before);
            if (enabled) restoreStore.captureIfAbsent(packageName, before);
            if (!bootStore.setTarget(packageName, enabled)) return fail("Could not change boot cleanup.", before);
            Set<StartupRecoveryPolicy.ManagedAction> actions = enabled
                    ? actionsWith(packageName, StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN)
                    : actionsWithout(packageName, StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN);
            StartupRestoreRecord record = restoreStore.record(packageName);
            if (record != null) restoreStore.updateManagedActions(packageName, actions, before);
            return ok(enabled ? "Clean after boot: ON" : "Clean after boot: OFF", withActions(before, actions));
        } catch (Exception failure) {
            return fail(message(failure), safeProbe(packageName));
        }
    }

    public Result setBackgroundBlock(String packageName, boolean enabled) {
        if (!validPackageName(packageName)) return fail("Invalid package name.", null);
        try {
            StartupPackageState before = bridge.probe(packageName);
            StartupRecoveryPolicy.Assessment assessment = StartupRecoveryPolicy.assess(before, capabilities);
            if (assessment.protectedPackage()) return fail(assessment.protectionReason(), before);
            StartupRestoreRecord record = restoreStore.record(packageName);
            if (enabled) {
                if (record == null) record = restoreStore.captureIfAbsent(packageName, before);
                bridge.setBackgroundModes(packageName, "ignore", "ignore");
                StartupPackageState after = bridge.probe(packageName);
                if (!"ignore".equals(after.runInBackgroundMode()) || !"ignore".equals(after.runAnyInBackgroundMode()))
                    throw new IllegalStateException("Background block could not be verified.");
                Set<StartupRecoveryPolicy.ManagedAction> actions = actionsWith(packageName, StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK);
                restoreStore.updateManagedActions(packageName, actions, after);
                return ok("Prevent background start: ON", withActions(after, actions));
            }
            if (record == null || !record.managedActions().contains(StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK))
                return ok("Prevent background start: already OFF", before);
            bridge.setBackgroundModes(packageName, record.originalRunInBackground(), record.originalRunAnyInBackground());
            StartupPackageState after = bridge.probe(packageName);
            if (!record.originalRunInBackground().equals(after.runInBackgroundMode())
                    || !record.originalRunAnyInBackground().equals(after.runAnyInBackgroundMode()))
                throw new IllegalStateException("Background restore could not be verified.");
            Set<StartupRecoveryPolicy.ManagedAction> actions = actionsWithout(packageName, StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK);
            restoreStore.updateManagedActions(packageName, actions, after);
            return ok("Prevent background start: OFF", withActions(after, actions));
        } catch (Exception failure) {
            return fail(message(failure), safeProbe(packageName));
        }
    }
    public Result restore(String packageName) {
        if (!validPackageName(packageName)) return fail("Invalid package name.", null);
        StartupRestoreRecord record = restoreStore.record(packageName);
        if (record == null) return fail("No BOOP restore record for this package.", safeProbe(packageName));
        try {
            if (record.managedActions().contains(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN)
                    && !bootStore.setTarget(packageName, false)) {
                throw new IllegalStateException("Could not remove boot cleanup.");
            }
            if (record.managedActions().contains(StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK)) {
                bridge.setBackgroundModes(packageName,
                        record.originalRunInBackground(), record.originalRunAnyInBackground());
            }
            StartupPackageState beforeRestore = bridge.probe(packageName);
            if (!enabledEquivalent(record.originalEnabledState(), beforeRestore.enabledState())) {
                bridge.setEnabledState(packageName, record.originalEnabledState());
            }
            StartupPackageState after = bridge.probe(packageName);
            if (!enabledEquivalent(record.originalEnabledState(), after.enabledState()))
                throw new IllegalStateException("Enabled state restore could not be verified.");
            if (!record.originalRunInBackground().equals(after.runInBackgroundMode())
                    || !record.originalRunAnyInBackground().equals(after.runAnyInBackgroundMode()))
                throw new IllegalStateException("Background state restore could not be verified.");
            if (bootStore.contains(packageName)) throw new IllegalStateException("Boot cleanup restore could not be verified.");
            if (!restoreStore.remove(packageName)) throw new IllegalStateException("Restore worked but receipt could not be cleared.");
            return ok("Original state restored.", withActions(after, Set.of()));
        } catch (Exception failure) {
            return fail(message(failure), safeProbe(packageName));
        }
    }

    private interface Mutation { StartupPackageState apply(StartupPackageState current) throws Exception; }

    private Result persistent(String packageName, StartupRecoveryPolicy.ManagedAction action,
                              Mutation mutation, boolean requireManageable) {
        if (!validPackageName(packageName)) return fail("Invalid package name.", null);
        try {
            StartupPackageState before = bridge.probe(packageName);
            StartupRecoveryPolicy.Assessment assessment = StartupRecoveryPolicy.assess(before, capabilities);
            if (requireManageable && assessment.protectedPackage()) return fail(assessment.protectionReason(), before);
            restoreStore.captureIfAbsent(packageName, before);
            StartupPackageState after = mutation.apply(before);
            Set<StartupRecoveryPolicy.ManagedAction> actions = actionsWith(packageName, action);
            restoreStore.updateManagedActions(packageName, actions, after);
            return ok("Done.", withActions(after, actions));
        } catch (Exception failure) {
            return fail(message(failure), safeProbe(packageName));
        }
    }
    private Set<StartupRecoveryPolicy.ManagedAction> actionsWith(
            String packageName, StartupRecoveryPolicy.ManagedAction action) {
        EnumSet<StartupRecoveryPolicy.ManagedAction> actions = EnumSet.noneOf(StartupRecoveryPolicy.ManagedAction.class);
        StartupRestoreRecord record = restoreStore.record(packageName);
        if (record != null) actions.addAll(record.managedActions());
        actions.add(action);
        return actions;
    }

    private Set<StartupRecoveryPolicy.ManagedAction> actionsWithout(
            String packageName, StartupRecoveryPolicy.ManagedAction action) {
        EnumSet<StartupRecoveryPolicy.ManagedAction> actions = EnumSet.noneOf(StartupRecoveryPolicy.ManagedAction.class);
        StartupRestoreRecord record = restoreStore.record(packageName);
        if (record != null) actions.addAll(record.managedActions());
        actions.remove(action);
        return actions;
    }

    private StartupPackageState safeProbe(String packageName) {
        try { return bridge.probe(packageName); }
        catch (Exception ignored) { return null; }
    }

    private static StartupPackageState withActions(
            StartupPackageState state, Set<StartupRecoveryPolicy.ManagedAction> actions) {
        if (state == null) return null;
        return new StartupPackageState(state.packageName(), state.label(), state.systemApp(), state.launcher(),
                state.enabledState(), state.runInBackgroundMode(), state.runAnyInBackgroundMode(), actions);
    }

    private static boolean enabledEquivalent(String expected, String actual) {
        if (expected == null || actual == null) return false;
        if (expected.equals(actual)) return true;
        return expected.equals("manifest-disabled") && actual.equals("default");
    }

    private static String message(Throwable failure) {
        String value = failure.getMessage();
        return value == null || value.isBlank() ? failure.getClass().getSimpleName() : value;
    }

    private static Result ok(String summary, StartupPackageState current) {
        return new Result(true, summary, current);
    }

    private static Result fail(String summary, StartupPackageState current) {
        return new Result(false, summary, current);
    }
}
