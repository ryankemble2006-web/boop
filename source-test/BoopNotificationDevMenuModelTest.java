package com.boop.alpha1;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class BoopNotificationDevMenuModelTest {
    @Test
    public void exposesTwoShelvesWithApprovedV70Actions() {
        List<BoopDevMenuModel.Shelf> shelves = BoopDevMenuModel.shelves();

        assertEquals(2, shelves.size());
        assertEquals("Animations", shelves.get(0).title());
        assertEquals(
                List.of(
                        BoopDevMenuModel.Action.WAKE,
                        BoopDevMenuModel.Action.THINK,
                        BoopDevMenuModel.Action.STOP,
                        BoopDevMenuModel.Action.BERRY_1,
                        BoopDevMenuModel.Action.BERRY_2,
                        BoopDevMenuModel.Action.BERRY_3,
                        BoopDevMenuModel.Action.SHAKE,
                        BoopDevMenuModel.Action.SLEEP),
                actions(shelves.get(0)));

        assertEquals("Notification demos", shelves.get(1).title());
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
                actions(shelves.get(1)));
    }

    private static List<BoopDevMenuModel.Action> actions(BoopDevMenuModel.Shelf shelf) {
        return shelf.items().stream()
                .map(BoopDevMenuModel.Item::action)
                .collect(Collectors.toList());
    }
}
