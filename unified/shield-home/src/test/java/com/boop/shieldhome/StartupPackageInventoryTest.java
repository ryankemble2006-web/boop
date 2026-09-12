package com.boop.shieldhome;

import java.util.Set;

public final class StartupPackageInventoryTest {
    private static void check(boolean v, String m) { if (!v) throw new AssertionError(m); }
    public static void main(String[] args) {
        String all = "package:com.android.tv.settings\npackage:com.google.android.tvlauncher\npackage:com.example.user\n";
        String system = "package:com.android.tv.settings\npackage:com.google.android.tvlauncher\n";
        String disabled = "package:com.google.android.tvlauncher\n";
        String launch = "20 activities found:\n  Activity #0:\n    com.android.tv.settings/.MainSettings\n  Activity #1:\n    com.example.user/.TvActivity\n";
        Set<String> parsed = StartupPackageInventory.parsePackageList(all);
        check(parsed.size() == 3 && parsed.contains("com.example.user"), "all packages parsed");
        Set<String> launchers = StartupPackageInventory.parseActivityPackages(launch);
        check(launchers.equals(Set.of("com.android.tv.settings", "com.example.user")), "activity packages parsed");
        var rows = StartupPackageInventory.merge(all, system, disabled, launch, pkg -> pkg.equals("com.example.user") ? "Example" : null);
        check(rows.size() == 3, "three inventory rows");
        var stock = rows.stream().filter(r -> r.packageName().equals("com.google.android.tvlauncher")).findFirst().orElseThrow();
        check(stock.systemApp() && stock.enabledState().equals("disabled-user"), "disabled system package represented");
        var user = rows.stream().filter(r -> r.packageName().equals("com.example.user")).findFirst().orElseThrow();
        check(!user.systemApp() && user.launcher() && user.label().equals("Example"), "visible label enriched");
        var settings = rows.stream().filter(r -> r.packageName().equals("com.android.tv.settings")).findFirst().orElseThrow();
        check(settings.label().equals("com.android.tv.settings"), "invisible label falls back to package name");
        System.out.println("StartupPackageInventoryTest PASS");
    }
}