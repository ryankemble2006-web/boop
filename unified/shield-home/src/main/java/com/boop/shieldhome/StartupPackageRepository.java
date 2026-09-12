package com.boop.shieldhome;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class StartupPackageRepository {
    @FunctionalInterface
    public interface PackageSource { List<RawPackage> loadInstalled(); }

    public record RawPackage(
            String packageName,
            String label,
            boolean systemApp,
            boolean launcher,
            String enabledState) { }

    private final PackageSource source;
    private final Supplier<Set<String>> bootCleanPackages;
    private final Supplier<Set<String>> backgroundBlockedPackages;

    public StartupPackageRepository(PackageSource source,
                                    Supplier<Set<String>> bootCleanPackages,
                                    Supplier<Set<String>> backgroundBlockedPackages) {
        this.source = source;
        this.bootCleanPackages = bootCleanPackages;
        this.backgroundBlockedPackages = backgroundBlockedPackages;
    }
    public List<StartupPackageState> load() {
        Set<String> boot = bootCleanPackages.get();
        Set<String> blocked = backgroundBlockedPackages.get();
        ArrayList<StartupPackageState> out = new ArrayList<>();
        for (RawPackage raw : source.loadInstalled()) {
            EnumSet<StartupRecoveryPolicy.ManagedAction> actions =
                    EnumSet.noneOf(StartupRecoveryPolicy.ManagedAction.class);
            if (boot.contains(raw.packageName())) actions.add(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN);
            if (blocked.contains(raw.packageName())) actions.add(StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK);
            String runMode = blocked.contains(raw.packageName()) ? "ignore" : "unknown";
            String runAnyMode = blocked.contains(raw.packageName()) ? "ignore" : "unknown";
            out.add(new StartupPackageState(
                    raw.packageName(), raw.label(), raw.systemApp(), raw.launcher(), raw.enabledState(),
                    runMode, runAnyMode, actions));
        }
        out.sort(Comparator.comparing(StartupPackageState::label, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(StartupPackageState::packageName, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(out);
    }
}
