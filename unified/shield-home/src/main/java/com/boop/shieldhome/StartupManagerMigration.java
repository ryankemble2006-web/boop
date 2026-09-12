package com.boop.shieldhome;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class StartupManagerMigration {
    public interface LegacySource {
        Set<String> cleanupTargets();
        Set<StartupPreventionRecord> preventionRecords();
    }
    public interface Probe { StartupPackageState probe(String packageName) throws Exception; }
    public interface Marker { boolean migrated(); boolean markMigrated(); }
    public record Result(boolean success, boolean alreadyMigrated, int importedPackages, String summary) { }

    private final LegacySource legacy;
    private final Probe probe;
    private final StartupRestoreStore restoreStore;
    private final Marker marker;

    public StartupManagerMigration(LegacySource legacy, Probe probe,
                                   StartupRestoreStore restoreStore, Marker marker) {
        this.legacy = legacy;
        this.probe = probe;
        this.restoreStore = restoreStore;
        this.marker = marker;
    }

    public Result runOnce() {
        if (marker.migrated()) return new Result(true, true, 0, "Already migrated.");
        try {
            Map<String, StartupPreventionRecord> prevention = new LinkedHashMap<>();
            for (StartupPreventionRecord record : legacy.preventionRecords()) {
                if (record != null) prevention.put(record.packageName(), record);
            }
            LinkedHashSet<String> packages = new LinkedHashSet<>(legacy.cleanupTargets());
            packages.addAll(prevention.keySet());
            int imported = 0;
            for (String packageName : packages) {
                StartupPackageState current = probe.probe(packageName);
                StartupPreventionRecord oldPrevention = prevention.get(packageName);
                StartupRestoreRecord record;
                if (oldPrevention != null) {
                    record = restoreStore.capturePreventionIfAbsent(
                            oldPrevention, current.enabledState(), System::currentTimeMillis);
                } else {
                    record = restoreStore.captureIfAbsent(packageName, current);
                }
                LinkedHashSet<StartupRecoveryPolicy.ManagedAction> actions =
                        new LinkedHashSet<>(record.managedActions());
                if (legacy.cleanupTargets().contains(packageName)) {
                    actions.add(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN);
                }
                if (oldPrevention != null && oldPrevention.managed()) {
                    actions.add(StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK);
                }
                restoreStore.updateManagedActions(packageName, actions, current);
                imported++;
            }
            if (!marker.markMigrated()) throw new IllegalStateException("Could not save migration marker.");
            return new Result(true, false, imported, "Imported " + imported + " package(s).");
        } catch (Exception failure) {
            String message = failure.getMessage();
            if (message == null || message.isBlank()) message = failure.getClass().getSimpleName();
            return new Result(false, false, 0, message);
        }
    }
}