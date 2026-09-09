package com.boop.alpha1;

import java.util.List;

final class BoopDevMenuModel {
    enum Action {
        WAKE,
        THINK,
        BERRY,
        SHAKE,
        SLEEP,
        NOTIFICATION_UNLOCKED,
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
            new Shelf("Animations", List.of(
                    new Item("Wake", Action.WAKE),
                    new Item("Think", Action.THINK),
                    new Item("Berry", Action.BERRY),
                    new Item("Shake", Action.SHAKE),
                    new Item("Sleep", Action.SLEEP))),
            new Shelf("Notification demos", List.of(
                    new Item("Unlocked", Action.NOTIFICATION_UNLOCKED),
                    new Item("Locked", Action.NOTIFICATION_LOCKED),
                    new Item("Bundle", Action.NOTIFICATION_BUNDLE))));

    private BoopDevMenuModel() { }

    static List<Shelf> shelves() {
        return SHELVES;
    }
}
