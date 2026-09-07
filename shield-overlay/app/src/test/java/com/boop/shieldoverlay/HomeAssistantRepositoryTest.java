package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public final class HomeAssistantRepositoryTest {
    @Test
    public void areaRegistryBecomesCleanAlphabeticalRoomCards() throws Exception {
        FakeCommandPort commands = new FakeCommandPort();
        HomeAssistantRepository repository = new HomeAssistantRepository(commands);

        AtomicReference<List<AreaInfo>> result = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();
        repository.loadAreas((areas, message) -> {
            result.set(areas);
            error.set(message);
        });

        assertEquals("config/area_registry/list", commands.lastType);
        assertNotNull(commands.callback);

        JSONArray payload = new JSONArray()
                .put(new JSONObject().put("area_id", "bedroom").put("name", "Bedroom"))
                .put(new JSONObject().put("area_id", "living_room").put("name", "Living Room"))
                .put(new JSONObject().put("area_id", "kitchen").put("name", "Kitchen"))
                .put(new JSONObject().put("area_id", "bad").put("name", "  "));
        commands.callback.onResult(true, payload, null);

        assertNull(error.get());
        assertNotNull(result.get());
        assertEquals(3, result.get().size());
        assertEquals("Bedroom", result.get().get(0).name());
        assertEquals("Kitchen", result.get().get(1).name());
        assertEquals("Living Room", result.get().get(2).name());
        assertEquals("living_room", result.get().get(2).id());
    }

    @Test
    public void failedOrMalformedAreaRequestIsPlainFailureNotEmptySuccess() throws Exception {
        FakeCommandPort commands = new FakeCommandPort();
        HomeAssistantRepository repository = new HomeAssistantRepository(commands);

        AtomicReference<List<AreaInfo>> result = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();
        repository.loadAreas((areas, message) -> {
            result.set(areas);
            error.set(message);
        });

        commands.callback.onResult(false, null, "not allowed");
        assertNull(result.get());
        assertNotNull(error.get());
    }

    private static final class FakeCommandPort implements HomeAssistantRepository.CommandPort {
        String lastType;
        HomeAssistantWebSocket.Callback callback;

        @Override
        public void send(String type, JSONObject body, HomeAssistantWebSocket.Callback callback) {
            this.lastType = type;
            this.callback = callback;
        }
    }
}
