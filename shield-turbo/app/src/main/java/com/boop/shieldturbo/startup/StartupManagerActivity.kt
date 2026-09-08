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
import com.boop.shieldturbo.cleanstart.CleanStartItem
import com.boop.shieldturbo.cleanstart.CleanStartStatus
import com.boop.shieldturbo.cleanstart.CleanStartStore
import com.boop.shieldturbo.cleanstart.CleanStartSummary
import com.boop.shieldturbo.power.LocalBridge
import com.boop.shieldturbo.power.PowerPolicy
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.Executors
import java.util.concurrent.Future

/** User-controlled CLEAN START plus exact rollback for older Turbo startup changes. */
class StartupManagerActivity : Activity() {
    private val worker = Executors.newSingleThreadExecutor()
    private val ui = Handler(Looper.getMainLooper())
    private lateinit var bridge: LocalBridge
    private lateinit var ledger: StartupLedger
    private lateinit var cleanStore: CleanStartStore
    private lateinit var list: LinearLayout
    private var task: Future<*>? = null
    private var busy = false
    private var generation = 0
    private var dialog: AlertDialog? = null

    private fun dp(n: Int) = (n * resources.displayMetrics.density + 0.5f).toInt()

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        bridge = LocalBridge(applicationContext)
        ledger = StartupLedger.android(applicationContext)
        cleanStore = CleanStartStore.android(applicationContext)
        render("Choose an app. STOP + VERIFY NOW proves the real cleanup before you automate it.")
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        if (busy) cancel() else super.onBackPressed()
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

    private fun button(label: String, allowWhileBusy: Boolean = false, action: () -> Unit) = Button(this).apply {
        id = View.generateViewId()
        text = label
        textSize = 18f
        setTextColor(Color.WHITE)
        isAllCaps = false
        isFocusable = true
        isFocusableInTouchMode = true
        minHeight = dp(58)
        background = getDrawable(R.drawable.focus_panel)
        setOnClickListener { if (!busy || allowWhileBusy) action() }
    }

    private data class Target(val packageName: String, val label: String, val system: Boolean)

    @Suppress("DEPRECATION")
    private fun targets(): List<Target> {
        val launchable = AppCatalog.query(this).associateBy { it.packageName }
        val packages = (
            launchable.keys +
                ledger.records().map { it.original.packageName } +
                cleanStore.targets()
            ).distinct()
        return packages.mapNotNull { pkg ->
            val info = runCatching {
                packageManager.getApplicationInfo(pkg, PackageManager.MATCH_DISABLED_COMPONENTS)
            }.getOrNull() ?: return@mapNotNull null
            val system = (info.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0
            if (!PowerPolicy.safeUserPackage(pkg, system)) return@mapNotNull null
            val applicationLabel = runCatching { info.loadLabel(packageManager).toString() }.getOrNull()
            val label = StartupAppLabels.resolve(pkg, launchable[pkg]?.label, applicationLabel)
            Target(pkg, label, system)
        }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

    @Suppress("DEPRECATION")
    private fun safeTarget(packageName: String): Boolean {
        val info = runCatching {
            packageManager.getApplicationInfo(packageName, PackageManager.MATCH_DISABLED_COMPONENTS)
        }.getOrNull() ?: return false
        val system = (info.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0
        return PowerPolicy.safeUserPackage(packageName, system)
    }

    private fun render(message: String) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(32), dp(16), dp(32), dp(16))
            setBackgroundColor(Color.BLACK)
        }
        root.addView(text("CLEAN START", 27f, Color.CYAN))
        root.addView(text("Stop only the optional apps you choose. CLEAN START does not clear data, uninstall apps or chase a fake free-RAM score.", 15f, Color.LTGRAY))
        root.addView(text(message, 16f))
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { isFillViewport = true; addView(list) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        if (busy) {
            list.addView(button("CANCEL", allowWhileBusy = true) { cancel() })
            list.getChildAt(0)?.requestFocus()
            return
        }

        val cleanTargets = cleanStore.targets()
        val auto = cleanStore.autoEnabled()
        list.addView(button("AUTO CLEAN AFTER REBOOT: ${if (auto) "ON" else "OFF"}") { toggleAuto() })
        if (cleanTargets.isNotEmpty()) {
            list.addView(button("RUN CLEAN START NOW / ${cleanTargets.size} APP${if (cleanTargets.size == 1) "" else "S"}") { runCleanStartGroup() })
        }
        cleanStore.lastSummary()?.let { summary ->
            list.addView(text(lastSummaryLine(summary), 14f, Color.LTGRAY))
        }
        cleanStore.lastIndicatorDiagnostic()?.let { diagnostic ->
            val detail = diagnostic.detail.takeIf { it.isNotBlank() }?.let { " • $it" }.orEmpty()
            list.addView(
                text(
                    "STARTUP NOTICE DIAGNOSTIC: permission=${if (diagnostic.overlayAllowed) "YES" else "NO"} • window=${diagnostic.windowMode.name} • add=${diagnostic.addStatus.name} • present=${diagnostic.presentationStatus.name} • ${diagnostic.elapsedMs}ms$detail",
                    14f,
                    Color.LTGRAY
                )
            )
        }
        cleanStore.lastTimingDiagnostic()?.let { timing ->
            val slowest = timing.slowestPackage.takeIf { it.isNotBlank() }
                ?.let { " • slowest=$it:${timing.slowestStopMs}ms" }
                .orEmpty()
            list.addView(
                text(
                    "CLEAN START TIMING: notice=${timing.noticeMs}ms • adbReady=${timing.adbReadyMs}ms • resumed=${timing.resumedQueryMs}ms • stops=${timing.stopsTotalMs}ms$slowest • total=${timing.totalJobMs}ms",
                    14f,
                    Color.LTGRAY
                )
            )
        }

        val oldRecords = ledger.records()
        if (oldRecords.isNotEmpty()) {
            list.addView(button("UNDO ALL OLD TURBO STARTUP CHANGES") { undoAll() })
            list.addView(text("${oldRecords.size} older Turbo restriction${if (oldRecords.size == 1) "" else "s"} still ${if (oldRecords.size == 1) "has" else "have"} an exact Undo record.", 14f, Color.LTGRAY))
        }

        val apps = targets()
        if (apps.isEmpty()) {
            list.addView(text("No safe user-installed launchable apps are visible. System, NVIDIA core and BOOP packages are excluded.", 17f))
        } else {
            apps.forEach { app ->
                val record = ledger.record(app.packageName)
                val suffix = buildString {
                    if (cleanTargets.contains(app.packageName)) append("  •  CLEAN START")
                    if (record?.mode == ManagedStartupMode.BACKGROUND_BLOCK) append("  •  OLD RESTRICTION")
                    if (record?.mode == ManagedStartupMode.HARD_BLOCK) append("  •  HARD BLOCK")
                }
                list.addView(button("${app.label}$suffix") { choose(app) }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(6) })
            }
        }
        list.addView(text("STOP + VERIFY NOW uses Android's real force-stop for one selected app and checks that its package processes are gone while the package stays enabled. Automatic CLEAN START skips an app that is currently in use.", 14f, Color.LTGRAY))
        (0 until list.childCount).map { list.getChildAt(it) }.firstOrNull { it is Button }?.requestFocus()
    }

