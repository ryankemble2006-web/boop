package com.boop.shieldturbo.system

import android.content.Context
import android.content.Intent
import com.boop.shieldturbo.power.FirmwarePages
import com.boop.shieldturbo.power.PowerActivity

object SystemRoutes {
    fun resolve(context: Context, shortcut: SystemShortcut): Intent? {
        shortcut.firmwarePage()?.let { page ->
            val saved = context.getSharedPreferences("turbo_power", Context.MODE_PRIVATE)
                .getString("confirmed_$page", null)
            if (saved != null) {
                val route = FirmwarePages.find(context, page).firstOrNull { it.component.flattenToString() == saved }
                if (route != null) return Intent(route.action).setComponent(route.component)
            }
            return Intent(context, PowerActivity::class.java).putExtra("settings_page", page)
        }
        return shortcut.actions().asSequence()
            .map(::Intent)
            .firstOrNull { it.resolveActivity(context.packageManager) != null }
    }

    fun storage(context: Context): Intent? = resolve(context, SystemShortcut.STORAGE)

    fun manageApps(context: Context): Intent? = resolve(context, SystemShortcut.APPS)
}
