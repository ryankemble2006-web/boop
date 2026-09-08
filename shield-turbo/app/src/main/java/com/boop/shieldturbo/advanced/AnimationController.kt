package com.boop.shieldturbo.advanced

enum class AnimationSpeed(val scale: Float) {
    OFF(0f),
    HALF(0.5f),
    NORMAL(1f)
}

class AnimationController(private val write: (String, Float) -> Boolean) {
    fun apply(speed: AnimationSpeed): Boolean {
        var success = true
        KEYS.forEach { key ->
            if (!write(key, speed.scale)) success = false
        }
        return success
    }

    companion object {
        val KEYS = listOf(
            "window_animation_scale",
            "transition_animation_scale",
            "animator_duration_scale"
        )
    }
}
