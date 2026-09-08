package com.boop.shieldturbo.startup

import org.junit.Assert.assertEquals
import org.junit.Test

/** Data-resolution tests only; no Android UI, screenshots or appearance checks. */
class StartupAppLabelsTest {
    private val pkg = "org.example.forki"

    @Test fun packageFallbackDoesNotHideTheInstalledAppName() {
        assertEquals("Forki", StartupAppLabels.resolve(pkg, pkg, "Forki"))
    }

    @Test fun distinctiveLauncherNameIsPreserved() {
        assertEquals("Forki Dev", StartupAppLabels.resolve(pkg, "Forki Dev", "Kodi"))
    }

    @Test fun blankLauncherLabelUsesApplicationLabel() {
        assertEquals("Forked Again", StartupAppLabels.resolve(pkg, "  ", "Forked Again"))
    }

    @Test fun disabledManagedAppCanUseApplicationLabelWithoutLauncherEntry() {
        assertEquals("Forki", StartupAppLabels.resolve(pkg, null, "Forki"))
    }

    @Test fun genuinelyMissingLabelsKeepExactPackageIdentity() {
        assertEquals(pkg, StartupAppLabels.resolve(pkg, pkg, pkg))
    }

    @Test fun unavailableResourcesDoNotInventAnAppName() {
        assertEquals(pkg, StartupAppLabels.resolve(pkg, null, null))
    }

    @Test fun incidentalWhitespaceIsRemoved() {
        assertEquals("Forki", StartupAppLabels.resolve(pkg, "  Forki  ", "Kodi"))
    }
}
