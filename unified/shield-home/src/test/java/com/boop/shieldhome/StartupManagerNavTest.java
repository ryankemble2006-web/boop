package com.boop.shieldhome;

import java.util.List;

public final class StartupManagerNavTest {
    private static void check(boolean v, String m) { if (!v) throw new AssertionError(m); }
    private static StartupManagerNav.State packages(String focused, int action) {
        return new StartupManagerNav.State(StartupManagerNav.Screen.PACKAGES, 0, focused, action,
                List.of("alpha", "beta", "gamma"));
    }
    public static void main(String[] args) {
        var down = StartupManagerNav.key(packages("beta", 1), StartupManagerNav.Key.DOWN, 4);
        check(down.state().focusedPackageId().equals("gamma"), "Down moves one row");
        check(down.state().actionIndex() == 1, "row move preserves action column");
        var bottom = StartupManagerNav.key(packages("gamma", 1), StartupManagerNav.Key.DOWN, 4);
        check(bottom.state().focusedPackageId().equals("gamma"), "Down clamps at bottom");
        var up = StartupManagerNav.key(packages("alpha", 1), StartupManagerNav.Key.UP, 4);
        check(up.state().focusedPackageId().equals("alpha"), "Up clamps at top");

        var right = StartupManagerNav.key(packages("beta", 1), StartupManagerNav.Key.RIGHT, 4);
        check(right.state().actionIndex() == 2 && right.state().focusedPackageId().equals("beta"), "Right stays on row");
        var rightEdge = StartupManagerNav.key(packages("beta", 3), StartupManagerNav.Key.RIGHT, 4);
        check(rightEdge.state().actionIndex() == 3, "Right clamps at final action");
        var leftEdge = StartupManagerNav.key(packages("beta", 0), StartupManagerNav.Key.LEFT, 4);
        check(leftEdge.state().actionIndex() == 0, "Left clamps at first action");

        var same = StartupManagerNav.reconcile(packages("beta", 2), List.of("beta", "gamma"));
        check(same.focusedPackageId().equals("beta"), "filter preserves focused package when visible");
        var removed = StartupManagerNav.reconcile(packages("beta", 2), List.of("alpha", "gamma"));
        check(removed.focusedPackageId().equals("gamma"), "removed row chooses nearest survivor");
        var empty = StartupManagerNav.reconcile(packages("beta", 2), List.of());
        check(empty.focusedPackageId() == null && empty.actionIndex() == 0, "empty list has stable back focus slot");

        var back = StartupManagerNav.key(packages("beta", 1), StartupManagerNav.Key.BACK, 4);
        check(back.state().screen() == StartupManagerNav.Screen.OVERVIEW, "Back returns one level");
        var menu = StartupManagerNav.key(packages("beta", 1), StartupManagerNav.Key.MENU, 4);
        check(menu.state().screen() == StartupManagerNav.Screen.RESTORE, "Menu opens Restore");
        var ok = StartupManagerNav.key(packages("beta", 1), StartupManagerNav.Key.OK, 4);
        check(ok.command() == StartupManagerNav.Command.ACTIVATE, "OK activates focused action");
        var overviewBack = StartupManagerNav.key(new StartupManagerNav.State(
                StartupManagerNav.Screen.OVERVIEW, 0, null, 0, List.of()), StartupManagerNav.Key.BACK, 4);
        check(overviewBack.command() == StartupManagerNav.Command.EXIT, "Back from overview exits manager");
        System.out.println("StartupManagerNavTest PASS");
    }
}