package com.boop.shieldhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages custom hardware remote & controller key mappings.
 * Ships with Ryan's exact Shield mappings as immutable built-in defaults,
 * while allowing other installations or future games to reconfigure via UI.
 */
public final class BoopKeyMappingStore {
    private static final String PREF_NAME = "boop_key_mappings";

    public static final String ACTION_DEFAULT = "default";
    public static final String ACTION_NONE = "none";
    public static final String ACTION_RECENTS = "recents";
    public static final String ACTION_NEXT_TRACK = "next_track";
    public static final String ACTION_PREV_TRACK = "prev_track";
    public static final String ACTION_PLAY_PAUSE = "play_pause";
    public static final String ACTION_DEEZER_FLOW = "deezer_flow";
    public static final String ACTION_SLEEP_TIMER = "sleep_timer";
    public static final String ACTION_BRIGHTNESS_SLIDER = "brightness_slider";
    public static final String ACTION_TORCH = "torch";
    public static final String ACTION_CUSTOM_APP = "custom_app";

    public static final int KEYCODE_NETFLIX = 199; // KEYCODE_BUTTON_12 on Shield remote

    public static final class KeyEntry {
        public final int keyCode;
        public final String name;
        public final String singleTapAction;
        public final String doubleTapAction;
        public final String longPressAction;
        public final String targetComponent;

        public KeyEntry(int keyCode, String name, String singleTapAction, String doubleTapAction, String longPressAction, String targetComponent) {
            this.keyCode = keyCode;
            this.name = name;
            this.singleTapAction = singleTapAction != null ? singleTapAction : ACTION_DEFAULT;
            this.doubleTapAction = doubleTapAction != null ? doubleTapAction : ACTION_DEFAULT;
            this.longPressAction = longPressAction != null ? longPressAction : ACTION_DEFAULT;
            this.targetComponent = targetComponent != null ? targetComponent : "";
        }

        public KeyEntry(int keyCode, String name, String singleTapAction, String longPressAction, String targetComponent) {
            this(keyCode, name, singleTapAction, ACTION_DEFAULT, longPressAction, targetComponent);
        }
    }

    private static final Map<Integer, KeyEntry> DEFAULTS = new LinkedHashMap<>();
    static {
        DEFAULTS.put(KeyEvent.KEYCODE_MEDIA_REWIND, new KeyEntry(
                KeyEvent.KEYCODE_MEDIA_REWIND, "Media Rewind (Key 89)", ACTION_PREV_TRACK, ACTION_DEFAULT, ACTION_DEFAULT, null));
        DEFAULTS.put(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, new KeyEntry(
                KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, "Media Fast Forward (Key 90)", ACTION_NEXT_TRACK, ACTION_DEFAULT, ACTION_DEFAULT, null));
        DEFAULTS.put(KEYCODE_NETFLIX, new KeyEntry(
                KEYCODE_NETFLIX, "Netflix Button (Key 199)", ACTION_SLEEP_TIMER, ACTION_TORCH, ACTION_BRIGHTNESS_SLIDER, "com.neon.sleeptvtimerpro/.MainActivity"));
        DEFAULTS.put(KeyEvent.KEYCODE_HOME, new KeyEntry(
                KeyEvent.KEYCODE_HOME, "Home Button (Key 3)", ACTION_DEFAULT, ACTION_RECENTS, ACTION_DEEZER_FLOW, null));
        DEFAULTS.put(KeyEvent.KEYCODE_MENU, new KeyEntry(
                KeyEvent.KEYCODE_MENU, "Menu / Burger (Key 82)", ACTION_NEXT_TRACK, ACTION_DEFAULT, ACTION_PREV_TRACK, null));
        DEFAULTS.put(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, new KeyEntry(
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, "Play / Pause (Key 85)", ACTION_PLAY_PAUSE, ACTION_DEFAULT, ACTION_DEFAULT, null));
    }

    private final SharedPreferences prefs;

