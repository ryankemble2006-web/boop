package com.boop.alpha1;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.boop.shieldoverlay.HomeAssistantWebSocket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.OkHttpClient;

/** Opt-in foreground sharing. Does not replace Wall's local hue store or rendering. */
final class BoopSharedEyeColourRuntime implements Application.ActivityLifecycleCallbacks {
    private static BoopSharedEyeColourRuntime instance;
    private final Context app;
    private final SharedPreferences settings, eyes;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final OkHttpClient http = new OkHttpClient.Builder()
            .pingInterval(25, java.util.concurrent.TimeUnit.SECONDS).build();
    private final Set<Activity> started = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Runnable> observers = new LinkedHashSet<>();
    private final SharedPreferences.OnSharedPreferenceChangeListener hueListener;
    private final SharedEyeColourState state;
    private HomeAssistantWebSocket socket;
    private SharedEyeColourLink link;
    private Future<?> authenticationJob;
    private long generation;
    private int retrySeconds = 2;
    private boolean connecting, applying;
    private String status = "Eye colour stays on this device.";
    private final Runnable flush = this::flush;
    private final Runnable stopAfterBackground = () -> {
        if (started.isEmpty()) {
            disconnect();
            status(enabled() ? "Sharing resumes when BOOP is open." : "Eye colour stays on this device.");
        }
    };

