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

/** Opt-in foreground voice-profile sharing through the already paired Home Assistant. */
final class BoopSharedVoiceProfileRuntime implements Application.ActivityLifecycleCallbacks {
    private static BoopSharedVoiceProfileRuntime instance;
    private final Context app;
    private final SharedPreferences settings, voice;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final OkHttpClient http = new OkHttpClient.Builder()
            .pingInterval(25, java.util.concurrent.TimeUnit.SECONDS).build();
    private final Set<Activity> started = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Runnable> observers = new LinkedHashSet<>();
    private final SharedPreferences.OnSharedPreferenceChangeListener voiceListener;
    private final SharedVoiceProfileState state;
    private HomeAssistantWebSocket socket;
    private SharedVoiceProfileLink link;
    private Future<?> authenticationJob;
    private long generation;
    private int retrySeconds = 2;
    private boolean connecting, applying;
    private String status = "Voice profile stays on this device.";
    private final Runnable flush = this::flush;
    private final Runnable stopAfterBackground = () -> {
        if (started.isEmpty()) {
            disconnect();
            status(enabled() ? "Voice sharing resumes when BOOP is open." : "Voice profile stays on this device.");
        }
    };

    static void initialize(Application application) {
        if (instance != null) return;
        instance = new BoopSharedVoiceProfileRuntime(application);
        application.registerActivityLifecycleCallbacks(instance);
    }

    static BoopSharedVoiceProfileRuntime get(Context context) {
        if (instance == null) initialize((Application) context.getApplicationContext());
        return instance;
    }

    private BoopSharedVoiceProfileRuntime(Context context) {
        app = context.getApplicationContext();
        settings = app.getSharedPreferences("boop_appearance", Context.MODE_PRIVATE);
        voice = app.getSharedPreferences(BoopVoiceController.PREFS_NAME, Context.MODE_PRIVATE);
        state = new SharedVoiceProfileState(localProfile());
        voiceListener = (store, key) -> {
            if (applying || !isProfileKey(key)) return;
            state.localChanged(localProfile());
            main.removeCallbacks(flush);
            if (state.isConnected()) main.postDelayed(flush, 180);
            notifyObservers();
        };
        voice.registerOnSharedPreferenceChangeListener(voiceListener);
    }

    boolean enabled() { return settings.getBoolean("shared_voice_enabled", false); }
    boolean ready() { return state.isConnected(); }
    String status() { return status; }

    Runnable observe(Runnable observer) {
        observers.add(observer);
        observer.run();
        return () -> observers.remove(observer);
    }

    void setEnabled(boolean enabled) {
        settings.edit().putBoolean("shared_voice_enabled", enabled).apply();
        disconnect();
        retrySeconds = 2;
        if (enabled) connect(true);
        else status("Voice profile stays on this device.");
    }

    private boolean current(long request) { return request == generation && enabled(); }

