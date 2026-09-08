package com.boop.shieldturbo.performance

data class SettingClue(val namespace: String, val key: String, val value: String)

data class PathCapability(
    val path: String,
    val present: Boolean,
    val shellWritable: Boolean,
    val value: String
)

data class PerformanceCapabilitySnapshot(
    val settingClues: List<SettingClue>,
    val propertyClues: List<SettingClue>,
    val paths: List<PathCapability>,
    val thermalStatus: Int?,
    val fixedPerformanceCommandExposed: Boolean,
    val trustedAdbAvailable: Boolean,
    val adbDetail: String
)

fun thermalLabel(status: Int?): String = when (status) {
    null -> "Not exposed"
    0 -> "None"
    1 -> "Light"
    2 -> "Moderate"
    3 -> "Severe"
    4 -> "Critical"
    5 -> "Emergency"
    6 -> "Shutdown"
    else -> "Unknown ($status)"
}
