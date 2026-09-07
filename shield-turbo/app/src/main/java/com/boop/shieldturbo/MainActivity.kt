package com.boop.shieldturbo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import com.boop.shieldturbo.analysis.ShieldAnalyzer
import com.boop.shieldturbo.model.ProbeResult
import com.boop.shieldturbo.model.ProbeStatus
import com.boop.shieldturbo.privilege.PrivilegeDetector
import com.boop.shieldturbo.privilege.PrivilegeTier
import com.boop.shieldturbo.probe.*
import java.util.concurrent.Executors
import java.util.concurrent.Future

class MainActivity : Activity() {
    private val worker = Executors.newSingleThreadExecutor()
    private val ui = Handler(Looper.getMainLooper())
    private var scan: Future<*>? = null
    private var generation = 0
    private var visible = false
    private var scanning = false
    private var details: AlertDialog? = null
    private lateinit var results: LinearLayout
    private lateinit var status: TextView
    private lateinit var analyseButton: Button
    private lateinit var accessButton: Button
    private lateinit var brightness: SeekBar
    private lateinit var brightnessValue: TextView
    private lateinit var scroll: ScrollView

    override fun onCreate(state: Bundle?) { super.onCreate(state); setContentView(buildUi()) }
    override fun onStart() { super.onStart(); visible = true; syncBrightness() }
    override fun onStop() {
        visible = false; generation++; scan?.cancel(true); scan = null; ui.removeCallbacksAndMessages(null)
        if (scanning) { scanning = false; status.setText(R.string.scan_paused); analyseButton.setText(R.string.analyse) }
        details?.dismiss(); details = null; super.onStop()
    }
    override fun onDestroy() { worker.shutdownNow(); ui.removeCallbacksAndMessages(null); super.onDestroy() }

    private fun dp(value: Int) = (value * resources.displayMetrics.density + 0.5f).toInt()
    private fun text(value: String, size: Float, colour: Int = Color.WHITE) = TextView(this).apply {
        text = value; textSize = size; setTextColor(colour); setPadding(0, dp(3), 0, dp(3))
    }
    private fun button(title: Int) = Button(this).apply {
        setText(title); textSize = 18f; setTextColor(Color.WHITE); isAllCaps = false
        isFocusable = true; isFocusableInTouchMode = true; minHeight = dp(56); background = getDrawable(R.drawable.focus_panel)
    }

