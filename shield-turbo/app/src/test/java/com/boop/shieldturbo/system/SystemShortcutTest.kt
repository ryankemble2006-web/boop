package com.boop.shieldturbo.system

import org.junit.Assert.*
import org.junit.Test

class SystemShortcutTest {
    @Test fun everyShortcutHasAnExplicitRoutingPolicy() {
        SystemShortcut.values().forEach { shortcut ->
            assertTrue(shortcut.actions().isNotEmpty() || shortcut.firmwarePage() != null)
        }
    }
    @Test fun displaySoundMustNotSilentlyOpenUnrelatedSettings() {
        assertTrue(SystemShortcut.DISPLAY_SOUND.actions().isEmpty())
        assertEquals("display", SystemShortcut.DISPLAY_SOUND.firmwarePage())
    }
    @Test fun developerOptionsKeepsItsPhysicallyConfirmedRoute() {
        assertEquals(listOf("android.settings.APPLICATION_DEVELOPMENT_SETTINGS"), SystemShortcut.DEVELOPER.actions())
        assertNull(SystemShortcut.DEVELOPER.firmwarePage())
    }
    @Test fun accessibilityUsesFirmwareDiscoveryNotAnEmptyServicePicker() {
        assertTrue(SystemShortcut.ACCESSIBILITY.actions().isEmpty())
        assertEquals("accessibility", SystemShortcut.ACCESSIBILITY.firmwarePage())
    }
    @Test fun firmwareOverridesAreScopedToTheTwoReportedFaults() {
        SystemShortcut.values().filter { it !in listOf(SystemShortcut.DISPLAY_SOUND, SystemShortcut.ACCESSIBILITY) }.forEach {
            assertNull(it.firmwarePage())
        }
    }
}
