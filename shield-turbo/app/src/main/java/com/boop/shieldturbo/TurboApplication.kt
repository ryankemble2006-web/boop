package com.boop.shieldturbo

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.os.Bundle

/** Shows the startup caveat once, on the first real app launch, never during boot cleanup. */
class TurboApplication : Application(), Application.ActivityLifecycleCallbacks {
    private var noteShownThisProcess = false

    override fun onCreate() {
        super.onCreate()
        // Old builds recorded presentation diagnostics for a boot banner that no longer exists.
        getSharedPreferences("turbo_clean_start", MODE_PRIVATE)
            .edit()
            .remove("last_indicator_diagnostic")
            .apply()
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity !is MainActivity || noteShownThisProcess || activity.isFinishing) return
        val prefs = getSharedPreferences(FIRST_INSTALL_PREFS, MODE_PRIVATE)
        if (prefs.getBoolean(FIRST_INSTALL_STARTUP_NOTE_SHOWN, false)) return

        noteShownThisProcess = true
        AlertDialog.Builder(activity)
            .setTitle(R.string.first_install_startup_title)
            .setMessage(R.string.first_install_startup_message)
            .setPositiveButton(R.string.close, null)
            .setOnDismissListener {
                prefs.edit().putBoolean(FIRST_INSTALL_STARTUP_NOTE_SHOWN, true).apply()
            }
            .show()
    }

    override fun onActivityCreated(activity: Activity, state: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    companion object {
        private const val FIRST_INSTALL_PREFS = "first_install_ui"
        private const val FIRST_INSTALL_STARTUP_NOTE_SHOWN = "first_install_startup_note_shown"
    }
}
