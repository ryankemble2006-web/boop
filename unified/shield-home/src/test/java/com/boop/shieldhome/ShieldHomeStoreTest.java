package com.boop.shieldhome;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public final class ShieldHomeStoreTest {
    private static final class MemoryPreferences implements ShieldHomeStore.Preferences {
        private final Map<String, String> strings = new HashMap<>();
        private final Map<String, Boolean> booleans = new HashMap<>();

        @Override public String getString(String key, String fallback) {
            return strings.getOrDefault(key, fallback);
        }

        @Override public boolean getBoolean(String key, boolean fallback) {
            return booleans.getOrDefault(key, fallback);
        }

        @Override public void putString(String key, String value) {
            strings.put(key, value);
        }

        @Override public void putBoolean(String key, boolean value) {
            booleans.put(key, value);
        }
    }

    @Test public void favouritesRoundTripAsJson() {
        String raw = ShieldHomeStore.encodeComponents(List.of("a/.A", "b/.B"));
        assertEquals(List.of("a/.A", "b/.B"), ShieldHomeStore.decodeComponents(raw));
    }

    @Test public void malformedFavouriteDataFailsSafeToEmpty() {
        assertEquals(List.of(), ShieldHomeStore.decodeComponents("not-json"));
        assertEquals(List.of(), ShieldHomeStore.decodeComponents(null));
    }

    @Test public void firstLoadSeedsInstalledAppsOnlyOnce() {
        MemoryPreferences preferences = new MemoryPreferences();
        ShieldHomeStore store = new ShieldHomeStore(preferences);

        List<TvAppEntry> firstInstalled = List.of(
                new TvAppEntry("a/.A", "a", "A"),
                new TvAppEntry("b/.B", "b", "B"));
        assertEquals(List.of("a/.A", "b/.B"), store.loadOrSeedFavourites(firstInstalled));
        assertTrue(preferences.getBoolean("favourites_initialised", false));

        List<TvAppEntry> laterInstalled = List.of(
                new TvAppEntry("a/.A", "a", "A"),
                new TvAppEntry("b/.B", "b", "B"),
                new TvAppEntry("c/.C", "c", "C"));
        assertEquals(List.of("a/.A", "b/.B"), store.loadOrSeedFavourites(laterInstalled));
    }

    @Test public void intentionallyEmptyFavouritesRemainEmptyAfterInitialisation() {
        MemoryPreferences preferences = new MemoryPreferences();
        preferences.putBoolean("favourites_initialised", true);
        preferences.putString("favourites_json", "[]");
        ShieldHomeStore store = new ShieldHomeStore(preferences);

        assertEquals(List.of(), store.loadOrSeedFavourites(
                List.of(new TvAppEntry("a/.A", "a", "A"))));
    }

    @Test public void optionalRowsDefaultOffAndPersistIndependently() {
        MemoryPreferences preferences = new MemoryPreferences();
        ShieldHomeStore store = new ShieldHomeStore(preferences);

        assertFalse(store.rowEnabled(OptionalRowRegistry.Key.PLAY_NEXT));
        assertFalse(store.rowEnabled(OptionalRowRegistry.Key.APP_CHANNELS));

        store.setRowEnabled(OptionalRowRegistry.Key.PLAY_NEXT, true);
        assertTrue(store.rowEnabled(OptionalRowRegistry.Key.PLAY_NEXT));
        assertFalse(store.rowEnabled(OptionalRowRegistry.Key.APP_CHANNELS));

        store.setRowEnabled(OptionalRowRegistry.Key.APP_CHANNELS, true);
        assertTrue(store.rowEnabled(OptionalRowRegistry.Key.PLAY_NEXT));
        assertTrue(store.rowEnabled(OptionalRowRegistry.Key.APP_CHANNELS));
    }

    @Test public void preferredNowPlayingPlayerDefaultsAutomaticAndPersists() {
        MemoryPreferences preferences = new MemoryPreferences();
        ShieldHomeStore store = new ShieldHomeStore(preferences);

        assertEquals("", store.nowPlayingPlayerPackage());
        store.setNowPlayingPlayerPackage("deezer.android.app");
        assertEquals("deezer.android.app", store.nowPlayingPlayerPackage());
        store.setNowPlayingPlayerPackage(null);
        assertEquals("", store.nowPlayingPlayerPackage());
    }
}