    private void connect(boolean explicitSetup) {
        if (!enabled() || started.isEmpty() || connecting || socket != null) return;
        connecting = true;
        final long request = ++generation;
        status("Connecting voice sharing...");
        main.postDelayed(() -> {
            if (current(request) && connecting) failed("Voice sharing timed out. Local controls still work.", true);
        }, 15000);
        authenticationJob = worker.submit(() -> {
            try {
                SecureTokenStore tokens = BoopVoiceTokenStore.create(app);
                if (!tokens.hasConnection()) throw new IllegalStateException("No paired Home Assistant");
                String base = tokens.getBaseUrl();
                String scope = scope(base);
                String saved = settings.getString("voice_server_scope", "");
                if (!explicitSetup && !scope.equals(saved)) {
                    main.post(() -> {
                        if (current(request)) failed("Home Assistant changed. Turn voice sharing on again for this home.", false);
                    });
                    return;
                }
                String token = new HomeAssistantAuth(app, tokens).freshAccessToken();
                main.post(() -> {
                    if (!current(request)) return;
                    if (explicitSetup) settings.edit().putString("voice_server_scope", scope).apply();
                    open(request, base, token, explicitSetup);
                });
            } catch (Exception unavailable) {
                final boolean retry = unavailable instanceof java.io.IOException;
                main.post(() -> {
                    if (current(request)) failed("Connect BOOP to Home Assistant, then retry voice sharing. Local controls are unchanged.", retry);
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
                                    failed("Home Assistant did not allow voice updates. Local controls still work.", false);
                                    return;
                                }
                                link.begin(explicitSetup);
                            }));
                        } catch (RuntimeException offline) {
                            failed("Voice sharing disconnected. Local controls still work.", true);
                        }
                    });
                }
                @Override public void onOffline(String ignored) {
                    main.post(() -> {
                        if (current(request)) failed("Voice sharing is offline. Keeping this device's last profile.", true);
                    });
                }
                @Override public void onReauthRequired(String ignored) {
                    main.post(() -> {
                        if (current(request)) failed("Home Assistant needs BOOP to reconnect before sharing voice.", false);
                    });
                }
            });
        } catch (RuntimeException invalid) {
            failed("Voice sharing could not connect. Local controls still work.", false);
        }
    }

    private void prepareLink(long request, HomeAssistantWebSocket connection) {
        link = new SharedVoiceProfileLink((type, body, reply) -> {
            if (!current(request)) return;
            AtomicBoolean completed = new AtomicBoolean();
            Runnable timeout = () -> {
                if (current(request) && completed.compareAndSet(false, true))
                    failed("Voice sharing timed out. Local controls still work.", true);
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
                failed("Voice sharing is offline. Local controls still work.", true);
            }
        }, new SharedVoiceProfileLink.Listener() {
            @Override public void ready(SharedVoiceProfileProtocol.Profile profile) {
                state.connected(profile);
                connecting = false;
                retrySeconds = 2;
                applyShared();
                status("Voice profile is shared through Home Assistant.");
            }
            @Override public void profile(SharedVoiceProfileProtocol.Profile profile) {
                state.remoteChanged(profile);
                applyShared();
            }
            @Override public void confirmed(SharedVoiceProfileProtocol.Profile profile) {
                state.writeConfirmed(profile);
                applyShared();
                main.removeCallbacks(flush);
                main.postDelayed(flush, 180);
            }
            @Override public void failed(String message) {
                BoopSharedVoiceProfileRuntime.this.failed(message, false);
            }
        }, this::localProfile);
    }

    private void flush() {
        if (link == null || !enabled() || started.isEmpty()) return;
        SharedVoiceProfileProtocol.Profile profile = state.nextWrite();
        if (profile != null) link.write(profile);
    }

    private SharedVoiceProfileProtocol.Profile localProfile() {
        String backend = voice.getString(BoopVoiceController.KEY_SELECTED_BACKEND, BoopVoiceController.BACKEND_ANDROID);
        if (!BoopVoiceController.BACKEND_NATURAL.equals(backend)) backend = BoopVoiceController.BACKEND_ANDROID;
        String key = voice.getString(BoopVoiceController.KEY_NATURAL_SPEAKER_KEY, "bf_emma");
        if (BoopVoiceController.findNaturalVoice(key) == null) key = "bf_emma";
        float pitch = BoopVoiceTuning.clampPitch(
                voice.getFloat(BoopVoiceController.KEY_PITCH, BoopVoiceTuning.DEFAULT_PITCH));
        float rate = BoopVoiceTuning.clampRate(
                voice.getFloat(BoopVoiceController.KEY_SPEECH_RATE, BoopVoiceTuning.DEFAULT_RATE));
        return new SharedVoiceProfileProtocol.Profile(
                backend, key, Math.round(pitch * 1000f), Math.round(rate * 1000f));
    }

    private void applyShared() {
        SharedVoiceProfileProtocol.Profile profile = state.localProfile();
        SharedVoiceProfileProtocol.Profile current = localProfile();
        if (!profile.equals(current)) {
            applying = true;
            try {
                voice.edit()
                        .putString(BoopVoiceController.KEY_SELECTED_BACKEND, profile.backend)
                        .putString(BoopVoiceController.KEY_NATURAL_SPEAKER_KEY, profile.naturalVoiceKey)
                        .putFloat(BoopVoiceController.KEY_PITCH, profile.pitchMilli / 1000f)
                        .putFloat(BoopVoiceController.KEY_SPEECH_RATE, profile.rateMilli / 1000f)
                        .apply();
            } finally {
                applying = false;
            }
        }
        notifyObservers();
    }

    private boolean isProfileKey(String key) {
        return key == null
                || BoopVoiceController.KEY_SELECTED_BACKEND.equals(key)
                || BoopVoiceController.KEY_NATURAL_SPEAKER_KEY.equals(key)
                || BoopVoiceController.KEY_PITCH.equals(key)
                || BoopVoiceController.KEY_SPEECH_RATE.equals(key);
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
