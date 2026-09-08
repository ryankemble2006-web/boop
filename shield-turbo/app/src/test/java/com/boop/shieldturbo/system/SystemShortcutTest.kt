package com.boop.shieldturbo.system

import org.junit.Assert.assertFalse
import org.junit.Test

class SystemShortcutTest {
    @Test fun everyShortcutHasAtLeastOnePublicRouteCandidate() {
        SystemShortcut.values().forEach { shortcut ->
            assertFalse(shortcut.actions().isEmpty())
        }
    }
}
