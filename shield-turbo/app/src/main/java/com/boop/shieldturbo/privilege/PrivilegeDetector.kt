package com.boop.shieldturbo.privilege

import android.content.Context
import android.content.pm.PackageManager

enum class PrivilegeTier { STANDARD, ADB_TURBO, ROOT }
class PrivilegeDetector(private val adbEvidence:()->Boolean, private val rootEvidence:()->Boolean){ fun detect()=when{rootEvidence()->PrivilegeTier.ROOT;adbEvidence()->PrivilegeTier.ADB_TURBO;else->PrivilegeTier.STANDARD}
    companion object { fun android(context:Context)=PrivilegeDetector({ context.checkSelfPermission("android.permission.PACKAGE_USAGE_STATS")==PackageManager.PERMISSION_GRANTED }, { arrayOf("/system/xbin/su","/system/bin/su","/sbin/su").any{java.io.File(it).exists()} }) }
}
