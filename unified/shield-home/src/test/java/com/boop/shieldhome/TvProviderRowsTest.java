package com.boop.shieldhome;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public final class TvProviderRowsTest {
    @Test public void watchNextProviderDoesNoWorkUntilLoaded() {
        AtomicInteger watchLoads = new AtomicInteger();
        HomeContentCard card = new HomeContentCard(
                "Episode 4", "app://play/4", "content://poster/4");
        TvProviderRows.QuerySource source = new TvProviderRows.QuerySource() {
            @Override public List<HomeContentCard> loadWatchNextCards() {
                watchLoads.incrementAndGet();
                return List.of(card);
            }

            @Override public List<HomeRow> loadAppChannelRows() {
                throw new AssertionError("App channels should not be touched");
            }
        };

        HomeRowProvider provider = TvProviderRows.factory(source)
                .create(OptionalRowRegistry.Key.PLAY_NEXT);
        assertEquals(0, watchLoads.get());

        List<HomeRow> rows = provider.load();
        assertEquals(1, watchLoads.get());
        assertEquals(1, rows.size());
        assertEquals("Play Next", rows.get(0).title());
        assertEquals(1, rows.get(0).cards().size());
        assertEquals("Episode 4", rows.get(0).cards().get(0).title());
        assertEquals("app://play/4", rows.get(0).cards().get(0).intentUri());
        assertEquals("content://poster/4", rows.get(0).cards().get(0).posterArtUri());
    }

    @Test public void emptyWatchNextCreatesNoBlankRow() {
        TvProviderRows.QuerySource source = new TvProviderRows.QuerySource() {
            @Override public List<HomeContentCard> loadWatchNextCards() { return List.of(); }
            @Override public List<HomeRow> loadAppChannelRows() { return List.of(); }
        };

        assertTrue(TvProviderRows.factory(source)
                .create(OptionalRowRegistry.Key.PLAY_NEXT)
                .load().isEmpty());
    }

    @Test public void appChannelsProviderIsLazy() {
        AtomicInteger channelLoads = new AtomicInteger();
        HomeRow expected = new HomeRow("BBC", List.of(
                new HomeContentCard("News", "app://bbc/news", "")));
        TvProviderRows.QuerySource source = new TvProviderRows.QuerySource() {
            @Override public List<HomeContentCard> loadWatchNextCards() {
                throw new AssertionError("Watch Next should not be touched");
            }

            @Override public List<HomeRow> loadAppChannelRows() {
                channelLoads.incrementAndGet();
                return List.of(expected);
            }
        };

        HomeRowProvider provider = TvProviderRows.factory(source)
                .create(OptionalRowRegistry.Key.APP_CHANNELS);
        assertEquals(0, channelLoads.get());
        assertEquals(List.of(expected), provider.load());
        assertEquals(1, channelLoads.get());
    }

    @Test public void optionalProviderFailureFailsClosed() {
        TvProviderRows.QuerySource source = new TvProviderRows.QuerySource() {
            @Override public List<HomeContentCard> loadWatchNextCards() {
                throw new IllegalStateException("provider gone");
            }
            @Override public List<HomeRow> loadAppChannelRows() {
                throw new IllegalStateException("provider gone");
            }
        };

        assertTrue(TvProviderRows.factory(source)
                .create(OptionalRowRegistry.Key.PLAY_NEXT).load().isEmpty());
        assertTrue(TvProviderRows.factory(source)
                .create(OptionalRowRegistry.Key.APP_CHANNELS).load().isEmpty());
    }
}
