package com.boop.shieldturbo.performance

import com.boop.shieldturbo.model.ProbeResult

object CompactAnalysisReport {
    fun format(readings: List<ProbeResult>): String = readings.joinToString("\n") { reading ->
        listOf(
            reading.label,
            "[${reading.status.name.replace('_', ' ')}]",
            compact(reading.value),
            compact(reading.evidence)
        ).filter { it.isNotBlank() }.joinToString(" • ")
    }

    private fun compact(value: String): String = value
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" | ")
}
