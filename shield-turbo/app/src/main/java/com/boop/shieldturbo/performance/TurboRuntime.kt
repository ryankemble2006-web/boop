package com.boop.shieldturbo.performance

import android.content.Context
import android.os.Build
import android.os.PowerManager

class TurboRuntime(context: Context) {
    private val app = context.applicationContext
    private val store = TurboStore(app)
    private val controller = TurboController(
        store = store,
        control = NvidiaTurboPort(app),
        thermal = AndroidTurboThermalPort(app),
        clock = TurboClock { System.currentTimeMillis() },
        severeStatus = PowerManager.THERMAL_STATUS_SEVERE
    )

    fun snapshot(): TurboSnapshot = synchronized(lock) { store.load() }

    fun enable(): TurboOutcome = synchronized(lock) {
        controller.enable()
    }

    fun disable(reason: String = "Manual NORMAL"): TurboOutcome = synchronized(lock) {
        controller.disable(reason)
    }

    fun bootReapply(): TurboOutcome = synchronized(lock) {
        controller.bootReapply()
    }

    fun thermal(status: Int): TurboOutcome = synchronized(lock) {
        controller.onThermalStatus(status)
    }

    private companion object {
        val lock = Any()
    }
}

private class AndroidTurboThermalPort(context: Context) : TurboThermalPort {
    private val power = context.getSystemService(PowerManager::class.java)

    override fun currentStatus(): Int? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) power?.currentThermalStatus else null
}
