package com.boop.shieldhome;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class StartupRestoreStoreTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static StartupPackageState state(String enabled, String run, String runAny,
                                             Set<StartupRecoveryPolicy.ManagedAction> actions) {
        return new StartupPackageState("com.example.app", "Example", false, false,
                enabled, run, runAny, actions);
    }

    public static void main(String[] args) {
        Map<String,String> disk = new LinkedHashMap<>();
        StartupRestoreStore.Backend backend = StartupRestoreStore.mapBackend(disk);
        StartupRestoreStore store = new StartupRestoreStore(backend, () -> 1234L);

        StartupPackageState original = state("enabled", "allow", "default", Set.of());
        StartupRestoreRecord first = store.captureIfAbsent("com.example.app", original);
        check(first.originalEnabledState().equals("enabled"), "enabled baseline captured");
        check(first.originalRunInBackground().equals("allow"), "run baseline captured");
        check(first.originalRunAnyInBackground().equals("default"), "run-any baseline captured");
        check(first.firstChangedAtMillis() == 1234L, "first change timestamp captured");

        StartupPackageState later = state("disabled-user", "ignore", "ignore", Set.of());
        StartupRestoreRecord stillFirst = store.captureIfAbsent("com.example.app", later);
        check(stillFirst.originalEnabledState().equals("enabled"), "first baseline immutable");
        StartupPackageState applied = state("disabled-user", "ignore", "ignore",
                Set.of(StartupRecoveryPolicy.ManagedAction.DISABLED,
                        StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK,
                        StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN));
        StartupRestoreRecord updated = store.updateManagedActions(
                "com.example.app", applied.managedActions(), applied);
        check(updated.managedActions().size() == 3, "managed actions coexist");
        check(updated.originalEnabledState().equals("enabled"), "action update preserves baseline");
        check(!updated.drifted(applied), "last applied state is not drift");
        check(updated.drifted(state("enabled", "ignore", "ignore", applied.managedActions())),
                "outside state change detected");

        StartupRestoreStore recreated = new StartupRestoreStore(StartupRestoreStore.mapBackend(disk), () -> 9999L);
        StartupRestoreRecord persisted = recreated.record("com.example.app");
        check(persisted != null && persisted.managedActions().size() == 3, "receipt survives recreation");
        check(recreated.remove("com.example.app"), "explicit success removal works");
        check(recreated.record("com.example.app") == null, "removed only when asked");

        StartupPreventionRecord prevention = new StartupPreventionRecord(
                "com.example.app", "deny", "default", true);
        StartupRestoreRecord imported = recreated.capturePreventionIfAbsent(
                prevention, "enabled", () -> 7777L);
        check(imported.originalRunInBackground().equals("deny"), "prevention run mode preserved");
        check(imported.originalRunAnyInBackground().equals("default"), "prevention run-any mode preserved");
        check(imported.managedActions().contains(StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK),
                "prevention managed action imported");
        check(imported.firstChangedAtMillis() == 7777L, "migration timestamp set once");

        String encoded = imported.encode();
        check(encoded.startsWith("{"), "record encoded as JSON object");
        check(StartupRestoreRecord.decode(encoded).equals(imported), "JSON round trip");
        System.out.println("StartupRestoreStoreTest PASS");
    }
}
