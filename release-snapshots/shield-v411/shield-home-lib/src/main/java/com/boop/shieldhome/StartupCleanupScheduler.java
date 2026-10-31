package com.boop.shieldhome;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;

public final class StartupCleanupScheduler {
    public static final String EXTRA_ATTEMPT = "boop_clean_start_attempt";
    public static final int MAX_ATTEMPTS = 3;
    private static final int BASE_JOB_ID = 0x42600;
    private StartupCleanupScheduler() { }

    static long delayForAttempt(int attempt) {
        return switch (attempt) { case 0 -> 30_000L; case 1 -> 60_000L; case 2 -> 120_000L; default -> -1L; };
    }

    public static boolean schedule(Context context, int attempt) {
        long delay = delayForAttempt(attempt);
        if (delay < 0) return false;
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        if (scheduler == null) return false;
        PersistableBundle extras = new PersistableBundle();
        extras.putInt(EXTRA_ATTEMPT, attempt);
        JobInfo job = new JobInfo.Builder(BASE_JOB_ID + attempt,
                new ComponentName(context, StartupCleanupJobService.class))
                .setMinimumLatency(delay)
                .setOverrideDeadline(delay + 30_000L)
                .setExtras(extras).build();
        return scheduler.schedule(job) == JobScheduler.RESULT_SUCCESS;
    }
}
