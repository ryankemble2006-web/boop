package com.boop.shieldturbo.performance

import java.nio.charset.StandardCharsets
import java.util.Base64

object TurboStateCodec {
    private val requiredKeys = setOf(
        "schema",
        "phase",
        "desired",
        "baseline",
        "controls",
        "reason",
        "change",
        "thermal",
        "fallback"
    )

    fun encode(snapshot: TurboSnapshot): String {
        require(snapshot.schema == TurboSnapshot.SCHEMA) { "Unsupported TURBO state schema" }
        require(snapshot.baselineMode == null || snapshot.baselineMode == 0 || snapshot.baselineMode == 1) {
            "Unproven processor baseline"
        }
        require(snapshot.appliedControls.all { it == TurboSnapshot.NVIDIA_POWER_MODE }) {
            "Unknown TURBO control"
        }
        require(snapshot.lastChangeEpochMs >= 0L) { "Invalid change time" }
        require(snapshot.lastThermalStatus == null || snapshot.lastThermalStatus in 0..6) { "Invalid thermal status" }
        require(snapshot.lastThermalFallbackEpochMs == null || snapshot.lastThermalFallbackEpochMs >= 0L) {
            "Invalid thermal fallback time"
        }
        val reason = Base64.getUrlEncoder().withoutPadding().encodeToString(
            snapshot.lastReason.toByteArray(StandardCharsets.UTF_8)
        )
        return listOf(
            "schema=${snapshot.schema}",
            "phase=${snapshot.phase.name}",
            "desired=${if (snapshot.desiredTurbo) 1 else 0}",
            "baseline=${snapshot.baselineMode?.toString() ?: ""}",
            "controls=${snapshot.appliedControls.sorted().joinToString(",")}",
            "reason=$reason",
            "change=${snapshot.lastChangeEpochMs}",
            "thermal=${snapshot.lastThermalStatus?.toString() ?: ""}",
            "fallback=${snapshot.lastThermalFallbackEpochMs?.toString() ?: ""}"
        ).joinToString("\n")
    }

    fun decode(raw: String): TurboSnapshot? = runCatching {
        val entries = raw.lineSequence()
            .filter { it.isNotBlank() }
            .map { line ->
                val split = line.indexOf('=')
                require(split > 0) { "Malformed TURBO state" }
                line.substring(0, split) to line.substring(split + 1)
            }
            .toList()
        require(entries.size == requiredKeys.size) { "Incomplete TURBO state" }
        require(entries.map { it.first }.toSet() == requiredKeys) { "Unexpected TURBO state key" }
        val fields = entries.toMap()

        require(fields.getValue("schema").toIntOrNull() == TurboSnapshot.SCHEMA) { "Unsupported TURBO state schema" }
        val phase = TurboPhase.valueOf(fields.getValue("phase"))
        val desired = when (fields.getValue("desired")) {
            "0" -> false
            "1" -> true
            else -> error("Invalid TURBO desired state")
        }
        val baseline = fields.getValue("baseline").takeIf { it.isNotEmpty() }?.toIntOrNull()
        require(baseline == null || baseline == 0 || baseline == 1) { "Unproven processor baseline" }
        val controls = fields.getValue("controls")
            .takeIf { it.isNotEmpty() }
            ?.split(',')
            ?.toSet()
            ?: emptySet()
        require(controls.all { it == TurboSnapshot.NVIDIA_POWER_MODE }) { "Unknown TURBO control" }
        val reason = String(
            Base64.getUrlDecoder().decode(fields.getValue("reason")),
            StandardCharsets.UTF_8
        )
        val change = fields.getValue("change").toLong()
        require(change >= 0L) { "Invalid change time" }
        val thermal = fields.getValue("thermal").takeIf { it.isNotEmpty() }?.toInt()
        require(thermal == null || thermal in 0..6) { "Invalid thermal status" }
        val fallback = fields.getValue("fallback").takeIf { it.isNotEmpty() }?.toLong()
        require(fallback == null || fallback >= 0L) { "Invalid thermal fallback time" }

        TurboSnapshot(
            schema = TurboSnapshot.SCHEMA,
            phase = phase,
            desiredTurbo = desired,
            baselineMode = baseline,
            appliedControls = controls,
            lastReason = reason,
            lastChangeEpochMs = change,
            lastThermalStatus = thermal,
            lastThermalFallbackEpochMs = fallback
        )
    }.getOrNull()
}
