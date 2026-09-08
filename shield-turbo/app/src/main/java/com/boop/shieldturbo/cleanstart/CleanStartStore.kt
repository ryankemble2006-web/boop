package com.boop.shieldturbo.cleanstart

import android.content.Context

enum class CleanStartStatus { STOPPED, SKIPPED_IN_USE, FAILED, NOT_APPLIED }

data class CleanStartItem(
    val packageName: String,
    val status: CleanStartStatus,
    val detail: String = ""
)

data class CleanStartSummary(
    val timestampMillis: Long,
    val items: List<CleanStartItem>
)

enum class CleanStartIndicatorWindowMode {
    NOT_ATTEMPTED,
    APPLICATION_CONTEXT,
    DISPLAY_WINDOW_CONTEXT,
    FALLBACK_APPLICATION_CONTEXT
}

enum class CleanStartIndicatorAddStatus { NOT_ATTEMPTED, ADDED, FAILED }

enum class CleanStartIndicatorPresentationStatus {
    NOT_ATTEMPTED,
    BYPASSED,
    DRAWN,
    FRAME_COMMITTED,
    TIMEOUT
}

data class CleanStartIndicatorDiagnostic(
    val timestampMillis: Long,
    val overlayAllowed: Boolean,
    val windowMode: CleanStartIndicatorWindowMode,
    val addStatus: CleanStartIndicatorAddStatus,
    val presentationStatus: CleanStartIndicatorPresentationStatus,
    val elapsedMs: Long,
    val detail: String = ""
)

data class CleanStartTimingDiagnostic(
    val timestampMillis: Long,
    val noticeMs: Long,
    val adbReadyMs: Long,
    val resumedQueryMs: Long,
    val stopsTotalMs: Long,
    val slowestPackage: String,
    val slowestStopMs: Long,
    val totalJobMs: Long
)

class CleanStartStore(private val store: Store) {
    interface Store {
        fun get(key: String): String?
        fun put(key: String, value: String): Boolean
        fun remove(key: String): Boolean
    }

    companion object {
        private const val TARGETS = "targets"
        private const val AUTO = "auto_clean"
        private const val SUMMARY = "last_summary"
        private const val INDICATOR_DIAGNOSTIC = "last_indicator_diagnostic"
        private const val TIMING_DIAGNOSTIC = "last_timing_diagnostic"
        private const val MAX_DIAGNOSTIC_MS = 120_000L

        fun android(context: Context): CleanStartStore = CleanStartStore(PreferencesStore(context))
    }

    fun targets(): LinkedHashSet<String> = store.get(TARGETS)
        .orEmpty()
        .lineSequence()
        .map(String::trim)
        .filter(CleanStartPolicy::validPackage)
        .distinct()
        .sortedWith(String.CASE_INSENSITIVE_ORDER)
        .toCollection(linkedSetOf())

    fun setTarget(packageName: String, enabled: Boolean): Boolean {
        if (!CleanStartPolicy.validPackage(packageName)) return false
        val next = targets()
        if (enabled) next += packageName else next -= packageName
        return if (next.isEmpty()) store.remove(TARGETS)
        else store.put(TARGETS, next.sortedWith(String.CASE_INSENSITIVE_ORDER).joinToString("\n"))
    }

    fun autoEnabled(): Boolean = store.get(AUTO) == "1"

    fun setAutoEnabled(enabled: Boolean): Boolean =
        if (enabled) store.put(AUTO, "1") else store.remove(AUTO)

    fun recordSummary(summary: CleanStartSummary): Boolean {
        val encoded = encodeSummary(summary) ?: return false
        return store.put(SUMMARY, encoded)
    }

    fun lastSummary(): CleanStartSummary? = store.get(SUMMARY)?.let(::decodeSummary)

    fun recordIndicatorDiagnostic(diagnostic: CleanStartIndicatorDiagnostic): Boolean {
        if (diagnostic.timestampMillis < 0 || diagnostic.elapsedMs !in 0..60_000L) return false
        val detail = sanitizeDetail(diagnostic.detail)
        val encoded = listOf(
            "v1",
            diagnostic.timestampMillis.toString(),
            if (diagnostic.overlayAllowed) "1" else "0",
            diagnostic.windowMode.name,
            diagnostic.addStatus.name,
            diagnostic.presentationStatus.name,
            diagnostic.elapsedMs.toString(),
            detail
        ).joinToString("\t")
        return store.put(INDICATOR_DIAGNOSTIC, encoded)
    }

