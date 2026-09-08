package com.boop.shieldturbo.performance

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.boop.shieldturbo.R
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class TurboModePanel(private val activity: Activity) {
    private val runtime = TurboRuntime(activity.applicationContext)
    private val worker: ExecutorService = Executors.newSingleThreadExecutor()
    private val ui = Handler(Looper.getMainLooper())
    private var operation: Future<*>? = null
    private val prefs = activity.getSharedPreferences("turbo_mode_ui", Activity.MODE_PRIVATE)

    val root = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14), dp(10), dp(14), dp(12))
        setBackgroundColor(Color.rgb(10, 10, 10))
    }

    val modeButton = Button(activity).apply {
        id = View.generateViewId()
        isAllCaps = false
        isFocusable = true
        isFocusableInTouchMode = true
        minHeight = dp(74)
        textSize = 24f
        setTextColor(Color.WHITE)
        background = activity.getDrawable(R.drawable.focus_panel)
        setOnClickListener { toggle() }
    }

    private val processorMode = readout("Processor mode:")
    private val thermalState = readout("Thermal state:")
    private val watchdogState = readout("Watchdog:")
    private val lastChange = readout("Last change:")

    init {
        root.addView(modeButton, LinearLayout.LayoutParams(-1, dp(74)))
        root.addView(processorMode)
        root.addView(thermalState)
        root.addView(watchdogState)
        root.addView(lastChange)
        render(runtime.snapshot())
    }

    fun close() {
        operation?.cancel(true)
        operation = null
        worker.shutdownNow()
        ui.removeCallbacksAndMessages(null)
    }

    private fun toggle() {
        if (operation?.isDone == false) return
        val snapshot = runtime.snapshot()
        if (snapshot.phase == TurboPhase.TURBO_VERIFIED && snapshot.desiredTurbo) {
            runDisable()
            return
        }

        if (!prefs.getBoolean(FIRST_ENABLE_ACK, false)) {
            AlertDialog.Builder(activity)
                .setTitle(R.string.turbo_mode_confirm_title)
                .setMessage(R.string.turbo_mode_confirm_message)
                .setNegativeButton(R.string.close, null)
                .setPositiveButton(R.string.turbo_mode_enable) { _, _ ->
                    prefs.edit().putBoolean(FIRST_ENABLE_ACK, true).apply()
                    runEnable()
                }
                .show()
        } else {
            runEnable()
        }
    }

    private fun runEnable() {
        setBusy(true)
        operation = worker.submit {
            var outcome = runtime.enable()
            if (outcome.success && outcome.snapshot.phase == TurboPhase.TURBO_VERIFIED) {
                try {
                    TurboThermalWatchdogService.start(activity.applicationContext)
                } catch (_: Exception) {
                    outcome = runtime.disable("TURBO off: thermal watchdog could not start")
                }
            }
            postOutcome(outcome)
        }
    }

    private fun runDisable() {
        setBusy(true)
        operation = worker.submit {
            val outcome = runtime.disable("Manual NORMAL")
            if (outcome.success && outcome.snapshot.phase == TurboPhase.NORMAL) {
                TurboThermalWatchdogService.stop(activity.applicationContext)
            }
            postOutcome(outcome)
        }
    }

    private fun postOutcome(outcome: TurboOutcome) {
        ui.post {
            operation = null
            render(outcome.snapshot)
            modeButton.isEnabled = true
            if (!outcome.success) {
                AlertDialog.Builder(activity)
                    .setTitle(R.string.turbo_mode_result_title)
                    .setMessage(outcome.message)
                    .setPositiveButton(R.string.close, null)
                    .show()
            }
        }
    }

    private fun setBusy(busy: Boolean) {
        modeButton.isEnabled = !busy
        if (busy) modeButton.setText(R.string.turbo_mode_working)
    }

    private fun render(snapshot: TurboSnapshot) {
        val active = snapshot.phase == TurboPhase.TURBO_VERIFIED && snapshot.desiredTurbo
        modeButton.setText(if (active) R.string.turbo_mode_on else R.string.turbo_mode_off)
        modeButton.contentDescription = if (active) {
            "TURBO mode on. Press to restore the original stock processor mode."
        } else {
            "TURBO mode off. Press to enable the proven stock Max performance mode."
        }
        processorMode.text = "Processor mode: ${processorLabel(snapshot)}"
        thermalState.text = "Thermal state: ${thermalLabel(snapshot.lastThermalStatus)}"
        watchdogState.text = "Watchdog: ${if (active) "ON" else "OFF"}"
        lastChange.text = "Last change: ${lastChangeLabel(snapshot)}"
    }

    private fun processorLabel(snapshot: TurboSnapshot): String = when (snapshot.phase) {
        TurboPhase.TURBO_VERIFIED -> "Max performance verified"
        TurboPhase.NORMAL -> "Original stock setting"
        TurboPhase.ENABLING -> "Switching to Max performance"
        TurboPhase.RESTORING -> "Restoring original stock setting"
        TurboPhase.RECOVERY_REQUIRED -> "CHECK SHIELD SETTINGS"
    }

    private fun thermalLabel(status: Int?): String = when (status) {
        null -> "Not read yet"
        0 -> "NONE"
        1 -> "LIGHT"
        2 -> "MODERATE"
        3 -> "SEVERE"
        4 -> "CRITICAL"
        5 -> "EMERGENCY"
        6 -> "SHUTDOWN"
        else -> "Android status $status"
    }

    private fun lastChangeLabel(snapshot: TurboSnapshot): String {
        if (snapshot.lastChangeEpochMs <= 0L) return snapshot.lastReason
        val whenText = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(snapshot.lastChangeEpochMs))
        return "$whenText • ${snapshot.lastReason}"
    }

    private fun readout(prefix: String) = TextView(activity).apply {
        text = prefix
        textSize = 15f
        setTextColor(Color.LTGRAY)
        typeface = Typeface.DEFAULT
        setPadding(0, dp(5), 0, 0)
        isFocusable = false
    }

    private fun dp(value: Int) = (value * activity.resources.displayMetrics.density + 0.5f).toInt()

    private companion object {
        const val FIRST_ENABLE_ACK = "first_enable_acknowledged"
    }
}
