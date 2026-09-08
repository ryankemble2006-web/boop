package com.boop.shieldturbo.performance

import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessorModeActuatorReportTest {
    private val optimized = ProcessorModeActuatorState(1, 0, 0, 0, 15)
    private val max = ProcessorModeActuatorState(0, 5, 5, 5, 20)

    @Test fun successReportShowsVerifiedRoundTripAndNoVendorWrites() {
        val result = ProcessorModeActuatorProofResult(
            baseline = optimized,
            maxObserved = max,
            finalState = optimized,
            maxVerified = true,
            restoreVerified = true,
            maxFailure = null,
            restoreFailure = null
        )

        val report = ProcessorModeActuatorReport.format(result)

        assertTrue(report.contains("RESULT • PASS"))
        assertTrue(report.contains("BASELINE • mode=1 cpu=0 gpu=0 frt=0 min=15"))
        assertTrue(report.contains("MAX READBACK • mode=0 cpu=5 gpu=5 frt=5 min=20 • VERIFIED"))
        assertTrue(report.contains("RESTORE READBACK • mode=1 cpu=0 gpu=0 frt=0 min=15 • VERIFIED"))
        assertTrue(report.contains("DIRECT VENDOR WRITES • NONE"))
        assertTrue(report.contains("FINAL STATE • OPTIMIZED VERIFIED"))
    }

    @Test fun failedMaxReportStillMakesVerifiedRestoreObvious() {
        val result = ProcessorModeActuatorProofResult(
            baseline = optimized,
            maxObserved = ProcessorModeActuatorState(0, 4, 5, 5, 20),
            finalState = optimized,
            maxVerified = false,
            restoreVerified = true,
            maxFailure = "Max state did not converge",
            restoreFailure = null
        )

        val report = ProcessorModeActuatorReport.format(result)

        assertTrue(report.contains("RESULT • FAIL"))
        assertTrue(report.contains("MAX • NOT VERIFIED"))
        assertTrue(report.contains("RESTORE READBACK • mode=1 cpu=0 gpu=0 frt=0 min=15 • VERIFIED"))
        assertTrue(report.contains("FINAL STATE • OPTIMIZED VERIFIED"))
    }
}
