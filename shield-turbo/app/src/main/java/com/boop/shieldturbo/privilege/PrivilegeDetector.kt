package com.boop.shieldturbo.privilege

import android.content.Context
import android.content.pm.PackageManager
import android.os.Process

enum class PrivilegeTier { STANDARD, ADB_TURBO, ROOT }

/** Reports this process's authority, not whether an unrelated root manager might be installed. */
class PrivilegeDetector(
    private val adbEvidence: () -> Boolean,
    private val rootEvidence: () -> Boolean
) {
    fun detect(): PrivilegeTier = when {
        safely(rootEvidence) -> PrivilegeTier.ROOT
        safely(adbEvidence) -> PrivilegeTier.ADB_TURBO
        else -> PrivilegeTier.STANDARD
    }

    private fun safely(evidence: () -> Boolean): Boolean = try {
        evidence()
    } catch (_: SecurityException) {
        false
    } catch (_: IllegalStateException) {
        false
    }

    companion object {
        fun android(context: Context) = PrivilegeDetector(
            adbEvidence = {
                context.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED
            },
            rootEvidence = { Process.myUid() == 0 }
        )
    }
}
