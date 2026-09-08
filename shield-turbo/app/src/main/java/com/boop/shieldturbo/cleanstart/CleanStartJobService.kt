package com.boop.shieldturbo.cleanstart

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.boop.shieldturbo.power.LocalBridge
import com.boop.shieldturbo.power.PowerPolicy
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * One-shot post-boot CLEAN START. It uses only an already-trusted local ADB key,
 * stops reviewed targets, records bounded results, and exits.
 */
class CleanStartJobService : JobService() {
    private val executor = Executors.newSingleThreadExecutor()
    @Volatile private var task: Future<*>? = null
    @Volatile private var bridge: LocalBridge? = null

    override fun onStartJob(params: JobParameters): Boolean {
        val attempt = params.extras.getInt(CleanStartScheduler.EXTRA_ATTEMPT, -1)
        if (attempt !in 0 until CleanStartScheduler.MAX_ATTEMPTS) return false

        val store = CleanStartStore.android(applicationContext)
        val targets = store.targets()
        if (!CleanStartScheduler.shouldSchedule(store.autoEnabled(), targets.size)) return false

        task = executor.submit {
            val localBridge = LocalBridge(applicationContext)
            bridge = localBridge
            val summary = try {
                localBridge.withTrustedAdb { adb ->
                    val resumed = localBridge.resumedPackage(adb)
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
                            else -> runCatching { localBridge.stopAndVerify(adb, packageName) }
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
                        }
                    }
                    CleanStartSummary(System.currentTimeMillis(), items)
                }
            } catch (failure: Exception) {
                val nextAttempt = attempt + 1
                val retryScheduled = nextAttempt < CleanStartScheduler.MAX_ATTEMPTS &&
                    store.autoEnabled() && store.targets().isNotEmpty() &&
                    CleanStartScheduler.schedule(applicationContext, nextAttempt)
                val detail = if (retryScheduled) {
                    "ADB unavailable; bounded retry scheduled"
                } else {
                    "ADB unavailable; cleanup not applied"
                }
                CleanStartSummary(
                    System.currentTimeMillis(),
                    targets.map { CleanStartItem(it, CleanStartStatus.NOT_APPLIED, detail) }
                )
            }

            store.recordSummary(summary)
            bridge = null
            jobFinished(params, false)
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        bridge?.cancel()
        task?.cancel(true)
        bridge = null
        return false
    }

    override fun onDestroy() {
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
