package com.boop.shieldturbo.startup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupPolicyTest {
    @Test fun acceptsOrdinaryPackageNamesAndRejectsShellInjection() {
        assertTrue(StartupPolicy.validPackage("org.xbmc.kodi"))
        assertTrue(StartupPolicy.validPackage("com.example.fork2"))
        assertFalse(StartupPolicy.validPackage("org.xbmc.kodi; reboot"))
        assertFalse(StartupPolicy.validPackage("org.xbmc.kodi && id"))
        assertFalse(StartupPolicy.validPackage("../kodi"))
        assertFalse(StartupPolicy.validPackage("com"))
    }

    @Test fun backgroundBlockUsesOnlyDocumentedBackgroundAppOps() {
        assertEquals(
            listOf(
                "cmd appops set org.xbmc.kodi RUN_IN_BACKGROUND ignore",
                "cmd appops set org.xbmc.kodi RUN_ANY_IN_BACKGROUND ignore"
            ),
            StartupPolicy.backgroundBlock("org.xbmc.kodi")
        )
    }

    @Test fun backgroundRestoreUsesSavedModesRatherThanAssumingAllow() {
        assertEquals(
            listOf(
                "cmd appops set org.xbmc.kodi RUN_IN_BACKGROUND default",
                "cmd appops set org.xbmc.kodi RUN_ANY_IN_BACKGROUND deny"
            ),
            StartupPolicy.backgroundRestore("org.xbmc.kodi", "default", "deny")
        )
    }

    @Test fun parsesAppOpModesFailClosed() {
        assertEquals("allow", StartupPolicy.parseMode("RUN_IN_BACKGROUND: allow"))
        assertEquals("ignore", StartupPolicy.parseMode("RUN_ANY_IN_BACKGROUND: ignore; time=+1h"))
        assertEquals("deny", StartupPolicy.parseMode("RUN_ANY_IN_BACKGROUND: deny"))
        assertEquals("default", StartupPolicy.parseMode("RUN_IN_BACKGROUND: default"))
        assertEquals(null, StartupPolicy.parseMode("RUN_IN_BACKGROUND: foreground"))
        assertEquals(null, StartupPolicy.parseMode("No operations."))
    }

    @Test fun hardBlockAndRestoreAreExplicitlySeparate() {
        assertEquals("pm disable-user --user current org.xbmc.kodi", StartupPolicy.hardBlock("org.xbmc.kodi"))
        assertEquals("pm enable --user current org.xbmc.kodi", StartupPolicy.restoreEnabled("org.xbmc.kodi", "enabled"))
        assertEquals("pm default-state --user current org.xbmc.kodi", StartupPolicy.restoreEnabled("org.xbmc.kodi", "default"))
    }
}
