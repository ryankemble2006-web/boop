package com.boop.shieldhome;

import java.util.List;
import java.util.Set;

public final class StartupPackageRepositoryTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        StartupPackageRepository.PackageSource source = () -> List.of(
                new StartupPackageRepository.RawPackage("com.z.user", "Zulu", false, false, "enabled"),
                new StartupPackageRepository.RawPackage("com.a.disabled", "", false, false, "disabled-user"),
                new StartupPackageRepository.RawPackage("com.google.android.tvlauncher", "Google TV Launcher", true, true, "enabled"),
                new StartupPackageRepository.RawPackage("com.nvidia.recs", "Recommendations", true, false, "enabled"),
                new StartupPackageRepository.RawPackage("com.hidden.service", "Hidden Service", true, false, "enabled"));

        StartupPackageRepository repository = new StartupPackageRepository(
                source,
                () -> Set.of("com.google.android.tvlauncher", "com.hidden.service"),
                () -> Set.of("com.nvidia.recs"));

        List<StartupPackageState> rows = repository.load();
        check(rows.size() == 5, "all installed packages included");
        check(rows.stream().anyMatch(s -> s.packageName().equals("com.a.disabled")
                && s.enabledState().equals("disabled-user")), "disabled package included");
        check(rows.stream().anyMatch(s -> s.packageName().equals("com.hidden.service")
                && !s.launcher()), "non-launcher package included");
        check(rows.stream().filter(s -> s.packageName().equals("com.a.disabled"))
                .findFirst().orElseThrow().label().equals("com.a.disabled"), "blank label falls back to package");
        check(rows.get(0).label().equals("com.a.disabled"), "stable label-first sort");
        check(rows.stream().filter(s -> s.packageName().equals("com.google.android.tvlauncher"))
                .findFirst().orElseThrow().managedActions().contains(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN),
                "boot-clean selection folded in");
        check(rows.stream().filter(s -> s.packageName().equals("com.nvidia.recs"))
                .findFirst().orElseThrow().managedActions().contains(StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK),
                "background block folded in");
        check(rows.stream().filter(s -> s.packageName().equals("com.nvidia.recs"))
                .findFirst().orElseThrow().runInBackgroundMode().equals("ignore"),
                "managed background state represented");
        System.out.println("StartupPackageRepositoryTest PASS");
    }
}
