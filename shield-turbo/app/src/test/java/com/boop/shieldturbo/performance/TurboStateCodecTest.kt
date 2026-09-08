package com.boop.shieldturbo.performance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TurboStateCodecTest {
    @Test
    fun roundTripPreservesBaselineReasonAndThermalFields() {
        val original = TurboSnapshot(
            phase = TurboPhase.TURBO_VERIFIED,
            desiredTurbo = true,
            baselineMode = 1,
            appliedControls = setOf(TurboSnapshot.NVIDIA_POWER_MODE),
            lastReason = "TURBO enabled • physical actuator verified",
            lastChangeEpochMs = 123456789L,
            lastThermalStatus = 2,
            lastThermalFallbackEpochMs = 123400000L
        )

        assertEquals(original, TurboStateCodec.decode(TurboStateCodec.encode(original)))
    }

    @Test
    fun malformedStateDecodesToNull() {
        assertNull(TurboStateCodec.decode("schema=1\nphase=TURBO_VERIFIED\ndesired=maybe"))
        assertNull(TurboStateCodec.decode("not-a-turbo-state"))
    }

    @Test
    fun unsupportedSchemaDecodesToNull() {
        val raw = TurboStateCodec.encode(TurboSnapshot()).replace("schema=1", "schema=2")

        assertNull(TurboStateCodec.decode(raw))
    }

    @Test
    fun unknownControlOrUnprovenBaselineDecodesToNull() {
        val unknownControl = TurboStateCodec.encode(
            TurboSnapshot(
                phase = TurboPhase.TURBO_VERIFIED,
                desiredTurbo = true,
                baselineMode = 1,
                appliedControls = setOf(TurboSnapshot.NVIDIA_POWER_MODE)
            )
        ).replace("controls=system:nv_power_mode", "controls=vendor:magic_boost")
        val unknownBaseline = TurboStateCodec.encode(
            TurboSnapshot(phase = TurboPhase.ENABLING, desiredTurbo = true, baselineMode = 1)
        ).replace("baseline=1", "baseline=9")

        assertNull(TurboStateCodec.decode(unknownControl))
        assertNull(TurboStateCodec.decode(unknownBaseline))
    }
}
