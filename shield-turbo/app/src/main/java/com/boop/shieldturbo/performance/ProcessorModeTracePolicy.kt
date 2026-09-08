package com.boop.shieldturbo.performance

object ProcessorModeTracePolicy {
    val captureCommands = listOf(
        "settings list global",
        "settings list secure",
        "settings list system",
        "getprop"
    )
}
