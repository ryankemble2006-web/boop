package com.boop.shieldturbo.system

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
    ACCESSIBILITY,
    DEVELOPER("android.settings.APPLICATION_DEVELOPMENT_SETTINGS"),
    ABOUT("android.settings.DEVICE_INFO_SETTINGS");

    fun actions(): List<String> = routeActions.toList()

    /** Firmware-specific destinations need installed-component discovery and human acceptance. */
    fun firmwarePage(): String? = when (this) {
        DISPLAY_SOUND -> "display"
        ACCESSIBILITY -> "accessibility"
        else -> null
    }
}
