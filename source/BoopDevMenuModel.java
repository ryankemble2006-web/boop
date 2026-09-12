package com.boop.alpha1;

import java.util.List;

final class BoopDevMenuModel {
    enum Action {
        WAKE,
        THINK,
        STOP,
        BERRY_1,
        BERRY_2,
        BERRY_3,
        SHAKE,
        SLEEP,
        NOTIFICATION_FACEBOOK,
        NOTIFICATION_WHATSAPP,
        NOTIFICATION_GMAIL,
        NOTIFICATION_X,
        NOTIFICATION_YOUTUBE,
        NOTIFICATION_MESSENGER,
        NOTIFICATION_INSTAGRAM,
        NOTIFICATION_DISCORD,
        NOTIFICATION_SPOTIFY,
        NOTIFICATION_REDDIT,
        NOTIFICATION_LOCKED,
        NOTIFICATION_BUNDLE
    }

    static final class Item {
        private final String label;
        private final Action action;

        Item(String label, Action action) {
            this.label = label;
            this.action = action;
        }

        String label() { return label; }
        Action action() { return action; }
    }

    static final class Shelf {
        private final String title;
        private final List<Item> items;

        Shelf(String title, List<Item> items) {
            this.title = title;
            this.items = List.copyOf(items);
        }

        String title() { return title; }
        List<Item> items() { return items; }
    }

    private static final List<Shelf> SHELVES = List.of(
            new Shelf("Notification demos", List.of(
                    new Item("Facebook", Action.NOTIFICATION_FACEBOOK),
                    new Item("WhatsApp", Action.NOTIFICATION_WHATSAPP),
                    new Item("Gmail", Action.NOTIFICATION_GMAIL),
                    new Item("X / Twitter", Action.NOTIFICATION_X),
                    new Item("YouTube", Action.NOTIFICATION_YOUTUBE),
                    new Item("Messenger", Action.NOTIFICATION_MESSENGER),
                    new Item("Instagram", Action.NOTIFICATION_INSTAGRAM),
                    new Item("Discord", Action.NOTIFICATION_DISCORD),
                    new Item("Spotify", Action.NOTIFICATION_SPOTIFY),
                    new Item("Reddit", Action.NOTIFICATION_REDDIT),
                    new Item("Locked", Action.NOTIFICATION_LOCKED),
                    new Item("Bundle", Action.NOTIFICATION_BUNDLE))));

    private BoopDevMenuModel() { }

    static List<Shelf> shelves() {
        return SHELVES;
    }
}
