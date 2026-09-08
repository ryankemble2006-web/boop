# SHIELD TURBO Performance Capability Probe Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build SHIELD TURBO v0.5.9 as a read-only real-device capability probe that discovers the Shield firmware's NVIDIA performance clues, stock CPU/GPU controls, shell writability, Android thermal status, and fixed-performance command support without changing any performance setting.

**Architecture:** Add a focused `performance` package containing pure parsing/model code and one Android/ADB-backed read-only probe. Reuse the existing worker thread in `MainActivity` and the existing trusted loopback ADB transport. Stage 1 deliberately adds no performance writer, boot receiver, watchdog service, foreground-service permission, governor write, processor-mode write, fan write, or fixed-performance command.

**Tech Stack:** Kotlin/JVM 17, Android API 28-36, Android `PowerManager`, existing `LocalBridge`/`AdbWire`, JUnit 4.13.2, Python `unittest` source contracts, existing GitHub Actions signing/package/archive checks.

**Spec:** `docs/superpowers/specs/2026-09-08-shield-turbo-stock-performance-mode-design.md`

## Global Constraints

- Branch: `shield-turbo-v01`; package: `com.boop.shieldturbo`.
- Starting live branch head for this plan: `7ccd9443406e83084f2730a6838067a85985d91b`.
- Permanent signer must remain `CN=BOOP Development,O=BOOP`, certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Stage 1 is **read-only**. It must not issue `settings put`, sysfs writes, `cmd power set-fixed-performance-mode-enabled true`, governor changes, frequency changes, fan changes, processor-mode changes, or any other performance write.
- Use `LocalBridge.withTrustedAdb` only for capability shell reads. Stage 1 must never trigger a fresh RSA approval.
- No root/su, bootloader/kernel/boot-image operation, voltage modification, thermal/throttle disable, frequency above firmware limits, or recursive `/sys` crawler.
- Do not add a thermal watchdog service or performance boot receiver in this stage.
- Do not change CLEAN START files, brightness behavior, APPS behavior, animation undo, or package/signing identity.
- Android fixed-performance mode is diagnostic-only in v0.5.9.
- Machine tests must not claim visual quality, Dolphin performance gain, thermal effectiveness, or physical safety.
- Physical Shield evidence is required before writing Plan 2 for any performance control.

## File Structure

- Create `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/PerformanceCapability.kt`: immutable capability data types and thermal labels.
- Create `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/PerformanceDiscoveryPolicy.kt`: reviewed path allowlist, exact read-only shell commands, settings/property clue parser, path-probe parser.
- Create `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/PerformanceCapabilityProbe.kt`: Android thermal read + trusted-ADB read-only discovery + conversion to existing `ProbeResult` cards.
- Create `shield-turbo/app/src/test/java/com/boop/shieldturbo/performance/PerformanceDiscoveryPolicyTest.kt`: pure parser/allowlist tests.
- Create `shield-turbo/app/src/test/java/com/boop/shieldturbo/performance/PerformanceCapabilityTest.kt`: thermal label tests.
- Create `shield-turbo/tests/test_performance_capability_contract.py`: source-level Stage-1 no-write/no-resident guard.
- Modify `shield-turbo/app/src/main/java/com/boop/shieldturbo/MainActivity.kt`: append capability results inside the existing background `analyse()` flow.
- Modify `shield-turbo/app/src/main/res/values/strings.xml`: read-only Stage-1 note.
- Modify `shield-turbo/app/build.gradle` and `.github/workflows/shield-turbo.yml` only after the feature tree is GREEN, changing code 15 / `0.5.8` to code 16 / `0.5.9`.

---

