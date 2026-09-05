package com.boop.shieldoverlay;

import android.content.Context;
import android.content.SharedPreferences;

public final class BoopPreferences {
    private static final String PREFS_NAME = "boop_home";
    private static final String KEY_AREA_ID = "selected_area_id_v1";
    private static final String KEY_AREA_NAME = "selected_area_name_v1";
    private static final String KEY_FAVOURITE_AREA_ID = "favourite_area_id_v1";
    private static final String KEY_FAVOURITE_ENTITY_ID = "favourite_entity_id_v1";
    private static final String KEY_FAVOURITE_NAME = "favourite_name_v1";
    private static final String KEY_FAVOURITE_STATE = "favourite_state_v1";

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

    public EntityCard cachedFavourite(AreaInfo room) {
        if (room == null) {
            throw new IllegalArgumentException("room is required");
        }
        String areaId = clean(store.getString(KEY_FAVOURITE_AREA_ID));
        if (!room.id().equals(areaId)) {
            return null;
        }
        String entityId = clean(store.getString(KEY_FAVOURITE_ENTITY_ID));
        String name = clean(store.getString(KEY_FAVOURITE_NAME));
        String state = clean(store.getString(KEY_FAVOURITE_STATE));
        if (entityId == null || name == null || (!"on".equals(state) && !"off".equals(state))) {
            return null;
        }
        return new EntityCard(entityId, room.id(), name, state, false, null);
    }

    public void setCachedFavourite(AreaInfo room, EntityCard card) {
        if (room == null) {
            throw new IllegalArgumentException("room is required");
        }
        if (card == null) {
            throw new IllegalArgumentException("card is required");
        }
        if (!"on".equals(card.state()) && !"off".equals(card.state())) {
            throw new IllegalArgumentException("cached favourite must have an on/off state");
        }
        store.putString(KEY_FAVOURITE_AREA_ID, room.id());
        store.putString(KEY_FAVOURITE_ENTITY_ID, card.entityId());
        store.putString(KEY_FAVOURITE_NAME, card.displayName());
        store.putString(KEY_FAVOURITE_STATE, card.state());
    }

    public void clearCachedFavourite(AreaInfo room) {
        if (room == null) {
            throw new IllegalArgumentException("room is required");
        }
        String storedAreaId = clean(store.getString(KEY_FAVOURITE_AREA_ID));
        if (storedAreaId != null && !room.id().equals(storedAreaId)) {
            return;
        }
        store.remove(KEY_FAVOURITE_AREA_ID);
        store.remove(KEY_FAVOURITE_ENTITY_ID);
        store.remove(KEY_FAVOURITE_NAME);
        store.remove(KEY_FAVOURITE_STATE);
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
