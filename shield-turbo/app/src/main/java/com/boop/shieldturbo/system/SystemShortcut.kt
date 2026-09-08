package com.boop.shieldturbo.system

data class SettingsComponent(val packageName: String, val className: String)

enum class SystemShortcut(private vararg val routeActions: String) {
    DISPLAY_SOUND,
    APPS("android.settings.APPLICATION_SETTINGS"),
    STORAGE(
        "android.settings.INTERNAL_STORAGE_SETTINGS",
        "android.settings.STORAGE_SETTINGS"
    ),
    NETWORK(
        "android.settings.NETWORK_OPERATOR_SETTINGS",
        "android.settings.WIFI_SETTINGS"
    ),
    ACCESSIBILITY("android.settings.ACCESSIBILITY_SETTINGS"),
    DEVELOPER("android.settings.APPLICATION_DEVELOPMENT_SETTINGS"),
    ABOUT("android.settings.DEVICE_INFO_SETTINGS");

    fun actions(): List<String> = routeActions.toList()

    /** The combined TV page, not Android's general, display-only or sound-only settings. */
    fun component(): SettingsComponent? = when (this) {
        DISPLAY_SOUND -> SettingsComponent(
            "com.android.tv.settings",
            "com.android.tv.settings.device.displaysound.DisplaySoundActivity"
        )
        else -> null
    }
}