### Task 1: Pure capability model and discovery policy

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/PerformanceCapability.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/PerformanceDiscoveryPolicy.kt`
- Create: `shield-turbo/app/src/test/java/com/boop/shieldturbo/performance/PerformanceDiscoveryPolicyTest.kt`
- Create: `shield-turbo/app/src/test/java/com/boop/shieldturbo/performance/PerformanceCapabilityTest.kt`

**Interfaces:**
- Produces:
  - `data class SettingClue(val namespace: String, val key: String, val value: String)`
  - `data class PathCapability(val path: String, val present: Boolean, val shellWritable: Boolean, val value: String)`
  - `data class PerformanceCapabilitySnapshot(val settingClues: List<SettingClue>, val propertyClues: List<SettingClue>, val paths: List<PathCapability>, val thermalStatus: Int?, val fixedPerformanceCommandExposed: Boolean, val trustedAdbAvailable: Boolean, val adbDetail: String)`
  - `object PerformanceDiscoveryPolicy`
  - `fun settingClues(namespace: String, text: String): List<SettingClue>`
  - `fun pathProbeCommand(): String`
  - `fun parsePathProbe(text: String): List<PathCapability>`
  - `fun thermalLabel(status: Int?): String`
- Consumes: no Android runtime.

- [ ] **Step 1: Write failing tests**

Create `PerformanceDiscoveryPolicyTest.kt`:

```kotlin
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
}
```

Create `PerformanceCapabilityTest.kt`:

```kotlin
package com.boop.shieldturbo.performance

import org.junit.Assert.assertEquals
import org.junit.Test

class PerformanceCapabilityTest {
    @Test fun thermalLabelsAreStable() {
        assertEquals("Not exposed", thermalLabel(null))
        assertEquals("None", thermalLabel(0))
        assertEquals("Light", thermalLabel(1))
        assertEquals("Moderate", thermalLabel(2))
        assertEquals("Severe", thermalLabel(3))
        assertEquals("Critical", thermalLabel(4))
        assertEquals("Emergency", thermalLabel(5))
        assertEquals("Shutdown", thermalLabel(6))
    }
}
```

- [ ] **Step 2: Run tests and verify RED**

```bash
gradle --no-daemon -p shield-turbo :app:testDebugUnitTest --tests 'com.boop.shieldturbo.performance.*' --console=plain --stacktrace
```

Expected: compile failure because the `performance` types do not exist.

- [ ] **Step 3: Implement `PerformanceCapability.kt`**

```kotlin
package com.boop.shieldturbo.performance

data class SettingClue(val namespace: String, val key: String, val value: String)

data class PathCapability(
    val path: String,
    val present: Boolean,
    val shellWritable: Boolean,
    val value: String
)

data class PerformanceCapabilitySnapshot(
    val settingClues: List<SettingClue>,
    val propertyClues: List<SettingClue>,
    val paths: List<PathCapability>,
    val thermalStatus: Int?,
    val fixedPerformanceCommandExposed: Boolean,
    val trustedAdbAvailable: Boolean,
    val adbDetail: String
)

fun thermalLabel(status: Int?): String = when (status) {
    null -> "Not exposed"
    0 -> "None"
    1 -> "Light"
    2 -> "Moderate"
    3 -> "Severe"
    4 -> "Critical"
    5 -> "Emergency"
    6 -> "Shutdown"
    else -> "Unknown ($status)"
}
```

- [ ] **Step 4: Implement `PerformanceDiscoveryPolicy.kt` with the literal allowlist**

```kotlin
package com.boop.shieldturbo.performance

object PerformanceDiscoveryPolicy {
    val clueCommands = listOf(
        "settings list global | grep -Ei 'nvidia|processor|performance|fan|power' | head -n 120",
        "settings list secure | grep -Ei 'nvidia|processor|performance|fan|power' | head -n 120",
        "settings list system | grep -Ei 'nvidia|processor|performance|fan|power' | head -n 120",
        "getprop | grep -Ei 'nvidia|processor|performance|fan|power' | head -n 120",
        "cmd power help"
    )

    val paths = listOf(
        "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq",
        "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq",
        "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
        "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors",
        "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq",
        "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq",
        "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq",
        "/sys/devices/system/cpu/cpufreq/policy0/scaling_cur_freq",
        "/sys/devices/system/cpu/cpufreq/policy0/scaling_governor",
        "/sys/devices/system/cpu/cpufreq/policy0/scaling_available_governors",
        "/sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq",
        "/sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq",
        "/sys/devices/system/cpu/cpufreq/policy0/cpuinfo_max_freq",
        "/sys/class/devfreq/57000000.gpu/cur_freq",
        "/sys/class/devfreq/57000000.gpu/min_freq",
        "/sys/class/devfreq/57000000.gpu/max_freq",
        "/sys/class/devfreq/57000000.gpu/governor",
        "/sys/class/devfreq/57000000.gpu/available_governors",
        "/sys/class/devfreq/57000000.gpu/available_frequencies",
        "/sys/devices/57000000.gpu/devfreq/57000000.gpu/cur_freq",
        "/sys/devices/57000000.gpu/devfreq/57000000.gpu/min_freq",
        "/sys/devices/57000000.gpu/devfreq/57000000.gpu/max_freq",
        "/sys/devices/57000000.gpu/devfreq/57000000.gpu/governor",
        "/sys/devices/57000000.gpu/devfreq/57000000.gpu/available_governors",
        "/sys/devices/57000000.gpu/devfreq/57000000.gpu/available_frequencies"
    )

