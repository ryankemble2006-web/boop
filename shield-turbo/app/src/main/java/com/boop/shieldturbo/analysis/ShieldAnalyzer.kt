package com.boop.shieldturbo.analysis

import com.boop.shieldturbo.model.ProbeResult
import com.boop.shieldturbo.probe.Probe

data class AnalysisSnapshot(val capturedAtMillis: Long, val results: List<ProbeResult>)
class ShieldAnalyzer(private val probes: List<Probe>, private val clock: () -> Long = System::currentTimeMillis) {
    fun analyze() = AnalysisSnapshot(clock(), probes.map { it.read() })
}
