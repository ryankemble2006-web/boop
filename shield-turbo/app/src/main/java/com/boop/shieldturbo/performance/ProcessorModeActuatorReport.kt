package com.boop.shieldturbo.performance

object ProcessorModeActuatorReport {
    fun format(result: ProcessorModeActuatorProofResult): String = buildString {
        appendLine("SHIELD TURBO • NVIDIA ACTUATOR PROOF")
        appendLine("RESULT • ${if (result.success) "PASS" else "FAIL"}")
        appendLine("BASELINE • ${stateText(result.baseline)}")
        appendLine("REQUEST MAX • settings put system nv_power_mode 0")
        if (result.maxVerified && result.maxObserved != null) {
            appendLine("MAX READBACK • ${stateText(result.maxObserved)} • VERIFIED")
        } else {
            appendLine("MAX • NOT VERIFIED${reason(result.maxFailure)}")
            result.maxObserved?.let { appendLine("MAX READBACK • ${stateText(it)}") }
        }
        appendLine("RESTORE • settings put system nv_power_mode 1")
        if (result.restoreVerified && result.finalState != null) {
            appendLine("RESTORE READBACK • ${stateText(result.finalState)} • VERIFIED")
        } else {
            appendLine("RESTORE • NOT VERIFIED${reason(result.restoreFailure)}")
            result.finalState?.let { appendLine("RESTORE READBACK • ${stateText(it)}") }
        }
        appendLine("DIRECT VENDOR WRITES • NONE")
        append("FINAL STATE • ${if (result.restoreVerified) "OPTIMIZED VERIFIED" else "NOT VERIFIED • SET SHIELD PROCESSOR MODE TO OPTIMIZED MANUALLY"}")
    }

    private fun stateText(state: ProcessorModeActuatorState): String =
        "mode=${state.mode ?: "?"} cpu=${state.cpuBoost ?: "?"} gpu=${state.gpuBoost ?: "?"} frt=${state.frtBoost ?: "?"} min=${state.frtMin ?: "?"}"

    private fun reason(value: String?): String = value?.let { " • $it" } ?: ""
}
