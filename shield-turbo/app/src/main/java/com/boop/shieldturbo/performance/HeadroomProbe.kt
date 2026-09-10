package com.boop.shieldturbo.performance

import android.content.Context
import com.boop.shieldturbo.power.AdbWire
import com.boop.shieldturbo.power.LocalBridge

data class HeadroomSnapshot(
    val cpu: String,
    val gpu: String,
    val memory: String,
    val cooling: String,
    val extraStockControls: String
)

/** Read-only discovery of stock Shield performance headroom. No values are written. */
class HeadroomProbe(
    context: Context,
    private val bridge: LocalBridge = LocalBridge(context.applicationContext)
) {
    fun scan(): HeadroomSnapshot = bridge.withTrustedAdb { adb ->
        HeadroomSnapshot(
            cpu = readOptional(adb, CPU_COMMAND),
            gpu = readOptional(adb, GPU_COMMAND),
            memory = readOptional(adb, MEMORY_COMMAND),
            cooling = readOptional(adb, COOLING_COMMAND),
            extraStockControls = readOptional(adb, EXTRA_STOCK_COMMAND)
        )
    }

    private fun readOptional(adb: AdbWire, command: String): String = runCatching {
        bridge.checked(adb, command)
    }.getOrDefault("")
        .lineSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .take(MAX_LINES_PER_SECTION)
        .joinToString("\n") { it.take(MAX_CHARS_PER_LINE) }

    private companion object {
        const val MAX_LINES_PER_SECTION = 6
        const val MAX_CHARS_PER_LINE = 180

        val CPU_COMMAND = """
            sh -c '
            printf "online="; cat /sys/devices/system/cpu/online 2>/dev/null || printf "?"; printf "\n"
            for p in /sys/devices/system/cpu/cpufreq/policy*; do
              [ -d "${'$'}p" ] || continue
              printf "%s " "${'$'}{p##*/}"
              for f in scaling_cur_freq scaling_max_freq scaling_governor; do
                if [ -r "${'$'}p/${'$'}f" ]; then
                  printf "%s=" "${'$'}f"
                  cat "${'$'}p/${'$'}f" 2>/dev/null | tr "\n" " "
                fi
              done
              printf "\n"
            done
            true'
        """.trimIndent()

        val GPU_COMMAND = """
            sh -c '
            for p in /sys/class/devfreq/*gpu* /sys/devices/*gpu*/devfreq/*; do
              [ -d "${'$'}p" ] || continue
              printf "gpu=%s\n" "${'$'}p"
              for f in cur_freq max_freq min_freq governor available_frequencies; do
                if [ -r "${'$'}p/${'$'}f" ]; then
                  printf "%s=" "${'$'}f"
                  cat "${'$'}p/${'$'}f" 2>/dev/null | tr "\n" " "
                  printf "\n"
                fi
              done
              break
            done
            true'
        """.trimIndent()

        val MEMORY_COMMAND = """
            sh -c '
            for f in \
              /sys/kernel/debug/clock/emc/rate \
              /sys/kernel/debug/bpmp/debug/clk/emc/rate \
              /sys/kernel/debug/clk/emc/clk_rate \
              /sys/class/devfreq/*emc*/cur_freq \
              /sys/class/devfreq/*emc*/max_freq; do
              if [ -r "${'$'}f" ]; then
                printf "emc %s=" "${'$'}f"
                cat "${'$'}f" 2>/dev/null | tr "\n" " "
                printf "\n"
              fi
            done
            true'
        """.trimIndent()

        val COOLING_COMMAND = """
            sh -c '
            for z in /sys/class/thermal/thermal_zone*; do
              [ -d "${'$'}z" ] || continue
              if [ -r "${'$'}z/type" ]; then printf "thermal "; cat "${'$'}z/type" 2>/dev/null | tr "\n" " "; fi
              if [ -r "${'$'}z/temp" ]; then printf "temp="; cat "${'$'}z/temp" 2>/dev/null | tr "\n" " "; fi
              printf "\n"
            done
            for c in /sys/class/thermal/cooling_device*; do
              [ -d "${'$'}c" ] || continue
              if [ -r "${'$'}c/type" ]; then printf "cooling_device "; cat "${'$'}c/type" 2>/dev/null | tr "\n" " "; fi
              if [ -r "${'$'}c/cur_state" ]; then printf "state="; cat "${'$'}c/cur_state" 2>/dev/null | tr "\n" " "; fi
              if [ -r "${'$'}c/max_state" ]; then printf "max="; cat "${'$'}c/max_state" 2>/dev/null | tr "\n" " "; fi
              printf "\n"
            done
            getprop 2>/dev/null | grep -Ei "fan|thermal|cool" | head -n 8 || true
            true'
        """.trimIndent()

        val EXTRA_STOCK_COMMAND = """
            sh -c '
            printf "nv_power_mode="; settings get system nv_power_mode 2>/dev/null; printf "\n"
            settings list system 2>/dev/null | grep -Ei "nvidia|processor|performance|fan|power|emc" | grep -v "^nv_power_mode=" | head -n 8 || true
            settings list global 2>/dev/null | grep -Ei "nvidia|processor|performance|fan|power|emc" | head -n 8 || true
            true'
        """.trimIndent()
    }
}
