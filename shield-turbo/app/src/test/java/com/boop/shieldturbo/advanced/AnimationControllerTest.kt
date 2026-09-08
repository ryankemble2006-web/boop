package com.boop.shieldturbo.advanced

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimationControllerTest {
    @Test fun halfSpeedWritesEveryAndroidAnimationScale() {
        val writes = mutableListOf<Pair<String, Float>>()
        val controller = AnimationController { key, value ->
            writes += key to value
            true
        }

        val result = controller.apply(AnimationSpeed.HALF)

        assertTrue(result)
        assertEquals(
            listOf(
                "window_animation_scale" to 0.5f,
                "transition_animation_scale" to 0.5f,
                "animator_duration_scale" to 0.5f
            ),
            writes
        )
    }

    @Test fun normalRestoresAllAnimationScalesToOne() {
        val writes = mutableListOf<Pair<String, Float>>()
        val controller = AnimationController { key, value ->
            writes += key to value
            true
        }

        assertTrue(controller.apply(AnimationSpeed.NORMAL))
        assertEquals(listOf(1f, 1f, 1f), writes.map { it.second })
    }

    @Test fun aFailedWriteMakesTheActionFailHonestly() {
        var count = 0
        val controller = AnimationController { _, _ ->
            count++
            count != 2
        }

        assertEquals(false, controller.apply(AnimationSpeed.OFF))
    }
}
