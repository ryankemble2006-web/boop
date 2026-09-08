package com.boop.shieldturbo.performance

object ProcessorModeTraceReport {
    fun format(changes: List<ProcessorModeTraceChange>): String {
        if (changes.isEmpty()) {
            return "NO CHANGED SETTINGS OR PROPERTIES\nThe Shield did not expose a difference in this capture."
        }
        return changes
            .sortedWith(compareByDescending<ProcessorModeTraceChange> { candidateScore(it) }
                .thenBy { it.source }
                .thenBy { it.key })
            .joinToString("\n") { change ->
                "${change.source}:${change.key} • ${change.before ?: MISSING} -> ${change.after ?: MISSING}"
            }
    }

    private fun candidateScore(change: ProcessorModeTraceChange): Int {
        val key = change.key.lowercase()
        var score = 0
        if ("power" in key && "mode" in key) score += 12
        if ("nvidia" in key) score += 8
        if ("performance" in key || "perf" in key) score += 6
        if ("mode" in key || "profile" in key) score += 5
        if ("tegra" in key || "cpu" in key || "gpu" in key) score += 4
        if ("thermal" in key || "fan" in key) score += 3
        if ("vendor" in key || change.source == "property") score += 2
        return score
    }

    private const val MISSING = "<missing>"
}
