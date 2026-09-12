package com.boop.shieldhome;

public final class StartupPackageCommandsTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check(StartupPackageCommands.disable("com.android.tvlauncher")
                .equals("pm disable-user --user current 'com.android.tvlauncher'"), "disable-user command");
        check(StartupPackageCommands.disablePlain("com.example.app")
                .equals("pm disable --user current 'com.example.app'"), "disabled command");
        check(StartupPackageCommands.enable("com.android.tvlauncher")
                .equals("pm enable --user current 'com.android.tvlauncher'"), "enable command");
        check(StartupPackageCommands.resetEnabled("com.android.tvlauncher")
                .equals("pm default-state --user current 'com.android.tvlauncher'"), "default-state command");
        check(StartupPackageCommands.forceStop("com.android.systemui")
                .equals("am force-stop --user current 'com.android.systemui'"), "force-stop system package");
        check(StartupPackageCommands.userState("com.example.app", 0).contains("User 0:"), "user-state command");
        check(StartupPackageCommands.queryAppOp("com.nvidia.recommendations", "RUN_IN_BACKGROUND")
                .equals("cmd appops get com.nvidia.recommendations RUN_IN_BACKGROUND"), "query app-op");
        check(StartupPackageCommands.setAppOp("com.nvidia.recommendations", "RUN_ANY_IN_BACKGROUND", "ignore")
                .equals("cmd appops set com.nvidia.recommendations RUN_ANY_IN_BACKGROUND ignore"), "set app-op");
        check(StartupPackageCommands.parseEnabled("User 0: installed=true stopped=false enabled=3")
                .equals("disabled-user"), "parse disabled-user");
        check(StartupPackageCommands.parseEnabled("User 0: installed=true stopped=false enabled=0")
                .equals("default"), "parse default");
        check("com.android.tvlauncher".equals(StartupPackageCommands.parseResumedPackage("mResumedActivity: ActivityRecord{abc u0 com.android.tvlauncher/.MainActivity t1}")), "parse system resumed package");
        check(Boolean.TRUE.equals(StartupPackageCommands.parseStopped(
                "User 0: installed=true stopped=true enabled=0")), "parse stopped");
        check(StartupPackageCommands.packageProcesses("com.example.app",
                "111 com.example.app\n112 com.example.app:worker\n113 com.other.app").size() == 2,
                "match main and child processes");
        boolean rejected = false;
        try { StartupPackageCommands.disable("com.example.app; reboot"); }
        catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected, "shell injection rejected");
        System.out.println("StartupPackageCommandsTest PASS");
    }
}
