package com.boop.shieldturbo.performance

import org.junit.Assert.*
import org.junit.Test

class PerformanceDiscoveryPolicyTest {
    @Test fun findsOnlyPerformanceRelatedSettingClues() {
        val text = """
            animator_duration_scale=0.5
            nvidia_processor_mode=optimized
            fan_mode=quiet
            unrelated_key=hello
            performance_hint=1
        """.trimIndent()
        val clues = PerformanceDiscoveryPolicy.settingClues("global", text)
        assertEquals(listOf("nvidia_processor_mode", "fan_mode", "performance_hint"), clues.map { it.key })
        assertTrue(clues.all { it.namespace == "global" })
    }

    @Test fun pathProbeParserPreservesPresenceWritabilityAndValue() {
        val parsed = PerformanceDiscoveryPolicy.parsePathProbe(
            "/sys/a\t1\t0\tschedutil\n/sys/b\t1\t1\t2035200000\n/sys/c\t0\t0\t\n"
        )
        assertEquals(PathCapability("/sys/a", true, false, "schedutil"), parsed[0])
        assertEquals(PathCapability("/sys/b", true, true, "2035200000"), parsed[1])
        assertEquals(PathCapability("/sys/c", false, false, ""), parsed[2])
    }

    @Test fun commandIsReadOnlyAndUsesOnlyReviewedPaths() {
        val command = PerformanceDiscoveryPolicy.pathProbeCommand()
        assertTrue(command.contains("test -w"))
        assertTrue(command.contains("cat \"\$p\""))
        assertFalse(command.contains("settings put"))
        assertFalse(command.contains("set-fixed-performance-mode-enabled true"))
        assertFalse(Regex("echo\\s+.+>").containsMatchIn(command))
        PerformanceDiscoveryPolicy.paths.forEach { assertTrue(command.contains(it)) }
    }

    @Test fun clueCommandsAreReadOnlyAndTreatNoMatchAsEmptyEvidence() {
        val expected = listOf(
            "(settings list global | grep -Ei 'nvidia|processor|performance|fan|power' | head -n 120) || true",
            "(settings list secure | grep -Ei 'nvidia|processor|performance|fan|power' | head -n 120) || true",
            "(settings list system | grep -Ei 'nvidia|processor|performance|fan|power' | head -n 120) || true",
            "(getprop | grep -Ei 'nvidia|processor|performance|fan|power' | head -n 120) || true",
            "cmd power help"
        )
        assertEquals(expected, PerformanceDiscoveryPolicy.clueCommands)
        expected.dropLast(1).forEach { assertTrue(it.endsWith("|| true")) }
        expected.forEach {
            assertFalse(it.contains("settings put"))
            assertFalse(it.contains("set-fixed-performance-mode-enabled true"))
        }
    }
}
