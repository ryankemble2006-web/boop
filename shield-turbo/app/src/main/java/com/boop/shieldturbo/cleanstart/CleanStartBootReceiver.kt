package com.boop.shieldturbo.cleanstart

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Schedules one bounded cleanup after a real boot only when the user enabled it. */
class CleanStartBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val appContext = context.applicationContext
        val store = CleanStartStore.android(appContext)
        val targets = store.targets()
        if (CleanStartScheduler.shouldSchedule(store.autoEnabled(), targets.size)) {
            CleanStartScheduler.schedule(appContext, 0)
        }
    }
}
