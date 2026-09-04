package com.boop.alpha1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity implements RecognitionListener, TextToSpeech.OnInitListener {
    private static final int REQ_RECORD_AUDIO = 1001;
    private static final int REQ_NEARBY_WIFI = 1002;
    private static final String HOME_AREA = "Living Room";
    private static final long IDLE_TIMEOUT_MS = 30_000L;

    private BoopFaceView face;
    private BoopPresenceState presenceState;
    private Handler presenceHandler;
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean listening = false;
    private boolean pendingListenAfterPermission = false;
    private boolean pendingDiscoveryAfterPermission = false;
    private boolean connectPromptShowing = false;
    private boolean setupFailureSpoken = false;

    private ExecutorService executor;
    private SecureTokenStore tokenStore;
    private HomeAssistantAuth haAuth;
    private HomeAssistantClient haClient;
    private HomeAssistantDeviceSetup deviceSetup;
    private HomeAssistantDiscovery discovery;

    private final Runnable faceIdleRunnable = () -> {
        if (presenceState == null || face == null) {
            return;
        }
        if (listening) {
            scheduleFaceIdle();
            return;
        }
        if (presenceState.idle()) {
            face.goIdleBlack();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenceHandler = new Handler(Looper.getMainLooper());
        presenceState = new BoopPresenceState();

        FrameLayout interactionSurface = new FrameLayout(this);
        interactionSurface.setBackgroundColor(Color.BLACK);
        interactionSurface.setContentDescription("BOOP face. Tap anywhere to speak.");
        interactionSurface.setOnTouchListener(this::onFaceTouch);

        face = new BoopFaceView(this);
        interactionSurface.addView(face, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(interactionSurface);
        face.showIdleBlackImmediately();

        keepAwakeAndHideSystemUi();

        tts = new TextToSpeech(this, this);
        createRecognizer();

        executor = Executors.newSingleThreadExecutor();
        tokenStore = new SecureTokenStore(this);
        haAuth = new HomeAssistantAuth(this, tokenStore);
        haClient = new HomeAssistantClient(tokenStore, haAuth, HOME_AREA);
        deviceSetup = new HomeAssistantDeviceSetup(tokenStore, haAuth);
        discovery = new HomeAssistantDiscovery(this);
        handleAuthIntent(getIntent());
    }

    private boolean onFaceTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            wakeFaceForInteraction();
            return true;
        }
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return true;
        }

        if (listening) {
            stopListening();
        } else {
            beginTapToSpeak();
        }
        return true;
    }

    private void wakeFaceForInteraction() {
        if (presenceState != null && face != null && presenceState.wake()) {
            face.wakeFromIdle();
        }
        scheduleFaceIdle();
    }

    private void scheduleFaceIdle() {
        if (presenceHandler == null) {
            return;
        }
        presenceHandler.removeCallbacks(faceIdleRunnable);
        presenceHandler.postDelayed(faceIdleRunnable, IDLE_TIMEOUT_MS);
    }

    private void beginTapToSpeak() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            pendingListenAfterPermission = true;
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQ_RECORD_AUDIO);
            return;
        }
        startListening();
    }

    private void createRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizer = null;
            return;
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(this);
    }

    private void startListening() {
        wakeFaceForInteraction();
        if (recognizer == null) {
            speak("I can't hear speech on this device yet.");
            return;
        }

        if (tts != null) {
            tts.stop();
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);

        listening = true;
        face.animate().alpha(0.78f).setDuration(120).start();
        recognizer.startListening(intent);
    }

    private void stopListening() {
        listening = false;
        face.animate().alpha(1.0f).setDuration(120).start();
        if (recognizer != null) {
            recognizer.cancel();
        }
        scheduleFaceIdle();
    }

    private void handleRecognizedSpeech(String transcript) {
        if (!tokenStore.hasConnection()) {
            speak("I need to connect to the house first.");
            ensureHouseConnection();
            return;
        }

        executor.execute(() -> {
            HomeAssistantDeviceSetup.SetupResult setup = tokenStore.hasHaDeviceIdentity()
                    ? HomeAssistantDeviceSetup.SetupResult.READY
                    : deviceSetup.ensureReady();
            if (setup != HomeAssistantDeviceSetup.SetupResult.READY) {
                runOnUiThread(() -> handleDeviceSetupFailure(setup));
                return;
            }

            setupFailureSpoken = false;
            CommandOutcome outcome = haClient.process(transcript);
            runOnUiThread(() -> {
                speak(LocalReply.forOutcome(outcome));
                if (outcome.status() == CommandOutcome.Status.AUTH_REQUIRED) {
                    tokenStore.clear();
                    setupFailureSpoken = false;
                    ensureHouseConnection();
                }
            });
        });
    }

    private void handleDeviceSetupFailure(HomeAssistantDeviceSetup.SetupResult result) {
        switch (result) {
            case AUTH_REQUIRED:
                tokenStore.clear();
                setupFailureSpoken = false;
                speak("I need to reconnect to the house.");
                ensureHouseConnection();
                return;
            case UNREACHABLE:
                speak("I can't reach the house right now.");
                return;
            case AREA_NOT_FOUND:
            case FORBIDDEN:
            case FAILED:
            default:
                if (!setupFailureSpoken) {
                    setupFailureSpoken = true;
                    speak("I couldn't set my room.");
                }
        }
    }

    private void ensureHouseConnection() {
        if (tokenStore.hasConnection()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES)
                != PackageManager.PERMISSION_GRANTED) {
            pendingDiscoveryAfterPermission = true;
            requestPermissions(
                    new String[]{Manifest.permission.NEARBY_WIFI_DEVICES},
                    REQ_NEARBY_WIFI);
            return;
        }
        startHouseDiscovery();
    }

    private void startHouseDiscovery() {
        discovery.start(new HomeAssistantDiscovery.Listener() {
            @Override
            public void onFound(String displayName, String baseUrl) {
                runOnUiThread(() -> showConnectPrompt(displayName, baseUrl));
            }

            @Override
            public void onUnavailable(String reason) {
                runOnUiThread(() -> speak("I can't find the house right now."));
            }
        });
    }

    private void showConnectPrompt(String displayName, String baseUrl) {
        if (connectPromptShowing || tokenStore.hasConnection()) {
            return;
        }
        connectPromptShowing = true;

        new AlertDialog.Builder(this)
                .setTitle("I found your house")
                .setMessage(displayName + "\n" + baseUrl)
                .setPositiveButton("Connect", (dialog, which) -> {
                    connectPromptShowing = false;
                    discovery.stop();
                    try {
                        String authorizeUrl = haAuth.begin(baseUrl);
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authorizeUrl)));
                    } catch (RuntimeException e) {
                        speak("That didn't connect.");
                    }
                })
                .setNegativeButton("Not now", (dialog, which) -> {
                    connectPromptShowing = false;
                    discovery.stop();
                })
                .setOnCancelListener(dialog -> {
                    connectPromptShowing = false;
                    discovery.stop();
                })
                .show();
    }

    private void handleAuthIntent(Intent intent) {
        if (intent == null || intent.getData() == null) {
            return;
        }
        Uri data = intent.getData();
        if (!"boop".equals(data.getScheme()) || !"auth-callback".equals(data.getHost())) {
            return;
        }

        executor.execute(() -> {
            try {
                haAuth.completeCallback(data);
                setupFailureSpoken = false;
                runOnUiThread(() -> speak("House connected."));
                HomeAssistantDeviceSetup.SetupResult setup = deviceSetup.ensureReady();
                if (setup == HomeAssistantDeviceSetup.SetupResult.READY) {
                    setupFailureSpoken = false;
                } else {
                    runOnUiThread(() -> handleDeviceSetupFailure(setup));
                }
            } catch (Exception e) {
                runOnUiThread(() -> speak("That didn't connect."));
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleAuthIntent(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        keepAwakeAndHideSystemUi();
        if (face != null) {
            if (presenceState != null && presenceState.isIdleBlack()) {
                face.showIdleBlackImmediately();
            } else {
                face.invalidate();
            }
        }
    }

    private void speak(String text) {
        wakeFaceForInteraction();
        if (ttsReady && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "boop-alpha3");
        }
    }

    private void keepAwakeAndHideSystemUi() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            keepAwakeAndHideSystemUi();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_RECORD_AUDIO) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (granted && pendingListenAfterPermission) {
                pendingListenAfterPermission = false;
                startListening();
            } else if (!granted) {
                pendingListenAfterPermission = false;
                speak("I need microphone permission to hear you.");
            }
            return;
        }

        if (requestCode == REQ_NEARBY_WIFI) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (granted && pendingDiscoveryAfterPermission) {
                pendingDiscoveryAfterPermission = false;
                startHouseDiscovery();
            } else if (!granted) {
                pendingDiscoveryAfterPermission = false;
                speak("I need nearby devices permission to find the house.");
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS && tts != null) {
            int result = tts.setLanguage(Locale.getDefault());
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
            tts.setSpeechRate(1.0f);
            tts.setPitch(1.0f);
        }
    }

    @Override public void onReadyForSpeech(Bundle params) { }
    @Override public void onBeginningOfSpeech() { }
    @Override public void onRmsChanged(float rmsdB) { }
    @Override public void onBufferReceived(byte[] buffer) { }
    @Override public void onEndOfSpeech() { }

    private String speechErrorName(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "network timeout";
            case SpeechRecognizer.ERROR_NETWORK: return "network";
            case SpeechRecognizer.ERROR_AUDIO: return "audio";
            case SpeechRecognizer.ERROR_SERVER: return "server";
            case SpeechRecognizer.ERROR_CLIENT: return "client";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "speech timeout";
            case SpeechRecognizer.ERROR_NO_MATCH: return "no match";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "recognizer busy";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "insufficient permissions";
            case SpeechRecognizer.ERROR_TOO_MANY_REQUESTS: return "too many requests";
            case SpeechRecognizer.ERROR_SERVER_DISCONNECTED: return "server disconnected";
            case SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED: return "language not supported";
            case SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE: return "language unavailable";
            case SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT: return "cannot check support";
            case SpeechRecognizer.ERROR_CANNOT_LISTEN_TO_DOWNLOAD_EVENTS: return "cannot listen to download events";
            default: return "unknown";
        }
    }

    @Override
    public void onError(int error) {
        listening = false;
        face.animate().alpha(1.0f).setDuration(120).start();
        speak("Speech error " + error + ", " + speechErrorName(error) + ".");
    }

    @Override
    public void onResults(Bundle results) {
        listening = false;
        face.animate().alpha(1.0f).setDuration(120).start();

        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String best = matches.get(0).trim();
            if (!best.isEmpty()) {
                handleRecognizedSpeech(best);
                return;
            }
        }
        speak("I didn't catch that.");
    }

    @Override public void onPartialResults(Bundle partialResults) { }
    @Override public void onEvent(int eventType, Bundle params) { }

    @Override
    protected void onDestroy() {
        if (presenceHandler != null) {
            presenceHandler.removeCallbacks(faceIdleRunnable);
            presenceHandler = null;
        }
        if (discovery != null) {
            discovery.stop();
        }
        if (recognizer != null) {
            recognizer.destroy();
            recognizer = null;
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        super.onDestroy();
    }
}
