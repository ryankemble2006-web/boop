package com.boop.shieldturbo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import com.boop.shieldturbo.analysis.ShieldAnalyzer
import com.boop.shieldturbo.apps.AppCatalog
import com.boop.shieldturbo.apps.AppRoutes
import com.boop.shieldturbo.model.ProbeResult
import com.boop.shieldturbo.model.ProbeStatus
import com.boop.shieldturbo.picture.DisplayFacts
import com.boop.shieldturbo.privilege.PrivilegeDetector
import com.boop.shieldturbo.privilege.PrivilegeTier
import com.boop.shieldturbo.probe.*
import com.boop.shieldturbo.system.QuickCheck
import com.boop.shieldturbo.system.SystemRoutes
import com.boop.shieldturbo.system.SystemShortcut
import com.boop.shieldturbo.ui.TurboSection
import java.util.concurrent.Executors
import java.util.concurrent.Future

class MainActivity : Activity() {
    private val worker = Executors.newSingleThreadExecutor()
    private val ui = Handler(Looper.getMainLooper())
    private var scan: Future<*>? = null
    private var generation = 0
    private var visible = false
    private var scanning = false
    private var currentSection: TurboSection? = null
    private var details: AlertDialog? = null

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var results: LinearLayout
    private lateinit var status: TextView
    private lateinit var analyseButton: Button
    private lateinit var accessButton: Button
    private lateinit var brightness: SeekBar
    private lateinit var brightnessValue: TextView
    private lateinit var scroll: ScrollView
    private lateinit var quickCheckText: TextView

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(buildUi())
        showHome()
    }

    override fun onStart() {
        super.onStart()
        visible = true
        syncBrightness()
    }

    override fun onStop() {
        visible = false
        cancelScan()
        ui.removeCallbacksAndMessages(null)
        details?.dismiss()
        details = null
        super.onStop()
    }

    override fun onDestroy() {
        worker.shutdownNow()
        ui.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (currentSection != null) {
            cancelScan()
            showHome()
        } else {
            super.onBackPressed()
        }
    }

    private fun cancelScan() {
        generation++
        scan?.cancel(true)
        scan = null
        if (scanning) {
            scanning = false
            if (::status.isInitialized) status.setText(R.string.scan_paused)
            if (::analyseButton.isInitialized) analyseButton.setText(R.string.analyse)
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density + 0.5f).toInt()

    private fun text(value: String, size: Float, colour: Int = Color.WHITE) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(colour)
        setPadding(0, dp(3), 0, dp(3))
    }

    private fun button(title: Int) = Button(this).apply {
        setText(title)
        textSize = 18f
        setTextColor(Color.WHITE)
        isAllCaps = false
        isFocusable = true
        isFocusableInTouchMode = true
        minHeight = dp(56)
        background = getDrawable(R.drawable.focus_panel)
    }

    private fun actionButton(title: String, action: () -> Unit) = Button(this).apply {
        text = title
        textSize = 17f
        setTextColor(Color.WHITE)
        isAllCaps = false
        isFocusable = true
        isFocusableInTouchMode = true
        minHeight = dp(54)
        background = getDrawable(R.drawable.focus_panel)
        setOnClickListener { action() }
    }

    private fun buildUi(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(32), dp(16), dp(32), dp(16))
            setBackgroundColor(Color.BLACK)
        }
        root.addView(text(getString(R.string.app_name), 28f, Color.CYAN).apply {
            setTypeface(typeface, Typeface.BOLD)
        })
        root.addView(text(getString(R.string.control_centre_tagline), 16f, Color.LTGRAY))
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(12) })
        return root
    }

    private fun showHome() {
        currentSection = null
        content.removeAllViews()
        content.addView(
            text(getString(R.string.control_centre_intro), 18f, Color.LTGRAY),
            LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        )

        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val sections = listOf(
            Triple(TurboSection.TURBO, R.string.section_turbo, R.string.section_turbo_help),
            Triple(TurboSection.PICTURE, R.string.section_picture, R.string.section_picture_help),
            Triple(TurboSection.APPS, R.string.section_apps, R.string.section_apps_help),
            Triple(TurboSection.NETWORK, R.string.section_network, R.string.section_network_help),
            Triple(TurboSection.SHIELD, R.string.section_shield, R.string.section_shield_help)
        )
        val cards = sections.map { (section, title, help) ->
            Button(this).apply {
                id = View.generateViewId()
                setText(title)
                contentDescription = "${getString(title)}. ${getString(help)}"
                textSize = 20f
                setTextColor(Color.WHITE)
                isAllCaps = false
                isFocusable = true
                isFocusableInTouchMode = true
                minHeight = dp(112)
                background = getDrawable(R.drawable.focus_panel)
                setOnClickListener { openSection(section) }
            }
        }
        cards.forEachIndexed { index, card ->
            card.nextFocusLeftId = if (index == 0) card.id else cards[index - 1].id
            card.nextFocusRightId = if (index == cards.lastIndex) card.id else cards[index + 1].id
            row.addView(card, LinearLayout.LayoutParams(0, dp(112), 1f).apply {
                if (index > 0) marginStart = dp(10)
            })
        }
        content.addView(row, LinearLayout.LayoutParams(-1, -2))
        content.addView(
            text(getString(R.string.control_centre_footer), 15f, Color.LTGRAY),
            LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(14) }
        )
        cards.first().requestFocus()
    }

    private fun openSection(section: TurboSection) {
        currentSection = section
        content.removeAllViews()
        when (section) {
            TurboSection.TURBO -> renderTurbo()
            TurboSection.PICTURE -> renderPicture()
            TurboSection.APPS -> renderApps()
            TurboSection.NETWORK -> renderNetwork()
            TurboSection.SHIELD -> renderShield()
        }
    }

    private fun sectionHeader(title: Int, help: Int) {
        content.addView(text(getString(title), 25f, Color.CYAN).apply { setTypeface(typeface, Typeface.BOLD) })
        content.addView(
            text(getString(help), 15f, Color.LTGRAY),
            LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) }
        )
    }

    private fun renderTurbo() {
        sectionHeader(R.string.section_turbo, R.string.section_turbo_help)
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        analyseButton = button(R.string.analyse).apply {
            id = R.id.analyse_button
            setOnClickListener { analyse() }
        }
        accessButton = button(R.string.access_details).apply {
            id = R.id.access_button
            setOnClickListener { showAccess() }
        }
        controls.addView(analyseButton, LinearLayout.LayoutParams(0, dp(56), 2f))
        controls.addView(accessButton, LinearLayout.LayoutParams(0, dp(56), 1f).apply { marginStart = dp(12) })
        content.addView(controls, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(4) })

        status = text(getString(R.string.ready), 15f, Color.LTGRAY).apply {
            id = R.id.analysis_status
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        content.addView(status)
        quickCheckText = text(storageQuickCheck(), 15f, Color.LTGRAY)
        content.addView(quickCheckText)

        results = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            id = R.id.analysis_results
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        }
        scroll = ScrollView(this).apply {
            isFillViewport = true
            isFocusable = false
            addView(results)
        }
        content.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val maintenance = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val storage = actionButton("FREE SPACE") { safeStart(SystemRoutes.storage(this@MainActivity)) }
        val apps = actionButton("MANAGE APPS") { safeStart(SystemRoutes.manageApps(this@MainActivity)) }
        val restart = actionButton("RESTART TURBO") { recreate() }
        maintenance.addView(storage, LinearLayout.LayoutParams(0, dp(54), 1f))
        maintenance.addView(apps, LinearLayout.LayoutParams(0, dp(54), 1f).apply { marginStart = dp(10) })
        maintenance.addView(restart, LinearLayout.LayoutParams(0, dp(54), 1f).apply { marginStart = dp(10) })
        content.addView(maintenance, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6) })

        analyseButton.requestFocus()
        analyse()
    }

    private fun storageQuickCheck(): String = try {
        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        val finding = QuickCheck.storage(stat.availableBytes, stat.totalBytes)
        "Quick check: ${finding.message}. No automatic cleanup was run."
    } catch (_: Exception) {
        "Quick check: storage capacity not exposed. No automatic cleanup was run."
    }

    private fun renderApps() {
        sectionHeader(R.string.section_apps, R.string.section_apps_help)
        val apps = AppCatalog.query(this)
        if (apps.isEmpty()) {
            content.addView(text("No launchable TV apps are visible to SHIELD TURBO.", 18f))
            val manage = actionButton("OPEN ANDROID APP SETTINGS") { safeStart(SystemRoutes.manageApps(this)) }
            content.addView(manage, LinearLayout.LayoutParams(-1, dp(56)).apply { topMargin = dp(10) })
            manage.requestFocus()
            return
        }

        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val appButtons = apps.map { app ->
            actionButton(app.label) {
                details?.dismiss()
                details = AlertDialog.Builder(this)
                    .setTitle(app.label)
                    .setMessage(app.packageName)
                    .setItems(arrayOf("LAUNCH", "APP INFO")) { _, which ->
                        if (which == 0) safeStart(AppRoutes.launch(this, app.packageName))
                        else safeStart(AppRoutes.info(app.packageName))
                    }
                    .setNegativeButton(R.string.close, null)
                    .show()
            }.apply {
                id = View.generateViewId()
                contentDescription = "${app.label}. ${app.packageName}"
            }
        }
        appButtons.forEachIndexed { index, appButton ->
            appButton.nextFocusUpId = if (index == 0) appButton.id else appButtons[index - 1].id
            appButton.nextFocusDownId = if (index == appButtons.lastIndex) appButton.id else appButtons[index + 1].id
            list.addView(appButton, LinearLayout.LayoutParams(-1, dp(56)).apply { bottomMargin = dp(6) })
        }
        val appScroll = ScrollView(this).apply { isFillViewport = true; addView(list) }
        content.addView(appScroll, LinearLayout.LayoutParams(-1, 0, 1f))
        content.addView(text("Launch or open Android's own App Info page. TURBO does not force-stop or clear other apps.", 14f, Color.LTGRAY))
        appButtons.first().requestFocus()
    }

    private fun renderNetwork() {
        sectionHeader(R.string.section_network, R.string.section_network_help)
        val reading = NetworkProbe(this).read()
        content.addView(text("${reading.label}: ${reading.value}", 22f, Color.CYAN))
        content.addView(text(reading.evidence, 16f, Color.LTGRAY))
        val state = when (ConnectivityCheck.current(this)) {
            Reachability.INTERNET_REACHABLE -> "Internet reachable: Android has validated this connection."
            Reachability.LOCAL_ONLY -> "Local network connected; internet access is not validated."
            Reachability.OFFLINE -> "No active network connection."
            Reachability.UNKNOWN -> "Connection reachability is not exposed right now."
        }
        content.addView(text(state, 20f), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(14) })
        content.addView(text("This is a connectivity check, not a speed score.", 14f, Color.LTGRAY))

        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val check = actionButton("CHECK AGAIN") { renderNetworkFresh() }
        val settings = actionButton("NETWORK SETTINGS") { safeStart(SystemRoutes.resolve(this, SystemShortcut.NETWORK)) }
        row.addView(check, LinearLayout.LayoutParams(0, dp(56), 1f))
        row.addView(settings, LinearLayout.LayoutParams(0, dp(56), 1f).apply { marginStart = dp(10) })
        content.addView(row, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(16) })
        check.requestFocus()
    }

    private fun renderNetworkFresh() {
        if (currentSection != TurboSection.NETWORK) return
        content.removeAllViews()
        renderNetwork()
    }

    private fun renderShield() {
        sectionHeader(R.string.section_shield, R.string.section_shield_help)
        val uptimeMinutes = SystemClock.elapsedRealtime() / 60000L
        content.addView(text("${Build.MANUFACTURER} ${Build.MODEL}", 22f, Color.CYAN))
        content.addView(text("${Build.DEVICE} • Android ${Build.VERSION.RELEASE} • API ${Build.VERSION.SDK_INT}", 16f))
        content.addView(text("Build: ${Build.DISPLAY}", 14f, Color.LTGRAY))
        content.addView(text("Uptime: ${uptimeMinutes / 60}h ${uptimeMinutes % 60}m", 14f, Color.LTGRAY))
        content.addView(text("Useful Shield settings", 18f, Color.CYAN), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })

        val shortcuts = listOf(
            "DISPLAY + SOUND" to SystemShortcut.DISPLAY_SOUND,
            "APPS" to SystemShortcut.APPS,
            "STORAGE" to SystemShortcut.STORAGE,
            "NETWORK" to SystemShortcut.NETWORK,
            "ACCESSIBILITY" to SystemShortcut.ACCESSIBILITY,
            "DEVELOPER OPTIONS" to SystemShortcut.DEVELOPER,
            "ABOUT" to SystemShortcut.ABOUT
        )
        val grid = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val shortcutButtons = shortcuts.map { (label, shortcut) ->
            actionButton(label) { safeStart(SystemRoutes.resolve(this, shortcut)) }.apply { id = View.generateViewId() }
        }
        shortcutButtons.forEachIndexed { index, shortcutButton ->
            shortcutButton.nextFocusUpId = if (index == 0) shortcutButton.id else shortcutButtons[index - 1].id
            shortcutButton.nextFocusDownId = if (index == shortcutButtons.lastIndex) shortcutButton.id else shortcutButtons[index + 1].id
            grid.addView(shortcutButton, LinearLayout.LayoutParams(-1, dp(54)).apply { bottomMargin = dp(5) })
        }
        val shortcutScroll = ScrollView(this).apply { isFillViewport = true; addView(grid) }
        content.addView(shortcutScroll, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(8) })
        content.addView(text("Sleep and reboot stay out of STANDARD mode until a safe, proven route exists.", 14f, Color.LTGRAY))
        shortcutButtons.first().requestFocus()
    }

    private fun renderPicture() {
        sectionHeader(R.string.section_picture, R.string.section_picture_help)
        val brightTop = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        brightTop.addView(text(getString(R.string.brightness_title), 18f, Color.CYAN).apply {
            setTypeface(typeface, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        brightnessValue = text("100%", 18f, Color.WHITE).apply { setTypeface(typeface, Typeface.BOLD) }
        brightTop.addView(brightnessValue)
        content.addView(brightTop, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        brightness = SeekBar(this).apply {
            id = R.id.brightness_seek
            max = 90
            isFocusable = true
            isFocusableInTouchMode = true
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val percent = progress + Brightness.MIN_PERCENT
                    brightnessValue.text = "$percent%"
                    if (fromUser) applyBrightness(percent)
                }
                override fun onStartTrackingTouch(bar: SeekBar?) = Unit
                override fun onStopTrackingTouch(bar: SeekBar?) = Unit
            })
        }
        content.addView(brightness, LinearLayout.LayoutParams(-1, dp(48)))
        content.addView(text(getString(R.string.brightness_help), 14f, Color.LTGRAY))

        val facts = DisplayFacts.current(this)
        content.addView(text("ACTIVE DISPLAY", 18f, Color.CYAN), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(16) })
        content.addView(text(facts.modeText(), 22f))
        content.addView(text("HDR exposed by Android: ${facts.hdrText()}", 16f, Color.LTGRAY))
        content.addView(text("Read-only. STANDARD mode does not change HDR, refresh rate or HDMI modes.", 14f, Color.LTGRAY))
        syncBrightness()
        brightness.requestFocus()
    }

    private fun syncBrightness() {
        if (!::brightness.isInitialized) return
        val percent = BrightnessService.savedPercent(this)
        brightness.progress = percent - Brightness.MIN_PERCENT
        brightnessValue.text = "$percent%"
    }

    private fun applyBrightness(percent: Int) {
        val safe = Brightness.clampPercent(percent)
        if (safe < 100 && !Settings.canDrawOverlays(this)) {
            brightness.progress = 90
            brightnessValue.text = "100%"
            details?.dismiss()
            details = AlertDialog.Builder(this)
                .setTitle(R.string.brightness_title)
                .setMessage(R.string.overlay_needed)
                .setNegativeButton(R.string.close, null)
                .setPositiveButton("ALLOW") { _, _ ->
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                }.show()
            return
        }
        startService(
            Intent(this, BrightnessService::class.java)
                .setAction(BrightnessService.ACTION_APPLY)
                .putExtra(BrightnessService.EXTRA_PERCENT, safe)
        )
    }

    private fun analyse() {
        if (scanning || !visible || currentSection != TurboSection.TURBO) return
        scanning = true
        val token = ++generation
        status.setText(R.string.scanning)
        analyseButton.setText(R.string.scanning)
        quickCheckText.text = storageQuickCheck()
        scan = worker.submit {
            try {
                val context = applicationContext
                val tier = PrivilegeDetector.android(context).detect()
                val snapshot = ShieldAnalyzer(
                    listOf(DeviceProbe(), MemoryProbe(context), StorageProbe(), CpuProbe(), ThermalProbe(), NetworkProbe(context))
                ).analyze()
                val privilege = ProbeResult(
                    "privilege",
                    getString(R.string.capability_tier),
                    ProbeStatus.AVAILABLE,
                    tier.name.replace('_', ' '),
                    when (tier) {
                        PrivilegeTier.STANDARD -> getString(R.string.standard_evidence)
                        PrivilegeTier.ADB_TURBO -> getString(R.string.adb_evidence)
                        PrivilegeTier.ROOT -> getString(R.string.root_evidence)
                    }
                )
                ui.post {
                    if (visible && generation == token && !isDestroyed && currentSection == TurboSection.TURBO) {
                        render(snapshot.results + privilege)
                        scanning = false
                        scan = null
                        analyseButton.setText(R.string.analyse)
                    }
                }
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (_: Exception) {
                ui.post {
                    if (visible && generation == token && !isDestroyed && currentSection == TurboSection.TURBO) {
                        scanning = false
                        scan = null
                        analyseButton.setText(R.string.analyse)
                        status.setText(R.string.scan_unavailable)
                    }
                }
            }
        }
    }

    private fun render(readings: List<ProbeResult>) {
        results.removeAllViews()
        val cards = readings.map { reading ->
            LinearLayout(this).apply {
                id = View.generateViewId()
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(10), dp(16), dp(10))
                background = getDrawable(R.drawable.focus_panel)
                isFocusable = true
                isFocusableInTouchMode = true
                isClickable = true
                contentDescription = "Reading: ${reading.label}. ${reading.value}. ${statusLabel(reading.status)}"
                addView(text("${reading.label}  •  ${statusLabel(reading.status)}", 16f, Color.CYAN))
                addView(text(reading.value, 24f))
                addView(text(reading.evidence, 14f, Color.LTGRAY))
                setOnClickListener {
                    details?.dismiss()
                    details = AlertDialog.Builder(this@MainActivity)
                        .setTitle(reading.label)
                        .setMessage("${reading.value}\n\n${reading.evidence}\n\n${getString(R.string.reading_note)}")
                        .setPositiveButton(R.string.close, null)
                        .show()
                }
            }
        }
        cards.forEachIndexed { index, card ->
            card.nextFocusUpId = if (index == 0) analyseButton.id else cards[index - 1].id
            card.nextFocusDownId = if (index == cards.lastIndex) card.id else cards[index + 1].id
            results.addView(card, LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(4), 0, dp(4)) })
        }
        if (cards.isNotEmpty()) {
            analyseButton.nextFocusDownId = cards.first().id
            accessButton.nextFocusDownId = cards.first().id
        }
        results.addView(text(getString(R.string.no_changes), 14f, Color.LTGRAY))
        val available = readings.count { it.status == ProbeStatus.AVAILABLE }
        status.text = getString(R.string.scan_complete, available, readings.size)
        scroll.scrollTo(0, 0)
    }

    private fun statusLabel(value: ProbeStatus) = getString(
        when (value) {
            ProbeStatus.AVAILABLE -> R.string.available
            ProbeStatus.RESTRICTED -> R.string.restricted
            ProbeStatus.UNSUPPORTED -> R.string.not_exposed
            ProbeStatus.ERROR -> R.string.unavailable
        }
    )

    private fun safeStart(intent: Intent?) {
        if (intent == null) {
            showUnavailable()
            return
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            showUnavailable()
        }
    }

    private fun showUnavailable() {
        details?.dismiss()
        details = AlertDialog.Builder(this)
            .setTitle("NOT AVAILABLE")
            .setMessage("This Shield firmware does not expose that shortcut to a normal app.")
            .setPositiveButton(R.string.close, null)
            .show()
    }

    private fun showAccess() {
        details?.dismiss()
        details = AlertDialog.Builder(this)
            .setTitle(R.string.access_details)
            .setMessage(R.string.access_explanation)
            .setPositiveButton(R.string.close, null)
            .show()
    }
}
