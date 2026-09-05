package com.boop.shieldoverlay;

import android.content.Context;
import android.content.SharedPreferences;

public final class BoopPreferences {
    private static final String PREFS_NAME = "boop_home";
    private static final String KEY_AREA_ID = "selected_area_id_v1";
    private static final String KEY_AREA_NAME = "selected_area_name_v1";

    public interface Store {
        String getString(String key);
        void putString(String key, String value);
        void remove(String key);
    }

    private final Store store;

    public BoopPreferences(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.store = new SharedPreferencesStore(preferences);
    }

    BoopPreferences(Store store) {
        if (store == null) {
            throw new IllegalArgumentException("preference store is required");
        }
        this.store = store;
    }

    public AreaInfo selectedRoom() {
        String id = clean(store.getString(KEY_AREA_ID));
        String name = clean(store.getString(KEY_AREA_NAME));
        if (id == null || name == null) {
            return null;
        }
        return new AreaInfo(id, name);
    }

    public boolean hasSelectedRoom() {
        return selectedRoom() != null;
    }

    public void setSelectedRoom(AreaInfo area) {
        if (area == null) {
            throw new IllegalArgumentException("area is required");
        }
        store.putString(KEY_AREA_ID, area.id());
        store.putString(KEY_AREA_NAME, area.name());
    }

    public void clearSelectedRoom() {
        store.remove(KEY_AREA_ID);
        store.remove(KEY_AREA_NAME);
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static final class SharedPreferencesStore implements Store {
        private final SharedPreferences preferences;

        private SharedPreferencesStore(SharedPreferences preferences) {
            this.preferences = preferences;
        }

        @Override
        public String getString(String key) {
            return preferences.getString(key, null);
        }

        @Override
        public void putString(String key, String value) {
            preferences.edit().putString(key, value).apply();
        }

        @Override
        public void remove(String key) {
            preferences.edit().remove(key).apply();
        }
    }
}