    private val clueTokens = listOf("nvidia", "processor", "performance", "fan", "power")

    fun settingClues(namespace: String, text: String): List<SettingClue> =
        text.lineSequence().mapNotNull { line ->
            val parts = line.trim().split('=', limit = 2)
            if (parts.size != 2) return@mapNotNull null
            val key = parts[0].trim()
            if (clueTokens.none { key.contains(it, ignoreCase = true) }) return@mapNotNull null
            SettingClue(namespace, key, parts[1].trim().take(512))
        }.take(120).toList()

    fun pathProbeCommand(): String {
        val quoted = paths.joinToString(" ") { "'$it'" }
        return "for p in $quoted; do " +
            "if [ -e \"\$p\" ]; then " +
            "w=0; [ -w \"\$p\" ] && w=1; " +
            "v=\$(cat \"\$p\" 2>/dev/null | head -c 1024 | tr '\\n' ' '); " +
            "printf '%s\\t1\\t%s\\t%s\\n' \"\$p\" \"\$w\" \"\$v\"; " +
            "else printf '%s\\t0\\t0\\t\\n' \"\$p\"; fi; done"
    }

    fun parsePathProbe(text: String): List<PathCapability> = text.lineSequence().mapNotNull { line ->
        if (line.isBlank()) return@mapNotNull null
        val parts = line.split('\t', limit = 4)
        if (parts.size < 3) return@mapNotNull null
        PathCapability(
            path = parts[0],
            present = parts[1] == "1",
            shellWritable = parts[2] == "1",
            value = parts.getOrElse(3) { "" }.trim().take(1024)
        )
    }.toList()
}
```

- [ ] **Step 5: Run focused and full JVM tests**

```bash
gradle --no-daemon -p shield-turbo :app:testDebugUnitTest --tests 'com.boop.shieldturbo.performance.*' --console=plain --stacktrace
gradle --no-daemon -p shield-turbo :app:testDebugUnitTest --console=plain --stacktrace
```

Expected: GREEN.

- [ ] **Step 6: Commit Task 1**

```bash
git add shield-turbo/app/src/main/java/com/boop/shieldturbo/performance \
        shield-turbo/app/src/test/java/com/boop/shieldturbo/performance
git commit -m "feat(shield-turbo): add read-only performance discovery policy"
```

---

### Task 2: Android + trusted-ADB capability probe

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/PerformanceCapabilityProbe.kt`
- Test: `shield-turbo/app/src/test/java/com/boop/shieldturbo/performance/PerformanceDiscoveryPolicyTest.kt`

**Interfaces:**
- Consumes: `PerformanceDiscoveryPolicy.clueCommands`, `paths`, `pathProbeCommand()`, `settingClues()`, `parsePathProbe()`, existing `LocalBridge.withTrustedAdb` and `LocalBridge.checked`.
- Produces:
  - `class PerformanceCapabilityProbe(private val context: Context, private val bridge: LocalBridge = LocalBridge(context))`
  - `fun read(): PerformanceCapabilitySnapshot`
  - `fun readResults(): List<ProbeResult>`

- [ ] **Step 1: Add failing exact-command test**

Append to `PerformanceDiscoveryPolicyTest.kt`:

