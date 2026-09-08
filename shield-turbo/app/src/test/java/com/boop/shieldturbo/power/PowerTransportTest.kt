package com.boop.shieldturbo.power

import org.junit.Assert.*
import org.junit.Test

class PowerTransportTest {
    @Test fun actualLoopbackAuthenticationAndShellProtocol() { AdbWireSelfTest.runAll() }
    @Test fun onlySelectedNonSystemAppsCanBeRestarted() {
        assertTrue(PowerPolicy.safeUserPackage("org.xbmc.kodi", false))
        assertFalse(PowerPolicy.safeUserPackage("org.xbmc.kodi", true))
        assertFalse(PowerPolicy.safeUserPackage("com.boop.alpha1", false))
        assertFalse(PowerPolicy.safeUserPackage("com.android.tv.settings", false))
        assertFalse(PowerPolicy.safeUserPackage("com.nvidia.shield", false))
        assertFalse(PowerPolicy.safeUserPackage("com.google.android.gms", false))
    }
    @Test fun shellMetacharactersAndMismatchedTargetsAreRejected() {
        assertFalse(PowerPolicy.validComponent("org.app/.Main;reboot"))
        assertFalse(PowerPolicy.safeUserPackage("org.app;reboot", false))
        assertFalse(PowerPolicy.validComponent("org.app/.Main\nreboot"))
        try { PowerPolicy.restart("org.app", "other.app/.Main", false); fail("Wrong app accepted") }
        catch (_: IllegalArgumentException) { }
    }
    @Test fun restartCommandDoesNotClearOrDisableTheApp() {
        val command = PowerPolicy.restart("org.xbmc.kodi", "org.xbmc.kodi/.Main", false)
        assertTrue(command.startsWith("am force-stop --user current 'org.xbmc.kodi' && am start -W"))
        assertFalse(command.contains("pm clear"))
        assertFalse(command.contains("disable"))
    }
    @Test fun genericSettingsCannotMasqueradeAsTheCombinedPage() {
        assertEquals(0, PowerPolicy.pageScore("display", "com.android.tv.settings.MainSettings", "Display and Sound"))
        assertEquals(0, PowerPolicy.pageScore("display", "com.android.tv.settings.SoundActivity", "Sound"))
        assertTrue(PowerPolicy.pageScore("display", "com.android.tv.settings.NvDisplaySoundActivity", "") > 0)
    }
    @Test fun accessibilityServicePickerIsNotTheMainAccessibilityPage() {
        assertEquals(0, PowerPolicy.pageScore("accessibility", "com.tv.AccessibilityServiceActivity", ""))
        assertEquals(100, PowerPolicy.pageScore("accessibility", "com.tv.AccessibilityActivity", ""))
    }
}
