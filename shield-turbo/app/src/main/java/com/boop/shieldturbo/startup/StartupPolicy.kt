package com.boop.shieldturbo.startup

object StartupPolicy {
    private val packageName = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+")
    private val allowedModes = setOf("allow", "ignore", "deny", "default")

    fun validPackage(value: String?): Boolean =
        value != null && value.length < 180 && packageName.matches(value)

    private fun requirePackage(value: String): String = value.also {
        require(validPackage(it)) { "Invalid package name" }
    }

    private fun requireMode(value: String): String = value.lowercase().also {
        require(it in allowedModes) { "Unsupported app-op mode" }
    }

    fun backgroundBlock(packageName: String): List<String> {
        val pkg = requirePackage(packageName)
        return listOf(
            "cmd appops set $pkg RUN_IN_BACKGROUND ignore",
            "cmd appops set $pkg RUN_ANY_IN_BACKGROUND ignore"
        )
    }

    fun backgroundRestore(packageName: String, runInBackground: String, runAnyInBackground: String): List<String> {
        val pkg = requirePackage(packageName)
        val first = requireMode(runInBackground)
        val second = requireMode(runAnyInBackground)
        return listOf(
            "cmd appops set $pkg RUN_IN_BACKGROUND $first",
            "cmd appops set $pkg RUN_ANY_IN_BACKGROUND $second"
        )
    }

    fun query(packageName: String, op: String): String {
        val pkg = requirePackage(packageName)
        require(op == "RUN_IN_BACKGROUND" || op == "RUN_ANY_IN_BACKGROUND") { "Unsupported app-op" }
        return "cmd appops get $pkg $op"
    }

    fun parseMode(output: String): String? =
        Regex("(?:RUN_IN_BACKGROUND|RUN_ANY_IN_BACKGROUND):\\s*(allow|ignore|deny|default)\\b")
            .find(output)?.groupValues?.get(1)

    fun hardBlock(packageName: String): String =
        "pm disable-user --user current ${requirePackage(packageName)}"

    fun restoreEnabled(packageName: String, savedState: String): String {
        val pkg = requirePackage(packageName)
        return when (savedState) {
            "enabled" -> "pm enable --user current $pkg"
            "default" -> "pm default-state --user current $pkg"
            "disabled", "disabled-user" -> "pm disable-user --user current $pkg"
            "disabled-until-used" -> "pm disable-until-used --user current $pkg"
            else -> throw IllegalArgumentException("Unsupported enabled state")
        }
    }

    fun enabledQuery(packageName: String): String =
        "dumpsys package ${requirePackage(packageName)} | grep -m1 -o 'enabled=[0-4]'"

    fun parseEnabled(output: String): String? = when (Regex("enabled=([0-4])").find(output)?.groupValues?.get(1)) {
        "0" -> "default"
        "1" -> "enabled"
        "2" -> "disabled"
        "3" -> "disabled-user"
        "4" -> "disabled-until-used"
        else -> null
    }
}
