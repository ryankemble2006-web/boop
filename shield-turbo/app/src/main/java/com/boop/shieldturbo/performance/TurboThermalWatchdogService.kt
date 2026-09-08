package com.boop.shieldturbo.performance

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TurboThermalWatchdogService : Service() {
    private val worker: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var runtime: TurboRuntime
    private lateinit var power: PowerManager
    private var unregisterThermalListener: (() -> Unit)? = null

    override fun onCreate() {
        super.onCreate()
        runtime = TurboRuntime(applicationContext)
        power = getSystemService(PowerManager::class.java)
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(ACTIVE_NOTIFICATION_ID, activeNotification())

        val snapshot = runtime.snapshot()
        if (snapshot.phase != TurboPhase.TURBO_VERIFIED || !snapshot.desiredTurbo) {
            stopSelf()
            return START_NOT_STICKY
        }

        registerThermalListenerOnce()
        val bootReapply = intent?.getBooleanExtra(EXTRA_BOOT_REAPPLY, false) == true
        val current = currentThermalStatus()

        worker.execute {
            val outcome = when {
                current == null -> runtime.disable("TURBO off: thermal protection is unavailable")
                bootReapply -> runtime.bootReapply()
                current >= PowerManager.THERMAL_STATUS_SEVERE -> runtime.thermal(current)
                else -> null
            }
            if (outcome != null) handleOutcome(outcome)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterThermalListener?.invoke()
        unregisterThermalListener = null
        worker.shutdownNow()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerThermalListenerOnce() {
        if (unregisterThermalListener != null || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        unregisterThermalListener = ThermalApi29.register(power, worker) { status ->
            val outcome = runtime.thermal(status)
            handleOutcome(outcome)
        }
    }

    private fun currentThermalStatus(): Int? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ThermalApi29.current(power) else null

    private fun handleOutcome(outcome: TurboOutcome) {
        if (outcome.snapshot.phase == TurboPhase.TURBO_VERIFIED && outcome.snapshot.desiredTurbo) {
            notificationManager().notify(ACTIVE_NOTIFICATION_ID, activeNotification())
            return
        }

        val severeFallback = outcome.snapshot.lastThermalStatus
            ?.let { it >= PowerManager.THERMAL_STATUS_SEVERE } == true &&
            outcome.snapshot.lastThermalFallbackEpochMs != null
        if (severeFallback) {
            notificationManager().notify(FALLBACK_NOTIFICATION_ID, fallbackNotification())
        }
        stopSelf()
    }

    private fun ensureChannel() {
        notificationManager().createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "SHIELD TURBO performance",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Thermal protection while stock TURBO performance mode is active"
                setShowBadge(false)
            }
        )
    }

    private fun activeNotification(): Notification = Notification.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_notify_sync_noanim)
        .setContentTitle("SHIELD TURBO")
        .setContentText("Performance mode active • thermal watchdog on")
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setShowWhen(false)
        .build()

    private fun fallbackNotification(): Notification = Notification.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_notify_error)
        .setContentTitle("SHIELD TURBO switched to NORMAL")
        .setContentText("Shield reached SEVERE thermal status. TURBO stays off until you turn it on again.")
        .setAutoCancel(true)
        .setShowWhen(false)
        .build()

    private fun notificationManager(): NotificationManager =
        getSystemService(NotificationManager::class.java)

    companion object {
        private const val CHANNEL_ID = "shield_turbo_performance"
        private const val ACTIVE_NOTIFICATION_ID = 4101
        private const val FALLBACK_NOTIFICATION_ID = 4102
        private const val EXTRA_BOOT_REAPPLY = "boot_reapply"

        fun start(context: Context, bootReapply: Boolean = false) {
            val intent = Intent(context, TurboThermalWatchdogService::class.java)
                .putExtra(EXTRA_BOOT_REAPPLY, bootReapply)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TurboThermalWatchdogService::class.java))
        }
    }
}

@TargetApi(Build.VERSION_CODES.Q)
private object ThermalApi29 {
    fun current(power: PowerManager): Int = power.currentThermalStatus

    fun register(
        power: PowerManager,
        executor: Executor,
        onStatus: (Int) -> Unit
    ): () -> Unit {
        val listener = PowerManager.OnThermalStatusChangedListener { status -> onStatus(status) }
        power.addThermalStatusListener(executor, listener)
        return { power.removeThermalStatusListener(listener) }
    }
}
