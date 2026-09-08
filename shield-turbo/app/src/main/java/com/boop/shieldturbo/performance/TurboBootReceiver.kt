package com.boop.shieldturbo.performance

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TurboBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val snapshot = TurboStore(context.applicationContext).load()
        if (snapshot.phase == TurboPhase.TURBO_VERIFIED && snapshot.desiredTurbo) {
            TurboThermalWatchdogService.start(context.applicationContext, bootReapply = true)
        }
    }
}
