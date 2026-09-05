package com.boop.shieldoverlay;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class RoutinesControllerTest {
    private static final RoutineItem BEDTIME =
            new RoutineItem("script.bedtime", "Bedtime", RoutineItem.Type.SCRIPT);
    private static final RoutineItem MOVIE =
            new RoutineItem("scene.movie", "Movie Night", RoutineItem.Type.SCENE);

    @Test
    public void startEmitsLoadingThenLiveIncludingEmptyList() {
        FakeRepository repository = new FakeRepository();
        ManualScheduler scheduler = new ManualScheduler();
        List<RoutinesController.ViewState> rendered = new ArrayList<>();
        RoutinesController controller = new RoutinesController(repository, scheduler, rendered::add);

        controller.start();

        assertEquals(2, rendered.size());
        assertEquals(RoutinesController.PageStatus.LOADING, rendered.get(0).pageStatus());
        assertEquals(RoutinesController.PageStatus.LIVE, rendered.get(1).pageStatus());
        assertTrue(rendered.get(1).rows().isEmpty());
    }

    @Test
    public void loadErrorEmitsOfflineWithNoRows() {
        FakeRepository repository = new FakeRepository();
        repository.loadError = "offline";
        AtomicReference<RoutinesController.ViewState> rendered = new AtomicReference<>();
        RoutinesController controller = new RoutinesController(
                repository, new ManualScheduler(), rendered::set);

        controller.start();

        assertEquals(RoutinesController.PageStatus.OFFLINE, rendered.get().pageStatus());
        assertTrue(rendered.get().rows().isEmpty());
    }

    @Test
    public void duplicateRunningRowIsIgnoredWhileOtherRowCanRun() {
        FakeRepository repository = repositoryWithTwoItems();
        AtomicReference<RoutinesController.ViewState> rendered = new AtomicReference<>();
        RoutinesController controller = new RoutinesController(
                repository, new ManualScheduler(), rendered::set);
        controller.start();

        controller.runRoutine("script.bedtime");
        controller.runRoutine("script.bedtime");
        controller.runRoutine("scene.movie");

        assertEquals(Arrays.asList("script.bedtime", "scene.movie"), repository.runCalls);
        assertEquals(RoutinesController.RowStatus.RUNNING,
                row(rendered.get(), "script.bedtime").status());
        assertEquals(RoutinesController.RowStatus.RUNNING,
                row(rendered.get(), "scene.movie").status());
    }

    @Test
    public void successShowsDoneForTwoSecondsThenReady() {
        FakeRepository repository = repositoryWithTwoItems();
        ManualScheduler scheduler = new ManualScheduler();
        AtomicReference<RoutinesController.ViewState> rendered = new AtomicReference<>();
        RoutinesController controller = new RoutinesController(repository, scheduler, rendered::set);
        controller.start();

        controller.runRoutine("scene.movie");
        repository.complete("scene.movie", true, null);

        assertEquals(RoutinesController.RowStatus.DONE,
                row(rendered.get(), "scene.movie").status());
        assertTrue(scheduler.hasDelay(2_000L));
        scheduler.runDelay(2_000L);
        assertEquals(RoutinesController.RowStatus.READY,
                row(rendered.get(), "scene.movie").status());
    }

    @Test
    public void failureShowsFailedForTwoSecondsThenReady() {
        FakeRepository repository = repositoryWithTwoItems();
        ManualScheduler scheduler = new ManualScheduler();
        AtomicReference<RoutinesController.ViewState> rendered = new AtomicReference<>();
        RoutinesController controller = new RoutinesController(repository, scheduler, rendered::set);
        controller.start();

        controller.runRoutine("scene.movie");
        repository.complete("scene.movie", false, "raw HA text");

        assertEquals(RoutinesController.RowStatus.FAILED,
                row(rendered.get(), "scene.movie").status());
        assertTrue(scheduler.hasDelay(2_000L));
        scheduler.runDelay(2_000L);
        assertEquals(RoutinesController.RowStatus.READY,
                row(rendered.get(), "scene.movie").status());
    }

    @Test
    public void scriptTimeoutCancelsExecutionAndShowsFailed() {
        FakeRepository repository = repositoryWithTwoItems();
        ManualScheduler scheduler = new ManualScheduler();
        AtomicReference<RoutinesController.ViewState> rendered = new AtomicReference<>();
        RoutinesController controller = new RoutinesController(repository, scheduler, rendered::set);
        controller.start();

        controller.runRoutine("script.bedtime");
        assertTrue(scheduler.hasDelay(120_000L));
        scheduler.runDelay(120_000L);

        assertTrue(repository.execution("script.bedtime").cancelled);
        assertEquals(RoutinesController.RowStatus.FAILED,
                row(rendered.get(), "script.bedtime").status());
        assertTrue(scheduler.hasDelay(2_000L));
    }

    @Test
    public void markOfflineWithoutRunClearsRowsImmediately() {
        FakeRepository repository = repositoryWithTwoItems();
        AtomicReference<RoutinesController.ViewState> rendered = new AtomicReference<>();
        RoutinesController controller = new RoutinesController(
                repository, new ManualScheduler(), rendered::set);
        controller.start();

        controller.markOffline();

        assertEquals(RoutinesController.PageStatus.OFFLINE, rendered.get().pageStatus());
        assertTrue(rendered.get().rows().isEmpty());
    }

    @Test
    public void markOfflineDuringRunShowsFailureThenCollapses() {
        FakeRepository repository = repositoryWithTwoItems();
        ManualScheduler scheduler = new ManualScheduler();
        AtomicReference<RoutinesController.ViewState> rendered = new AtomicReference<>();
        RoutinesController controller = new RoutinesController(repository, scheduler, rendered::set);
        controller.start();
        controller.runRoutine("script.bedtime");

        controller.markOffline();

        assertTrue(repository.execution("script.bedtime").cancelled);
        assertEquals(RoutinesController.PageStatus.OFFLINE, rendered.get().pageStatus());
        assertEquals(RoutinesController.RowStatus.FAILED,
                row(rendered.get(), "script.bedtime").status());
        for (RoutinesController.RowState row : rendered.get().rows()) {
            assertFalse(row.enabled());
        }
        assertTrue(scheduler.hasDelay(2_000L));
        scheduler.runDelay(2_000L);
        assertEquals(RoutinesController.PageStatus.OFFLINE, rendered.get().pageStatus());
        assertTrue(rendered.get().rows().isEmpty());
    }

    @Test
    public void closeCancelsActiveWorkAndIgnoresLateCallbacks() {
        FakeRepository repository = repositoryWithTwoItems();
        ManualScheduler scheduler = new ManualScheduler();
        List<RoutinesController.ViewState> rendered = new ArrayList<>();
        RoutinesController controller = new RoutinesController(repository, scheduler, rendered::add);
        controller.start();
        controller.runRoutine("script.bedtime");
        int beforeClose = rendered.size();

        controller.close();
        repository.complete("script.bedtime", true, null);

        assertTrue(repository.execution("script.bedtime").cancelled);
        assertEquals(beforeClose, rendered.size());
        assertFalse(scheduler.hasDelay(120_000L));
    }

    private static FakeRepository repositoryWithTwoItems() {
        FakeRepository repository = new FakeRepository();
        repository.items = Arrays.asList(BEDTIME, MOVIE);
        return repository;
    }

    private static RoutinesController.RowState row(
            RoutinesController.ViewState state,
            String entityId) {
        for (RoutinesController.RowState row : state.rows()) {
            if (entityId.equals(row.routine().entityId())) {
                return row;
            }
        }
        throw new AssertionError("Missing row " + entityId);
    }

    private static final class ManualScheduler implements RoutinesController.SchedulerPort {
        private static final class Entry implements RoutinesController.ScheduledTask {
            final long delayMs;
            final Runnable runnable;
            boolean cancelled;

            Entry(long delayMs, Runnable runnable) {
                this.delayMs = delayMs;
                this.runnable = runnable;
            }

            @Override
            public void cancel() {
                cancelled = true;
            }
        }

        final List<Entry> entries = new ArrayList<>();

        @Override
        public RoutinesController.ScheduledTask schedule(long delayMs, Runnable runnable) {
            Entry entry = new Entry(delayMs, runnable);
            entries.add(entry);
            return entry;
        }

        boolean hasDelay(long delayMs) {
            for (Entry entry : entries) {
                if (!entry.cancelled && entry.delayMs == delayMs) {
                    return true;
                }
            }
            return false;
        }

        void runDelay(long delayMs) {
            for (Entry entry : new ArrayList<>(entries)) {
                if (!entry.cancelled && entry.delayMs == delayMs) {
                    entry.cancelled = true;
                    entry.runnable.run();
                    return;
                }
            }
            throw new AssertionError("No active task for delay " + delayMs);
        }
    }

    private static final class FakeExecution implements RoutinesRepository.Execution {
        boolean cancelled;

        @Override
        public void cancel() {
            cancelled = true;
        }
    }

    private static final class PendingRun {
        final RoutinesRepository.RunCallback callback;
        final FakeExecution execution;

        PendingRun(RoutinesRepository.RunCallback callback, FakeExecution execution) {
            this.callback = callback;
            this.execution = execution;
        }
    }

    private static final class FakeRepository implements RoutinesController.RepositoryPort {
        List<RoutineItem> items = Collections.emptyList();
        String loadError;
        final List<String> runCalls = new ArrayList<>();
        final Map<String, PendingRun> pending = new HashMap<>();

        @Override
        public void loadRoutines(RoutinesRepository.LoadCallback callback) {
            callback.onResult(items, loadError);
        }

        @Override
        public RoutinesRepository.Execution run(
                RoutineItem routine,
                RoutinesRepository.RunCallback callback) {
            runCalls.add(routine.entityId());
            FakeExecution execution = new FakeExecution();
            pending.put(routine.entityId(), new PendingRun(callback, execution));
            return execution;
        }

        void complete(String entityId, boolean success, String error) {
            PendingRun run = pending.get(entityId);
            run.callback.onResult(success, error);
        }

        FakeExecution execution(String entityId) {
            return pending.get(entityId).execution;
        }
    }
}
