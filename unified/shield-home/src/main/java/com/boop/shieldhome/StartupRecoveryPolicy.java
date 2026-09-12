package com.boop.shieldhome;

import java.util.LinkedHashMap;
import java.util.Map;

public final class StartupRecoveryPolicy {
    public static final String STOCK_LAUNCHER = "com.google.android.tvlauncher";

    public enum Impact { LOW, MEDIUM, HIGH, PROTECTED }
    public enum ManagedAction { DISABLED, BOOT_CLEAN, BACKGROUND_BLOCK }

    public record Assessment(Impact impact, String protectionReason) {
        public boolean protectedPackage() { return impact == Impact.PROTECTED; }
    }

    public record RecoveryCapabilities(
            String boopPackage,
            String settingsPackage,
            String packageInstallerPackage,
            String inputPackage,
            String localBridgePackage) {

        public Map<String,String> protectedPackages() {
            LinkedHashMap<String,String> out = new LinkedHashMap<>();
            add(out, boopPackage, "BOOP must stay available for recovery.");
            add(out, settingsPackage, "Android Settings must stay available for recovery.");
            add(out, packageInstallerPackage, "The package installer must stay available for recovery.");
            add(out, inputPackage, "The active input path must stay available for recovery.");
            add(out, localBridgePackage, "BOOP local control must stay available for recovery.");
            return out;
        }

        private static void add(Map<String,String> out, String packageName, String reason) {
            if (packageName != null && !packageName.isBlank()) out.putIfAbsent(packageName, reason);
        }
    }

    private StartupRecoveryPolicy() { }

    public static Assessment assess(StartupPackageState state, RecoveryCapabilities capabilities) {
        if (state == null) throw new IllegalArgumentException("state");
        if (capabilities == null) throw new IllegalArgumentException("capabilities");

        String protectedReason = capabilities.protectedPackages().get(state.packageName());
        if (protectedReason != null) return new Assessment(Impact.PROTECTED, protectedReason);
        if (STOCK_LAUNCHER.equals(state.packageName()) || state.launcher()) return new Assessment(Impact.HIGH, null);
        if (state.systemApp()) return new Assessment(Impact.MEDIUM, null);
        return new Assessment(Impact.LOW, null);
    }
}
