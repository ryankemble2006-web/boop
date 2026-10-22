package com.boop.shieldhome;

import android.app.job.JobParameters;
import android.app.job.JobService;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class StartupCleanupJobService extends JobService {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile Future<?> task;
    private volatile StartupLocalBridge bridge;

    @Override public boolean onStartJob(JobParameters params) {
        int attempt = params.getExtras().getInt(StartupCleanupScheduler.EXTRA_ATTEMPT, -1);
        if (attempt < 0 || attempt >= StartupCleanupScheduler.MAX_ATTEMPTS) return false;
        StartupCleanupStore store = new StartupCleanupStore(getApplicationContext());
        Set<String> targets = store.targets();
        if (!store.autoEnabled() || targets.isEmpty()) return false;

        task = executor.submit(() -> {
            try {
                bridge = new StartupLocalBridge(getApplicationContext());
                store.recordSummary(bridge.run(targets, false, () -> {}));
            } catch (Exception failure) {
                int next = attempt + 1;
                boolean retry = next < StartupCleanupScheduler.MAX_ATTEMPTS
                        && store.autoEnabled() && !store.targets().isEmpty()
                        && StartupCleanupScheduler.schedule(getApplicationContext(), next);
                String detail = failure.getMessage();
                if (detail == null || detail.isBlank()) detail = failure.getClass().getSimpleName();
                store.recordSummary("Clean Start not applied: " + detail.replace('\n',' ').replace('\r',' ')
                        + (retry ? ". Retry scheduled." : "."));
            } finally {
                bridge = null;
                jobFinished(params, false);
            }
        });
        return true;
    }

    @Override public boolean onStopJob(JobParameters params) {
        if (bridge != null) bridge.cancel();
        if (task != null) task.cancel(true);
        bridge = null;
        return false;
    }

    @Override public void onDestroy() {
        if (bridge != null) bridge.cancel();
        if (task != null) task.cancel(true);
        executor.shutdownNow();
        super.onDestroy();
    }
}