    fun lastIndicatorDiagnostic(): CleanStartIndicatorDiagnostic? {
        val raw = store.get(INDICATOR_DIAGNOSTIC) ?: return null
        val fields = raw.split('\t', limit = 8)
        if (fields.size != 8 || fields[0] != "v1") return null
        val timestamp = fields[1].toLongOrNull()?.takeIf { it >= 0 } ?: return null
        val overlayAllowed = when (fields[2]) {
            "1" -> true
            "0" -> false
            else -> return null
        }
        val windowMode = runCatching { CleanStartIndicatorWindowMode.valueOf(fields[3]) }.getOrNull() ?: return null
        val addStatus = runCatching { CleanStartIndicatorAddStatus.valueOf(fields[4]) }.getOrNull() ?: return null
        val presentationStatus = runCatching {
            CleanStartIndicatorPresentationStatus.valueOf(fields[5])
        }.getOrNull() ?: return null
        val elapsed = fields[6].toLongOrNull()?.takeIf { it in 0..60_000L } ?: return null
        return CleanStartIndicatorDiagnostic(
            timestamp,
            overlayAllowed,
            windowMode,
            addStatus,
            presentationStatus,
            elapsed,
            sanitizeDetail(fields[7])
        )
    }

    fun recordTimingDiagnostic(diagnostic: CleanStartTimingDiagnostic): Boolean {
        if (diagnostic.timestampMillis < 0) return false
        val durations = listOf(
            diagnostic.noticeMs,
            diagnostic.adbReadyMs,
            diagnostic.resumedQueryMs,
            diagnostic.stopsTotalMs,
            diagnostic.slowestStopMs,
            diagnostic.totalJobMs
        )
        if (durations.any { it !in 0..MAX_DIAGNOSTIC_MS }) return false
        val slowestPackage = diagnostic.slowestPackage.trim()
        if (slowestPackage.isNotEmpty() && !CleanStartPolicy.validPackage(slowestPackage)) return false
        val encoded = listOf(
            "v1",
            diagnostic.timestampMillis.toString(),
            diagnostic.noticeMs.toString(),
            diagnostic.adbReadyMs.toString(),
            diagnostic.resumedQueryMs.toString(),
            diagnostic.stopsTotalMs.toString(),
            slowestPackage,
            diagnostic.slowestStopMs.toString(),
            diagnostic.totalJobMs.toString()
        ).joinToString("\t")
        return store.put(TIMING_DIAGNOSTIC, encoded)
    }

    fun lastTimingDiagnostic(): CleanStartTimingDiagnostic? {
        val raw = store.get(TIMING_DIAGNOSTIC) ?: return null
        val fields = raw.split('\t', limit = 9)
        if (fields.size != 9 || fields[0] != "v1") return null
        val timestamp = fields[1].toLongOrNull()?.takeIf { it >= 0 } ?: return null
        fun duration(index: Int): Long? = fields[index].toLongOrNull()?.takeIf { it in 0..MAX_DIAGNOSTIC_MS }
        val slowestPackage = fields[6].trim()
        if (slowestPackage.isNotEmpty() && !CleanStartPolicy.validPackage(slowestPackage)) return null
        return CleanStartTimingDiagnostic(
            timestampMillis = timestamp,
            noticeMs = duration(2) ?: return null,
            adbReadyMs = duration(3) ?: return null,
            resumedQueryMs = duration(4) ?: return null,
            stopsTotalMs = duration(5) ?: return null,
            slowestPackage = slowestPackage,
            slowestStopMs = duration(7) ?: return null,
            totalJobMs = duration(8) ?: return null
        )
    }

    private fun encodeSummary(summary: CleanStartSummary): String? {
        if (summary.timestampMillis < 0 || summary.items.size > 64) return null
        val lines = mutableListOf("v1\t${summary.timestampMillis}\t${summary.items.size}")
        for (item in summary.items) {
            if (!CleanStartPolicy.validPackage(item.packageName)) return null
            val detail = sanitizeDetail(item.detail)
            lines += "${item.packageName}\t${item.status.name}\t$detail"
        }
        return lines.joinToString("\n")
    }

    private fun decodeSummary(raw: String): CleanStartSummary? {
        val lines = raw.lineSequence().toList()
        if (lines.isEmpty()) return null
        val head = lines.first().split('\t')
        if (head.size != 3 || head[0] != "v1") return null
        val timestamp = head[1].toLongOrNull()?.takeIf { it >= 0 } ?: return null
        val count = head[2].toIntOrNull()?.takeIf { it in 0..64 } ?: return null
        if (lines.size != count + 1) return null
        val items = lines.drop(1).map { line ->
            val fields = line.split('\t', limit = 3)
            if (fields.size != 3 || !CleanStartPolicy.validPackage(fields[0])) return null
            val status = runCatching { CleanStartStatus.valueOf(fields[1]) }.getOrNull() ?: return null
            CleanStartItem(fields[0], status, fields[2])
        }
        return CleanStartSummary(timestamp, items)
    }

    private fun sanitizeDetail(value: String): String = value
        .replace('\t', ' ')
        .replace('\n', ' ')
        .replace('\r', ' ')
        .trim()
        .take(160)

    private class PreferencesStore(context: Context) : Store {
        private val prefs = context.getSharedPreferences("turbo_clean_start", Context.MODE_PRIVATE)
        override fun get(key: String): String? = prefs.getString(key, null)
        override fun put(key: String, value: String): Boolean = prefs.edit().putString(key, value).commit()
        override fun remove(key: String): Boolean = prefs.edit().remove(key).commit()
    }
}
