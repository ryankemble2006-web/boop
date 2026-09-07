package com.boop.shieldturbo.probe

import com.boop.shieldturbo.model.ProbeResult
import com.boop.shieldturbo.model.ProbeStatus
import java.io.ByteArrayOutputStream
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Paths
import java.util.Locale

sealed class SourceRead {
    data class Value(val path: String, val text: String) : SourceRead()
    data class Restricted(val path: String) : SourceRead()
    data class Failure(val path: String, val reason: String) : SourceRead()
    object Missing : SourceRead()
}

/** Read a bounded amount from known kernel attributes, never write to them. */
open class FileSourceReader {
    open fun readFirst(paths: List<String>): SourceRead {
        var denied: SourceRead.Restricted? = null
        var failure: SourceRead.Failure? = null
        for (path in paths) {
            if (Thread.currentThread().isInterrupted) throw InterruptedException()
            try {
                val text = Files.newInputStream(Paths.get(path)).use { input ->
                    val bytes = ByteArrayOutputStream()
                    val buffer = ByteArray(512)
                    while (true) {
                        if (Thread.currentThread().isInterrupted) throw InterruptedException()
                        val count = input.read(buffer)
                        if (count < 0) break
                        if (bytes.size() + count > 4096) {
                            throw IllegalArgumentException("Kernel attribute exceeds read limit")
                        }
                        bytes.write(buffer, 0, count)
                    }
                    bytes.toString("UTF-8").trim()
                }
                return SourceRead.Value(path, text)
            } catch (interrupted: InterruptedException) {
                Thread.currentThread().interrupt()
                throw interrupted
            } catch (_: NoSuchFileException) {
                // Missing and permission-denied sources are deliberately different states.
            } catch (_: AccessDeniedException) {
                denied = SourceRead.Restricted(path)
            } catch (_: SecurityException) {
                denied = SourceRead.Restricted(path)
            } catch (error: Exception) {
                failure = SourceRead.Failure(path, error.javaClass.simpleName)
            }
        }
        return denied ?: failure ?: SourceRead.Missing
    }
}

private fun mapped(
    key: String, label: String, source: SourceRead, parser: (String) -> String
): ProbeResult = when (source) {
    is SourceRead.Value -> try {
        ProbeResult(key, label, ProbeStatus.AVAILABLE, parser(source.text), source.path)
    } catch (_: IllegalArgumentException) {
        ProbeResult(key, label, ProbeStatus.ERROR, "Invalid reading", source.path)
    }
    is SourceRead.Restricted -> ProbeResult(
        key, label, ProbeStatus.RESTRICTED, "Restricted", "Android denied access: ${source.path}"
    )
    is SourceRead.Failure -> ProbeResult(
        key, label, ProbeStatus.ERROR, "Reading unavailable", "${source.path}: ${source.reason}"
    )
    SourceRead.Missing -> ProbeResult(
        key, label, ProbeStatus.UNSUPPORTED, "Not exposed", "None of the checked kernel attributes is exposed"
    )
}

class CpuProbe(private val reader: FileSourceReader = FileSourceReader()) : Probe {
    override fun read(): ProbeResult = mapped("cpu", "CPU 0 frequency", reader.readFirst(listOf(
        "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq",
        "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq"
    ))) {
        val khz = it.trim().toLong()
        require(khz > 0)
        "%.0f MHz".format(Locale.US, khz / 1000.0)
    }
}

class ThermalProbe(private val reader: FileSourceReader = FileSourceReader()) : Probe {
    override fun read(): ProbeResult = mapped("thermal", "Exposed thermal zone", reader.readFirst(listOf(
        "/sys/class/thermal/thermal_zone0/temp",
        "/sys/class/thermal/thermal_zone1/temp"
    ))) {
        // The Linux thermal-zone temp ABI is millidegrees Celsius, including zero/negative values.
        val celsius = it.trim().toDouble() / 1000.0
        require(celsius.isFinite() && celsius in -50.0..200.0)
        "%.1f °C".format(Locale.US, celsius)
    }
}
