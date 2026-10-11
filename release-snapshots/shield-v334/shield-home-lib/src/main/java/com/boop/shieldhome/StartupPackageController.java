package com.boop.shieldhome;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

/** Serial, verified package actions. The original receipt is durable BEFORE any write. */
public final class StartupPackageController {
    private static final Pattern PACKAGE = Pattern.compile("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+");
    public interface Bridge {
        StartupPackageState probe(String packageName) throws Exception;
        void setEnabledState(String packageName, String enabledState) throws Exception;
        void forceStop(String packageName) throws Exception;
        void setBackgroundModes(String packageName, String run, String any) throws Exception;
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
    public StartupPackageController(Bridge bridge, BootStore bootStore, StartupRestoreStore store,
                                    StartupRecoveryPolicy.RecoveryCapabilities capabilities) {
        this.bridge=bridge; this.bootStore=bootStore; this.restoreStore=store; this.capabilities=capabilities;
    }
    public static boolean validPackageName(String pkg) {
        return pkg != null && pkg.length()<180 && (pkg.equals("android") || PACKAGE.matcher(pkg).matches());
    }
    public Result disable(String pkg) {
        return change(pkg, StartupRecoveryPolicy.ManagedAction.DISABLED, true, true,
                () -> bridge.setEnabledState(pkg,"disabled-user"),
                after -> "disabled-user".equals(after.enabledState()), "App disabled. Restore is ready.");
    }
    public Result reenable(String pkg) {
        return change(pkg, StartupRecoveryPolicy.ManagedAction.DISABLED, false, false,
                () -> bridge.setEnabledState(pkg,"enabled"),
                after -> "enabled".equals(after.enabledState()), "App re-enabled.");
    }
    public Result forceStop(String pkg) {
        try {
            StartupPackageState before=observe(pkg); requireManageable(before);
            bridge.forceStop(pkg); // The bridge verifies stopped state and process absence.
            return new Result(true,"App stopped.",before);
        } catch(Exception failure) { return failed(pkg,failure); }
    }
    public Result setBootClean(String pkg, boolean enabled) {
        return change(pkg,StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN,enabled,enabled,
                () -> { if(!bootStore.setTarget(pkg,enabled)) throw new IllegalStateException("Could not save boot cleanup."); },
                after -> bootStore.contains(pkg)==enabled,
                enabled ? "Close after boot: ON" : "Close after boot: OFF");
    }
    public Result setBackgroundBlock(String pkg, boolean enabled) {
        if(enabled) return change(pkg,StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK,true,true,
                () -> bridge.setBackgroundModes(pkg,"ignore","ignore"),
                after -> "ignore".equals(after.runInBackgroundMode()) && "ignore".equals(after.runAnyInBackgroundMode()),
                "Background start limited. Manual opening is still allowed.");
        try {
            StartupPackageState before=observe(pkg);
            StartupRestoreRecord record=restoreStore.record(pkg);
            if(record==null) return new Result(true,"No BOOP background rule to remove.",before);
            // A partial write can leave no managed flag. The saved baseline still repairs it.
            return change(pkg,StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK,false,true,
                    () -> bridge.setBackgroundModes(pkg,record.originalRunInBackground(),record.originalRunAnyInBackground()),
                    after -> record.originalRunInBackground().equals(after.runInBackgroundMode())
                            && record.originalRunAnyInBackground().equals(after.runAnyInBackgroundMode()),
                    "Background settings restored.");
        } catch(Exception failure) { return failed(pkg,failure); }
    }
    public Result restore(String pkg) {
        try {
            StartupPackageState before=observe(pkg);
            StartupRestoreRecord record=restoreStore.record(pkg);
            if(record==null) return new Result(false,"No saved BOOP change for this package.",before);
            requireManageable(before);
            // Restore the baseline even after a partial mutation or a failed receipt update.
            // Never rely on successful-action flags to decide which half of a write needs undoing.
            if(bootStore.contains(pkg)!=record.originalBootClean()
                    && !bootStore.setTarget(pkg,record.originalBootClean()))
                throw new IllegalStateException("Could not restore boot cleanup.");
            if(!record.originalRunInBackground().equals(before.runInBackgroundMode())
                    || !record.originalRunAnyInBackground().equals(before.runAnyInBackgroundMode()))
                bridge.setBackgroundModes(pkg,record.originalRunInBackground(),record.originalRunAnyInBackground());
            if(!enabledEquivalent(record.originalEnabledState(),before.enabledState()))
                bridge.setEnabledState(pkg,record.originalEnabledState());
            StartupPackageState after=observe(pkg);
            if(!enabledEquivalent(record.originalEnabledState(),after.enabledState())
                    || !record.originalRunInBackground().equals(after.runInBackgroundMode())
                    || !record.originalRunAnyInBackground().equals(after.runAnyInBackgroundMode())
                    || bootStore.contains(pkg)!=record.originalBootClean())
                throw new IllegalStateException("Restore is incomplete. The saved original is still available.");
            if(!restoreStore.remove(pkg)) throw new IllegalStateException("Restored, but the saved receipt could not be cleared.");
            return new Result(true,"Original state restored.",after);
        } catch(Exception failure) { return failed(pkg,failure); }
    }
    private interface Mutation { void run() throws Exception; }
    private interface Check { boolean matches(StartupPackageState state); }
    private Result change(String pkg, StartupRecoveryPolicy.ManagedAction action, boolean active,
                          boolean protectedCheck, Mutation mutation, Check check, String summary) {
        try {
            StartupPackageState before=observe(pkg);
            if(protectedCheck) requireManageable(before);
            restoreStore.captureIfAbsent(pkg,before);
            mutation.run();
            StartupPackageState after=observe(pkg);
            if(!check.matches(after)) throw new IllegalStateException("Android did not confirm the change. Restore is still available.");
            EnumSet<StartupRecoveryPolicy.ManagedAction> actions=EnumSet.noneOf(StartupRecoveryPolicy.ManagedAction.class);
            actions.addAll(restoreStore.record(pkg).managedActions());
            if(active) actions.add(action); else actions.remove(action);
            restoreStore.updateManagedActions(pkg,actions,after);
            return new Result(true,summary,after);
        } catch(Exception failure) { return failed(pkg,failure); }
    }
    private StartupPackageState observe(String pkg) throws Exception {
        if(!validPackageName(pkg)) throw new IllegalArgumentException("Invalid package name.");
        StartupPackageState state=bridge.probe(pkg);
        if(state==null || !pkg.equals(state.packageName())) throw new IllegalStateException("Package identity could not be verified.");
        if(!StartupRestoreRecord.validEnabled(state.enabledState())
                || !StartupRestoreRecord.validMode(state.runInBackgroundMode())
                || !StartupRestoreRecord.validMode(state.runAnyInBackgroundMode()))
            throw new IllegalStateException("The original package state could not be read. Nothing was changed.");
        EnumSet<StartupRecoveryPolicy.ManagedAction> flags=EnumSet.noneOf(StartupRecoveryPolicy.ManagedAction.class);
        flags.addAll(state.managedActions());
        if(bootStore.contains(pkg)) flags.add(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN);
        else flags.remove(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN);
        return new StartupPackageState(pkg,state.label(),state.systemApp(),state.launcher(),state.enabledState(),
                state.runInBackgroundMode(),state.runAnyInBackgroundMode(),flags);
    }
    private void requireManageable(StartupPackageState state) {
        var assessment=StartupRecoveryPolicy.assess(state,capabilities);
        if(assessment.protectedPackage()) throw new IllegalStateException(assessment.protectionReason());
    }
    private Result failed(String pkg, Exception failure) {
        StartupPackageState current=null;
        try { current=observe(pkg); } catch(Exception ignored) { }
        String message=failure.getMessage();
        if(message==null || message.trim().isEmpty()) message="The package could not be changed.";
        return new Result(false,message,current);
    }
    private static boolean enabledEquivalent(String expected,String actual) {
        return expected.equals(actual) || expected.equals("manifest-disabled") && actual.equals("default");
    }
}