```kotlin
@Test fun clueCommandsAreReadOnlyAndStable() {
    assertEquals(listOf(
        "settings list global | grep -Ei 'nvidia|processor|performance|fan|power' | head -n 120",
        "settings list secure | grep -Ei 'nvidia|processor|performance|fan|power' | head -n 120",
        "settings list system | grep -Ei 'nvidia|processor|performance|fan|power' | head -n 120",
        "getprop | grep -Ei 'nvidia|processor|performance|fan|power' | head -n 120",
        "cmd power help"
    ), PerformanceDiscoveryPolicy.clueCommands)
}
```

Run the focused suite. Expected: GREEN if Task 1 already supplied exactly this list; if not, fix Task 1 before proceeding. This step is a reviewer gate against accidental command drift.

- [ ] **Step 2: Implement the Android thermal read and trusted-ADB snapshot**

Create `PerformanceCapabilityProbe.kt`:

```kotlin
package com.boop.shieldturbo.performance

import android.content.Context
import android.os.Build
import android.os.PowerManager
import com.boop.shieldturbo.model.ProbeResult
import com.boop.shieldturbo.model.ProbeStatus
import com.boop.shieldturbo.power.LocalBridge

class PerformanceCapabilityProbe(
    private val context: Context,
    private val bridge: LocalBridge = LocalBridge(context)
) {
    private fun currentThermalStatusOrNull(): Int? {
        if (Build.VERSION.SDK_INT < 29) return null
        return runCatching {
            val power = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            power?.currentThermalStatus
        }.getOrNull()
    }

    fun read(): PerformanceCapabilitySnapshot {
        val thermal = currentThermalStatusOrNull()
        return try {
            bridge.withTrustedAdb { adb ->
                val global = bridge.checked(adb, PerformanceDiscoveryPolicy.clueCommands[0])
                val secure = bridge.checked(adb, PerformanceDiscoveryPolicy.clueCommands[1])
                val system = bridge.checked(adb, PerformanceDiscoveryPolicy.clueCommands[2])
                val props = bridge.checked(adb, PerformanceDiscoveryPolicy.clueCommands[3])
                val powerHelp = bridge.checked(adb, PerformanceDiscoveryPolicy.clueCommands[4])
                val pathText = bridge.checked(adb, PerformanceDiscoveryPolicy.pathProbeCommand())
                PerformanceCapabilitySnapshot(
                    settingClues = PerformanceDiscoveryPolicy.settingClues("global", global) +
                        PerformanceDiscoveryPolicy.settingClues("secure", secure) +
                        PerformanceDiscoveryPolicy.settingClues("system", system),
                    propertyClues = PerformanceDiscoveryPolicy.settingClues("property", props),
                    paths = PerformanceDiscoveryPolicy.parsePathProbe(pathText),
                    thermalStatus = thermal,
                    fixedPerformanceCommandExposed = powerHelp.contains("set-fixed-performance-mode-enabled"),
                    trustedAdbAvailable = true,
                    adbDetail = "Trusted local ADB read completed"
                )
            }
        } catch (error: Exception) {
            PerformanceCapabilitySnapshot(
                settingClues = emptyList(),
                propertyClues = emptyList(),
                paths = emptyList(),
                thermalStatus = thermal,
                fixedPerformanceCommandExposed = false,
                trustedAdbAvailable = false,
                adbDetail = error.message?.take(240) ?: error.javaClass.simpleName
            )
        }
    }
```

- [ ] **Step 3: Implement concrete card formatting in the same class**

Append:

