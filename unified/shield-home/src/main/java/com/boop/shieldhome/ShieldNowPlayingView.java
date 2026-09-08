package com.boop.shieldhome;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/** Remote-first Now Playing card. It owns only media UI and never rerenders launcher rows. */
public final class ShieldNowPlayingView extends FrameLayout {
    private static final long PROGRESS_TICK_MS = 500L;
    static final int MASCOT_BAY_DP = 230;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ImageView artwork;
    private final TextView title;
    private final TextView subtitle;
    private final TextView stateLabel;
    private final ProgressBar progress;
    private final TextView sourceButton;
    private final TextView previousButton;
    private final TextView rewindButton;
    private final TextView playPauseButton;
    private final TextView fastForwardButton;
    private final TextView nextButton;

    private NowPlayingSnapshot snapshot;
    private ShieldHomeView.Callbacks callbacks;
    private boolean tickerRunning;

    private final Runnable progressTicker = new Runnable() {
        @Override public void run() {
            tickerRunning = false;
            NowPlayingSnapshot current = snapshot;
            if (current == null || !current.isPlaying() || getVisibility() != VISIBLE) {
                return;
            }
            updateProgress(current, SystemClock.elapsedRealtime());
            tickerRunning = true;
            handler.postDelayed(this, PROGRESS_TICK_MS);
        }
    };

    public ShieldNowPlayingView(Context context) {
        this(context, null);
    }

