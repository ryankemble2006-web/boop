package com.boop.shieldturbo.performance

data class ProcessorModeTraceChange(
    val source: String,
    val key: String,
    val before: String?,
    val after: String?
)

data class ProcessorModeTraceSnapshot internal constructor(
    internal val entries: Map<TraceKey, String>
)

internal data class TraceKey(val source: String, val key: String)

object ProcessorModeTrace {
    fun snapshotFromText(
        global: String,
        secure: String,
        system: String,
        properties: String
    ): ProcessorModeTraceSnapshot {
        val entries = linkedMapOf<TraceKey, String>()
        parseSettings("global", global, entries)
        parseSettings("secure", secure, entries)
        parseSettings("system", system, entries)
        parseProperties(properties, entries)
        return ProcessorModeTraceSnapshot(entries.toMap())
    }

    fun diff(
        before: ProcessorModeTraceSnapshot,
        after: ProcessorModeTraceSnapshot
    ): List<ProcessorModeTraceChange> {
        val keys = (before.entries.keys + after.entries.keys)
            .distinct()
            .sortedWith(compareBy<TraceKey>({ it.source }, { it.key }))
        return keys.mapNotNull { key ->
            val old = before.entries[key]
            val new = after.entries[key]
            if (old == new) null else ProcessorModeTraceChange(key.source, key.key, old, new)
        }
    }

    private fun parseSettings(
        source: String,
        text: String,
        destination: MutableMap<TraceKey, String>
    ) {
        text.lineSequence().forEach { raw ->
            val line = raw.trim()
            val separator = line.indexOf('=')
            if (separator <= 0) return@forEach
            val key = line.substring(0, separator).trim()
            if (key.isEmpty()) return@forEach
            val value = line.substring(separator + 1).trim().take(MAX_VALUE_LENGTH)
            destination[TraceKey(source, key)] = value
        }
    }

    private fun parseProperties(
        text: String,
        destination: MutableMap<TraceKey, String>
    ) {
        text.lineSequence().forEach { raw ->
            val line = raw.trim()
            if (!line.startsWith("[") || !line.endsWith("]")) return@forEach
            val separator = line.indexOf("]: [")
            if (separator <= 1) return@forEach
            val key = line.substring(1, separator).trim()
            if (key.isEmpty()) return@forEach
            val value = line.substring(separator + 4, line.length - 1).take(MAX_VALUE_LENGTH)
            destination[TraceKey("property", key)] = value
        }
    }

    private const val MAX_VALUE_LENGTH = 2048
}
