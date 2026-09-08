package com.boop.shieldturbo.picture

import android.app.Activity
import android.view.Display
import java.util.Locale

data class DisplayFacts(
    val width: Int,
    val height: Int,
    val refreshRateHz: Float,
    val hdrTypes: List<String>
) {
    fun modeText(): String = formatMode(width, height, refreshRateHz)
    fun hdrText(): String = if (hdrTypes.isEmpty()) "Not exposed" else hdrTypes.joinToString(", ")

    companion object {
        fun formatMode(width: Int, height: Int, refreshRateHz: Float): String {
            if (width <= 0 || height <= 0 || refreshRateHz <= 0f) return "Not exposed"
            return String.format(Locale.US, "%dx%d @ %.2f Hz", width, height, refreshRateHz)
        }

        @Suppress("DEPRECATION")
        fun current(activity: Activity): DisplayFacts = try {
            val display = activity.windowManager.defaultDisplay
            val mode = display.mode
            val types = display.hdrCapabilities?.supportedHdrTypes ?: intArrayOf()
            val hdr = types.map(::hdrName)
            DisplayFacts(mode.physicalWidth, mode.physicalHeight, mode.refreshRate, hdr)
        } catch (_: Exception) {
            DisplayFacts(0, 0, 0f, emptyList())
        }

        private fun hdrName(type: Int): String = when (type) {
            Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> "Dolby Vision"
            Display.HdrCapabilities.HDR_TYPE_HDR10 -> "HDR10"
            Display.HdrCapabilities.HDR_TYPE_HLG -> "HLG"
            Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> "HDR10+"
            else -> "HDR $type"
        }
    }
}
