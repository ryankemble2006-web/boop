package com.boop.shieldturbo.analysis

import com.boop.shieldturbo.model.ProbeResult
import com.boop.shieldturbo.model.ProbeStatus
import com.boop.shieldturbo.probe.Probe
import java.util.Collections

/** A single on-demand snapshot. One unavailable source cannot discard other readings. */
data class AnalysisSnapshot(val capturedAtMillis: Long, val results: List<ProbeResult>)

class ShieldAnalyzer(
    private val probes: List<Probe>,
    private val clock: () -> Long = System::currentTimeMillis
) {
    fun analyze(): AnalysisSnapshot {
        val capturedAt = clock()
        val readings = probes.mapIndexed { index, probe ->
            if (Thread.currentThread().isInterrupted) throw InterruptedException()
            try {
                probe.read()
            } catch (interrupted: InterruptedException) {
                Thread.currentThread().interrupt()
                throw interrupted
            } catch (denied: SecurityException) {
                unavailable(index, probe, ProbeStatus.RESTRICTED, "Android denied this reading", denied)
            } catch (failure: Exception) {
                unavailable(index, probe, ProbeStatus.ERROR, "Reading unavailable", failure)
            }
        }
        return AnalysisSnapshot(capturedAt, Collections.unmodifiableList(readings))
    }

    private fun unavailable(
        index: Int, probe: Probe, status: ProbeStatus, value: String, cause: Exception
    ): ProbeResult = ProbeResult(
        "probe_$index", probe.javaClass.simpleName.ifBlank { "Device reading" },
        status, value, cause.javaClass.simpleName
    )
}
