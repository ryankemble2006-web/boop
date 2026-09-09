package com.boop.alpha1;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class BoopNotificationDevMenuModelTest {
    @Test
    public void exposesTwoShelvesWithApprovedDemoActions() {
        List<BoopDevMenuModel.Shelf> shelves = BoopDevMenuModel.shelves();

        assertEquals(2, shelves.size());
        assertEquals("Animations", shelves.get(0).title());
        assertEquals(
                List.of(
                        BoopDevMenuModel.Action.WAKE,
                        BoopDevMenuModel.Action.THINK,
                        BoopDevMenuModel.Action.BERRY,
                        BoopDevMenuModel.Action.SHAKE,
                        BoopDevMenuModel.Action.SLEEP),
                actions(shelves.get(0)));

        assertEquals("Notification demos", shelves.get(1).title());
        assertEquals(
                List.of(
                        BoopDevMenuModel.Action.NOTIFICATION_UNLOCKED,
                        BoopDevMenuModel.Action.NOTIFICATION_LOCKED,
                        BoopDevMenuModel.Action.NOTIFICATION_BUNDLE),
                actions(shelves.get(1)));
    }

    private static List<BoopDevMenuModel.Action> actions(BoopDevMenuModel.Shelf shelf) {
        return shelf.items().stream()
                .map(BoopDevMenuModel.Item::action)
                .collect(Collectors.toList());
    }
}
