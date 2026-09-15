package com.boop.alpha1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.boop.eyes.*;
import java.util.Locale;

/** Current production artwork and catalogue, with local controls for joint device inspection. */
public final class BoopCanonicalAnimationActivity extends Activity implements Choreographer.FrameCallback {
    private final AnimationReviewTimeline timeline = new AnimationReviewTimeline();
    private GLSurfaceView surface;
    private CanonicalEyeRenderer renderer;
    private FrameLayout stage;
    private NotificationSignView sign;
    private LinearLayout panel;
    private TextView label;
    private SeekBar position;
    private Button pause, slow, reveal;
    private boolean resumed, focused, running, signActive, controlsHidden;
    private int signStyle;
    private long lastFrame;
    private double signTime;
    private String rendererError;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        FrameLayout frame = new FrameLayout(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        frame.addView(root, new FrameLayout.LayoutParams(-1, -1));
        label = new TextView(this);
        label.setTextColor(Color.WHITE); label.setTextSize(15);
        label.setPadding(dp(12), dp(4), dp(12), dp(4));
        root.addView(label, new LinearLayout.LayoutParams(-1, -2));

        surface = new GLSurfaceView(this);
        surface.setEGLContextClientVersion(2);
        surface.setPreserveEGLContextOnPause(true);
        renderer = new CanonicalEyeRenderer(getAssets(), detail -> runOnUiThread(() -> {
            rendererError = detail; updateControls();
        }));
        surface.setRenderer(renderer);
        surface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        EyeColourBinding.install(surface, renderer);
        stage = new FrameLayout(this);
        stage.addView(surface, new FrameLayout.LayoutParams(-1, -1));
        sign = new NotificationSignView(this); sign.setVisibility(View.GONE);
        stage.addView(sign, new FrameLayout.LayoutParams(-1, -1));
        stage.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob) -> resizeEyes());
        root.addView(stage, new LinearLayout.LayoutParams(-1, 0, 1));

        panel = new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL);
        root.addView(panel, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout clips = strip(panel);
        for (EyeMotion.Clip clip : EyeCatalogue.ALL) add(clips, clip.label, () -> select(clip.id));

        position = new SeekBar(this);
        position.setMax(10000); position.setKeyProgressIncrement(25);
        position.setContentDescription("Animation position");
        position.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                if (fromUser && !signActive) {
                    timeline.seek(timeline.clip().duration * value / 10000.0);
                    lastFrame = 0; renderFrame();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar bar) {
                timeline.setPaused(true); lastFrame = 0; updateControls();
            }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        });
        panel.addView(position, new LinearLayout.LayoutParams(-1, dp(38)));
        LinearLayout controls = strip(panel);
        pause = add(controls, "Pause", () -> {
            timeline.setPaused(!timeline.isPaused()); lastFrame = 0; updateControls();
        });
        slow = add(controls, "Slow review", () -> {
            timeline.setSlow(!timeline.isSlow()); lastFrame = 0; updateControls();
        });
        add(controls, "Half blink", () -> {
            leaveSign(); timeline.halfBlink(); lastFrame = 0; renderFrame();
        });
        add(controls, "-1 ms", () -> step(-1));
        add(controls, "+1 ms", () -> step(1));
        add(controls, "Hide controls", () -> setControlsHidden(true));
        add(controls, "Done", this::finish);

        LinearLayout signs = strip(panel);
        String[] signNames = {"WhatsApp sign", "Gmail sign", "Facebook sign", "X sign", "Freddie"};
        for (int i = 0; i < signNames.length; i++) {
            final int style = i; add(signs, signNames[i], () -> showSign(style));
        }
        reveal = new Button(this); reveal.setText("Show controls"); reveal.setAllCaps(false);
        reveal.setOnClickListener(v -> setControlsHidden(false));
        FrameLayout.LayoutParams revealParams = new FrameLayout.LayoutParams(dp(145), dp(48), Gravity.TOP | Gravity.END);
        frame.addView(reveal, revealParams);
        setContentView(frame);
        readIntent(getIntent());
        if (state != null) {
            timeline.select(state.getString("clip", "idle"));
            timeline.seek(state.getDouble("position"));
            timeline.setPaused(state.getBoolean("paused"));
            timeline.setSlow(state.getBoolean("slow"));
            signStyle = state.getInt("sign_style"); signTime = state.getDouble("sign_time");
            signActive = state.getBoolean("sign_active");
            sign.setVisibility(signActive ? View.VISIBLE : View.GONE);
            resizeEyes();
        }
        setControlsHidden(state != null && state.getBoolean("controls_hidden"));
        root.post(this::immersive);
        renderFrame();
    }

    private LinearLayout strip(LinearLayout parent) {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        scroll.addView(row);
        parent.addView(scroll, new LinearLayout.LayoutParams(-1, dp(48)));
        return row;
    }
    private Button add(LinearLayout row, String text, Runnable action) {
        Button button = new Button(this); button.setText(text); button.setTextSize(14);
        button.setAllCaps(false); button.setFocusable(true);
        button.setOnClickListener(v -> action.run());
        row.addView(button, new LinearLayout.LayoutParams(dp(130), dp(48)));
        return button;
    }
    private void select(String id) {
        leaveSign(); timeline.select(id); lastFrame = 0; renderFrame();
    }
    private void leaveSign() {
        signActive = false;
        if (sign != null) { sign.setVisibility(View.GONE); resizeEyes(); }
    }
    private void step(double delta) {
        if (signActive) { signTime = Math.max(0, signTime + delta); timeline.setPaused(true); }
        else timeline.step(delta);
        lastFrame = 0; renderFrame();
    }
    private void showSign(int style) {
        signStyle = Math.floorMod(style, 5); signTime = 0; signActive = true;
        timeline.setPaused(false); lastFrame = 0;
        sign.setVisibility(View.VISIBLE); resizeEyes(); renderFrame();
    }
    private void resizeEyes() {
        if (surface == null || stage == null) return;
        int height = signActive ? Math.max(1, (int)(stage.getHeight() * .64f)) : -1;
        if (surface.getLayoutParams().height != height)
            surface.setLayoutParams(new FrameLayout.LayoutParams(-1, height));
    }
    private void setControlsHidden(boolean hidden) {
        controlsHidden = hidden;
        label.setVisibility(hidden ? View.GONE : View.VISIBLE);
        panel.setVisibility(hidden ? View.GONE : View.VISIBLE);
        reveal.setVisibility(hidden ? View.VISIBLE : View.GONE);
        if (hidden) reveal.requestFocus(); else pause.requestFocus();
    }
    @Override public void onBackPressed() {
        if (controlsHidden) setControlsHidden(false); else super.onBackPressed();
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("clip", timeline.clip().id);
        state.putDouble("position", timeline.positionMs());
        state.putBoolean("paused", timeline.isPaused());
        state.putBoolean("slow", timeline.isSlow());
        state.putBoolean("controls_hidden", controlsHidden);
        state.putBoolean("sign_active", signActive);
        state.putInt("sign_style", signStyle); state.putDouble("sign_time", signTime);
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent); setIntent(intent); readIntent(intent);
    }
    private void readIntent(Intent intent) {
        select(intent == null ? "idle" : intent.getStringExtra("clip"));
        if (intent != null) {
            timeline.setSlow(intent.getBooleanExtra("slow", false));
            if (intent.hasExtra("sign")) showSign(intent.getIntExtra("sign", 0));
            if (intent.hasExtra("freeze_ms")) {
                if (signActive) { signTime = Math.max(0, intent.getIntExtra("freeze_ms", 0)); timeline.setPaused(true); }
                else timeline.seek(intent.getIntExtra("freeze_ms", 0));
            }
        }
        renderFrame();
    }
    private void immersive() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
    @Override protected void onResume() {
        super.onResume(); resumed = true; surface.onResume(); lastFrame = 0; updateLoop();
    }
    @Override protected void onPause() {
        resumed = false; updateLoop(); surface.onPause(); super.onPause();
    }
    @Override public void onWindowFocusChanged(boolean value) {
        super.onWindowFocusChanged(value); focused = value;
        if (surface != null) { if (value) immersive(); updateLoop(); }
    }
    private void updateLoop() {
        boolean shouldRun = resumed && focused;
        if (shouldRun && !running) {
            running = true; lastFrame = 0; Choreographer.getInstance().postFrameCallback(this);
        } else if (!shouldRun && running) {
            running = false; Choreographer.getInstance().removeFrameCallback(this); lastFrame = 0;
        }
    }
    @Override public void doFrame(long now) {
        if (!running) return;
        if (lastFrame != 0) {
            double elapsed = Math.min(100, Math.max(0, (now - lastFrame) / 1000000.0));
            if (signActive) {
                if (!timeline.isPaused()) signTime += elapsed * (timeline.isSlow() ? .15 : 1);
            } else timeline.advance(elapsed);
        }
        lastFrame = now; renderFrame();
        Choreographer.getInstance().postFrameCallback(this);
    }
    private void renderFrame() {
        if (renderer == null) return;
        if (signActive) {
            SignMotion.Pose pose = signStyle == 4 ? FreddieMotion.sample(signTime) : SignMotion.sample(signTime, signStyle);
            renderer.pose = pose.eyes;
            if (signStyle == 4) sign.showFreddie(pose); else sign.show(pose, signStyle);
        } else renderer.pose = timeline.pose();
        surface.requestRender(); updateControls();
    }
    private void updateControls() {
        if (pause == null || position == null) return;
        pause.setText(timeline.isPaused() ? "Resume" : "Pause");
        slow.setText(timeline.isSlow() ? "Normal speed" : "Slow review");
        position.setEnabled(!signActive);
        position.setProgress((int)Math.round(10000 * timeline.positionMs() / timeline.clip().duration));
        EyeMotion.Pose pose = renderer.pose;
        label.setText(rendererError != null ? "Renderer error: " + rendererError : String.format(Locale.US,
            "BOOP Felt Lab · %s · %.1f ms · lids %.0f%% / %.0f%%%s",
            signActive ? "Sign" : timeline.clip().label, signActive ? signTime : timeline.positionMs(),
            pose.left * 100, pose.right * 100, timeline.isPaused() ? " · Paused" : ""));
    }
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
}
