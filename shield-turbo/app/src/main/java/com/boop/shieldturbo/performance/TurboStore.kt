package com.boop.shieldturbo.performance

import android.annotation.SuppressLint
import android.content.Context

class TurboStore(context: Context) : TurboStateStore {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun load(): TurboSnapshot {
        val raw = prefs.getString(KEY, null) ?: return TurboSnapshot()
        return TurboStateCodec.decode(raw) ?: TurboSnapshot(
            phase = TurboPhase.RECOVERY_REQUIRED,
            desiredTurbo = false,
            lastReason = "CHECK PERFORMANCE SETTINGS: saved TURBO state is invalid"
        )
    }

    @SuppressLint("ApplySharedPref")
    override fun save(snapshot: TurboSnapshot): Boolean =
        prefs.edit().putString(KEY, TurboStateCodec.encode(snapshot)).commit()

    private companion object {
        const val PREFS = "turbo_performance_state"
        const val KEY = "snapshot"
    }
}
