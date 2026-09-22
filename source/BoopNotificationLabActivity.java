package com.boop.alpha1;

import android.app.Activity;
import android.graphics.Color;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import java.net.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;

/** Explicit foreground-only entry to the existing production notification previews. */
public final class BoopNotificationLabActivity extends Activity {
    private final Handler ui = new Handler(Looper.getMainLooper());
    private FrameLayout root;
    private BoopPreviewServer server;
    private NsdManager nsd;
    private NsdManager.RegistrationListener registration;
    private String code = "", discovery = "Starting local discovery…";
    private boolean active, preview;
    private int generation;
    private final Runnable timeout = this::showReady;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(5894);
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        setContentView(root);
    }
    @Override public void onStart() {
        super.onStart();
        active = true;
        final int session = ++generation;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        code = String.format(Locale.ROOT, "%06d", new SecureRandom().nextInt(1000000));
        try {
            server = new BoopPreviewServer(code, command -> dispatch(session, command));
            nsd = getSystemService(NsdManager.class);
            NsdServiceInfo info = new NsdServiceInfo();
            info.setServiceName("BOOP Phone " + android.os.Build.MODEL);
            info.setServiceType(BoopPreviewProtocol.SERVICE_TYPE);
            info.setPort(server.port());
            registration = new NsdManager.RegistrationListener() {
                public void onServiceRegistered(NsdServiceInfo service) {
                    ui.post(() -> { if (active && session == generation) { discovery = service.getServiceName(); if (!preview) showReady(); } });
                }
                public void onRegistrationFailed(NsdServiceInfo service, int error) {
                    ui.post(() -> { if (active && session == generation) { discovery = "Use the address below on Shield"; if (!preview) showReady(); } });
                }
                public void onServiceUnregistered(NsdServiceInfo service) { }
                public void onUnregistrationFailed(NsdServiceInfo service, int error) { }
            };
            nsd.registerService(info, NsdManager.PROTOCOL_DNS_SD, registration);
        } catch (Exception failure) {
            discovery = "Unable to start. Close and reopen this test.";
        }
        showReady();
    }
    private boolean dispatch(int session, String command) {
        FutureTask<Boolean> task = new FutureTask<>(() -> {
            if (!active || session != generation || server == null || !server.isRunning()) return false;
            if ("STOP".equals(command)) showReady(); else showPreview(command);
            android.util.Log.i("BOOPNotifyLab", "accepted " + command);
            return true;
        });
        ui.post(task);
        try { return task.get(900, TimeUnit.MILLISECONDS); }
        catch (Exception failure) { task.cancel(false); return false; }
    }
    private TextView label(String value, int size) {
        TextView text = new TextView(this);
        text.setText(value); text.setTextSize(size); text.setTextColor(Color.WHITE);
        text.setGravity(Gravity.CENTER); text.setPadding(12, 12, 12, 12);
        return text;
    }
    private void showReady() {
        ui.removeCallbacks(timeout);
        if (!active) return;
        preview = false;
        root.removeAllViews();
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL); column.setGravity(Gravity.CENTER);
        column.setPadding(24, 24, 24, 24);
        root.addView(column, new FrameLayout.LayoutParams(-1, -1));
        column.addView(label("BOOP Notification Test", 27));
        column.addView(label("Open BOOP Test Sender on Shield", 19));
        column.addView(label(server != null && server.isRunning() ? code : "Not listening", 42));
        column.addView(label(discovery, 17));
        column.addView(label(addresses(), 15));
        column.addView(label("Fake previews only. Close this screen to disconnect.", 16));
        Button done = new Button(this); done.setText("Close test");
        done.setOnClickListener(v -> finish()); column.addView(done);
    }
    private String addresses() {
        if (server == null || !server.isRunning()) return "";
        List<String> found = new ArrayList<>();
        try {
            for (NetworkInterface adapter : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress address : Collections.list(adapter.getInetAddresses())) {
                    if (address instanceof Inet4Address && address.isSiteLocalAddress())
                        found.add(address.getHostAddress() + ":" + server.port());
                }
            }
        } catch (SocketException ignored) { }
        return String.join("\n", found);
    }
    private void showPreview(String command) {
        ui.removeCallbacks(timeout);
        preview = true; root.removeAllViews();
        BoopNotificationPresentation presentation = BoopDevNotificationPreview.presentation(
                BoopDevMenuModel.Action.valueOf("NOTIFICATION_" + command), System.currentTimeMillis());
        root.addView(new BoopNotificationPuppetView(this, presentation, new BoopNotificationPuppetView.Callback() {
            public void onOpen(String key) { showReady(); }
            public void onOpenBundle() { showReady(); }
            public void onDismiss() { showReady(); }
        }), new FrameLayout.LayoutParams(-1, -1));
        TextView badge = label("TEST • " + command, 12);
        root.addView(badge, new FrameLayout.LayoutParams(-1, -2, Gravity.TOP));
        ui.postDelayed(timeout, 8000);
    }
    @Override public void onBackPressed() { if (preview) showReady(); else super.onBackPressed(); }
    @Override public void onStop() {
        active = false; generation++;
        ui.removeCallbacksAndMessages(null);
        if (server != null) { server.close(); server = null; }
        if (registration != null && nsd != null) {
            try { nsd.unregisterService(registration); } catch (IllegalArgumentException ignored) { }
            registration = null;
        }
        root.removeAllViews();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onStop();
    }
}
