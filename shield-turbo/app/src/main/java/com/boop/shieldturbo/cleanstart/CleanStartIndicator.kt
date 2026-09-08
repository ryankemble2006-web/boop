package com.boop.shieldturbo.cleanstart

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Static, non-interactive notice shown only while automatic CLEAN START is running. */
class CleanStartIndicator(context: Context) {
    private val context = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private val presented = CountDownLatch(1)
    @Volatile private var view: View? = null
    private var windowManager: WindowManager? = null

    private fun dp(value: Int): Int =
        (value * context.resources.displayMetrics.density + 0.5f).toInt()

    fun show() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            main.post { show() }
            return
        }
        if (view != null) return
        if (!Settings.canDrawOverlays(context)) {
            presented.countDown()
            return
        }
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        if (manager == null) {
            presented.countDown()
            return
        }

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(12), dp(24), dp(12))
            background = GradientDrawable().apply {
                setColor(0xEE000000.toInt())
                cornerRadius = dp(14).toFloat()
                setStroke(dp(1), Color.CYAN)
            }
            addView(TextView(context).apply {
                text = "SHIELD TURBO · CLEAN START"
                textSize = 18f
                setTextColor(Color.CYAN)
                setTypeface(typeface, Typeface.BOLD)
                gravity = Gravity.CENTER
            })
            addView(TextView(context).apply {
                text = "Tidying startup apps"
                textSize = 14f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
            })
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dp(28)
            windowAnimations = 0
        }

        val added = runCatching { manager.addView(card, params) }.isSuccess
        if (!added) {
            presented.countDown()
            return
        }

        windowManager = manager
        view = card
        armPresentationSignal(card)
    }

    private fun armPresentationSignal(card: View) {
        val observer = card.viewTreeObserver
        if (!observer.isAlive) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && card.isHardwareAccelerated) {
            runCatching {
                observer.registerFrameCommitCallback { presented.countDown() }
            }
        } else {
            runCatching {
                observer.addOnDrawListener { presented.countDown() }
            }
        }
        card.invalidate()
    }

    fun awaitPresented(timeoutMs: Long): Boolean =
        presented.await(timeoutMs, TimeUnit.MILLISECONDS)

    fun hide() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            main.post { hide() }
            return
        }
        val current = view ?: return
        runCatching { windowManager?.removeView(current) }
        view = null
        windowManager = null
    }
}
