package com.boop.shieldturbo.system

import android.content.Context
import android.content.Intent

object SystemRoutes {
    fun resolve(context: Context, shortcut: SystemShortcut): Intent? {
        shortcut.component()?.let { target ->
            // Explicit starts do not need package-visibility preflight. MainActivity.safeStart
            // handles a missing/disabled/private firmware activity without opening a wrong page.
            return Intent(Intent.ACTION_MAIN).setClassName(target.packageName, target.className)
        }
        return shortcut.actions().asSequence()
            .map(::Intent)
            .firstOrNull { it.resolveActivity(context.packageManager) != null }
    }

    fun storage(context: Context): Intent? = resolve(context, SystemShortcut.STORAGE)

    fun manageApps(context: Context): Intent? = resolve(context, SystemShortcut.APPS)
}
