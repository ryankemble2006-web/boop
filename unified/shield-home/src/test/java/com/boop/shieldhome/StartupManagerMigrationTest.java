package com.boop.shieldhome;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class StartupManagerMigrationTest {
    private static void check(boolean v, String m) { if (!v) throw new AssertionError(m); }
    private static StartupPackageState state(String pkg, String run, String runAny) {
        return new StartupPackageState(pkg, pkg, pkg.startsWith("com.google") || pkg.startsWith("com.android"),
                pkg.contains("launcher"), "default", run, runAny, Set.of());
    }
    private static final class Legacy implements StartupManagerMigration.LegacySource {
        final Set<String> clean = new LinkedHashSet<>();
        final Set<StartupPreventionRecord> prevention = new LinkedHashSet<>();
        @Override public Set<String> cleanupTargets() { return Set.copyOf(clean); }
        @Override public Set<StartupPreventionRecord> preventionRecords() { return Set.copyOf(prevention); }
    }
    private static final class Marker implements StartupManagerMigration.Marker {
        boolean value;
        boolean failWrite;
        @Override public boolean migrated() { return value; }
        @Override public boolean markMigrated() { if (failWrite) return false; value = true; return true; }
    }
    public static void main(String[] args) {
        Legacy legacy = new Legacy();
        legacy.clean.addAll(List.of("com.example.boot", "com.example.both", "com.google.android.tvlauncher"));
        legacy.prevention.add(new StartupPreventionRecord("com.example.bg", "allow", "default", true));
        legacy.prevention.add(new StartupPreventionRecord("com.example.both", "default", "allow", true));
        StartupRestoreStore store = new StartupRestoreStore(StartupRestoreStore.memoryBackend(), () -> 55L);
        Marker marker = new Marker();
        StartupManagerMigration migration = new StartupManagerMigration(legacy, pkg -> switch (pkg) {
            case "com.example.bg" -> state(pkg, "ignore", "ignore");
            case "com.example.both" -> state(pkg, "ignore", "ignore");
            default -> state(pkg, "default", "default");
        }, store, marker);
        StartupManagerMigration.Result result = migration.runOnce();
        check(result.success() && result.importedPackages() == 4, "four unique packages imported");
        check(marker.value, "migration marker written after import");
        check(store.record("com.example.boot").managedActions().equals(Set.of(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN)), "boot-only action imported");
        check(store.record("com.example.bg").originalRunInBackground().equals("allow"), "prevention original run mode retained");
        check(store.record("com.example.bg").originalRunAnyInBackground().equals("default"), "prevention original run-any mode retained");
        check(store.record("com.example.both").managedActions().containsAll(Set.of(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN, StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK)), "combined actions imported");
        check(store.record("com.google.android.tvlauncher").managedActions().contains(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN), "stock launcher cleanup imported");
        check(migration.runOnce().alreadyMigrated(), "second run idempotent");

        Legacy failingLegacy = new Legacy();
        failingLegacy.clean.add("com.example.fail");
        Marker failingMarker = new Marker();
        StartupRestoreStore failingStore = new StartupRestoreStore(StartupRestoreStore.memoryBackend(), () -> 77L);
        StartupManagerMigration failing = new StartupManagerMigration(failingLegacy, pkg -> { throw new Exception("probe failed"); }, failingStore, failingMarker);
        StartupManagerMigration.Result failed = failing.runOnce();
        check(!failed.success() && !failingMarker.value, "failed import leaves marker clear");
        check(failingLegacy.clean.contains("com.example.fail"), "legacy data untouched on failure");
        Legacy bootLegacy = new Legacy(); bootLegacy.clean.add("com.example.booted");
        StartupRestoreStore bootStore = new StartupRestoreStore(StartupRestoreStore.memoryBackend(), () -> 80L);
        StartupPackageState bootState = new StartupPackageState("com.example.booted", "Booted", false, false,
                "default", "default", "default", Set.of(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN));
        check(new StartupManagerMigration(bootLegacy, pkg -> bootState, bootStore, new Marker()).runOnce().success(), "boot migration succeeds");
        check(!bootStore.record("com.example.booted").originalBootClean(), "Undo removes the original BOOP v1 boot rule");
        check(bootStore.record("com.example.booted").lastAppliedBootClean(), "current boot rule is kept during migration");
        System.out.println("StartupManagerMigrationTest PASS");
    }
}