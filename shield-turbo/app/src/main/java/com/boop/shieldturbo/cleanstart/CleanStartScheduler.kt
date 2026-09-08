package com.boop.shieldturbo.cleanstart

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle

/** Finite one-shot scheduling only. CLEAN START never becomes a periodic RAM killer. */
object CleanStartScheduler {
    const val MAX_ATTEMPTS = 3
    const val EXTRA_ATTEMPT = "clean_start_attempt"
    private const val BASE_JOB_ID = 0x42500

    fun delayForAttempt(attempt: Int): Long? = when (attempt) {
        0 -> 30_000L
        1 -> 60_000L
        2 -> 120_000L
        else -> null
    }

    fun shouldSchedule(autoEnabled: Boolean, targetCount: Int): Boolean =
        autoEnabled && targetCount > 0

    fun schedule(context: Context, attempt: Int): Boolean {
        val delay = delayForAttempt(attempt) ?: return false
        val scheduler = context.getSystemService(JobScheduler::class.java) ?: return false
        val extras = PersistableBundle().apply { putInt(EXTRA_ATTEMPT, attempt) }
        val job = JobInfo.Builder(
            BASE_JOB_ID + attempt,
            ComponentName(context, CleanStartJobService::class.java)
        )
            .setMinimumLatency(delay)
            .setOverrideDeadline(delay + 30_000L)
            .setExtras(extras)
            .build()
        return scheduler.schedule(job) == JobScheduler.RESULT_SUCCESS
    }
}
