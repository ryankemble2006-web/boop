package com.boop.shieldturbo.system

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemShortcutTest {
    @Test fun ordinaryShortcutsKeepTheirPublicRouteCandidates() {
        SystemShortcut.values().filter { it != SystemShortcut.DISPLAY_SOUND }.forEach { shortcut ->
            assertFalse(shortcut.actions().isEmpty())
        }
    }

    @Test fun displaySoundMustNotSilentlyOpenUnrelatedSettings() {
        // Ryan's Shield opened general Settings instead of the combined native page.
        // A broad Settings, display-only or sound-only fallback is not that destination.
        assertTrue(SystemShortcut.DISPLAY_SOUND.actions().isEmpty())
    }

    @Test fun physicallyConfirmedDeveloperRouteIsUnchanged() {
        assertEquals(
            listOf("android.settings.APPLICATION_DEVELOPMENT_SETTINGS"),
            SystemShortcut.DEVELOPER.actions()
        )
    }
}
