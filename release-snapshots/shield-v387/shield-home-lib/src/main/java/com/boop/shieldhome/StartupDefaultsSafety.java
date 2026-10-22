package com.boop.shieldhome;
import java.util.Set;

/** Conservative, feature-only guard; does not narrow the existing manual package console. */
public final class StartupDefaultsSafety {
    private StartupDefaultsSafety() { }
    public static String reason(String pkg,boolean shield,boolean setupComplete,String foreground,
            StartupRecoveryPolicy.RecoveryCapabilities caps,Set<String> enabled,Set<String> activeInput,boolean homeReady) {
        if(!shield)return "BOOP defaults is only available on NVIDIA Shield TV.";
        if(!setupComplete)return "Finish Android's initial setup before using BOOP defaults.";
        if(foreground==null||foreground.isEmpty())return "The app on screen could not be checked.";
        if(pkg.equals(foreground))return "This app is currently on screen; it will be left alone.";
        if(caps.protectedPackages().containsKey(pkg))return caps.protectedPackages().get(pkg);
        if(activeInput.contains(pkg))return "This app currently handles input or accessibility. Its settings are kept.";
        for(String required:new String[]{caps.boopPackage(),caps.settingsPackage(),caps.packageInstallerPackage(),caps.inputPackage(),caps.localBridgePackage(),"com.android.systemui","com.android.providers.settings","com.android.bluetooth","com.android.shell"})
            if(required==null||!enabled.contains(required))return "An input or recovery service is unavailable. Restore that first.";
        if(StartupRecoveryPolicy.STOCK_LAUNCHER.equals(pkg)&&!homeReady)return "BOOP Home must be available before changing the stock launcher.";
        return "";
    }
}
