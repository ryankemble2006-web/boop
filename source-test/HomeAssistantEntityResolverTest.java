package com.boop.alpha1;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class HomeAssistantEntityResolverTest {
    @Test public void partialWordsDoNotSelectAnotherDevice() {
        assertNull(HomeAssistantEntityResolver.resolve(new GenericHomeCommand("lamp", "turn_on", false),
                Arrays.asList(entity("switch.clamp", "Desk Clamp", true, false, false, "off"))));
    }

    @Test public void wholeWordPhrasesStillMatch() {
        assertEquals("light.desk", HomeAssistantEntityResolver.resolve(
                new GenericHomeCommand("desk lamp", "turn_on", false),
                Arrays.asList(entity("light.desk", "Office Desk Lamp", true, false, false, "off"))).entityId());
    }

    @Test public void emptyNormalizedNamesCannotMatchEverything() {
        assertNull(HomeAssistantEntityResolver.resolve(new GenericHomeCommand("???", "turn_on", false),
                Arrays.asList(entity("light.desk", "Desk Lamp", true, false, false, "off"))));
        assertNull(HomeAssistantEntityResolver.resolve(new GenericHomeCommand("desk lamp", "turn_on", false),
                Arrays.asList(entity("light.empty", "", true, false, false, "off"))));
    }

    @Test public void nonLatinNamesRemainDistinct() {
        assertEquals("light.two", HomeAssistantEntityResolver.resolve(
                new GenericHomeCommand("卧室灯", "turn_on", false), Arrays.asList(
                entity("light.one", "厨房灯", true, false, false, "off"),
                entity("light.two", "卧室灯", true, false, false, "off"))).entityId());
    }

    @Test public void selectsUniqueVisibleAvailableRoomEntity() {
        GenericHomeCommand c = new GenericHomeCommand("floor lamp", "turn_on", false);
        HomeAssistantEntity e = HomeAssistantEntityResolver.resolve(c, Arrays.asList(
                entity("light.floor_lamp", "Floor Lamp", true, false, false, "off"),
                entity("light.floor_lamp_hidden", "Floor Lamp", true, true, false, "off")));
        assertEquals("light.floor_lamp", e.entityId());
    }

    @Test public void tiesFailClosed() {
        GenericHomeCommand c = new GenericHomeCommand("lamp", "turn_off", false);
        assertNull(HomeAssistantEntityResolver.resolve(c, Arrays.asList(
                entity("light.one", "Table Lamp", true, false, false, "on"),
                entity("switch.two", "Lamp Plug", true, false, false, "on"))));
    }

    @Test public void tiesAreDistinguishedFromNoMatch() {
        GenericHomeCommand c = new GenericHomeCommand("lamp", "turn_off", false);
        HomeAssistantEntityResolver.Result result = HomeAssistantEntityResolver.resolveResult(c, Arrays.asList(
                entity("light.one", "Table Lamp", true, false, false, "on"),
                entity("switch.two", "Lamp Plug", true, false, false, "on")));
        assertEquals(HomeAssistantEntityResolver.Kind.AMBIGUOUS, result.kind());
    }

    @Test public void unavailableAndUnexposedEntitiesAreNotSelected() {
        GenericHomeCommand c = new GenericHomeCommand("fan", "turn_on", false);
        assertNull(HomeAssistantEntityResolver.resolve(c, Arrays.asList(
                entity("fan.room", "Fan", false, false, false, "off"),
                entity("fan.dead", "Fan", true, false, false, "unavailable"))));
    }

    private static HomeAssistantEntity entity(String id, String name, boolean exposed,
            boolean hidden, boolean disabled, String state) {
        return new HomeAssistantEntity(id, name, exposed, hidden, disabled, state);
    }
}
