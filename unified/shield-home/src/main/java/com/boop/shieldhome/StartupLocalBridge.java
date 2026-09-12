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

    public String openDeezerLyrics() throws Exception {
        cancelled = false;
        return withAdb(false, null, adb -> {
            com.boop.shared.DeezerScreen screen = dumpDeezerScreen(adb);
            if (screen.hasLyricsPanel()) return "Lyrics already open.";
            com.boop.shared.DeezerScreen.Target target = screen.lyricsTarget();
            if (target == null) throw new IOException("Deezer Lyrics control is not available.");
            checked(adb, "input tap " + target.x + " " + target.y);
            for (int attempt = 0; attempt < 5; attempt++) {
                Thread.sleep(attempt == 0 ? 220L : 180L);
                try {
                    if (dumpDeezerScreen(adb).hasLyricsPanel()) return "Lyrics opened.";
                } catch (IOException settling) {
                    if (attempt == 4) throw settling;
                }
            }
            throw new IOException("Deezer did not open lyrics.");
        });
    }

    private com.boop.shared.DeezerScreen dumpDeezerScreen(AdbWire adb) throws Exception {
        String path = "/sdcard/boop_lyrics.xml";
        String command = "rm -f " + path + "; uiautomator dump " + path
                + " >/dev/null 2>&1; code=$?; if [ $code -eq 0 ]; then cat " + path
                + "; fi; rm -f " + path + "; exit $code";
        return com.boop.shared.DeezerScreen.parse(checked(adb, command));
    }

    public String run(Collection<String> requested, boolean allowApproval, Runnable approvalRequired) throws Exception {
        int stopped=0, skipped=0, failed=0;
        try(PackageSession session=openPackageSession(allowApproval,approvalRequired)) {
            for(String pkg: requested) {
                session.requireOpen();
                String foreground=StartupPackageCommands.parseResumedPackage(checked(session.adb,
                        "dumpsys activity activities | grep -m2 -E 'mResumedActivity:|topResumedActivity='"));
                if(foreground==null) throw new IOException("Could not verify which app is open; cleanup stopped.");
                if(pkg.equals(foreground)) { skipped++; continue; }
                try {
                    StartupPackageState state=session.probe(pkg);
                    if(StartupRecoveryPolicy.assess(state,AndroidRecoveryCapabilities.resolve(context)).protectedPackage()) {
                        skipped++; continue;
                    }
                    session.forceStop(pkg); stopped++;
                } catch(Exception failure) {
                    if(cancelled || Thread.currentThread().isInterrupted()) throw failure;
                    failed++;
                }
            }
        }
        return "Clean after boot: " + stopped + " stopped, " + skipped + " left open, " + failed + " not changed.";
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
        private final int userId;
        private boolean closed;
        private java.util.Map<String,StartupPackageState> known;
        private PackageSession(AdbWire adb) throws IOException {
            this.adb=adb;
            Integer current=StartupPackageCommands.parseCurrentUser(checked(adb,StartupPackageCommands.currentUser()));
            // Android UIDs reserve 100000 IDs per user. Receipts belong to this app's user only.
            if(current==null || current != context.getApplicationInfo().uid / 100000)
                throw new IOException("Open BOOP under the current Shield user before making changes.");
            userId=current;
        }
        private void loadKnown() throws Exception {
            if(known!=null) return;
            String all=checked(adb,StartupPackageCommands.installedPackages(false,false,userId));
            String system=checked(adb,StartupPackageCommands.installedPackages(true,false,userId));
            String disabled=checked(adb,StartupPackageCommands.installedPackages(false,true,userId));
            String home=checked(adb,StartupPackageCommands.homeActivities(userId));
            known=new java.util.LinkedHashMap<>();
            for(StartupPackageState row: StartupPackageInventory.merge(all,system,disabled,home,this::visibleLabel))
                known.put(row.packageName(),row);
        }
        @Override public StartupPackageState probe(String pkg) throws Exception {
            requireOpen(); loadKnown();
            StartupPackageState metadata=known.get(pkg);
            if(metadata==null) throw new IOException("This package is not installed for the current user.");
            String state=checked(adb,StartupPackageCommands.userState(pkg,userId));
            String run=StartupPreventionPolicy.parseMode(checked(adb,
                    StartupPackageCommands.forUser(StartupPackageCommands.queryAppOp(pkg,"RUN_IN_BACKGROUND"),userId)));
            String any=StartupPreventionPolicy.parseMode(checked(adb,
                    StartupPackageCommands.forUser(StartupPackageCommands.queryAppOp(pkg,"RUN_ANY_IN_BACKGROUND"),userId)));
            StartupRestoreRecord record=new StartupRestoreStore(new AndroidStartupRestoreBackend(context),
                    System::currentTimeMillis).record(pkg);
            java.util.EnumSet<StartupRecoveryPolicy.ManagedAction> flags=java.util.EnumSet.noneOf(StartupRecoveryPolicy.ManagedAction.class);
            if(record!=null) flags.addAll(record.managedActions());
            if(new StartupCleanupStore(context).targets().contains(pkg)) flags.add(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN);
            else flags.remove(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN);
            return StartupPackageInventory.detail(pkg,state,metadata.systemApp(),metadata.launcher(),metadata.label(),run,any,flags);
        }
        public List<StartupPackageState> inventory() throws Exception {
            requireOpen(); known=null; loadKnown();
            StartupRestoreStore restores=new StartupRestoreStore(new AndroidStartupRestoreBackend(context),System::currentTimeMillis);
            java.util.Set<String> boot=new StartupCleanupStore(context).targets();
            java.util.Set<String> legacy=new AndroidStartupManagerMigrationMarker(context).migrated()
                    ? java.util.Set.of() : new StartupPreventionStore(context).managedPackages();
            ArrayList<StartupPackageState> result=new ArrayList<>();
            for(StartupPackageState row:known.values()) {
                java.util.EnumSet<StartupRecoveryPolicy.ManagedAction> flags=java.util.EnumSet.noneOf(StartupRecoveryPolicy.ManagedAction.class);
                try { StartupRestoreRecord record=restores.record(row.packageName()); if(record!=null) flags.addAll(record.managedActions()); }
                catch(IllegalStateException damaged) { /* Preserve unreadable receipts; per-package mutation fails closed. */ }
                if(boot.contains(row.packageName())) flags.add(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN);
                else flags.remove(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN);
                if(legacy.contains(row.packageName())) flags.add(StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK);
                result.add(new StartupPackageState(row.packageName(),row.label(),row.systemApp(),row.launcher(),row.enabledState(),
                        "unknown","unknown",flags));
            }
            return List.copyOf(result);
        }
        private String visibleLabel(String pkg) {
            try {
                ApplicationInfo info=context.getPackageManager().getApplicationInfo(pkg,PackageManager.MATCH_DISABLED_COMPONENTS);
                CharSequence label=context.getPackageManager().getApplicationLabel(info);
                return label==null ? pkg : label.toString();
            } catch(Exception invisible) { return pkg; }
        }
        @Override public void setEnabledState(String pkg,String state) throws Exception {
            requireOpen();
            String command=switch(state) {
                case "default","manifest-disabled" -> StartupPackageCommands.resetEnabled(pkg);
                case "enabled" -> StartupPackageCommands.enable(pkg);
                case "disabled" -> StartupPackageCommands.disablePlain(pkg);
                case "disabled-user" -> StartupPackageCommands.disable(pkg);
                case "disabled-until-used" -> StartupPackageCommands.disableUntilUsed(pkg);
                default -> throw new IOException("Unknown enabled state.");
            };
            checked(adb,StartupPackageCommands.forUser(command,userId));
        }
        public boolean canCleanNow(String pkg) throws Exception {
            requireOpen();
            String foreground=StartupPackageCommands.parseResumedPackage(checked(adb,
                    "dumpsys activity activities | grep -m2 -E 'mResumedActivity:|topResumedActivity='"));
            return foreground!=null && !pkg.equals(foreground);
        }
        @Override public void forceStop(String pkg) throws Exception {
            requireOpen();
            checked(adb,StartupPackageCommands.forUser(StartupPackageCommands.forceStop(pkg),userId));
            String processes=checked(adb,StartupPackageCommands.processSnapshot());
            String state=checked(adb,StartupPackageCommands.userState(pkg,userId));
            if(!StartupPackageCommands.verifiedStopped(pkg,processes,state)) throw new IOException("Stop could not be verified.");
        }
        @Override public void setBackgroundModes(String pkg,String run,String any) throws Exception {
            requireOpen();
            checked(adb,StartupPackageCommands.forUser(StartupPackageCommands.setAppOp(pkg,"RUN_IN_BACKGROUND",run),userId));
            requireOpen();
            checked(adb,StartupPackageCommands.forUser(StartupPackageCommands.setAppOp(pkg,"RUN_ANY_IN_BACKGROUND",any),userId));
        }
        private void requireOpen() throws IOException {
            if(closed || cancelled || Thread.currentThread().isInterrupted()) throw new IOException("Cancelled");
            Integer current=StartupPackageCommands.parseCurrentUser(checked(adb,StartupPackageCommands.currentUser()));
            if(current==null || current!=userId) throw new IOException("The Shield user changed. No further changes were made.");
        }
        @Override public void close() {
            closed=true;
            try { adb.close(); } catch(Exception ignored) { }
            if(active==adb) active=null;
        }
    }

    private boolean isLauncherPackage(String packageName) {
        PackageManager pm = context.getPackageManager();
        for (String category : List.of(android.content.Intent.CATEGORY_HOME)) {
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
        if(cancelled || Thread.currentThread().isInterrupted()) throw new IOException("Cancelled");
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
