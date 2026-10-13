package com.boop.alpha1;

import android.content.Context;
import android.content.SharedPreferences;

/** Separate from house credentials, voice settings and any browser login. */
final class BoopChatModeStore {
    private final SharedPreferences preferences;
    BoopChatModeStore(Context context) {
        preferences = context.getSharedPreferences("boop_chat_mode", Context.MODE_PRIVATE);
    }
    BoopChatMode load() {
        return BoopChatMode.fromStored(preferences.getString("mode", null));
    }
    boolean save(BoopChatMode mode) {
        // A tiny, explicit user preference: confirm it is saved before reporting success.
        return preferences.edit().putString("mode", mode.storedValue()).commit();
    }
}
