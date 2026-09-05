package com.boop.alpha1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity implements RecognitionListener, TextToSpeech.OnInitListener {
    private static final int REQ_RECORD_AUDIO = 1001;
    private static final int REQ_NEARBY_WIFI = 1002;
    private static final String HOME_AREA = "Living Room";
    private static final long IDLE_TIMEOUT_MS = 30_000L;
    private static final long ASSISTANT_FOLLOW_UP_SILENCE_MS = 5_000L;

    private enum RecognitionMode { NONE, TAP, WAKE }

    private FrameLayout interactionSurface;
    private BoopFaceView face;
    private BoopPresenceState presenceState;
    private Handler presenceHandler;
    private SpeechRecognizer recognizer;
    private RecognitionMode recognitionMode = RecognitionMode.NONE;
    private BoopWakeWordController wakeWordController;
    private BoopWakeSessionCoordinator wakeCoordinator;
    private BoopWakeAudioSession wakeAudioSession;
    private boolean wakeSupportCheckInFlight;
    private TextToSpeech tts;
    private BoopVoiceController voiceController;
    private LinearLayout voiceSettingsOverlay;
    private boolean ttsReady = false;
    private boolean listening = false;
    private boolean thinking = false;
    private boolean voiceSettingsOpen = false;
    private boolean pendingListenAfterPermission = false;
    private boolean assistantFollowUpAfterTts = false;
    private boolean assistantFollowUpListening = false;
    private boolean suppressNextRecognizerError = false;
    private String latestAssistantFollowUpPartial = null;
    private boolean pendingDiscoveryAfterPermission = false;
    private boolean connectPromptShowing = false;
    private boolean setupFailureSpoken = false;
    private boolean memberBerryConsumed = false;
    private int memberBerryVariant = 0;

    private ExecutorService executor;
    private SecureTokenStore tokenStore;
    private HomeAssistantAuth haAuth;
    private HomeAssistantClient haClient;
    private HomeAssistantGeneralAssistantClient generalAssistant;
    private BoopCommandRouter commandRouter;
    private HomeAssistantDeviceSetup deviceSetup;
    private HomeAssistantDiscovery discovery;

    private final Runnable faceIdleRunnable = () -> {
        if (presenceState == null || face == null) {
            return;
        }
        if (listening || thinking || voiceSettingsOpen) {
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
        if (interactionSurface == null || face == null || listening || voiceSettingsOpen) {
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

        presenceHandler = new Handler(Looper.getMainLooper());
        presenceState = new BoopPresenceState();

        interactionSurface = new FrameLayout(this);
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

        voiceController = new BoopVoiceController(this);
        tts = new TextToSpeech(this, this);
        installTtsListener();
        createRecognizer();
        createWakeObjects();

        executor = Executors.newSingleThreadExecutor();
        tokenStore = new SecureTokenStore(this);
        haAuth = new HomeAssistantAuth(this, tokenStore);
        haClient = new HomeAssistantClient(tokenStore, haAuth, HOME_AREA);
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
        deviceSetup = new HomeAssistantDeviceSetup(tokenStore, haAuth);
        discovery = new HomeAssistantDiscovery(this);
        handleAuthIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wakeCoordinator != null) {
            wakeCoordinator.beginForegroundSession();
            boolean granted = checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
            wakeCoordinator.setMicrophonePermission(granted);
            if (granted) {
                checkWakeRecognitionSupport();
            }
        }
    }

    @Override
    protected void onPause() {
        assistantFollowUpAfterTts = false;
        assistantFollowUpListening = false;
        latestAssistantFollowUpPartial = null;
        cancelAssistantFollowUpSilenceTimeout();
        closeWakeAudioSession();
        if (wakeCoordinator != null) {
            wakeCoordinator.endForegroundSession();
        }
        super.onPause();
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
        boolean openAssistantFollowUp = assistantFollowUpAfterTts;
        assistantFollowUpAfterTts = false;

        // Preserve the proven hand-off: reserve the tap recognizer before releasing TTS,
        // so the wake-word engine cannot race the follow-up microphone.
        if (openAssistantFollowUp && wakeCoordinator != null) {
            wakeCoordinator.onTapStarted();
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.onTtsFinished();
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
        if (face != null) {
            face.animate().alpha(1.0f).setDuration(120).start();
        }
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
                    startWakeRecognition(session);
                });
            }

            @Override
            public void onWakeFailure(String message) {
                runOnUiThread(() -> {
                    if (wakeCoordinator != null) {
                        wakeCoordinator.failWakeSession();
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

            @Override public void shutdown() {
                if (wakeWordController != null) {
                    wakeWordController.shutdown();
                }
            }
        });
    }

    private boolean onFaceTouch(View view, MotionEvent event) {
        if (voiceSettingsOpen) {
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            memberBerryConsumed = false;
            wakeFaceForInteraction();
            scheduleMemberBerryHold();
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            cancelMemberBerryHold();
            memberBerryConsumed = false;
            return true;
        }
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return true;
        }

        cancelMemberBerryHold();
        if (memberBerryConsumed) {
            memberBerryConsumed = false;
            return true;
        }

        if (listening) {
            stopListening();
        } else {
            beginTapToSpeak();
        }
        return true;
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
        if (interactionSurface == null || voiceController == null || voiceSettingsOpen) {
            return;
        }

        wakeFaceForInteraction();
        voiceSettingsOpen = true;
        if (wakeCoordinator != null) {
            wakeCoordinator.setVoiceSettingsOpen(true);
        }

        voiceSettingsOverlay = new LinearLayout(this);
        voiceSettingsOverlay.setOrientation(LinearLayout.VERTICAL);
        voiceSettingsOverlay.setGravity(Gravity.CENTER);
        voiceSettingsOverlay.setPadding(dp(42), dp(40), dp(42), dp(40));
        voiceSettingsOverlay.setBackgroundColor(Color.argb(236, 0, 0, 0));
        voiceSettingsOverlay.setClickable(true);
        voiceSettingsOverlay.setContentDescription("BOOP voice settings");

        TextView title = voiceSettingLabel("Voice", 30f, true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, dp(28));
        voiceSettingsOverlay.addView(title, titleParams);

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
        voiceSettingsOverlay.addView(done, doneParams);

        interactionSurface.addView(voiceSettingsOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        voiceSettingsOverlay.bringToFront();
        scheduleFaceIdle();
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
        if (interactionSurface != null && voiceSettingsOverlay != null) {
            interactionSurface.removeView(voiceSettingsOverlay);
        }
        voiceSettingsOverlay = null;
        voiceSettingsOpen = false;
        if (wakeCoordinator != null) {
            wakeCoordinator.setVoiceSettingsOpen(false);
        }
        wakeFaceForInteraction();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
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

        if (tts != null) {
            tts.stop();
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, assistantFollowUpListening);

        listening = true;
        face.animate().alpha(0.78f).setDuration(120).start();
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
        wakeAudioSession = session;
        recognitionMode = RecognitionMode.WAKE;
        listening = true;
        face.animate().alpha(0.78f).setDuration(120).start();
        try {
            recognizer.startListening(
                    BoopWakeRecognitionIntent.build(Locale.getDefault(), session.audioSource()));
        } catch (RuntimeException e) {
            closeWakeAudioSession();
            listening = false;
            recognitionMode = RecognitionMode.NONE;
            face.animate().alpha(1.0f).setDuration(120).start();
            if (wakeCoordinator != null) {
                wakeCoordinator.failWakeSession();
            }
        }
    }

    private void stopListening() {
        RecognitionMode stoppedMode = recognitionMode;
        cancelAssistantFollowUpSilenceTimeout();
        assistantFollowUpListening = false;
        latestAssistantFollowUpPartial = null;
        recognitionMode = RecognitionMode.NONE;
        listening = false;
        face.animate().alpha(1.0f).setDuration(120).start();
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
        if (wakeCoordinator == null) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || recognizer == null) {
            wakeCoordinator.setRecognitionSupported(false);
            return;
        }
        if (wakeSupportCheckInFlight) {
            return;
        }

        ParcelFileDescriptor[] pipe = null;
        try {
            pipe = ParcelFileDescriptor.createPipe();
            ParcelFileDescriptor readSide = pipe[0];
            ParcelFileDescriptor writeSide = pipe[1];
            writeSide.close();
            wakeSupportCheckInFlight = true;
            Intent intent = BoopWakeRecognitionIntent.build(Locale.getDefault(), readSide);
            recognizer.checkRecognitionSupport(intent, getMainExecutor(), new RecognitionSupportCallback() {
                private void finishProbe() {
                    try {
                        readSide.close();
                    } catch (IOException ignored) {
                    }
                    wakeSupportCheckInFlight = false;
                }

                @Override
                public void onSupportResult(RecognitionSupport recognitionSupport) {
                    finishProbe();
                    if (wakeCoordinator != null) {
                        wakeCoordinator.setRecognitionSupported(true);
                    }
                }

                @Override
                public void onError(int error) {
                    finishProbe();
                    if (wakeCoordinator != null) {
                        wakeCoordinator.setRecognitionSupported(false);
                    }
                }
            });
        } catch (IOException | RuntimeException error) {
            wakeSupportCheckInFlight = false;
            if (pipe != null) {
                for (ParcelFileDescriptor descriptor : pipe) {
                    if (descriptor != null) {
                        try {
                            descriptor.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
            wakeCoordinator.setRecognitionSupported(false);
        }
    }

    private void handleRecognizedSpeech(String transcript) {
        if (BoopVoiceSettingsIntent.matches(transcript)) {
            showVoiceSettings();
            return;
        }

        String voiceReply = voiceController.maybeChangeVoice(transcript);
        if (voiceReply != null) {
            speak(voiceReply);
            return;
        }

        String exitReply = BoopConversationExitIntent.replyFor(transcript);
        if (exitReply != null) {
            speak(exitReply);
            return;
        }

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
            CommandOutcome outcome = commandRouter.process(transcript);
            runOnUiThread(() -> {
                if (outcome.status() == CommandOutcome.Status.ASSISTANT_REPLY) {
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

    private void speakThenOpenAssistantFollowUp(String text) {
        assistantFollowUpAfterTts = true;
        speak(text);
    }

    private void speak(String text) {
        wakeFaceForInteraction();
        if (wakeCoordinator != null) {
            wakeCoordinator.onTtsStarting();
        }
        if (ttsReady && tts != null) {
            int result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "boop-alpha3");
            if (result == TextToSpeech.ERROR) {
                finishTtsUtterance();
            }
        } else {
            finishTtsUtterance();
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
            if (ttsReady) {
                voiceController.initialize(tts, Locale.getDefault());
            }
        }
    }

    @Override public void onReadyForSpeech(Bundle params) { }
    @Override
    public void onBeginningOfSpeech() {
        if (assistantFollowUpListening && recognitionMode == RecognitionMode.TAP) {
            cancelAssistantFollowUpSilenceTimeout();
        }
    }
    @Override public void onRmsChanged(float rmsdB) { }
    @Override public void onBufferReceived(byte[] buffer) { }

    @Override
    public void onEndOfSpeech() {
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
        boolean failedAssistantFollowUp =
                failedMode == RecognitionMode.TAP && assistantFollowUpListening;
        String fallback = latestAssistantFollowUpPartial;
        cancelAssistantFollowUpSilenceTimeout();
        assistantFollowUpListening = false;
        latestAssistantFollowUpPartial = null;
        recognitionMode = RecognitionMode.NONE;
        listening = false;
        face.animate().alpha(1.0f).setDuration(120).start();

        if (suppressNextRecognizerError) {
            suppressNextRecognizerError = false;
            return;
        }

        if (failedMode == RecognitionMode.WAKE) {
            closeWakeAudioSession();
            if (wakeCoordinator != null) {
                if (error == SpeechRecognizer.ERROR_NO_MATCH
                        || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    wakeCoordinator.cancelWakeCapture();
                } else {
                    wakeCoordinator.failWakeSession();
                }
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
        RecognitionMode completedMode = recognitionMode;
        boolean completedAssistantFollowUp =
                completedMode == RecognitionMode.TAP && assistantFollowUpListening;
        cancelAssistantFollowUpSilenceTimeout();
        assistantFollowUpListening = false;
        latestAssistantFollowUpPartial = null;
        recognitionMode = RecognitionMode.NONE;
        listening = false;
        face.animate().alpha(1.0f).setDuration(120).start();

        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String best = null;
        if (matches != null && !matches.isEmpty()) {
            String candidate = matches.get(0).trim();
            if (!candidate.isEmpty()) {
                best = candidate;
            }
        }

        if (completedMode == RecognitionMode.WAKE) {
            closeWakeAudioSession();
            if (wakeCoordinator != null) {
                wakeCoordinator.markWakeProcessing();
            }
            String normalized = BoopWakeTranscriptNormalizer.stripLeadingWakeWord(best);
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
        thinking = false;
        voiceSettingsOpen = false;
        closeWakeAudioSession();
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
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        voiceController = null;
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        interactionSurface = null;
        super.onDestroy();
    }
}
