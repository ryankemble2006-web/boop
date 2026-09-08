package com.boop.shieldturbo.performance

object ProcessorModeTraceCapture {
    fun capture(read: (String) -> String): ProcessorModeTraceSnapshot {
        val outputs = ProcessorModeTracePolicy.captureCommands.map(read)
        return ProcessorModeTrace.snapshotFromText(
            global = outputs[0],
            secure = outputs[1],
            system = outputs[2],
            properties = outputs[3]
        )
    }
}
