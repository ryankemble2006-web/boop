package com.boop.shieldturbo.cleanstart

import org.junit.Assert.*
import org.junit.Test

class CleanStartStoreTest {
    private class MemoryStore : CleanStartStore.Store {
        private val values = linkedMapOf<String, String>()
        override fun get(key: String): String? = values[key]
        override fun put(key: String, value: String): Boolean { values[key] = value; return true }
        override fun remove(key: String): Boolean { values.remove(key); return true }
    }

    @Test fun targetsAreValidatedSortedAndIndividuallyRemovable() {
        val store = CleanStartStore(MemoryStore())
        assertTrue(store.setTarget("org.xbmc.zeta", true))
        assertTrue(store.setTarget("org.xbmc.alpha", true))
        assertFalse(store.setTarget("com.boop.shieldturbo", true))
        assertEquals(linkedSetOf("org.xbmc.alpha", "org.xbmc.zeta"), store.targets())
        assertTrue(store.setTarget("org.xbmc.alpha", false))
        assertEquals(linkedSetOf("org.xbmc.zeta"), store.targets())
    }

    @Test fun autoCleanIsExplicitAndDefaultsOff() {
        val store = CleanStartStore(MemoryStore())
        assertFalse(store.autoEnabled())
        assertTrue(store.setAutoEnabled(true))
        assertTrue(store.autoEnabled())
        assertTrue(store.setAutoEnabled(false))
        assertFalse(store.autoEnabled())
    }

    @Test fun conciseSummaryRoundTripsWithoutRawShellData() {
        val store = CleanStartStore(MemoryStore())
        val summary = CleanStartSummary(
            timestampMillis = 123456L,
            items = listOf(
                CleanStartItem("org.xbmc.alpha", CleanStartStatus.STOPPED, "verified"),
                CleanStartItem("org.xbmc.zeta", CleanStartStatus.SKIPPED_IN_USE, "foreground")
            )
        )
        assertTrue(store.recordSummary(summary))
        assertEquals(summary, store.lastSummary())
    }

    @Test fun malformedStoredDataFailsClosed() {
        val backing = MemoryStore()
        val store = CleanStartStore(backing)
        backing.put("targets", "org.xbmc.good\n;reboot\ncom.boop.shieldturbo")
        assertEquals(linkedSetOf("org.xbmc.good"), store.targets())
        backing.put("last_summary", "garbage")
        assertNull(store.lastSummary())
    }
}
