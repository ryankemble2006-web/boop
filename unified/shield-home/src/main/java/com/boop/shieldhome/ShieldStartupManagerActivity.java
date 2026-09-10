package com.boop.shieldhome;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ShieldStartupManagerActivity extends Activity {
    private StartupCleanupStore store;
    private StartupPreventionStore preventionStore;
    private ExecutorService executor;
    private volatile StartupLocalBridge activeBridge;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        store = new StartupCleanupStore(this);
        preventionStore = new StartupPreventionStore(this);
        executor = Executors.newSingleThreadExecutor();
        render();
    }

    private void render() {
        ShieldStartupManagerView view = new ShieldStartupManagerView(this);
        StartupLocalBridge probe = new StartupLocalBridge(this);
        Set<String> selected = store.targets();
        view.render(store.autoEnabled(), probe.hasIdentity(), preventionStore.managedPackages(), selected, candidates(), store.lastSummary(),
                new ShieldStartupManagerView.Callbacks() {
            @Override public void onCheckLocalLink() { authorize(false); }
            @Override public void onSetAuto(boolean enabled) {
                if (enabled) authorize(true);
                else { store.setAutoEnabled(false); render(); }
            }
            @Override public void onTogglePrevention(String packageName, boolean enabled) { togglePrevention(packageName, enabled); }
            @Override public void onToggleTarget(String packageName, boolean enabled) {
                if (!store.setTarget(packageName, enabled)) {
                    Toast.makeText(ShieldStartupManagerActivity.this,
                            "Choose up to " + StartupCleanupPolicy.MAX_TARGETS + " apps.", Toast.LENGTH_SHORT).show();
                }
                render();
            }
            @Override public void onRunNow() { runNow(); }
            @Override public void onBack() { finish(); }
        });
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(view);
        setContentView(scroll);
    }

    private void authorize(boolean enableAfter) {
        executor.execute(() -> {
            StartupLocalBridge bridge = new StartupLocalBridge(this);
            activeBridge = bridge;
            try {
                String result = bridge.authorize(() -> runOnUiThread(() ->
                        Toast.makeText(this, "Approve BOOP on the Shield.", Toast.LENGTH_LONG).show()));
                if (enableAfter) store.setAutoEnabled(true);
                store.recordSummary(result);
                runOnUiThread(this::render);
            } catch (Exception failure) { showFailure(failure); }
            finally { activeBridge = null; }
        });
    }

    private void togglePrevention(String packageName, boolean enabled) {
        executor.execute(() -> {
            StartupLocalBridge bridge = new StartupLocalBridge(this);
            activeBridge = bridge;
            try {
                String result = bridge.setPrevention(packageName, enabled, true, () -> runOnUiThread(() ->
                        Toast.makeText(this, "Approve BOOP on the Shield.", Toast.LENGTH_LONG).show()));
                store.recordSummary(result);
                runOnUiThread(this::render);
            } catch (Exception failure) { showFailure(failure); }
            finally { activeBridge = null; }
        });
    }

    private void runNow() {
        Set<String> targets = store.targets();
        if (targets.isEmpty()) {
            Toast.makeText(this, "Choose at least one app first.", Toast.LENGTH_SHORT).show();
            return;
        }
        executor.execute(() -> {
            StartupLocalBridge bridge = new StartupLocalBridge(this);
            activeBridge = bridge;
            try {
                String result = bridge.run(targets, true, () -> runOnUiThread(() ->
                        Toast.makeText(this, "Approve BOOP on the Shield.", Toast.LENGTH_LONG).show()));
                store.recordSummary(result);
                runOnUiThread(this::render);
            } catch (Exception failure) { showFailure(failure); }
            finally { activeBridge = null; }
        });
    }

    private void showFailure(Exception failure) {
        String message = failure.getMessage();
        if (message == null || message.isBlank()) message = failure.getClass().getSimpleName();
        final String safe = message.length() > 180 ? message.substring(0, 180) : message;
        store.recordSummary("Not applied: " + safe);
        runOnUiThread(() -> {
            Toast.makeText(this, safe, Toast.LENGTH_LONG).show();
            render();
        });
    }

    private List<TvAppEntry> candidates() {
        PackageManager pm = getPackageManager();
        LinkedHashMap<String,TvAppEntry> found = new LinkedHashMap<>();
        for (String category : List.of(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)) {
            Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(category);
            for (ResolveInfo info : pm.queryIntentActivities(intent, 0)) {
                if (info.activityInfo == null) continue;
                String pkg = info.activityInfo.packageName;
                try {
                    ApplicationInfo app = pm.getApplicationInfo(pkg, PackageManager.MATCH_DISABLED_COMPONENTS);
                    boolean system = (app.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
                    if (!StartupCleanupPolicy.eligible(pkg, system)) continue;
                    CharSequence raw = info.loadLabel(pm);
                    String label = raw == null || raw.toString().isBlank() ? pkg : raw.toString().trim();
                    found.putIfAbsent(pkg, new TvAppEntry(pkg + "/" + info.activityInfo.name, pkg, label));
                } catch (PackageManager.NameNotFoundException ignored) { }
            }
        }
        ArrayList<TvAppEntry> out = new ArrayList<>(found.values());
        out.sort((a,b) -> a.label().compareToIgnoreCase(b.label()));
        return out;
    }

    @Override protected void onDestroy() {
        if (activeBridge != null) activeBridge.cancel();
        if (executor != null) executor.shutdownNow();
        super.onDestroy();
    }
}
