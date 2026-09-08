package com.boop.shieldturbo.cleanstart

import org.junit.Assert.*
import org.junit.Test

class CleanStartPolicyTest {
    @Test fun forceStopTargetsExactlyOneValidatedPackage() {
        assertEquals("am force-stop --user current 'org.xbmc.kodi'", CleanStartPolicy.forceStopCommand("org.xbmc.kodi"))
        try { CleanStartPolicy.forceStopCommand("org.xbmc.kodi; reboot"); fail("shell injection accepted") }
        catch (_: IllegalArgumentException) { }
        try { CleanStartPolicy.forceStopCommand("com.boop.shieldturbo"); fail("Turbo accepted as a target") }
        catch (_: IllegalArgumentException) { }
    }

    @Test fun processParserFindsMainAndColonSubprocessesOnly() {
        val ps = """
            PID NAME
            101 org.xbmc.kodi
            102 org.xbmc.kodi:service
            103 org.xbmc.kodi.helper
            104 com.android.tv.settings
            bad-line
        """.trimIndent()
        val names = CleanStartPolicy.parseProcessNames(ps)
        assertEquals(listOf("org.xbmc.kodi", "org.xbmc.kodi:service"), CleanStartPolicy.packageProcesses("org.xbmc.kodi", names))
        assertFalse(names.contains("NAME"))
    }

    @Test fun stoppedStateMustBeExplicitlyParsed() {
        assertEquals(true, CleanStartPolicy.parseStopped("User 0: installed=true hidden=false stopped=true enabled=0"))
        assertEquals(false, CleanStartPolicy.parseStopped("User 0: installed=true hidden=false stopped=false enabled=1"))
        assertNull(CleanStartPolicy.parseStopped("Package has no user-state line"))
    }

    @Test fun resumedPackageParserIsConservative() {
        assertEquals("org.xbmc.kodi", CleanStartPolicy.parseResumedPackage("mResumedActivity: ActivityRecord{abc u0 org.xbmc.kodi/.MainActivity t12}"))
        assertEquals("com.google.android.tvlauncher", CleanStartPolicy.parseResumedPackage("topResumedActivity=ActivityRecord{abc u0 com.google.android.tvlauncher/.Main t1}"))
        assertNull(CleanStartPolicy.parseResumedPackage("mResumedActivity: null"))
    }
}