    private fun choose(app: Target) {
        val record = ledger.record(app.packageName)
        val clean = cleanStore.targets().contains(app.packageName)
        val actions = mutableListOf<String>()

        if (record?.mode != ManagedStartupMode.HARD_BLOCK) {
            actions += if (clean) "REMOVE FROM CLEAN START" else "ADD TO CLEAN START"
            actions += "STOP + VERIFY NOW"
        }
        if (record != null && record.mode != ManagedStartupMode.NONE) {
            actions += if (record.mode == ManagedStartupMode.BACKGROUND_BLOCK) {
                "UNDO OLD BACKGROUND RESTRICTION"
            } else {
                "UNDO TURBO CHANGES"
            }
        } else {
            actions += "HARD BLOCK / DISABLE APP"
        }
        actions += "LAUNCH APP NOW"
        actions += "ANDROID APP INFO"

        dialog?.dismiss()
        dialog = AlertDialog.Builder(this)
            .setTitle(app.label)
            .setItems(actions.toTypedArray()) { _, which ->
                when (actions[which]) {
                    "ADD TO CLEAN START" -> setCleanTarget(app, true)
                    "REMOVE FROM CLEAN START" -> setCleanTarget(app, false)
                    "STOP + VERIFY NOW" -> confirmStopNow(app)
                    "HARD BLOCK / DISABLE APP" -> confirmHardBlock(app)
                    "UNDO OLD BACKGROUND RESTRICTION", "UNDO TURBO CHANGES" -> undo(app)
                    "LAUNCH APP NOW" -> launch(app)
                    "ANDROID APP INFO" -> runCatching { startActivity(AppRoutes.info(app.packageName)) }
                }
            }.setNegativeButton("CANCEL", null).show()
    }

    private fun setCleanTarget(app: Target, enabled: Boolean) {
        if (!safeTarget(app.packageName)) {
            render("${app.label} is no longer an eligible user app. Nothing changed.")
            return
        }
        val saved = cleanStore.setTarget(app.packageName, enabled)
        render(if (saved) {
            if (enabled) "${app.label} added to CLEAN START. Test STOP + VERIFY NOW before relying on reboot cleanup."
            else "${app.label} removed from CLEAN START. Android package state was not changed."
        } else "Could not save CLEAN START membership. Nothing changed.")
    }

