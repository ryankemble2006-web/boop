package com.boop.shieldturbo.performance

import com.boop.shieldturbo.model.ProbeResult
import com.boop.shieldturbo.model.ProbeStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompactAnalysisReportTest {
    @Test fun flattensEveryReadingToOneLineWithoutDroppingEvidence() {
        val readings = listOf(
            ProbeResult(
                key = "cpu",
                label = "CPU stock controls",
                status = ProbeStatus.AVAILABLE,
                value = "governor=schedutil",
                evidence = "/sys/cpu/a | read-only\n/sys/cpu/b | writable"
            ),
            ProbeResult(
                key = "thermal",
                label = "Android thermal status",
                status = ProbeStatus.UNSUPPORTED,
                value = "Not exposed",
                evidence = "PowerManager status unavailable"
            )
        )

        val report = CompactAnalysisReport.format(readings)
        val lines = report.lines()

        assertEquals(2, lines.size)
        assertTrue(lines[0].contains("CPU stock controls"))
        assertTrue(lines[0].contains("AVAILABLE"))
        assertTrue(lines[0].contains("governor=schedutil"))
        assertTrue(lines[0].contains("/sys/cpu/a | read-only | /sys/cpu/b | writable"))
        assertTrue(lines[1].contains("Android thermal status"))
        assertTrue(lines[1].contains("UNSUPPORTED"))
        assertTrue(lines[1].contains("PowerManager status unavailable"))
    }
}
