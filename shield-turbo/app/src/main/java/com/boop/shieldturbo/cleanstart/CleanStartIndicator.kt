package com.boop.shieldturbo.cleanstart

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.Display
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Static, non-interactive notice shown only while automatic CLEAN START is running. */
class CleanStartIndicator(context: Context) {
    private val context = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private val presented = CountDownLatch(1)
    private val startedElapsed = SystemClock.elapsedRealtime()
    @Volatile private var view: View? = null
    private var windowManager: WindowManager? = null
    @Volatile private var overlayAllowed = false
    @Volatile private var windowMode = CleanStartIndicatorWindowMode.NOT_ATTEMPTED
    @Volatile private var addStatus = CleanStartIndicatorAddStatus.NOT_ATTEMPTED
    @Volatile private var presentationStatus = CleanStartIndicatorPresentationStatus.NOT_ATTEMPTED
    @Volatile private var diagnosticDetail = ""

    private fun dp(windowContext: Context, value: Int): Int =
        (value * windowContext.resources.displayMetrics.density + 0.5f).toInt()

    private fun noteDetail(value: String) {
        if (diagnosticDetail.isBlank()) diagnosticDetail = value.take(160)
    }

    private fun overlayWindowContext(): Context {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            windowMode = CleanStartIndicatorWindowMode.APPLICATION_CONTEXT
            return context
        }
        val displayManager = context.getSystemService(DisplayManager::class.java)
        if (displayManager == null) {
            windowMode = CleanStartIndicatorWindowMode.FALLBACK_APPLICATION_CONTEXT
            noteDetail("DisplayManager unavailable")
            return context
        }
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        if (display == null) {
            windowMode = CleanStartIndicatorWindowMode.FALLBACK_APPLICATION_CONTEXT
            noteDetail("Default display unavailable")
            return context
        }
        return runCatching {
            context.createDisplayContext(display)
                .createWindowContext(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, null)
        }.fold(
            onSuccess = {
                windowMode = CleanStartIndicatorWindowMode.DISPLAY_WINDOW_CONTEXT
                it
            },
            onFailure = { failure ->
                windowMode = CleanStartIndicatorWindowMode.FALLBACK_APPLICATION_CONTEXT
                noteDetail("Window context ${failure.javaClass.simpleName}")
                context
            }
        )
    }

    fun show() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            main.post { show() }
            return
        }
        if (view != null) return
        overlayAllowed = Settings.canDrawOverlays(context)
        if (!overlayAllowed) {
            presentationStatus = CleanStartIndicatorPresentationStatus.BYPASSED
            noteDetail("Overlay permission not granted")
            presented.countDown()
            return
        }

        val windowContext = overlayWindowContext()
        val manager = windowContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        if (manager == null) {
            addStatus = CleanStartIndicatorAddStatus.FAILED
            presentationStatus = CleanStartIndicatorPresentationStatus.BYPASSED
            noteDetail("WindowManager unavailable")
            presented.countDown()
            return
        }

        val card = LinearLayout(windowContext).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                dp(windowContext, 24),
                dp(windowContext, 12),
                dp(windowContext, 24),
                dp(windowContext, 12)
            )
            background = GradientDrawable().apply {
                setColor(0xEE000000.toInt())
                cornerRadius = dp(windowContext, 14).toFloat()
                setStroke(dp(windowContext, 1), Color.CYAN)
            }
            addView(TextView(windowContext).apply {
                text = "SHIELD TURBO · CLEAN START"
                textSize = 18f
                setTextColor(Color.CYAN)
                setTypeface(typeface, Typeface.BOLD)
                gravity = Gravity.CENTER
            })
            addView(TextView(windowContext).apply {
                text = "Tidying startup apps"
                textSize = 14f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
            })
        }

        // Mirror the full-screen overlay surface shape already proven by BrightnessService,
        // while keeping the CLEAN START card itself small, static and top-centred.
        val host = FrameLayout(windowContext).apply {
            setBackgroundColor(Color.TRANSPARENT)
        }
        host.addView(
            card,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.CENTER_HORIZONTAL
            ).apply {
                topMargin = dp(windowContext, 28)
            }
        )

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            windowAnimations = 0
        }

        val addResult = runCatching { manager.addView(host, params) }
        if (addResult.isFailure) {
            addStatus = CleanStartIndicatorAddStatus.FAILED
            presentationStatus = CleanStartIndicatorPresentationStatus.BYPASSED
            val failure = addResult.exceptionOrNull()
            noteDetail("addView ${failure?.javaClass?.simpleName ?: "failed"}")
            presented.countDown()
            return
        }

        addStatus = CleanStartIndicatorAddStatus.ADDED
        windowManager = manager
        view = host
        armPresentationSignal(host)
    }

    private fun armPresentationSignal(card: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (card.isAttachedToWindow) {
                armAttachedPresentationSignal(card)
                return
            }
            card.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    v.removeOnAttachStateChangeListener(this)
                    armAttachedPresentationSignal(v)
                }

                override fun onViewDetachedFromWindow(v: View) = Unit
            })
            return
        }
        armAttachedPresentationSignal(card)
    }

    private fun armAttachedPresentationSignal(card: View) {
        val observer = card.viewTreeObserver
        if (!observer.isAlive) {
            noteDetail("ViewTreeObserver not alive")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!card.isHardwareAccelerated) {
                noteDetail("Attached overlay not hardware accelerated")
                return
            }
            runCatching {
                observer.registerFrameCommitCallback {
                    presentationStatus = CleanStartIndicatorPresentationStatus.FRAME_COMMITTED
                    presented.countDown()
                }
            }.onFailure { failure ->
                noteDetail("frame callback ${failure.javaClass.simpleName}")
            }
        } else {
            runCatching {
                observer.addOnDrawListener {
                    presentationStatus = CleanStartIndicatorPresentationStatus.DRAWN
                    presented.countDown()
                }
            }.onFailure { failure ->
                noteDetail("draw listener ${failure.javaClass.simpleName}")
            }
        }
        card.invalidate()
    }

    fun awaitPresented(timeoutMs: Long): Boolean {
        val result = presented.await(timeoutMs, TimeUnit.MILLISECONDS)
        if (!result && presentationStatus == CleanStartIndicatorPresentationStatus.NOT_ATTEMPTED) {
            presentationStatus = CleanStartIndicatorPresentationStatus.TIMEOUT
        }
        return result
    }

    fun diagnostic(presented: Boolean): CleanStartIndicatorDiagnostic {
        if (!presented && presentationStatus == CleanStartIndicatorPresentationStatus.NOT_ATTEMPTED) {
            presentationStatus = CleanStartIndicatorPresentationStatus.TIMEOUT
        }
        return CleanStartIndicatorDiagnostic(
            timestampMillis = System.currentTimeMillis(),
            overlayAllowed = overlayAllowed,
            windowMode = windowMode,
            addStatus = addStatus,
            presentationStatus = presentationStatus,
            elapsedMs = (SystemClock.elapsedRealtime() - startedElapsed).coerceAtLeast(0L),
            detail = diagnosticDetail
        )
    }

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
