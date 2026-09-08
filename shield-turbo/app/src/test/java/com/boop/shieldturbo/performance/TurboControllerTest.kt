package com.boop.shieldturbo.performance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TurboControllerTest {
    private val optimized = ProcessorModeActuatorProof.EXPECTED_OPTIMIZED
    private val max = ProcessorModeActuatorProof.EXPECTED_MAX

    @Test
    fun baselineIsPersistedBeforeFirstWrite() {
        val events = mutableListOf<String>()
        val store = FakeStore(events = events)
        val control = FakeControl(events = events, reads = mutableListOf(optimized), waits = mutableListOf(max))
        val controller = controller(store, control, FakeThermal(events, 0))

        val outcome = controller.enable()

        assertTrue(outcome.success)
        assertTrue(events.indexOf("save:ENABLING") < events.indexOf("write:0"))
        assertEquals(1, store.saved.first { it.phase == TurboPhase.ENABLING }.baselineMode)
    }

    @Test
    fun enableVerifiesMaxAndKeepsOriginalMode() {
        val store = FakeStore()
        val control = FakeControl(reads = mutableListOf(optimized), waits = mutableListOf(max))

        val outcome = controller(store, control, FakeThermal(status = 1)).enable()

        assertTrue(outcome.success)
        assertEquals(TurboPhase.TURBO_VERIFIED, outcome.snapshot.phase)
        assertTrue(outcome.snapshot.desiredTurbo)
        assertEquals(1, outcome.snapshot.baselineMode)
        assertEquals(setOf(TurboSnapshot.NVIDIA_POWER_MODE), outcome.snapshot.appliedControls)
        assertEquals(listOf(0), control.writes)
    }

    @Test
    fun partialEnableFailureRestoresOriginalMode() {
        val store = FakeStore()
        val control = FakeControl(
            reads = mutableListOf(optimized),
            waits = mutableListOf(optimized, optimized)
        )

        val outcome = controller(store, control, FakeThermal(status = 0)).enable()

        assertFalse(outcome.success)
        assertEquals(listOf(0, 1), control.writes)
        assertEquals(TurboPhase.NORMAL, outcome.snapshot.phase)
        assertFalse(outcome.snapshot.desiredTurbo)
        assertEquals(null, outcome.snapshot.baselineMode)
    }

    @Test
    fun failedRestoreKeepsBaselineAndMarksRecoveryRequired() {
        val store = FakeStore(
            initial = TurboSnapshot(
                phase = TurboPhase.TURBO_VERIFIED,
                desiredTurbo = true,
                baselineMode = 1,
                appliedControls = setOf(TurboSnapshot.NVIDIA_POWER_MODE)
            )
        )
        val control = FakeControl(waits = mutableListOf(max))

        val outcome = controller(store, control, FakeThermal(status = 0)).disable("Manual NORMAL")

        assertFalse(outcome.success)
        assertEquals(TurboPhase.RECOVERY_REQUIRED, outcome.snapshot.phase)
        assertEquals(1, outcome.snapshot.baselineMode)
        assertFalse(outcome.snapshot.desiredTurbo)
        assertEquals(listOf(1), control.writes)
    }

    @Test
    fun bootChecksThermalBeforeAnyWrite() {
        val events = mutableListOf<String>()
        val store = FakeStore(
            initial = TurboSnapshot(
                phase = TurboPhase.TURBO_VERIFIED,
                desiredTurbo = true,
                baselineMode = 1,
                appliedControls = setOf(TurboSnapshot.NVIDIA_POWER_MODE)
            ),
            events = events
        )
        val control = FakeControl(events = events, reads = mutableListOf(optimized), waits = mutableListOf(max))

        val outcome = controller(store, control, FakeThermal(events, 2)).bootReapply()

        assertTrue(outcome.success)
        assertTrue(events.indexOf("thermal") < events.indexOf("write:0"))
        assertEquals(TurboPhase.TURBO_VERIFIED, outcome.snapshot.phase)
        assertEquals(1, outcome.snapshot.baselineMode)
    }

    @Test
    fun severeThermalStatusRestoresNormalAndDisarmsTurbo() {
        val store = FakeStore(
            initial = TurboSnapshot(
                phase = TurboPhase.TURBO_VERIFIED,
                desiredTurbo = true,
                baselineMode = 1,
                appliedControls = setOf(TurboSnapshot.NVIDIA_POWER_MODE)
            )
        )
        val control = FakeControl(waits = mutableListOf(optimized))

        val outcome = controller(store, control, FakeThermal(status = 3)).bootReapply()

        assertTrue(outcome.success)
        assertEquals(listOf(1), control.writes)
        assertEquals(TurboPhase.NORMAL, outcome.snapshot.phase)
        assertFalse(outcome.snapshot.desiredTurbo)
        assertEquals(3, outcome.snapshot.lastThermalStatus)
        assertEquals(1234L, outcome.snapshot.lastThermalFallbackEpochMs)
    }

    @Test
    fun lowerThermalStatusLeavesTurboActive() {
        val initial = TurboSnapshot(
            phase = TurboPhase.TURBO_VERIFIED,
            desiredTurbo = true,
            baselineMode = 1,
            appliedControls = setOf(TurboSnapshot.NVIDIA_POWER_MODE)
        )
        val store = FakeStore(initial)
        val control = FakeControl()

        val outcome = controller(store, control, FakeThermal(status = 0)).onThermalStatus(2)

        assertTrue(outcome.success)
        assertFalse(outcome.changed)
        assertEquals(TurboPhase.TURBO_VERIFIED, outcome.snapshot.phase)
        assertTrue(control.writes.isEmpty())
    }

    @Test
    fun ambiguousPersistedStateRestoresTowardNormalAndNeverReappliesTurbo() {
        val store = FakeStore(
            initial = TurboSnapshot(
                phase = TurboPhase.ENABLING,
                desiredTurbo = true,
                baselineMode = 1
            )
        )
        val control = FakeControl(waits = mutableListOf(optimized))

        val outcome = controller(store, control, FakeThermal(status = 0)).bootReapply()

        assertTrue(outcome.success)
        assertEquals(listOf(1), control.writes)
        assertFalse(control.writes.contains(0))
        assertEquals(TurboPhase.NORMAL, outcome.snapshot.phase)
    }

    private fun controller(
        store: TurboStateStore,
        control: TurboControlPort,
        thermal: TurboThermalPort
    ) = TurboController(store, control, thermal, TurboClock { 1234L }, severeStatus = 3)

    private class FakeStore(
        initial: TurboSnapshot = TurboSnapshot(),
        private val events: MutableList<String> = mutableListOf()
    ) : TurboStateStore {
        private var current = initial
        val saved = mutableListOf<TurboSnapshot>()

        override fun load(): TurboSnapshot = current

        override fun save(snapshot: TurboSnapshot): Boolean {
            events += "save:${snapshot.phase.name}"
            saved += snapshot
            current = snapshot
            return true
        }
    }

    private class FakeControl(
        private val events: MutableList<String> = mutableListOf(),
        private val reads: MutableList<ProcessorModeActuatorState> = mutableListOf(),
        private val waits: MutableList<ProcessorModeActuatorState> = mutableListOf()
    ) : TurboControlPort {
        val writes = mutableListOf<Int>()

        override fun readState(): ProcessorModeActuatorState {
            events += "read"
            return if (reads.isNotEmpty()) reads.removeAt(0) else ProcessorModeActuatorProof.EXPECTED_MAX
        }

        override fun writeMode(mode: Int) {
            events += "write:$mode"
            writes += mode
        }

        override fun waitForState(expected: ProcessorModeActuatorState): ProcessorModeActuatorState {
            events += "wait:${expected.mode}"
            return if (waits.isNotEmpty()) waits.removeAt(0) else expected
        }
    }

    private class FakeThermal(
        private val events: MutableList<String> = mutableListOf(),
        private val status: Int?
    ) : TurboThermalPort {
        override fun currentStatus(): Int? {
            events += "thermal"
            return status
        }
    }
}
