package com.boop.shieldturbo.startup

import android.content.Context

enum class ManagedStartupMode { NONE, BACKGROUND_BLOCK, HARD_BLOCK }

data class OriginalStartupState(
    val packageName: String,
    val runInBackground: String,
    val runAnyInBackground: String,
    val enabledState: String
)

data class StartupRecord(
    val original: OriginalStartupState,
    val mode: ManagedStartupMode = ManagedStartupMode.NONE
)

class StartupLedger(private val store: Store) {
    interface Store {
        fun get(key: String): String?
        fun putIfAbsent(key: String, value: String): Boolean
        fun put(key: String, value: String): Boolean
        fun remove(key: String): Boolean
        fun entries(): Map<String, String>
    }

    companion object {
        private const val PREFIX = "entry."
        private val APP_OP_MODES = setOf("allow", "ignore", "deny", "default")
        private val ENABLED_STATES = setOf("default", "enabled", "disabled", "disabled-user", "disabled-until-used")

        fun android(context: Context): StartupLedger = StartupLedger(PreferencesStore(context))
    }

    fun rememberOriginal(original: OriginalStartupState): Boolean {
        if (!valid(original)) return false
        val key = key(original.packageName)
        val existing = store.get(key)
        if (existing != null) return decode(existing) != null
        return store.putIfAbsent(key, encode(StartupRecord(original)))
    }

    fun markMode(packageName: String, mode: ManagedStartupMode): Boolean {
        val current = record(packageName) ?: return false
        return store.put(key(packageName), encode(current.copy(mode = mode)))
    }

    fun record(packageName: String): StartupRecord? {
        if (!StartupPolicy.validPackage(packageName)) return null
        return store.get(key(packageName))?.let(::decode)
    }

    fun records(): List<StartupRecord> = store.entries()
        .filterKeys { it.startsWith(PREFIX) }
        .values
        .mapNotNull(::decode)
        .sortedBy { it.original.packageName.lowercase() }

    fun remove(packageName: String): Boolean =
        StartupPolicy.validPackage(packageName) && store.remove(key(packageName))

    private fun key(packageName: String) = "$PREFIX$packageName"

    private fun valid(original: OriginalStartupState): Boolean =
        StartupPolicy.validPackage(original.packageName) &&
            original.runInBackground in APP_OP_MODES &&
            original.runAnyInBackground in APP_OP_MODES &&
            original.enabledState in ENABLED_STATES

    private fun encode(record: StartupRecord): String = listOf(
        "v1",
        record.original.packageName,
        record.original.runInBackground,
        record.original.runAnyInBackground,
        record.original.enabledState,
        record.mode.name
    ).joinToString("\t")

    private fun decode(raw: String): StartupRecord? {
        val fields = raw.split('\t')
        if (fields.size != 6 || fields[0] != "v1") return null
        val original = OriginalStartupState(fields[1], fields[2], fields[3], fields[4])
        if (!valid(original)) return null
        val mode = runCatching { ManagedStartupMode.valueOf(fields[5]) }.getOrNull() ?: return null
        return StartupRecord(original, mode)
    }

    private class PreferencesStore(context: Context) : Store {
        private val prefs = context.getSharedPreferences("turbo_startup_ledger", Context.MODE_PRIVATE)

        override fun get(key: String): String? = prefs.getString(key, null)

        override fun putIfAbsent(key: String, value: String): Boolean {
            if (prefs.contains(key)) return true
            return prefs.edit().putString(key, value).commit()
        }

        override fun put(key: String, value: String): Boolean =
            prefs.edit().putString(key, value).commit()

        override fun remove(key: String): Boolean =
            prefs.edit().remove(key).commit()

        override fun entries(): Map<String, String> = prefs.all.mapNotNull { (key, value) ->
            (value as? String)?.let { key to it }
        }.toMap()
    }
}
