package com.boop.alpha1;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class NamedHomeTargetResolverTest {
    @Test public void areaTargetsEveryExposedAvailableLightAndNoOtherEntity() {
        NamedHomeTargetResolver.Result result = NamedHomeTargetResolver.resolve(
                "Aquarium Lights", "aquarium", Arrays.asList(
                        "light.tank_one", "light.tank_two", "switch.filter"),
                new HashSet<>(Arrays.asList("light.tank_one", "light.tank_two", "switch.filter")),
                Arrays.asList(entity("light.tank_one", "Tank One", false, "on"),
                        entity("light.tank_two", "Tank Two", false, "off"),
                        entity("switch.filter", "Filter", false, "on")));

        assertEquals(NamedHomeTargetResolver.Kind.AREA, result.kind());
        assertEquals(Arrays.asList("light.tank_one", "light.tank_two"), result.entityIds());
        assertEquals("light", result.serviceDomain());
    }

    @Test public void knownAreaWithNoAvailableExposedLightsDoesNotFallThroughToCurrentRoom() {
        NamedHomeTargetResolver.Result result = NamedHomeTargetResolver.resolve(
                "Living Room", "living_room", Collections.singletonList("light.hidden"),
                Collections.emptySet(), Collections.singletonList(
                        entity("light.selected_room", "Living Room", true, "off")));

        assertEquals(NamedHomeTargetResolver.Kind.AREA, result.kind());
        assertTrue(result.entityIds().isEmpty());
    }

    @Test public void exactNamedGroupMatchesButFuzzyGlobalMatchesDoNot() {
        NamedHomeTargetResolver.Result exact = NamedHomeTargetResolver.resolve(
                "Aquarium Lights", "", Collections.emptyList(), Collections.emptySet(), Arrays.asList(
                        entity("light.aquarium_lights", "Aquarium Lights", true, "off"),
                        entity("light.other", "Aquarium Light", true, "off")));
        NamedHomeTargetResolver.Result fuzzy = NamedHomeTargetResolver.resolve(
                "Aquarium Lights", "", Collections.emptyList(), Collections.emptySet(),
                Collections.singletonList(entity("light.other", "Aquarium Light", true, "off")));

        assertEquals(NamedHomeTargetResolver.Kind.GROUP, exact.kind());
        assertEquals(Collections.singletonList("light.aquarium_lights"), exact.entityIds());
        assertEquals(NamedHomeTargetResolver.Kind.NONE, fuzzy.kind());
    }

    @Test public void exactHomeAssistantGroupUsesHomeassistantServiceAndAmbiguityFailsClosed() {
        NamedHomeTargetResolver.Result group = NamedHomeTargetResolver.resolve(
                "Aquarium Lights", "", Collections.emptyList(), Collections.emptySet(),
                Collections.singletonList(entity("group.aquarium_lights", "Aquarium Lights", true, "on")));
        NamedHomeTargetResolver.Result ambiguous = NamedHomeTargetResolver.resolve(
                "Aquarium Lights", "", Collections.emptyList(), Collections.emptySet(), Arrays.asList(
                        entity("light.aquarium_lights", "Aquarium Lights", true, "off"),
                        entity("group.aquarium_lights", "Aquarium Lights", true, "on")));

        assertEquals("homeassistant", group.serviceDomain());
        assertEquals(NamedHomeTargetResolver.Kind.AMBIGUOUS, ambiguous.kind());
    }

    @Test public void hiddenDisabledUnavailableAndUnexposedGroupsAreIgnored() {
        NamedHomeTargetResolver.Result result = NamedHomeTargetResolver.resolve(
                "Aquarium Lights", "", Collections.emptyList(), Collections.emptySet(), Arrays.asList(
                        entity("light.hidden", "Aquarium Lights", true, "off", true, false),
                        entity("light.disabled", "Aquarium Lights", true, "off", false, true),
                        entity("light.offline", "Aquarium Lights", true, "unavailable"),
                        entity("group.private", "Aquarium Lights", false, "on")));

        assertEquals(NamedHomeTargetResolver.Kind.NONE, result.kind());
    }

    private static HomeAssistantEntity entity(String id, String name, boolean exposed, String state) {
        return entity(id, name, exposed, state, false, false);
    }

    private static HomeAssistantEntity entity(String id, String name, boolean exposed, String state,
            boolean hidden, boolean disabled) {
        return new HomeAssistantEntity(id, name, exposed, hidden, disabled, state);
    }
}
