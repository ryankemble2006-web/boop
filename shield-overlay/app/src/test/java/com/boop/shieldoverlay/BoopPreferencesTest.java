package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public final class BoopPreferencesTest {
    @Test
    public void selectedRoomRoundTripsAsShieldLocalPreference() {
        FakeStore store = new FakeStore();
        BoopPreferences preferences = new BoopPreferences(store);

        assertFalse(preferences.hasSelectedRoom());
        assertNull(preferences.selectedRoom());

        preferences.setSelectedRoom(new AreaInfo("living_room", "Living Room"));

        assertTrue(preferences.hasSelectedRoom());
        AreaInfo selected = preferences.selectedRoom();
        assertEquals("living_room", selected.id());
        assertEquals("Living Room", selected.name());

        preferences.clearSelectedRoom();
        assertFalse(preferences.hasSelectedRoom());
        assertNull(preferences.selectedRoom());
    }

    @Test
    public void cachedFavouriteRoundTripsOnlyForTheMatchingRoom() {
        FakeStore store = new FakeStore();
        BoopPreferences preferences = new BoopPreferences(store);
        AreaInfo livingRoom = new AreaInfo("living_room", "Living Room");
        EntityCard lamp = new EntityCard(
                "light.floor_lamp",
                "living_room",
                "Floor lamp",
                "on",
                false,
                null);

        preferences.setCachedFavourite(livingRoom, lamp);

        EntityCard cached = preferences.cachedFavourite(livingRoom);
        assertEquals("light.floor_lamp", cached.entityId());
        assertEquals("living_room", cached.areaId());
        assertEquals("Floor lamp", cached.displayName());
        assertEquals("on", cached.state());
        assertNull(preferences.cachedFavourite(new AreaInfo("bedroom", "Bedroom")));
    }

    @Test
    public void clearingCachedFavouriteRemovesLastKnownControl() {
        FakeStore store = new FakeStore();
        BoopPreferences preferences = new BoopPreferences(store);
        AreaInfo livingRoom = new AreaInfo("living_room", "Living Room");
        preferences.setCachedFavourite(
                livingRoom,
                new EntityCard(
                        "switch.corner_lamp",
                        "living_room",
                        "Corner lamp",
                        "off",
                        false,
                        null));

        preferences.clearCachedFavourite(livingRoom);

        assertNull(preferences.cachedFavourite(livingRoom));
    }

    private static final class FakeStore implements BoopPreferences.Store {
        private final Map<String, String> values = new HashMap<>();

        @Override
        public String getString(String key) {
            return values.get(key);
        }

        @Override
        public void putString(String key, String value) {
            values.put(key, value);
        }

        @Override
        public void remove(String key) {
            values.remove(key);
        }
    }
}
