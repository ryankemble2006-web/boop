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