```kotlin
    fun readResults(): List<ProbeResult> {
        val snapshot = read()
        val clues = (snapshot.settingClues + snapshot.propertyClues)
        val present = snapshot.paths.filter { it.present }
        val cpu = present.filter { it.path.contains("/cpu") || it.path.contains("cpufreq") }
        val gpu = present.filter { it.path.contains("57000000.gpu") }

        fun summary(items: List<PathCapability>): String = items.joinToString(" • ") {
            "${it.path.substringAfterLast('/')}: ${it.value.ifBlank { "(empty)" }}"
        }.ifBlank { "Not exposed" }.take(900)

        fun statusFor(items: List<PathCapability>) = when {
            !snapshot.trustedAdbAvailable -> ProbeStatus.RESTRICTED
            items.isEmpty() -> ProbeStatus.UNSUPPORTED
            else -> ProbeStatus.AVAILABLE
        }

        val clueText = clues.joinToString(" • ") {
            "${it.namespace}:${it.key}=${it.value}"
        }.ifBlank { "No matching key exposed" }.take(900)

        return listOf(
            ProbeResult(
                "performance_probe", "Performance probe",
                if (snapshot.trustedAdbAvailable) ProbeStatus.AVAILABLE else ProbeStatus.RESTRICTED,
                if (snapshot.trustedAdbAvailable) "Trusted ADB read" else "ADB TURBO not authorised",
                snapshot.adbDetail
            ),
            ProbeResult(
                "processor_mode_clues", "Processor-mode clues",
                if (snapshot.trustedAdbAvailable && clues.isNotEmpty()) ProbeStatus.AVAILABLE
                else if (!snapshot.trustedAdbAvailable) ProbeStatus.RESTRICTED else ProbeStatus.UNSUPPORTED,
                clueText,
                "Read-only settings/property clues; no processor mode was changed"
            ),
            ProbeResult("cpu_stock_controls", "CPU stock controls", statusFor(cpu), summary(cpu),
                "Reviewed CPU/cpufreq paths only"),
            ProbeResult("gpu_stock_controls", "GPU stock controls", statusFor(gpu), summary(gpu),
                "Reviewed Tegra 57000000.gpu devfreq paths only"),
            ProbeResult(
                "performance_write_access", "Performance write access",
                if (!snapshot.trustedAdbAvailable) ProbeStatus.RESTRICTED
                else if (present.isEmpty()) ProbeStatus.UNSUPPORTED else ProbeStatus.AVAILABLE,
                "${present.count { it.shellWritable }} writable of ${present.size} present allowlisted paths",
                "Writability was tested with test -w only; nothing was written"
            ),
            ProbeResult(
                "android_thermal", "Android thermal status",
                if (snapshot.thermalStatus == null) ProbeStatus.UNSUPPORTED else ProbeStatus.AVAILABLE,
                thermalLabel(snapshot.thermalStatus),
                "PowerManager current thermal status; no thermal listener is running in this build"
            ),
            ProbeResult(
                "fixed_performance", "Android fixed-performance",
                if (!snapshot.trustedAdbAvailable) ProbeStatus.RESTRICTED
                else if (snapshot.fixedPerformanceCommandExposed) ProbeStatus.AVAILABLE else ProbeStatus.UNSUPPORTED,
                if (snapshot.fixedPerformanceCommandExposed) "Command exposed (diagnostic only)" else "Not exposed",
                "TURBO does not enable Android fixed-performance mode in v0.5.9"
            )
        )
    }
}
```

- [ ] **Step 4: Run full JVM tests**

```bash
gradle --no-daemon -p shield-turbo :app:testDebugUnitTest --console=plain --stacktrace
```

Expected: GREEN.

- [ ] **Step 5: Commit Task 2**

```bash
git add shield-turbo/app/src/main/java/com/boop/shieldturbo/performance \
        shield-turbo/app/src/test/java/com/boop/shieldturbo/performance
git commit -m "feat(shield-turbo): probe stock performance capabilities"
```

---

### Task 3: Integrate capability results into the existing TURBO scan and add no-write contracts

**Files:**
- Modify: `shield-turbo/app/src/main/java/com/boop/shieldturbo/MainActivity.kt`
- Modify: `shield-turbo/app/src/main/res/values/strings.xml`
- Create: `shield-turbo/tests/test_performance_capability_contract.py`

**Interfaces:**
- Consumes: `PerformanceCapabilityProbe(context).readResults(): List<ProbeResult>`
- Produces: seven read-only performance capability cards appended to the existing `ANALYSE SHIELD` results.

- [ ] **Step 1: Write the failing source contract first**

Create `shield-turbo/tests/test_performance_capability_contract.py`:

