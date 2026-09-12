package com.boop.shieldhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ShieldStartupManagerActivity extends Activity {
    private enum Screen { OVERVIEW, PACKAGES, RESTORE }
    private StartupCleanupStore cleanupStore;
    private StartupPreventionStore preventionStore;
    private StartupRestoreStore restoreStore;
    private StartupRecoveryPolicy.RecoveryCapabilities recoveryCapabilities;
    private ExecutorService executor;
    private volatile StartupLocalBridge activeBridge;
    private Screen screen = Screen.OVERVIEW;
    private StartupManagerUiModel.Filter filter = StartupManagerUiModel.Filter.ALL;
    private StartupManagerUiModel.Mode mode = StartupManagerUiModel.Mode.DISABLE;
    private List<StartupPackageState> packages = List.of();
    private final LinkedHashSet<String> restoreSelection = new LinkedHashSet<>();
    private String focusPackage;
    private int focusAction = 1;
    private boolean loadingPackages;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        cleanupStore = new StartupCleanupStore(this);
        preventionStore = new StartupPreventionStore(this);
        restoreStore = new StartupRestoreStore(new AndroidStartupRestoreBackend(this), System::currentTimeMillis);
        recoveryCapabilities = AndroidRecoveryCapabilities.resolve(this);
        executor = Executors.newSingleThreadExecutor();
        render();
    }

    private void render() {
        ShieldStartupManagerView view = new ShieldStartupManagerView(this);
        ShieldStartupManagerView.Callbacks callbacks = callbacks();
        if (screen == Screen.OVERVIEW) {
            view.renderOverview(new StartupLocalBridge(this).hasIdentity(), cleanupStore.autoEnabled(),
                    restoreStore.records().size(), cleanupStore.lastSummary(), callbacks);
        } else if (screen == Screen.PACKAGES) {
            List<StartupPackageState> visible = StartupManagerUiModel.filter(packages, filter);
            view.renderPackages(visible, filter, mode, recoveryCapabilities, focusPackage, focusAction, callbacks);
        } else {
            view.renderRestore(restoreStore.records(), Set.copyOf(restoreSelection), focusPackage, callbacks);
        }
        setContentView(view);
    }

    private ShieldStartupManagerView.Callbacks callbacks() {
        return new ShieldStartupManagerView.Callbacks() {
            @Override public void onOpenPackages(StartupManagerUiModel.Mode requested) {
                mode = requested;
                filter = StartupManagerUiModel.Filter.ALL;
                screen = Screen.PACKAGES;
                focusPackage = null;
                focusAction = requested == StartupManagerUiModel.Mode.BOOT_CLEAN ? 2
                        : requested == StartupManagerUiModel.Mode.BACKGROUND ? 3 : 1;
                render();
                loadPackages();
            }
            @Override public void onOpenRestore() { screen = Screen.RESTORE; focusPackage = null; render(); }
            @Override public void onCheckLocalLink() { authorize(); }
            @Override public void onRunNow() { runCleanupNow(); }
            @Override public void onFilter(StartupManagerUiModel.Filter requested) {
                filter = requested;
                if (focusPackage != null && StartupManagerUiModel.filter(packages, filter).stream()
                        .noneMatch(p -> p.packageName().equals(focusPackage))) focusPackage = null;
                render();
            }
            @Override public void onPackageInfo(String packageName) { showPackageInfo(packageName); }
            @Override public void onPrimary(String packageName) { primary(packageName); }
            @Override public void onToggleBoot(String packageName, boolean enabled) {
                packageAction(packageName, controller -> controller.setBootClean(packageName, enabled), result -> {
                    if (result.success() && enabled) cleanupStore.setAutoEnabled(true);
                });
            }
            @Override public void onToggleBackground(String packageName, boolean enabled) {
                packageAction(packageName, controller -> controller.setBackgroundBlock(packageName, enabled), null);
            }
            @Override public void onForceStop(String packageName) {
                packageAction(packageName, controller -> controller.forceStop(packageName), null);
            }
            @Override public void onProtected(String packageName, String reason) {
                Toast.makeText(ShieldStartupManagerActivity.this,
                        reason == null || reason.isBlank() ? "Protected recovery package." : reason,
                        Toast.LENGTH_LONG).show();
            }
            @Override public void onPackageFocus(String packageName, int actionIndex) {
                focusPackage = packageName; focusAction = actionIndex;
            }
            @Override public void onToggleRestoreSelection(String packageName, boolean selected) {
                if (selected) restoreSelection.add(packageName); else restoreSelection.remove(packageName);
                focusPackage = packageName; render();
            }
            @Override public void onRestoreOne(String packageName) {
                focusPackage = packageName;
                packageAction(packageName, controller -> controller.restore(packageName), result -> {
                    if (result.success()) restoreSelection.remove(packageName);
                });
            }
            @Override public void onRestoreSelected() { restoreSelected(); }
            @Override public void onBack() { handleBack(); }
        };
    }

    private void primary(String packageName) {
        StartupPackageState state = findPackage(packageName);
        if (state == null) return;
        if (StartupManagerUiModel.isDisabled(state)) {
            packageAction(packageName, controller -> controller.reenable(packageName), null);
            return;
        }
        StartupRecoveryPolicy.Assessment assessment = StartupRecoveryPolicy.assess(state, recoveryCapabilities);
        if (assessment.impact() == StartupRecoveryPolicy.Impact.HIGH) {
            new AlertDialog.Builder(this)
                    .setTitle("Disable " + state.label() + "?")
                    .setMessage("This is a high-impact package. Disabling it may remove stock Home behavior, recommendations, or related features. BOOP keeps recovery-critical packages protected and records an exact Restore baseline.")
                    .setPositiveButton("Disable", (dialog, which) ->
                            packageAction(packageName, controller -> controller.disable(packageName), null))
                    .setNegativeButton("Cancel", null).show();
        } else {
            packageAction(packageName, controller -> controller.disable(packageName), null);
        }
    }

    private void showPackageInfo(String packageName) {
        StartupPackageState state = findPackage(packageName);
        if (state == null) return;
        StartupRecoveryPolicy.Assessment assessment = StartupRecoveryPolicy.assess(state, recoveryCapabilities);
        String message = state.packageName() + "\n\n"
                + (state.systemApp() ? "System package" : "User package")
                + (state.launcher() ? " • Launcher" : "")
                + "\nCurrent state: " + state.enabledState()
                + "\nImpact: " + assessment.impact()
                + (assessment.protectionReason() == null ? "" : "\n\nProtected because: " + assessment.protectionReason());
        new AlertDialog.Builder(this).setTitle(state.label()).setMessage(message)
                .setPositiveButton("OK", null).show();
    }

    private StartupPackageState findPackage(String packageName) {
        for (StartupPackageState state : packages) if (state.packageName().equals(packageName)) return state;
        return null;
    }

    private void loadPackages() {
        if (loadingPackages) return;
        loadingPackages = true;
        executor.execute(() -> {
            StartupLocalBridge bridge = new StartupLocalBridge(this);
            activeBridge = bridge;
            try (StartupLocalBridge.PackageSession session = bridge.openPackageSession(true, this::showApprovalPrompt)) {
                ensureMigration(session);
                List<StartupPackageState> loaded = session.inventory();
                runOnUiThread(() -> { packages = loaded; loadingPackages = false; render(); });
            } catch (Exception failure) {
                loadingPackages = false;
                showFailure(failure);
            } finally { activeBridge = null; }
        });
    }

    private void authorize() {
        executor.execute(() -> {
            StartupLocalBridge bridge = new StartupLocalBridge(this);
            activeBridge = bridge;
            try {
                String result = bridge.authorize(this::showApprovalPrompt);
                cleanupStore.recordSummary(result);
                runOnUiThread(this::render);
            } catch (Exception failure) { showFailure(failure); }
            finally { activeBridge = null; }
        });
    }

    private void runCleanupNow() {
        Set<String> targets = cleanupStore.targets();
        if (targets.isEmpty()) {
            Toast.makeText(this, "No Boot close packages selected.", Toast.LENGTH_SHORT).show();
            return;
        }
        executor.execute(() -> {
            StartupLocalBridge bridge = new StartupLocalBridge(this);
            activeBridge = bridge;
            try {
                String result = bridge.run(targets, true, this::showApprovalPrompt);
                cleanupStore.recordSummary(result);
                runOnUiThread(this::render);
            } catch (Exception failure) { showFailure(failure); }
            finally { activeBridge = null; }
        });
    }

    private interface ControllerAction { StartupPackageController.Result run(StartupPackageController controller); }
    private interface ResultHook { void apply(StartupPackageController.Result result); }

    private void packageAction(String packageName, ControllerAction action, ResultHook hook) {
        executor.execute(() -> {
            StartupLocalBridge bridge = new StartupLocalBridge(this);
            activeBridge = bridge;
            try (StartupLocalBridge.PackageSession session = bridge.openPackageSession(true, this::showApprovalPrompt)) {
                ensureMigration(session);
                StartupPackageController controller = new StartupPackageController(
                        session, bootStore(), restoreStore, recoveryCapabilities);
                StartupPackageController.Result result = action.run(controller);
                if (hook != null) hook.apply(result);
                cleanupStore.recordSummary(result.summary());
                List<StartupPackageState> loaded = session.inventory();
                runOnUiThread(() -> {
                    packages = loaded;
                    Toast.makeText(this, result.summary(), result.success() ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
                    render();
                });
            } catch (Exception failure) { showFailure(failure); }
            finally { activeBridge = null; }
        });
    }

    private StartupPackageController.BootStore bootStore() {
        return new StartupPackageController.BootStore() {
            @Override public boolean setTarget(String packageName, boolean enabled) {
                return cleanupStore.setTarget(packageName, enabled);
            }
            @Override public boolean contains(String packageName) { return cleanupStore.targets().contains(packageName); }
        };
    }

    private void restoreSelected() {
        if (restoreSelection.isEmpty()) {
            Toast.makeText(this, "Choose at least one package to restore.", Toast.LENGTH_SHORT).show();
            return;
        }
        Set<String> requested = Set.copyOf(restoreSelection);
        executor.execute(() -> {
            StartupLocalBridge bridge = new StartupLocalBridge(this);
            activeBridge = bridge;
            try (StartupLocalBridge.PackageSession session = bridge.openPackageSession(true, this::showApprovalPrompt)) {
                ensureMigration(session);
                StartupPackageController controller = new StartupPackageController(session, bootStore(), restoreStore, recoveryCapabilities);
                ArrayList<String> failed = new ArrayList<>();
                int restored = 0;
                for (String pkg : requested) {
                    StartupPackageController.Result result = controller.restore(pkg);
                    if (result.success()) { restored++; restoreSelection.remove(pkg); }
                    else failed.add(pkg + ": " + result.summary());
                }
                String summary = restored + " restored" + (failed.isEmpty() ? "." : "; " + failed.size() + " not restored. " + String.join("; ", failed));
                cleanupStore.recordSummary(summary);
                List<StartupPackageState> loaded = session.inventory();
                runOnUiThread(() -> { packages = loaded; Toast.makeText(this, summary, failed.isEmpty() ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show(); render(); });
            } catch (Exception failure) { showFailure(failure); }
            finally { activeBridge = null; }
        });
    }

    private void ensureMigration(StartupLocalBridge.PackageSession session) throws Exception {
        AndroidStartupManagerMigrationMarker marker = new AndroidStartupManagerMigrationMarker(this);
        if (marker.migrated()) return;
        StartupManagerMigration.LegacySource legacy = new StartupManagerMigration.LegacySource() {
            @Override public Set<String> cleanupTargets() { return cleanupStore.targets(); }
            @Override public Set<StartupPreventionRecord> preventionRecords() { return preventionStore.records(); }
        };
        StartupManagerMigration.Result result = new StartupManagerMigration(
                legacy, session::probe, restoreStore, marker).runOnce();
        if (!result.success()) throw new IOException("Could not import existing Startup Manager state: " + result.summary());
    }

    private void showApprovalPrompt() {
        runOnUiThread(() -> Toast.makeText(this, "Approve BOOP on the Shield.", Toast.LENGTH_LONG).show());
    }

    private void showFailure(Exception failure) {
        String message = failure.getMessage();
        if (message == null || message.isBlank()) message = failure.getClass().getSimpleName();
        final String safe = message.length() > 220 ? message.substring(0, 220) : message;
        cleanupStore.recordSummary("Not applied: " + safe);
        runOnUiThread(() -> { Toast.makeText(this, safe, Toast.LENGTH_LONG).show(); render(); });
    }

    private void handleBack() {
        if (screen == Screen.OVERVIEW) { finish(); return; }
        screen = Screen.OVERVIEW; filter = StartupManagerUiModel.Filter.ALL; focusPackage = null; render();
    }

    @Override public void onBackPressed() { handleBack(); }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
            screen = Screen.RESTORE; focusPackage = null; render(); return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override protected void onDestroy() {
        if (activeBridge != null) activeBridge.cancel();
        if (executor != null) executor.shutdownNow();
        super.onDestroy();
    }
}