    private fun toggleAuto() {
        val next = !cleanStore.autoEnabled()
        if (next && cleanStore.targets().isEmpty()) {
            render("Add at least one optional app to CLEAN START before enabling automatic cleanup.")
            return
        }
        val saved = cleanStore.setAutoEnabled(next)
        render(if (saved) "AUTO CLEAN AFTER REBOOT is now ${if (next) "ON" else "OFF"}." else "Could not save the automatic cleanup setting.")
    }

    private fun confirmStopNow(app: Target) {
        dialog?.dismiss()
        dialog = AlertDialog.Builder(this)
            .setTitle("STOP + VERIFY ${app.label}?")
            .setMessage("This stops this one app now. Playback or unsaved work in it will stop. Its data, logins and normal launcher remain.")
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("STOP + VERIFY") { _, _ -> stopNow(app) }
            .show()
    }

    private fun stopNow(app: Target) {
        if (!safeTarget(app.packageName)) {
            render("${app.label} is no longer an eligible user app. Nothing was stopped.")
            return
        }
        runTask("Stopping ${app.label} and checking the result") {
            bridge.withAdb { adb ->
                if (!safeTarget(app.packageName)) throw IllegalStateException("App is no longer an eligible CLEAN START target")
                val result = bridge.stopAndVerify(adb, app.packageName)
                "${app.label}: STOPPED AND VERIFIED. ${result.beforeProcesses.size} matching process${if (result.beforeProcesses.size == 1) "" else "es"} before; none after. The app remains launchable."
            }
        }
    }

    private fun runCleanStartGroup() {
        val packages = cleanStore.targets().toList()
        if (packages.isEmpty()) {
            render("CLEAN START has no selected apps yet.")
            return
        }
        runTask("Running CLEAN START for ${packages.size} selected app${if (packages.size == 1) "" else "s"}") {
            val summary = bridge.withAdb { adb ->
                val resumed = bridge.resumedPackage(adb)
                val items = packages.map { packageName ->
                    when {
                        packageName == resumed -> CleanStartItem(packageName, CleanStartStatus.SKIPPED_IN_USE, "currently in use")
                        ledger.record(packageName)?.mode == ManagedStartupMode.HARD_BLOCK -> CleanStartItem(packageName, CleanStartStatus.NOT_APPLIED, "hard blocked")
                        !safeTarget(packageName) -> CleanStartItem(packageName, CleanStartStatus.NOT_APPLIED, "not an eligible installed user app")
                        else -> runCatching {
                            bridge.stopAndVerify(adb, packageName)
                            CleanStartItem(packageName, CleanStartStatus.STOPPED, "verified")
                        }.getOrElse { failure ->
                            CleanStartItem(packageName, CleanStartStatus.FAILED, failure.message.orEmpty().take(120))
                        }
                    }
                }
                CleanStartSummary(System.currentTimeMillis(), items)
            }
            cleanStore.recordSummary(summary)
            summaryMessage(summary)
        }
    }

    private fun summaryMessage(summary: CleanStartSummary): String {
        val stopped = summary.items.count { it.status == CleanStartStatus.STOPPED }
        val skipped = summary.items.count { it.status == CleanStartStatus.SKIPPED_IN_USE }
        val failed = summary.items.count { it.status == CleanStartStatus.FAILED || it.status == CleanStartStatus.NOT_APPLIED }
        return "CLEAN START finished: $stopped stopped + verified, $skipped skipped because in use, $failed failed/not applied."
    }

    private fun lastSummaryLine(summary: CleanStartSummary): String {
        val base = "LAST CLEAN START: ${summaryMessage(summary).removePrefix("CLEAN START finished: ")}"
        val detail = summary.items.firstOrNull { item ->
            (item.status == CleanStartStatus.FAILED || item.status == CleanStartStatus.NOT_APPLIED) &&
                item.detail.isNotBlank()
        }?.detail?.take(160)
        return if (detail.isNullOrBlank()) base else "$base\nLAST CLEAN START DETAIL: $detail"
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
            .setTitle("UNDO ALL OLD TURBO STARTUP CHANGES?")
            .setMessage("Turbo will restore the exact saved original state for ${records.size} app${if (records.size == 1) "" else "s"}. CLEAN START membership is separate and will not be removed.")
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("UNDO ALL") { _, _ ->
                runTask("Restoring all saved old startup states") {
                    val failures = mutableListOf<String>()
                    bridge.withAdb { adb ->
                        records.forEach { record ->
                            val pkg = record.original.packageName
                            val restored = runCatching { bridge.restoreStartup(adb, record.original) }.isSuccess
                            if (restored) ledger.remove(pkg) else failures += pkg
                        }
                    }
                    if (failures.isNotEmpty()) throw IllegalStateException("Could not verify restore for: ${failures.joinToString()}. Their ledger entries were kept.")
                    "All ${records.size} old Turbo startup states were restored and checked."
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
        render("Cancelled. A stop command already sent may have taken effect; cancellation is not Undo.")
    }
}