    private fun buildUi(): View {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(32), dp(16), dp(32), dp(16)); setBackgroundColor(Color.BLACK) }
        root.addView(text(getString(R.string.app_name), 28f, Color.CYAN).apply { setTypeface(typeface, Typeface.BOLD) })
        root.addView(text(getString(R.string.tagline), 16f, Color.LTGRAY))
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        analyseButton = button(R.string.analyse).apply { id = R.id.analyse_button; setOnClickListener { analyse() } }
        accessButton = button(R.string.access_details).apply { id = R.id.access_button; setOnClickListener { showAccess() } }
        controls.addView(analyseButton, LinearLayout.LayoutParams(0, dp(56), 2f))
        controls.addView(accessButton, LinearLayout.LayoutParams(0, dp(56), 1f).apply { marginStart = dp(12) })
        root.addView(controls, LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(8), 0, dp(4)) })

        val brightTop = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        brightTop.addView(text(getString(R.string.brightness_title), 18f, Color.CYAN).apply { setTypeface(typeface, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        brightnessValue = text("100%", 18f, Color.WHITE).apply { setTypeface(typeface, Typeface.BOLD) }
        brightTop.addView(brightnessValue)
        root.addView(brightTop, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        brightness = SeekBar(this).apply {
            id = R.id.brightness_seek; max = 90; isFocusable = true; isFocusableInTouchMode = true
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val percent = progress + Brightness.MIN_PERCENT; brightnessValue.text = "$percent%"
                    if (fromUser) applyBrightness(percent)
                }
                override fun onStartTrackingTouch(bar: SeekBar?) = Unit
                override fun onStopTrackingTouch(bar: SeekBar?) = Unit
            })
        }
        root.addView(brightness, LinearLayout.LayoutParams(-1, dp(48)))
        root.addView(text(getString(R.string.brightness_help), 14f, Color.LTGRAY))

        status = text(getString(R.string.ready), 15f, Color.LTGRAY).apply { id = R.id.analysis_status; accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE }
        root.addView(status)
        results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; id = R.id.analysis_results; descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS }
        scroll = ScrollView(this).apply { isFillViewport = true; isFocusable = false; addView(results) }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        analyseButton.nextFocusDownId = brightness.id; accessButton.nextFocusDownId = brightness.id
        brightness.nextFocusUpId = analyseButton.id
        analyseButton.requestFocus(); syncBrightness(); return root
    }

    private fun syncBrightness() {
        if (!::brightness.isInitialized) return
        val percent = BrightnessService.savedPercent(this)
        brightness.progress = percent - Brightness.MIN_PERCENT; brightnessValue.text = "$percent%"
    }

    private fun applyBrightness(percent: Int) {
        val safe = Brightness.clampPercent(percent)
        if (safe < 100 && !Settings.canDrawOverlays(this)) {
            brightness.progress = 90; brightnessValue.text = "100%"
            details?.dismiss()
            details = AlertDialog.Builder(this).setTitle(R.string.brightness_title).setMessage(R.string.overlay_needed)
                .setNegativeButton(R.string.close, null)
                .setPositiveButton("ALLOW") { _, _ -> startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) }.show()
            return
        }
        startService(Intent(this, BrightnessService::class.java).setAction(BrightnessService.ACTION_APPLY).putExtra(BrightnessService.EXTRA_PERCENT, safe))
    }

    private fun analyse() {
        if (scanning || !visible) return
        scanning = true; val token = ++generation; status.setText(R.string.scanning); analyseButton.setText(R.string.scanning)
        scan = worker.submit {
            try {
                val context = applicationContext; val tier = PrivilegeDetector.android(context).detect()
                val snapshot = ShieldAnalyzer(listOf(DeviceProbe(), MemoryProbe(context), StorageProbe(), CpuProbe(), ThermalProbe(), NetworkProbe(context))).analyze()
                val privilege = ProbeResult("privilege", getString(R.string.capability_tier), ProbeStatus.AVAILABLE, tier.name.replace('_', ' '), when (tier) {
                    PrivilegeTier.STANDARD -> getString(R.string.standard_evidence); PrivilegeTier.ADB_TURBO -> getString(R.string.adb_evidence); PrivilegeTier.ROOT -> getString(R.string.root_evidence) })
                ui.post { if (visible && generation == token && !isDestroyed) { render(snapshot.results + privilege); scanning = false; scan = null; analyseButton.setText(R.string.analyse) } }
            } catch (_: InterruptedException) { Thread.currentThread().interrupt() }
            catch (_: Exception) { ui.post { if (visible && generation == token && !isDestroyed) { scanning = false; scan = null; analyseButton.setText(R.string.analyse); status.setText(R.string.scan_unavailable) } } }
        }
    }

    private fun render(readings: List<ProbeResult>) {
        results.removeAllViews()
        val cards = readings.map { reading -> LinearLayout(this).apply {
            id = View.generateViewId(); orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(10), dp(16), dp(10)); background = getDrawable(R.drawable.focus_panel)
            isFocusable = true; isFocusableInTouchMode = true; isClickable = true; contentDescription = "Reading: ${reading.label}. ${reading.value}. ${statusLabel(reading.status)}"
            addView(text("${reading.label}  •  ${statusLabel(reading.status)}", 16f, Color.CYAN)); addView(text(reading.value, 24f)); addView(text(reading.evidence, 14f, Color.LTGRAY))
            setOnClickListener { details?.dismiss(); details = AlertDialog.Builder(this@MainActivity).setTitle(reading.label).setMessage("${reading.value}\n\n${reading.evidence}\n\n${getString(R.string.reading_note)}").setPositiveButton(R.string.close, null).show() }
        } }
        cards.forEachIndexed { index, card -> card.nextFocusUpId = if (index == 0) brightness.id else cards[index - 1].id; card.nextFocusDownId = if (index == cards.lastIndex) card.id else cards[index + 1].id; results.addView(card, LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(4), 0, dp(4)) }) }
        if (cards.isNotEmpty()) brightness.nextFocusDownId = cards.first().id
        results.addView(text(getString(R.string.no_changes), 14f, Color.LTGRAY)); val available = readings.count { it.status == ProbeStatus.AVAILABLE }
        status.text = getString(R.string.scan_complete, available, readings.size); scroll.scrollTo(0, 0)
    }

    private fun statusLabel(value: ProbeStatus) = getString(when (value) { ProbeStatus.AVAILABLE -> R.string.available; ProbeStatus.RESTRICTED -> R.string.restricted; ProbeStatus.UNSUPPORTED -> R.string.not_exposed; ProbeStatus.ERROR -> R.string.unavailable })
    private fun showAccess() { details?.dismiss(); details = AlertDialog.Builder(this).setTitle(R.string.access_details).setMessage(R.string.access_explanation).setPositiveButton(R.string.close, null).show() }
}
