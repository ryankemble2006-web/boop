package com.boop.shieldturbo.performance

object ProcessorModeActuatorPolicy {
    val readCommands = listOf(
        "settings get system nv_power_mode",
        "getprop persist.vendor.sys.phs.cpufreq.boost",
        "getprop persist.vendor.sys.phs.gpufreq.boost",
        "getprop persist.vendor.sys.phs.frt.boost",
        "getprop persist.vendor.sys.phs.frt.min"
    )

    fun writeModeCommand(mode: Int): String {
        require(mode == 0 || mode == 1) { "Only NVIDIA Max(0) and Optimized(1) are allowed" }
        return "settings put system nv_power_mode $mode"
    }

    fun parseState(outputs: List<String>): ProcessorModeActuatorState {
        require(outputs.size == readCommands.size) { "Incomplete NVIDIA power-mode readback" }
        return ProcessorModeActuatorState(
            mode = outputs[0].trim().toIntOrNull(),
            cpuBoost = outputs[1].trim().toIntOrNull(),
            gpuBoost = outputs[2].trim().toIntOrNull(),
            frtBoost = outputs[3].trim().toIntOrNull(),
            frtMin = outputs[4].trim().toIntOrNull()
        )
    }
}
