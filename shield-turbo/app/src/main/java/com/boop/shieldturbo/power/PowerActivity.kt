package com.boop.shieldturbo.power

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.boop.shieldturbo.R
import com.boop.shieldturbo.apps.AppCatalog
import com.boop.shieldturbo.apps.AppRoutes
import com.boop.shieldturbo.startup.StartupManagerActivity
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/** User-triggered tools only. No service, boot receiver, server, or continuous monitoring. */
class PowerActivity : Activity() {
    private val worker = Executors.newSingleThreadExecutor()
    private val timer = Executors.newSingleThreadScheduledExecutor()
    private val ui = Handler(Looper.getMainLooper())
    private lateinit var bridge: LocalBridge
    private lateinit var list: LinearLayout
    private lateinit var status: TextView
    private var task: Future<*>? = null
    private var busy = false
    private var generation = 0
    private var setupOnReturn = false
    private var setupLeft = false
    private var page = ""
    private var message = "Connect locally to this Shield. Nothing runs automatically."
    private var dialog: AlertDialog? = null
    private val prefs by lazy { getSharedPreferences("turbo_power", MODE_PRIVATE) }
    private fun dp(n: Int) = (n * resources.displayMetrics.density + 0.5f).toInt()

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        bridge = LocalBridge(applicationContext)
        page = intent.getStringExtra("settings_page").orEmpty().takeIf { it in setOf("display", "accessibility") }.orEmpty()
        render()
    }
    override fun onPause() {
        if (setupOnReturn) setupLeft = true
        super.onPause()
    }
    override fun onResume() {
        super.onResume()
        if (setupOnReturn && setupLeft) {
            setupOnReturn = false; setupLeft = false; enable()
        } else if (!busy) confirmPendingRoute()
    }
    override fun onDestroy() {
        generation++; bridge.cancel(); task?.cancel(true)
        worker.shutdownNow(); timer.shutdownNow(); ui.removeCallbacksAndMessages(null)
        dialog?.dismiss(); super.onDestroy()
    }
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (busy) { cancel(); return }
        if (page.isNotEmpty()) {
            if (intent.hasExtra("settings_page")) finish() else { page = ""; render() }
            return
        }
        super.onBackPressed()
    }
    private fun text(value: String, size: Float = 17f) = TextView(this).apply {
        text = value; textSize = size; setTextColor(Color.WHITE); setPadding(0, dp(5), 0, dp(5))
    }
    private fun action(label: String, block: () -> Unit) {
        list.addView(Button(this).apply {
            id = View.generateViewId(); text = label; textSize = 18f; isAllCaps = false
            setTextColor(Color.WHITE); minHeight = dp(56); background = getDrawable(R.drawable.focus_panel)
            isFocusable = true; isFocusableInTouchMode = true; setOnClickListener { if (!busy) block() }
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(6) })
    }
    private fun render() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(32), dp(16), dp(32), dp(16)); setBackgroundColor(Color.BLACK)
        }
        root.addView(text(if (page.isEmpty()) "ADB TURBO / POWER TOOLS" else if (page == "display") "SHIELD DISPLAY & SOUND" else "SHIELD ACCESSIBILITY", 26f).apply { setTextColor(Color.CYAN) })
        status = text(message, 16f); root.addView(status)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { isFillViewport = true; addView(list) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        if (busy) {
            list.addView(Button(this).apply {
                text = "CANCEL"; minHeight = dp(56); setOnClickListener { cancel() }; requestFocus()
            }); return
        }
        if (page.isNotEmpty()) { settingsPage(); return }
        action("ENABLE ADB TURBO") { enable() }
        action("STARTUP MANAGER") { startActivity(Intent(this, StartupManagerActivity::class.java)) }
        action("READ SHIELD DIAGNOSTICS") {
            runTask("Reading CPU, memory, thermal and storage facts", true) {
                bridge.withAdb(::approval) { adb -> bridge.checked(adb,
                    "printf 'CPU ACTIVITY\n'; dumpsys cpuinfo | head -n 24; printf '\nMEMORY\n'; cat /proc/meminfo | head -n 10; printf '\nTHERMAL SERVICE\n'; dumpsys thermalservice | head -n 35; printf '\nSTORAGE\n'; df -h /data") }
            }
        }
        action("RESTART A STUCK APP") { selectApp() }
        action("SLEEP SHIELD") {
            confirm("Sleep this Shield?", "Playback will stop. HDMI-CEC may also switch off connected equipment. Wake it with your remote.") {
                runTask("Sending sleep") { bridge.withAdb(::approval) { bridge.checked(it, "input keyevent 223") }; "Sleep command completed. Wake the Shield with your remote." }
            }
        }
        action("REBOOT SHIELD") {
            confirm("Reboot this Shield?", "This interrupts playback and all running apps. It does not erase their data.") {
                runTask("Requesting reboot") {
                    bridge.withAdb(::approval) { adb ->
                        try { bridge.checked(adb, "svc power reboot"); "Reboot command completed." }
                        catch (e: java.io.IOException) {
                            if (e is java.io.EOFException || e is java.net.SocketException || e.message?.contains("without a command result") == true)
                                "ADB disconnected during the reboot request. Check the TV to confirm it restarted."
                            else throw e
                        }
                    }
                }
            }
        }
        action("FAST ANIMATIONS / 0.5x") { animation("0.5") }
        action("ANIMATIONS OFF") { animation("0") }
        action("NORMAL ANIMATIONS / 1x") { animation("1") }
        action("UNDO MY ANIMATION CHANGES") { animation(null) }
        action("DISPLAY & SOUND / FIND NATIVE PAGE") { page = "display"; render() }
        action("ACCESSIBILITY / FIND NATIVE PAGE") { page = "accessibility"; render() }
        action("DEVELOPER OPTIONS") { developer(false) }
        list.addView(text("Only this device's loopback is contacted. The private approval key stays on this Shield. Each ADB operation closes its connection. Use Network Debugging only on a trusted LAN; switch it off in Developer Options when no longer needed.", 14f))
        list.getChildAt(0)?.requestFocus()
    }
    private fun approval() { ui.post { if (!isDestroyed && busy) status.text = "On the Shield prompt, select Always allow and Allow debugging. No computer is needed." } }
    private fun enable() {
        runTask("Connecting to this Shield; approve its debugging prompt if asked") {
            bridge.enable(::approval).also { prefs.edit().putBoolean("paired", true).apply() }
        }
    }
    private fun animation(scale: String?) {
        if (!bridge.hasSettingsAccess()) {
            confirm("Enable ADB TURBO first", "The app needs its one-time settings access. No commands need typing.") { enable() }
        } else runTask("Checking animation changes") { bridge.animation(scale) }
    }
    private fun developer(resumeSetup: Boolean) {
        try {
            setupOnReturn = resumeSetup
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        } catch (_: Exception) { setupOnReturn = false; showReport("Developer options", "Open Shield Settings > Device Preferences > Developer options, then enable Network Debugging.") }
    }
    private fun cancel() {
        generation++; bridge.cancel(); task?.cancel(true); busy = false
        message = "Cancelled. A command already sent may have taken effect; cancellation is not undo."; render()
    }
    private fun runTask(label: String, report: Boolean = false, operation: () -> String) {
        if (busy) return
        busy = true; message = label; val token = ++generation; bridge.resetCancellation(); render()
        val watchdog = timer.schedule({ bridge.cancel() }, 70, TimeUnit.SECONDS)
        task = worker.submit {
            val result = runCatching { operation() }
            watchdog.cancel(false)
            ui.post {
                if (isDestroyed || generation != token) return@post
                busy = false
                val failure = result.exceptionOrNull()
                message = result.getOrNull()?.take(280) ?: when (failure) {
                    is ConnectException -> "Network Debugging is off or its local port is unavailable."
                    is SocketTimeoutException -> "ADB timed out. Check the Shield's Allow debugging prompt, then try again."
                    else -> failure?.message?.take(500) ?: "The Shield did not return a result."
                }
                render()
                if (failure is ConnectException) {
                    dialog?.dismiss()
                    dialog = AlertDialog.Builder(this).setTitle("One-time Shield switch")
                        .setMessage("Enable Network Debugging in Developer Options, then press Back. TURBO will reconnect automatically and show Android's approval prompt.")
                        .setNegativeButton("CANCEL", null).setPositiveButton("OPEN DEVELOPER OPTIONS") { _, _ -> developer(true) }.show()
                } else if (report && result.isSuccess) showReport(label, result.getOrThrow())
                else if (prefs.contains("pending_component")) confirmPendingRoute()
            }
        }
    }
    private fun confirm(title: String, body: String, yes: () -> Unit) {
        dialog?.dismiss()
        dialog = AlertDialog.Builder(this).setTitle(title).setMessage(body)
            .setNegativeButton("CANCEL", null).setPositiveButton("CONTINUE") { _, _ -> yes() }.show()
    }
    private fun showReport(title: String, report: String) {
        dialog?.dismiss()
        val content = text(report.take(40000), 15f).apply { setPadding(dp(18), dp(8), dp(18), dp(8)); setTextIsSelectable(true) }
        dialog = AlertDialog.Builder(this).setTitle(title).setView(ScrollView(this).apply { addView(content) })
            .setPositiveButton("CLOSE", null).show()
    }
    @Suppress("DEPRECATION")
    private fun selectApp() {
        val apps = AppCatalog.query(this).filter { app ->
            val flags = runCatching { packageManager.getApplicationInfo(app.packageName, 0).flags }.getOrNull() ?: return@filter false
            PowerPolicy.safeUserPackage(app.packageName, (flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0)
        }
        if (apps.isEmpty()) { showReport("Restart an app", "No restartable user-installed apps were found. System apps, BOOP and NVIDIA services are deliberately excluded."); return }
        dialog?.dismiss()
        dialog = AlertDialog.Builder(this).setTitle("Choose ONE app to restart")
            .setItems(apps.map { it.label }.toTypedArray()) { _, index ->
                val app = apps[index]
                confirm("Restart ${app.label}?", "Only this app will be stopped and reopened. Its data and logins are kept. Any playback or unsaved work in it will stop.") {
                    runTask("Restarting ${app.label}") {
                        val flags = packageManager.getApplicationInfo(app.packageName, 0).flags
                        val launch = AppRoutes.launch(this, app.packageName) ?: throw IllegalStateException("App has no launch activity")
                        val component = launch.component ?: launch.resolveActivity(packageManager) ?: throw IllegalStateException("App is no longer installed")
                        val cmd = PowerPolicy.restart(app.packageName, component.flattenToString(), (flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0)
                        bridge.withAdb(::approval) { bridge.checked(it, cmd) }
                        "${app.label}: Android accepted the stop and launch commands."
                    }
                }
            }.setNegativeButton("CANCEL", null).show()
    }
    private fun settingsPage() {
        val routes = FirmwarePages.find(this, page)
        val confirmed = prefs.getString("confirmed_$page", null)
        list.addView(text("These entries come from this Shield's installed system apps, not guessed activity names. Try the combined page first. When you return, confirm the destination to save it.", 15f))
        if (routes.isEmpty()) list.addView(text("This firmware exposes no matching public page. The direct shortcut remains unavailable; general Settings is not counted as a fix.", 16f))
        routes.forEach { route ->
            val saved = if (route.component.flattenToString() == confirmed) "SAVED: " else ""
            val suffix = if (route.score == 0) " / subpage" else ""
            action("$saved${route.title}$suffix") { openRoute(route) }
        }
        action("SHOW INSTALLED ROUTE DETAILS") {
            showReport("Actual firmware entries", routes.joinToString("\n\n") { "${it.title}\n${it.component.flattenToString()}\n${it.action ?: "explicit component"}" }.ifBlank { "No matching exported activities" })
        }
        action("OPEN GENERAL SETTINGS MANUALLY") {
            try { startActivity(Intent(Settings.ACTION_SETTINGS)) } catch (_: Exception) { showReport("Settings", "Use the Shield remote's Settings button.") }
        }
        action("BACK TO POWER TOOLS") { page = ""; render() }
        (0 until list.childCount).map { list.getChildAt(it) }.firstOrNull { it is Button }?.requestFocus()
    }
    private fun openRoute(route: FirmwarePage) {
        val component = route.component.flattenToString()
        prefs.edit().putString("pending_component", component).putString("pending_page", page).commit()
        try {
            startActivity(Intent(route.action).setComponent(route.component))
        } catch (failure: Exception) {
            prefs.edit().remove("pending_component").remove("pending_page").apply()
            message = "This entry could not open: ${failure.javaClass.simpleName}. Try another installed entry."; render()
        }
    }
    private fun confirmPendingRoute() {
        val component = prefs.getString("pending_component", null) ?: return
        val selectedPage = prefs.getString("pending_page", "display") ?: "display"
        prefs.edit().remove("pending_component").remove("pending_page").apply()
        dialog?.dismiss()
        dialog = AlertDialog.Builder(this).setTitle("Was that the correct Shield page?")
            .setMessage("Only save it if it opened the page you wanted. Bouncing Home, a blank page or general Settings is not a successful shortcut.")
            .setNegativeButton("NO, TRY ANOTHER") { _, _ ->
                prefs.edit().remove("confirmed_$selectedPage").apply(); page = selectedPage; message = "That firmware entry was rejected. Try another detected entry."; render()
            }.setPositiveButton("YES, SAVE IT") { _, _ ->
                prefs.edit().putString("confirmed_$selectedPage", component).apply()
                message = "Saved your confirmed native Shield route."; page = selectedPage; render()
            }.show()
    }
}