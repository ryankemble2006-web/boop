package com.boop.shieldturbo.performance

import android.content.Context
import android.os.Build
import android.os.PowerManager
import com.boop.shieldturbo.model.ProbeResult
import com.boop.shieldturbo.model.ProbeStatus
import com.boop.shieldturbo.power.LocalBridge

class PerformanceCapabilityProbe(
    private val context: Context,
    private val bridge: LocalBridge = LocalBridge(context.applicationContext)
) {
    fun read(): PerformanceCapabilitySnapshot {
        val thermal = currentThermalStatusOrNull()
        return try {
            bridge.withTrustedAdb { adb ->
                fun optionalRead(command: String): String =
                    runCatching { bridge.checked(adb, command) }.getOrDefault("")

                val global = optionalRead(PerformanceDiscoveryPolicy.clueCommands[0])
                val secure = optionalRead(PerformanceDiscoveryPolicy.clueCommands[1])
                val system = optionalRead(PerformanceDiscoveryPolicy.clueCommands[2])
                val props = optionalRead(PerformanceDiscoveryPolicy.clueCommands[3])
                val powerHelp = optionalRead(PerformanceDiscoveryPolicy.clueCommands[4])
                val pathText = optionalRead(PerformanceDiscoveryPolicy.pathProbeCommand())

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

    fun readResults(): List<ProbeResult> {
        val snapshot = read()
        val clues = snapshot.settingClues + snapshot.propertyClues
        val presentPaths = snapshot.paths.filter { it.present }
        val cpuPaths = presentPaths.filter { it.path.contains("/cpu") }
        val gpuPaths = presentPaths.filter { it.path.contains("gpu", ignoreCase = true) }
        val writablePaths = presentPaths.filter { it.shellWritable }

        val results = mutableListOf<ProbeResult>()
        results += ProbeResult(
            "performance_probe",
            "Performance probe",
            if (snapshot.trustedAdbAvailable) ProbeStatus.AVAILABLE else ProbeStatus.RESTRICTED,
            if (snapshot.trustedAdbAvailable) "Trusted ADB read" else "ADB TURBO not authorised",
            if (snapshot.trustedAdbAvailable) {
                "Read-only capability probe. No processor, GPU, fan, governor or thermal setting was changed."
            } else {
                snapshot.adbDetail.ifBlank { "Trusted local ADB is not available" }
            }
        )

        results += if (!snapshot.trustedAdbAvailable) {
            restricted("processor_mode_clues", "Processor-mode clues", snapshot.adbDetail)
        } else if (clues.isEmpty()) {
            unsupported("processor_mode_clues", "Processor-mode clues", "No matching key exposed", "No matching NVIDIA/processor/performance/fan/power key was exposed by settings or getprop.")
        } else {
            ProbeResult(
                "processor_mode_clues",
                "Processor-mode clues",
                ProbeStatus.AVAILABLE,
                clueSummary(clues),
                clues.take(24).joinToString("\n") { "${it.namespace}:${it.key}=${it.value}" }
            )
        }

        results += pathResult(
            key = "cpu_stock_controls",
            label = "CPU stock controls",
            paths = cpuPaths,
            trustedAdbAvailable = snapshot.trustedAdbAvailable,
            adbDetail = snapshot.adbDetail
        )
        results += pathResult(
            key = "gpu_stock_controls",
            label = "GPU stock controls",
            paths = gpuPaths,
            trustedAdbAvailable = snapshot.trustedAdbAvailable,
            adbDetail = snapshot.adbDetail
        )

        results += if (!snapshot.trustedAdbAvailable) {
            restricted("performance_write_access", "Performance write access", snapshot.adbDetail)
        } else if (presentPaths.isEmpty()) {
            unsupported(
                "performance_write_access",
                "Performance write access",
                "No allowlisted control path exposed",
                "No reviewed CPU/GPU sysfs path was present. No write was attempted."
            )
        } else {
            ProbeResult(
                "performance_write_access",
                "Performance write access",
                ProbeStatus.AVAILABLE,
                "${writablePaths.size} writable of ${presentPaths.size} present allowlisted paths",
                if (writablePaths.isEmpty()) {
                    "No present allowlisted sysfs path is writable by the ADB shell. No write was attempted."
                } else {
                    writablePaths.joinToString("\n") { it.path } + "\nWritability was tested with test -w only; no value was written."
                }
            )
        }

        results += if (snapshot.thermalStatus == null) {
            unsupported(
                "android_thermal_status",
                "Android thermal status",
                thermalLabel(null),
                "PowerManager thermal status is not exposed on this API/device."
            )
        } else {
            ProbeResult(
                "android_thermal_status",
                "Android thermal status",
                ProbeStatus.AVAILABLE,
                thermalLabel(snapshot.thermalStatus),
                "Android PowerManager currentThermalStatus=${snapshot.thermalStatus}. Read only; no listener is active in this build."
            )
        }

        results += if (!snapshot.trustedAdbAvailable) {
            restricted("android_fixed_performance", "Android fixed-performance", snapshot.adbDetail)
        } else if (snapshot.fixedPerformanceCommandExposed) {
            ProbeResult(
                "android_fixed_performance",
                "Android fixed-performance",
                ProbeStatus.AVAILABLE,
                "Command exposed (diagnostic only)",
                "cmd power help advertises set-fixed-performance-mode-enabled. TURBO did not enable it."
            )
        } else {
            unsupported(
                "android_fixed_performance",
                "Android fixed-performance",
                "Not exposed",
                "cmd power help did not advertise set-fixed-performance-mode-enabled. No command was changed."
            )
        }

        return results
    }

    private fun currentThermalStatusOrNull(): Int? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return runCatching {
            context.getSystemService(PowerManager::class.java)?.currentThermalStatus
        }.getOrNull()
    }

    private fun clueSummary(clues: List<SettingClue>): String = clues
        .take(8)
        .joinToString(" • ") { "${it.namespace}:${it.key}=${it.value}" }
        .ifBlank { "No matching key exposed" }

    private fun pathResult(
        key: String,
        label: String,
        paths: List<PathCapability>,
        trustedAdbAvailable: Boolean,
        adbDetail: String
    ): ProbeResult {
        if (!trustedAdbAvailable) return restricted(key, label, adbDetail)
        if (paths.isEmpty()) {
            return unsupported(key, label, "Not exposed", "None of the reviewed allowlisted paths is present. No write was attempted.")
        }
        return ProbeResult(
            key,
            label,
            ProbeStatus.AVAILABLE,
            paths.take(10).joinToString(" • ") {
                "${it.path.substringAfterLast('/')}=${it.value.ifBlank { "<empty>" }}${if (it.shellWritable) " [writable]" else " [read-only]"}"
            },
            paths.joinToString("\n") {
                "${it.path} | ${if (it.shellWritable) "writable" else "read-only"} | ${it.value.ifBlank { "<empty>" }}"
            }
        )
    }

    private fun restricted(key: String, label: String, detail: String) = ProbeResult(
        key,
        label,
        ProbeStatus.RESTRICTED,
        "ADB TURBO not authorised",
        detail.ifBlank { "Trusted local ADB is not available" }
    )

    private fun unsupported(key: String, label: String, value: String, evidence: String) = ProbeResult(
        key,
        label,
        ProbeStatus.UNSUPPORTED,
        value,
        evidence
    )
}
