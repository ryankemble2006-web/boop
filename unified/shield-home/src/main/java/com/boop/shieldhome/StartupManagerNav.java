package com.boop.shieldhome;

import java.util.List;

public final class StartupManagerNav {
    public enum Screen { OVERVIEW, PACKAGES, RESTORE }
    public enum Key { UP, DOWN, LEFT, RIGHT, OK, BACK, MENU }
    public enum Command { NONE, ACTIVATE, EXIT }
    public record State(Screen screen, int filterIndex, String focusedPackageId,
                        int actionIndex, List<String> visiblePackageIds) {
        public State {
            visiblePackageIds = visiblePackageIds == null ? List.of() : List.copyOf(visiblePackageIds);
            if (filterIndex < 0) filterIndex = 0;
            if (actionIndex < 0) actionIndex = 0;
        }
    }
    public record Transition(State state, Command command) { }

    private StartupManagerNav() { }

    public static Transition key(State state, Key key, int actionCount) {
        if (state == null || key == null) throw new IllegalArgumentException();
        if (key == Key.MENU) return new Transition(
                new State(Screen.RESTORE, state.filterIndex(), state.focusedPackageId(),
                        state.actionIndex(), state.visiblePackageIds()), Command.NONE);
        if (key == Key.BACK) {
            if (state.screen() == Screen.OVERVIEW) return new Transition(state, Command.EXIT);
            return new Transition(new State(Screen.OVERVIEW, state.filterIndex(), null, 0, List.of()), Command.NONE);
        }
        if (key == Key.OK) return new Transition(state, Command.ACTIVATE);
        if (state.screen() != Screen.PACKAGES && state.screen() != Screen.RESTORE)
            return new Transition(state, Command.NONE);
        if (state.visiblePackageIds().isEmpty()) return new Transition(state, Command.NONE);

        int row = state.visiblePackageIds().indexOf(state.focusedPackageId());
        if (row < 0) row = 0;
        int action = Math.max(0, state.actionIndex());
        if (key == Key.UP) row = Math.max(0, row - 1);
        if (key == Key.DOWN) row = Math.min(state.visiblePackageIds().size() - 1, row + 1);
        if (key == Key.LEFT) action = Math.max(0, action - 1);
        if (key == Key.RIGHT) action = Math.min(Math.max(0, actionCount - 1), action + 1);
        State next = new State(state.screen(), state.filterIndex(),
                state.visiblePackageIds().get(row), action, state.visiblePackageIds());
        return new Transition(next, Command.NONE);
    }

    public static State reconcile(State state, List<String> newVisible) {
        List<String> next = newVisible == null ? List.of() : List.copyOf(newVisible);
        if (next.isEmpty()) return new State(state.screen(), state.filterIndex(), null, 0, next);
        if (state.focusedPackageId() != null && next.contains(state.focusedPackageId()))
            return new State(state.screen(), state.filterIndex(), state.focusedPackageId(), state.actionIndex(), next);
        int oldIndex = state.focusedPackageId() == null ? 0 : state.visiblePackageIds().indexOf(state.focusedPackageId());
        if (oldIndex < 0) oldIndex = 0;
        int newIndex = Math.min(oldIndex, next.size() - 1);
        return new State(state.screen(), state.filterIndex(), next.get(newIndex), state.actionIndex(), next);
    }
}