package com.boop.shieldturbo.startup

/** Keep installed human-readable labels ahead of Android's package-name fallback. */
object StartupAppLabels {
    fun resolve(packageName: String, launcherLabel: String?, applicationLabel: String?): String =
        sequenceOf(launcherLabel, applicationLabel)
            .mapNotNull { it?.trim()?.takeIf { label -> label.isNotEmpty() && label != packageName } }
            .firstOrNull() ?: packageName
}
