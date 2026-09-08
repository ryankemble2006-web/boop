package com.boop.shieldturbo

import com.boop.shieldturbo.ui.TurboSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TurboSectionTest {
    @Test
    fun sectionKeysAreStable() {
        assertEquals(TurboSection.TURBO, TurboSection.fromKey("turbo"))
        assertEquals(TurboSection.PICTURE, TurboSection.fromKey("picture"))
        assertEquals(TurboSection.APPS, TurboSection.fromKey("apps"))
        assertEquals(TurboSection.NETWORK, TurboSection.fromKey("network"))
        assertEquals(TurboSection.SHIELD, TurboSection.fromKey("shield"))
        assertNull(TurboSection.fromKey("adb"))
    }
}