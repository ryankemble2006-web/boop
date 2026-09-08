package com.boop.shieldturbo.startup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupLedgerTest {
    private class MemoryStore : StartupLedger.Store {
        val values = linkedMapOf<String, String>()
        override fun get(key: String): String? = values[key]
        override fun putIfAbsent(key: String, value: String): Boolean {
            if (values.containsKey(key)) return true
            values[key] = value
            return true
        }
        override fun put(key: String, value: String): Boolean { values[key] = value; return true }
        override fun remove(key: String): Boolean { values.remove(key); return true }
        override fun entries(): Map<String, String> = values.toMap()
    }

    private val original = OriginalStartupState("org.xbmc.kodi", "allow", "default", "default")

    @Test fun firstOriginalWinsAcrossLaterChanges() {
        val ledger = StartupLedger(MemoryStore())
        assertTrue(ledger.rememberOriginal(original))
        assertTrue(ledger.rememberOriginal(original.copy(runInBackground = "deny")))
        assertEquals("allow", ledger.record("org.xbmc.kodi")!!.original.runInBackground)
    }

    @Test fun managedModeCanChangeWithoutReplacingOriginal() {
        val ledger = StartupLedger(MemoryStore())
        ledger.rememberOriginal(original)
        ledger.markMode("org.xbmc.kodi", ManagedStartupMode.BACKGROUND_BLOCK)
        ledger.markMode("org.xbmc.kodi", ManagedStartupMode.HARD_BLOCK)
        val record = ledger.record("org.xbmc.kodi")!!
        assertEquals(ManagedStartupMode.HARD_BLOCK, record.mode)
        assertEquals(original, record.original)
    }

    @Test fun removalIsPerAppAndMalformedRecordsFailClosed() {
        val store = MemoryStore()
        val ledger = StartupLedger(store)
        ledger.rememberOriginal(original)
        ledger.rememberOriginal(OriginalStartupState("com.example.two", "ignore", "deny", "enabled"))
        assertTrue(ledger.remove("org.xbmc.kodi"))
        assertNull(ledger.record("org.xbmc.kodi"))
        assertTrue(ledger.record("com.example.two") != null)
        store.values["entry.bad.package"] = "garbage"
        assertFalse(ledger.records().any { it.original.packageName == "bad.package" })
    }
}
