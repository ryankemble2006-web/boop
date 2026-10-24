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
            // 1. BOOP Ecosystem
            add(out, boopPackage, "BOOP must stay available for recovery.");
            add(out, localBridgePackage, "BOOP local control must stay available for recovery.");
            add(out, "com.boop.shieldoverlay", "BOOP Shield Launcher must stay available.");
            add(out, "com.boop.alpha1", "BOOP core package must stay available.");
            add(out, "com.boop.notificationlab", "BOOP Notification Lab is protected.");
            add(out, "com.boop.animationlab", "BOOP Animation Lab is protected.");

            // 2. Core Android OS, Framework, Storage & Providers
            add(out, "android", "Android itself must stay available for recovery.");
            add(out, settingsPackage, "Android Settings must stay available for recovery.");
            add(out, "com.android.tv.settings", "Android TV Settings must stay available for recovery.");
            add(out, "com.android.tv.settings.gms.resoverlay", "Settings overlay is required for TV Settings.");
            add(out, packageInstallerPackage, "The package installer must stay available for recovery.");
            add(out, "com.google.android.packageinstaller", "The package installer must stay available for recovery.");
            add(out, inputPackage, "The active input path must stay available for recovery.");
            add(out, "com.android.providers.settings", "Saved Android settings are needed for local recovery.");
            add(out, "com.android.providers.media", "Media storage provider is required by the OS.");
            add(out, "com.android.providers.media.module", "Media module provider is required by the OS.");
            add(out, "com.android.providers.downloads", "Downloads provider is required by the OS.");
            add(out, "com.android.providers.tv", "TV provider is required by the OS.");
            add(out, "com.android.providers.calendar", "Calendar provider is required by the OS.");
            add(out, "com.android.providers.userdictionary", "User dictionary provider is required by the OS.");
            add(out, "com.android.systemui", "The system screen and permission prompts are needed for recovery.");
            add(out, "com.android.shell", "The local recovery connection needs Android shell.");
            add(out, "com.android.se", "Android Secure Element service is required by the OS.");
            add(out, "com.android.keychain", "Android Keychain is required for credentials and ADB keys.");
            add(out, "com.android.certinstaller", "Certificate installer is required by the OS.");
            add(out, "com.android.externalstorage", "External storage access is required by the OS.");
            add(out, "com.android.documentsui", "Documents UI is required for file picker access.");
            add(out, "com.android.vpndialogs", "VPN dialog service is required for VPN connections.");
            add(out, "com.android.proxyhandler", "Proxy handler is required for network routing.");
            add(out, "com.android.pacprocessor", "PAC processor is required for network routing.");
            add(out, "com.android.captiveportallogin", "Captive portal login is required by networking.");
            add(out, "com.android.inputdevices", "Input devices service keeps remotes and controllers working.");
            add(out, "com.android.location.fused", "Fused location provider is required by SystemServer.");
            add(out, "com.android.bluetooth", "Bluetooth keeps the physical remote connected.");
            add(out, "com.android.bluetoothmidiservice", "Bluetooth service component.");
            add(out, "com.android.mtp", "MTP USB transfer service is required for USB recovery.");
            add(out, "com.android.soundpicker", "System sound picker is required by Android.");
            add(out, "com.android.companiondevicemanager", "Companion device manager is required for pairing.");
            add(out, "com.android.dynsystem", "Dynamic system service is required by Android.");
            add(out, "com.android.wallpaperbackup", "System backup component.");
            add(out, "com.android.sharedstoragebackup", "System storage backup component.");
            add(out, "com.android.backupconfirm", "System backup confirmation component.");
            add(out, "com.android.localtransport", "System local transport component.");

            // 3. Networking & Tethering Stack (Critical for Ethernet/Wi-Fi boot)
            add(out, "com.android.networkstack", "The network connection is needed for local recovery.");
            add(out, "com.android.networkstack.inprocess", "In-process NetworkStack is required to boot Ethernet and Wi-Fi.");
            add(out, "com.android.networkstack.tethering.inprocess", "Network tethering stack is required by NetworkStack.");
            add(out, "com.android.networkstack.tethering.nvidia", "NVIDIA network tethering stack is required by NetworkStack.");
            add(out, "com.android.networkstack.permissionconfig", "NetworkStack permission config is required to boot networking.");
            add(out, "com.google.android.networkstack", "The network connection is needed for local recovery.");

            // 4. Google Core Services, Permissions, Keyboard & Cast
            add(out, "com.google.android.gms", "Google Play Services is required by Android TV apps.");
            add(out, "com.google.android.gsf", "Google Services Framework is required by the OS.");
            add(out, "com.google.android.sss.authbridge", "Google authentication bridge is required by the OS.");
            add(out, "com.android.vending", "Google Play Store is protected.");
            add(out, "com.google.android.permissioncontroller", "Permission controller is required for Android runtime permissions.");
            add(out, "com.google.android.overlay.modules.permissioncontroller.forframework", "Permission controller framework overlay.");
            add(out, "com.google.android.overlay.modules.ext.services", "Extended services framework overlay.");
            add(out, "com.google.android.overlay.modules.modulemetadata.forframework", "Module metadata framework overlay.");
            add(out, "com.google.android.ext.services", "Android Extended Services is required by the OS.");
            add(out, "com.google.android.modulemetadata", "Android Module Metadata is required by PackageManager.");
            add(out, "com.google.android.leanback.ime", "Leanback keyboard is required for TV text input.");
            add(out, "com.google.android.inputmethod.latin", "System keyboard is required for text input.");
            add(out, "com.google.android.tts", "Speech services are required for accessibility and voice.");
            add(out, "com.google.android.tv.frameworkpackagestubs", "TV framework stubs are required by Leanback.");
            add(out, "com.google.android.tv.remote.service", "Android TV Remote service is required for remote control.");
            add(out, "com.google.android.katniss", "Google Assistant core service.");
            add(out, "com.google.android.apps.mediashell", "Chromecast built-in receiver.");
            add(out, "com.google.android.backdrop", "Android TV Ambient / Backdrop service.");

            // 5. NVIDIA Shield Hardware, Audio (Dolby/eARC), Remote, CEC & Platform Services
            add(out, "com.nvidia.shieldservice", "NVIDIA Shield core hardware service.");
            add(out, "com.nvidia.shieldtech.hooks", "NVIDIA Shield system hooks are required to boot.");
            add(out, "com.nvidia.shieldtech.proxy", "NVIDIA Shield system proxy service.");
            add(out, "com.nvidia.shieldtech.accessoryui", "NVIDIA Shield remote and accessory service.");
            add(out, "com.nvidia.blakepairing", "NVIDIA Shield Bluetooth remote pairing service.");
            add(out, "com.nvidia.remotelocator", "NVIDIA Shield remote locator service.");
            add(out, "com.nvidia.irtuner", "NVIDIA IR and HDMI-CEC volume/power control service.");
            add(out, "com.nvidia.nvaudiosvc", "NVIDIA audio service is required for HDMI/eARC audio.");
            add(out, "com.dolby.android.audio.service", "Dolby Atmos audio processing service.");
            add(out, "com.nvidia.avsync", "NVIDIA audio/video sync service.");
            add(out, "com.nvidia.overscancomp", "NVIDIA display overscan calibration service.");
            add(out, "com.nvidia.osc", "NVIDIA display on-screen control service.");
            add(out, "com.nvidia.ControllerMapper", "NVIDIA controller and button mapping service.");
            add(out, "com.nvidia.inputviewer", "NVIDIA input viewer service.");
            add(out, "com.nvidia.bluetooth.ShieldBluetoothManager", "NVIDIA Bluetooth hardware manager.");
            add(out, "com.nvidia.bluetooth.ps3usbpairer", "NVIDIA USB controller pairing service.");
            add(out, "com.nvidia.NvCPLSvc", "NVIDIA Control Panel hardware service.");
            add(out, "com.nvidia.NvCPLUpdater", "NVIDIA Control Panel updater.");
            add(out, "com.nvidia.packagemanagerservice", "NVIDIA package manager service.");
            add(out, "com.nvidia.ota", "NVIDIA system firmware update service.");
            add(out, "com.nvidia.wifi.countrycode", "NVIDIA Wi-Fi regulatory service.");
            add(out, "com.nvidia.shield.smbserver", "NVIDIA SMB network storage server.");
            add(out, "com.nvidia.shield.appselector", "NVIDIA app selector service.");
            add(out, "com.nvidia.KeyComboOta", "NVIDIA hardware recovery key combo service.");
            add(out, "com.nvidia.tegraprofiler.security", "NVIDIA Tegra security service.");
            add(out, "com.nvidia.factorysyschecker", "NVIDIA system integrity checker.");
            add(out, "com.nvidia.factorybundling", "NVIDIA factory bundling service.");
            add(out, "com.nvidia.hotwordsetup", "NVIDIA hotword setup service.");
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

    /** Strictly forbids uninstalling ANY system app or protected package. Only true user APKs can be ripped out. */
    public static boolean canUninstall(StartupPackageState state, RecoveryCapabilities capabilities) {
        if (state == null || capabilities == null) return false;
        if (state.systemApp()) return false;
        return !assess(state, capabilities).protectedPackage();
    }
}
