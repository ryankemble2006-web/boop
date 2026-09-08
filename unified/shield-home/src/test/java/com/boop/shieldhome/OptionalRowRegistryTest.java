package com.boop.shieldhome;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public final class OptionalRowRegistryTest {
    @Test public void disabledRowsCreateNoProviders() {
        AtomicInteger created = new AtomicInteger();
        List<HomeRowProvider> providers = OptionalRowRegistry.loadEnabled(
                key -> false,
                key -> {
                    created.incrementAndGet();
                    return List::of;
                });

        assertTrue(providers.isEmpty());
        assertEquals(0, created.get());
    }

    @Test public void onlyEnabledProviderIsConstructed() {
        List<OptionalRowRegistry.Key> created = new ArrayList<>();
        List<HomeRowProvider> providers = OptionalRowRegistry.loadEnabled(
                key -> key == OptionalRowRegistry.Key.APP_CHANNELS,
                key -> {
                    created.add(key);
                    return List::of;
                });

        assertEquals(1, providers.size());
        assertEquals(List.of(OptionalRowRegistry.Key.APP_CHANNELS), created);
    }

    @Test public void preferenceKeysAreStableAndNonPromotional() {
        assertEquals("row_play_next", OptionalRowRegistry.Key.PLAY_NEXT.preferenceKey());
        assertEquals("row_app_channels", OptionalRowRegistry.Key.APP_CHANNELS.preferenceKey());
        assertEquals(2, OptionalRowRegistry.Key.values().length);
    }
}
