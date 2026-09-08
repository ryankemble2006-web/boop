package com.boop.shieldturbo.performance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.ArrayDeque

class ProcessorModeActuatorProofTest {
    private val optimized = ProcessorModeActuatorState(
        mode = 1,
        cpuBoost = 0,
        gpuBoost = 0,
        frtBoost = 0,
        frtMin = 15
    )
    private val max = ProcessorModeActuatorState(
        mode = 0,
        cpuBoost = 5,
        gpuBoost = 5,
        frtBoost = 5,
        frtMin = 20
    )

    @Test fun successfulProofWritesOnlyMaxThenRestoreAndVerifiesBothStates() {
        val observed = ArrayDeque(
            listOf(
                optimized,
                ProcessorModeActuatorState(0, 0, 0, 0, 15),
                max,
                ProcessorModeActuatorState(1, 5, 5, 5, 20),
                optimized
            )
        )
        val writes = mutableListOf<Int>()

        val result = ProcessorModeActuatorProof.run(
            readState = { observed.removeFirst() },
            writeMode = { writes += it },
            pause = {}
        )

        assertEquals(listOf(0, 1), writes)
        assertTrue(result.maxVerified)
        assertTrue(result.restoreVerified)
        assertTrue(result.success)
        assertEquals(optimized, result.baseline)
        assertEquals(max, result.maxObserved)
        assertEquals(optimized, result.finalState)
    }

    @Test fun wrongBaselineRefusesToWriteAnything() {
        val writes = mutableListOf<Int>()

        val result = ProcessorModeActuatorProof.run(
            readState = { max },
            writeMode = { writes += it },
            pause = {}
        )

        assertTrue(writes.isEmpty())
        assertFalse(result.success)
        assertFalse(result.maxVerified)
        assertFalse(result.restoreVerified)
        assertEquals(max, result.baseline)
    }

    @Test fun failedMaxVerificationStillRestoresOptimized() {
        val badMax = ProcessorModeActuatorState(0, 4, 5, 5, 20)
        val observed = ArrayDeque(listOf(optimized, badMax, badMax, badMax, optimized))
        val writes = mutableListOf<Int>()

        val result = ProcessorModeActuatorProof.run(
            readState = { observed.removeFirst() },
            writeMode = { writes += it },
            pause = {}
        )

        assertEquals(listOf(0, 1), writes)
        assertFalse(result.maxVerified)
        assertTrue(result.restoreVerified)
        assertFalse(result.success)
        assertEquals(optimized, result.finalState)
    }

    @Test fun policyUsesOnlyNvPowerModeForWrites() {
        assertEquals("settings put system nv_power_mode 0", ProcessorModeActuatorPolicy.writeModeCommand(0))
        assertEquals("settings put system nv_power_mode 1", ProcessorModeActuatorPolicy.writeModeCommand(1))
        assertTrue(ProcessorModeActuatorPolicy.readCommands.contains("settings get system nv_power_mode"))
        assertTrue(ProcessorModeActuatorPolicy.readCommands.contains("getprop persist.vendor.sys.phs.cpufreq.boost"))
        assertTrue(ProcessorModeActuatorPolicy.readCommands.contains("getprop persist.vendor.sys.phs.gpufreq.boost"))
        assertTrue(ProcessorModeActuatorPolicy.readCommands.contains("getprop persist.vendor.sys.phs.frt.boost"))
        assertTrue(ProcessorModeActuatorPolicy.readCommands.contains("getprop persist.vendor.sys.phs.frt.min"))
    }
}
