package com.boop.shieldhome;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import com.boop.shieldturbo.power.AdbWire;
import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class StartupLocalBridge {
    private final Context context;
    private volatile AdbWire active;
    private volatile boolean cancelled;

    public StartupLocalBridge(Context context) { this.context = context.getApplicationContext(); }

    public boolean hasIdentity() {
        return new File(context.getNoBackupFilesDir(), "boop-unified-local-adb.key").isFile();
    }

    public void cancel() {
        cancelled = true;
        try { if (active != null) active.close(); } catch (Exception ignored) { }
    }

    public String authorize(Runnable approvalRequired) throws Exception {
        cancelled = false;
        return withAdb(true, approvalRequired, adb -> {
            checked(adb, "echo BOOP_LOCAL_READY");
            return "Local cleanup link ready.";
        });
    }

    public String run(Collection<String> requested, boolean allowApproval, Runnable approvalRequired) throws Exception {
        cancelled = false;
        return withAdb(allowApproval, approvalRequired, adb -> {
            String resumed = StartupPackageCommands.parseResumedPackage(
                    checked(adb, StartupCleanupPolicy.resumedActivityCommand()));
            Integer userId = StartupPackageCommands.parseCurrentUser(
                    checked(adb, StartupPackageCommands.currentUser()));
            if (userId == null) throw new IOException("Could not verify the current Shield user.");

            List<String> details = new ArrayList<>();
            int stopped = 0, skipped = 0, failed = 0;
            for (String packageName : requested) {
                if (!manageableInstalled(packageName)) {
                    details.add(packageName + ": no longer eligible");
                    failed++;
                    continue;
                }
                if (packageName.equals(resumed)) {
                    details.add(packageName + ": left open");
                    skipped++;
                    continue;
                }
                try {
                    checked(adb, StartupPackageCommands.forceStop(packageName));
                    String processes = checked(adb, StartupPackageCommands.processSnapshot());
                    String state = checked(adb, StartupPackageCommands.userState(packageName, userId));
                    if (!StartupPackageCommands.verifiedStopped(packageName, processes, state))
                        throw new IOException("stop could not be verified");
                    stopped++;
                } catch (Exception failure) {
                    failed++;
                    details.add(packageName + ": " + compact(failure));
                }
            }
            String summary = "Clean Start: " + stopped + " stopped";
            if (skipped > 0) summary += ", " + skipped + " left open";
            if (failed > 0) summary += ", " + failed + " not changed";
            if (!details.isEmpty()) summary += ". " + String.join("; ", details);
            return summary;
        });
    }

    public String setPrevention(String packageName, boolean enabled, boolean allowApproval, Runnable approvalRequired) throws Exception {
        cancelled = false;
        if (!eligibleInstalledForPrevention(packageName)) throw new IOException("App is not eligible for startup prevention.");
        StartupPreventionStore store = new StartupPreventionStore(context);
        return withAdb(allowApproval, approvalRequired, adb -> {
            StartupPreventionRecord saved = store.record(packageName);
            if (enabled) {
                if (saved == null) {
                    String first = StartupPreventionPolicy.parseMode(checked(adb,
                            StartupPreventionPolicy.queryCommand(packageName, "RUN_IN_BACKGROUND")));
                    String second = StartupPreventionPolicy.parseMode(checked(adb,
                            StartupPreventionPolicy.queryCommand(packageName, "RUN_ANY_IN_BACKGROUND")));
                    if (first == null || second == null) throw new IOException("Could not capture the original startup state.");
                    saved = new StartupPreventionRecord(packageName, first, second, false);
                    if (!store.rememberOriginal(saved)) throw new IOException("Could not save the original startup state.");
                }
                if (!store.markManaged(packageName)) throw new IOException("Could not arm the saved Undo record.");
                for (String command : StartupPreventionPolicy.blockCommands(packageName)) checked(adb, command);
                String firstNow = StartupPreventionPolicy.parseMode(checked(adb, StartupPreventionPolicy.queryCommand(packageName, "RUN_IN_BACKGROUND")));
                String secondNow = StartupPreventionPolicy.parseMode(checked(adb, StartupPreventionPolicy.queryCommand(packageName, "RUN_ANY_IN_BACKGROUND")));
                if (!"ignore".equals(firstNow) || !"ignore".equals(secondNow)) throw new IOException("Startup prevention could not be verified; Undo remains available.");
                return "Prevent background start: ON";
            }
            if (saved == null || !saved.managed()) return "Prevent background start: already OFF";
            for (String command : StartupPreventionPolicy.restoreCommands(packageName, saved.originalRunInBackground(), saved.originalRunAnyInBackground())) checked(adb, command);
            String firstNow = StartupPreventionPolicy.parseMode(checked(adb, StartupPreventionPolicy.queryCommand(packageName, "RUN_IN_BACKGROUND")));
            String secondNow = StartupPreventionPolicy.parseMode(checked(adb, StartupPreventionPolicy.queryCommand(packageName, "RUN_ANY_IN_BACKGROUND")));
            if (!saved.originalRunInBackground().equals(firstNow) || !saved.originalRunAnyInBackground().equals(secondNow))
                throw new IOException("Undo could not be verified.");
            if (!store.remove(packageName)) throw new IOException("Undo worked but BOOP could not clear its saved record.");
            return "Prevent background start: OFF - original state restored";
        });
    }

    public PackageSession openPackageSession(boolean allowNewApproval, Runnable approvalRequired) throws Exception {
        cancelled = false;
        if (Thread.currentThread().isInterrupted()) throw new IOException("Cancelled");
        File keyFile = new File(context.getNoBackupFilesDir(), "boop-unified-local-adb.key");
        if (!allowNewApproval && !keyFile.exists())
            throw new AdbWire.AdbApprovalRequiredException("Local cleanup has not been approved yet.");
        KeyPair identity = AdbWire.identity(keyFile);
        AdbWire adb = new AdbWire();
        try {
            active = adb;
            adb.connect(5555, identity, 45_000,
                    approvalRequired == null ? () -> {} : approvalRequired, allowNewApproval);
            String uid = checked(adb, "id -u").trim();
            if (!uid.equals("2000") && !uid.equals("0"))
                throw new IOException("Local connection is not an ADB shell.");
            return new PackageSession(adb);
        } catch (Exception failure) {
            try { adb.close(); } catch (Exception ignored) { }
            active = null;
            throw failure;
        }
    }

    public final class PackageSession implements StartupPackageController.Bridge, AutoCloseable {
        private final AdbWire adb;
        private boolean closed;

        private PackageSession(AdbWire adb) { this.adb = adb; }

        @Override public StartupPackageState probe(String packageName) throws Exception {
            requireOpen();
            if (!StartupPackageController.validPackageName(packageName))
                throw new IOException("Invalid package name.");
            Integer userId = StartupPackageCommands.parseCurrentUser(
                    checked(adb, StartupPackageCommands.currentUser()));
            if (userId == null) throw new IOException("Could not verify the current Shield user.");
            String userState = checked(adb, StartupPackageCommands.userState(packageName, userId));
            String enabledState = StartupPackageCommands.parseEnabled(userState);
            if (enabledState == null) throw new IOException("Could not read package enabled state.");
            String first = StartupPreventionPolicy.parseMode(checked(adb,
                    StartupPackageCommands.queryAppOp(packageName, "RUN_IN_BACKGROUND")));
            String second = StartupPreventionPolicy.parseMode(checked(adb,
                    StartupPackageCommands.queryAppOp(packageName, "RUN_ANY_IN_BACKGROUND")));
            if (first == null || second == null) throw new IOException("Could not read background state.");

            ApplicationInfo app;
            try {
                app = context.getPackageManager().getApplicationInfo(
                        packageName, PackageManager.MATCH_DISABLED_COMPONENTS);
            } catch (PackageManager.NameNotFoundException failure) {
                throw new IOException("Package is no longer installed.", failure);
            }
            if (enabledState.equals("default") && !app.enabled) enabledState = "manifest-disabled";
            boolean system = (app.flags & (ApplicationInfo.FLAG_SYSTEM
                    | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
            CharSequence rawLabel = context.getPackageManager().getApplicationLabel(app);
            String label = rawLabel == null || rawLabel.toString().isBlank()
                    ? packageName : rawLabel.toString().trim();
            StartupRestoreRecord restore = new StartupRestoreStore(
                    new AndroidStartupRestoreBackend(context), System::currentTimeMillis).record(packageName);
            java.util.Set<StartupRecoveryPolicy.ManagedAction> actions = restore == null
                    ? java.util.Set.of() : restore.managedActions();
            return new StartupPackageState(packageName, label, system,
                    isLauncherPackage(packageName), enabledState, first, second, actions);
        }

        public List<StartupPackageState> inventory() throws Exception {
            requireOpen();
            String all = checked(adb, "pm list packages -u");
            String system = checked(adb, "pm list packages -s -u");
            String disabled = checked(adb, "pm list packages -d -u");
            String launcher = checked(adb, "cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.LEANBACK_LAUNCHER")
                    + "\n" + checked(adb, "cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.LAUNCHER")
                    + "\n" + checked(adb, "cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME");
            List<StartupPackageState> base = StartupPackageInventory.merge(
                    all, system, disabled, launcher, this::visibleLabel);
            StartupRestoreStore restores = new StartupRestoreStore(
                    new AndroidStartupRestoreBackend(context), System::currentTimeMillis);
            boolean migrated = new AndroidStartupManagerMigrationMarker(context).migrated();
            java.util.Set<String> boot = migrated ? java.util.Set.of() : new StartupCleanupStore(context).targets();
            java.util.Set<String> background = migrated ? java.util.Set.of() : new StartupPreventionStore(context).managedPackages();
            ArrayList<StartupPackageState> out = new ArrayList<>();
            for (StartupPackageState row : base) {
                java.util.EnumSet<StartupRecoveryPolicy.ManagedAction> actions =
                        java.util.EnumSet.noneOf(StartupRecoveryPolicy.ManagedAction.class);
                StartupRestoreRecord record = restores.record(row.packageName());
                if (record != null) actions.addAll(record.managedActions());
                if (boot.contains(row.packageName())) actions.add(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN);
                if (background.contains(row.packageName())) actions.add(StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK);
                out.add(new StartupPackageState(row.packageName(), row.label(), row.systemApp(), row.launcher(),
                        row.enabledState(), row.runInBackgroundMode(), row.runAnyInBackgroundMode(), actions));
            }
            return List.copyOf(out);
        }

        private String visibleLabel(String packageName) {
            try {
                ApplicationInfo app = context.getPackageManager().getApplicationInfo(
                        packageName, PackageManager.MATCH_DISABLED_COMPONENTS);
                CharSequence raw = context.getPackageManager().getApplicationLabel(app);
                return raw == null ? null : raw.toString().trim();
            } catch (Exception ignored) {
                return null;
            }
        }
        @Override public void setEnabledState(String packageName, String enabledState) throws Exception {
            requireOpen();
            String command = switch (enabledState) {
                case "default", "manifest-disabled" -> StartupPackageCommands.resetEnabled(packageName);
                case "enabled" -> StartupPackageCommands.enable(packageName);
                case "disabled" -> StartupPackageCommands.disablePlain(packageName);
                case "disabled-user" -> StartupPackageCommands.disable(packageName);
                case "disabled-until-used" -> StartupPackageCommands.disableUntilUsed(packageName);
                default -> throw new IOException("Unsupported enabled state: " + enabledState);
            };
            checked(adb, command);
        }

        @Override public void forceStop(String packageName) throws Exception {
            requireOpen();
            Integer userId = StartupPackageCommands.parseCurrentUser(
                    checked(adb, StartupPackageCommands.currentUser()));
            if (userId == null) throw new IOException("Could not verify the current Shield user.");
            checked(adb, StartupPackageCommands.forceStop(packageName));
            String processes = checked(adb, StartupPackageCommands.processSnapshot());
            String userState = checked(adb, StartupPackageCommands.userState(packageName, userId));
            if (!StartupPackageCommands.verifiedStopped(packageName, processes, userState))
                throw new IOException("Stop could not be verified.");
        }

        @Override public void setBackgroundModes(String packageName, String runInBackground,
                                                 String runAnyInBackground) throws Exception {
            requireOpen();
            checked(adb, StartupPackageCommands.setAppOp(
                    packageName, "RUN_IN_BACKGROUND", runInBackground));
            checked(adb, StartupPackageCommands.setAppOp(
                    packageName, "RUN_ANY_IN_BACKGROUND", runAnyInBackground));
        }

        private void requireOpen() throws IOException {
            if (closed || cancelled) throw new IOException("Cancelled");
        }

        @Override public void close() {
            if (closed) return;
            closed = true;
            try { adb.close(); } catch (Exception ignored) { }
            if (active == adb) active = null;
        }
    }

    private boolean isLauncherPackage(String packageName) {
        PackageManager pm = context.getPackageManager();
        for (String category : List.of(android.content.Intent.CATEGORY_LEANBACK_LAUNCHER,
                android.content.Intent.CATEGORY_LAUNCHER, android.content.Intent.CATEGORY_HOME)) {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_MAIN)
                    .addCategory(category);
            for (android.content.pm.ResolveInfo info : pm.queryIntentActivities(
                    intent, PackageManager.MATCH_DISABLED_COMPONENTS)) {
                if (info.activityInfo != null && packageName.equals(info.activityInfo.packageName)) return true;
            }
        }
        return false;
    }

    private boolean manageableInstalled(String packageName) {
        if (!StartupPackageController.validPackageName(packageName)) return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(
                    packageName, PackageManager.MATCH_DISABLED_COMPONENTS);
            boolean system = (info.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
            StartupPackageState state = new StartupPackageState(packageName, packageName, system,
                    false, "default", "default", "default", java.util.Set.of());
            return !StartupRecoveryPolicy.assess(state, AndroidRecoveryCapabilities.resolve(context)).protectedPackage();
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }
    private boolean eligibleInstalledForPrevention(String packageName) {
        if (!StartupCleanupPolicy.validPackage(packageName)) return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(
                    packageName, PackageManager.MATCH_DISABLED_COMPONENTS);
            boolean system = (info.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
            return StartupCleanupPolicy.eligibleForPrevention(packageName, system);
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    private boolean eligibleInstalledForCleanStart(String packageName) {
        if (!StartupCleanupPolicy.validPackage(packageName)) return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(
                    packageName, PackageManager.MATCH_DISABLED_COMPONENTS);
            boolean system = (info.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
            return StartupCleanupPolicy.eligibleForCleanStart(packageName, system);
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    private <T> T withAdb(boolean allowNewApproval, Runnable approvalRequired, AdbTask<T> operation) throws Exception {
        if (cancelled || Thread.currentThread().isInterrupted()) throw new IOException("Cancelled");
        File keyFile = new File(context.getNoBackupFilesDir(), "boop-unified-local-adb.key");
        if (!allowNewApproval && !keyFile.exists())
            throw new AdbWire.AdbApprovalRequiredException("Local cleanup has not been approved yet.");
        KeyPair identity = AdbWire.identity(keyFile);
        try (AdbWire adb = new AdbWire()) {
            active = adb;
            adb.connect(5555, identity, 45_000, approvalRequired == null ? () -> {} : approvalRequired, allowNewApproval);
            String uid = checked(adb, "id -u").trim();
            if (!uid.equals("2000") && !uid.equals("0")) throw new IOException("Local connection is not an ADB shell.");
            return operation.run(adb);
        } finally { active = null; }
    }

    private String checked(AdbWire adb, String command) throws IOException {
        AdbWire.Result result = adb.execute(command, 20_000);
        if (result.exitCode != 0 || result.output.contains("SecurityException") || result.output.contains("Permission Denial"))
            throw new IOException(result.output.isBlank() ? "Shield rejected the command." :
                    result.output.substring(0, Math.min(160, result.output.length())));
        return result.output;
    }

    private static String compact(Throwable failure) {
        String value = failure.getMessage();
        if (value == null || value.isBlank()) value = failure.getClass().getSimpleName();
        value = value.replace('\n', ' ').replace('\r', ' ');
        return value.substring(0, Math.min(120, value.length()));
    }

    private interface AdbTask<T> { T run(AdbWire adb) throws Exception; }
}
