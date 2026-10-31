package com.boop.shieldhome;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public final class StartupCleanupStore {
    private static final String PREFS = "boop_startup_cleanup_v1";
    private static final String TARGETS = "targets";
    private static final String AUTO = "auto";
    private static final String SUMMARY = "last_summary";
    private final SharedPreferences prefs;

    public StartupCleanupStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public LinkedHashSet<String> targets() {
        ArrayList<String> values = new ArrayList<>();
        for (String value : prefs.getStringSet(TARGETS, Set.of())) {
            if (StartupPackageController.validPackageName(value)) values.add(value);
        }
        values.sort(String.CASE_INSENSITIVE_ORDER);
        return new LinkedHashSet<>(values);
    }

    public boolean setTarget(String packageName, boolean enabled) {
        if (!StartupPackageController.validPackageName(packageName)) return false;
        LinkedHashSet<String> next = targets();
        if (enabled) next.add(packageName);
        else next.remove(packageName);
        return prefs.edit().putStringSet(TARGETS, new LinkedHashSet<>(next)).commit();
    }

    public boolean autoEnabled() { return prefs.getBoolean(AUTO, false); }
    public boolean setAutoEnabled(boolean enabled) { return prefs.edit().putBoolean(AUTO, enabled).commit(); }

    public void recordSummary(String value) {
        String safe = value == null ? "" : value.replace('\n', ' ').replace('\r', ' ').trim();
        if (safe.length() > 500) safe = safe.substring(0, 500);
        prefs.edit().putString(SUMMARY, safe).apply();
    }

    public String lastSummary() {
        String value = prefs.getString(SUMMARY, "");
        return value == null ? "" : value.trim();
    }
}
