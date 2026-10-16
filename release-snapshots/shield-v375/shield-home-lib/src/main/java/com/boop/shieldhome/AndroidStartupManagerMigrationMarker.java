package com.boop.shieldhome;

import android.content.Context;
import android.content.SharedPreferences;

final class AndroidStartupManagerMigrationMarker implements StartupManagerMigration.Marker {
    private static final String PREFS = "boop_startup_manager_v2";
    private static final String MIGRATED = "v1_migrated";
    private final SharedPreferences prefs;

    AndroidStartupManagerMigrationMarker(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    @Override public boolean migrated() { return prefs.getBoolean(MIGRATED, false); }
    @Override public boolean markMigrated() { return prefs.edit().putBoolean(MIGRATED, true).commit(); }
}