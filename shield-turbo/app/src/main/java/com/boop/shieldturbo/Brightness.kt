package com.boop.shieldturbo

object Brightness {
    const val MIN_PERCENT = 10
    const val MAX_PERCENT = 100

    fun clampPercent(percent: Int): Int = percent.coerceIn(MIN_PERCENT, MAX_PERCENT)

    fun dimAlpha(percent: Int): Float =
        (MAX_PERCENT - clampPercent(percent)) / MAX_PERCENT.toFloat()
}
