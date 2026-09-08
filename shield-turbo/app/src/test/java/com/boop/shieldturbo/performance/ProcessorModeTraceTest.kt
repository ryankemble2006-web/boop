package com.boop.shieldturbo.performance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessorModeTraceTest {
    @Test fun capturesFourSourcesAndDiffsOnlyChangedValues() {
        val before = ProcessorModeTrace.snapshotFromText(
            global = "stay=1\nnvidia_mode=optimized\n",
            secure = "secure_stay=yes\n",
            system = "system_stay=yes\n",
            properties = "[persist.vendor.power.mode]: [0]\n[ro.product.name]: [foster]\n"
        )
        val after = ProcessorModeTrace.snapshotFromText(
            global = "stay=1\nnvidia_mode=max\n",
            secure = "secure_stay=yes\nnew_secure_flag=on\n",
            system = "system_stay=yes\n",
            properties = "[persist.vendor.power.mode]: [1]\n[ro.product.name]: [foster]\n"
        )

        val changes = ProcessorModeTrace.diff(before, after)

        assertEquals(3, changes.size)
        assertTrue(changes.contains(ProcessorModeTraceChange("global", "nvidia_mode", "optimized", "max")))
        assertTrue(changes.contains(ProcessorModeTraceChange("property", "persist.vendor.power.mode", "0", "1")))
        assertTrue(changes.contains(ProcessorModeTraceChange("secure", "new_secure_flag", null, "on")))
        assertFalse(changes.any { it.key == "stay" || it.key == "ro.product.name" })
    }

    @Test fun reportsRemovedEntriesAndKeepsOutputDeterministic() {
        val before = ProcessorModeTrace.snapshotFromText(
            global = "z_key=gone\na_key=before\n",
            secure = "",
            system = "",
            properties = ""
        )
        val after = ProcessorModeTrace.snapshotFromText(
            global = "a_key=after\n",
            secure = "",
            system = "",
            properties = ""
        )

        assertEquals(
            listOf(
                ProcessorModeTraceChange("global", "a_key", "before", "after"),
                ProcessorModeTraceChange("global", "z_key", "gone", null)
            ),
            ProcessorModeTrace.diff(before, after)
        )
    }

    @Test fun captureCommandsAreBoundedReadsOnly() {
        val commands = ProcessorModeTracePolicy.captureCommands

        assertEquals(
            listOf(
                "settings list global",
                "settings list secure",
                "settings list system",
                "getprop"
            ),
            commands
        )
        val joined = commands.joinToString("\n").lowercase()
        listOf("settings put", "settings delete", "setprop", "cmd power set", "echo >", "tee ").forEach { forbidden ->
            assertFalse("read-only trace must not contain $forbidden", joined.contains(forbidden))
        }
    }
}
