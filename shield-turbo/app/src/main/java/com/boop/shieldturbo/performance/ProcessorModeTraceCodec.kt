package com.boop.shieldturbo.performance

import java.nio.charset.StandardCharsets
import java.util.Base64

object ProcessorModeTraceCodec {
    fun encode(snapshot: ProcessorModeTraceSnapshot): String = snapshot.entries.entries
        .sortedWith(compareBy<Map.Entry<TraceKey, String>>({ it.key.source }, { it.key.key }))
        .joinToString("\n") { entry ->
            listOf(entry.key.source, entry.key.key, entry.value).joinToString("\t", transform = ::encodePart)
        }

    fun decode(encoded: String): ProcessorModeTraceSnapshot {
        if (encoded.isBlank()) return ProcessorModeTraceSnapshot(emptyMap())
        val entries = linkedMapOf<TraceKey, String>()
        encoded.lineSequence().filter { it.isNotBlank() }.forEach { line ->
            val parts = line.split('\t')
            require(parts.size == 3) { "Invalid processor trace snapshot" }
            val source = decodePart(parts[0])
            val key = decodePart(parts[1])
            val value = decodePart(parts[2])
            entries[TraceKey(source, key)] = value
        }
        return ProcessorModeTraceSnapshot(entries.toMap())
    }

    private fun encodePart(value: String): String = Base64.getEncoder()
        .encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decodePart(value: String): String = String(
        Base64.getDecoder().decode(value),
        StandardCharsets.UTF_8
    )
}
