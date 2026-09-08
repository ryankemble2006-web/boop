package com.boop.shieldhome;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ShieldHomeStore {
    private static final String PREFS = "boop_shield_home_v1";
    private static final String KEY_INITIALISED = "favourites_initialised";
    private static final String KEY_FAVOURITES = "favourites_json";

    public interface Preferences {
        String getString(String key, String fallback);
        boolean getBoolean(String key, boolean fallback);
        void putString(String key, String value);
        void putBoolean(String key, boolean value);
    }

    private static final class AndroidPreferences implements Preferences {
        private final SharedPreferences preferences;

        AndroidPreferences(Context context) {
            preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        }

        @Override public String getString(String key, String fallback) {
            return preferences.getString(key, fallback);
        }

        @Override public boolean getBoolean(String key, boolean fallback) {
            return preferences.getBoolean(key, fallback);
        }

        @Override public void putString(String key, String value) {
            preferences.edit().putString(key, value).apply();
        }

        @Override public void putBoolean(String key, boolean value) {
            preferences.edit().putBoolean(key, value).apply();
        }
    }

    private final Preferences preferences;

    public ShieldHomeStore(Context context) {
        this(new AndroidPreferences(context.getApplicationContext()));
    }

    ShieldHomeStore(Preferences preferences) {
        this.preferences = preferences;
    }

    public List<String> loadOrSeedFavourites(List<TvAppEntry> installed) {
        if (preferences.getBoolean(KEY_INITIALISED, false)) {
            return decodeComponents(preferences.getString(KEY_FAVOURITES, "[]"));
        }

        ArrayList<String> seeded = new ArrayList<>();
        if (installed != null) {
            for (TvAppEntry entry : installed) {
                if (entry != null && !entry.component().isEmpty() && !seeded.contains(entry.component())) {
                    seeded.add(entry.component());
                }
            }
        }
        preferences.putString(KEY_FAVOURITES, encodeComponents(seeded));
        preferences.putBoolean(KEY_INITIALISED, true);
        return seeded;
    }

    public void saveFavourites(List<String> components) {
        preferences.putString(KEY_FAVOURITES, encodeComponents(components));
        preferences.putBoolean(KEY_INITIALISED, true);
    }

    public boolean rowEnabled(OptionalRowRegistry.Key key) {
        return preferences.getBoolean(key.preferenceKey(), false);
    }

    public void setRowEnabled(OptionalRowRegistry.Key key, boolean enabled) {
        preferences.putBoolean(key.preferenceKey(), enabled);
    }

    public static String encodeComponents(List<String> components) {
        JSONArray array = new JSONArray();
        if (components != null) {
            Set<String> seen = new HashSet<>();
            for (String component : components) {
                if (component != null && !component.isEmpty() && seen.add(component)) {
                    array.put(component);
                }
            }
        }
        return array.toString();
    }

    public static List<String> decodeComponents(String raw) {
        ArrayList<String> out = new ArrayList<>();
        if (raw == null) {
            return out;
        }
        try {
            JSONArray array = new JSONArray(raw);
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < array.length(); i++) {
                String component = array.optString(i, "");
                if (!component.isEmpty() && seen.add(component)) {
                    out.add(component);
                }
            }
        } catch (JSONException | RuntimeException ignored) {
            out.clear();
        }
        return out;
    }
}
