package com.boop.shieldhome;

import java.util.Set;

public final class StartupRecoveryPolicyTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static StartupPackageState state(String pkg, boolean system, boolean launcher) {
        return new StartupPackageState(pkg, pkg, system, launcher, "enabled",
                "default", "default", Set.of());
    }

    public static void main(String[] args) {
        StartupRecoveryPolicy.RecoveryCapabilities caps =
                new StartupRecoveryPolicy.RecoveryCapabilities(
                        "com.boop.alpha1", "com.android.tv.settings",
                        "com.google.android.packageinstaller", "com.android.inputmethod.latin",
                        "com.boop.alpha1");

        check(StartupRecoveryPolicy.assess(state("com.boop.alpha1", false, true), caps).impact()
                == StartupRecoveryPolicy.Impact.PROTECTED, "BOOP protected");
        check(StartupRecoveryPolicy.assess(state("com.android.tv.settings", true, false), caps).impact()
                == StartupRecoveryPolicy.Impact.PROTECTED, "Settings protected");
        check(StartupRecoveryPolicy.assess(state("com.google.android.packageinstaller", true, false), caps).impact()
                == StartupRecoveryPolicy.Impact.PROTECTED, "package installer protected");
        check(StartupRecoveryPolicy.assess(state("com.android.inputmethod.latin", true, false), caps).impact()
                == StartupRecoveryPolicy.Impact.PROTECTED, "input path protected");
        check(StartupRecoveryPolicy.assess(state("com.google.android.tvlauncher", true, true), caps).impact()
                == StartupRecoveryPolicy.Impact.HIGH, "stock launcher manageable high impact");
        check(StartupRecoveryPolicy.assess(state("com.nvidia.recommendations", true, false), caps).impact()
                == StartupRecoveryPolicy.Impact.MEDIUM, "system recommendation manageable");
        check(StartupRecoveryPolicy.assess(state("com.google.android.adservice", true, false), caps).impact()
                != StartupRecoveryPolicy.Impact.PROTECTED, "Google prefix is not blanket protected");
        check(StartupRecoveryPolicy.assess(state("com.nvidia.promohub", true, false), caps).impact()
                != StartupRecoveryPolicy.Impact.PROTECTED, "NVIDIA prefix is not blanket protected");
        check(StartupRecoveryPolicy.assess(state("com.example.userapp", false, false), caps).impact()
                == StartupRecoveryPolicy.Impact.LOW, "ordinary user app low impact");
        check(StartupRecoveryPolicy.assess(state("com.example.launcher", false, true), caps).impact()
                == StartupRecoveryPolicy.Impact.HIGH, "launcher high impact");

        StartupRecoveryPolicy.Assessment protectedAssessment =
                StartupRecoveryPolicy.assess(state("com.android.tv.settings", true, false), caps);
        check(protectedAssessment.protectionReason() != null
                && !protectedAssessment.protectionReason().isBlank(), "protected item explains why");
        System.out.println("StartupRecoveryPolicyTest PASS");
    }
}
