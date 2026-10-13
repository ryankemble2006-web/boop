package com.boop.alpha1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.boop.eyes.AnimationSpeedPreferences;
import com.boop.eyes.CanonicalEyeRenderer;
import com.boop.eyes.EyeColourBinding;
import com.boop.eyes.FeltColourPreferences;
import com.boop.eyes.HandColourPreferences;
import com.boop.eyes.NotificationSignView;
import com.boop.eyes.SignMotion;
import android.widget.FrameLayout;
import android.opengl.GLSurfaceView;

/** Shared settings with one canonical, live iris-colour preview. */
public final class BoopAppearanceActivity extends Activity {
    private static final double[] SPEEDS = {.5, 1, 1.5, 2};
    private static final String[] SPEED_LABELS = {"0.5x", "1x", "1.5x", "2x"};
    private BoopSharedEyeColourRuntime sharing;
    private BoopSharedFeltColourRuntime feltSharing;
    private SeekBar felt;
    private TextView feltLabel, feltStatus;
    private Button feltShare, feltRetry;
    private boolean feltDragging;
    private Runnable unwatchFelt;
    private BoopSharedHandColourRuntime handSharing;
    private SeekBar hand;
    private TextView handLabel, handStatus;
    private Button handShare, handRetry;
    private boolean handDragging;
    private Runnable unwatchHand;
    private BoopSharedVoiceProfileRuntime voiceSharing;
    private BoopVoiceController voiceController;
    private SeekBar voicePitch, voiceSpeed;
    private TextView voicePitchLabel, voiceSpeedLabel, voiceStatus;
    private Button voiceShare, voiceRetry;
    private boolean voicePitchDragging, voiceSpeedDragging;
    private Runnable unwatchVoice;
    private SharedPreferences eyes, appearance, voice;
    private TextView status, hueLabel, speedLabel;
    private SeekBar hue;
    private GLSurfaceView preview;
    private Button share, retry;
    private final Button[] speedButtons = new Button[4];
    private Runnable unwatch;
    private boolean dragging;
    private final SharedPreferences.OnSharedPreferenceChangeListener hueListener = (store, key) -> {
        if (key == null || "hue_degrees".equals(key)) refresh();
        if (key == null || FeltColourPreferences.KEY.equals(key)) refreshFelt();
        if (key == null || HandColourPreferences.KEY.equals(key)) refreshHand();
    };
    private final SharedPreferences.OnSharedPreferenceChangeListener speedListener = (store, key) -> {
        if (key == null || "animation_speed".equals(key)) refreshSpeed();
    };
    private final SharedPreferences.OnSharedPreferenceChangeListener voiceListener = (store, key) -> {
        if (key == null
                || BoopVoiceController.KEY_PITCH.equals(key)
                || BoopVoiceController.KEY_SPEECH_RATE.equals(key)
                || BoopVoiceController.KEY_SELECTED_BACKEND.equals(key)
                || BoopVoiceController.KEY_NATURAL_SPEAKER_KEY.equals(key)) {
            refreshVoice();
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        sharing = BoopSharedEyeColourRuntime.get(this);
        feltSharing = BoopSharedFeltColourRuntime.get(this);
        handSharing = BoopSharedHandColourRuntime.get(this);
        voiceSharing = BoopSharedVoiceProfileRuntime.get(this);
        voiceController = new BoopVoiceController(this);
        eyes = getSharedPreferences("boop_eyes", MODE_PRIVATE);
        appearance = getSharedPreferences("boop_appearance", MODE_PRIVATE);
        voice = getSharedPreferences(BoopVoiceController.PREFS_NAME, MODE_PRIVATE);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        preview = new GLSurfaceView(this);
        preview.setEGLContextClientVersion(2);
        preview.setPreserveEGLContextOnPause(true);
        preview.setFocusable(false);
        preview.setClickable(false);
        preview.setContentDescription("BOOP live eye, felt and hand colour preview");
        CanonicalEyeRenderer previewRenderer = new CanonicalEyeRenderer(
                getAssets(), detail -> android.util.Log.e("BOOPEyes", detail));
        preview.setRenderer(previewRenderer);
        preview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        EyeColourBinding.install(preview, previewRenderer);
        // Fixed above the scrolling controls: slider edits never scroll BOOP away.
        int previewHeight = Math.min(dp(200), Math.max(dp(96),
                getResources().getDisplayMetrics().heightPixels / 4));
        FrameLayout previewStage = new FrameLayout(this);
        previewStage.addView(preview, new FrameLayout.LayoutParams(-1, Math.round(previewHeight * .64f)));
        NotificationSignView previewHands = new NotificationSignView(this);
        previewHands.show(SignMotion.sample(10000, 0), 0);
        previewStage.addView(previewHands, new FrameLayout.LayoutParams(-1, -1));
        root.addView(previewStage, new LinearLayout.LayoutParams(-1, previewHeight));
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(28), dp(24), dp(28), dp(24));
        column.setBackgroundColor(Color.BLACK);
        scroll.addView(column);
        text(column, "Build a Boop", 28);
        text(column, "A little colour. A lot of character. Yours to make, for free.", 18);
        hueLabel = text(column, "Eye colour", 20);
        hue = new SeekBar(this);
        hue.setMax(359);
        hue.setKeyProgressIncrement(5);
        hue.setContentDescription("Eye colour hue");
        hue.setProgress(BoopEyeHue.loadHue(this));
        hue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                if (fromUser) BoopEyeHue.saveHue(BoopAppearanceActivity.this, value);
                hueLabel.setText("Eye colour: " + value);
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { dragging = true; }
            @Override public void onStopTrackingTouch(SeekBar bar) { dragging = false; refresh(); }
        });
        column.addView(hue, new LinearLayout.LayoutParams(-1, dp(60)));
        button(column, "Original blue", () -> BoopEyeHue.saveHue(this, BoopEyeHueMath.DEFAULT_HUE_DEGREES));
        feltLabel = text(column, "Felt colour", 20);
        felt = new SeekBar(this);
        felt.setMax(359); felt.setKeyProgressIncrement(5);
        felt.setContentDescription("Felt colour, zero is original charcoal");
        felt.setProgress(FeltColourPreferences.load(this));
        felt.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                if (fromUser) FeltColourPreferences.save(BoopAppearanceActivity.this, value);
                feltLabel.setText(value == 0 ? "Felt colour: Original charcoal" : "Felt colour: " + value);
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { feltDragging = true; }
            @Override public void onStopTrackingTouch(SeekBar bar) { feltDragging = false; refreshFelt(); }
        });
        column.addView(felt, new LinearLayout.LayoutParams(-1, dp(60)));
        button(column, "Original charcoal", () -> FeltColourPreferences.save(this, 0));
        handLabel = text(column, "Hand colour", 20);
        hand = new SeekBar(this);
        hand.setMax(359); hand.setKeyProgressIncrement(5);
        hand.setContentDescription("Hand colour, zero is original yellow");
        hand.setProgress(HandColourPreferences.load(this));
        hand.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                if (fromUser) HandColourPreferences.save(BoopAppearanceActivity.this, value);
                handLabel.setText(value == 0 ? "Hand colour: Original yellow" : "Hand colour: " + value);
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { handDragging = true; }
            @Override public void onStopTrackingTouch(SeekBar bar) { handDragging = false; refreshHand(); }
        });
        column.addView(hand, new LinearLayout.LayoutParams(-1, dp(60)));
        button(column, "Original yellow", () -> HandColourPreferences.save(this, 0));
        handStatus = text(column, "", 18);
        handShare = button(column, "Share hand colour: Off", () -> {
            if (handSharing.enabled()) handSharing.setEnabled(false); else confirmHandSharing();
        });
        handRetry = button(column, "Retry hand sharing", this::confirmHandSharing);
        feltStatus = text(column, "", 18);
        feltShare = button(column, "Share felt colour: Off", () -> {
            if (feltSharing.enabled()) feltSharing.setEnabled(false); else confirmFeltSharing();
        });
        feltRetry = button(column, "Retry felt sharing", this::confirmFeltSharing);
        text(column, "Share the same eye colour with your other BOOPs using the same Home Assistant. Sharing works while BOOP is open; the last colour stays available offline.", 18);
        status = text(column, "", 18);
        share = button(column, "Share eye colour: Off", () -> {
            if (sharing.enabled()) sharing.setEnabled(false);
            else confirmSharing();
        });
        retry = button(column, "Retry sharing", this::confirmSharing);

        voicePitchLabel = text(column, "Voice pitch", 20);
        voicePitch = new SeekBar(this);
        voicePitch.setMax(BoopVoiceTuning.PROGRESS_MAX);
        voicePitch.setKeyProgressIncrement(25);
        voicePitch.setContentDescription("Voice pitch");
        voicePitch.setProgress(BoopVoiceTuning.progressFromPitch(voiceController.pitch()));
        voicePitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                if (fromUser) voiceController.setPitch(BoopVoiceTuning.pitchFromProgress(value));
                refreshVoiceLabels();
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { voicePitchDragging = true; }
            @Override public void onStopTrackingTouch(SeekBar bar) { voicePitchDragging = false; refreshVoice(); }
        });
        column.addView(voicePitch, new LinearLayout.LayoutParams(-1, dp(60)));

        voiceSpeedLabel = text(column, "Voice speed", 20);
        voiceSpeed = new SeekBar(this);
        voiceSpeed.setMax(BoopVoiceTuning.PROGRESS_MAX);
        voiceSpeed.setKeyProgressIncrement(25);
        voiceSpeed.setContentDescription("Voice speed");
        voiceSpeed.setProgress(BoopVoiceTuning.progressFromRate(voiceController.speechRate()));
        voiceSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                if (fromUser) voiceController.setSpeechRate(BoopVoiceTuning.rateFromProgress(value));
                refreshVoiceLabels();
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { voiceSpeedDragging = true; }
            @Override public void onStopTrackingTouch(SeekBar bar) { voiceSpeedDragging = false; refreshVoice(); }
        });
        column.addView(voiceSpeed, new LinearLayout.LayoutParams(-1, dp(60)));
        text(column, "Natural voice choice, pitch and speed can follow this BOOP to your other BOOPs. Each device keeps its own optional natural-voice download.", 18);
        voiceStatus = text(column, "", 18);
        voiceShare = button(column, "Share voice profile: Off", () -> {
            if (voiceSharing.enabled()) voiceSharing.setEnabled(false); else confirmVoiceSharing();
        });
        voiceRetry = button(column, "Retry voice sharing", this::confirmVoiceSharing);

        speedLabel = text(column, "Animation speed: 1x", 20);
        text(column, "BOOP only, on this device. 1x keeps the original timing. Android transitions, music and voice stay unchanged.", 18);
        LinearLayout speeds = new LinearLayout(this);
        speeds.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < SPEEDS.length; i++) {
            final int index = i;
            Button button = new Button(this);
            button.setText(SPEED_LABELS[i]); button.setTextSize(18); button.setAllCaps(false);
            button.setMinWidth(0); button.setMinimumWidth(0); button.setMinHeight(dp(60));
            button.setOnClickListener(v -> AnimationSpeedPreferences.save(this, SPEEDS[index]));
            button.setOnFocusChangeListener((v, focused) -> refreshSpeed());
            speedButtons[i] = button;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(64), 1f);
            params.setMargins(dp(3), dp(4), dp(3), dp(4));
            speeds.addView(button, params);
        }
        column.addView(speeds, new LinearLayout.LayoutParams(-1, -2));
        button(column, "Felt animation lab", () -> startActivity(
                new android.content.Intent(this, BoopCanonicalAnimationActivity.class)));
        button(column, "Done", this::finish);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
        refresh();
        refreshFelt();
        refreshHand();
        refreshVoice();
        refreshSpeed();
    }

    private void confirmVoiceSharing() {
        new AlertDialog.Builder(this).setTitle("Share BOOP's voice profile?")
                .setMessage("Use the same natural voice choice, pitch and speed on your BOOPs through your paired Home Assistant. BOOP may create a separate voice-profile setting using administrator access. The natural voice model itself stays private on each device and is never transferred.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Share", (dialog, which) -> voiceSharing.setEnabled(true)).show();
    }

    private void refreshVoice() {
        if (voicePitch == null || voiceSpeed == null || voiceShare == null) return;
        float pitch = voiceController.pitch();
        float rate = voiceController.speechRate();
        if (!voicePitchDragging) voicePitch.setProgress(BoopVoiceTuning.progressFromPitch(pitch));
        if (!voiceSpeedDragging) voiceSpeed.setProgress(BoopVoiceTuning.progressFromRate(rate));
        refreshVoiceLabels();
        voiceShare.setText("Share voice profile: " + (voiceSharing.enabled() ? "On" : "Off"));
        voiceStatus.setText(voiceSharing.enabled() ? voiceSharing.status() : "Voice profile stays on this device.");
        voiceRetry.setVisibility(voiceSharing.enabled() && !voiceSharing.ready()
                ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void refreshVoiceLabels() {
        if (voicePitchLabel != null && voiceController != null) {
            voicePitchLabel.setText("Voice pitch: " + voiceScale(voiceController.pitch()));
        }
        if (voiceSpeedLabel != null && voiceController != null) {
            voiceSpeedLabel.setText("Voice speed: " + voiceScale(voiceController.speechRate()));
        }
    }

    private String voiceScale(float value) {
        return String.format(java.util.Locale.ROOT, "%.2fx", value);
    }

    private void confirmHandSharing() {
        new AlertDialog.Builder(this).setTitle("Share BOOP's hand colour?")
                .setMessage("Use the same hand colour on your BOOPs through your paired Home Assistant. BOOP may create a separate hand-colour setting using administrator access. Existing shared hand colour is used first; offline edits stay local.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Share", (dialog, which) -> handSharing.setEnabled(true)).show();
    }
    private void refreshHand() {
        if (hand == null || handShare == null) return;
        int value = HandColourPreferences.load(this);
        if (!handDragging) hand.setProgress(value);
        handLabel.setText(value == 0 ? "Hand colour: Original yellow" : "Hand colour: " + value);
        handShare.setText("Share hand colour: " + (handSharing.enabled() ? "On" : "Off"));
        handStatus.setText(handSharing.status());
        handRetry.setVisibility(handSharing.enabled() && !handSharing.ready()
                ? android.view.View.VISIBLE : android.view.View.GONE);
    }
    private void confirmFeltSharing() {
        new AlertDialog.Builder(this).setTitle("Share BOOP's felt colour?")
                .setMessage("Use the same felt colour on your BOOPs through your paired Home Assistant. BOOP may create a separate felt-colour setting using administrator access. Existing shared felt is used first; offline edits stay local.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Share", (dialog, which) -> feltSharing.setEnabled(true)).show();
    }
    private void refreshFelt() {
        if (felt == null || feltShare == null) return;
        int value = FeltColourPreferences.load(this);
        if (!feltDragging) felt.setProgress(value);
        feltLabel.setText(value == 0 ? "Felt colour: Original charcoal" : "Felt colour: " + value);
        feltShare.setText("Share felt colour: " + (feltSharing.enabled() ? "On" : "Off"));
        feltStatus.setText(feltSharing.status());
        feltRetry.setVisibility(feltSharing.enabled() && !feltSharing.ready()
                ? android.view.View.VISIBLE : android.view.View.GONE);
    }
    private void confirmSharing() {
        new AlertDialog.Builder(this).setTitle("Share BOOP's eye colour?")
                .setMessage("BOOP will use one eye-colour setting in your paired Home Assistant. If needed, it will create that setting using your administrator access. Existing shared colour is used first. Offline edits stay local and are not sent later.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Share", (dialog, which) -> sharing.setEnabled(true)).show();
    }
    private void refresh() {
        if (hue == null) return;
        int value = BoopEyeHue.loadHue(this);
        if (!dragging) hue.setProgress(value);
        hueLabel.setText("Eye colour: " + value);
        share.setText("Share eye colour: " + (sharing.enabled() ? "On" : "Off"));
        status.setText(sharing.enabled() ? sharing.status() : "Eye colour stays on this device.");
        retry.setVisibility(sharing.enabled() && !sharing.ready()
                ? android.view.View.VISIBLE : android.view.View.GONE);
    }
    private void refreshSpeed() {
        if (speedLabel == null) return;
        double selected = AnimationSpeedPreferences.load(this);
        for (int i = 0; i < SPEEDS.length; i++) {
            Button button = speedButtons[i];
            if (button == null) continue;
            boolean chosen = SPEEDS[i] == selected;
            if (chosen) speedLabel.setText("Animation speed: " + SPEED_LABELS[i]);
            button.setSelected(chosen);
            button.setTextColor(chosen || button.hasFocus() ? 0xff4db8ff : Color.WHITE);
            button.setContentDescription("BOOP animation speed " + SPEED_LABELS[i] + (chosen ? ", selected" : ""));
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.rgb(42, 42, 42));
            background.setCornerRadius(dp(10));
            if (button.hasFocus() || chosen) background.setStroke(dp(button.hasFocus() ? 3 : 1), 0xff4db8ff);
            button.setBackground(background);
        }
    }
    @Override protected void onResume() {
        super.onResume();
        preview.onResume();
        preview.requestRender();
    }
    @Override protected void onPause() {
        preview.onPause();
        super.onPause();
    }
    @Override protected void onStart() {
        super.onStart();
        eyes.registerOnSharedPreferenceChangeListener(hueListener);
        appearance.registerOnSharedPreferenceChangeListener(speedListener);
        voice.registerOnSharedPreferenceChangeListener(voiceListener);
        unwatch = sharing.observe(this::refresh);
        unwatchFelt = feltSharing.observe(this::refreshFelt);
        unwatchHand = handSharing.observe(this::refreshHand);
        unwatchVoice = voiceSharing.observe(this::refreshVoice);
        refreshVoice();
        refreshSpeed();
    }
    @Override protected void onStop() {
        eyes.unregisterOnSharedPreferenceChangeListener(hueListener);
        appearance.unregisterOnSharedPreferenceChangeListener(speedListener);
        voice.unregisterOnSharedPreferenceChangeListener(voiceListener);
        if (unwatch != null) { unwatch.run(); unwatch = null; }
        if (unwatchFelt != null) { unwatchFelt.run(); unwatchFelt = null; }
        if (unwatchHand != null) { unwatchHand.run(); unwatchHand = null; }
        if (unwatchVoice != null) { unwatchVoice.run(); unwatchVoice = null; }
        super.onStop();
    }
    private TextView text(LinearLayout column, String value, int size) {
        TextView view = new TextView(this);
        view.setText(value); view.setTextSize(size); view.setTextColor(Color.WHITE);
        view.setPadding(0, dp(10), 0, dp(10));
        column.addView(view);
        return view;
    }
    private Button button(LinearLayout column, String title, Runnable action) {
        Button view = new Button(this);
        view.setText(title); view.setTextSize(20); view.setMinHeight(dp(60));
        view.setOnClickListener(v -> action.run());
        column.addView(view);
        return view;
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
