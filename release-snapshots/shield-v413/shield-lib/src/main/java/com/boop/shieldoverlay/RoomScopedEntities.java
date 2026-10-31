package com.boop.shieldoverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class RoomScopedEntities {
    private RoomScopedEntities() { }

    static List<EntityCard> keep(AreaInfo room, List<EntityCard> cards) {
        if (room == null || cards == null || cards.isEmpty()) {
            return Collections.emptyList();
        }
        List<EntityCard> scoped = new ArrayList<>();
        for (EntityCard card : cards) {
            if (belongsTo(room, card)) {
                scoped.add(card);
            }
        }
        return Collections.unmodifiableList(scoped);
    }

    static boolean belongsTo(AreaInfo room, EntityCard card) {
        return room != null
                && card != null
                && room.id().equals(card.areaId());
    }
}
