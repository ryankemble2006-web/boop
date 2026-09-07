package com.boop.shieldturbo

import com.boop.shieldturbo.analysis.ShieldAnalyzer
import com.boop.shieldturbo.model.*
import com.boop.shieldturbo.probe.*
import com.boop.shieldturbo.privilege.*
import org.junit.Assert.*
import org.junit.Test

class RegressionTest {
    private fun source(value: SourceRead) = object : FileSourceReader() {
        override fun readFirst(paths: List<String>) = value
    }
    @Test fun missingCpuIsUnsupported() {
        assertEquals(ProbeStatus.UNSUPPORTED, CpuProbe(source(SourceRead.Missing)).read().status)
    }
    @Test fun permissionDeniedIsNotMissing() {
        assertEquals(ProbeStatus.RESTRICTED, CpuProbe(source(SourceRead.Restricted("denied"))).read().status)
    }
    @Test fun validCpuReportsMHz() {
        assertEquals("1912 MHz", CpuProbe(source(SourceRead.Value("cpu", "1912000"))).read().value)
    }
    @Test fun negativeCpuIsRejected() {
        assertEquals(ProbeStatus.ERROR, CpuProbe(source(SourceRead.Value("cpu", "-1"))).read().status)
    }
    @Test fun malformedCpuIsRejected() {
        assertEquals(ProbeStatus.ERROR, CpuProbe(source(SourceRead.Value("cpu", "unknown"))).read().status)
    }
    @Test fun thermalUsesMillidegreesAtTheBoundary() {
        assertEquals("1.0 °C", ThermalProbe(source(SourceRead.Value("temp", "1000"))).read().value)
    }
    @Test fun thermalUsesMillidegreesForSubZeroValues() {
        assertEquals("-5.0 °C", ThermalProbe(source(SourceRead.Value("temp", "-5000"))).read().value)
    }
    @Test fun thermalRejectsNonFiniteValues() {
        listOf("NaN", "Infinity", "-Infinity").forEach {
            assertEquals(ProbeStatus.ERROR, ThermalProbe(source(SourceRead.Value("temp", it))).read().status)
        }
    }
    @Test fun oneDeniedProbeDoesNotAbortTheOtherProbes() {
        val denied = Probe { throw SecurityException("private details must not escape") }
        val good = Probe { ProbeResult("ok", "Good probe", ProbeStatus.AVAILABLE, "Ready", "test") }
        val snapshot = ShieldAnalyzer(listOf(denied, good)) { 123L }.analyze()
        assertEquals(123L, snapshot.capturedAtMillis)
        assertEquals(2, snapshot.results.size)
        assertEquals(ProbeStatus.RESTRICTED, snapshot.results[0].status)
        assertFalse(snapshot.results[0].evidence.contains("private details"))
        assertEquals("ok", snapshot.results[1].key)
    }
    @Test fun unexpectedProbeFailureIsContained() {
        val snapshot = ShieldAnalyzer(listOf(Probe { throw IllegalStateException("private") })).analyze()
        assertEquals(ProbeStatus.ERROR, snapshot.results.single().status)
        assertFalse(snapshot.results.single().evidence.contains("private"))
    }
    @Test fun unavailablePrivilegeEvidenceFailsClosed() {
        val detector = PrivilegeDetector({ throw SecurityException() }, { false })
        assertEquals(PrivilegeTier.STANDARD, detector.detect())
    }
}
