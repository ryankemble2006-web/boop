package com.boop.shieldturbo.cleanstart

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.SystemClock
import com.boop.shieldturbo.power.LocalBridge
import com.boop.shieldturbo.power.PowerPolicy
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * One-shot post-boot CLEAN START. It uses only an already-trusted local ADB key,
 * stops reviewed targets, records bounded results, and exits.
 */
class CleanStartJobService : JobService() {
    companion object {
        private const val INDICATOR_PRESENT_TIMEOUT_MS = 500L
    }

    private val executor = Executors.newSingleThreadExecutor()
    @Volatile private var task: Future<*>? = null
    @Volatile private var bridge: LocalBridge? = null
    @Volatile private var activeIndicator: CleanStartIndicator? = null

    override fun onStartJob(params: JobParameters): Boolean {
        val attempt = params.extras.getInt(CleanStartScheduler.EXTRA_ATTEMPT, -1)
        if (attempt !in 0 until CleanStartScheduler.MAX_ATTEMPTS) return false

        val store = CleanStartStore.android(applicationContext)
        val targets = store.targets()
        if (!CleanStartScheduler.shouldSchedule(store.autoEnabled(), targets.size)) return false

        val jobStartedElapsed = SystemClock.elapsedRealtime()
        val indicator = CleanStartIndicator(applicationContext)
        activeIndicator = indicator
        indicator.show()

        task = executor.submit {
            var noticeMs = 0L
            var adbReadyMs = 0L
            var resumedQueryMs = 0L
            var stopsTotalMs = 0L
            var slowestPackage = ""
            var slowestStopMs = 0L
            var adbStartedElapsed = 0L
            var adbEntered = false
            try {
                // Do not begin ADB/force-stop work merely because the window was requested.
                // Wait briefly for a committed static frame, then fail open so presentation
                // can never add several seconds to the physically proven cleanup path.
                val presented = indicator.awaitPresented(INDICATOR_PRESENT_TIMEOUT_MS)
                noticeMs = (SystemClock.elapsedRealtime() - jobStartedElapsed).coerceAtLeast(0L)
                store.recordIndicatorDiagnostic(indicator.diagnostic(presented))
                if (!presented) indicator.hide()

                val localBridge = LocalBridge(applicationContext)
                bridge = localBridge
                val summary = try {
                    adbStartedElapsed = SystemClock.elapsedRealtime()
                    localBridge.withTrustedAdb { adb ->
                        adbEntered = true
                        adbReadyMs = (SystemClock.elapsedRealtime() - adbStartedElapsed).coerceAtLeast(0L)

                        val resumedStarted = SystemClock.elapsedRealtime()
                        val resumed = localBridge.resumedPackage(adb)
                        resumedQueryMs = (SystemClock.elapsedRealtime() - resumedStarted).coerceAtLeast(0L)

                        val items = targets.map { packageName ->
                            when {
                                !safeTarget(packageName) -> CleanStartItem(
                                    packageName,
                                    CleanStartStatus.FAILED,
                                    "No longer an eligible user app"
                                )
                                packageName == resumed -> CleanStartItem(
                                    packageName,
                                    CleanStartStatus.SKIPPED_IN_USE,
                                    "App is currently in use"
                                )
                                else -> {
                                    val stopStarted = SystemClock.elapsedRealtime()
                                    val item = runCatching { localBridge.stopAndVerify(adb, packageName) }
                                        .fold(
                                            onSuccess = {
                                                CleanStartItem(packageName, CleanStartStatus.STOPPED, "Stopped and verified")
                                            },
                                            onFailure = { failure ->
                                                CleanStartItem(
                                                    packageName,
                                                    CleanStartStatus.FAILED,
                                                    failure.message?.take(160) ?: "Stop could not be verified"
                                                )
                                            }
                                        )
                                    val stopMs = (SystemClock.elapsedRealtime() - stopStarted).coerceAtLeast(0L)
                                    stopsTotalMs += stopMs
                                    if (stopMs > slowestStopMs) {
                                        slowestStopMs = stopMs
                                        slowestPackage = packageName
                                    }
                                    item
                                }
                            }
                        }
                        CleanStartSummary(System.currentTimeMillis(), items)
                    }
                } catch (failure: Exception) {
                    if (!adbEntered && adbStartedElapsed > 0L) {
                        adbReadyMs = (SystemClock.elapsedRealtime() - adbStartedElapsed).coerceAtLeast(0L)
                    }
                    val nextAttempt = attempt + 1
                    val retryScheduled = nextAttempt < CleanStartScheduler.MAX_ATTEMPTS &&
                        store.autoEnabled() && store.targets().isNotEmpty() &&
                        CleanStartScheduler.schedule(applicationContext, nextAttempt)
                    val detail = buildString {
                        append("ADB unavailable: ")
                        append(failure.javaClass.simpleName)
                        failure.message?.trim()?.takeIf { it.isNotBlank() }?.let { message ->
                            append(" - ")
                            append(message.take(100))
                        }
                        append(if (retryScheduled) "; bounded retry scheduled" else "; cleanup not applied")
                    }.take(160)
                    CleanStartSummary(
                        System.currentTimeMillis(),
                        targets.map { CleanStartItem(it, CleanStartStatus.NOT_APPLIED, detail) }
                    )
                }

                store.recordSummary(summary)
            } finally {
                val totalJobMs = (SystemClock.elapsedRealtime() - jobStartedElapsed).coerceAtLeast(0L)
                runCatching {
                    store.recordTimingDiagnostic(
                        CleanStartTimingDiagnostic(
                            timestampMillis = System.currentTimeMillis(),
                            noticeMs = noticeMs,
                            adbReadyMs = adbReadyMs,
                            resumedQueryMs = resumedQueryMs,
                            stopsTotalMs = stopsTotalMs,
                            slowestPackage = slowestPackage,
                            slowestStopMs = slowestStopMs,
                            totalJobMs = totalJobMs
                        )
                    )
                }
                bridge = null
                indicator.hide()
                if (activeIndicator === indicator) activeIndicator = null
                jobFinished(params, false)
            }
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        activeIndicator?.hide()
        activeIndicator = null
        bridge?.cancel()
        task?.cancel(true)
        bridge = null
        return false
    }

    override fun onDestroy() {
        activeIndicator?.hide()
        activeIndicator = null
        bridge?.cancel()
        task?.cancel(true)
        executor.shutdownNow()
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    private fun safeTarget(packageName: String): Boolean {
        if (!CleanStartPolicy.validPackage(packageName)) return false
        val info = runCatching {
            packageManager.getApplicationInfo(packageName, PackageManager.MATCH_DISABLED_COMPONENTS)
        }.getOrNull() ?: return false
        val system = (info.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0
        return PowerPolicy.safeUserPackage(packageName, system)
    }
}
