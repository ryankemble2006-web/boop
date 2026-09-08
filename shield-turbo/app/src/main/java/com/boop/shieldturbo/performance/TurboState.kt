package com.boop.shieldturbo.performance

enum class TurboPhase {
    NORMAL,
    ENABLING,
    TURBO_VERIFIED,
    RESTORING,
    RECOVERY_REQUIRED
}

data class TurboSnapshot(
    val schema: Int = SCHEMA,
    val phase: TurboPhase = TurboPhase.NORMAL,
    val desiredTurbo: Boolean = false,
    val baselineMode: Int? = null,
    val appliedControls: Set<String> = emptySet(),
    val lastReason: String = "NORMAL",
    val lastChangeEpochMs: Long = 0L,
    val lastThermalStatus: Int? = null,
    val lastThermalFallbackEpochMs: Long? = null
) {
    companion object {
        const val SCHEMA = 1
        const val NVIDIA_POWER_MODE = "system:nv_power_mode"
    }
}

fun interface TurboClock {
    fun now(): Long
}

interface TurboStateStore {
    fun load(): TurboSnapshot
    fun save(snapshot: TurboSnapshot): Boolean
}

interface TurboControlPort {
    fun readState(): ProcessorModeActuatorState
    fun writeMode(mode: Int)
    fun waitForState(expected: ProcessorModeActuatorState): ProcessorModeActuatorState
}

interface TurboThermalPort {
    fun currentStatus(): Int?
}

data class TurboOutcome(
    val success: Boolean,
    val changed: Boolean,
    val snapshot: TurboSnapshot,
    val message: String
)
