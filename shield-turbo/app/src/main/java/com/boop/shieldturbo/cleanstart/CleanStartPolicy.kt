package com.boop.shieldturbo.cleanstart

/** Pure command and parser policy for CLEAN START. No Android framework dependency. */
object CleanStartPolicy {
    private val packageName = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+")
    private val protectedExact = setOf("com.boop.shieldturbo", "com.boop.alpha1")

    fun validPackage(value: String?): Boolean =
        value != null && value.length < 180 && packageName.matches(value) && value !in protectedExact

    private fun requirePackage(value: String): String = value.also {
        require(validPackage(it)) { "Invalid or protected package name" }
    }

    fun forceStopCommand(packageName: String): String =
        "am force-stop --user current '${requirePackage(packageName)}'"

    fun currentUserCommand(): String = "cmd activity get-current-user"

    fun processSnapshotCommand(): String = "ps -A -o PID,NAME"

    fun userStateCommand(packageName: String, userId: Int): String {
        require(userId >= 0) { "Invalid user id" }
        val pkg = requirePackage(packageName)
        return "dumpsys package '$pkg' | grep -m1 'User $userId:'"
    }

    fun parseCurrentUser(output: String): Int? = output.trim().toIntOrNull()?.takeIf { it >= 0 }

    fun parseProcessNames(output: String): List<String> = output.lineSequence().mapNotNull { raw ->
        val line = raw.trim()
        if (line.isEmpty()) return@mapNotNull null
        val parts = line.split(Regex("\\s+"))
        if (parts.size < 2 || parts.first().toIntOrNull() == null) return@mapNotNull null
        parts.last().takeIf { it.isNotBlank() }
    }.toList()

    fun packageProcesses(packageName: String, names: List<String>): List<String> {
        val pkg = requirePackage(packageName)
        return names.filter { it == pkg || it.startsWith("$pkg:") }
    }

    fun parseStopped(output: String): Boolean? = when (
        Regex("\\bstopped=(true|false)\\b").find(output)?.groupValues?.get(1)
    ) {
        "true" -> true
        "false" -> false
        else -> null
    }

    fun parseEnabled(output: String): String? = when (
        Regex("\\benabled=([0-4])\\b").find(output)?.groupValues?.get(1)
    ) {
        "0" -> "default"
        "1" -> "enabled"
        "2" -> "disabled"
        "3" -> "disabled-user"
        "4" -> "disabled-until-used"
        else -> null
    }

    fun parseResumedPackage(output: String): String? {
        val marker = Regex("(?:mResumedActivity:|topResumedActivity=)[^\\n]*?\\bu\\d+\\s+([A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z0-9_]+)+)/")
        return marker.find(output)?.groupValues?.get(1)?.takeIf(::validPackage)
    }
}

data class CleanStopResult(
    val packageName: String,
    val beforeProcesses: List<String>,
    val afterProcesses: List<String>,
    val stopped: Boolean,
    val enabledState: String
) {
    val verified: Boolean
        get() = afterProcesses.isEmpty() && stopped && enabledState !in setOf("disabled", "disabled-user", "disabled-until-used")
}
