package com.boop.shieldturbo.ui

enum class TurboSection(val key: String) {
    TURBO("turbo"),
    PICTURE("picture"),
    APPS("apps"),
    NETWORK("network"),
    SHIELD("shield");

    companion object {
        fun fromKey(key: String): TurboSection? = values().firstOrNull { it.key == key }
    }
}
