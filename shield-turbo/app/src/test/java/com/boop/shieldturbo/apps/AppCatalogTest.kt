package com.boop.shieldturbo.apps

import org.junit.Assert.assertEquals
import org.junit.Test

class AppCatalogTest {
    @Test fun appsAreSortedCaseInsensitivelyAndDedupedByPackage() {
        val result = AppCatalog.sortAndDedupe(listOf(
            LaunchableApp("b", "Kodi"),
            LaunchableApp("a", "deezer"),
            LaunchableApp("b", "Kodi duplicate")
        ))
        assertEquals(listOf("a", "b"), result.map { it.packageName })
    }
}
