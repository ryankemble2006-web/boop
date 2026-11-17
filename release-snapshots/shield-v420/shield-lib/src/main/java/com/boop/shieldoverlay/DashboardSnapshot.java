package com.boop.shieldoverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DashboardSnapshot {
    private final AreaInfo room;
    private final List<EntityCard> cards;

    public DashboardSnapshot(AreaInfo room, List<EntityCard> cards) {
        if (room == null) {
            throw new IllegalArgumentException("room is required");
        }
        this.room = room;
        List<EntityCard> safeCards = cards == null
                ? Collections.emptyList()
                : new ArrayList<>(cards);
        this.cards = Collections.unmodifiableList(safeCards);
    }

    public AreaInfo room() {
        return room;
    }

    public List<EntityCard> cards() {
        return cards;
    }
}
