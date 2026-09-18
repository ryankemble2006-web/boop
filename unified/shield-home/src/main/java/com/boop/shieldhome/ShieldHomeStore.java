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
    private static final String KEY_NOW_PLAYING_PLAYER = "now_playing_player_package_v1";
    private static final String KEY_ROOM_CONTROL_ORDER_PREFIX = "room_control_order_v1:";
    private static final String KEY_ACCENT_HUE = "accent_hue_v1";
    static final int DEFAULT_ACCENT_HUE = 204;

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

    private static final String SMART_HOME_PANEL = "smart_home_panel_enabled_v1";
    public boolean smartHomePanelEnabled() { return preferences.getBoolean(SMART_HOME_PANEL, true); }
    public void setSmartHomePanelEnabled(boolean enabled) { preferences.putBoolean(SMART_HOME_PANEL, enabled); }

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


    public List<String> loadRoomControlOrder(String roomId, List<String> availableEntityIds) {
        String stableRoomId = roomId == null ? "" : roomId.trim();
        if (stableRoomId.isEmpty()) return reconcileOrder(List.of(), availableEntityIds);
        List<String> saved = decodeComponents(preferences.getString(
                KEY_ROOM_CONTROL_ORDER_PREFIX + stableRoomId, "[]"));
        return reconcileOrder(saved, availableEntityIds);
    }

    public void saveRoomControlOrder(String roomId, List<String> entityIds) {
        String stableRoomId = roomId == null ? "" : roomId.trim();
        if (stableRoomId.isEmpty()) return;
        preferences.putString(KEY_ROOM_CONTROL_ORDER_PREFIX + stableRoomId, encodeComponents(entityIds));
    }

    static List<String> reconcileOrder(List<String> preferred, List<String> available) {
        ArrayList<String> out = new ArrayList<>();
        Set<String> availableSet = new HashSet<>();
        if (available != null) {
            for (String value : available) if (value != null && !value.isEmpty()) availableSet.add(value);
        }
        if (preferred != null) {
            for (String value : preferred) {
                if (value != null && availableSet.contains(value) && !out.contains(value)) out.add(value);
            }
        }
        if (available != null) {
            for (String value : available) {
                if (value != null && !value.isEmpty() && !out.contains(value)) out.add(value);
            }
        }
        return out;
    }

    public int accentHue() {
        String raw = preferences.getString(KEY_ACCENT_HUE, Integer.toString(DEFAULT_ACCENT_HUE));
        try {
            int hue = Integer.parseInt(raw == null ? "" : raw.trim());
            return Math.max(0, Math.min(359, hue));
        } catch (NumberFormatException ignored) {
            return DEFAULT_ACCENT_HUE;
        }
    }

    public void setAccentHue(int hue) {
        preferences.putString(KEY_ACCENT_HUE, Integer.toString(Math.max(0, Math.min(359, hue))));
    }

    public boolean rowEnabled(OptionalRowRegistry.Key key) {
        return preferences.getBoolean(key.preferenceKey(), false);
    }

    public void setRowEnabled(OptionalRowRegistry.Key key, boolean enabled) {
        preferences.putBoolean(key.preferenceKey(), enabled);
    }

    public String nowPlayingPlayerPackage() {
        String value = preferences.getString(KEY_NOW_PLAYING_PLAYER, "");
        return value == null ? "" : value.trim();
    }

    public void setNowPlayingPlayerPackage(String packageName) {
        preferences.putString(
                KEY_NOW_PLAYING_PLAYER,
                packageName == null ? "" : packageName.trim());
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
