package com.boop.shieldoverlay;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class HomeDashboardControllerTest {
    private static final AreaInfo LOUNGE = new AreaInfo("living_room", "Living Room");

    @Test
    public void liveLoadPublishesFavouriteAndCachesIt() {
        EntityCard lamp = card("light.floor_lamp", "Floor lamp", "off");
        FakeRepository repository = new FakeRepository();
        repository.snapshot = new DashboardSnapshot(
                LOUNGE,
                Arrays.asList(
                        new EntityCard("sensor.temperature", "living_room", "Temperature", "21", false, null),
                        lamp));
        FakeCache cache = new FakeCache();
        AtomicReference<HomeDashboardController.ViewState> rendered = new AtomicReference<>();

        HomeDashboardController controller = new HomeDashboardController(
                LOUNGE,
                repository,
                cache,
                rendered::set);
        controller.start();

        HomeDashboardController.ViewState state = rendered.get();
        assertNotNull(state);
        assertEquals(HomeDashboardController.Status.LIVE, state.status());
        assertEquals("light.floor_lamp", state.favourite().entityId());
        assertFalse(state.stale());
        assertTrue(state.actionsEnabled());
        assertEquals("light.floor_lamp", cache.saved.entityId());
    }

    @Test
    public void failedLoadKeepsCachedFavouriteVisibleButDisablesActions() {
        FakeRepository repository = new FakeRepository();
        repository.loadError = "Home Assistant is offline.";
        FakeCache cache = new FakeCache();
        cache.loaded = card("switch.corner_lamp", "Corner lamp", "on");
        AtomicReference<HomeDashboardController.ViewState> rendered = new AtomicReference<>();

        HomeDashboardController controller = new HomeDashboardController(
                LOUNGE,
                repository,
                cache,
                rendered::set);
        controller.start();

        HomeDashboardController.ViewState state = rendered.get();
        assertEquals(HomeDashboardController.Status.STALE, state.status());
        assertEquals("switch.corner_lamp", state.favourite().entityId());
        assertTrue(state.stale());
        assertFalse(state.actionsEnabled());
        assertEquals("Home Assistant is offline.", state.message());
    }

    @Test
    public void staleFavouriteNeverCallsHomeAssistant() {
        FakeRepository repository = new FakeRepository();
        repository.loadError = "Offline";
        FakeCache cache = new FakeCache();
        cache.loaded = card("fan.lounge", "Lounge fan", "off");
        AtomicReference<HomeDashboardController.ViewState> rendered = new AtomicReference<>();

        HomeDashboardController controller = new HomeDashboardController(
                LOUNGE,
                repository,
                cache,
                rendered::set);
        controller.start();
        controller.toggleFavourite();

        assertEquals(0, repository.toggleCalls.get());
        assertEquals(HomeDashboardController.Status.STALE, rendered.get().status());
        assertFalse(rendered.get().actionsEnabled());
    }

    @Test
    public void liveToggleOnlyPublishesConfirmedFreshState() {
        EntityCard lamp = card("light.floor_lamp", "Floor lamp", "off");
        FakeRepository repository = new FakeRepository();
        repository.snapshot = new DashboardSnapshot(LOUNGE, Collections.singletonList(lamp));
        FakeCache cache = new FakeCache();
        AtomicReference<HomeDashboardController.ViewState> rendered = new AtomicReference<>();

        HomeDashboardController controller = new HomeDashboardController(
                LOUNGE,
                repository,
                cache,
                rendered::set);
        controller.start();
        controller.toggleFavourite();

        assertEquals(1, repository.toggleCalls.get());
        assertEquals("off", rendered.get().favourite().state());

        repository.completeToggle(true, lamp.withState("on"), null);

        assertEquals("on", rendered.get().favourite().state());
        assertEquals("on", cache.saved.state());
        assertTrue(rendered.get().actionsEnabled());
    }

    @Test
    public void failedToggleMarksExistingCardStaleInsteadOfPretendingSuccess() {
        EntityCard lamp = card("light.floor_lamp", "Floor lamp", "off");
        FakeRepository repository = new FakeRepository();
        repository.snapshot = new DashboardSnapshot(LOUNGE, Collections.singletonList(lamp));
        FakeCache cache = new FakeCache();
        AtomicReference<HomeDashboardController.ViewState> rendered = new AtomicReference<>();

        HomeDashboardController controller = new HomeDashboardController(
                LOUNGE,
                repository,
                cache,
                rendered::set);
        controller.start();
        controller.toggleFavourite();
        repository.completeToggle(false, null, "Home Assistant didn't do that.");

        HomeDashboardController.ViewState state = rendered.get();
        assertEquals("off", state.favourite().state());
        assertEquals(HomeDashboardController.Status.STALE, state.status());
        assertFalse(state.actionsEnabled());
        assertEquals("Home Assistant didn't do that.", state.message());
    }

    @Test
    public void noSupportedFavouriteProducesLiveEmptyDashboard() {
        FakeRepository repository = new FakeRepository();
        repository.snapshot = new DashboardSnapshot(LOUNGE, Collections.singletonList(
                new EntityCard("sensor.temperature", "living_room", "Temperature", "21", false, null)));
        FakeCache cache = new FakeCache();
        AtomicReference<HomeDashboardController.ViewState> rendered = new AtomicReference<>();

        new HomeDashboardController(LOUNGE, repository, cache, rendered::set).start();

        assertEquals(HomeDashboardController.Status.LIVE, rendered.get().status());
        assertNull(rendered.get().favourite());
        assertFalse(rendered.get().actionsEnabled());
    }

    private static EntityCard card(String entityId, String name, String state) {
        return new EntityCard(entityId, "living_room", name, state, false, null);
    }

    private static final class FakeRepository implements HomeDashboardController.RepositoryPort {
        private DashboardSnapshot snapshot;
        private String loadError;
        private HomeAssistantRepository.BinaryActionCallback pendingToggle;
        private final AtomicInteger toggleCalls = new AtomicInteger();

        @Override
        public void loadDashboard(AreaInfo room, HomeAssistantRepository.DashboardCallback callback) {
            callback.onResult(snapshot, loadError);
        }

        @Override
        public void toggleBinary(EntityCard card, HomeAssistantRepository.BinaryActionCallback callback) {
            toggleCalls.incrementAndGet();
            pendingToggle = callback;
        }

        private void completeToggle(boolean success, EntityCard card, String error) {
            HomeAssistantRepository.BinaryActionCallback callback = pendingToggle;
            pendingToggle = null;
            callback.onResult(success, card, error);
        }
    }

    private static final class FakeCache implements HomeDashboardController.CachePort {
        private EntityCard loaded;
        private EntityCard saved;

        @Override
        public EntityCard load(AreaInfo room) {
            return loaded;
        }

        @Override
        public void save(AreaInfo room, EntityCard card) {
            saved = card;
        }

        @Override
        public void clear(AreaInfo room) {
            loaded = null;
            saved = null;
        }
    }
}
