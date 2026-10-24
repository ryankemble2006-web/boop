package com.boop.shieldhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class ShieldStartupManagerActivity extends Activity {
    private enum Screen { OVERVIEW, PACKAGES, RESTORE, DEFAULTS, DEFAULTS_UNDO }
    private StartupCleanupStore cleanup;
    private StartupPreventionStore prevention;
    private StartupRestoreStore restores;
    private ExecutorService executor;
    private Future<?> task;
    private volatile StartupLocalBridge activeBridge;
    private final StartupActionGate gate = new StartupActionGate();
    private Screen screen = Screen.PACKAGES, restoreReturn = Screen.PACKAGES;
    private StartupManagerUiModel.Filter filter = StartupManagerUiModel.Filter.ALL;
    private StartupManagerUiModel.Mode mode = StartupManagerUiModel.Mode.DISABLE;
    private List<StartupPackageState> packages = List.of();
    private final Set<String> selected = new LinkedHashSet<>();
    private final Set<String> ripSelected = new LinkedHashSet<>();
    private String packageFocus, restoreFocus, focusTag, status = "";
    private int actionFocus;
    private ShieldStartupManagerView currentView;
    private StartupDefaultsJournal defaultsJournal;
    private StartupDefaultsCoordinator.Preview defaultsPreview;
    private StartupDefaultsCoordinator.UndoPlan defaultsUndo;
    private final Set<String> defaultsSelected = new LinkedHashSet<>();

    @Override protected void onCreate(Bundle state) {
        BoopTvChrome.applyShieldMetrics(this);
        super.onCreate(state);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        cleanup = new StartupCleanupStore(this);
        prevention = new StartupPreventionStore(this);
        restores = new StartupRestoreStore(new AndroidStartupRestoreBackend(this), System::currentTimeMillis);
        executor = Executors.newSingleThreadExecutor();
        status = cleanup.lastSummary();
        defaultsJournal = new StartupDefaultsJournal(new AndroidStartupDefaultsBackend(this));
        if (state != null) {
            try {
                screen = Screen.valueOf(state.getString("screen", "PACKAGES"));
                mode = StartupManagerUiModel.Mode.valueOf(state.getString("mode", "DISABLE"));
                filter = StartupManagerUiModel.Filter.valueOf(state.getString("filter", "ALL"));
                packageFocus = state.getString("package");
                actionFocus = state.getInt("action", 0);
                ArrayList<String> values = state.getStringArrayList("selected");
                if (values != null) selected.addAll(values);
                ArrayList<String> rips = state.getStringArrayList("ripSelected");
                if (rips != null) ripSelected.addAll(rips);
            } catch (IllegalArgumentException ignored) {
                screen = Screen.PACKAGES;
            }
        }
        if (screen == Screen.OVERVIEW || screen == Screen.DEFAULTS || screen == Screen.DEFAULTS_UNDO) {
            screen = Screen.PACKAGES;
        }
        render();
        loadPackages();
    }

    @Override protected void onResume() {
        super.onResume();
        BoopTvChrome.applyShieldMetrics(this);
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        out.putString("screen", screen.name());
        out.putString("mode", mode.name());
        out.putString("filter", filter.name());
        out.putString("package", packageFocus);
        out.putInt("action", actionFocus);
        out.putStringArrayList("selected", new ArrayList<>(selected));
        out.putStringArrayList("ripSelected", new ArrayList<>(ripSelected));
        super.onSaveInstanceState(out);
    }

    private void updateStatusOnly(String message, boolean working) {
        status = message == null ? "" : message;
        if (currentView != null) {
            currentView.setStatus(status, working);
        }
    }

    private void render() {
        if (isFinishing() || isDestroyed()) return;
        BoopTvChrome.applyShieldMetrics(this);
        if (screen == Screen.DEFAULTS && defaultsPreview != null) {
            currentView = null;
            setContentView(new StartupDefaultsScreen(this, defaultsPreview.rows(), defaultsSelected, false, gate.busy(), status,
                    !defaultsPreview.auto(), this::applyBoopDefaults, this::handleBack, null));
            return;
        }
        if (screen == Screen.DEFAULTS_UNDO && defaultsUndo != null) {
            currentView = null;
            setContentView(new StartupDefaultsScreen(this, defaultsUndo.rows(), Set.of(), true, gate.busy(), status,
                    defaultsUndo.batch() != null && defaultsUndo.batch().autoArmed(), this::undoBoopDefaults, this::handleBack,
                    defaultsUndo.batch() == null ? null : this::keepBoopDefaults));
            return;
        }
        ShieldStartupManagerView view = new ShieldStartupManagerView(this);
        currentView = view;
        view.setStatus(status, gate.busy());
        view.setPackageLabels(packages);
        var cb = callbacks();
        if (screen == Screen.RESTORE) {
            Set<String> remaining = new LinkedHashSet<>();
            for (var record : restores.records()) remaining.add(record.packageName());
            selected.retainAll(remaining);
            view.renderRestore(restores.records(), Set.copyOf(selected), restoreFocus, cb);
        } else {
            view.renderPackages(
                    StartupManagerUiModel.filter(packages, filter),
                    packages,
                    filter,
                    mode,
                    AndroidRecoveryCapabilities.resolve(this),
                    Set.copyOf(ripSelected),
                    packageFocus,
                    actionFocus,
                    cb);
        }
        setContentView(view);
        if (focusTag != null) {
            String target = focusTag;
            view.post(() -> {
                View focus = view.findViewWithTag(target);
                if (focus != null) focus.requestFocus();
            });
        }
    }

    private ShieldStartupManagerView.Callbacks callbacks() {
        return new ShieldStartupManagerView.Callbacks() {
            public void onOpenOverview() { handleBack(); }
            public void onOpenPackages(StartupManagerUiModel.Mode requested) {
                if (gate.busy()) cancelWork();
                mode = requested;
                screen = Screen.PACKAGES;
                focusTag = null;
                render();
                loadPackages();
            }
            public void onOpenRestore() { openRestore(); }
            public void onCheckLocalLink() {
                perform("Checking the local connection", session -> new Outcome(null, "Local control connection verified.", null));
            }
            public void onRunNow() { runCleanup(); }
            public void onRefresh() { loadPackages(); }
            public void onBoopDefaults(boolean undo) { openBoopDefaults(undo); }
            public void onSetAuto(boolean enabled) {
                if (!enabled) {
                    cancelWork();
                    boolean saved = cleanup.setAutoEnabled(false);
                    updateStatusOnly(saved ? "Automatic boot cleanup is OFF." : "Could not save boot cleanup.", false);
                    return;
                }
                perform("Checking boot cleanup", session -> {
                    if (!cleanup.setAutoEnabled(true) || !cleanup.autoEnabled()) throw new IOException("Could not save boot cleanup.");
                    return new Outcome(null, "Automatic boot cleanup is ON.", null);
                });
            }
            public void onFilter(StartupManagerUiModel.Filter value) {
                filter = value;
                focusTag = "startup:filter:" + value;
                render();
            }
            public void onPackageInfo(String pkg) { inspect(pkg); }
            public void onPrimary(String pkg) { primary(pkg); }
            public void onToggleBoot(String pkg, boolean enabled) { act(pkg, c -> c.setBootClean(pkg, enabled)); }
            public void onToggleBackground(String pkg, boolean enabled) { act(pkg, c -> c.setBackgroundBlock(pkg, enabled)); }
            public void onForceStop(String pkg) { act(pkg, c -> c.forceStop(pkg)); }
            public void onProtected(String pkg, String reason) {
                updateStatusOnly(reason, gate.busy());
            }
            public void onPackageFocus(String pkg, int action) {
                packageFocus = pkg;
                actionFocus = action;
                focusTag = null;
            }
            public void onToggleRipSelect(String pkg, boolean checked) {
                if (gate.busy()) return;
                StartupPackageState row = cached(pkg);
                var caps = AndroidRecoveryCapabilities.resolve(ShieldStartupManagerActivity.this);
                if (row == null || !StartupRecoveryPolicy.canUninstall(row, caps)) {
                    ripSelected.remove(pkg);
                    updateStatusOnly("System apps are protected from uninstallation.", false);
                    return;
                }
                if (checked) ripSelected.add(pkg);
                else ripSelected.remove(pkg);
                packageFocus = pkg;
                actionFocus = 1;
                focusTag = null;
                // Update status text in-place without calling render()!
                updateStatusOnly(ripSelected.isEmpty()
                        ? "Selection cleared."
                        : ripSelected.size() + (ripSelected.size() == 1 ? " user app" : " user apps") + " marked for silent rip-out.",
                        false);
            }
            public void onSelectAllUser(boolean selectAll) {
                if (gate.busy()) return;
                if (!selectAll) {
                    ripSelected.clear();
                    focusTag = "startup:packages:clear-all";
                    status = "Rip-out selection cleared.";
                    render();
                    return;
                }
                var caps = AndroidRecoveryCapabilities.resolve(ShieldStartupManagerActivity.this);
                for (var row : StartupManagerUiModel.filter(packages, filter)) {
                    if (StartupRecoveryPolicy.canUninstall(row, caps)) {
                        ripSelected.add(row.packageName());
                    }
                }
                focusTag = "startup:packages:rip-out";
                status = ripSelected.size() + " user app(s) marked for silent rip-out.";
                render();
            }
            public void onRipOutSelected() { ripOutSelected(); }
            public void onToggleRestoreSelection(String pkg, boolean checked) {
                if (gate.busy()) return;
                if (checked) selected.add(pkg);
                else selected.remove(pkg);
                restoreFocus = pkg;
                render();
            }
            public void onRestoreOne(String pkg) { prepareRestore(Set.of(pkg)); }
            public void onRestoreSelected() { prepareRestore(Set.copyOf(selected)); }
            public void onBack() { handleBack(); }
        };
    }

    private void ripOutSelected() {
        if (gate.busy()) return;
        if (ripSelected.isEmpty()) {
            updateStatusOnly("Tick Rip Out on one or more user apps first.", false);
            return;
        }
        var caps = AndroidRecoveryCapabilities.resolve(this);
        final List<String> queue = new ArrayList<>();
        for (String pkg : ripSelected) {
            StartupPackageState row = cached(pkg);
            if (row != null && StartupRecoveryPolicy.canUninstall(row, caps)) {
                queue.add(pkg);
            }
        }
        if (queue.isEmpty()) {
            ripSelected.clear();
            updateStatusOnly("Only user-installed apps can be uninstalled.", false);
            render();
            return;
        }
        perform("Silently ripping out " + queue.size() + (queue.size() == 1 ? " user app..." : " user apps..."), session -> {
            ensureMigration(session);
            List<String> removed = new ArrayList<>();
            List<String> failed = new ArrayList<>();
            for (int i = 0; i < queue.size(); i++) {
                String pkg = queue.get(i);
                StartupPackageState cachedRow = cached(pkg);
                String name = cachedRow != null ? cachedRow.label() : pkg;
                final int index = i + 1;
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        updateStatusOnly("Ripping out (" + index + "/" + queue.size() + "): " + name + "...", true);
                    }
                });
                try {
                    session.uninstall(pkg);
                    cleanup.setTarget(pkg, false);
                    restores.remove(pkg);
                    removed.add(pkg);
                } catch (Exception err) {
                    failed.add(name + " (" + safeMessage(err) + ")");
                }
            }
            List<StartupPackageState> updated = session.inventory();
            String summary = "Silently ripped out " + removed.size() + (removed.size() == 1 ? " app." : " apps.")
                    + (failed.isEmpty() ? " Zero clicking required." : " Skipped: " + String.join("; ", failed));
            return new Outcome(updated, summary, () -> {
                ripSelected.removeAll(removed);
                render();
            });
        });
    }

    private StartupDefaultsCoordinator defaultsController(StartupLocalBridge.PackageSession session) {
        return new StartupDefaultsCoordinator(new AndroidStartupDefaultsEnvironment(this, session), defaultsJournal);
    }

    private void openBoopDefaults(boolean undo) {
        if (gate.busy()) return;
        try { if (defaultsJournal.load() != null) undo = true; }
        catch (RuntimeException invalid) { status = safeMessage(invalid); render(); return; }
        final boolean showUndo = undo;
        perform(showUndo ? "Reading saved BOOP defaults" : "Checking BOOP defaults on this Shield", session -> {
            var coordinator = defaultsController(session);
            if (showUndo) {
                var review = coordinator.previewUndo();
                return new Outcome(null, "Review before Undo.", () -> {
                    defaultsUndo = review;
                    screen = Screen.DEFAULTS_UNDO;
                    focusTag = null;
                    render();
                });
            }
            var review = coordinator.preview();
            return new Outcome(null, "Opening this review does not change any packages.", () -> {
                defaultsPreview = review;
                defaultsSelected.clear();
                for (var row : review.rows()) if (row.available()) defaultsSelected.add(row.profile().packageName());
                screen = Screen.DEFAULTS;
                focusTag = null;
                render();
            });
        });
    }

    private void applyBoopDefaults() {
        if (gate.busy() || defaultsPreview == null || defaultsSelected.isEmpty()) return;
        var review = defaultsPreview;
        Set<String> selection = Set.copyOf(defaultsSelected);
        perform("Applying selected BOOP defaults. Back stops remaining changes.", session -> {
            ensureMigration(session);
            var result = defaultsController(session).apply(review, selection, () -> Thread.currentThread().isInterrupted());
            return defaultsOutcome(result, session);
        });
    }

    private void undoBoopDefaults() {
        if (gate.busy() || defaultsUndo == null) return;
        var review = defaultsUndo;
        perform("Undoing only BOOP defaults changes", session -> {
            var result = defaultsController(session).undo(review, () -> Thread.currentThread().isInterrupted());
            return defaultsOutcome(result, session);
        });
    }

    private Outcome defaultsOutcome(StartupDefaultsCoordinator.Result result, StartupLocalBridge.PackageSession session) {
        List<StartupPackageState> rows = null;
        try { rows = session.inventory(); } catch (Exception ignored) { }
        return new Outcome(rows, result.summary(), () -> {
            screen = Screen.PACKAGES;
            defaultsPreview = null;
            defaultsUndo = null;
            focusTag = null;
            render();
        });
    }

    private void keepBoopDefaults() {
        if (gate.busy() || defaultsUndo == null || defaultsUndo.batch() == null) return;
        var reviewed = defaultsUndo.batch();
        dialog("Keep current settings?", "No package settings will change. This forgets the grouped BOOP defaults Undo only.", "Keep settings", () -> {
            if (gate.busy()) return;
            try {
                new StartupDefaultsCoordinator(null, defaultsJournal).keepCurrent(reviewed);
                screen = Screen.PACKAGES;
                status = "Current settings kept.";
                focusTag = null;
                render();
            } catch (RuntimeException failure) {
                status = safeMessage(failure);
                render();
            }
        });
    }

    private interface Action { StartupPackageController.Result apply(StartupPackageController controller); }
    private interface Work { Outcome run(StartupLocalBridge.PackageSession session) throws Exception; }
    private record Outcome(List<StartupPackageState> rows, String message, Runnable afterRender) { }

    private void perform(String message, Work work) {
        long token = gate.begin();
        if (token < 0) return;
        if (currentView != null && !packages.isEmpty()) {
            updateStatusOnly(message, true);
        } else {
            status = message;
            render();
        }
        task = executor.submit(() -> {
            StartupLocalBridge bridge = new StartupLocalBridge(getApplicationContext());
            activeBridge = bridge;
            if (!gate.current(token)) {
                bridge.cancel();
                return;
            }
            try (StartupLocalBridge.PackageSession session = bridge.openPackageSession(true, () -> runOnUiThread(() -> {
                if (gate.current(token)) {
                    updateStatusOnly("Approve BOOP's local connection on the Shield.", true);
                }
            }))) {
                if (!gate.current(token)) return;
                Outcome result = work.run(session);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed() || !gate.complete(token)) return;
                    if (result.rows() != null) replacePackages(result.rows());
                    status = result.message() == null ? "" : result.message();
                    if (!status.isEmpty()) cleanup.recordSummary(status);
                    render();
                    if (result.afterRender() != null) result.afterRender().run();
                });
            } catch (Exception | LinkageError failure) {
                Log.w("BOOP-Startup", "Package action did not finish", failure);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed() || !gate.complete(token)) return;
                    status = failure instanceof LinkageError ? "This build could not open that function on this Shield." : safeMessage(failure);
                    cleanup.recordSummary(status);
                    render();
                });
            } finally {
                if (activeBridge == bridge) activeBridge = null;
            }
        });
    }

    private void replacePackages(List<StartupPackageState> rows) {
        List<String> oldIds = new ArrayList<>(), newIds = new ArrayList<>();
        var caps = AndroidRecoveryCapabilities.resolve(this);
        Set<String> allValidUninstallIds = new LinkedHashSet<>();
        for (var row : rows) {
            if (StartupRecoveryPolicy.canUninstall(row, caps)) {
                allValidUninstallIds.add(row.packageName());
            }
        }
        ripSelected.retainAll(allValidUninstallIds);

        for (var row : StartupManagerUiModel.filter(packages, filter)) oldIds.add(row.packageName());
        for (var row : StartupManagerUiModel.filter(rows, filter)) newIds.add(row.packageName());
        var current = new StartupManagerNav.State(StartupManagerNav.Screen.PACKAGES, filter.ordinal(), packageFocus, actionFocus, oldIds);
        var next = StartupManagerNav.reconcile(current, newIds);
        packageFocus = next.focusedPackageId();
        actionFocus = Math.min(1, next.actionIndex());
        packages = rows;
    }

    private void loadPackages() {
        perform("Reading installed packages...", session -> {
            String migration = "";
            try { ensureMigration(session); }
            catch (Exception failed) { migration = "Existing rules kept unchanged. " + safeMessage(failed); }
            List<StartupPackageState> rows = session.inventory();
            return new Outcome(rows, migration.isEmpty() ? rows.size() + " installed packages ready." : migration, null);
        });
    }

    private StartupPackageState cached(String pkg) {
        for (var row : packages) if (row.packageName().equals(pkg)) return row;
        return null;
    }

    private void primary(String pkg) {
        StartupPackageState displayed = cached(pkg);
        if (displayed == null || gate.busy()) return;
        boolean enable = StartupManagerUiModel.isDisabled(displayed);
        actionFocus = 0;
        packageFocus = pkg;
        perform((enable ? "Enabling " : "Disabling ") + displayed.label() + "...", session -> {
            ensureMigration(session);
            StartupPackageState live = session.probe(pkg);
            if (enable != StartupManagerUiModel.isDisabled(live)) {
                return new Outcome(session.inventory(), "That app's state changed. Try again.", null);
            }
            var risk = StartupRecoveryPolicy.assess(live, AndroidRecoveryCapabilities.resolve(this));
            if (!enable && risk.protectedPackage()) {
                return new Outcome(null, risk.protectionReason(), null);
            }
            var c = controller(session);
            var res = enable ? c.reenable(pkg) : c.disable(pkg);
            return refreshed(session, res.summary());
        });
    }

    private void act(String pkg, Action action) {
        perform("Applying package change", session -> {
            ensureMigration(session);
            var result = action.apply(controller(session));
            return refreshed(session, result.summary());
        });
    }

    private Outcome refreshed(StartupLocalBridge.PackageSession session, String message) {
        try { return new Outcome(session.inventory(), message, null); }
        catch (Exception unavailable) { return new Outcome(null, message + " Use Refresh to update the list.", null); }
    }

    private void inspect(String pkg) {
        perform("Reading current package state", session -> {
            StartupPackageState live = session.probe(pkg);
            StartupRestoreRecord record = restores.record(pkg);
            var risk = StartupRecoveryPolicy.assess(live, AndroidRecoveryCapabilities.resolve(this));
            String text = pkg + "\n\nCurrent enabled state: " + live.enabledState()
                    + "\nBackground: " + live.runInBackgroundMode() + " / " + live.runAnyInBackgroundMode()
                    + "\nClose after boot: " + (cleanup.targets().contains(pkg) ? "ON" : "OFF")
                    + (risk.protectedPackage() ? "\n\n" + risk.protectionReason() : "")
                    + (record != null && record.drifted(live) ? "\n\nChanged outside BOOP. Restore still has the original." : "");
            return new Outcome(null, "Current package state verified.", () -> dialog(live.label(), text, record == null ? "OK" : "Restore", record == null ? () -> { } : () -> prepareRestore(Set.of(pkg))));
        });
    }

    private void prepareRestore(Set<String> requested) {
        if (requested.isEmpty()) { updateStatusOnly("Select at least one package to restore.", false); return; }
        perform("Checking saved originals", session -> {
            ensureMigration(session);
            Map<String, StartupPackageState> expected = new LinkedHashMap<>();
            boolean drift = false;
            for (String pkg : requested) {
                var live = session.probe(pkg);
                var receipt = restores.record(pkg);
                if (receipt == null) throw new IOException("No saved original for " + pkg);
                expected.put(pkg, live);
                drift |= receipt.drifted(live);
            }
            String message = (drift ? "Changed outside BOOP or an earlier action was interrupted.\n\n" : "")
                    + "Restore " + requested.size() + " package(s) to their saved enabled, background and boot-cleanup settings?";
            return new Outcome(null, "", () -> dialog("Restore saved settings?", message, "Restore", () -> restoreBatch(expected)));
        });
    }

    private void restoreBatch(Map<String, StartupPackageState> expected) {
        perform("Restoring saved settings", session -> {
            List<String> completed = new ArrayList<>(), failed = new ArrayList<>();
            var c = controller(session);
            for (var entry : expected.entrySet()) {
                var current = session.probe(entry.getKey());
                if (!sameState(entry.getValue(), current)) { failed.add(entry.getKey() + ": state changed; review again"); continue; }
                var result = c.restore(entry.getKey());
                if (result.success()) completed.add(entry.getKey());
                else failed.add(entry.getKey() + ": " + result.summary());
            }
            String message = completed.size() + " restored." + (failed.isEmpty() ? "" : " " + failed.size() + " need attention. " + String.join("; ", failed));
            List<StartupPackageState> rows = session.inventory();
            return new Outcome(rows, message, () -> {
                selected.removeAll(completed);
                if (completed.contains(restoreFocus)) restoreFocus = null;
                render();
            });
        });
    }

    private static boolean sameState(StartupPackageState a, StartupPackageState b) {
        return a.packageName().equals(b.packageName()) && a.enabledState().equals(b.enabledState())
                && a.runInBackgroundMode().equals(b.runInBackgroundMode()) && a.runAnyInBackgroundMode().equals(b.runAnyInBackgroundMode())
                && a.managedActions().contains(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN) == b.managedActions().contains(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN);
    }

    private StartupPackageController controller(StartupLocalBridge.PackageSession session) {
        return new StartupPackageController(session, new StartupPackageController.BootStore() {
            public boolean setTarget(String pkg, boolean enabled) { return cleanup.setTarget(pkg, enabled); }
            public boolean contains(String pkg) { return cleanup.targets().contains(pkg); }
        }, restores, AndroidRecoveryCapabilities.resolve(this));
    }

    private void ensureMigration(StartupLocalBridge.PackageSession session) throws Exception {
        var marker = new AndroidStartupManagerMigrationMarker(this);
        if (marker.migrated()) return;
        var legacy = new StartupManagerMigration.LegacySource() {
            public Set<String> cleanupTargets() { return cleanup.targets(); }
            public Set<StartupPreventionRecord> preventionRecords() { return prevention.records(); }
        };
        var result = new StartupManagerMigration(legacy, session::probe, restores, marker).runOnce();
        if (!result.success()) throw new IOException("Existing rules could not be imported: " + result.summary());
    }

    private void runCleanup() {
        Set<String> targets = cleanup.targets();
        if (targets.isEmpty()) { updateStatusOnly("No packages are selected for boot cleanup.", false); return; }
        perform("Closing selected packages", session -> {
            int stopped = 0, skipped = 0;
            for (String pkg : targets) {
                if (!session.canCleanNow(pkg)) { skipped++; continue; }
                var result = controller(session).forceStop(pkg);
                if (result.success()) stopped++;
                else skipped++;
            }
            return refreshed(session, stopped + " stopped; " + skipped + " left unchanged.");
        });
    }

    private void dialog(String title, String message, String positive, Runnable action) {
        if (isFinishing() || isDestroyed()) return;
        AlertDialog dialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle(title).setMessage(message).setPositiveButton(positive, (d, w) -> action.run())
                .setNegativeButton("Back", null).create();
        dialog.setOnShowListener(ignored -> {
            if (dialog.getWindow() != null) dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.65), WindowManager.LayoutParams.WRAP_CONTENT);
            TextView content = dialog.findViewById(android.R.id.message);
            if (content != null) content.setTextSize(18);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(18);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).requestFocus();
        });
        dialog.show();
    }

    private static String safeMessage(Throwable e) {
        String m = e.getMessage();
        if (m == null || m.trim().isEmpty()) return "The action could not be completed.";
        m = m.replace('\n', ' ').replace('\r', ' ');
        return m.substring(0, Math.min(240, m.length()));
    }

    private void cancelWork() {
        if (gate.busy()) status = "Stopped waiting.";
        gate.cancel();
        if (activeBridge != null) activeBridge.cancel();
        if (task != null) task.cancel(true);
        task = null;
    }

    private void openRestore() {
        cancelWork();
        if (screen != Screen.RESTORE) restoreReturn = screen;
        screen = Screen.RESTORE;
        focusTag = null;
        render();
    }

    private void handleBack() {
        cancelWork();
        focusTag = null;
        if (screen == Screen.PACKAGES || screen == Screen.OVERVIEW) {
            finish();
            return;
        }
        screen = Screen.PACKAGES;
        render();
    }

    @Override public void onBackPressed() { handleBack(); }

    @Override public boolean onKeyDown(int key, KeyEvent event) {
        if (key == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
            openRestore();
            return true;
        }
        return super.onKeyDown(key, event);
    }

    @Override protected void onDestroy() {
        cancelWork();
        if (executor != null) executor.shutdownNow();
        super.onDestroy();
    }
}
