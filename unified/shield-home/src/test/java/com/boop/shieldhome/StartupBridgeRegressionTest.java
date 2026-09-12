package com.boop.shieldhome;
import java.util.Set;
public final class StartupBridgeRegressionTest {
    static void check(boolean value,String why) { if(!value) throw new AssertionError(why); }
    public static void main(String[] args) {
        check(StartupPackageCommands.installedPackages(false,false,7).equals("pm list packages --user 7"),"inventory is installed-only and user-scoped");
        check(StartupPackageCommands.installedPackages(true,false,0).contains("-s"),"system inventory");
        check(StartupPackageCommands.homeActivities(0).contains("android.intent.category.HOME"),"launchers means HOME not every visible app");
        check(StartupPackageCommands.homeActivities(0).contains("--query-flags 512"),"disabled Home apps remain classified");
        check(StartupPackageCommands.forUser(StartupPackageCommands.disable("com.example.app"),7).contains("--user 7"),"writes pinned to captured user");
        check(StartupPackageInventory.parsePackageList("package:android\npackage:com.example.app\n").contains("android"),"framework is visible, not manageable");
        var hidden=StartupPackageInventory.detail("com.hidden.worker","User 0: installed=true stopped=false enabled=3",true,false,null,"default","ignore",Set.of());
        check(hidden.label().equals("com.hidden.worker") && hidden.systemApp(),"hidden metadata is not needed to control a known package");
        check(hidden.enabledState().equals("disabled-user"),"exact enabled mode is observed");
        boolean rejected=false;
        try { StartupPackageInventory.detail("com.example.app","User 0: installed=false enabled=0",false,false,null,"default","default",Set.of()); }
        catch(IllegalArgumentException expected) { rejected=true; }
        check(rejected,"uninstalled entries cannot be mutated");
        System.out.println("StartupBridgeRegressionTest PASS");
    }
}
