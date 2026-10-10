package com.boop.shieldhome;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Set;

final class AndroidStartupRestoreBackend implements StartupRestoreStore.Backend {
    private static final String PREFS = "boop_startup_restore_v2";
    private final SharedPreferences prefs;

    AndroidStartupRestoreBackend(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    @Override public String get(String key) { return prefs.getString(key, null); }
    @Override public boolean put(String key, String value) { return prefs.edit().putString(key, value).commit(); }
    @Override public boolean remove(String key) { return prefs.edit().remove(key).commit(); }
    @Override public Set<String> keys() { return Set.copyOf(prefs.getAll().keySet()); }
}
