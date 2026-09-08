package com.boop.shieldturbo.startup

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.boop.shieldturbo.R
import com.boop.shieldturbo.apps.AppCatalog
import com.boop.shieldturbo.apps.AppRoutes
import com.boop.shieldturbo.power.LocalBridge
import com.boop.shieldturbo.power.PowerPolicy
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.Executors
import java.util.concurrent.Future

/** User-triggered startup policy only. It deliberately has no boot receiver or background service. */
class StartupManagerActivity : Activity() {
    private val worker = Executors.newSingleThreadExecutor()
    private val ui = Handler(Looper.getMainLooper())
    private lateinit var bridge: LocalBridge
    private lateinit var ledger: StartupLedger
    private lateinit var root: LinearLayout
    private lateinit var list: LinearLayout
    private lateinit var status: TextView
    private var task: Future<*>? = null
    private var busy = false
    private var generation = 0
    private var dialog: AlertDialog? = null

    private fun dp(n: Int) = (n * resources.displayMetrics.density + 0.5f).toInt()

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        bridge = LocalBridge(applicationContext)
        ledger = StartupLedger.android(applicationContext)
        render("Choose an app. BLOCK STARTUP is the normal choice; manual launch stays available.")
    }

    override fun onDestroy() {
        generation++
        bridge.cancel()
        task?.cancel(true)
        worker.shutdownNow()
        ui.removeCallbacksAndMessages(null)
        dialog?.dismiss()
        super.onDestroy()
    }

    private fun text(value: String, size: Float = 16f, colour: Int = Color.WHITE) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(colour)
        setPadding(0, dp(5), 0, dp(5))
    }

    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        id = View.generateViewId()
        text = label
        textSize = 18f
        setTextColor(Color.WHITE)
        isAllCaps = false
        isFocusable = true
        isFocusableInTouchMode = true
        minHeight = dp(58)
        background = getDrawable(R.drawable.focus_panel)
        setOnClickListener { if (!busy) action() }
    }

    private data class Target(val packageName: String, val label: String, val system: Boolean)

    @Suppress("DEPRECATION")
    private fun targets(): List<Target> {
        val launchable = AppCatalog.query(this).associateBy { it.packageName }
        val packages = (launchable.keys + ledger.records().map { it.original.packageName }).distinct()
        return packages.mapNotNull { pkg ->
            val info = runCatching {
                packageManager.getApplicationInfo(pkg, PackageManager.MATCH_DISABLED_COMPONENTS)
            }.getOrNull() ?: return@mapNotNull null
            val system = (info.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0
            if (!PowerPolicy.safeUserPackage(pkg, system)) return@mapNotNull null
            val label = launchable[pkg]?.label ?: runCatching { info.loadLabel(packageManager).toString() }.getOrNull().orEmpty().ifBlank { pkg }
            Target(pkg, label, system)
        }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

    private fun render(message: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(32), dp(16), dp(32), dp(16))
            setBackgroundColor(Color.BLACK)
        }
        root.addView(text("STARTUP MANAGER", 27f, Color.CYAN))
        root.addView(text("Stop chosen user apps waking themselves in the background. This is not a RAM score or bulk cleaner.", 15f, Color.LTGRAY))
        status = text(message, 16f)
        root.addView(status)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { isFillViewport = true; addView(list) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        if (busy) {
            list.addView(button("CANCEL") { cancel() })
            list.getChildAt(0)?.requestFocus()
            return
        }

        val managed = ledger.records()
        if (managed.isNotEmpty()) {
            list.addView(button("UNDO ALL TURBO STARTUP CHANGES") { undoAll() })
            list.addView(text("Managed by Turbo: ${managed.size} app${if (managed.size == 1) "" else "s"}.", 14f, Color.LTGRAY))
        }

        val apps = targets()
        if (apps.isEmpty()) {
            list.addView(text("No safe user-installed launchable apps are visible. System, NVIDIA and BOOP packages are excluded.", 17f))
        } else {
            apps.forEach { app ->
                val record = ledger.record(app.packageName)
                val suffix = when (record?.mode) {
                    ManagedStartupMode.BACKGROUND_BLOCK -> "  •  BLOCK STARTUP"
                    ManagedStartupMode.HARD_BLOCK -> "  •  HARD BLOCK"
                    else -> ""
                }
                list.addView(button("${app.label}$suffix") { choose(app) }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(6) })
            }
        }
        list.addView(text("BLOCK STARTUP uses Android's background app-ops and keeps the package enabled. HARD BLOCK disables the chosen package for this Android user until Undo restores its saved state.", 14f, Color.LTGRAY))
        (0 until list.childCount).map { list.getChildAt(it) }.firstOrNull { it is Button }?.requestFocus()
    }

    private fun choose(app: Target) {
        val record = ledger.record(app.packageName)
        val actions = mutableListOf<String>()
        if (record == null || record.mode == ManagedStartupMode.NONE) {
            actions += "BLOCK STARTUP / KEEP LAUNCHABLE"
            actions += "HARD BLOCK / DISABLE APP"
        } else {
            actions += "UNDO TURBO CHANGES"
        }
        actions += "LAUNCH APP NOW"
        actions += "ANDROID APP INFO"
        dialog?.dismiss()
        dialog = AlertDialog.Builder(this)
            .setTitle(app.label)
            .setMessage("${app.packageName}\n\nTurbo changes one selected app only and keeps its original state for Undo.")
            .setItems(actions.toTypedArray()) { _, which ->
                when (actions[which]) {
                    "BLOCK STARTUP / KEEP LAUNCHABLE" -> blockStartup(app)
                    "HARD BLOCK / DISABLE APP" -> confirmHardBlock(app)
                    "UNDO TURBO CHANGES" -> undo(app)
                    "LAUNCH APP NOW" -> launch(app)
                    "ANDROID APP INFO" -> runCatching { startActivity(AppRoutes.info(app.packageName)) }
                }
            }.setNegativeButton("CANCEL", null).show()
    }

    private fun blockStartup(app: Target) {
        runTask("Saving ${app.label}'s original state, then blocking background startup") {
            bridge.withAdb { adb ->
                val existing = ledger.record(app.packageName)
                val original = existing?.original ?: bridge.readStartupState(adb, app.packageName).also {
                    if (!ledger.rememberOriginal(it)) throw IllegalStateException("Could not save the original state; nothing changed")
                }
                bridge.blockStartup(adb, original)
                if (!ledger.markMode(app.packageName, ManagedStartupMode.BACKGROUND_BLOCK)) {
                    runCatching { bridge.restoreStartup(adb, original) }
                    throw IllegalStateException("Could not record the change; original state was restored where possible")
                }
            }
            "${app.label}: background startup restricted and read back. The app remains enabled for manual launch."
        }
    }

    private fun confirmHardBlock(app: Target) {
        dialog?.dismiss()
        dialog = AlertDialog.Builder(this)
            .setTitle("HARD BLOCK ${app.label}?")
            .setMessage("This disables only this package for the current Shield user. It will NOT launch until you return here and Undo. App data and logins are kept.")
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("HARD BLOCK") { _, _ -> hardBlock(app) }
            .show()
    }

    private fun hardBlock(app: Target) {
        runTask("Saving ${app.label}'s original state, then disabling that package") {
            bridge.withAdb { adb ->
                val existing = ledger.record(app.packageName)
                val original = existing?.original ?: bridge.readStartupState(adb, app.packageName).also {
                    if (!ledger.rememberOriginal(it)) throw IllegalStateException("Could not save the original state; nothing changed")
                }
                bridge.hardBlock(adb, original)
                if (!ledger.markMode(app.packageName, ManagedStartupMode.HARD_BLOCK)) {
                    runCatching { bridge.restoreStartup(adb, original) }
                    throw IllegalStateException("Could not record the hard block; original state was restored where possible")
                }
            }
            "${app.label}: package hard-blocked for this Shield user. Undo restores the saved original state."
        }
    }

    private fun undo(app: Target) {
        val record = ledger.record(app.packageName) ?: return
        runTask("Restoring ${app.label}'s saved original state") {
            bridge.withAdb { adb ->
                bridge.restoreStartup(adb, record.original)
                if (!ledger.remove(app.packageName)) throw IllegalStateException("State restored, but Turbo could not clear its local ledger entry")
            }
            "${app.label}: original background and enabled state restored and checked."
        }
    }

    private fun undoAll() {
        val records = ledger.records()
        if (records.isEmpty()) return
        dialog?.dismiss()
        dialog = AlertDialog.Builder(this)
            .setTitle("UNDO ALL STARTUP CHANGES?")
            .setMessage("Turbo will restore the exact saved original state for ${records.size} app${if (records.size == 1) "" else "s"}. Entries are removed only after read-back succeeds.")
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("UNDO ALL") { _, _ ->
                runTask("Restoring all saved startup states") {
                    val failures = mutableListOf<String>()
                    bridge.withAdb { adb ->
                        records.forEach { record ->
                            val pkg = record.original.packageName
                            val restored = runCatching { bridge.restoreStartup(adb, record.original) }.isSuccess
                            if (restored) ledger.remove(pkg) else failures += pkg
                        }
                    }
                    if (failures.isNotEmpty()) throw IllegalStateException("Could not verify restore for: ${failures.joinToString()}. Their ledger entries were kept.")
                    "All ${records.size} saved startup states were restored and checked."
                }
            }.show()
    }

    private fun launch(app: Target) {
        val intent: Intent? = AppRoutes.launch(this, app.packageName)
        if (intent == null) {
            render("${app.label} has no available launch activity. If it is HARD BLOCKED, Undo it first.")
            return
        }
        runCatching { startActivity(intent) }.onFailure { render("${app.label} could not launch. If it is HARD BLOCKED, Undo it first.") }
    }

    private fun runTask(label: String, operation: () -> String) {
        if (busy) return
        busy = true
        val token = ++generation
        bridge.resetCancellation()
        render(label)
        task = worker.submit {
            val result = runCatching { operation() }
            ui.post {
                if (isDestroyed || generation != token) return@post
                busy = false
                val failure = result.exceptionOrNull()
                val message = result.getOrNull() ?: when (failure) {
                    is ConnectException -> "ADB TURBO is not connected. Enable Network Debugging and ENABLE ADB TURBO first."
                    is SocketTimeoutException -> "ADB timed out. Check for the Shield's debugging approval prompt."
                    else -> failure?.message?.take(600) ?: "The Shield did not return a result."
                }
                render(message)
            }
        }
    }

    private fun cancel() {
        generation++
        bridge.cancel()
        task?.cancel(true)
        busy = false
        render("Cancelled. A command already sent may have taken effect; use Undo if Turbo recorded a managed app.")
    }
}