package com.boop.shieldturbo.performance

import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessorModeTraceReportTest {
    @Test fun likelyProcessorModeChangesComeFirstWithoutDroppingOtherChanges() {
        val changes = listOf(
            ProcessorModeTraceChange("global", "boring_counter", "1", "2"),
            ProcessorModeTraceChange("property", "persist.vendor.power.mode", "0", "1"),
            ProcessorModeTraceChange("secure", "nvidia_performance_profile", null, "max")
        )

        val report = ProcessorModeTraceReport.format(changes)
        val lines = report.lines()

        assertTrue(lines[0].contains("persist.vendor.power.mode"))
        assertTrue(lines[1].contains("nvidia_performance_profile"))
        assertTrue(report.contains("boring_counter"))
        assertTrue(report.contains("<missing> -> max"))
    }

    @Test fun emptyDiffExplainsThatNoChangeWasVisible() {
        assertTrue(ProcessorModeTraceReport.format(emptyList()).contains("NO CHANGED SETTINGS OR PROPERTIES"))
    }
}
