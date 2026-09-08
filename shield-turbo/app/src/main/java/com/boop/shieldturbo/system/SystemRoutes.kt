package com.boop.shieldturbo.system

import android.content.Context
import android.content.Intent

object SystemRoutes {
    fun resolve(context: Context, shortcut: SystemShortcut): Intent? =
        shortcut.actions().asSequence()
            .map(::Intent)
            .firstOrNull { it.resolveActivity(context.packageManager) != null }

    fun storage(context: Context): Intent? = resolve(context, SystemShortcut.STORAGE)

    fun manageApps(context: Context): Intent? = resolve(context, SystemShortcut.APPS)
}
