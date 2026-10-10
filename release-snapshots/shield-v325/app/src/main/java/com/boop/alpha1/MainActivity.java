package com.boop.alpha1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognitionSupport;
import android.speech.RecognitionSupportCallback;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity implements RecognitionListener, TextToSpeech.OnInitListener, SensorEventListener {
    private static final int REQ_RECORD_AUDIO = 1001;
    private static final int REQ_NEARBY_WIFI = 1002;
    private static final int REQ_CAMERA = 1003;
    private static final String HOME_AREA = "Living Room";
    private static final long IDLE_TIMEOUT_MS = 30_000L;
    private static final long ASSISTANT_FOLLOW_UP_SILENCE_MS = 5_000L;

    private enum RecognitionMode { NONE, TAP, WAKE }

    private FrameLayout interactionSurface;
    private BoopNotificationInPlaceController notificationInPlaceController;
    private BoopCanonicalFaceView face;
    private BoopPresenceState presenceState;
    private Handler presenceHandler;
    private SensorManager sensorManager;
    private Sensor shakeSensor;
    private final BoopShakeDetector shakeDetector = new BoopShakeDetector();
    private SpeechRecognizer recognizer;
    private RecognitionMode recognitionMode = RecognitionMode.NONE;
    private BoopWakeWordController wakeWordController;
    private BoopWakeSessionCoordinator wakeCoordinator;
    private BoopWakeAudioSession wakeAudioSession;
    // BOOP_WAKE_ASR_DIAGNOSTIC_V1
    private BoopWakeDiagnosticTrace wakeDiagnosticTrace;
    // BOOP_POWERED_WAKE_RESILIENCE_V1
    private String lastWakeDiagnosticSummary = "No wake diagnostic captured yet.";
    private String lastWakeDiagnosticRecovery = "none";
    private final BoopWakeTranscriptAccumulator wakeTranscriptAccumulator = new BoopWakeTranscriptAccumulator();
    private boolean wakeSupportCheckInFlight;
    private BoopDockModeController dockModeController;
    private BoopPresencePeekController presencePeekController;
    private BoopMirrorController mirrorController;
    private boolean pendingMirrorAfterPermission;
    private TextToSpeech tts;
    private BoopVoiceController voiceController;
    // BOOP_NATURAL_VOICES_V70
    private BoopAndroidSpeechBackend androidSpeechBackend;
    private BoopNaturalVoiceManifest naturalVoiceManifest;
    private BoopNaturalVoicePack naturalVoicePack;
    private BoopNaturalVoiceDownloader naturalVoiceDownloader;
    private BoopNaturalSpeechBackend naturalSpeechBackend;
    private int naturalPreviewGeneration;
    private final BoopToastEgg toastEgg = new BoopToastEgg();
    private LinearLayout voiceSettingsOverlay;
    // BOOP_DEVELOPER_MENU_IN_PLACE_FIELDS_V4
    private FrameLayout developerMenuOverlay;
    private BoopCanonicalFaceView developerMenuFace;
    private boolean developerMenuOpen = false;
    private int developerAnimationScrollX = 0;
    private int developerNotificationScrollX = 0;
    private ScrollView voiceSettingsScroll;
    private EditText wakeNameInput;
    private Button wakeNameTrainButton;
    private TextView wakeNameTrainingStatus;
    private boolean wakeEnrollmentActive;
    private boolean wakeEnrollmentPauseOwned;
    private String pendingWakeEnrollmentName;
    // BOOP_WAKE_AUTO_ENROLMENT_V1
    private String pendingWakeEnrollmentAfterSettings;
    private boolean ttsReady = false;
    private boolean listening = false;
    private boolean thinking = false;
    private boolean voiceSettingsOpen = false;
    private boolean pendingListenAfterPermission = false;
    private boolean assistantFollowUpAfterTts = false;
    private boolean sleepFaceAfterTts = false;
    private boolean assistantFollowUpListening = false;
    private boolean suppressNextRecognizerError = false;
    private String latestAssistantFollowUpPartial = null;
    private boolean pendingDiscoveryAfterPermission = false;
    private boolean assistantOneShot = false;
    private boolean assistantOneShotStarted = false;
    private boolean connectPromptShowing = false;
    private boolean setupFailureSpoken = false;
    private boolean memberBerryConsumed = false;
    private int memberBerryVariant = 0;
    private float faceTouchDownX;
    private float faceTouchDownY;
    private boolean swipeHadMultiplePointers = false;
    // BOOP_CHAT_MODE_V1: local control is never switched to a web provider.
    // BOOP_OPENAI_RELAY_V1: native chat is fallback-only after local NO_MATCH.
    private BoopChatModeStore chatModeStore;
    private OpenAiRelayAssistantClient nativeChatClient;
    private AlertDialog chatModeDialog;
    private boolean chatModeOpen;
    private boolean activityInForeground;
    private volatile int chatModeRevision;
    private boolean faceTouchActive;
    private boolean faceGestureMoved;
    private BoopEyeHueOverlay eyeHueOverlay;
    private final BoopChatModeHold chatModeHold = new BoopChatModeHold();
    private final Runnable chatModeHoldRunnable = () -> {
        if (!activityInForeground || !faceTouchActive || voiceSettingsOpen || chatModeOpen
                || isFinishing() || isDestroyed()) return;
        if (chatModeHold.tryOpen(android.os.SystemClock.uptimeMillis())) {
            memberBerryConsumed = true;
            cancelMemberBerryHold();
            showChatModeMenu();
        }
    };

    private ExecutorService executor;
    private SecureTokenStore tokenStore;
    private HomeAssistantAuth haAuth;
    private HomeAssistantClient haClient;
    private HomeAssistantGeneralAssistantClient generalAssistant;
    private BoopCommandRouter commandRouter;
    private final BoopRecipeSession recipeSession = new BoopRecipeSession();
    private BoopRecipePanel recipePanel;
    private long recipeRequestTicket = -1;

    private HomeAssistantDeviceSetup deviceSetup;
    private HomeAssistantDiscovery discovery;

    private final Runnable faceIdleRunnable = () -> {
        if (presenceState == null || face == null) {
            return;
        }
        if (listening || thinking || voiceSettingsOpen || chatModeOpen
                || (eyeHueOverlay != null && eyeHueOverlay.isVisible())) {
            scheduleFaceIdle();
            return;
        }
        if (presenceState.idle()) {
            face.goIdleBlack();
        }
    };

    private final Runnable assistantFollowUpSilenceRunnable =
            this::finishAssistantFollowUpSilently;

    private final Runnable memberBerryRunnable = () -> {
        if (interactionSurface == null || face == null || listening || voiceSettingsOpen || chatModeOpen) {
            return;
        }
        memberBerryConsumed = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            interactionSurface.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        } else {
            interactionSurface.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        }
        face.playMemberBerry(memberBerryVariant++);
        scheduleFaceIdle();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatModeStore = new BoopChatModeStore(this);
        nativeChatClient = new OpenAiRelayAssistantClient(
                OpenAiRelayConfig.fromBuildConfig(), new OpenAiRelayOkHttpTransport());
        presenceHandler = new Handler(Looper.getMainLooper());
        presenceState = new BoopPresenceState();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            shakeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        interactionSurface = new FrameLayout(this);
        interactionSurface.setBackgroundColor(Color.BLACK);
        interactionSurface.setContentDescription("BOOP face. Tap anywhere to speak.");
        interactionSurface.setOnTouchListener(this::onFaceTouch);

        face = new BoopCanonicalFaceView(this);
        // BOOP_IDLE_BLINK_V1: read state only; never extend the idle deadline.
        face.setIdleBlinkAllowed(() -> activityInForeground
                && presenceState != null && !presenceState.isIdleBlack()
                && !listening && !thinking && (tts == null || !tts.isSpeaking())
                && !voiceSettingsOpen && !chatModeOpen && !faceTouchActive);
        interactionSurface.addView(face, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        eyeHueOverlay = new BoopEyeHueOverlay(this, interactionSurface, face);
        setContentView(interactionSurface);
        // BOOP_NOTIFICATION_IN_PLACE_HOST_V1
        notificationInPlaceController = new BoopNotificationInPlaceController(interactionSurface);
        face.showIdleBlackImmediately();

        keepAwakeAndHideSystemUi();

        voiceController = new BoopVoiceController(this);
        try {
            naturalVoiceManifest = BoopNaturalVoiceManifest.load(this);
            naturalVoicePack = new BoopNaturalVoicePack(this, naturalVoiceManifest);
            boolean naturalPackReady = naturalVoicePack.isInstalled();
            if (naturalPackReady) {
                voiceController.onNaturalPackVerified(naturalVoiceManifest.version());
            } else {
                voiceController.setNaturalPackUsable(false);
            }
            naturalSpeechBackend = new BoopNaturalSpeechBackend(naturalVoicePack);
            naturalVoiceDownloader = new BoopNaturalVoiceDownloader(
                    new okhttp3.OkHttpClient.Builder().build(),
                    naturalVoiceManifest,
                    naturalVoicePack);
        } catch (Throwable unavailable) {
            android.util.Log.w("BOOP-NaturalVoice", "Natural voice layer unavailable; Android TTS remains active", unavailable);
            naturalVoiceManifest = null;
            naturalVoicePack = null;
            naturalVoiceDownloader = null;
            naturalSpeechBackend = null;
            voiceController.setNaturalPackUsable(false);
        }
        tts = new TextToSpeech(this, this);
        androidSpeechBackend = new BoopAndroidSpeechBackend(tts);
        createRecognizer();
        createWakeObjects();
        presencePeekController = new BoopPresencePeekController(
                this,
                personSeen -> {
                    if (personSeen) {
                        wakeFaceForInteraction();
                    }
                });
        mirrorController = new BoopMirrorController(
                this,
                interactionSurface,
                this::wakeFaceForInteraction);
        dockModeController = new BoopDockModeController(
                this,
                new BoopDockModeController.Listener() {
                    @Override
                    public void onWakePowerChanged(boolean powered) {
                        applyDockWakePolicy(powered);
                    }

                    @Override
                    public void onPresenceNudge() {
                        handleDockPresenceNudge();
                    }
                });

        executor = Executors.newSingleThreadExecutor();
        tokenStore = BoopVoiceTokenStore.create(this);
        haAuth = new HomeAssistantAuth(this, tokenStore);
        BoopRoomSource roomSource = new BoopRoomPreferences(this);
        haClient = new HomeAssistantClient(tokenStore, haAuth, roomSource);
        generalAssistant = new HomeAssistantGeneralAssistantClient(tokenStore, haAuth);
        commandRouter = new BoopCommandRouter(
                haClient::process,
                generalAssistant::ask,
                new BoopCommandRouter.AssistantActivity() {
                    @Override
                    public void onAssistantStarted() {
                        startAssistantThinking();
                    }

                    @Override
                    public void onAssistantFinished() {
                        stopAssistantThinking();
                    }
                });
        deviceSetup = new HomeAssistantDeviceSetup(tokenStore, haAuth, roomSource);
        discovery = new HomeAssistantDiscovery(this);
        handleAuthIntent(getIntent());
        if (getIntent().getBooleanExtra("boop_open_voice_settings", false)) {
            interactionSurface.post(this::showVoiceSettings);
        }
        if (savedInstanceState == null) acceptAssistantIntent(getIntent());
    }

    @Override
    protected void onResume() {
        if (recipePanel != null && recipeSession.active()) recipePanel.render(recipeSession);
        super.onResume();
        if (notificationInPlaceController != null) {
            try {
                BoopNotificationRuntime.get(this).registerWallHost(notificationInPlaceController);
            } catch (RuntimeException ignored) {
                // Notification setup is optional; normal Wall behavior continues.
            }
        }
        activityInForeground = true;
        if (wakeCoordinator != null) wakeCoordinator.setVoiceSettingsOpen(voiceSettingsOpen || chatModeOpen);
        shakeDetector.reset();
        if (sensorManager != null && shakeSensor != null) {
            sensorManager.registerListener(this, shakeSensor, SensorManager.SENSOR_DELAY_GAME);
        }
        if (mirrorController != null) {
            mirrorController.onResume();
        }
        if (dockModeController != null) {
            dockModeController.onResume();
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.beginForegroundSession();
            boolean powered = dockModeController != null && dockModeController.isWakePowered();
            wakeCoordinator.setWakeAllowed(powered);
            boolean granted = checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
            wakeCoordinator.setMicrophonePermission(granted);
            if (granted && powered) {
                checkWakeRecognitionSupport();
            }
        }
        maybeStartAssistantOneShot();
    }

    @Override
    protected void onPause() {
        if (naturalSpeechBackend != null) naturalSpeechBackend.stop();
        if (androidSpeechBackend != null) androidSpeechBackend.stop();
        recipeSession.cancelPending();
        if (recipePanel != null) recipePanel.close();
        if (notificationInPlaceController != null) {
            try {
                BoopNotificationRuntime.get(this).unregisterWallHost(notificationInPlaceController);
            } catch (RuntimeException ignored) {
                // Notification setup is optional; normal Wall behavior continues.
            }
        }
        activityInForeground = false;
        cancelFaceHolds();
        faceTouchActive = false;
        if (chatModeDialog != null) chatModeDialog.dismiss();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        shakeDetector.reset();
        assistantFollowUpAfterTts = false;
        sleepFaceAfterTts = false;
        assistantFollowUpListening = false;
        latestAssistantFollowUpPartial = null;
        cancelAssistantFollowUpSilenceTimeout();
        if (face != null) face.stopListeningCue();
        closeWakeAudioSession();
        if (presencePeekController != null) {
            presencePeekController.cancel();
        }
        if (mirrorController != null) {
            mirrorController.onPause();
        }
        if (dockModeController != null) {
            dockModeController.onPause();
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.endForegroundSession();
            wakeCoordinator.onTtsFinished();
        }
        if (haClient != null) haClient.cancelMusicClarification();
        com.boop.shared.BoopState.INSTANCE.speech(false, false);
        super.onPause();
    }

    private void applyDockWakePolicy(boolean powered) {
        if (wakeCoordinator == null) {
            return;
        }
        wakeCoordinator.setWakeAllowed(powered);
        if (powered
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            checkWakeRecognitionSupport();
        }
    }

    private void handleDockPresenceNudge() {
        if (dockModeController == null
                || !dockModeController.isWirelessDocked()
                || presenceState == null
                || !presenceState.isIdleBlack()
                || listening
                || thinking
                || voiceSettingsOpen
                || (mirrorController != null && mirrorController.isOpen())) {
            return;
        }
        if (presencePeekController != null
                && checkSelfPermission(Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
                && presencePeekController.peek()) {
            return;
        }
        wakeFaceForInteraction();
    }

    private void openMirror() {
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            pendingMirrorAfterPermission = true;
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
            return;
        }
        if (mirrorController == null || !mirrorController.open()) {
            speak("I can't open the mirror on this device.");
            return;
        }
        finishLocalWakeProcessing();
    }

    private void closeMirror() {
        if (mirrorController != null && mirrorController.isOpen()) {
            mirrorController.close();
        }
        finishLocalWakeProcessing();
        wakeFaceForInteraction();
    }

    private void finishLocalWakeProcessing() {
        if (wakeCoordinator != null
                && wakeCoordinator.state() == BoopWakeSessionState.State.PROCESSING) {
            wakeCoordinator.finishWakeProcessing();
        }
    }

    @Override
    public void onBackPressed() {
        if (recipeSession.active()) {
            recipeSession.close();
            if (recipePanel != null) recipePanel.close();
            return;
        }
        if (mirrorController != null && mirrorController.isOpen()) {
            closeMirror();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null || event.sensor != shakeSensor || event.values == null || event.values.length < 3) {
            return;
        }
        if (presenceState == null
                || !presenceState.isIdleBlack()
                || voiceSettingsOpen
                || listening
                || thinking) {
            shakeDetector.reset();
            return;
        }

        long timestampMs = event.timestamp / 1_000_000L;
        if (shakeDetector.onAccelerometer(
                event.values[0], event.values[1], event.values[2], timestampMs)) {
            handleShakeWake(shakeDetector.lastTriggerStrength());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private void handleShakeWake(float strength) {
        if (presenceState == null
                || face == null
                || !presenceState.isIdleBlack()
                || voiceSettingsOpen
                || listening
                || thinking) {
            return;
        }
        if (!presenceState.wake()) {
            return;
        }
        face.playShakeMuppet(strength);
        scheduleFaceIdle();
    }

    private void installTtsListener() {
        if (tts == null) {
            return;
        }
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) { }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(() -> finishTtsUtterance());
            }

            @Override
            public void onError(String utteranceId) {
                runOnUiThread(() -> finishTtsUtterance());
            }

            @Override
            public void onStop(String utteranceId, boolean interrupted) {
                runOnUiThread(() -> finishTtsUtterance());
            }
        });
    }

    private void finishTtsUtterance() {
        com.boop.shared.BoopState.INSTANCE.speech(false,false);
        boolean openAssistantFollowUp = assistantFollowUpAfterTts;
        boolean sleepAfterTts = sleepFaceAfterTts;
        assistantFollowUpAfterTts = false;
        sleepFaceAfterTts = false;

        // Preserve the proven hand-off: reserve the tap recognizer before releasing TTS,
        // so the wake-word engine cannot race the follow-up microphone.
        if (openAssistantFollowUp && wakeCoordinator != null) {
            wakeCoordinator.onTapStarted();
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.onTtsFinished();
        }
        if (assistantOneShot && !openAssistantFollowUp) {
            finishAssistantOneShot();
            return;
        }
        if (sleepAfterTts) {
            sleepFaceImmediately();
            return;
        }
        if (!openAssistantFollowUp) {
            return;
        }

        assistantFollowUpListening = true;
        latestAssistantFollowUpPartial = null;
        recognitionMode = RecognitionMode.TAP;
        startListening();
        scheduleAssistantFollowUpSilenceTimeout();
    }

    private void scheduleAssistantFollowUpSilenceTimeout() {
        if (presenceHandler == null
                || !assistantFollowUpListening
                || recognitionMode != RecognitionMode.TAP
                || !listening) {
            return;
        }
        presenceHandler.removeCallbacks(assistantFollowUpSilenceRunnable);
        presenceHandler.postDelayed(
                assistantFollowUpSilenceRunnable,
                ASSISTANT_FOLLOW_UP_SILENCE_MS);
    }

    private void cancelAssistantFollowUpSilenceTimeout() {
        if (presenceHandler != null) {
            presenceHandler.removeCallbacks(assistantFollowUpSilenceRunnable);
        }
    }

    private void sleepFaceImmediately() {
        if (presenceHandler != null) {
            presenceHandler.removeCallbacks(faceIdleRunnable);
        }
        if (presenceState != null) {
            presenceState.idle();
        }
        if (face != null) {
            face.goIdleBlack();
        }
    }

    private void playWakeAcceptedCue() {
        final ToneGenerator tone;
        try {
            tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 55);
        } catch (RuntimeException unavailable) {
            return;
        }
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 90);
        if (presenceHandler != null) {
            presenceHandler.postDelayed(() -> {
                try {
                    tone.release();
                } catch (RuntimeException ignored) {
                    // A cue must never interfere with waking BOOP.
                }
            }, 140L);
        } else {
            tone.release();
        }
    }

    private void finishAssistantFollowUpSilently() {
        if (!assistantFollowUpListening || recognitionMode != RecognitionMode.TAP) {
            return;
        }
        cancelAssistantFollowUpSilenceTimeout();
        assistantFollowUpListening = false;
        latestAssistantFollowUpPartial = null;
        suppressNextRecognizerError = true;
        recognitionMode = RecognitionMode.NONE;
        listening = false;
        com.boop.shared.BoopState.INSTANCE.speech(false,false);
        if (recognizer != null) {
            recognizer.cancel();
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.onTapFinished();
        }
        sleepFaceImmediately();
    }

    private void createWakeObjects() {
        wakeWordController = new BoopWakeWordController(this, new BoopWakeWordController.Listener() {
            @Override
            public void onWakeDetected(BoopWakeAudioSession session, long detectedAtMs) {
                runOnUiThread(() -> {
                    if (wakeCoordinator == null || !wakeCoordinator.onWakeDetected(detectedAtMs)) {
                        session.close();
                        return;
                    }
                    wakeFaceForInteraction();
                    // BOOP_WAKE_COMMAND_SEAM_V1
                    // Keep the wake-to-command handoff acoustically silent. The
                    // live microphone is already capturing the user's command.
                    startWakeRecognition(session);
                });
            }

            @Override
            public void onWakeFailure(String message) {
                runOnUiThread(() -> {
                    lastWakeDiagnosticSummary = "WAKE ENGINE FAILURE\n" + message;
                    if (wakeCoordinator != null) {
                        wakeCoordinator.failWakeSession();
                        updateWakeRecoverySnapshot();
                    }
                });
            }
        });

        wakeCoordinator = new BoopWakeSessionCoordinator(new BoopWakeSessionCoordinator.Engine() {
            @Override public boolean arm() {
                return wakeWordController != null && wakeWordController.arm();
            }

            @Override public void suspendAll() {
                if (wakeWordController != null) {
                    wakeWordController.suspendAll();
                }
            }

            @Override public void reload() {
                if (wakeWordController != null) {
                    wakeWordController.reloadSensitivity();
                }
            }

            @Override public void shutdown() {
                if (wakeWordController != null) {
                    wakeWordController.shutdown();
                }
            }
        });
    }

    private boolean onFaceTouch(View view, MotionEvent event) {
        if (voiceSettingsOpen || chatModeOpen) return true;
        int action = event.getActionMasked();
        if (eyeHueOverlay != null && eyeHueOverlay.isVisible()) {
            if (action == MotionEvent.ACTION_DOWN && !eyeHueOverlay.isSliderTouch(event)) {
                eyeHueOverlay.hide();
                if (wakeCoordinator != null) wakeCoordinator.setVoiceSettingsOpen(false);
                wakeFaceForInteraction();
                scheduleFaceIdle();
            }
            return true;
        }
        if (action == MotionEvent.ACTION_DOWN) {
            cancelFaceHolds();
            faceTouchActive = true;
            faceGestureMoved = false;
            memberBerryConsumed = false;
            swipeHadMultiplePointers = false;
            faceTouchDownX = event.getX();
            faceTouchDownY = event.getY();
            wakeFaceForInteraction();
            scheduleMemberBerryHold();
            chatModeHold.begin(android.os.SystemClock.uptimeMillis(),
                    faceTouchDownX, faceTouchDownY,
                    ViewConfiguration.get(this).getScaledTouchSlop());
            if (presenceHandler != null) {
                presenceHandler.postDelayed(chatModeHoldRunnable, BoopChatModeHold.HOLD_MS);
            }
            return true;
        }
        if (action == MotionEvent.ACTION_POINTER_DOWN) {
            swipeHadMultiplePointers = true;
            cancelFaceHolds();
            return true;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            chatModeHold.move(event.getX(), event.getY(), event.getPointerCount());
            float movedX = Math.abs(event.getX() - faceTouchDownX);
            float movedY = Math.abs(event.getY() - faceTouchDownY);
            if (event.getPointerCount() != 1) swipeHadMultiplePointers = true;
            if (swipeHadMultiplePointers
                    || movedX > ViewConfiguration.get(this).getScaledTouchSlop()
                    || movedY > ViewConfiguration.get(this).getScaledTouchSlop()) {
                faceGestureMoved = true;
                cancelFaceHolds();
            }
            return true;
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            cancelFaceHolds();
            faceTouchActive = false;
            memberBerryConsumed = false;
            swipeHadMultiplePointers = false;
            return true;
        }
        if (action != MotionEvent.ACTION_UP) return true;
        cancelFaceHolds();
        if (!faceTouchActive) return true;
        faceTouchActive = false;
        if (memberBerryConsumed) {
            memberBerryConsumed = false;
            return true;
        }
        if (BoopLauncherSwipeGesture.shouldOpenLauncher(
                faceTouchDownX, faceTouchDownY, event.getX(), event.getY(), false,
                swipeHadMultiplePointers, dp(96))) {
            swipeHadMultiplePointers = false;
            launchLauncher();
            return true;
        }
        boolean ignoreRelease = swipeHadMultiplePointers || faceGestureMoved;
        swipeHadMultiplePointers = false;
        if (ignoreRelease) return true;
        if (listening) stopListening();
        else beginTapToSpeak();
        return true;
    }

    private void showEyeHueControl() {
        if (eyeHueOverlay == null || eyeHueOverlay.isVisible() || voiceSettingsOpen || chatModeOpen) {
            return;
        }
        cancelFaceHolds();
        assistantFollowUpAfterTts = false;
        sleepFaceAfterTts = false;
        cancelAssistantFollowUpSilenceTimeout();
        if (listening) {
            suppressNextRecognizerError = true;
            stopListening();
        }
        if (tts != null) tts.stop();
        wakeFaceForInteraction();
        interactionSurface.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        eyeHueOverlay.show();
        if (wakeCoordinator != null) wakeCoordinator.setVoiceSettingsOpen(true);
        scheduleFaceIdle();
    }

    private void cancelFaceHolds() {
        cancelMemberBerryHold();
        chatModeHold.cancel();
        if (presenceHandler != null) presenceHandler.removeCallbacks(chatModeHoldRunnable);
    }

    private void showChatModeMenu() {
        if (chatModeOpen || voiceSettingsOpen || !activityInForeground || chatModeStore == null) return;
        cancelFaceHolds();
        memberBerryConsumed = true;
        chatModeOpen = true;
        assistantFollowUpAfterTts = false;
        sleepFaceAfterTts = false;
        cancelAssistantFollowUpSilenceTimeout();
        if (wakeCoordinator != null) wakeCoordinator.setVoiceSettingsOpen(true);
        if (listening) {
            suppressNextRecognizerError = true;
            stopListening();
        }
        if (tts != null) tts.stop();
        interactionSurface.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

        LinearLayout choices = new LinearLayout(this);
        choices.setOrientation(LinearLayout.VERTICAL);
        choices.setPadding(dp(20), dp(8), dp(20), dp(8));
        choices.addView(chatModeButton(BoopChatMode.OPENCODE, "OpenCode"));
        choices.addView(chatModeButton(BoopChatMode.NATIVE_CHAT, "ChatGPT"));
        choices.addView(chatModeButton(BoopChatMode.FREE_CHAT, "Free Chat"));
        TextView explanation = voiceSettingLabel(
                "ChatGPT answers inside BOOP when its private relay is set up. Free Chat opens the browser and copies your question.",
                17f, false);
        explanation.setPadding(0, dp(14), 0, dp(8));
        choices.addView(explanation);
        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        scroll.addView(choices);
        chatModeDialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle("Chat mode")
                .setView(scroll)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create();
        chatModeDialog.setOnDismissListener(dialog -> {
            chatModeDialog = null;
            chatModeOpen = false;
            if (wakeCoordinator != null) wakeCoordinator.setVoiceSettingsOpen(voiceSettingsOpen);
            if (activityInForeground) wakeFaceForInteraction();
        });
        chatModeDialog.show();
    }

    private Button chatModeButton(BoopChatMode mode, String label) {
        Button button = new Button(this);
        button.setAllCaps(false);
        boolean selected = chatModeStore.load() == mode;
        button.setText(selected ? label + "  \u2713" : label);
        button.setTextSize(22f);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(selected ? Color.rgb(28, 76, 86) : Color.rgb(42, 42, 42));
        button.setContentDescription("Use " + label);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(64));
        params.setMargins(0, dp(6), 0, dp(6));
        button.setLayoutParams(params);
        button.setOnClickListener(view -> {
            BoopChatMode previous = chatModeStore.load();
            if (!chatModeStore.save(mode)) {
                Toast.makeText(this, "That choice didn't save. Try again.", Toast.LENGTH_LONG).show();
                return;
            }
            if (previous != mode) chatModeRevision++;
            stopAssistantThinking();
            if (chatModeDialog != null) chatModeDialog.dismiss();
            Toast.makeText(this, label + " selected.", Toast.LENGTH_SHORT).show();
        });
        return button;
    }

    private void openFreeChat(String question) {
        cancelFaceHolds();
        assistantFollowUpAfterTts = false;
        sleepFaceAfterTts = false;
        cancelAssistantFollowUpSilenceTimeout();
        stopAssistantThinking();
        if (wakeCoordinator != null) {
            wakeCoordinator.setVoiceSettingsOpen(true);
            wakeCoordinator.finishWakeProcessing();
        }
        if (!BoopFreeChat.open(this, question)) {
            if (wakeCoordinator != null) wakeCoordinator.setVoiceSettingsOpen(false);
            speak("I couldn't open Free Chat. Please check that a browser is installed.");
        }
    }

    private void launchLauncher() {
        Intent launcherIntent = new Intent().setClassName(getPackageName(), "com.boop.launcher.MainActivity");
        if (launcherIntent == null) {
            Toast.makeText(this, "BOOP Launcher is not installed.", Toast.LENGTH_SHORT).show();
            return;
        }
        android.app.ActivityOptions options = android.app.ActivityOptions.makeCustomAnimation(
                this, R.anim.boop_launcher_enter_from_right, R.anim.boop_wall_exit_to_left);
        startActivity(launcherIntent, options.toBundle());
    }

    private void scheduleMemberBerryHold() {
        cancelMemberBerryHold();
        if (presenceHandler != null && !listening && !voiceSettingsOpen) {
            presenceHandler.postDelayed(
                    memberBerryRunnable,
                    ViewConfiguration.getLongPressTimeout());
        }
    }

    private void cancelMemberBerryHold() {
        if (presenceHandler != null) {
            presenceHandler.removeCallbacks(memberBerryRunnable);
        }
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

    private void startAssistantThinking() {
        runOnUiThread(() -> {
            thinking = true;
            wakeFaceForInteraction();
            if (face != null) {
                face.startThinking();
            }
            scheduleFaceIdle();
        });
    }

    private void stopAssistantThinking() {
        runOnUiThread(() -> {
            thinking = false;
            if (face != null) {
                face.stopThinking();
            }
            scheduleFaceIdle();
        });
    }

    private void showVoiceSettings() {
        if (interactionSurface == null || voiceController == null || voiceSettingsOpen || chatModeOpen) {
            return;
        }

        if (face != null) face.setVisibility(View.INVISIBLE);
        voiceSettingsOpen = true;
        if (face != null) face.setOccluded("voice_settings", true);
        if (wakeCoordinator != null) {
            wakeCoordinator.setVoiceSettingsOpen(true);
        }

        voiceSettingsOverlay = new LinearLayout(this);
        voiceSettingsOverlay.setOrientation(LinearLayout.VERTICAL);
        voiceSettingsOverlay.setGravity(Gravity.CENTER);
        voiceSettingsOverlay.setPadding(dp(42), dp(40), dp(42), dp(40));
        voiceSettingsOverlay.setBackgroundColor(Color.BLACK);
        voiceSettingsOverlay.setClickable(true);
        voiceSettingsOverlay.setContentDescription("BOOP voice settings");

        TextView title = voiceSettingLabel("Voice", 30f, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, dp(28));
        voiceSettingsOverlay.addView(title, titleParams);

        TextView wakeNameLabel = voiceSettingLabel("BOOP's name", 22f, false);
        voiceSettingsOverlay.addView(wakeNameLabel);
        wakeNameInput = new EditText(this);
        wakeNameInput.setSingleLine(true);
        wakeNameInput.setText(BoopWakeNameStore.load(this));
        wakeNameInput.setSelectAllOnFocus(true);
        wakeNameInput.setTextColor(Color.WHITE);
        wakeNameInput.setHintTextColor(Color.GRAY);
        wakeNameInput.setHint("BOOP");
        wakeNameInput.setTextSize(22f);
        wakeNameInput.setContentDescription("BOOP's spoken wake name");
        wakeNameInput.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) saveWakeNameFromSettings();
        });
        LinearLayout.LayoutParams wakeNameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(60));
        wakeNameParams.setMargins(0, dp(4), 0, dp(8));
        voiceSettingsOverlay.addView(wakeNameInput, wakeNameParams);

        wakeNameTrainButton = new Button(this);
        wakeNameTrainButton.setText("Train wake name · say it 5 times");
        wakeNameTrainButton.setTextSize(18f);
        wakeNameTrainButton.setAllCaps(false);
        wakeNameTrainButton.setContentDescription("Train custom wake name five times");
        wakeNameTrainButton.setOnClickListener(view -> {
            saveWakeNameFromSettings();
            pendingWakeEnrollmentAfterSettings = null;
            String selected = BoopWakeNameStore.load(this);
            if (BoopWakeName.isDefault(selected)) speak("BOOP is always ready.");
            else promptWakeNameEnrollment(selected);
        });
        LinearLayout.LayoutParams wakeTrainParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(60));
        voiceSettingsOverlay.addView(wakeNameTrainButton, wakeTrainParams);
        wakeNameTrainingStatus = voiceSettingLabel(wakeNameTrainingStatusText(), 18f, false);
        LinearLayout.LayoutParams wakeStatusParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        wakeStatusParams.setMargins(0, dp(4), 0, dp(22));
        voiceSettingsOverlay.addView(wakeNameTrainingStatus, wakeStatusParams);

        TextView pitchLabel = voiceSettingLabel("Pitch", 22f, false);
        voiceSettingsOverlay.addView(pitchLabel);

        SeekBar pitchSlider = new SeekBar(this);
        pitchSlider.setMax(BoopVoiceTuning.PROGRESS_MAX);
        pitchSlider.setProgress(BoopVoiceTuning.progressFromPitch(voiceController.pitch()));
        pitchSlider.setContentDescription("Voice pitch");
        pitchSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    voiceController.setPitch(BoopVoiceTuning.pitchFromProgress(progress));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        LinearLayout.LayoutParams pitchParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64));
        pitchParams.setMargins(0, dp(4), 0, dp(24));
        voiceSettingsOverlay.addView(pitchSlider, pitchParams);

        TextView cadenceLabel = voiceSettingLabel("Cadence", 22f, false);
        voiceSettingsOverlay.addView(cadenceLabel);

        SeekBar cadenceSlider = new SeekBar(this);
        cadenceSlider.setMax(BoopVoiceTuning.PROGRESS_MAX);
        cadenceSlider.setProgress(BoopVoiceTuning.progressFromRate(voiceController.speechRate()));
        cadenceSlider.setContentDescription("Voice cadence");
        cadenceSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    voiceController.setSpeechRate(BoopVoiceTuning.rateFromProgress(progress));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        LinearLayout.LayoutParams cadenceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64));
        cadenceParams.setMargins(0, dp(4), 0, dp(28));
        voiceSettingsOverlay.addView(cadenceSlider, cadenceParams);

        Button testVoice = new Button(this);
        testVoice.setText("TEST VOICE");
        testVoice.setTextSize(19f);
        testVoice.setTextColor(Color.WHITE);
        testVoice.setBackgroundColor(Color.rgb(42, 42, 42));
        testVoice.setContentDescription("Test current BOOP voice");
        testVoice.setOnClickListener(v -> testCurrentVoice());
        LinearLayout.LayoutParams testVoiceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(60));
        testVoiceParams.setMargins(0, dp(6), 0, dp(12));
        voiceSettingsOverlay.addView(testVoice, testVoiceParams);

        TextView naturalStatus = addNaturalVoiceSettings();
        BoopVoiceSharingControls.install(this, voiceSettingsOverlay, voiceController,
                pitchSlider, cadenceSlider, naturalStatus);

        TextView wakeSensitivityLabel = voiceSettingLabel("Wake sensitivity", 22f, false);
        voiceSettingsOverlay.addView(wakeSensitivityLabel);

        SeekBar wakeSensitivitySlider = new SeekBar(this);
        wakeSensitivitySlider.setMax(BoopWakeSensitivity.PROGRESS_MAX);
        wakeSensitivitySlider.setProgress(BoopWakeSensitivity.loadProgress(this));
        wakeSensitivitySlider.setContentDescription("Wake sensitivity");
        wakeSensitivitySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    BoopWakeSensitivity.saveProgress(MainActivity.this, progress);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        LinearLayout.LayoutParams wakeSensitivityParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64));
        wakeSensitivityParams.setMargins(0, dp(4), 0, dp(28));
        voiceSettingsOverlay.addView(wakeSensitivitySlider, wakeSensitivityParams);

        // BOOP_NOTIFICATION_SETTINGS_ENTRY_V1
        Button notifications = new Button(this);
        notifications.setText("Notifications");
        notifications.setTextSize(19f);
        notifications.setTextColor(Color.WHITE);
        notifications.setBackgroundColor(Color.rgb(42, 42, 42));
        notifications.setContentDescription("Notification settings");
        notifications.setOnClickListener(v -> {
            hideVoiceSettings();
            startActivity(new Intent().setClassName(
                    getPackageName(), "com.boop.alpha1.BoopNotificationSettingsActivity"));
        });
        LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64));
        notificationParams.setMargins(0, 0, 0, dp(12));
        voiceSettingsOverlay.addView(notifications, notificationParams);

        // BOOP_DEVELOPER_MENU_SETTINGS_ENTRY_V4
        Button devMenu = new Button(this);
        devMenu.setText("Developer menu");
        devMenu.setTextSize(19f);
        devMenu.setTextColor(Color.WHITE);
        devMenu.setBackgroundColor(Color.rgb(42, 42, 42));
        devMenu.setContentDescription("Open BOOP developer demos");
        devMenu.setOnClickListener(v -> {
            hideVoiceSettings();
            showDeveloperMenu();
        });
        LinearLayout.LayoutParams devMenuParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64));
        devMenuParams.setMargins(0, 0, 0, dp(12));
        voiceSettingsOverlay.addView(devMenu, devMenuParams);

        Button done = new Button(this);
        done.setText("Done");
        done.setTextSize(21f);
        done.setTextColor(Color.WHITE);
        done.setBackgroundColor(Color.rgb(42, 42, 42));
        done.setContentDescription("Done adjusting voice");
        done.setOnClickListener(v -> hideVoiceSettings());
        LinearLayout.LayoutParams doneParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64));
        Button profiles = new Button(this);
        profiles.setText("Device and room settings"); profiles.setTextSize(20f);
        profiles.setOnClickListener(v -> startActivity(new Intent(this,BoopProfileActivity.class)));
        voiceSettingsOverlay.addView(profiles);
        voiceSettingsOverlay.addView(done, doneParams);

        // BOOP_VOICE_SETTINGS_SCROLL_V70_FIX
        voiceSettingsScroll = new ScrollView(this);
        voiceSettingsScroll.setFillViewport(true);
        voiceSettingsScroll.setBackgroundColor(Color.argb(236, 0, 0, 0));
        voiceSettingsScroll.setContentDescription("Scrollable BOOP voice settings");
        BoopReadableControls.fitText(voiceSettingsOverlay);
        voiceSettingsScroll.addView(voiceSettingsOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
        interactionSurface.addView(voiceSettingsScroll, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        voiceSettingsScroll.bringToFront();
        com.boop.shieldhome.BoopTvChrome.prepareVoiceSettings(
                voiceSettingsScroll, voiceSettingsOverlay, pitchSlider);
        scheduleFaceIdle();
    }

    private void testCurrentVoice() {
        final int previewGeneration = ++naturalPreviewGeneration;
        final String phrase = "This is how BOOP sounds.";
        if (wakeCoordinator != null) {
            wakeCoordinator.onTtsStarting();
        }
        if (voiceController != null
                && voiceController.naturalBackendSelectedAndUsable()
                && naturalSpeechBackend != null) {
            BoopVoiceController.NaturalVoice voice = voiceController.selectedNaturalVoice();
            if (voice != null) {
                boolean started = naturalSpeechBackend.speak(
                        phrase,
                        voice.sid(),
                        voiceController.pitch(),
                        voiceController.speechRate(),
                        new BoopSpeechBackend.Callback() {
                            @Override public void onCancelled() { }
                            @Override public void onDone() {
                                runOnUiThread(() -> {
                                    if (previewGeneration == naturalPreviewGeneration && voiceSettingsOpen
                                            && activityInForeground && !isFinishing() && !isDestroyed()) finishTtsUtterance();
                                });
                            }

                            @Override public void onError(Throwable error) {
                                android.util.Log.w(
                                        "BOOP-NaturalVoice",
                                        "Natural voice demo failed; using Android TTS",
                                        error);
                                runOnUiThread(() -> {
                                    if (previewGeneration == naturalPreviewGeneration && voiceSettingsOpen
                                            && activityInForeground && !isFinishing() && !isDestroyed()) speakWithAndroidTts(phrase);
                                });
                            }
                        });
                if (started) return;
            }
        }
        speakWithAndroidTts(phrase);
    }

    private TextView addNaturalVoiceSettings() {
        TextView naturalTitle = new TextView(this);
        naturalTitle.setText("Natural voices");
        naturalTitle.setTextColor(Color.WHITE);
        naturalTitle.setTextSize(22f);
        naturalTitle.setGravity(Gravity.CENTER);
        naturalTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams naturalTitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        naturalTitleParams.setMargins(0, 0, 0, dp(8));
        voiceSettingsOverlay.addView(naturalTitle, naturalTitleParams);

        TextView naturalStatus = new TextView(this);
        naturalStatus.setTextColor(Color.LTGRAY);
        naturalStatus.setTextSize(16f);
        naturalStatus.setGravity(Gravity.CENTER);
        naturalStatus.setPadding(0, 0, 0, dp(10));
        voiceSettingsOverlay.addView(naturalStatus);

        Button naturalDownload = new Button(this);
        naturalDownload.setText("Download natural voices");
        naturalDownload.setTextSize(18f);
        naturalDownload.setTextColor(Color.WHITE);
        naturalDownload.setBackgroundColor(Color.rgb(42, 42, 42));
        naturalDownload.setContentDescription("Download natural voices for offline use");
        LinearLayout.LayoutParams naturalButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(60));
        naturalButtonParams.setMargins(0, 0, 0, dp(8));
        voiceSettingsOverlay.addView(naturalDownload, naturalButtonParams);

        Button naturalCancel = new Button(this);
        naturalCancel.setText("Cancel download");
        naturalCancel.setTextSize(17f);
        naturalCancel.setTextColor(Color.WHITE);
        naturalCancel.setBackgroundColor(Color.rgb(42, 42, 42));
        naturalCancel.setContentDescription("Cancel natural voice download");
        naturalCancel.setVisibility(View.GONE);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56));
        cancelParams.setMargins(0, 0, 0, dp(8));
        voiceSettingsOverlay.addView(naturalCancel, cancelParams);

        Button emma = new Button(this);
        emma.setText("Emma");
        prepareNaturalVoiceButton(emma, "bf_emma", "Hello. I'm Emma.", naturalStatus);
        voiceSettingsOverlay.addView(emma, naturalVoiceButtonParams());

        Button isabella = new Button(this);
        isabella.setText("Isabella");
        prepareNaturalVoiceButton(isabella, "bf_isabella", "Hello. I'm Isabella.", naturalStatus);
        voiceSettingsOverlay.addView(isabella, naturalVoiceButtonParams());

        Button george = new Button(this);
        george.setText("George");
        prepareNaturalVoiceButton(george, "bm_george", "Hello. I'm George.", naturalStatus);
        voiceSettingsOverlay.addView(george, naturalVoiceButtonParams());

        Button fable = new Button(this);
        fable.setText("Fable");
        prepareNaturalVoiceButton(fable, "bm_fable", "Hello. I'm Fable.", naturalStatus);
        voiceSettingsOverlay.addView(fable, naturalVoiceButtonParams());

        Button[] naturalChoices = {emma, isabella, george, fable};
        boolean installed = naturalVoicePack != null && naturalVoicePack.isInstalled();
        if (voiceController != null) voiceController.setNaturalPackUsable(installed);
        setNaturalVoiceChoicesVisible(naturalChoices, installed);
        naturalDownload.setVisibility(installed ? View.GONE : View.VISIBLE);
        naturalStatus.setText(installed
                ? "Downloaded. Pick a voice below."
                : "Optional. Downloads once, then speaks locally on this device.");

        naturalCancel.setOnClickListener(v -> {
            if (naturalVoiceDownloader != null) naturalVoiceDownloader.cancel();
        });

        naturalDownload.setOnClickListener(v -> {
            if (naturalVoiceDownloader == null || naturalVoiceManifest == null || naturalVoicePack == null) {
                naturalStatus.setText("Natural voices aren't available on this device.");
                return;
            }
            naturalDownload.setEnabled(false);
            naturalCancel.setVisibility(View.VISIBLE);
            boolean started = naturalVoiceDownloader.start(new BoopNaturalVoiceDownloader.Listener() {
                @Override
                public void onStatus(String status) {
                    runOnUiThread(() -> naturalStatus.setText(status));
                }

                @Override
                public void onProgress(long downloadedBytes, long totalBytes) {
                    long safeTotal = Math.max(1L, totalBytes);
                    int percent = (int) Math.max(0L, Math.min(100L, downloadedBytes * 100L / safeTotal));
                    runOnUiThread(() -> naturalStatus.setText("Downloading natural voices… " + percent + "%"));
                }

                @Override
                public void onReady() {
                    runOnUiThread(() -> {
                        if (voiceController != null && naturalVoiceManifest != null) {
                            voiceController.onNaturalPackVerified(naturalVoiceManifest.version());
                            voiceController.setNaturalPackUsable(true);
                        }
                        naturalStatus.setText("Natural voices ready. Pick one below.");
                        naturalDownload.setVisibility(View.GONE);
                        naturalCancel.setVisibility(View.GONE);
                        setNaturalVoiceChoicesVisible(naturalChoices, true);
                    });
                }

                @Override
                public void onCancelled() {
                    runOnUiThread(() -> {
                        naturalStatus.setText("Download cancelled. Android voice is unchanged.");
                        naturalDownload.setEnabled(true);
                        naturalDownload.setVisibility(View.VISIBLE);
                        naturalCancel.setVisibility(View.GONE);
                        setNaturalVoiceChoicesVisible(naturalChoices, false);
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        naturalStatus.setText(message);
                        naturalDownload.setEnabled(true);
                        naturalDownload.setVisibility(View.VISIBLE);
                        naturalCancel.setVisibility(View.GONE);
                        setNaturalVoiceChoicesVisible(naturalChoices, false);
                        if (voiceController != null) voiceController.setNaturalPackUsable(false);
                    });
                }
            });
            if (!started) {
                naturalDownload.setEnabled(true);
                naturalCancel.setVisibility(View.GONE);
            }
        });
        return naturalStatus;
    }

    private LinearLayout.LayoutParams naturalVoiceButtonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58));
        params.setMargins(0, 0, 0, dp(6));
        return params;
    }

    private void prepareNaturalVoiceButton(Button button, String key, String preview, TextView naturalStatus) {
        button.setTextSize(18f);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.rgb(42, 42, 42));
        button.setVisibility(View.GONE);
        button.setOnClickListener(v -> previewNaturalVoice(key, preview, naturalStatus, button));
    }

    private void previewNaturalVoice(String key, String text, TextView naturalStatus, Button button) {
        // v200 remove preview face wake: Voice Settings owns the whole screen.
        final int previewGeneration = ++naturalPreviewGeneration;
        if (wakeCoordinator != null) {
            wakeCoordinator.onTtsStarting();
        }
        BoopVoiceController.NaturalVoice voice = BoopVoiceController.findNaturalVoice(key);
        if (voiceController == null
                || !voiceController.naturalPackReadyForPreview()
                || naturalSpeechBackend == null
                || voice == null) {
            finishTtsUtterance();
            naturalStatus.setText("Natural voice isn't ready. Android voice kept.");
            android.widget.Toast.makeText(
                    this,
                    "Natural voice isn't ready.",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        naturalStatus.setText("Trying " + button.getText() + "…");
        boolean started = naturalSpeechBackend.speak(
                text,
                voice.sid(),
                voiceController.pitch(),
                voiceController.speechRate(),
                new BoopSpeechBackend.Callback() {
                    @Override public void onCancelled() { }
                    @Override
                    public void onDone() {
                        runOnUiThread(() -> {
                            if (previewGeneration != naturalPreviewGeneration || !voiceSettingsOpen
                                    || !activityInForeground || isFinishing() || isDestroyed()) return;
                            if (voiceController.selectNaturalVoice(key)) {
                                voiceController.markNaturalPlaybackProven();
                                naturalStatus.setText("Selected: " + button.getText());
                            } else {
                                naturalStatus.setText("Natural voice wasn't selected. Android voice kept.");
                            }
                            finishTtsUtterance();
                        });
                    }

                    @Override
                    public void onError(Throwable error) {
                        android.util.Log.w(
                                "BOOP-NaturalVoice",
                                "Natural voice preview failed; Android voice remains active",
                                error);
                        runOnUiThread(() -> {
                            if (previewGeneration != naturalPreviewGeneration || !voiceSettingsOpen
                                    || !activityInForeground || isFinishing() || isDestroyed()) return;
                            String stage = "speech";
                            if (error instanceof BoopNaturalSpeechBackend.NaturalSpeechException) {
                                stage = ((BoopNaturalSpeechBackend.NaturalSpeechException) error).stage();
                            }
                            finishTtsUtterance();
                            naturalStatus.setText("Natural voice " + stage + " failed. Android voice kept.");
                            showNaturalVoiceDevDiagnostic(error);
                            android.widget.Toast.makeText(
                                    MainActivity.this,
                                    "Natural voice " + stage + " failed.",
                                    android.widget.Toast.LENGTH_SHORT).show();
                        });
                    }
                });
        if (!started) {
            finishTtsUtterance();
            naturalStatus.setText("Natural voice couldn't start. Android voice kept.");
            android.widget.Toast.makeText(
                    this,
                    "Natural voice couldn't start.",
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    // BOOP_NATURAL_RUNTIME_DIAGNOSTICS_V89
    private void showNaturalVoiceDevDiagnostic(Throwable error) {
        if (!BuildConfig.DEBUG || error == null || isFinishing()) return;

        String stage = "unknown";
        if (error instanceof BoopNaturalSpeechBackend.NaturalSpeechException) {
            stage = ((BoopNaturalSpeechBackend.NaturalSpeechException) error).stage();
        }

        String code = "BOOP DEV E899";
        String explanation = "Natural voice failed at an unknown stage.";
        if ("files".equals(stage)) {
            code = "BOOP DEV E890";
            explanation = "Natural voice files are incomplete.";
        } else if ("initialization".equals(stage)) {
            code = "BOOP DEV E891";
            explanation = "Kokoro could not open the voice model.";
        } else if ("synthesis".equals(stage)) {
            code = "BOOP DEV E892";
            explanation = "Kokoro opened, but speech generation failed.";
        } else if ("playback".equals(stage)) {
            code = "BOOP DEV E893";
            explanation = "Speech was generated, but Android audio playback failed.";
        }

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(32), dp(32), dp(32), dp(32));
        panel.setBackgroundColor(Color.BLACK);

        TextView codeView = new TextView(this);
        codeView.setText(code);
        codeView.setTextColor(Color.WHITE);
        codeView.setTextSize(42f);
        codeView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        codeView.setGravity(Gravity.CENTER);
        panel.addView(codeView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView explanationView = new TextView(this);
        explanationView.setText(explanation);
        explanationView.setTextColor(Color.WHITE);
        explanationView.setTextSize(25f);
        explanationView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams explanationParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        explanationParams.setMargins(0, dp(18), 0, 0);
        panel.addView(explanationView, explanationParams);

        TextView detailView = new TextView(this);
        detailView.setText(naturalVoiceRootCause(error));
        detailView.setTextColor(Color.LTGRAY);
        detailView.setTextSize(18f);
        detailView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        detailParams.setMargins(0, dp(18), 0, 0);
        panel.addView(detailView, detailParams);

        Button close = new Button(this);
        close.setText("Close");
        close.setTextSize(22f);
        close.setTextColor(Color.WHITE);
        close.setBackgroundColor(Color.rgb(42, 42, 42));
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64));
        closeParams.setMargins(0, dp(28), 0, 0);
        panel.addView(close, closeParams);

        android.app.Dialog dialog = new android.app.Dialog(
                this,
                android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
        dialog.setContentView(panel);
        close.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private String naturalVoiceRootCause(Throwable error) {
        Throwable root = error;
        for (int depth = 0; depth < 8 && root.getCause() != null && root.getCause() != root; depth++) {
            root = root.getCause();
        }
        String type = root.getClass().getSimpleName();
        if (type == null || type.isEmpty()) type = "Runtime failure";
        String message = root.getMessage();
        if (message == null || message.trim().isEmpty()) message = "No detail from runtime.";
        message = message.replace('\n', ' ').replace('\r', ' ').trim();
        message = message.replaceAll("https?://\\S+", "[url]");
        message = message.replaceAll("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b", "[address]");
        message = message.replaceAll("/data/(?:user/\\d+|data)/[^/\\s]+", "[app storage]");
        if (message.length() > 180) message = message.substring(0, 180) + "…";
        return type + ": " + message;
    }

    private void setNaturalVoiceChoicesVisible(Button[] buttons, boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        for (Button button : buttons) button.setVisibility(visibility);
    }

    // BOOP_DEVELOPER_MENU_IN_PLACE_V4
    private void showDeveloperMenu() {
        if (developerMenuOpen && developerMenuOverlay != null) {
            showDeveloperMenuContent();
            return;
        }
        if (interactionSurface == null) {
            return;
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.finishWakeProcessing();
        }
        if (voiceSettingsOpen) {
            hideVoiceSettings();
        }
        cancelMemberBerryHold();
        developerMenuOpen = true;
        if (face != null) face.setOccluded("developer", true);
        developerMenuOverlay = new FrameLayout(this);
        developerMenuOverlay.setBackgroundColor(Color.BLACK);
        developerMenuOverlay.setClickable(true);
        developerMenuOverlay.setContentDescription("BOOP developer menu");
        interactionSurface.addView(developerMenuOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        developerMenuOverlay.bringToFront();
        showDeveloperMenuContent();
    }

    private void showDeveloperMenuContent() {
        if (!developerMenuOpen || developerMenuOverlay == null) {
            return;
        }
        stopDeveloperAnimation();
        developerMenuFace = null;
        developerMenuOverlay.removeAllViews();

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        column.setBackgroundColor(Color.BLACK);
        column.setPadding(dp(24), dp(14), dp(24), dp(14));
        ScrollView menuScroll = new ScrollView(this);
        menuScroll.setFillViewport(true);
        menuScroll.setContentDescription("Scrollable BOOP developer menu");
        developerMenuOverlay.addView(menuScroll, new FrameLayout.LayoutParams(-1, -1));
        menuScroll.addView(column, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));

        TextView title = voiceSettingLabel("BOOP Dev Lab", 26f, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, dp(4));
        column.addView(title, titleParams);

        developerMenuFace = new BoopCanonicalFaceView(this);
        LinearLayout.LayoutParams faceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(160),
                1.0f);
        faceParams.setMargins(0, 0, 0, dp(4));
        column.addView(developerMenuFace, faceParams);
        developerMenuFace.post(() -> {
            if (!developerMenuOpen || developerMenuFace == null) {
                return;
            }
            developerMenuFace.showIdleBlackImmediately();
            developerMenuFace.wakeFromIdle();
        });

        Button remoteTest = new Button(this);
        remoteTest.setText("Test notifications from Shield");
        remoteTest.setOnClickListener(v -> startActivity(new android.content.Intent(this, BoopNotificationLabActivity.class)));
        column.addView(remoteTest, new LinearLayout.LayoutParams(-1, dp(56)));

        addCanonicalDeveloperShelf(column);

        for (BoopDevMenuModel.Shelf shelf : BoopDevMenuModel.shelves()) {
            boolean animationShelf = "Animations".equals(shelf.title());
            addDeveloperShelf(
                    column,
                    animationShelf ? "Animations" : "Notification doods",
                    shelf,
                    animationShelf);
        }

        Button done = new Button(this);
        done.setText("Done");
        done.setTextSize(20f);
        done.setTextColor(Color.WHITE);
        done.setBackgroundColor(Color.rgb(42, 42, 42));
        done.setOnClickListener(v -> hideDeveloperMenu());
        LinearLayout.LayoutParams doneParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56));
        doneParams.setMargins(0, dp(4), 0, 0);
        column.addView(done, doneParams);
        BoopReadableControls.fitText(column);
    }


    private void addCanonicalDeveloperShelf(LinearLayout column) {
        addDeveloperSection(column, "Canonical animations");
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setContentDescription("Canonical animations horizontal selector");
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (com.boop.eyes.EyeMotion.Clip clip : com.boop.eyes.EyeCatalogue.ALL) {
            Button button = new Button(this);
            button.setText(clip.label); button.setAllCaps(false);
            button.setTextSize(18f); button.setTextColor(Color.WHITE);
            button.setBackgroundColor(Color.rgb(42, 42, 42));
            button.setContentDescription(clip.label + " canonical animation");
            button.setOnClickListener(v -> {
                if (developerMenuFace != null) developerMenuFace.playCanonicalClip(clip.id);
            });
            LinearLayout.LayoutParams item = new LinearLayout.LayoutParams(dp(180), dp(64));
            item.setMargins(0, 0, dp(12), 0); row.addView(button, item);
        }
        scroll.addView(row);
        scroll.setOnScrollChangeListener((v, x, y, ox, oy) -> developerAnimationScrollX = x);
        int saved = developerAnimationScrollX;
        scroll.post(() -> scroll.scrollTo(saved, 0));
        column.addView(scroll, new LinearLayout.LayoutParams(-1, -2));
    }

    private void addDeveloperShelf(
            LinearLayout column,
            String title,
            BoopDevMenuModel.Shelf shelf,
            boolean animationShelf) {
        addDeveloperSection(column, title);

        HorizontalScrollView shelfScroll = new HorizontalScrollView(this);
        shelfScroll.setHorizontalScrollBarEnabled(false);
        shelfScroll.setFillViewport(false);
        shelfScroll.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        shelfScroll.setContentDescription(title + " horizontal selector");

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, dp(18), 0);

        for (BoopDevMenuModel.Item item : shelf.items()) {
            final String label = item.label();
            final BoopDevMenuModel.Action action = item.action();
            Button button = new Button(this);
            button.setText(label);
            button.setTextSize(18f);
            button.setTextColor(Color.WHITE);
            button.setBackgroundColor(Color.rgb(42, 42, 42));
            button.setAllCaps(false);
            button.setContentDescription((animationShelf ? "Run " : "Preview ") + label);
            button.setOnClickListener(v -> {
                if (animationShelf) {
                    runDeveloperAction(action);
                } else {
                    showDeveloperNotificationPreview(action);
                }
            });
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    dp(152),
                    dp(64));
            itemParams.setMargins(0, 0, dp(12), 0);
            row.addView(button, itemParams);
        }

        shelfScroll.addView(row, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));

        int savedScrollX = animationShelf
                ? developerAnimationScrollX
                : developerNotificationScrollX;
        shelfScroll.setOnScrollChangeListener((view, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (animationShelf) {
                developerAnimationScrollX = scrollX;
            } else {
                developerNotificationScrollX = scrollX;
            }
        });
        shelfScroll.post(() -> shelfScroll.scrollTo(savedScrollX, 0));

        LinearLayout.LayoutParams shelfParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        shelfParams.setMargins(0, 0, 0, dp(2));
        column.addView(shelfScroll, shelfParams);
    }

    private void addDeveloperSection(LinearLayout column, String text) {
        TextView label = voiceSettingLabel(text, 18f, true);
        label.setGravity(Gravity.START);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(2), 0, dp(4));
        column.addView(label, params);
    }

    private void runDeveloperAction(BoopDevMenuModel.Action action) {
        switch (action) {
            case WAKE:
                stopDeveloperAnimation();
                if (developerMenuFace != null) {
                    developerMenuFace.showIdleBlackImmediately();
                    developerMenuFace.wakeFromIdle();
                }
                return;
            case THINK:
                stopDeveloperAnimation();
                if (developerMenuFace != null) developerMenuFace.startThinking();
                return;
            case STOP:
                stopDeveloperAnimation();
                return;
            case BERRY_1:
                stopDeveloperAnimation();
                if (developerMenuFace != null) developerMenuFace.playMemberBerry(0);
                return;
            case BERRY_2:
                stopDeveloperAnimation();
                if (developerMenuFace != null) developerMenuFace.playMemberBerry(1);
                return;
            case BERRY_3:
                stopDeveloperAnimation();
                if (developerMenuFace != null) developerMenuFace.playMemberBerry(2);
                return;
            case SHAKE:
                stopDeveloperAnimation();
                if (developerMenuFace != null) developerMenuFace.playShakeMuppet(0.85f);
                return;
            case SLEEP:
                stopDeveloperAnimation();
                if (developerMenuFace != null) developerMenuFace.goIdleBlack();
                return;
            case NOTIFICATION_FACEBOOK:
            case NOTIFICATION_WHATSAPP:
            case NOTIFICATION_GMAIL:
            case NOTIFICATION_X:
            case NOTIFICATION_YOUTUBE:
            case NOTIFICATION_MESSENGER:
            case NOTIFICATION_INSTAGRAM:
            case NOTIFICATION_DISCORD:
            case NOTIFICATION_SPOTIFY:
            case NOTIFICATION_REDDIT:
            case NOTIFICATION_LOCKED:
            case NOTIFICATION_BUNDLE:
                showDeveloperNotificationPreview(action);
                return;
            default:
                return;
        }
    }

    private void stopDeveloperAnimation() {
        if (developerMenuFace != null) {
            developerMenuFace.stopThinking();
            developerMenuFace.animate().cancel();
        }
    }

    private void showDeveloperNotificationPreview(BoopDevMenuModel.Action action) {
        if (developerMenuOverlay == null) {
            return;
        }
        stopDeveloperAnimation();
        developerMenuFace = null;
        developerMenuOverlay.removeAllViews();

        LinearLayout preview = new LinearLayout(this);
        preview.setOrientation(LinearLayout.VERTICAL);
        preview.setGravity(Gravity.CENTER_HORIZONTAL);
        preview.setBackgroundColor(Color.BLACK);
        preview.setContentDescription("BOOP notification dood preview");
        developerMenuOverlay.addView(preview, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        BoopNotificationPresentation presentation =
                BoopDevNotificationPreview.presentation(action, System.currentTimeMillis());
        BoopNotificationPuppetView puppet = new BoopNotificationPuppetView(
                this,
                presentation,
                new BoopNotificationPuppetView.Callback() {
                    @Override public void onOpen(String notificationKey) {
                        showDeveloperMenuContent();
                    }
                    @Override public void onOpenBundle() {
                        showDeveloperMenuContent();
                    }
                    @Override public void onDismiss() {
                        showDeveloperMenuContent();
                    }
                });
        LinearLayout.LayoutParams puppetParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f);
        preview.addView(puppet, puppetParams);

        Button dismiss = new Button(this);
        dismiss.setText("Dismiss");
        dismiss.setTextSize(20f);
        dismiss.setTextColor(Color.WHITE);
        dismiss.setBackgroundColor(Color.rgb(42, 42, 42));
        dismiss.setOnClickListener(v -> showDeveloperMenuContent());
        LinearLayout.LayoutParams dismissParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64));
        dismissParams.setMargins(dp(28), dp(8), dp(28), dp(18));
        preview.addView(dismiss, dismissParams);

    }

    private void hideDeveloperMenu() {
        stopDeveloperAnimation();
        developerMenuFace = null;
        developerMenuOpen = false;
        if (face != null) face.setOccluded("developer", false);
        if (interactionSurface != null && developerMenuOverlay != null) {
            interactionSurface.removeView(developerMenuOverlay);
        }
        developerMenuOverlay = null;
        developerAnimationScrollX = 0;
        developerNotificationScrollX = 0;
        wakeFaceForInteraction();
    }

    private TextView voiceSettingLabel(String text, float sizeSp, boolean bold) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(Color.WHITE);
        label.setTextSize(sizeSp);
        label.setGravity(Gravity.CENTER);
        if (bold) {
            label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return label;
    }

    private void hideVoiceSettings() {
        if (!voiceSettingsOpen) {
            return;
        }
        if (naturalVoiceDownloader != null && naturalVoiceDownloader.isRunning()) {
            naturalVoiceDownloader.cancel();
        }
        naturalPreviewGeneration++;
        if (naturalSpeechBackend != null) naturalSpeechBackend.stop();
        if (wakeCoordinator != null) wakeCoordinator.onTtsFinished();
        if (wakeEnrollmentActive) cancelWakeNameEnrollment(false);
        saveWakeNameFromSettings();
        String trainAfterClose = pendingWakeEnrollmentAfterSettings;
        pendingWakeEnrollmentAfterSettings = null;
        if (interactionSurface != null && voiceSettingsOverlay != null) {
            interactionSurface.removeView(voiceSettingsScroll);
        }
        voiceSettingsScroll = null;
        voiceSettingsOverlay = null;
        wakeNameInput = null;
        wakeNameTrainButton = null;
        wakeNameTrainingStatus = null;
        if (face != null) {
            face.setVisibility(View.VISIBLE);
            face.showIdleBlackImmediately();
        }
        voiceSettingsOpen = false;
        if (face != null) face.setOccluded("voice_settings", false);
        if (wakeCoordinator != null) {
            // Reload through the coordinator so its engineArmed bookkeeping cannot
            // claim the microphone is still armed after the controller was stopped.
            wakeCoordinator.reloadEngine();
            wakeCoordinator.setVoiceSettingsOpen(false);
        } else if (wakeWordController != null) {
            wakeWordController.reloadSensitivity();
        }
        wakeFaceForInteraction();
        if (trainAfterClose != null) promptWakeNameEnrollment(trainAfterClose);
    }

    private void saveWakeNameFromSettings() {
        if (wakeNameInput == null) return;
        applyWakeName(wakeNameInput.getText() == null ? null : wakeNameInput.getText().toString(), false);
    }

    private void applyWakeName(String requestedName, boolean confirmByVoice) {
        String before = BoopWakeNameStore.load(this);
        String saved = BoopWakeNameStore.save(this, requestedName);
        boolean matchingProfilePresent = BoopWakeEnrollmentStore.hasProfile(this, saved);
        boolean autoTrainAfterSettings = BoopWakeTrainingPolicy.shouldPromptAfterSettings(
                before, saved, matchingProfilePresent);
        boolean changed = !before.equals(saved);
        if (changed) {
            if (!matchingProfilePresent) BoopWakeEnrollmentStore.clear(this);
            if (wakeCoordinator != null) wakeCoordinator.reloadEngine();
            else if (wakeWordController != null) wakeWordController.reloadSensitivity();
        }
        if (wakeNameInput != null && !saved.contentEquals(wakeNameInput.getText())) {
            wakeNameInput.setText(saved);
            wakeNameInput.setSelection(saved.length());
        }
        updateWakeNameTrainingUi();
        if (confirmByVoice) {
            pendingWakeEnrollmentAfterSettings = null;
            if (BoopWakeName.isDefault(saved)) speak("BOOP. Got it.");
            else if (matchingProfilePresent) speak(saved + ". Got it.");
            else promptWakeNameEnrollment(saved);
            return;
        }
        if (BoopWakeName.isDefault(saved)) {
            pendingWakeEnrollmentAfterSettings = null;
        } else if (autoTrainAfterSettings) {
            pendingWakeEnrollmentAfterSettings = saved;
        }
    }

    private String wakeNameTrainingStatusText() {
        String selected = BoopWakeNameStore.load(this);
        if (BoopWakeName.isDefault(selected)) return "BOOP stays available as the permanent fallback.";
        return BoopWakeEnrollmentStore.hasProfile(this, selected)
                ? selected + " is trained locally. BOOP still works too."
                : selected + " is not trained yet. Say it five times.";
    }

    private void updateWakeNameTrainingUi() {
        if (wakeNameTrainingStatus != null) wakeNameTrainingStatus.setText(wakeNameTrainingStatusText());
        if (wakeNameTrainButton != null) {
            String selected = BoopWakeNameStore.load(this);
            wakeNameTrainButton.setEnabled(!BoopWakeName.isDefault(selected) && !wakeEnrollmentActive);
        }
    }

    private void promptWakeNameEnrollment(String requestedName) {
        String selected = BoopWakeName.normalize(requestedName);
        if (BoopWakeName.isDefault(selected) || wakeEnrollmentActive) return;
        pendingWakeEnrollmentAfterSettings = null;
        pendingWakeEnrollmentName = selected;
        speak("Say " + selected + " five times.");
        waitForWakeEnrollmentPrompt(selected, 0);
    }

    private void waitForWakeEnrollmentPrompt(String selected, int attempts) {
        if (!selected.equals(pendingWakeEnrollmentName) || wakeEnrollmentActive) return;
        if (tts != null && tts.isSpeaking() && attempts < 120) {
            interactionSurface.postDelayed(() -> waitForWakeEnrollmentPrompt(selected, attempts + 1), 100L);
            return;
        }
        pendingWakeEnrollmentName = null;
        beginWakeNameEnrollment(selected);
    }

    private void beginWakeNameEnrollment(String selected) {
        if (wakeWordController == null || wakeEnrollmentActive || BoopWakeName.isDefault(selected)) return;
        if (listening) {
            suppressNextRecognizerError = true;
            stopListening();
        }
        if (tts != null) tts.stop();
        wakeEnrollmentPauseOwned = !voiceSettingsOpen;
        if (wakeCoordinator != null) wakeCoordinator.setVoiceSettingsOpen(true);
        wakeEnrollmentActive = true;
        if (wakeNameTrainingStatus != null) wakeNameTrainingStatus.setText(selected + " · 0 / 5");
        if (wakeNameTrainButton != null) wakeNameTrainButton.setEnabled(false);

        boolean started = wakeWordController.startEnrollment(selected, new BoopWakeWordController.EnrollmentListener() {
            @Override public void onEnrollmentProgress(String name, int accepted, int required) {
                if (wakeNameTrainingStatus != null) wakeNameTrainingStatus.setText(name + " · " + accepted + " / " + required);
            }
            @Override public void onEnrollmentRetry(String name, int accepted, int required) {
                if (wakeNameTrainingStatus != null) wakeNameTrainingStatus.setText("Again please · " + accepted + " / " + required);
            }
            @Override public void onEnrollmentComplete(String name) {
                wakeEnrollmentActive = false;
                finishWakeNameEnrollmentPause();
                updateWakeNameTrainingUi();
                speak(name + ". Got it.");
            }
            @Override public void onEnrollmentFailure(String message) {
                wakeEnrollmentActive = false;
                finishWakeNameEnrollmentPause();
                updateWakeNameTrainingUi();
                speak("Training stopped. BOOP still works.");
            }
        });
        if (!started) {
            wakeEnrollmentActive = false;
            finishWakeNameEnrollmentPause();
            updateWakeNameTrainingUi();
        }
    }

    private void finishWakeNameEnrollmentPause() {
        if (wakeCoordinator != null) {
            wakeCoordinator.reloadEngine();
            if (wakeEnrollmentPauseOwned) wakeCoordinator.setVoiceSettingsOpen(false);
        } else if (wakeWordController != null) {
            wakeWordController.reloadSensitivity();
        }
        wakeEnrollmentPauseOwned = false;
    }

    private void cancelWakeNameEnrollment(boolean restoreWake) {
        pendingWakeEnrollmentName = null;
        if (wakeWordController != null) wakeWordController.suspendAll();
        wakeEnrollmentActive = false;
        if (restoreWake) finishWakeNameEnrollmentPause();
        wakeEnrollmentPauseOwned = false;
        updateWakeNameTrainingUi();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void acceptAssistantIntent(Intent intent) {
        if (intent == null || !intent.getBooleanExtra(
                BoopVoiceInteractionSession.EXTRA_ONE_SHOT_ASSIST, false)) {
            return;
        }
        intent.removeExtra(BoopVoiceInteractionSession.EXTRA_ONE_SHOT_ASSIST);
        assistantOneShot = true;
        assistantOneShotStarted = false;
        int inputDeviceId = intent.getIntExtra(Intent.EXTRA_ASSIST_INPUT_DEVICE_ID, -1);
        android.util.Log.i("BOOP-Assist", "Assistant one-shot requested; inputDeviceId=" + inputDeviceId);
    }

    private void maybeStartAssistantOneShot() {
        if (!assistantOneShot || assistantOneShotStarted || voiceSettingsOpen || listening || thinking) {
            return;
        }
        assistantOneShotStarted = true;
        beginTapToSpeak();
    }

    private void finishAssistantOneShot() {
        if (!assistantOneShot) return;
        assistantOneShot = false;
        assistantOneShotStarted = false;
        assistantFollowUpAfterTts = false;
        assistantFollowUpListening = false;
        finish();
        overridePendingTransition(0, 0);
    }

    private void beginTapToSpeak() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            pendingListenAfterPermission = true;
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQ_RECORD_AUDIO);
            return;
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.onTapStarted();
        }
        recognitionMode = RecognitionMode.TAP;
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
        suppressNextRecognizerError = false;
        wakeFaceForInteraction();
        if (recognizer == null) {
            speak("I can't hear speech on this device yet.");
            if (recognitionMode == RecognitionMode.TAP && wakeCoordinator != null) {
                wakeCoordinator.onTapFinished();
            }
            recognitionMode = RecognitionMode.NONE;
            return;
        }

        if (naturalSpeechBackend != null) {
            naturalSpeechBackend.stop();
        }
        if (androidSpeechBackend != null) {
            androidSpeechBackend.stop();
        } else if (tts != null) {
            tts.stop();
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, assistantFollowUpListening);
        intent.putExtra(RecognizerIntent.EXTRA_MASK_OFFENSIVE_WORDS, false);
        // BOOP_UNCENSORED_SPEECH_V1

        listening = true;
        com.boop.shared.BoopState.INSTANCE.speech(true,false);
        face.startListeningCue();
        recognizer.startListening(intent);
    }

    private void startWakeRecognition(BoopWakeAudioSession session) {
        if (recognizer == null) {
            session.close();
            if (wakeCoordinator != null) {
                wakeCoordinator.failWakeSession();
            }
            return;
        }
        final BoopWakeDiagnosticTrace diagnosticTrace =
                new BoopWakeDiagnosticTrace(SystemClock.elapsedRealtime());
        wakeDiagnosticTrace = diagnosticTrace;
        wakeAudioSession = session;
        wakeTranscriptAccumulator.reset();
        recognitionMode = RecognitionMode.WAKE;
        listening = true;
        com.boop.shared.BoopState.INSTANCE.speech(true,false);
        face.startListeningCue();
        try {
            recognizer.startListening(
                    BoopWakeRecognitionIntent.build(Locale.getDefault(), session.audioSource()));
        } catch (RuntimeException e) {
            diagnosticTrace.startFailure(
                    e.getClass().getSimpleName(), SystemClock.elapsedRealtime());
            rememberWakeDiagnostic(diagnosticTrace);
            closeWakeAudioSession();
            listening = false;
        com.boop.shared.BoopState.INSTANCE.speech(false,false);
            recognitionMode = RecognitionMode.NONE;
            face.stopListeningCue();
            if (wakeCoordinator != null) {
                wakeCoordinator.recoverWakeSession();
                updateWakeRecoverySnapshot();
            }
        }
    }

    private void rememberWakeDiagnostic(BoopWakeDiagnosticTrace trace) {
        if (trace == null) return;
        lastWakeDiagnosticSummary = trace.summary(SystemClock.elapsedRealtime());
        if (trace.terminal() && wakeDiagnosticTrace == trace) {
            wakeDiagnosticTrace = null;
        }
    }

    private void updateWakeRecoverySnapshot() {
        lastWakeDiagnosticRecovery = wakeCoordinator == null
                ? "no coordinator"
                : wakeCoordinator.state().name();
    }

    private void showRequestedWakeDiagnostics() {
        if (wakeCoordinator != null
                && wakeCoordinator.state() == BoopWakeSessionState.State.PROCESSING) {
            wakeCoordinator.finishWakeProcessing();
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.setVoiceSettingsOpen(true);
        }
        boolean powered = dockModeController != null && dockModeController.isWakePowered();
        boolean wireless = dockModeController != null && dockModeController.isWirelessDocked();
        boolean micGranted = checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
        String wakeState = wakeCoordinator == null ? "none" : wakeCoordinator.state().name();
        String message = lastWakeDiagnosticSummary
                + "\n\nwakeName=" + BoopWakeNameStore.load(this)
                + "\nexternalPower=" + powered
                + "\nwirelessDock=" + wireless
                + "\nmicPermission=" + micGranted
                + "\nwakeState=" + wakeState
                + "\nrecognitionMode=" + recognitionMode.name()
                + "\nlistening=" + listening
                + "\nlastRecovery=" + lastWakeDiagnosticRecovery;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("BOOP diagnostics")
                .setMessage(message)
                .setPositiveButton("Close", (shown, which) -> shown.dismiss())
                .create();
        dialog.setOnDismissListener(shown -> {
            if (wakeCoordinator != null) {
                wakeCoordinator.setVoiceSettingsOpen(voiceSettingsOpen || chatModeOpen);
            }
            wakeFaceForInteraction();
        });
        dialog.show();
    }

    private void stopListening() {
        RecognitionMode stoppedMode = recognitionMode;
        cancelAssistantFollowUpSilenceTimeout();
        assistantFollowUpListening = false;
        latestAssistantFollowUpPartial = null;
        recognitionMode = RecognitionMode.NONE;
        listening = false;
        com.boop.shared.BoopState.INSTANCE.speech(false,false);
        face.stopListeningCue();
        if (recognizer != null) {
            recognizer.cancel();
        }
        if (stoppedMode == RecognitionMode.WAKE) {
            closeWakeAudioSession();
            if (wakeCoordinator != null) {
                wakeCoordinator.cancelWakeCapture();
            }
        } else if (stoppedMode == RecognitionMode.TAP && wakeCoordinator != null) {
            wakeCoordinator.onTapFinished();
        }
        scheduleFaceIdle();
    }

    private void closeWakeAudioSession() {
        BoopWakeAudioSession session = wakeAudioSession;
        wakeAudioSession = null;
        if (session != null) {
            session.close();
        }
    }

    private void checkWakeRecognitionSupport() {
        // BOOP_WAKE_REAL_ATTEMPT_V1
        if (wakeCoordinator == null) {
            return;
        }
        // checkRecognitionSupport() is advisory and has returned false negatives on
        // otherwise working Android recognizers. Sherpa must still arm; the existing
        // wake recognition start/error path remains the real capability check.
        wakeSupportCheckInFlight = false;
        wakeCoordinator.setRecognitionSupported(
                BoopWakeRecognitionCapability.canAttempt(
                        Build.VERSION.SDK_INT,
                        recognizer != null));
    }

    private void handleRecognizedSpeech(String transcript) {
        if (handleRecipeSpeech(transcript)) return;
        if (haClient != null) haClient.prepareMusicTurn(transcript);
        if ("boop settings".equalsIgnoreCase(transcript.trim()) || "device settings".equalsIgnoreCase(transcript.trim())) {
            startActivity(new Intent(this,BoopProfileActivity.class)); return;
        }
        if (BoopLocalMedia.handle(this,transcript,this::speak)) return;
        if (BoopWakeDiagnosticsIntent.matches(transcript)) {
            showRequestedWakeDiagnostics();
            return;
        }
        BoopWakeNameIntent.Result wakeNameChange = BoopWakeNameIntent.parse(transcript);
        if (wakeNameChange.action() == BoopWakeNameIntent.Action.SET
                || wakeNameChange.action() == BoopWakeNameIntent.Action.RESET) {
            applyWakeName(wakeNameChange.name(), true);
            return;
        }

        BoopMirrorIntent.Action mirrorAction = BoopMirrorIntent.actionFor(transcript);
        if (mirrorAction == BoopMirrorIntent.Action.OPEN) {
            openMirror();
            return;
        }
        if (mirrorAction == BoopMirrorIntent.Action.CLOSE) {
            closeMirror();
            return;
        }
        if (BoopEyeHueVoiceIntent.matches(transcript)) {
            showEyeHueControl();
            return;
        }
        if (BoopVoiceSettingsIntent.matches(transcript)) {
            showVoiceSettings();
            return;
        }

        // BOOP_DEVELOPER_MENU_SPOKEN_ENTRY_V4
        if (BoopDevMenuIntent.matches(transcript)) {
            showDeveloperMenu();
            return;
        }

        String voiceReply = voiceController.maybeChangeVoice(transcript);
        if (voiceReply != null) {
            speak(voiceReply);
            return;
        }

        String exitReply = BoopConversationExitIntent.replyFor(transcript);
        if (exitReply != null) {
            speakThenSleep(exitReply);
            return;
        }

        if (toastEgg.matches(transcript)) {
            BoopToastEgg.Moment toastMoment = toastEgg.next();
            wakeFaceForInteraction();
            if (face != null) {
                face.playMemberBerry(toastMoment.level());
            }
            speak(toastMoment.line());
            return;
        }

        if (!tokenStore.hasConnection()) {
            speak("I need to connect to the house first.");
            ensureHouseConnection();
            return;
        }

        final BoopChatMode requestChatMode = chatModeStore.load();
        final int requestChatRevision = chatModeRevision;
        executor.execute(() -> {
            HomeAssistantDeviceSetup.SetupResult setup = deviceSetup.ensureReady();
            if (setup != HomeAssistantDeviceSetup.SetupResult.READY) {
                runOnUiThread(() -> handleDeviceSetupFailure(setup));
                return;
            }

            setupFailureSpoken = false;
            CommandOutcome outcome;
            if (requestChatMode == BoopChatMode.NATIVE_CHAT) {
                outcome = commandRouter.processWithAssistant(transcript, nativeChatClient::ask);
            } else {
                outcome = commandRouter.process(transcript, () ->
                        requestChatRevision == chatModeRevision
                                && chatModeStore.load() == BoopChatMode.OPENCODE);
            }
            runOnUiThread(() -> {
                if (!activityInForeground || chatModeOpen || isFinishing() || isDestroyed()
                        || requestChatRevision != chatModeRevision) return;
                if (requestChatMode == BoopChatMode.FREE_CHAT
                        && outcome.status() == CommandOutcome.Status.NO_MATCH) {
                    openFreeChat(transcript);
                    return;
                }
                // Accepted music is its own confirmation; do not wait for neural TTS.
                if (assistantOneShot && !voiceSettingsOpen && outcome.hasAcceptedMusicPlayback()) {
                    android.util.Log.i("BOOP-Assist", "Music accepted; closing assistant");
                    finishAssistantOneShot();
                    return;
                }
                if (outcome.status() == CommandOutcome.Status.ASSISTANT_REPLY || outcome.status() == CommandOutcome.Status.LOCAL_QUESTION) {
                    speakThenOpenAssistantFollowUp(LocalReply.forOutcome(outcome));
                } else {
                    speak(LocalReply.forOutcome(outcome));
                }
                if (outcome.status() == CommandOutcome.Status.AUTH_REQUIRED) {
                    tokenStore.clear();
                    setupFailureSpoken = false;
                    ensureHouseConnection();
                }
            });
        });
    }

    private boolean handleRecipeSpeech(String transcript) {
        if (getResources().getConfiguration().smallestScreenWidthDp < 600
                || BoopDeviceProfile.resolve(this) != BoopDeviceProfile.Mode.WALL) return false;
        String reply = recipeSession.handle(transcript);
        if (reply == null) {
            if (!recipeSession.active() && recipePanel != null) recipePanel.close();
            return false;
        }
        if (recipePanel == null) recipePanel = new BoopRecipePanel(this, interactionSurface, this::handleRecognizedSpeech);
        recipePanel.render(recipeSession);
        String prompt = recipeSession.prompt();
        if (prompt == null) { speak(reply); return true; }
        final long ticket = recipeSession.ticket();
        if (recipeRequestTicket == ticket) { speak(reply); return true; }
        recipeRequestTicket = ticket;
        final BoopChatMode mode = chatModeStore.load();
        final int revision = chatModeRevision;
        if (mode != BoopChatMode.OPENCODE && mode != BoopChatMode.NATIVE_CHAT) {
            recipeSession.failed("Choose your existing BOOP conversation service in settings to generate recipes. Loaded cooking steps work offline.");
            recipePanel.render(recipeSession); speak(recipeSession.reply()); return true;
        }
        speak(reply);
        executor.execute(() -> {
            CommandOutcome outcome;
            try {
                outcome = mode == BoopChatMode.NATIVE_CHAT ? nativeChatClient.ask(prompt) : generalAssistant.ask(prompt);
            } catch (RuntimeException error) { outcome = CommandOutcome.assistantFailed(); }
            final CommandOutcome result = outcome;
            runOnUiThread(() -> {
                if (!activityInForeground || isFinishing() || isDestroyed()
                        || ticket != recipeSession.ticket()) return;
                if (revision != chatModeRevision) {
                    recipeSession.cancelPending(); recipePanel.render(recipeSession); return;
                }
                if (result.status() != CommandOutcome.Status.ASSISTANT_REPLY) {
                    recipeSession.failed(LocalReply.forOutcome(result));
                } else if (!recipeSession.accept(ticket, result.assistantSpeech())) {
                    recipeSession.failed("I couldn't turn that reply into clear cooking steps. Say that's everything to try again, or close the recipe.");
                }
                recipePanel.render(recipeSession);
                speak(recipeSession.collecting() ? recipeSession.reply()
                        : recipeSession.title() + ". Check the ingredients. Say next step when you're ready.");
            });
        });
        return true;
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
        if (BoopDeviceProfile.resolve(this) == BoopDeviceProfile.Mode.SHIELD) {
            startActivity(new Intent(this, com.boop.shieldoverlay.BoopHomeActivity.class));
            finish(); // The next remote request must reload any repaired pairing.
            return;
        }
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
        // NSD resolution may already be queued when the one-shot activity finishes.
        // Stopping discovery does not retract that callback from the main queue.
        if (isFinishing() || isDestroyed() || connectPromptShowing || tokenStore.hasConnection()) {
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
        if (intent.getBooleanExtra("boop_open_voice_settings", false)) {
            interactionSurface.post(this::showVoiceSettings);
        }
        acceptAssistantIntent(intent);
        maybeStartAssistantOneShot();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        cancelFaceHolds();
        faceTouchActive = false;
        keepAwakeAndHideSystemUi();
        if (face != null) {
            if (presenceState != null && presenceState.isIdleBlack()) {
                face.showIdleBlackImmediately();
            } else {
                face.invalidate();
            }
        }
    }

    private void speakThenSleep(String text) {
        assistantFollowUpAfterTts = false;
        sleepFaceAfterTts = true;
        speak(text);
    }

    private void speakThenOpenAssistantFollowUp(String text) {
        assistantFollowUpAfterTts = true;
        speak(text);
    }

    private void speak(String text) {
        if (!activityInForeground || isFinishing() || isDestroyed()) return;
        com.boop.shared.BoopState.INSTANCE.speech(false,true);
        wakeFaceForInteraction();
        if (wakeCoordinator != null) {
            wakeCoordinator.onTtsStarting();
        }
        if (voiceController != null
                && voiceController.naturalBackendSelectedAndUsable()
                && naturalSpeechBackend != null) {
            BoopVoiceController.NaturalVoice voice = voiceController.selectedNaturalVoice();
            boolean started = naturalSpeechBackend.speak(
                    text,
                    voice.sid(),
                    voiceController.pitch(),
                    voiceController.speechRate(),
                    new BoopSpeechBackend.Callback() {
                        @Override public void onCancelled() { }
                        @Override
                        public void onDone() {
                            runOnUiThread(() -> finishTtsUtterance());
                        }

                        @Override
                        public void onError(Throwable error) {
                            android.util.Log.w(
                                    "BOOP-NaturalVoice",
                                    "Natural voice failed; using Android TTS for the same utterance",
                                    error);
                            runOnUiThread(() -> speakWithAndroidTts(text));
                        }
                    });
            if (started) return;
        }
        speakWithAndroidTts(text);
    }

    private void speakWithAndroidTts(String text) {
        if (!activityInForeground || isFinishing() || isDestroyed()) return;
        if (androidSpeechBackend != null && voiceController != null) {
            boolean started = androidSpeechBackend.speak(
                    text,
                    -1,
                    voiceController.pitch(),
                    voiceController.speechRate(),
                    new BoopSpeechBackend.Callback() {
                        @Override
                        public void onDone() {
                            runOnUiThread(() -> finishTtsUtterance());
                        }

                        @Override
                        public void onError(Throwable error) {
                            runOnUiThread(() -> finishTtsUtterance());
                        }
                    });
            if (started) return;
        }
        finishTtsUtterance();
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
        if (!hasFocus) {
            cancelFaceHolds();
            faceTouchActive = false;
        }
        if (hasFocus) {
            keepAwakeAndHideSystemUi();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_RECORD_AUDIO) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (wakeCoordinator != null) {
                wakeCoordinator.setMicrophonePermission(granted);
                if (granted) {
                    checkWakeRecognitionSupport();
                }
            }
            if (granted && pendingListenAfterPermission) {
                pendingListenAfterPermission = false;
                if (wakeCoordinator != null) {
                    wakeCoordinator.onTapStarted();
                }
                recognitionMode = RecognitionMode.TAP;
                startListening();
            } else if (!granted) {
                pendingListenAfterPermission = false;
                speak("I need microphone permission to hear you.");
            }
            return;
        }

        if (requestCode == REQ_CAMERA) {
            boolean granted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean openAfterGrant = pendingMirrorAfterPermission;
            pendingMirrorAfterPermission = false;
            if (granted && openAfterGrant) {
                openMirror();
            } else if (!granted && openAfterGrant) {
                speak("I need camera permission to be a mirror.");
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
        if (isFinishing() || isDestroyed()) return;
        ttsReady = false;
        if (status == TextToSpeech.SUCCESS && tts != null) {
            int result = tts.setLanguage(Locale.getDefault());
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
            if (ttsReady && voiceController != null) {
                voiceController.initialize(tts, Locale.getDefault());
            }
        }
        if (androidSpeechBackend != null) androidSpeechBackend.onInitializationFinished(ttsReady);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        if (recognitionMode == RecognitionMode.WAKE && wakeDiagnosticTrace != null) {
            wakeDiagnosticTrace.ready(SystemClock.elapsedRealtime());
        }
    }
    @Override
    public void onBeginningOfSpeech() {
        if (recognitionMode == RecognitionMode.WAKE && wakeDiagnosticTrace != null) {
            wakeDiagnosticTrace.begin(SystemClock.elapsedRealtime());
        }
        if (assistantFollowUpListening && recognitionMode == RecognitionMode.TAP) {
            cancelAssistantFollowUpSilenceTimeout();
        }
    }
    @Override public void onRmsChanged(float rmsdB) { }
    @Override public void onBufferReceived(byte[] buffer) { }

    @Override
    public void onEndOfSpeech() {
        if (recognitionMode == RecognitionMode.WAKE && wakeDiagnosticTrace != null) {
            wakeDiagnosticTrace.end(SystemClock.elapsedRealtime());
        }
        if (recognitionMode == RecognitionMode.WAKE && wakeAudioSession != null) {
            wakeAudioSession.finishCapture();
        }
    }

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
        RecognitionMode failedMode = recognitionMode;
        BoopWakeDiagnosticTrace failedWakeTrace = wakeDiagnosticTrace;
        boolean failedAssistantFollowUp =
                failedMode == RecognitionMode.TAP && assistantFollowUpListening;
        String fallback = latestAssistantFollowUpPartial;
        cancelAssistantFollowUpSilenceTimeout();
        assistantFollowUpListening = false;
        latestAssistantFollowUpPartial = null;
        recognitionMode = RecognitionMode.NONE;
        listening = false;
        com.boop.shared.BoopState.INSTANCE.speech(false,false);

        if (suppressNextRecognizerError) {
            suppressNextRecognizerError = false;
            sleepFaceImmediately();
            return;
        }

        face.stopListeningCue();

        if (failedMode == RecognitionMode.WAKE) {
            if (failedWakeTrace != null) {
                failedWakeTrace.error(error, SystemClock.elapsedRealtime());
                rememberWakeDiagnostic(failedWakeTrace);
            }
            wakeTranscriptAccumulator.reset();
            closeWakeAudioSession();
            if (wakeCoordinator != null) {
                wakeCoordinator.recoverWakeSession();
                updateWakeRecoverySnapshot();
            }
            scheduleFaceIdle();
            return;
        }

        if (failedAssistantFollowUp
                && error == SpeechRecognizer.ERROR_NO_MATCH
                && fallback != null
                && !fallback.isBlank()) {
            if (wakeCoordinator != null) {
                wakeCoordinator.onTapFinished();
            }
            handleRecognizedSpeech(fallback);
            return;
        }

        if (failedAssistantFollowUp
                && (error == SpeechRecognizer.ERROR_NO_MATCH
                || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
            if (wakeCoordinator != null) {
                wakeCoordinator.onTapFinished();
            }
            sleepFaceImmediately();
            return;
        }

        if (failedMode == RecognitionMode.TAP) {
            speak("Speech error " + error + ", " + speechErrorName(error) + ".");
            if (wakeCoordinator != null) {
                wakeCoordinator.onTapFinished();
            }
            return;
        }

        speak("Speech error " + error + ", " + speechErrorName(error) + ".");
    }

    @Override
    public void onResults(Bundle results) {
        if (chatModeOpen || !activityInForeground) {
            suppressNextRecognizerError = true;
            stopListening();
            return;
        }
        RecognitionMode completedMode = recognitionMode;
        BoopWakeDiagnosticTrace completedWakeTrace = wakeDiagnosticTrace;
        boolean completedAssistantFollowUp =
                completedMode == RecognitionMode.TAP && assistantFollowUpListening;
        cancelAssistantFollowUpSilenceTimeout();
        assistantFollowUpListening = false;
        latestAssistantFollowUpPartial = null;
        recognitionMode = RecognitionMode.NONE;
        listening = false;
        com.boop.shared.BoopState.INSTANCE.speech(false,false);
        face.stopListeningCue();

        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String best = null;
        if (matches != null && !matches.isEmpty()) {
            String candidate = matches.get(0).trim();
            if (!candidate.isEmpty()) {
                best = candidate;
            }
        }

        if (completedMode == RecognitionMode.WAKE) {
            best = wakeTranscriptAccumulator.chooseFinal(best);
            wakeTranscriptAccumulator.reset();
            if (completedWakeTrace != null) {
                completedWakeTrace.result(best, SystemClock.elapsedRealtime());
                rememberWakeDiagnostic(completedWakeTrace);
                lastWakeDiagnosticRecovery = "reply pending";
            }
            closeWakeAudioSession();
            if (wakeCoordinator != null) {
                wakeCoordinator.markWakeProcessing();
            }
            String normalized = BoopWakeTranscriptNormalizer.stripLeadingWakeWord(best, BoopWakeNameStore.load(this));
            if (normalized.isEmpty()) {
                if (wakeCoordinator != null) {
                    wakeCoordinator.finishWakeProcessing();
                }
                scheduleFaceIdle();
                return;
            }
            handleRecognizedSpeech(normalized);
            return;
        }

        if (completedMode == RecognitionMode.TAP) {
            if (best != null) {
                if (wakeCoordinator != null) {
                    wakeCoordinator.onTapFinished();
                }
                handleRecognizedSpeech(best);
                return;
            }
            if (wakeCoordinator != null) {
                wakeCoordinator.onTapFinished();
            }
            if (completedAssistantFollowUp) {
                sleepFaceImmediately();
                return;
            }
            speak("I didn't catch that.");
            return;
        }

        if (best != null) {
            handleRecognizedSpeech(best);
            return;
        }
        speak("I didn't catch that.");
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        if (recognitionMode == RecognitionMode.WAKE
                && wakeDiagnosticTrace != null
                && partialResults != null) {
            ArrayList<String> diagnosticMatches = partialResults.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
            if (diagnosticMatches != null && !diagnosticMatches.isEmpty()) {
                String diagnosticCandidate = diagnosticMatches.get(0);
                if (diagnosticCandidate != null && !diagnosticCandidate.isBlank()) {
                    wakeDiagnosticTrace.partial(
                            diagnosticCandidate, SystemClock.elapsedRealtime());
                }
            }
        }
        if (recognitionMode == RecognitionMode.WAKE && partialResults != null) {
            ArrayList<String> wakeMatches = partialResults.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
            if (wakeMatches != null && !wakeMatches.isEmpty()) {
                wakeTranscriptAccumulator.rememberPartial(wakeMatches.get(0));
            }
            return;
        }
        if (!assistantFollowUpListening || recognitionMode != RecognitionMode.TAP) {
            return;
        }
        ArrayList<String> matches = partialResults.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches == null || matches.isEmpty()) {
            return;
        }
        String candidate = matches.get(0);
        if (candidate == null) {
            return;
        }
        candidate = candidate.trim();
        if (!candidate.isEmpty()) {
            latestAssistantFollowUpPartial = candidate;
        }
    }
    @Override public void onEvent(int eventType, Bundle params) { }

    @Override
    protected void onDestroy() {
        recipeSession.close();
        if (recipePanel != null) recipePanel.close();
        activityInForeground = false;
        cancelFaceHolds();
        if (chatModeDialog != null) chatModeDialog.dismiss();
        thinking = false;
        voiceSettingsOpen = false;
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        shakeDetector.reset();
        wakeDiagnosticTrace = null;
        closeWakeAudioSession();
        if (dockModeController != null) {
            dockModeController.onPause();
            dockModeController = null;
        }
        if (presencePeekController != null) {
            presencePeekController.shutdown();
            presencePeekController = null;
        }
        if (mirrorController != null) {
            mirrorController.shutdown();
            mirrorController = null;
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.shutdown();
            wakeCoordinator = null;
        }
        wakeWordController = null;
        if (interactionSurface != null && voiceSettingsOverlay != null) {
            interactionSurface.removeView(voiceSettingsOverlay);
        }
        voiceSettingsOverlay = null;
        if (face != null) {
            face.stopListeningCue();
            face.stopThinking();
        }
        if (presenceHandler != null) {
            presenceHandler.removeCallbacks(faceIdleRunnable);
            presenceHandler.removeCallbacks(assistantFollowUpSilenceRunnable);
            presenceHandler.removeCallbacks(memberBerryRunnable);
            presenceHandler = null;
        }
        if (discovery != null) {
            discovery.stop();
        }
        if (recognizer != null) {
            recognizer.destroy();
            recognizer = null;
        }
        if (naturalVoiceDownloader != null) {
            naturalVoiceDownloader.cancel();
            naturalVoiceDownloader = null;
        }
        if (naturalSpeechBackend != null) {
            naturalSpeechBackend.release();
            naturalSpeechBackend = null;
        }
        if (androidSpeechBackend != null) {
            androidSpeechBackend.release();
            androidSpeechBackend = null;
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        naturalVoicePack = null;
        naturalVoiceManifest = null;
        voiceController = null;
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        interactionSurface = null;
        super.onDestroy();
    }
}

// BOOP_SINGLE_FACE_OWNERSHIP_V148

// BOOP_V200_VOICE_UI
