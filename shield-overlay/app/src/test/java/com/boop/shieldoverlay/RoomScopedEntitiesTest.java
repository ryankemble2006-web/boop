package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public final class RoomScopedEntitiesTest {
    private static final AreaInfo LOUNGE = new AreaInfo("living_room", "Living Room");

    @Test public void onlyCardsConfirmedInAssignedRoomSurvive() {
        EntityCard loungeLamp = new EntityCard(
                "light.lounge", "living_room", "Lounge lamp", "on", false, null);
        EntityCard bedroomLamp = new EntityCard(
                "light.bedroom", "bedroom", "Bedroom lamp", "off", false, null);

        List<EntityCard> scoped = RoomScopedEntities.keep(
                LOUNGE,
                Arrays.asList(loungeLamp, bedroomLamp));

        assertEquals(1, scoped.size());
        assertEquals("light.lounge", scoped.get(0).entityId());
        assertTrue(RoomScopedEntities.belongsTo(LOUNGE, loungeLamp));
        assertFalse(RoomScopedEntities.belongsTo(LOUNGE, bedroomLamp));
    }
}
