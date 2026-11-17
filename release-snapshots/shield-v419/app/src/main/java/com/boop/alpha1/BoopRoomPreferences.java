package com.boop.alpha1;

import android.content.Context;
import android.content.SharedPreferences;

final class BoopRoomPreferences implements BoopRoomSource {
    static final String PREFERENCES = "boop_unified";
    static final String KEY_ROOM_ID = "room_id";
    static final String KEY_ROOM_NAME = "room_name";

    private final SharedPreferences preferences;

    BoopRoomPreferences(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override public BoopRoom currentRoom() {
        return new BoopRoom(
                preferences.getString(KEY_ROOM_ID, BoopRoom.DEFAULT_ID),
                preferences.getString(KEY_ROOM_NAME, BoopRoom.DEFAULT_NAME));
    }
}
