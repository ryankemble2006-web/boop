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

    @Test public void liveLoadPublishesRoomDevicesWithoutFavouriteSemantics() {
        EntityCard lamp = card("light.floor_lamp", "Floor lamp", "off");
        FakeRepository repository = new FakeRepository();
        repository.snapshot = new DashboardSnapshot(LOUNGE, Arrays.asList(
                new EntityCard("sensor.temperature", "living_room", "Temperature", "21", false, null), lamp));
        FakeCache cache = new FakeCache();
        AtomicReference<HomeDashboardController.ViewState> rendered = new AtomicReference<>();
        new HomeDashboardController(LOUNGE, repository, cache, rendered::set).start();
        HomeDashboardController.ViewState state = rendered.get();
        assertNotNull(state);
        assertEquals(HomeDashboardController.Status.LIVE, state.status());
        assertEquals(1, state.cards().size());
        assertEquals("light.floor_lamp", state.cards().get(0).entityId());
        assertNull(state.favourite());
        assertNull(cache.saved);
        assertTrue(state.actionsEnabled());
    }

    @Test public void crossRoomCardsAreHiddenBeforeTheyReachTheTv() {
        EntityCard loungeLamp = card("light.floor_lamp", "Floor lamp", "off");
        EntityCard bedroomLamp = new EntityCard("light.bedroom", "bedroom", "Bedroom lamp", "on", false, null);
        FakeRepository repository = new FakeRepository();
        repository.snapshot = new DashboardSnapshot(LOUNGE, Arrays.asList(loungeLamp, bedroomLamp));
        AtomicReference<HomeDashboardController.ViewState> rendered = new AtomicReference<>();
        new HomeDashboardController(LOUNGE, repository, new FakeCache(), rendered::set).start();
        assertEquals(1, rendered.get().cards().size());
        assertEquals("light.floor_lamp", rendered.get().cards().get(0).entityId());
        assertEquals("I hid controls that aren't confirmed in Living Room.", rendered.get().message());
    }

    @Test public void wrongRoomSnapshotFailsClosedInsteadOfShowingWholeHouse() {
        FakeRepository repository = new FakeRepository();
        repository.snapshot = new DashboardSnapshot(new AreaInfo("bedroom", "Bedroom"), Collections.singletonList(
                new EntityCard("light.bedroom", "bedroom", "Bedroom lamp", "on", false, null)));
        AtomicReference<HomeDashboardController.ViewState> rendered = new AtomicReference<>();
        new HomeDashboardController(LOUNGE, repository, new FakeCache(), rendered::set).start();
        assertTrue(rendered.get().cards().isEmpty());
        assertFalse(rendered.get().actionsEnabled());
        assertEquals("I couldn't confirm this room, so I hid the controls.", rendered.get().message());
    }

    @Test public void failedLoadDoesNotResurrectCachedFavourite() {
        FakeRepository repository = new FakeRepository();
        repository.loadError = "Home Assistant is offline.";
        FakeCache cache = new FakeCache();
        cache.loaded = card("switch.corner_lamp", "Corner lamp", "on");
        AtomicReference<HomeDashboardController.ViewState> rendered = new AtomicReference<>();
        new HomeDashboardController(LOUNGE, repository, cache, rendered::set).start();
        assertTrue(rendered.get().cards().isEmpty());
        assertNull(rendered.get().favourite());
        assertFalse(rendered.get().actionsEnabled());
        assertEquals("Home Assistant is offline.", rendered.get().message());
    }

    @Test public void liveDeviceTogglePublishesConfirmedFreshState() {
        EntityCard lamp = card("light.floor_lamp", "Floor lamp", "off");
        FakeRepository repository = new FakeRepository();
        repository.snapshot = new DashboardSnapshot(LOUNGE, Collections.singletonList(lamp));
        AtomicReference<HomeDashboardController.ViewState> rendered = new AtomicReference<>();
        HomeDashboardController controller = new HomeDashboardController(LOUNGE, repository, new FakeCache(), rendered::set);
        controller.start();
        rendered.get().toggle(lamp);
        assertEquals(1, repository.toggleCalls.get());
        repository.completeToggle(true, lamp.withState("on"), null);
        assertEquals("on", rendered.get().cards().get(0).state());
        assertTrue(rendered.get().actionsEnabled());
    }

    @Test public void crossRoomToggleConfirmationIsRejected() {
        EntityCard lamp = card("light.floor_lamp", "Floor lamp", "off");
        FakeRepository repository = new FakeRepository();
        repository.snapshot = new DashboardSnapshot(LOUNGE, Collections.singletonList(lamp));
        AtomicReference<HomeDashboardController.ViewState> rendered = new AtomicReference<>();
        HomeDashboardController controller = new HomeDashboardController(LOUNGE, repository, new FakeCache(), rendered::set);
        controller.start(); rendered.get().toggle(lamp);
        repository.completeToggle(true, new EntityCard(lamp.entityId(), "bedroom", lamp.displayName(), "on", false, null), null);
        assertEquals(HomeDashboardController.Status.STALE, rendered.get().status());
        assertEquals("off", rendered.get().cards().get(0).state());
        assertFalse(rendered.get().actionsEnabled());
    }

    @Test public void noSupportedDeviceProducesLiveEmptyDashboard() {
        FakeRepository repository = new FakeRepository();
        repository.snapshot = new DashboardSnapshot(LOUNGE, Collections.singletonList(
                new EntityCard("sensor.temperature", "living_room", "Temperature", "21", false, null)));
        AtomicReference<HomeDashboardController.ViewState> rendered = new AtomicReference<>();
        new HomeDashboardController(LOUNGE, repository, new FakeCache(), rendered::set).start();
        assertEquals(HomeDashboardController.Status.LIVE, rendered.get().status());
        assertTrue(rendered.get().cards().isEmpty());
        assertFalse(rendered.get().actionsEnabled());
    }

    private static EntityCard card(String entityId, String name, String state) { return new EntityCard(entityId, "living_room", name, state, false, null); }
    private static final class FakeRepository implements HomeDashboardController.RepositoryPort {
        private DashboardSnapshot snapshot; private String loadError; private HomeAssistantRepository.BinaryActionCallback pendingToggle; private final AtomicInteger toggleCalls = new AtomicInteger();
        @Override public void loadDashboard(AreaInfo room, HomeAssistantRepository.DashboardCallback callback) { callback.onResult(snapshot, loadError); }
        @Override public void toggleBinary(EntityCard card, HomeAssistantRepository.BinaryActionCallback callback) { toggleCalls.incrementAndGet(); pendingToggle = callback; }
        private void completeToggle(boolean success, EntityCard card, String error) { HomeAssistantRepository.BinaryActionCallback callback = pendingToggle; pendingToggle = null; callback.onResult(success, card, error); }
    }
    private static final class FakeCache implements HomeDashboardController.CachePort {
        private EntityCard loaded; private EntityCard saved;
        @Override public EntityCard load(AreaInfo room) { return loaded; }
        @Override public void save(AreaInfo room, EntityCard card) { saved = card; }
        @Override public void clear(AreaInfo room) { loaded = null; saved = null; }
    }
}