```python
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'app/src/main/java/com/boop/shieldturbo'
ANDROID = '{http://schemas.android.com/apk/res/android}'

class PerformanceCapabilityContractTest(unittest.TestCase):
    def test_stage_one_is_read_only_and_uses_trusted_adb(self):
        perf = '\n'.join(p.read_text() for p in (SOURCE / 'performance').glob('*.kt'))
        self.assertIn('withTrustedAdb', perf)
        self.assertNotIn('withAdb(', perf)
        self.assertNotIn('settings put', perf)
        self.assertNotIn('set-fixed-performance-mode-enabled true', perf)
        self.assertNotIn(' su ', f' {perf} ')
        self.assertNotRegex(perf, r'echo\s+[^\n]+>')

    def test_stage_one_adds_no_resident_performance_component(self):
        manifest = ET.parse(ROOT / 'app/src/main/AndroidManifest.xml').getroot()
        app = manifest.find('application')
        services = {s.get(ANDROID + 'name') for s in app.findall('service')}
        receivers = {r.get(ANDROID + 'name') for r in app.findall('receiver')}
        permissions = {p.get(ANDROID + 'name') for p in manifest.findall('uses-permission')}
        self.assertNotIn('.performance.ThermalWatchdogService', services)
        self.assertNotIn('.performance.PerformanceBootReceiver', receivers)
        self.assertNotIn('android.permission.FOREGROUND_SERVICE', permissions)

    def test_turbo_scan_surfaces_performance_capability_results(self):
        main = (SOURCE / 'MainActivity.kt').read_text()
        self.assertIn('PerformanceCapabilityProbe(context).readResults()', main)
        self.assertIn('snapshot.results + performance + privilege', main)

if __name__ == '__main__':
    unittest.main(verbosity=2)
```

- [ ] **Step 2: Run source contracts and verify RED**

```bash
python3 -m unittest discover -s shield-turbo/tests -p 'test_*.py' -v
```

Expected: the new integration test fails because `MainActivity` does not call `PerformanceCapabilityProbe` yet.

- [ ] **Step 3: Wire the probe into the existing worker-thread scan**

Add import:

```kotlin
import com.boop.shieldturbo.performance.PerformanceCapabilityProbe
```

Inside the existing background block in `analyse()`, immediately after `ShieldAnalyzer(...).analyze()`:

```kotlin
val performance = PerformanceCapabilityProbe(context).readResults()
```

Change:

```kotlin
render(snapshot.results + privilege)
```

to:

```kotlin
render(snapshot.results + performance + privilege)
```

Do not add another executor, timer, service, receiver, or continuous scan.

- [ ] **Step 4: Add read-only explanatory copy**

Add to `strings.xml`:

```xml
<string name="performance_probe_note">Performance capability is read-only in this build. TURBO has not changed processor, GPU, fan, governor or thermal settings.</string>
```

In `render(readings)`, append this note immediately before the existing `no_changes` text only when one of the results has key `performance_probe`:

```kotlin
if (readings.any { it.key == "performance_probe" }) {
    results.addView(text(getString(R.string.performance_probe_note), 14f, Color.LTGRAY))
}
results.addView(text(getString(R.string.no_changes), 14f, Color.LTGRAY))
```

- [ ] **Step 5: Run contracts, JVM tests and lint**

```bash
python3 -m unittest discover -s shield-turbo/tests -p 'test_*.py' -v
gradle --no-daemon -p shield-turbo :app:testDebugUnitTest --console=plain --stacktrace
gradle --no-daemon -p shield-turbo :app:lintDebug --console=plain --stacktrace
```

Expected: all tests pass; lint reports 0 errors.

- [ ] **Step 6: Verify frozen subsystems were untouched**

```bash
git diff --name-only 7ccd9443406e83084f2730a6838067a85985d91b..HEAD -- \
  shield-turbo/app/src/main/java/com/boop/shieldturbo/cleanstart \
  shield-turbo/app/src/main/java/com/boop/shieldturbo/BrightnessService.kt
```

Expected: no output.

- [ ] **Step 7: Commit Task 3**

```bash
git add shield-turbo/app/src/main/java/com/boop/shieldturbo/MainActivity.kt \
        shield-turbo/app/src/main/res/values/strings.xml \
        shield-turbo/tests/test_performance_capability_contract.py
git commit -m "feat(shield-turbo): show performance capability evidence"
```

---

### Task 4: Green gate, atomic v0.5.9 release stamp, and physical-evidence handoff

**Files:**
- Modify after all feature tests are GREEN: `shield-turbo/app/build.gradle`, `.github/workflows/shield-turbo.yml`
- Modify after signed candidate verification: `shield-turbo/SESSION_HANDOFF.md`, `shield-turbo/STATUS.md`, `shield-turbo/MEMORY.md`