    static void initialize(Application application) {
        if (instance != null) return;
        instance = new BoopSharedEyeColourRuntime(application);
        application.registerActivityLifecycleCallbacks(instance);
    }
    static BoopSharedEyeColourRuntime get(Context context) {
        if (instance == null) initialize((Application) context.getApplicationContext());
        return instance;
    }
    private BoopSharedEyeColourRuntime(Context context) {
        app = context.getApplicationContext();
        settings = app.getSharedPreferences("boop_appearance", Context.MODE_PRIVATE);
        eyes = app.getSharedPreferences("boop_eyes", Context.MODE_PRIVATE);
        state = new SharedEyeColourState(BoopEyeHue.loadHue(app));
        hueListener = (store, key) -> {
            if (applying || (key != null && !"hue_degrees".equals(key))) return;
            state.localChanged(BoopEyeHue.loadHue(app));
            main.removeCallbacks(flush);
            if (state.isConnected()) main.postDelayed(flush, 180);
            notifyObservers();
        };
        eyes.registerOnSharedPreferenceChangeListener(hueListener);
    }
    boolean enabled() { return settings.getBoolean("shared_colour_enabled", false); }
    boolean ready() { return state.isConnected(); }
    String status() { return status; }
    Runnable observe(Runnable observer) {
        observers.add(observer);
        observer.run();
        return () -> observers.remove(observer);
    }
    void setEnabled(boolean enabled) {
        settings.edit().putBoolean("shared_colour_enabled", enabled).apply();
        disconnect();
        retrySeconds = 2;
        if (enabled) connect(true);
        else status("Eye colour stays on this device.");
    }
    // Allow replies during the short Activity-to-Activity handover. A real background
    // stop closes the link and advances generation, invalidating every old callback.
    private boolean current(long request) { return request == generation && enabled(); }
    private void connect(boolean explicitSetup) {
        if (!enabled() || started.isEmpty() || connecting || socket != null) return;
        connecting = true;
        final long request = ++generation;
        status("Connecting colour sharing...");
        main.postDelayed(() -> {
            if (current(request) && connecting) failed("Colour sharing timed out. Local colour still works.", true);
        }, 15000);
        authenticationJob = worker.submit(() -> {
            try {
                SecureTokenStore tokens = BoopVoiceTokenStore.create(app);
                if (!tokens.hasConnection()) throw new IllegalStateException("No paired Home Assistant");
                String base = tokens.getBaseUrl();
                String scope = scope(base);
                String saved = settings.getString("colour_server_scope", "");
                if (!explicitSetup && !scope.equals(saved)) {
                    main.post(() -> {
                        if (current(request)) failed("Home Assistant changed. Turn sharing on again to choose this home.", false);
                    });
                    return;
                }
                String token = new HomeAssistantAuth(app, tokens).freshAccessToken();
                main.post(() -> {
                    if (!current(request)) return;
                    if (explicitSetup) settings.edit().putString("colour_server_scope", scope).apply();
                    open(request, base, token, explicitSetup);
                });
            } catch (Exception unavailable) {
                final boolean retry = unavailable instanceof java.io.IOException;
                main.post(() -> {
                    if (current(request)) failed("Connect BOOP to Home Assistant, then retry colour sharing. Your local colour is unchanged.", retry);
                });
            }
        });
    }
    private void open(long request, String base, String token, boolean explicitSetup) {
        final HomeAssistantWebSocket connection = new HomeAssistantWebSocket(http);
        socket = connection;
        try {
            connection.connect(base, token, new HomeAssistantWebSocket.Listener() {
                @Override public void onReady() {
                    main.post(() -> {
                        if (!current(request)) return;
                        try {
                            prepareLink(request, connection);
                            connection.subscribeStateChanges((entity, value) -> main.post(() -> {
                                if (current(request) && link != null) link.event(entity, value);
                            }), (subscription, error) -> main.post(() -> {
                                if (!current(request)) {
                                    if (subscription != null) subscription.cancel();
                                    return;
                                }
                                if (subscription == null) {
                                    failed("Home Assistant did not allow colour updates. Local colour still works.", false);
                                    return;
                                }
                                link.begin(explicitSetup);
                            }));
                        } catch (RuntimeException offline) {
                            failed("Colour sharing disconnected. Local colour still works.", true);
                        }
                    });
                }
                @Override public void onOffline(String ignored) {
                    main.post(() -> {
                        if (current(request)) failed("Sharing is offline. Keeping this device's last colour.", true);
                    });
                }
                @Override public void onReauthRequired(String ignored) {
                    main.post(() -> {
                        if (current(request)) failed("Home Assistant needs BOOP to reconnect before sharing colour.", false);
                    });
                }
            });
        } catch (RuntimeException invalid) {
            failed("Colour sharing could not connect. Local colour still works.", false);
        }
    }
    private void prepareLink(long request, HomeAssistantWebSocket connection) {
        link = new SharedEyeColourLink((type, body, reply) -> {
            if (!current(request)) return;
            AtomicBoolean completed = new AtomicBoolean();
            Runnable timeout = () -> {
                if (current(request) && completed.compareAndSet(false, true))
                    failed("Colour sharing timed out. Local colour still works.", true);
            };
            main.postDelayed(timeout, 8000);
            try {
                connection.send(type, body, (success, result, error) -> main.post(() -> {
                    if (!current(request) || !completed.compareAndSet(false, true)) return;
                    main.removeCallbacks(timeout);
                    reply.complete(success, result);
                }));
            } catch (RuntimeException offline) {
                main.removeCallbacks(timeout);
                failed("Colour sharing is offline. Local colour still works.", true);
            }
        }, new SharedEyeColourLink.Listener() {
            @Override public void ready(int hue) {
                state.connected(hue);
                connecting = false;
                retrySeconds = 2;
                applyShared();
                status("Eye colour is shared through Home Assistant.");
            }
            @Override public void colour(int hue) { state.remoteChanged(hue); applyShared(); }
            @Override public void confirmed(int hue) {
                state.writeConfirmed(hue);
                applyShared();
                main.removeCallbacks(flush);
                main.postDelayed(flush, 180);
            }
            @Override public void failed(String message) { BoopSharedEyeColourRuntime.this.failed(message, false); }
        }, () -> BoopEyeHue.loadHue(app));
    }
    private void flush() {
        if (link == null || !enabled() || started.isEmpty()) return;
        Integer value = state.nextWrite();
        if (value != null) link.write(value);
    }
    private void applyShared() {
        int value = state.localHue();
        if (BoopEyeHue.loadHue(app) != value) {
            applying = true;
            try { BoopEyeHue.saveHue(app, value); }
            finally { applying = false; }
        }
        notifyObservers();
    }
    private void failed(String message, boolean retry) {
        disconnect();
        status(message);
        if (!retry || !enabled() || started.isEmpty()) return;
        long request = generation;
        int delay = retrySeconds;
        retrySeconds = Math.min(30, retrySeconds * 2);
        main.postDelayed(() -> { if (current(request)) connect(false); }, delay * 1000L);
    }
    private void disconnect() {
        generation++;
        connecting = false;
        if (authenticationJob != null) { authenticationJob.cancel(true); authenticationJob = null; }
        main.removeCallbacksAndMessages(null);
        state.disconnected();
        if (link != null) { link.close(); link = null; }
        HomeAssistantWebSocket old = socket;
        socket = null;
        if (old != null) old.close();
    }
    private void status(String value) { status = value; notifyObservers(); }
    private void notifyObservers() { for (Runnable observer : new LinkedHashSet<>(observers)) observer.run(); }
    private static String scope(String base) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(
                HomeAssistantAuthUrls.trim(base).getBytes(StandardCharsets.UTF_8));
        StringBuilder out = new StringBuilder();
        for (byte b : hash) out.append(String.format(java.util.Locale.ROOT, "%02x", b & 255));
        return out.toString();
    }
    @Override public void onActivityStarted(Activity activity) {
        started.add(activity);
        main.removeCallbacks(stopAfterBackground);
        connect(false);
        if (state.isConnected()) { main.removeCallbacks(flush); main.postDelayed(flush, 180); }
    }
    @Override public void onActivityStopped(Activity activity) {
        started.remove(activity);
        if (started.isEmpty()) main.postDelayed(stopAfterBackground, 700);
    }
    @Override public void onActivityCreated(Activity activity, Bundle state) { }
    @Override public void onActivityResumed(Activity activity) { }
    @Override public void onActivityPaused(Activity activity) { }
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) { }
    @Override public void onActivityDestroyed(Activity activity) { started.remove(activity); }
}
