package com.boop.shieldturbo

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class BrightnessService : Service() {
    companion object {
        const val ACTION_APPLY = "com.boop.shieldturbo.APPLY_BRIGHTNESS"
        const val EXTRA_PERCENT = "percent"
        private const val PREFS = "shield_turbo"
        private const val KEY = "brightness_percent"

        fun savedPercent(service: android.content.Context): Int =
            Brightness.clampPercent(service.getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY, 100))
    }

    private var shade: View? = null
    private var windowManager: WindowManager? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val percent = Brightness.clampPercent(intent?.getIntExtra(EXTRA_PERCENT, savedPercent(this)) ?: savedPercent(this))
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(KEY, percent).apply()
        apply(percent)
        return START_STICKY
    }

    private fun apply(percent: Int) {
        removeShade()
        if (percent >= 100 || !Settings.canDrawOverlays(this)) {
            if (percent >= 100) stopSelf()
            return
        }
        val alpha = Brightness.dimAlpha(percent)
        val view = View(this).apply { setBackgroundColor(Color.BLACK) }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.alpha = alpha
        }
        windowManager?.addView(view, params)
        shade = view
    }

    private fun removeShade() {
        shade?.let { runCatching { windowManager?.removeView(it) } }
        shade = null
    }

    override fun onDestroy() {
        removeShade()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
