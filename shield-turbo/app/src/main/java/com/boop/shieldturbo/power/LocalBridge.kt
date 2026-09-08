package com.boop.shieldturbo.power

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import com.boop.shieldturbo.cleanstart.CleanStartPolicy
import com.boop.shieldturbo.cleanstart.CleanStopResult
import com.boop.shieldturbo.startup.OriginalStartupState
import com.boop.shieldturbo.startup.StartupPolicy
import java.io.File
import java.io.IOException

class LocalBridge(private val context: Context) {
    @Volatile private var active: AdbWire? = null
    @Volatile private var cancelled = false

    fun resetCancellation() { cancelled = false }
    fun cancel() { cancelled = true; runCatching { active?.close() } }

    fun <T> withAdb(approval: () -> Unit = {}, operation: (AdbWire) -> T): T =
        withAdbInternal(allowNewApproval = true, approval = approval, operation = operation)

    /** Background/boot use only: the saved key must already exist and already be trusted. */
    fun <T> withTrustedAdb(operation: (AdbWire) -> T): T =
        withAdbInternal(allowNewApproval = false, approval = {}, operation = operation)

    private fun <T> withAdbInternal(
        allowNewApproval: Boolean,
        approval: () -> Unit,
        operation: (AdbWire) -> T
    ): T {
        if (cancelled || Thread.currentThread().isInterrupted) throw IOException("Cancelled")
        val identityFile = File(context.noBackupFilesDir, "turbo-local-adb.key")
        if (!allowNewApproval && !identityFile.exists()) {
            throw AdbWire.AdbApprovalRequiredException("ADB TURBO has not been authorised interactively yet")
        }
        val identity = AdbWire.identity(identityFile)
        AdbWire().use { adb ->
            active = adb
            try {
                if (cancelled || Thread.currentThread().isInterrupted) throw IOException("Cancelled")
                adb.connect(5555, identity, 45000, Runnable { approval() }, allowNewApproval)
                val uid = checked(adb, "id -u").trim()
                if (uid != "2000" && uid != "0") throw IOException("The local connection is not an ADB shell")
                return operation(adb)
            } finally { active = null }
        }
    }

    fun checked(adb: AdbWire, command: String): String {
        val result = adb.execute(command, 20000)
        if (result.exitCode != 0 || result.output.lineSequence().any {
            it.startsWith("Error:") || it.contains("SecurityException") || it.contains("Permission Denial")
        }) throw IOException(result.output.take(1200).ifBlank { "Shield rejected the command (${result.exitCode})" })
        return result.output
    }

    fun stopAndVerify(adb: AdbWire, packageName: String): CleanStopResult {
        require(CleanStartPolicy.validPackage(packageName)) { "Invalid or protected package" }
        val currentUser = CleanStartPolicy.parseCurrentUser(checked(adb, CleanStartPolicy.currentUserCommand()))
            ?: throw IOException("Could not verify the current Shield user")
        val beforeNames = CleanStartPolicy.parseProcessNames(checked(adb, CleanStartPolicy.processSnapshotCommand()))
        val before = CleanStartPolicy.packageProcesses(packageName, beforeNames)
        checked(adb, CleanStartPolicy.forceStopCommand(packageName))
        val afterNames = CleanStartPolicy.parseProcessNames(checked(adb, CleanStartPolicy.processSnapshotCommand()))
        val after = CleanStartPolicy.packageProcesses(packageName, afterNames)
        val userState = checked(adb, CleanStartPolicy.userStateCommand(packageName, currentUser))
        val stopped = CleanStartPolicy.parseStopped(userState)
            ?: throw IOException("Could not verify the package stopped state")
        val enabled = CleanStartPolicy.parseEnabled(userState)
            ?: throw IOException("Could not verify the package enabled state")
        return CleanStopResult(packageName, before, after, stopped, enabled).also {
            if (!it.verified) {
                val detail = when {
                    it.afterProcesses.isNotEmpty() -> "processes remained: ${it.afterProcesses.joinToString()}"
                    !it.stopped -> "Android did not retain stopped state"
                    else -> "package became $enabled"
                }
                throw IOException("CLEAN START could not verify $packageName: $detail")
            }
        }
    }

    fun resumedPackage(adb: AdbWire): String? =
        CleanStartPolicy.parseResumedPackage(checked(adb, CleanStartPolicy.resumedActivityCommand()))

    fun hasSettingsAccess() = context.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED
    fun enable(approval: () -> Unit): String = withAdb(approval) { adb ->
        checked(adb, "pm grant --user current com.boop.shieldturbo android.permission.WRITE_SECURE_SETTINGS")
        if (!hasSettingsAccess()) throw IOException("ADB connected, but the Shield did not grant settings access")
        "ADB TURBO connected and settings access verified. No laptop command is needed."
    }

