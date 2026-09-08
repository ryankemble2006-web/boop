package com.boop.shieldturbo.power

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings

data class FirmwarePage(val title: String, val component: ComponentName, val action: String?, val score: Int)

object FirmwarePages {
    private val actions = listOf(Settings.ACTION_DISPLAY_SETTINGS, Settings.ACTION_SOUND_SETTINGS, Settings.ACTION_ACCESSIBILITY_SETTINGS)
    @Suppress("DEPRECATION")
    fun find(context: Context, page: String): List<FirmwarePage> {
        val pm = context.packageManager
        val packages = linkedSetOf("com.android.tv.settings", "com.android.settings")
        val actionFor = mutableMapOf<String, String>()
        actions.forEach { action ->
            pm.queryIntentActivities(Intent(action), 0).forEach { result ->
                val info = result.activityInfo ?: return@forEach
                if ((info.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                    packages += info.packageName
                    actionFor[ComponentName(info.packageName, info.name).flattenToString()] = action
                }
            }
        }
        return packages.flatMap { pkg ->
            val info = runCatching { pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES) }.getOrNull()
            info?.activities.orEmpty().mapNotNull { activity ->
                if (!activity.exported || !activity.enabled || !activity.applicationInfo.enabled ||
                    (activity.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) return@mapNotNull null
                val ownLabel = if (activity.labelRes != 0 || activity.nonLocalizedLabel != null) runCatching { activity.loadLabel(pm).toString() }.getOrDefault("") else ""
                val score = PowerPolicy.pageScore(page, activity.name, ownLabel)
                val name = activity.name.lowercase()
                val related = if (page == "accessibility") name.contains("accessibility") else name.contains("display") || name.contains("sound")
                if (score == 0 && !related) return@mapNotNull null
                val component = ComponentName(pkg, activity.name)
                if (!PowerPolicy.validComponent(component.flattenToString())) return@mapNotNull null
                val short = activity.name.substringAfterLast('.').removeSuffix("Activity").replace(Regex("([a-z])([A-Z])"), "$1 $2")
                FirmwarePage(ownLabel.ifBlank { short }, component, actionFor[component.flattenToString()], score)
            }
        }.distinctBy { it.component }.sortedWith(compareByDescending<FirmwarePage> { it.score }.thenBy { it.title })
    }
}
