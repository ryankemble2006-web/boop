package com.boop.shieldhome;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.LinkedHashSet;
import java.util.Set;

public final class StartupPreventionStore {
    private static final String PREFS = "boop_startup_prevention_v1";
    private static final String PREFIX = "entry.";
    private final SharedPreferences prefs;

    public StartupPreventionStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public StartupPreventionRecord record(String packageName) {
        if (!StartupCleanupPolicy.validPackage(packageName)) return null;
        return StartupPreventionRecord.decode(prefs.getString(PREFIX + packageName, null));
    }

    public boolean rememberOriginal(StartupPreventionRecord original) {
        if (original == null || original.managed()) return false;
        StartupPreventionRecord existing = record(original.packageName());
        if (existing != null) return true;
        return prefs.edit().putString(PREFIX + original.packageName(), original.encode()).commit();
    }
    public boolean markManaged(String packageName) {
        StartupPreventionRecord current = record(packageName);
        if (current == null) return false;
        StartupPreventionRecord next = new StartupPreventionRecord(packageName,
                current.originalRunInBackground(), current.originalRunAnyInBackground(), true);
        return prefs.edit().putString(PREFIX + packageName, next.encode()).commit();
    }

    public boolean remove(String packageName) {
        if (!StartupCleanupPolicy.validPackage(packageName)) return false;
        return prefs.edit().remove(PREFIX + packageName).commit();
    }

    public Set<String> managedPackages() {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String key : prefs.getAll().keySet()) {
            if (!key.startsWith(PREFIX)) continue;
            StartupPreventionRecord record = StartupPreventionRecord.decode(
                    prefs.getString(key, null));
            if (record != null && record.managed()) out.add(record.packageName());
        }
        return out;
    }

    public Set<StartupPreventionRecord> records() {
        LinkedHashSet<StartupPreventionRecord> out = new LinkedHashSet<>();
        for (String key : prefs.getAll().keySet()) {
            if (!key.startsWith(PREFIX)) continue;
            StartupPreventionRecord record = StartupPreventionRecord.decode(prefs.getString(key, null));
            if (record != null) out.add(record);
        }
        return out;
    }
}
