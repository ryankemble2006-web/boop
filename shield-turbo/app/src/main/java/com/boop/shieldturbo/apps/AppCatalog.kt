package com.boop.shieldturbo.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

object AppCatalog {
    fun sortAndDedupe(apps: List<LaunchableApp>): List<LaunchableApp> =
        apps.distinctBy { it.packageName }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })

    fun query(context: Context): List<LaunchableApp> {
        val pm = context.packageManager
        val found = mutableListOf<LaunchableApp>()
        val intents = listOf(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER),
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        )
        intents.forEach { intent ->
            // Launcher intent filters do not have to declare CATEGORY_DEFAULT.
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0).forEach { info ->
                val pkg = info.activityInfo?.packageName ?: return@forEach
                if (pkg != context.packageName) {
                    found += LaunchableApp(pkg, info.loadLabel(pm).toString().ifBlank { pkg })
                }
            }
        }
        return sortAndDedupe(found)
    }
}
