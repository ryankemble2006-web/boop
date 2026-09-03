package com.boop.alpha1;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Locale;

public final class MainActivity extends Activity implements RecognitionListener, TextToSpeech.OnInitListener {
    private static final int REQ_RECORD_AUDIO = 1001;

    private ImageView face;
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean listening = false;
    private boolean pendingListenAfterPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        face = new ImageView(this);
        face.setBackgroundColor(Color.BLACK);
        face.setImageResource(R.drawable.boop_eyes);
        face.setScaleType(ImageView.ScaleType.FIT_CENTER);
        face.setAdjustViewBounds(false);
        face.setContentDescription("BOOP face. Tap anywhere to speak.");
        face.setOnTouchListener(this::onFaceTouch);
        setContentView(face);

        keepAwakeAndHideSystemUi();

        tts = new TextToSpeech(this, this);
        createRecognizer();
    }

    private boolean onFaceTouch(View view, MotionEvent event) {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(this)) {
            recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(this);
        } else {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        }
        recognizer.setRecognitionListener(this);
    }

    private void startListening() {
        if (recognizer == null) {
            speak("I can't hear speech on this device yet.");
            return;
        }

        if (tts != null) {
            tts.stop();
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);

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
    }

    private void speak(String text) {
        if (ttsReady && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "boop-alpha1");
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

    @Override
    public void onError(int error) {
        listening = false;
        face.animate().alpha(1.0f).setDuration(120).start();

        if (error == SpeechRecognizer.ERROR_CLIENT) {
            return;
        }
        if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            speak("I didn't catch that.");
        } else {
            speak("Try that again.");
        }
    }

    @Override
    public void onResults(Bundle results) {
        listening = false;
        face.animate().alpha(1.0f).setDuration(120).start();

        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String best = matches.get(0).trim();
            if (!best.isEmpty()) {
                speak("You said, " + best);
                return;
            }
        }
        speak("I didn't catch that.");
    }

    @Override public void onPartialResults(Bundle partialResults) { }
    @Override public void onEvent(int eventType, Bundle params) { }

    @Override
    protected void onDestroy() {
        if (recognizer != null) {
            recognizer.destroy();
            recognizer = null;
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        super.onDestroy();
    }
}
