package com.boop.alpha1;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Locale;

/** Voice Settings uses the same opt-in profile as Build a Boop. */
final class BoopVoiceSharingControls implements View.OnAttachStateChangeListener {
    private final Activity activity;
    private final BoopVoiceController voice;
    private final BoopSharedVoiceProfileRuntime sharing;
    private final SeekBar pitch, cadence;
    private final TextView profile, status, naturalStatus;
    private String displayedVoice;
    private final Button toggle, retry;
    private boolean pitchDragging, cadenceDragging;
    private Runnable unwatch;

    static void install(Activity activity, LinearLayout column, BoopVoiceController voice,
                        SeekBar pitch, SeekBar cadence, TextView naturalStatus) {
        new BoopVoiceSharingControls(activity, column, voice, pitch, cadence, naturalStatus);
    }

    private BoopVoiceSharingControls(Activity activity, LinearLayout column,
                                    BoopVoiceController voice, SeekBar pitch, SeekBar cadence, TextView naturalStatus) {
        this.activity = activity;
        this.voice = voice;
        this.pitch = pitch;
        this.cadence = cadence;
        this.naturalStatus = naturalStatus;
        sharing = BoopSharedVoiceProfileRuntime.get(activity);
        profile = label(column, "");
        toggle = button(column, "Share voice profile: Off", () -> {
            if (sharing.enabled()) sharing.setEnabled(false); else confirm();
        });
        status = label(column, "");
        retry = button(column, "Retry voice sharing", this::confirm);
        label(column, "Share the natural voice, pitch and cadence with your other BOOPs. "
                + "Turn sharing on on each device. Natural voices must be downloaded on each one.");
        bindSlider(pitch, true);
        bindSlider(cadence, false);
        column.addOnAttachStateChangeListener(this);
        if (column.isAttachedToWindow()) onViewAttachedToWindow(column);
    }

    private void bindSlider(SeekBar slider, boolean isPitch) {
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (!fromUser) return;
                if (isPitch) voice.setPitch(BoopVoiceTuning.pitchFromProgress(progress));
                else voice.setSpeechRate(BoopVoiceTuning.rateFromProgress(progress));
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {
                if (isPitch) pitchDragging = true; else cadenceDragging = true;
            }
            @Override public void onStopTrackingTouch(SeekBar bar) {
                if (isPitch) pitchDragging = false; else cadenceDragging = false;
                refresh();
            }
        });
    }

    private void confirm() {
        new AlertDialog.Builder(activity).setTitle("Share BOOP's voice profile?")
                .setMessage("Use the same natural voice, pitch and cadence through your paired Home Assistant. "
                        + "The first BOOP shares its current voice; others join that profile. "
                        + "BOOP may create a voice-profile setting using administrator access. "
                        + "Voice downloads stay on each device.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Share", (dialog, which) -> sharing.setEnabled(true)).show();
    }

    private void refresh() {
        if (!pitchDragging) pitch.setProgress(BoopVoiceTuning.progressFromPitch(voice.pitch()));
        if (!cadenceDragging) cadence.setProgress(BoopVoiceTuning.progressFromRate(voice.speechRate()));
        String name = voice.naturalBackendSelectedAndUsable()
                ? voice.selectedNaturalVoice().name() : "Android voice";
        if (!name.equals(displayedVoice)) {
            if (voice.naturalBackendSelectedAndUsable()) naturalStatus.setText("Selected: " + name);
            else if (displayedVoice != null) naturalStatus.setText("Android voice is active on this device.");
            displayedVoice = name;
        }
        profile.setText(String.format(Locale.ROOT, "%s · Pitch %.2fx · Cadence %.2fx",
                name, voice.pitch(), voice.speechRate()));
        toggle.setText("Share voice profile: " + (sharing.enabled() ? "On" : "Off"));
        status.setText(sharing.enabled() ? sharing.status() : "Voice profile stays on this device.");
        retry.setVisibility(sharing.enabled() && !sharing.ready() ? View.VISIBLE : View.GONE);
    }

    @Override public void onViewAttachedToWindow(View view) {
        if (unwatch == null) unwatch = sharing.observe(this::refresh);
    }

    @Override public void onViewDetachedFromWindow(View view) {
        if (unwatch != null) { unwatch.run(); unwatch = null; }
        pitchDragging = cadenceDragging = false;
    }

    private TextView label(LinearLayout column, String text) {
        TextView view = new TextView(activity);
        view.setText(text);
        view.setTextColor(Color.LTGRAY);
        view.setTextSize(16f);
        view.setGravity(Gravity.CENTER);
        view.setPadding(0, dp(6), 0, dp(10));
        column.addView(view);
        return view;
    }

    private Button button(LinearLayout column, String text, Runnable action) {
        Button view = new Button(activity);
        view.setText(text);
        view.setTextColor(Color.WHITE);
        view.setTextSize(18f);
        view.setBackgroundColor(Color.rgb(42, 42, 42));
        view.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(60));
        params.setMargins(0, dp(4), 0, dp(8));
        column.addView(view, params);
        return view;
    }

    private int dp(int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
