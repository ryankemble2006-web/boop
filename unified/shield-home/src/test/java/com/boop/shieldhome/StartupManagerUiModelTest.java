package com.boop.shieldhome;

import java.util.List;
import java.util.Set;

public final class StartupManagerUiModelTest {
    private static void check(boolean v, String m) { if (!v) throw new AssertionError(m); }
    private static StartupPackageState s(String pkg, String label, boolean system, boolean launcher, String enabled) {
        return new StartupPackageState(pkg, label, system, launcher, enabled, "unknown", "unknown", Set.of());
    }
    public static void main(String[] args) {
        List<StartupPackageState> rows = List.of(
                s("com.google.android.tvlauncher", "Google TV Launcher", true, true, "default"),
                s("com.nvidia.recommendations", "Home Recommendations", true, false, "default"),
                s("com.example.app", "Example App", false, true, "default"),
                s("com.example.disabled", "Disabled Thing", false, false, "disabled-user"),
                s("com.vendor.promohub", "Promo Hub", true, false, "default"));
        check(StartupManagerUiModel.filter(rows, StartupManagerUiModel.Filter.ALL).size() == 5, "all filter");
        check(StartupManagerUiModel.filter(rows, StartupManagerUiModel.Filter.LAUNCHERS).size() == 2, "launcher filter");
        check(StartupManagerUiModel.filter(rows, StartupManagerUiModel.Filter.SYSTEM).size() == 3, "system filter");
        check(StartupManagerUiModel.filter(rows, StartupManagerUiModel.Filter.USER).size() == 2, "user filter");
        check(StartupManagerUiModel.filter(rows, StartupManagerUiModel.Filter.DISABLED).size() == 1, "disabled filter");
        check(StartupManagerUiModel.filter(rows, StartupManagerUiModel.Filter.ADS).size() == 2, "ads/promos heuristic filter");
        check(StartupManagerUiModel.isDisabled(rows.get(3)), "disabled state helper");
        check(!StartupManagerUiModel.isDisabled(rows.get(0)), "enabled state helper");
        check(StartupManagerUiModel.primaryAction(rows.get(3)).equals("Re-enable"), "disabled package action");
        check(StartupManagerUiModel.primaryAction(rows.get(0)).equals("Disable"), "enabled package action");
        System.out.println("StartupManagerUiModelTest PASS");
    }
}