    public BoopKeyMappingStore(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static List<Integer> watchedKeyCodes() {
        return new ArrayList<>(DEFAULTS.keySet());
    }

    public List<KeyEntry> loadAll() {
        List<KeyEntry> list = new ArrayList<>();
        for (Map.Entry<Integer, KeyEntry> def : DEFAULTS.entrySet()) {
            int code = def.getKey();
            KeyEntry d = def.getValue();
            String single = prefs.getString(code + "_single", d.singleTapAction);
            String dbl = prefs.getString(code + "_double", d.doubleTapAction);
            String lp = prefs.getString(code + "_long", d.longPressAction);
            String comp = prefs.getString(code + "_comp", d.targetComponent);
            list.add(new KeyEntry(code, d.name, single, dbl, lp, comp));
        }
        return list;
    }

    public KeyEntry getEntry(int keyCode) {
        KeyEntry d = DEFAULTS.get(keyCode);
        if (d == null) return null;
        String single = prefs.getString(keyCode + "_single", d.singleTapAction);
        String dbl = prefs.getString(keyCode + "_double", d.doubleTapAction);
        if (keyCode == KEYCODE_NETFLIX && ACTION_DEFAULT.equals(dbl) && !prefs.getBoolean(keyCode + "_double_user_override", false)) {
            dbl = ACTION_TORCH;
        }
        String lp = prefs.getString(keyCode + "_long", d.longPressAction);
        String comp = prefs.getString(keyCode + "_comp", d.targetComponent);
        return new KeyEntry(keyCode, d.name, single, dbl, lp, comp);
    }

    public void saveEntry(int keyCode, String singleTapAction, String doubleTapAction, String longPressAction, String targetComponent) {
        prefs.edit()
                .putString(keyCode + "_single", singleTapAction)
                .putString(keyCode + "_double", doubleTapAction)
                .putString(keyCode + "_long", longPressAction)
                .putString(keyCode + "_comp", targetComponent != null ? targetComponent : "")
                .putBoolean(keyCode + "_double_user_override", true)
                .apply();
    }

    public void saveEntry(int keyCode, String singleTapAction, String longPressAction, String targetComponent) {
        saveEntry(keyCode, singleTapAction, ACTION_DEFAULT, longPressAction, targetComponent);
    }

    public void resetToDefaults() {
        prefs.edit().clear().apply();
    }

    public static String actionLabel(String action) {
        if (ACTION_NONE.equals(action)) return "No Action / Disabled (Swallow)";
        if (ACTION_RECENTS.equals(action)) return "Recents / Task Switcher";
        if (ACTION_NEXT_TRACK.equals(action)) return "Next Track";
        if (ACTION_PREV_TRACK.equals(action)) return "Previous Track";
        if (ACTION_PLAY_PAUSE.equals(action)) return "Play / Pause";
        if (ACTION_DEEZER_FLOW.equals(action)) return "Deezer Flow";
        if (ACTION_SLEEP_TIMER.equals(action)) return "Sleep TV Timer Pro";
        if (ACTION_BRIGHTNESS_SLIDER.equals(action)) return "Screen Brightness Slider";
        if (ACTION_TORCH.equals(action)) return "Screen Torch / Max White";
        if (ACTION_CUSTOM_APP.equals(action)) return "Launch Custom App";
        return "Default";
    }

    public static List<String> availableActions() {
        List<String> actions = new ArrayList<>();
        actions.add(ACTION_DEFAULT);
        actions.add(ACTION_NONE);
        actions.add(ACTION_RECENTS);
        actions.add(ACTION_NEXT_TRACK);
        actions.add(ACTION_PREV_TRACK);
        actions.add(ACTION_PLAY_PAUSE);
        actions.add(ACTION_DEEZER_FLOW);
        actions.add(ACTION_SLEEP_TIMER);
        actions.add(ACTION_BRIGHTNESS_SLIDER);
        actions.add(ACTION_TORCH);
        return actions;
    }
}
