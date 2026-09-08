package com.boop.shieldturbo.performance

import org.junit.Assert.assertEquals
import org.junit.Test

class ProcessorModeTraceCaptureTest {
    @Test fun captureReadsExactlyTheApprovedSourcesInOrder() {
        val seen = mutableListOf<String>()
        val outputs = mapOf(
            "settings list global" to "mode=optimized\n",
            "settings list secure" to "secure_flag=yes\n",
            "settings list system" to "system_flag=yes\n",
            "getprop" to "[persist.vendor.mode]: [0]\n"
        )

        val snapshot = ProcessorModeTraceCapture.capture { command ->
            seen += command
            outputs.getValue(command)
        }

        assertEquals(ProcessorModeTracePolicy.captureCommands, seen)
        val expected = ProcessorModeTrace.snapshotFromText(
            outputs.getValue("settings list global"),
            outputs.getValue("settings list secure"),
            outputs.getValue("settings list system"),
            outputs.getValue("getprop")
        )
        assertEquals(emptyList<ProcessorModeTraceChange>(), ProcessorModeTrace.diff(expected, snapshot))
    }
}
