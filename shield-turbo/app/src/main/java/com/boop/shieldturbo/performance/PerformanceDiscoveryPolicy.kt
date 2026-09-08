package com.boop.shieldturbo.performance

object PerformanceDiscoveryPolicy {
    private val clueTokens = listOf("nvidia", "processor", "performance", "fan", "power")

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

    fun settingClues(namespace: String, text: String): List<SettingClue> = text
        .lineSequence()
        .mapNotNull { parseClueLine(namespace, it) }
        .filter { clue -> clueTokens.any { token -> clue.key.contains(token, ignoreCase = true) } }
        .take(120)
        .toList()

    private fun parseClueLine(namespace: String, raw: String): SettingClue? {
        val line = raw.trim()
        if (line.isEmpty()) return null
        val key: String
        val value: String
        if (line.startsWith("[") && line.contains("]: [")) {
            val separator = line.indexOf("]: [")
            if (separator <= 1 || !line.endsWith(']')) return null
            key = line.substring(1, separator)
            value = line.substring(separator + 4, line.length - 1)
        } else {
            val separator = line.indexOf('=')
            if (separator <= 0) return null
            key = line.substring(0, separator).trim()
            value = line.substring(separator + 1).trim()
        }
        if (key.isBlank()) return null
        return SettingClue(namespace, key, value.take(512))
    }

    fun pathProbeCommand(): String {
        val quoted = paths.joinToString(" ") { "'$it'" }
        return """
            for p in $quoted; do
              if [ -e "${'$'}p" ]; then
                w=0
                test -w "${'$'}p" && w=1
                v=${'$'}(cat "${'$'}p" 2>/dev/null | head -c 1024 | tr '\n' ' ')
                printf '%s\t1\t%s\t%s\n' "${'$'}p" "${'$'}w" "${'$'}v"
              else
                printf '%s\t0\t0\t\n' "${'$'}p"
              fi
            done
        """.trimIndent()
    }

    fun parsePathProbe(text: String): List<PathCapability> = text
        .lineSequence()
        .filter { it.isNotEmpty() }
        .mapNotNull { line ->
            val parts = line.split('\t', limit = 4)
            if (parts.size < 3 || parts[0].isBlank()) return@mapNotNull null
            PathCapability(
                path = parts[0],
                present = parts[1] == "1",
                shellWritable = parts[2] == "1",
                value = parts.getOrElse(3) { "" }.trim().take(1024)
            )
        }
        .toList()
}