    fun readStartupState(adb: AdbWire, packageName: String): OriginalStartupState {
        val run = readAppOp(adb, packageName, "RUN_IN_BACKGROUND")
        val runAny = readAppOp(adb, packageName, "RUN_ANY_IN_BACKGROUND")
        val enabled = StartupPolicy.parseEnabled(checked(adb, StartupPolicy.enabledQuery(packageName)))
            ?: throw IOException("Could not capture the app's original enabled state; nothing changed")
        return OriginalStartupState(packageName, run, runAny, enabled)
    }

    private fun readAppOp(adb: AdbWire, packageName: String, op: String): String {
        val output = checked(adb, StartupPolicy.query(packageName, op))
        StartupPolicy.parseMode(output)?.let { return it }
        if (output.contains("No operations", ignoreCase = true) || output.isBlank()) return "default"
        throw IOException("Could not capture the app's original $op mode; nothing changed")
    }

    fun blockStartup(adb: AdbWire, original: OriginalStartupState) {
        try {
            StartupPolicy.backgroundBlock(original.packageName).forEach { checked(adb, it) }
            val run = readAppOp(adb, original.packageName, "RUN_IN_BACKGROUND")
            val runAny = readAppOp(adb, original.packageName, "RUN_ANY_IN_BACKGROUND")
            if (run != "ignore" || runAny != "ignore") throw IOException("Shield did not keep the background restriction")
        } catch (failure: Exception) {
            runCatching { restoreStartup(adb, original) }
            throw failure
        }
    }

    fun hardBlock(adb: AdbWire, original: OriginalStartupState) {
        try {
            checked(adb, StartupPolicy.hardBlock(original.packageName))
            val state = StartupPolicy.parseEnabled(checked(adb, StartupPolicy.enabledQuery(original.packageName)))
            if (state != "disabled-user" && state != "disabled") throw IOException("Shield did not keep the hard block")
        } catch (failure: Exception) {
            runCatching { restoreStartup(adb, original) }
            throw failure
        }
    }

    fun restoreStartup(adb: AdbWire, original: OriginalStartupState) {
        StartupPolicy.backgroundRestore(
            original.packageName,
            original.runInBackground,
            original.runAnyInBackground
        ).forEach { checked(adb, it) }
        checked(adb, StartupPolicy.restoreEnabled(original.packageName, original.enabledState))
        val run = readAppOp(adb, original.packageName, "RUN_IN_BACKGROUND")
        val runAny = readAppOp(adb, original.packageName, "RUN_ANY_IN_BACKGROUND")
        val enabled = StartupPolicy.parseEnabled(checked(adb, StartupPolicy.enabledQuery(original.packageName)))
        if (run != original.runInBackground || runAny != original.runAnyInBackground || enabled != original.enabledState) {
            throw IOException("Undo read-back did not match the saved original state")
        }
    }

    fun animation(scale: String?): String {
        check(hasSettingsAccess()) { "Use ENABLE ADB TURBO first" }
        val keys = listOf("window_animation_scale", "transition_animation_scale", "animator_duration_scale")
        val prefs = context.getSharedPreferences("turbo_animation_undo", Context.MODE_PRIVATE)
        val before = keys.associateWith { Settings.Global.getString(context.contentResolver, it) }
        if (scale != null && !prefs.getBoolean("saved", false)) {
            val edit = prefs.edit().putBoolean("saved", true)
            before.forEach { (key, value) -> if (value == null) edit.remove(key) else edit.putString(key, value) }
            check(edit.commit()) { "Could not save your original animation settings; nothing changed" }
        }
        if (scale == null) check(prefs.getBoolean("saved", false)) { "No animation changes to undo yet" }
        else require(scale in listOf("0", "0.5", "1"))
        val desired = keys.associateWith { if (scale == null) prefs.getString(it, null) else scale }
        try {
            desired.forEach { (key, value) ->
                check(Settings.Global.putString(context.contentResolver, key, value)) { "The Shield rejected $key" }
                val actual = Settings.Global.getString(context.contentResolver, key)
                check(if (value == null) actual == null else actual?.toFloatOrNull() == value.toFloatOrNull()) { "Animation read-back did not match" }
            }
        } catch (failure: Exception) {
            val restored = before.map { (key, value) -> runCatching {
                Settings.Global.putString(context.contentResolver, key, value) && Settings.Global.getString(context.contentResolver, key) == value
            }.getOrDefault(false) }.all { it }
            throw IOException(if (restored) "Change rejected; previous values restored" else "Change was incomplete. Check animation settings before continuing", failure)
        }
        if (scale == null) prefs.edit().clear().commit()
        return if (scale == null) "Your saved animation values were restored and checked." else "All three animation scales read back as ${scale}x. UNDO restores your previous values."
    }
}
