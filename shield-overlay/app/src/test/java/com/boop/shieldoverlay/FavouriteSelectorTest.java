package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public final class FavouriteSelectorTest {
    @Test
    public void selectedRoomWins() {
        FavouriteSelector selector = new FavouriteSelector();
        EntityCard chosen = selector.select(
                "living_room",
                Arrays.asList(
                        card("light.bedside", "bedroom", "Bedside lamp", "off", false, null),
                        card("light.sofa", "living_room", "Sofa lamp", "off", false, null)));

        assertEquals("light.sofa", chosen.entityId());
    }

    @Test
    public void sensorsNeverBecomeFavouriteControls() {
        FavouriteSelector selector = new FavouriteSelector();
        EntityCard chosen = selector.select(
                "living_room",
                Collections.singletonList(
                        card("sensor.temperature", "living_room", "Temperature", "21.4", false, null)));

        assertNull(chosen);
    }

    @Test
    public void hiddenOrDiagnosticEntitiesAreExcluded() {
        FavouriteSelector selector = new FavouriteSelector();
        EntityCard chosen = selector.select(
                "living_room",
                Arrays.asList(
                        card("light.hidden", "living_room", "Hidden light", "off", true, null),
                        card("switch.config", "living_room", "Config switch", "off", false, "config"),
                        card("fan.diagnostic", "living_room", "Diagnostic fan", "off", false, "diagnostic"),
                        card("input_boolean.movie_mode", "living_room", "Movie mode", "off", false, null)));

        assertEquals("input_boolean.movie_mode", chosen.entityId());
    }

    private static EntityCard card(
            String entityId,
            String areaId,
            String name,
            String state,
            boolean hidden,
            String category) {
        return new EntityCard(entityId, areaId, name, state, hidden, category);
    }
}
