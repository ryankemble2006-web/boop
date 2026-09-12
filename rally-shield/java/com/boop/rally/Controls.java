package com.boop.rally;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/** Android key codes to libretro keyboard keys. No Android dependency. */
public final class Controls {
    public interface Sink { void key(int key, boolean down); }
    private final Sink sink;
    private final Map<Integer,Integer> held = new HashMap<>();
    public Controls(Sink sink) { this.sink = sink; }
    public static int mapKey(int key) {
        if (key >= 29 && key <= 54) return 97 + key - 29;
        if (key >= 7 && key <= 16) return 48 + key - 7;
        if (key >= 131 && key <= 142) return 282 + key - 131;
        switch(key) {
            case 19: case 105: return 273;
            case 20: case 104: return 274;
            case 21: return 276;
            case 22: return 275;
            case 23: case 66: case 96: return 13;
            case 97: case 111: return 27;
            case 62: case 99: return 32;
            case 100: return 282;
            case 102: return 122;
            case 103: return 97;
            case 61: return 9;
            case 67: return 8;
            case 59: return 304;
            case 60: return 303;
            case 113: return 306;
            case 114: return 305;
            case 57: return 308;
            case 58: return 307;
            default: return 0;
        }
    }
    private void source(int token, int key, boolean down) {
        Integer old = held.get(token);
        if (old != null && (!down || old != key)) {
            held.remove(token);
            if (!held.containsValue(old)) sink.key(old, false);
        }
        if (down && key > 0 && !held.containsKey(token)) {
            boolean already = held.containsValue(key);
            held.put(token, key);
            if (!already) sink.key(key, true);
        }
    }
    public boolean key(int androidKey, boolean down) {
        int key = mapKey(androidKey);
        if (key == 0) return false;
        source(androidKey + 1000, key, down);
        return true;
    }
    public void axes(float x, float y, float leftTrigger, float rightTrigger) {
        source(-1,276,x < -.3f); source(-2,275,x > .3f);
        source(-3,273,y < -.3f); source(-4,274,y > .3f);
        source(-5,274,leftTrigger > .25f); source(-6,273,rightTrigger > .25f);
    }
    public void clear() {
        HashSet<Integer> keys = new HashSet<>(held.values());
        held.clear();
        for (Integer key: keys) sink.key(key, false);
    }
}
