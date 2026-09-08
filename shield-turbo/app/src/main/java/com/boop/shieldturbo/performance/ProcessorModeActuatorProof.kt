package com.boop.shieldturbo.performance

data class ProcessorModeActuatorState(
    val mode: Int?,
    val cpuBoost: Int?,
    val gpuBoost: Int?,
    val frtBoost: Int?,
    val frtMin: Int?
)

data class ProcessorModeActuatorProofResult(
    val baseline: ProcessorModeActuatorState,
    val maxObserved: ProcessorModeActuatorState?,
    val finalState: ProcessorModeActuatorState?,
    val maxVerified: Boolean,
    val restoreVerified: Boolean,
    val maxFailure: String?,
    val restoreFailure: String?
) {
    val success: Boolean get() = maxVerified && restoreVerified
}

object ProcessorModeActuatorProof {
    val EXPECTED_OPTIMIZED = ProcessorModeActuatorState(1, 0, 0, 0, 15)
    val EXPECTED_MAX = ProcessorModeActuatorState(0, 5, 5, 5, 20)

    fun run(
        readState: () -> ProcessorModeActuatorState,
        writeMode: (Int) -> Unit,
        pause: (Long) -> Unit
    ): ProcessorModeActuatorProofResult {
        val baseline = readState()
        if (baseline != EXPECTED_OPTIMIZED) {
            return ProcessorModeActuatorProofResult(
                baseline = baseline,
                maxObserved = null,
                finalState = baseline,
                maxVerified = false,
                restoreVerified = false,
                maxFailure = "Baseline is not the physically proven Optimized state",
                restoreFailure = "No write was attempted"
            )
        }

        var maxObserved: ProcessorModeActuatorState? = null
        var maxVerified = false
        var maxFailure: String? = null
        try {
            writeMode(0)
            val sampled = pollFor(EXPECTED_MAX, readState, pause)
            maxObserved = sampled.last
            maxVerified = sampled.matched
            if (!maxVerified) maxFailure = "Max state did not converge to 0/5/5/5/20"
        } catch (failure: Exception) {
            maxFailure = failure.message ?: failure.javaClass.simpleName
        }

        var finalState: ProcessorModeActuatorState? = null
        var restoreVerified = false
        var restoreFailure: String? = null
        try {
            writeMode(1)
            val sampled = pollFor(EXPECTED_OPTIMIZED, readState, pause)
            finalState = sampled.last
            restoreVerified = sampled.matched
            if (!restoreVerified) restoreFailure = "Optimized restore did not converge to 1/0/0/0/15"
        } catch (failure: Exception) {
            restoreFailure = failure.message ?: failure.javaClass.simpleName
            finalState = runCatching { readState() }.getOrNull()
        }

        return ProcessorModeActuatorProofResult(
            baseline = baseline,
            maxObserved = maxObserved,
            finalState = finalState,
            maxVerified = maxVerified,
            restoreVerified = restoreVerified,
            maxFailure = maxFailure,
            restoreFailure = restoreFailure
        )
    }

    private fun pollFor(
        expected: ProcessorModeActuatorState,
        readState: () -> ProcessorModeActuatorState,
        pause: (Long) -> Unit
    ): PollResult {
        var last: ProcessorModeActuatorState? = null
        repeat(3) { attempt ->
            last = readState()
            if (last == expected) return PollResult(true, last!!)
            if (attempt < 2) pause(250L)
        }
        return PollResult(false, checkNotNull(last))
    }

    private data class PollResult(
        val matched: Boolean,
        val last: ProcessorModeActuatorState
    )
}
