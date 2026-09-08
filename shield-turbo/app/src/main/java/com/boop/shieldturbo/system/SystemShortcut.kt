package com.boop.shieldturbo.system

enum class SystemShortcut(private vararg val routeActions: String) {
    DISPLAY_SOUND(
        "android.settings.SETTINGS",
        "android.settings.DISPLAY_SETTINGS",
        "android.settings.SOUND_SETTINGS"
    ),
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
}
