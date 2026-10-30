package com.boop.shieldhome;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class StartupRecoveryPolicy {
    public static final String STOCK_LAUNCHER = "com.google.android.tvlauncher";
    public static final String SETUP_WRAITH = "com.google.android.tungsten.setupwraith";

    private static final Set<String> DAILY_KEEPER_APPS = Set.of(
            "deezer.android.app",
            "org.xbmc.kodi",
            "net.kodinerds.maven.kodi21",
            "com.forki.app",
            "com.fork2.app",
            "com.nvidia.bbciplayer",
            "uk.local.eastenders",
            "uk.local.casualty",
            "com.neon.sleeptvtimerpro",
            "com.google.android.youtube.tv"
    );

    /**
     * Canonical Shield Experience 9.x bloatware, telemetry, partner stub, and dead app list
     * safe to completely rip out for user 0 (pm disable-user --user 0 + pm uninstall --user 0).
     */
    public static final List<String> AUTO_OPTIMISE_RIP_PACKAGES = List.of(
            "android.autoinstalls.config.nvidia",
            "com.amazon.amazonvideo.livingroom.nvidia",
            "deezer.android.tv",
            "com.nvidia.tegrazone3",
            "com.nvidia.nvgamecast",
            "com.nvidia.osc",
            "com.nvidia.shieldbeta",
            "com.nvidia.shield.beta",
            "com.nvidia.stats",
            "com.nvidia.feedback",
            "com.nvidia.diagtools",
            "com.nvidia.shield.registration",
            "com.nvidia.shield.welcome",
            "com.nvidia.shield.ask",
            "com.nvidia.factorybundling",
            "com.nvidia.factory",
            "com.nvidia.factorysyschecker",
            "com.nvidia.hotwordsetup",
            "com.nvidia.app.messaging",
            "com.nvidia.bluetooth.ps3usbpairer",
            "com.nvidia.inputviewer",
            "com.nvidia.developerwidget",
            "com.nvidia.tegraprofiler.security",
            "com.nvidia.benchmarkblocker",
            "com.google.android.feedback",
            "com.google.android.partnersetup",
            "com.google.android.onetimeinitializer",
            "com.google.android.tv",
            "com.google.android.syncadapters.calendar",
            "com.google.android.marvin.talkback",
            "com.google.android.apps.nbu.smartconnect.tv",
            "com.android.providers.calendar",
            "com.android.providers.contacts",
            "com.android.providers.userdictionary",
            "com.android.printspooler",
            "com.android.dreams.basic",
            "com.android.gallery3d",
            "com.android.bluetoothmidiservice",
            "com.android.cts.ctsshim",
            "com.android.cts.priv.ctsshim",
            "com.android.backupconfirm",
            "com.android.sharedstoragebackup",
            "com.android.localtransport",
            "com.android.wallpaperbackup",
            "com.android.hotwordenrollment.okgoogle",
            "com.android.hotwordenrollment.xgoogle",
            "com.android.htmlviewer",
            "com.android.soundpicker"
    );

    /**
     * Stock launcher, ad recommendations, OTA, and SetupWraith packages that should be disabled
     * (pm disable-user --user 0) rather than uninstalled (uninstalling SetupWraith's /data update wakes it up).
     */
    public static final List<String> AUTO_OPTIMISE_DISABLE_ONLY_PACKAGES = List.of(
            "com.google.android.tungsten.setupwraith",
            "com.google.android.tvlauncher",
            "com.google.android.tvrecommendations",
            "com.google.android.backdrop",
            "com.nvidia.ota",
            "com.nvidia.shield.appselector"
    );

    /**
     * Apps & services kept enabled for manual launch or system voice/cast, but blocked from
     * background autostart (RUN_IN_BACKGROUND ignore + RUN_ANY_IN_BACKGROUND ignore + force-stop).
     */
    public static final List<String> AUTO_OPTIMISE_SILENCE_BACKGROUND_PACKAGES = List.of(
            "com.nvidia.bbciplayer",
            "com.google.android.youtube.tv",
            "com.google.android.apps.mediashell",
            "com.google.android.katniss",
            "com.nvidia.shield.smbserver",
            "com.nvidia.shield.nas",
            "com.nvidia.shield.smbauth"
    );

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
            // 1. BOOP Ecosystem
            add(out, boopPackage, "BOOP must stay available for recovery.");
            add(out, localBridgePackage, "BOOP local control must stay available for recovery.");
            add(out, "com.boop.shieldoverlay", "BOOP Shield Launcher must stay available.");
            add(out, "com.boop.alpha1", "BOOP core package must stay available.");
            add(out, "com.boop.notificationlab", "BOOP Notification Lab is protected.");
            add(out, "com.boop.animationlab", "BOOP Animation Lab is protected.");

            // 2. Boot-Critical Android OS, Storage & Settings
            add(out, "android", "Android framework is required to boot.");
            add(out, settingsPackage, "Android Settings must stay available.");
            add(out, "com.android.tv.settings", "Android TV Settings must stay available.");
            add(out, "com.android.tv.settings.gms.resoverlay", "Settings overlay is required for TV Settings.");
            add(out, "com.android.settings.intelligence", "Settings search/index service.");
            add(out, packageInstallerPackage, "Package installer must stay available.");
            add(out, "com.google.android.packageinstaller", "Package installer must stay available.");
            add(out, inputPackage, "Active keyboard/input method must stay available.");
            add(out, "com.android.providers.settings", "Settings provider is required to boot.");
            add(out, "com.android.providers.media", "Media storage provider is required by the OS.");
            add(out, "com.android.providers.media.module", "Media module provider is required by the OS.");
            add(out, "com.android.providers.downloads", "Downloads provider is required by the OS.");
            add(out, "com.android.providers.tv", "TV provider is required by the OS.");
            add(out, "com.android.systemui", "SystemUI is required for display and permission prompts.");
            add(out, "com.android.shell", "ADB shell is required for local control.");
            add(out, "com.android.se", "Secure Element service is required to boot.");
            add(out, "com.android.keychain", "Android Keychain is required for credentials.");
            add(out, "com.android.certinstaller", "Certificate installer is required by the OS.");
            add(out, "com.android.externalstorage", "External storage provider is required by the OS.");
            add(out, "com.android.documentsui", "Documents UI is required for storage access.");
            add(out, "com.android.vpndialogs", "VPN dialog service is required for VPN connections.");
            add(out, "com.android.proxyhandler", "Proxy handler is required for networking.");
            add(out, "com.android.pacprocessor", "PAC processor is required for networking.");
            add(out, "com.android.captiveportallogin", "Captive portal login is required by networking.");
            add(out, "com.android.inputdevices", "Input devices service keeps remotes working.");
            add(out, "com.android.location.fused", "Fused location provider is required by SystemServer.");
            add(out, "com.android.bluetooth", "Bluetooth keeps the physical Shield remote connected.");
            add(out, "com.android.mtp", "MTP USB service is required for USB recovery.");
            add(out, "com.android.companiondevicemanager", "Companion device manager is required for pairing.");
            add(out, "com.android.dynsystem", "Dynamic system service is required by Android.");
            add(out, "com.android.statementservice", "Intent filter verification service.");

            // 3. Networking & Wi-Fi Stack (Required for Ethernet/Wi-Fi boot)
            add(out, "com.android.networkstack", "NetworkStack is required for Ethernet/Wi-Fi.");
            add(out, "com.android.networkstack.inprocess", "In-process NetworkStack is required to boot Ethernet/Wi-Fi.");
            add(out, "com.android.networkstack.tethering.inprocess", "Network tethering stack is required by NetworkStack.");
            add(out, "com.android.networkstack.tethering.nvidia", "NVIDIA tethering overlay is required by NetworkStack.");
            add(out, "com.android.networkstack.permissionconfig", "NetworkStack permission config is required to boot.");
            add(out, "com.google.android.networkstack", "NetworkStack is required for Ethernet/Wi-Fi.");
            add(out, "com.android.wifi.resources", "Wi-Fi system resources are required by NetworkStack.");
            add(out, "com.android.hotspot2.osulogin", "Wi-Fi OSU login module.");

            // 4. Google Core Services, Play Store, WebView & Input
            add(out, "com.google.android.gms", "Google Play Services is required by Android TV apps.");
            add(out, "com.google.android.gsf", "Google Services Framework is required by the OS.");
            add(out, "com.google.android.sss.authbridge", "Google authentication bridge is required by the OS.");
            add(out, "com.android.vending", "Google Play Store is protected.");
            add(out, "com.google.android.webview", "Android System WebView is required by iPlayer and TV apps.");
            add(out, "com.google.android.permissioncontroller", "Permission controller is required to boot.");
            add(out, "com.google.android.overlay.modules.permissioncontroller", "Permission controller overlay.");
            add(out, "com.google.android.overlay.modules.permissioncontroller.forframework", "Permission controller framework overlay.");
            add(out, "com.google.android.overlay.modules.ext.services", "Extended services framework overlay.");
            add(out, "com.google.android.overlay.modules.modulemetadata.forframework", "Module metadata framework overlay.");
            add(out, "com.google.android.ext.services", "Android Extended Services is required to boot.");
            add(out, "com.google.android.ext.shared", "Android Extended Shared library is required by the OS.");
            add(out, "com.google.android.modulemetadata", "Module Metadata is required by PackageManager.");
            add(out, "com.google.android.leanback.ime", "Leanback keyboard is required for TV text input.");
            add(out, "com.google.android.inputmethod.latin", "System keyboard is required for text input.");
            add(out, "com.google.android.tts", "Speech services are required for voice.");
            add(out, "com.google.android.tv.frameworkpackagestubs", "TV framework stubs are required by Leanback.");
            add(out, "com.google.android.tv.remote.service", "Android TV Remote service.");

            // 5. Essential NVIDIA Hardware, Dolby Audio, Remote & HDMI-CEC
            add(out, "com.nvidia.shieldservice", "NVIDIA Shield core hardware/fan/LED service.");
            add(out, "com.nvidia.shieldtech.hooks", "NVIDIA Shield system hooks are required to boot.");
            add(out, "com.nvidia.shieldtech.proxy", "NVIDIA Shield system proxy service.");
            add(out, "com.nvidia.shieldtech.accessoryui", "NVIDIA Shield remote and accessory UI service.");
            add(out, "com.nvidia.blakepairing", "NVIDIA Shield Bluetooth remote pairing service.");
            add(out, "com.nvidia.remotelocator", "NVIDIA Shield remote locator service.");
            add(out, "com.nvidia.irtuner", "NVIDIA IR and HDMI-CEC volume/power control service.");
            add(out, "com.nvidia.shield.remote.server", "NVIDIA Shield remote server.");
            add(out, "com.nvidia.nvaudiosvc", "NVIDIA audio service is required for HDMI/eARC audio.");
            add(out, "com.dolby.android.audio.service", "Dolby Atmos audio processing service.");
            add(out, "com.nvidia.avsync", "NVIDIA audio/video sync service.");
            add(out, "com.nvidia.overscancomp", "NVIDIA display overscan service.");
            add(out, "com.nvidia.ControllerMapper", "NVIDIA controller and button mapping service.");
            add(out, "com.nvidia.bluetooth.ShieldBluetoothManager", "NVIDIA Bluetooth hardware manager.");
            add(out, "com.nvidia.NvCPLSvc", "NVIDIA Control Panel display/HDMI hardware service.");
            add(out, "com.nvidia.NvCPLUpdater", "NVIDIA Control Panel updater.");
            add(out, "com.nvidia.packagemanagerservice", "NVIDIA package manager service.");
            add(out, "com.nvidia.shield.nvcustomize", "NVIDIA Shield settings customization service.");
            add(out, "com.nvidia.wifi.countrycode", "NVIDIA Wi-Fi regulatory service.");
            add(out, "com.nvidia.KeyComboOta", "NVIDIA hardware recovery key combo service.");
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

    /**
     * Allows ripping out any non-protected app (both 3rd-party user apps AND non-protected system bloatware),
     * EXCEPT SetupWraith which must only ever be disabled (uninstalling SetupWraith's /data update wakes it up!).
     */
    public static boolean canUninstall(StartupPackageState state, RecoveryCapabilities capabilities) {
        if (state == null || capabilities == null) return false;
        if (SETUP_WRAITH.equals(state.packageName())) return false;
        return !assess(state, capabilities).protectedPackage();
    }

    public static boolean isBloatCandidate(StartupPackageState state, RecoveryCapabilities capabilities) {
        if (!canUninstall(state, capabilities)) return false;
        if (STOCK_LAUNCHER.equals(state.packageName())) return false;
        if (AUTO_OPTIMISE_DISABLE_ONLY_PACKAGES.contains(state.packageName())) return false;
        return !DAILY_KEEPER_APPS.contains(state.packageName());
    }

    public static boolean isDailyKeeper(String packageName) {
        return packageName != null && DAILY_KEEPER_APPS.contains(packageName);
    }
}
