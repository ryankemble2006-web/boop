package com.boop.shieldhome;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class StartupManagerUiModel {
    public enum Filter { ALL, LAUNCHERS, ADS, SYSTEM, USER, DISABLED }
    public enum Mode { DISABLE, BOOT_CLEAN, BACKGROUND }

    private StartupManagerUiModel() { }

    public static List<StartupPackageState> filter(List<StartupPackageState> rows, Filter filter) {
        ArrayList<StartupPackageState> out = new ArrayList<>();
        for (StartupPackageState row : rows) {
            if (matches(row, filter)) out.add(row);
        }
        return List.copyOf(out);
    }

    private static boolean matches(StartupPackageState row, Filter filter) {
        return switch (filter) {
            case ALL -> true;
            case LAUNCHERS -> row.launcher();
            case SYSTEM -> row.systemApp();
            case USER -> !row.systemApp();
            case DISABLED -> isDisabled(row);
            case ADS -> adLike(row);
        };
    }

    public static boolean isDisabled(StartupPackageState row) {
        String state = row.enabledState();
        return state != null && (state.equals("disabled") || state.equals("disabled-user")
                || state.equals("disabled-until-used") || state.equals("manifest-disabled"));
    }

    public static boolean canUsePrimary(StartupPackageState row, StartupRecoveryPolicy.RecoveryCapabilities caps) {
        return isDisabled(row) || !StartupRecoveryPolicy.assess(row,caps).protectedPackage();
    }

    public static String primaryAction(StartupPackageState row) {
        return isDisabled(row) ? "Re-enable" : "Disable";
    }

    public static boolean adLike(StartupPackageState row) {
        String hay = (row.packageName() + " " + row.label()).toLowerCase(Locale.ROOT);
        return hay.contains("recommend") || hay.contains("promo") || hay.contains("sponsor")
                || hay.contains("suggest") || hay.contains("adservice") || hay.contains("ads.")
                || hay.contains(".ads") || hay.contains(" advertising") || hay.contains("advertising ");
    }
}