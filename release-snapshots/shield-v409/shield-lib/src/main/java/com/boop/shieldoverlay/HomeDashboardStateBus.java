package com.boop.shieldoverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class HomeDashboardStateBus {
    interface Listener { void onState(HomeDashboardController.ViewState state); }

    private static final Map<String, HomeDashboardController.ViewState> latest = new HashMap<>();
    private static final Map<String, List<Listener>> listeners = new HashMap<>();

    private HomeDashboardStateBus() { }

    static synchronized HomeDashboardController.ViewState latest(AreaInfo room) {
        return room == null ? null : latest.get(room.id());
    }

    static Runnable subscribe(AreaInfo room, Listener listener) {
        if (room == null || listener == null) return () -> { };
        HomeDashboardController.ViewState initial;
        synchronized (HomeDashboardStateBus.class) {
            listeners.computeIfAbsent(room.id(), ignored -> new ArrayList<>()).add(listener);
            initial = latest.get(room.id());
        }
        if (initial != null) listener.onState(initial);
        return () -> unsubscribe(room.id(), listener);
    }

    static void publish(AreaInfo room, HomeDashboardController.ViewState state) {
        if (room == null || state == null) return;
        List<Listener> snapshot;
        synchronized (HomeDashboardStateBus.class) {
            latest.put(room.id(), state);
            List<Listener> current = listeners.get(room.id());
            snapshot = current == null ? List.of() : new ArrayList<>(current);
        }
        for (Listener listener : snapshot) listener.onState(state);
    }

    private static synchronized void unsubscribe(String roomId, Listener listener) {
        List<Listener> current = listeners.get(roomId);
        if (current == null) return;
        current.remove(listener);
        if (current.isEmpty()) listeners.remove(roomId);
    }
}
