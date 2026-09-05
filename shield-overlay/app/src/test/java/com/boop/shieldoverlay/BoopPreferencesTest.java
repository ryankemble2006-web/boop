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
