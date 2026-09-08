package com.boop.shieldturbo.performance

class TurboController(
    private val store: TurboStateStore,
    private val control: TurboControlPort,
    private val thermal: TurboThermalPort,
    private val clock: TurboClock,
    private val severeStatus: Int
) {
    fun enable(): TurboOutcome {
        val current = store.load()
        if (current.phase == TurboPhase.TURBO_VERIFIED && current.desiredTurbo) {
            return TurboOutcome(true, false, current, "TURBO already active")
        }
        if (current.phase != TurboPhase.NORMAL) {
            return TurboOutcome(false, false, current, "TURBO not applied: recover NORMAL first")
        }

        val thermalStatus = thermal.currentStatus()
            ?: return TurboOutcome(false, false, current, "TURBO not applied: thermal protection is unavailable")
        if (thermalStatus >= severeStatus) {
            return TurboOutcome(false, false, current, "TURBO not applied: Shield thermal status is SEVERE or higher")
        }

        val baseline = try {
            control.readState()
        } catch (failure: Exception) {
            return TurboOutcome(false, false, current, "TURBO not applied: ${detail(failure)}")
        }
        val baselineMode = baseline.mode
        val expectedBaseline = expectedForMode(baselineMode)
            ?: return TurboOutcome(false, false, current, "TURBO not applied: processor mode is not a proven stock state")
        if (baseline != expectedBaseline) {
            return TurboOutcome(false, false, current, "TURBO not applied: processor state did not match its proven stock profile")
        }

        val enabling = current.copy(
            phase = TurboPhase.ENABLING,
            desiredTurbo = true,
            baselineMode = baselineMode,
            appliedControls = emptySet(),
            lastReason = "Enabling TURBO",
            lastChangeEpochMs = clock.now(),
            lastThermalStatus = thermalStatus
        )
        if (!store.save(enabling)) {
            return TurboOutcome(false, false, current, "TURBO not applied: original NORMAL state could not be saved")
        }

        return try {
            control.writeMode(MAX_MODE)
            val observed = control.waitForState(ProcessorModeActuatorProof.EXPECTED_MAX)
            if (observed != ProcessorModeActuatorProof.EXPECTED_MAX) {
                throw IllegalStateException("Max processor state did not verify")
            }
            val active = enabling.copy(
                phase = TurboPhase.TURBO_VERIFIED,
                desiredTurbo = true,
                appliedControls = setOf(TurboSnapshot.NVIDIA_POWER_MODE),
                lastReason = "TURBO enabled: NVIDIA Max performance verified",
                lastChangeEpochMs = clock.now()
            )
            if (!store.save(active)) {
                rollbackEnableFailure(enabling, "TURBO not applied: verified state could not be saved")
            } else {
                TurboOutcome(true, true, active, "TURBO ON: NVIDIA Max performance verified")
            }
        } catch (failure: Exception) {
            rollbackEnableFailure(enabling, "TURBO not applied: ${detail(failure)}")
        }
    }

    fun disable(reason: String = "Manual NORMAL"): TurboOutcome {
        val current = store.load()
        if (current.phase == TurboPhase.NORMAL && current.baselineMode == null) {
            return TurboOutcome(true, false, current.copy(desiredTurbo = false), "TURBO already off")
        }
        return restore(current, reason)
    }

    fun bootReapply(): TurboOutcome {
        val current = store.load()
        if (current.phase == TurboPhase.NORMAL && current.baselineMode == null) {
            return TurboOutcome(true, false, current.copy(desiredTurbo = false), "NORMAL retained after reboot")
        }

        if (current.phase != TurboPhase.TURBO_VERIFIED || !current.desiredTurbo) {
            return if (current.baselineMode != null) {
                restore(current, "Recovering NORMAL after incomplete TURBO state")
            } else {
                recovery(current, "Recovery required: saved TURBO state is incomplete")
            }
        }

        // Safety order is deliberate: current thermal state is obtained before any performance write.
        val thermalStatus = thermal.currentStatus()
        if (thermalStatus == null) {
            return restore(current, "TURBO off: thermal protection is unavailable")
        }
        if (thermalStatus >= severeStatus) {
            return restore(
                current,
                "TURBO off: Shield reached SEVERE thermal status",
                thermalFallbackStatus = thermalStatus
            )
        }

        return try {
            val observed = control.readState()
            if (observed == ProcessorModeActuatorProof.EXPECTED_MAX) {
                val retained = current.copy(
                    lastReason = "TURBO retained after reboot",
                    lastChangeEpochMs = clock.now(),
                    lastThermalStatus = thermalStatus
                )
                if (store.save(retained)) {
                    TurboOutcome(true, false, retained, "TURBO retained: NVIDIA Max performance already verified")
                } else {
                    restore(current, "TURBO off: reboot state could not be saved")
                }
            } else {
                control.writeMode(MAX_MODE)
                val reapplied = control.waitForState(ProcessorModeActuatorProof.EXPECTED_MAX)
                if (reapplied != ProcessorModeActuatorProof.EXPECTED_MAX) {
                    throw IllegalStateException("Max processor state did not verify after reboot")
                }
                val active = current.copy(
                    phase = TurboPhase.TURBO_VERIFIED,
                    desiredTurbo = true,
                    lastReason = "TURBO reapplied after reboot",
                    lastChangeEpochMs = clock.now(),
                    lastThermalStatus = thermalStatus
                )
                if (!store.save(active)) {
                    restore(current, "TURBO off: reboot verification could not be saved")
                } else {
                    TurboOutcome(true, true, active, "TURBO reapplied after reboot")
                }
            }
        } catch (failure: Exception) {
            restore(current, "TURBO off: reboot reapply failed: ${detail(failure)}")
        }
    }

    fun onThermalStatus(status: Int): TurboOutcome {
        val current = store.load()
        if (current.phase != TurboPhase.TURBO_VERIFIED || !current.desiredTurbo) {
            val updated = current.copy(lastThermalStatus = status)
            if (updated != current) store.save(updated)
            return TurboOutcome(true, false, updated, "TURBO is not active")
        }
        if (status < severeStatus) {
            val updated = current.copy(lastThermalStatus = status)
            if (updated != current) store.save(updated)
            return TurboOutcome(true, false, updated, "Thermal status below SEVERE")
        }
        return restore(
            current,
            "TURBO off: Shield reached SEVERE thermal status",
            thermalFallbackStatus = status
        )
    }

    private fun rollbackEnableFailure(source: TurboSnapshot, reason: String): TurboOutcome {
        val restored = restore(source, reason)
        return if (restored.snapshot.phase == TurboPhase.NORMAL) {
            restored.copy(
                success = false,
                message = "$reason; original NORMAL restored and verified"
            )
        } else {
            restored.copy(success = false)
        }
    }

    private fun restore(
        source: TurboSnapshot,
        reason: String,
        thermalFallbackStatus: Int? = null
    ): TurboOutcome {
        val baselineMode = source.baselineMode
            ?: return recovery(source.copy(desiredTurbo = false), "CHECK PERFORMANCE SETTINGS: NORMAL baseline is missing")
        val expected = expectedForMode(baselineMode)
            ?: return recovery(source.copy(desiredTurbo = false), "CHECK PERFORMANCE SETTINGS: saved processor mode is invalid")
        val now = clock.now()
        val restoring = source.copy(
            phase = TurboPhase.RESTORING,
            desiredTurbo = false,
            lastReason = reason,
            lastChangeEpochMs = now,
            lastThermalStatus = thermalFallbackStatus ?: source.lastThermalStatus,
            lastThermalFallbackEpochMs = if (thermalFallbackStatus != null) now else source.lastThermalFallbackEpochMs
        )
        // Preserve the baseline in persistent state before attempting the restore.
        store.save(restoring)

        return try {
            control.writeMode(baselineMode)
            val observed = control.waitForState(expected)
            if (observed != expected) throw IllegalStateException("Original processor mode did not verify")
            val normal = restoring.copy(
                phase = TurboPhase.NORMAL,
                desiredTurbo = false,
                baselineMode = null,
                appliedControls = emptySet(),
                lastReason = if (thermalFallbackStatus != null) {
                    "TURBO OFF: Shield reached SEVERE thermal status; NORMAL verified"
                } else {
                    "NORMAL restored: original processor settings verified"
                },
                lastChangeEpochMs = clock.now()
            )
            if (store.save(normal)) {
                TurboOutcome(true, true, normal, normal.lastReason)
            } else {
                recovery(restoring, "CHECK PERFORMANCE SETTINGS: restored NORMAL state could not be saved")
            }
        } catch (failure: Exception) {
            recovery(restoring, "CHECK PERFORMANCE SETTINGS: restore could not be fully verified: ${detail(failure)}")
        }
    }

    private fun recovery(source: TurboSnapshot, reason: String): TurboOutcome {
        val recovery = source.copy(
            phase = TurboPhase.RECOVERY_REQUIRED,
            desiredTurbo = false,
            lastReason = reason,
            lastChangeEpochMs = clock.now()
        )
        store.save(recovery)
        return TurboOutcome(false, true, recovery, reason)
    }

    private fun expectedForMode(mode: Int?): ProcessorModeActuatorState? = when (mode) {
        OPTIMIZED_MODE -> ProcessorModeActuatorProof.EXPECTED_OPTIMIZED
        MAX_MODE -> ProcessorModeActuatorProof.EXPECTED_MAX
        else -> null
    }

    private fun detail(failure: Exception): String =
        failure.message?.take(240)?.ifBlank { failure.javaClass.simpleName } ?: failure.javaClass.simpleName

    private companion object {
        const val MAX_MODE = 0
        const val OPTIMIZED_MODE = 1
    }
}