**Interfaces:**
- Produces: signed `com.boop.shieldturbo` v0.5.9 / versionCode 16 physical capability candidate.
- Physical output required for Plan 2: exact text from `Processor-mode clues`, `CPU stock controls`, `GPU stock controls`, `Performance write access`, `Android thermal status`, and `Android fixed-performance`.

- [ ] **Step 1: Run complete pre-stamp verification**

```bash
gradle --no-daemon -p shield-turbo :app:testDebugUnitTest --console=plain --stacktrace
python3 -m unittest discover -s shield-turbo/tests -p 'test_*.py' -v
gradle --no-daemon -p shield-turbo :app:lintDebug --console=plain --stacktrace
```

Expected: all tests pass and lint reports 0 errors.

- [ ] **Step 2: Review the feature diff for prohibited writes and frozen-file changes**

```bash
if git diff 7ccd9443406e83084f2730a6838067a85985d91b..HEAD -- shield-turbo/app/src/main | \
  grep -Eiq 'settings put|set-fixed-performance-mode-enabled true|(^|[^a-z])su([^a-z]|$)|echo .*>|scaling_(min|max)_freq.*>|governor.*>'; then
  echo 'Prohibited Stage-1 write pattern found'
  exit 1
fi

git diff --name-only 7ccd9443406e83084f2730a6838067a85985d91b..HEAD -- \
  shield-turbo/app/src/main/java/com/boop/shieldturbo/cleanstart \
  shield-turbo/app/src/main/java/com/boop/shieldturbo/BrightnessService.kt
```

Expected: first command exits 0 with no prohibited match; second command prints nothing.

- [ ] **Step 3: Stamp versionCode 16 / versionName 0.5.9 atomically**

In `shield-turbo/app/build.gradle`:

```groovy
versionCode 16
versionName '0.5.9'
```

In `.github/workflows/shield-turbo.yml` change only the expected badging assertions:

```bash
grep -q "versionCode='16'" shield-turbo-badging.txt
grep -q "versionName='0.5.9'" shield-turbo-badging.txt
```

Commit only those two files:

```bash
git add shield-turbo/app/build.gradle .github/workflows/shield-turbo.yml
git commit -m "release(shield-turbo): stamp performance probe v0.5.9"
```

- [ ] **Step 4: Push and verify the GitHub Actions release run**

Require fresh evidence for:

- complete JVM suite;
- complete Python source contracts;
- lint 0 errors;
- package `com.boop.shieldturbo`;
- versionCode 16 / versionName 0.5.9;
- signer certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- APK ZIP integrity;
- nonvisual install/cold/warm launch/no-package-fatal smoke.

Do not infer physical performance capability from CI or emulator output.

- [ ] **Step 5: Download and independently verify the signed artifact**

Download the `SHIELD-TURBO` artifact, verify its GitHub artifact digest, extract exactly one APK, verify the APK SHA-256 against `shield-turbo-sha256.txt`, verify package/version with `aapt`, and verify the permanent signer with `apksigner`. Deliver it as `Shield-Turbo-v0.5.9.apk`.

- [ ] **Step 6: Update handoff/status/memory as physical-pending**

Record exact built source, workflow run/job/artifact IDs and hashes, test totals, lint result, signer/package/version receipt, and these explicit limits: v0.5.9 is read-only capability discovery; no performance write/watchdog/persistent TURBO exists yet; CLEAN START and brightness files stayed unchanged.

Commit docs only with `[skip ci]`, then fetch live Turbo and `main` heads again.

- [ ] **Step 7: Physical Shield checkpoint**

Install v0.5.9 over v0.5.8, open **TURBO**, run **ANALYSE SHIELD**, and report exact text from:

1. `Processor-mode clues`
2. `CPU stock controls`
3. `GPU stock controls`
4. `Performance write access`
5. `Android thermal status`
6. `Android fixed-performance`

Also report whether the scan remained responsive and whether Android displayed any unexpected permission/RSA prompt. Expected behavior: no performance settings change and no fresh ADB approval prompt.

**Stop after this physical checkpoint.** Plan 2 must be based on the real Shield output and must name an exact stock interface that can be read, changed, read back, restored, and re-verified without root.
