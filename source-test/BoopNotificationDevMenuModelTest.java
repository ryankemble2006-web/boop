package com.boop.alpha1;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class BoopNotificationDevMenuModelTest {
    @Test
    public void exposesNotificationShelfWhileAnimationsComeFromCanonicalCatalogue() {
        List<BoopDevMenuModel.Shelf> shelves = BoopDevMenuModel.shelves();
        assertEquals(1, shelves.size());
        assertEquals("Notification demos", shelves.get(0).title());
        assertEquals(
                List.of(
                        BoopDevMenuModel.Action.NOTIFICATION_FACEBOOK,
                        BoopDevMenuModel.Action.NOTIFICATION_WHATSAPP,
                        BoopDevMenuModel.Action.NOTIFICATION_GMAIL,
                        BoopDevMenuModel.Action.NOTIFICATION_X,
                        BoopDevMenuModel.Action.NOTIFICATION_YOUTUBE,
                        BoopDevMenuModel.Action.NOTIFICATION_MESSENGER,
                        BoopDevMenuModel.Action.NOTIFICATION_INSTAGRAM,
                        BoopDevMenuModel.Action.NOTIFICATION_DISCORD,
                        BoopDevMenuModel.Action.NOTIFICATION_SPOTIFY,
                        BoopDevMenuModel.Action.NOTIFICATION_REDDIT,
                        BoopDevMenuModel.Action.NOTIFICATION_LOCKED,
                        BoopDevMenuModel.Action.NOTIFICATION_BUNDLE),
                actions(shelves.get(0)));
    }

    private static List<BoopDevMenuModel.Action> actions(BoopDevMenuModel.Shelf shelf) {
        return shelf.items().stream()
                .map(BoopDevMenuModel.Item::action)
                .collect(Collectors.toList());
    }
}
