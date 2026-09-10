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
            String resumed = StartupCleanupPolicy.parseResumedPackage(
                    checked(adb, StartupCleanupPolicy.resumedActivityCommand()));
            Integer userId = StartupCleanupPolicy.parseCurrentUser(
                    checked(adb, StartupCleanupPolicy.currentUserCommand()));
            if (userId == null) throw new IOException("Could not verify the current Shield user.");

            List<String> details = new ArrayList<>();
            int stopped = 0, skipped = 0, failed = 0;
            for (String packageName : requested) {
                if (!eligibleInstalled(packageName)) {
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
                    checked(adb, StartupCleanupPolicy.forceStopCommand(packageName));
                    String processes = checked(adb, StartupCleanupPolicy.processSnapshotCommand());
                    String state = checked(adb, StartupCleanupPolicy.userStateCommand(packageName, userId));
                    if (!StartupCleanupPolicy.verifiedStopped(packageName, processes, state))
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
        if (!eligibleInstalled(packageName)) throw new IOException("App is not eligible for startup prevention.");
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

    private boolean eligibleInstalled(String packageName) {
        if (!StartupCleanupPolicy.validPackage(packageName)) return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(
                    packageName, PackageManager.MATCH_DISABLED_COMPONENTS);
            boolean system = (info.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
            return StartupCleanupPolicy.eligible(packageName, system);
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