    public ShieldNowPlayingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClipChildren(false);
        setClipToPadding(false);
        setPadding(dp(18), dp(14), dp(18), dp(14));
        setBackground(cardBackground());

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setClipChildren(false);
        addView(row, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        artwork = new ImageView(context);
        artwork.setScaleType(ImageView.ScaleType.CENTER_CROP);
        artwork.setBackgroundColor(Color.rgb(28, 28, 28));
        artwork.setContentDescription("Open source player");
        artwork.setFocusable(true);
        artwork.setClickable(true);
        artwork.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onOpenNowPlayingSource();
        });
        installFocusPop(artwork);
        LinearLayout.LayoutParams artParams = new LinearLayout.LayoutParams(dp(154), dp(154));
        artParams.rightMargin = dp(20);
        row.addView(artwork, artParams);

        LinearLayout details = new LinearLayout(context);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(details, new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));

        LinearLayout titleRow = new LinearLayout(context);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        details.addView(titleRow, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout textStack = new LinearLayout(context);
        textStack.setOrientation(LinearLayout.VERTICAL);
        titleRow.addView(textStack, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        title = text(24, Color.WHITE);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        textStack.addView(title, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        subtitle = text(18, Color.LTGRAY);
        subtitle.setSingleLine(true);
        subtitle.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = dp(2);
        textStack.addView(subtitle, subtitleParams);

        sourceButton = actionButton("Open player");
        sourceButton.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onOpenNowPlayingSource();
        });
        LinearLayout.LayoutParams sourceParams = new LinearLayout.LayoutParams(dp(130), dp(44));
        sourceParams.leftMargin = dp(12);
        titleRow.addView(sourceButton, sourceParams);

        stateLabel = text(14, Color.LTGRAY);
        LinearLayout.LayoutParams stateParams = wrap();
        stateParams.topMargin = dp(5);
        details.addView(stateLabel, stateParams);

        progress = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(1000);
        progress.setProgressTintList(ColorStateList.valueOf(FocusChrome.accentColor(context)));
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, dp(8));
        progressParams.topMargin = dp(9);
        details.addView(progress, progressParams);

        LinearLayout controls = new LinearLayout(context);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams controlsParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, dp(46));
        controlsParams.topMargin = dp(4);
        details.addView(controls, controlsParams);

        previousButton = controlButton("Prev", () -> {
            if (callbacks != null) callbacks.onNowPlayingPrevious();
        });
        rewindButton = controlButton("Rew", () -> {
            if (callbacks != null) callbacks.onNowPlayingRewind();
        });
        playPauseButton = controlButton("Play", () -> {
            if (callbacks != null) callbacks.onNowPlayingPlayPause();
        });
        fastForwardButton = controlButton("Fwd", () -> {
            if (callbacks != null) callbacks.onNowPlayingFastForward();
        });
        nextButton = controlButton("Next", () -> {
            if (callbacks != null) callbacks.onNowPlayingNext();
        });

        addControl(controls, previousButton);
        addControl(controls, rewindButton);
        addControl(controls, playPauseButton);
        addControl(controls, fastForwardButton);
        addControl(controls, nextButton);

        // The launcher-owned headphones puppet is a separate non-focusable view layered by the
        // activity. Reserve identical physical space here so controls/text never draw beneath it.
        View mascotBay = new View(context);
        mascotBay.setFocusable(false);
        mascotBay.setClickable(false);
        mascotBay.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        row.addView(mascotBay, new LinearLayout.LayoutParams(
                dp(MASCOT_BAY_DP), LayoutParams.MATCH_PARENT));

        setVisibility(GONE);
    }

    public void bind(NowPlayingSnapshot snapshot, ShieldHomeView.Callbacks callbacks) {
        this.callbacks = callbacks;
        this.snapshot = snapshot;
        stopTicker();

        if (snapshot == null || !NowPlayingSelectionPolicy.eligible(snapshot.playbackState())) {
            setVisibility(GONE);
            artwork.setImageDrawable(null);
            return;
        }

        setVisibility(VISIBLE);
        artwork.setImageBitmap(snapshot.artwork());
        title.setText(snapshot.title().isEmpty() ? "Now Playing" : snapshot.title());
        subtitle.setText(snapshot.subtitle());
        stateLabel.setText(stateText(snapshot.playbackState()));
        playPauseButton.setText(snapshot.isPlaying() ? "Pause" : "Play");

        setControlEnabled(previousButton, snapshot.canPrevious());
        setControlEnabled(rewindButton, snapshot.canRewind());
        setControlEnabled(playPauseButton, snapshot.canPlayPause());
        setControlEnabled(fastForwardButton, snapshot.canFastForward());
        setControlEnabled(nextButton, snapshot.canNext());

        updateProgress(snapshot, SystemClock.elapsedRealtime());
        if (snapshot.isPlaying()) {
            startTicker();
        }
    }

    @Override protected void onDetachedFromWindow() {
        stopTicker();
        super.onDetachedFromWindow();
    }

    private void updateProgress(NowPlayingSnapshot current, long nowElapsedRealtimeMs) {
        long duration = current.durationMs();
        if (duration <= 0L) {
            progress.setProgress(0);
            progress.setVisibility(INVISIBLE);
            return;
        }
        progress.setVisibility(VISIBLE);
        long position = current.estimatedPositionMs(nowElapsedRealtimeMs);
        int scaled = (int) Math.max(0L, Math.min(1000L, (position * 1000L) / duration));
        progress.setProgress(scaled);
    }

    private void startTicker() {
        if (tickerRunning) return;
        tickerRunning = true;
        handler.postDelayed(progressTicker, PROGRESS_TICK_MS);
    }

    private void stopTicker() {
        handler.removeCallbacks(progressTicker);
        tickerRunning = false;
    }

    private void addControl(LinearLayout row, TextView button) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(70), dp(42));
        params.rightMargin = dp(8);
        row.addView(button, params);
    }

    private TextView controlButton(String label, Runnable action) {
        TextView button = actionButton(label);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        button.setPadding(dp(6), dp(4), dp(6), dp(4));
        button.setOnClickListener(v -> {
            if (v.isEnabled()) action.run();
        });
        return button;
    }

    private TextView actionButton(String label) {
        TextView view = text(16, Color.WHITE);
        view.setText(label);
        view.setGravity(Gravity.CENTER);
        view.setFocusable(true);
        view.setClickable(true);
        view.setPadding(dp(8), dp(5), dp(8), dp(5));
        view.setBackground(buttonBackground(false));
        installFocusPop(view);
        return view;
    }

    private void setControlEnabled(TextView view, boolean enabled) {
        view.setEnabled(enabled);
        view.setFocusable(enabled);
        view.setAlpha(enabled ? 1f : 0.34f);
    }

    private void installFocusPop(View view) {
        view.setOnFocusChangeListener((v, focused) -> {
            if (v == artwork) {
                v.setForeground(focused ? FocusChrome.outline(getContext(), 2) : null);
            } else if (v instanceof TextView) {
                v.setBackground(buttonBackground(focused));
            }
            v.animate()
                    .scaleX(focused ? TvAppCardView.FOCUSED_SCALE : 1f)
                    .scaleY(focused ? TvAppCardView.FOCUSED_SCALE : 1f)
                    .setDuration(TvAppCardView.FOCUS_DURATION_MS)
                    .start();
        });
    }

    private TextView text(int sp, int colour) {
        TextView view = new TextView(getContext());
        view.setTextColor(colour);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        return view;
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    private GradientDrawable cardBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(20, 20, 20));
        background.setCornerRadius(dp(14));
        background.setStroke(dp(1), Color.rgb(56, 56, 56));
        return background;
    }

    private GradientDrawable buttonBackground(boolean focused) {
        return FocusChrome.filled(getContext(), Color.rgb(44, 44, 44), 9, focused);
    }

    private String stateText(int state) {
        switch (state) {
            case PlaybackState.STATE_PLAYING: return "Playing";
            case PlaybackState.STATE_PAUSED: return "Paused";
            case PlaybackState.STATE_BUFFERING: return "Buffering";
            case PlaybackState.STATE_CONNECTING: return "Connecting";
            case PlaybackState.STATE_FAST_FORWARDING: return "Fast forwarding";
            case PlaybackState.STATE_REWINDING: return "Rewinding";
            case PlaybackState.STATE_SKIPPING_TO_NEXT: return "Skipping next";
            case PlaybackState.STATE_SKIPPING_TO_PREVIOUS: return "Skipping previous";
            default: return "Now Playing";
        }
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }
}
