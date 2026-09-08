package com.boop.shieldturbo.system

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SystemShortcutTest {
    @Test fun everyShortcutHasAtLeastOnePublicRouteCandidate() {
        SystemShortcut.values().forEach { shortcut ->
            assertFalse(shortcut.actions().isEmpty())
        }
    }

    @Test fun displaySoundUsesTvSettingsFallbackFirst() {
        assertEquals("android.settings.SETTINGS", SystemShortcut.DISPLAY_SOUND.actions().first())
    }
}
