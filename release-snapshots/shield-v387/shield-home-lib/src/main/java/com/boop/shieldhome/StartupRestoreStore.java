package com.boop.shieldhome;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.LongSupplier;

public final class StartupRestoreStore {
    private static final String PREFIX = "entry.";

    public interface Backend {
        String get(String key);
        boolean put(String key, String value);
        boolean remove(String key);
        Set<String> keys();
    }

    private final Backend backend;
    private final LongSupplier clock;

    public StartupRestoreStore(Backend backend, LongSupplier clock) {
        this.backend = backend;
        this.clock = clock;
    }

    public StartupRestoreRecord record(String packageName) {
        String raw = backend.get(PREFIX + packageName);
        StartupRestoreRecord value = StartupRestoreRecord.decode(raw);
        if (raw != null && (value == null || !packageName.equals(value.packageName())))
            throw new IllegalStateException("Saved restore data needs attention; nothing was changed.");
        return value;
    }

    public StartupRestoreRecord captureIfAbsent(String packageName, StartupPackageState baseline) {
        if (baseline == null || !packageName.equals(baseline.packageName()))
            throw new IllegalArgumentException("Restore target does not match the observed package");
        StartupRestoreRecord existing = record(packageName);
        if (existing != null) return existing;
        StartupRestoreRecord created = StartupRestoreRecord.fromBaseline(baseline, clock.getAsLong());
        if (!backend.put(PREFIX + packageName, created.encode())) throw new IllegalStateException("Could not save restore baseline");
        if (!created.equals(record(created.packageName()))) throw new IllegalStateException("Restore save could not be verified");
        return created;
    }
    public StartupRestoreRecord updateManagedActions(String packageName,
                                                     Set<StartupRecoveryPolicy.ManagedAction> actions,
                                                     StartupPackageState applied) {
        StartupRestoreRecord current = record(packageName);
        if (current == null) throw new IllegalStateException("Missing restore baseline");
        StartupRestoreRecord next = current.withManagedActions(actions, applied);
        if (!backend.put(PREFIX + packageName, next.encode())) throw new IllegalStateException("Could not update restore record");
        if (!next.equals(record(packageName))) throw new IllegalStateException("Restore update could not be verified");
        return next;
    }

    public StartupRestoreRecord capturePreventionIfAbsent(StartupPreventionRecord prevention,
                                                           String enabledState,
                                                           LongSupplier migrationClock) {
        StartupRestoreRecord existing = record(prevention.packageName());
        if (existing != null) return existing;
        StartupRestoreRecord created = new StartupRestoreRecord(
                prevention.packageName(), enabledState,
                prevention.originalRunInBackground(), prevention.originalRunAnyInBackground(),
                prevention.managed() ? Set.of(StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK) : Set.of(),
                enabledState,
                prevention.managed() ? "ignore" : prevention.originalRunInBackground(),
                prevention.managed() ? "ignore" : prevention.originalRunAnyInBackground(),
                migrationClock.getAsLong());
        if (!backend.put(PREFIX + prevention.packageName(), created.encode())) {
            throw new IllegalStateException("Could not import prevention restore baseline");
        }
        if (!created.equals(record(created.packageName()))) throw new IllegalStateException("Restore save could not be verified");
        return created;
    }

    public boolean remove(String packageName) {
        return backend.remove(PREFIX + packageName) && backend.get(PREFIX + packageName) == null;
    }

    public List<StartupRestoreRecord> records() {
        ArrayList<StartupRestoreRecord> out = new ArrayList<>();
        for (String key : backend.keys()) {
            if (!key.startsWith(PREFIX)) continue;
            StartupRestoreRecord record = StartupRestoreRecord.decode(backend.get(key));
            if (record != null) out.add(record);
        }
        out.sort((a,b) -> a.packageName().compareToIgnoreCase(b.packageName()));
        return List.copyOf(out);
    }
    public static Backend mapBackend(Map<String,String> map) {
        return new Backend() {
            @Override public String get(String key) { return map.get(key); }
            @Override public boolean put(String key, String value) { map.put(key, value); return true; }
            @Override public boolean remove(String key) { map.remove(key); return true; }
            @Override public Set<String> keys() { return Set.copyOf(map.keySet()); }
        };
    }

    public static Backend memoryBackend() {
        return mapBackend(new LinkedHashMap<>());
    }
}
