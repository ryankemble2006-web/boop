package com.boop.shieldoverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RoutinesController implements AutoCloseable {
    private static final long RESULT_HOLD_MS = 2_000L;
    private static final long SCRIPT_TIMEOUT_MS = 120_000L;

    public enum PageStatus {
        LOADING,
        LIVE,
        OFFLINE
    }

    public enum RowStatus {
        READY,
        RUNNING,
        DONE,
        FAILED
    }

    public interface RepositoryPort {
        void loadRoutines(RoutinesRepository.LoadCallback callback);

        RoutinesRepository.Execution run(
                RoutineItem routine,
                RoutinesRepository.RunCallback callback);
    }

    public interface ScheduledTask {
        void cancel();
    }

    public interface SchedulerPort {
        ScheduledTask schedule(long delayMs, Runnable runnable);
    }

    public interface Listener {
        void onViewState(ViewState state);
    }

    public static final class RowState {
        private final RoutineItem routine;
        private final RowStatus status;
        private final boolean enabled;

        private RowState(RoutineItem routine, RowStatus status, boolean enabled) {
            this.routine = routine;
            this.status = status;
            this.enabled = enabled;
        }

        public RoutineItem routine() {
            return routine;
        }

        public RowStatus status() {
            return status;
        }

        public boolean enabled() {
            return enabled;
        }
    }

    public static final class ViewState {
        private final PageStatus pageStatus;
        private final List<RowState> rows;

        private ViewState(PageStatus pageStatus, List<RowState> rows) {
            this.pageStatus = pageStatus;
            this.rows = Collections.unmodifiableList(new ArrayList<>(rows));
        }

        public PageStatus pageStatus() {
            return pageStatus;
        }

        public List<RowState> rows() {
            return rows;
        }

        public static ViewState loading() {
            return new ViewState(PageStatus.LOADING, Collections.emptyList());
        }

        public static ViewState offline() {
            return new ViewState(PageStatus.OFFLINE, Collections.emptyList());
        }
    }

    private static final class Operation {
        final RoutineItem routine;
        RoutinesRepository.Execution execution;
        ScheduledTask timeout;

        Operation(RoutineItem routine) {
            this.routine = routine;
        }
    }

    private final Object lock = new Object();
    private final RepositoryPort repository;
    private final SchedulerPort scheduler;
    private final Listener listener;
    private final LinkedHashMap<String, RoutineItem> routines = new LinkedHashMap<>();
    private final Map<String, RowStatus> statuses = new HashMap<>();
    private final Map<String, Operation> operations = new HashMap<>();
    private final Map<String, ScheduledTask> resetTasks = new HashMap<>();

    private PageStatus pageStatus = PageStatus.LOADING;
    private ScheduledTask offlineCollapseTask;
    private boolean closed;

    public RoutinesController(
            RepositoryPort repository,
            SchedulerPort scheduler,
            Listener listener) {
        if (repository == null) {
            throw new IllegalArgumentException("repository is required");
        }
        if (scheduler == null) {
            throw new IllegalArgumentException("scheduler is required");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener is required");
        }
        this.repository = repository;
        this.scheduler = scheduler;
        this.listener = listener;
    }

    public void start() {
        ViewState loading;
        synchronized (lock) {
            if (closed) {
                return;
            }
            pageStatus = PageStatus.LOADING;
            routines.clear();
            statuses.clear();
            loading = snapshotLocked();
        }
        emit(loading);

        try {
            repository.loadRoutines(this::onLoaded);
        } catch (RuntimeException unavailable) {
            onLoaded(null, "Home Assistant is offline.");
        }
    }

    public void runRoutine(String entityId) {
        final Operation operation;
        final ViewState running;
        synchronized (lock) {
            if (closed || pageStatus != PageStatus.LIVE) {
                return;
            }
            RoutineItem routine = routines.get(entityId);
            if (routine == null || statuses.get(entityId) != RowStatus.READY) {
                return;
            }

            ScheduledTask reset = resetTasks.remove(entityId);
            if (reset != null) {
                reset.cancel();
            }

            operation = new Operation(routine);
            operations.put(entityId, operation);
            statuses.put(entityId, RowStatus.RUNNING);
            running = snapshotLocked();
        }
        emit(running);

        if (operation.routine.type() == RoutineItem.Type.SCRIPT) {
            ScheduledTask timeout = scheduler.schedule(
                    SCRIPT_TIMEOUT_MS,
                    () -> onTimeout(operation));
            boolean keep;
            synchronized (lock) {
                keep = !closed && operations.get(entityId) == operation;
                if (keep) {
                    operation.timeout = timeout;
                }
            }
            if (!keep) {
                cancel(timeout);
                return;
            }
        }

        final RoutinesRepository.Execution execution;
        try {
            execution = repository.run(
                    operation.routine,
                    (success, error) -> onRunResult(operation, success));
        } catch (RuntimeException unavailable) {
            onRunResult(operation, false);
            return;
        }

        boolean keepExecution;
        synchronized (lock) {
            keepExecution = !closed && operations.get(entityId) == operation;
            if (keepExecution) {
                operation.execution = execution;
            }
        }
        if (!keepExecution) {
            cancel(execution);
        }
    }

    public void markOffline() {
        final List<RoutinesRepository.Execution> executions = new ArrayList<>();
        final List<ScheduledTask> timers = new ArrayList<>();
        final boolean retainFailedRows;
        final ViewState offlineState;

        synchronized (lock) {
            if (closed) {
                return;
            }
            pageStatus = PageStatus.OFFLINE;

            if (offlineCollapseTask != null) {
                timers.add(offlineCollapseTask);
                offlineCollapseTask = null;
            }
            timers.addAll(resetTasks.values());
            resetTasks.clear();

            boolean hadFailure = statuses.containsValue(RowStatus.FAILED);
            for (Operation operation : operations.values()) {
                if (operation.execution != null) {
                    executions.add(operation.execution);
                }
                if (operation.timeout != null) {
                    timers.add(operation.timeout);
                }
                if (statuses.get(operation.routine.entityId()) == RowStatus.RUNNING) {
                    statuses.put(operation.routine.entityId(), RowStatus.FAILED);
                    hadFailure = true;
                }
            }
            operations.clear();

            retainFailedRows = hadFailure;
            if (!retainFailedRows) {
                routines.clear();
                statuses.clear();
            }
            offlineState = snapshotLocked();
        }

        cancelExecutions(executions);
        cancelTasks(timers);
        emit(offlineState);

        if (retainFailedRows) {
            ScheduledTask collapse = scheduler.schedule(RESULT_HOLD_MS, this::collapseOfflineRows);
            boolean keep;
            synchronized (lock) {
                keep = !closed
                        && pageStatus == PageStatus.OFFLINE
                        && !routines.isEmpty()
                        && offlineCollapseTask == null;
                if (keep) {
                    offlineCollapseTask = collapse;
                }
            }
            if (!keep) {
                cancel(collapse);
            }
        }
    }

    @Override
    public void close() {
        final List<RoutinesRepository.Execution> executions = new ArrayList<>();
        final List<ScheduledTask> timers = new ArrayList<>();

        synchronized (lock) {
            if (closed) {
                return;
            }
            closed = true;

            for (Operation operation : operations.values()) {
                if (operation.execution != null) {
                    executions.add(operation.execution);
                }
                if (operation.timeout != null) {
                    timers.add(operation.timeout);
                }
            }
            operations.clear();

            timers.addAll(resetTasks.values());
            resetTasks.clear();
            if (offlineCollapseTask != null) {
                timers.add(offlineCollapseTask);
                offlineCollapseTask = null;
            }

            routines.clear();
            statuses.clear();
        }

        cancelExecutions(executions);
        cancelTasks(timers);
    }

    private void onLoaded(List<RoutineItem> items, String error) {
        final ViewState state;
        synchronized (lock) {
            if (closed) {
                return;
            }
            routines.clear();
            statuses.clear();

            if (error != null || items == null) {
                pageStatus = PageStatus.OFFLINE;
                state = snapshotLocked();
            } else {
                pageStatus = PageStatus.LIVE;
                for (RoutineItem item : items) {
                    if (item == null) {
                        continue;
                    }
                    routines.put(item.entityId(), item);
                    statuses.put(item.entityId(), RowStatus.READY);
                }
                state = snapshotLocked();
            }
        }
        emit(state);
    }

    private void onRunResult(Operation operation, boolean success) {
        final ScheduledTask timeout;
        final ViewState resultState;
        final String entityId = operation.routine.entityId();

        synchronized (lock) {
            if (closed || operations.get(entityId) != operation) {
                return;
            }
            operations.remove(entityId);
            timeout = operation.timeout;
            operation.timeout = null;
            statuses.put(entityId, success ? RowStatus.DONE : RowStatus.FAILED);
            resultState = snapshotLocked();
        }

        cancel(timeout);
        emit(resultState);
        scheduleResultReset(entityId);
    }

    private void onTimeout(Operation operation) {
        final RoutinesRepository.Execution execution;
        final ViewState failedState;
        final String entityId = operation.routine.entityId();

        synchronized (lock) {
            if (closed || operations.get(entityId) != operation) {
                return;
            }
            operations.remove(entityId);
            operation.timeout = null;
            execution = operation.execution;
            operation.execution = null;
            statuses.put(entityId, RowStatus.FAILED);
            failedState = snapshotLocked();
        }

        cancel(execution);
        emit(failedState);
        scheduleResultReset(entityId);
    }

    private void scheduleResultReset(String entityId) {
        ScheduledTask task = scheduler.schedule(
                RESULT_HOLD_MS,
                () -> resetResult(entityId));
        ScheduledTask old = null;
        boolean keep;
        synchronized (lock) {
            RowStatus status = statuses.get(entityId);
            keep = !closed
                    && pageStatus == PageStatus.LIVE
                    && (status == RowStatus.DONE || status == RowStatus.FAILED)
                    && !operations.containsKey(entityId);
            if (keep) {
                old = resetTasks.put(entityId, task);
            }
        }
        cancel(old);
        if (!keep) {
            cancel(task);
        }
    }

    private void resetResult(String entityId) {
        final ViewState state;
        synchronized (lock) {
            if (closed || pageStatus != PageStatus.LIVE || operations.containsKey(entityId)) {
                return;
            }
            RowStatus status = statuses.get(entityId);
            if (status != RowStatus.DONE && status != RowStatus.FAILED) {
                return;
            }
            resetTasks.remove(entityId);
            statuses.put(entityId, RowStatus.READY);
            state = snapshotLocked();
        }
        emit(state);
    }

    private void collapseOfflineRows() {
        final ViewState state;
        synchronized (lock) {
            if (closed || pageStatus != PageStatus.OFFLINE) {
                return;
            }
            offlineCollapseTask = null;
            routines.clear();
            statuses.clear();
            state = snapshotLocked();
        }
        emit(state);
    }

    private ViewState snapshotLocked() {
        List<RowState> rows = new ArrayList<>();
        for (Map.Entry<String, RoutineItem> entry : routines.entrySet()) {
            RowStatus status = statuses.get(entry.getKey());
            if (status == null) {
                status = RowStatus.READY;
            }
            boolean enabled = pageStatus == PageStatus.LIVE && status == RowStatus.READY;
            rows.add(new RowState(entry.getValue(), status, enabled));
        }
        return new ViewState(pageStatus, rows);
    }

    private void emit(ViewState state) {
        listener.onViewState(state);
    }

    private static void cancel(RoutinesRepository.Execution execution) {
        if (execution != null) {
            execution.cancel();
        }
    }

    private static void cancel(ScheduledTask task) {
        if (task != null) {
            task.cancel();
        }
    }

    private static void cancelExecutions(List<RoutinesRepository.Execution> executions) {
        for (RoutinesRepository.Execution execution : executions) {
            cancel(execution);
        }
    }

    private static void cancelTasks(List<ScheduledTask> tasks) {
        for (ScheduledTask task : tasks) {
            cancel(task);
        }
    }
}
