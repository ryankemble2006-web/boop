package com.boop.shieldturbo.performance

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.concurrent.Executors

class HeadroomActivity : Activity() {
    private val worker = Executors.newSingleThreadExecutor()
    private lateinit var probe: HeadroomProbe

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        probe = HeadroomProbe(applicationContext)
        showPage(
            title = "TURBO+ • READING SHIELD",
            body = "READ ONLY\n\nChecking CPU, GPU, memory, cooling and extra NVIDIA stock controls.",
            titleColour = Color.CYAN
        )
        worker.submit {
            try {
                val result = probe.scan()
                runOnUiThread {
                    if (!isFinishing && !isDestroyed) showResult(result)
                }
            } catch (failure: Exception) {
                runOnUiThread {
                    if (!isFinishing && !isDestroyed) showFailure(failure)
                }
            }
        }
    }

    override fun onDestroy() {
        worker.shutdownNow()
        super.onDestroy()
    }

    private fun showResult(result: HeadroomSnapshot) {
        val body = buildString {
            appendSection("CPU", result.cpu)
            appendSection("GPU", result.gpu)
            appendSection("MEMORY", result.memory)
            appendSection("COOLING", result.cooling)
            appendSection("EXTRA STOCK CONTROLS", result.extraStockControls)
        }.trim()
        showPage("TURBO+ • RESULT", body, Color.CYAN)
    }

    private fun StringBuilder.appendSection(label: String, evidence: String) {
        if (isNotEmpty()) append("\n\n")
        append(label)
        append('\n')
        if (evidence.isBlank()) {
            append("BLOCKED • no readable evidence on this Shield firmware")
        } else {
            append("FOUND • ")
            append(evidence)
        }
    }

    private fun showFailure(failure: Exception) {
        val detail = failure.message?.trim()?.take(300).orEmpty()
        val adbNotReady = detail.contains("ADB TURBO", ignoreCase = true) ||
            detail.contains("authorised", ignoreCase = true) ||
            detail.contains("approval", ignoreCase = true)
        val body = if (adbNotReady) {
            "ADB NOT READY\n\nOpen ADVANCED → ENABLE ADB TURBO once, then run this test again.\n\n${detail.ifBlank { "Trusted local ADB is not available." }}"
        } else {
            "HEADROOM TEST FAILED\n\n${failure.javaClass.simpleName}\n${detail.ifBlank { "The Shield did not return usable evidence." }}"
        }
        showPage("TURBO+ • STOP", body, Color.YELLOW)
    }

    private fun showPage(title: String, body: String, titleColour: Int) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(44), dp(24), dp(44), dp(20))
            setBackgroundColor(Color.BLACK)
        }

        root.addView(TextView(this).apply {
            text = title
            textSize = 32f
            setTextColor(titleColour)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            includeFontPadding = false
        })

        val bodyView = TextView(this).apply {
            text = body
            textSize = 22f
            setTextColor(Color.WHITE)
            typeface = Typeface.MONOSPACE
            setLineSpacing(dp(3).toFloat(), 1.0f)
            includeFontPadding = false
            isFocusable = true
            isFocusableInTouchMode = true
        }
        root.addView(ScrollView(this).apply {
            isFillViewport = true
            isFocusable = false
            addView(bodyView)
        }, LinearLayout.LayoutParams(-1, 0, 1f).apply {
            topMargin = dp(20)
            bottomMargin = dp(12)
        })

        root.addView(TextView(this).apply {
            text = "PHOTOGRAPH THIS • BACK TO CLOSE"
            textSize = 20f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
        })

        setContentView(root)
        bodyView.requestFocus()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density + 0.5f).toInt()
}
