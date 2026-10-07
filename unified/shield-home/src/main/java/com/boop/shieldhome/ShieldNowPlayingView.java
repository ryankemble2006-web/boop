package com.boop.shieldhome;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/** Remote-first Now Playing card. It owns only media UI and never rerenders launcher rows. */
public final class ShieldNowPlayingView extends FrameLayout {
    private static final long PROGRESS_TICK_MS = 500L;
    private static final int CONTROL_GAP_DP = 8;
    private static final int CONTROL_HEIGHT_DP = 32;
    private static final int ARTWORK_CORNER_DP = 10;
    static final int MASCOT_BAY_DP = 230;
    // Centre of the third favourites banner, measured from this card's left edge.
    private static final int PLAYBACK_END_DP = 2 * (TvAppCardView.HOME_ARTWORK_WIDTH_DP + 16)
            + TvAppCardView.HOME_ARTWORK_WIDTH_DP / 2;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ImageView artwork;
    private final TextView title;
    private final TextView subtitle;
    private final TextView stateLabel;
    private final ProgressBar progress;
    private final TextView lyricsButton;
    private final TextView sourceButton;
    private final TextView queueButton;
    private Runnable unsubscribeQueue;
    private final TextView previousButton;
    private final TextView playPauseButton;
    private final TextView nextButton;
    private final DeezerFavouriteButton favouriteButton;
    private final LinearLayout controls;
    private final ShieldSpectrumView puppetView;

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
        artwork.setBackground(artworkBackground());
        FocusChrome.clipRounded(artwork, ARTWORK_CORNER_DP);
        artwork.setContentDescription("Open source player");
        artwork.setFocusable(true);
        artwork.setClickable(true);
        artwork.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onBrowseNowPlayingAlbum();
        });
        installFocusPop(artwork);
        LinearLayout.LayoutParams artParams = new LinearLayout.LayoutParams(dp(154), dp(154));
        artParams.rightMargin = dp(20);
        row.addView(artwork, artParams);

        LinearLayout details = new OpticalStack(context);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setGravity(Gravity.TOP);
        details.setClipChildren(false);
        details.setClipToPadding(false);
        row.addView(details, new LinearLayout.LayoutParams(dp(PLAYBACK_END_DP - 18 - 154 - 20), dp(154)));

        title = text(26, Color.WHITE);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        details.addView(title, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        subtitle = text(18, Color.WHITE);
        subtitle.setSingleLine(true);
        subtitle.setEllipsize(TextUtils.TruncateAt.END);
        subtitle.setFocusable(true);
        subtitle.setClickable(true);
        BoopTvChrome.useTextOnlyFocus(subtitle);
        subtitle.setOnKeyListener(this::handleArtistKey);
        subtitle.setOnClickListener(v -> {
            if (callbacks != null && v.isEnabled()) callbacks.onBrowseNowPlayingArtist();
        });
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        details.addView(subtitle, subtitleParams);

        stateLabel = text(14, Color.LTGRAY);
        stateLabel.setSingleLine(true);
        stateLabel.setEllipsize(TextUtils.TruncateAt.END);
        details.addView(stateLabel, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        lyricsButton = actionButton("Lyrics");
        lyricsButton.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onOpenNowPlayingLyrics();
        });

        queueButton = actionButton("Queue");
        queueButton.setContentDescription("Browse the current album or playlist queue");
        queueButton.setOnClickListener(v -> { if (callbacks != null) callbacks.onNowPlayingQueue(); });
        queueButton.setVisibility(GONE);

        sourceButton = actionButton("Flow");
        sourceButton.setContentDescription("Start your Deezer Flow");
        sourceButton.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onNowPlayingFlow();
        });

        progress = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(1000);
        progress.setProgressTintList(ColorStateList.valueOf(FocusChrome.accentColor(context)));
        progress.setFocusable(true);
        progress.setClickable(false);
        progress.setContentDescription("Track position");
        progress.setOnFocusChangeListener((v, focused) -> v.animate()
                .scaleY(focused ? 1.8f : 1f)
                .setDuration(TvAppCardView.FOCUS_DURATION_MS).start());
        progress.setOnKeyListener(this::handleProgressKey);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, dp(4));
        details.addView(progress, progressParams);

        controls = new LinearLayout(context);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setBaselineAligned(false);
        controls.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        controls.setClipChildren(false);
        controls.setClipToPadding(false);
        controls.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams controlsParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, dp(CONTROL_HEIGHT_DP));
        details.addView(controls, controlsParams);

        previousButton = controlButton("Prev", () -> {
            if (callbacks != null) callbacks.onNowPlayingPrevious();
        });
        playPauseButton = controlButton("Play", () -> {
            if (callbacks != null) callbacks.onNowPlayingPlayPause();
        });
        nextButton = controlButton("Next", () -> {
            if (callbacks != null) callbacks.onNowPlayingNext();
        });

        addControl(controls, previousButton);
        addControl(controls, playPauseButton);
        addControl(controls, nextButton);
        favouriteButton = new DeezerFavouriteButton(context, DeezerFavouriteButton.TOGGLE,
                FocusChrome.accentColor(context));
        favouriteButton.useButtonChrome();
        installFocusPop(favouriteButton);
        addControl(controls, favouriteButton);
        addControl(controls, lyricsButton);
        addControl(controls, queueButton);
        addControl(controls, sourceButton);
        updateControlSpacing();
        installEdgeFocusNavigation();

        // Keep the entire remaining width for the PCM spectrum, even during silence.
        LinearLayout puppetBay = new LinearLayout(context);
        puppetBay.setFocusable(false);
        puppetBay.setClickable(false);
        LinearLayout.LayoutParams spectrumParams = new LinearLayout.LayoutParams(0, dp(154), 1f);
        spectrumParams.leftMargin = dp(20);
        row.addView(puppetBay, spectrumParams);
        puppetView = new ShieldSpectrumView(context);
        puppetBay.addView(puppetView, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        setVisibility(GONE);
    }

    public void bind(NowPlayingSnapshot snapshot, ShieldHomeView.Callbacks callbacks) {
        this.callbacks = callbacks;
        this.snapshot = snapshot;
        favouriteButton.setSnapshot(snapshot);
        updateQueueButton(ShieldNowPlayingManager.get(getContext()).queue().current());
        artwork.setContentDescription(snapshot != null && "deezer.android.app".equals(snapshot.packageName())
                ? "Browse album in Deezer" : "Open source player");
        stopTicker();
        puppetView.setSnapshot(snapshot);

        if (snapshot == null || !NowPlayingSelectionPolicy.eligible(snapshot.playbackState())) {
            setVisibility(GONE);
            artwork.setImageDrawable(null);
            return;
        }

        setVisibility(VISIBLE);
        artwork.setImageBitmap(snapshot.artwork());
        title.setText(snapshot.title().isEmpty() ? "Now Playing" : snapshot.title());
        subtitle.setText(snapshot.subtitle());
        boolean artistAvailable = !snapshot.subtitle().isEmpty() && !snapshot.title().isEmpty();
        subtitle.setEnabled(artistAvailable);
        subtitle.setFocusable(artistAvailable);
        subtitle.setClickable(artistAvailable);
        subtitle.setContentDescription("Browse artist " + snapshot.subtitle() + " in Deezer");
        stateLabel.setText(stateText(snapshot.playbackState()));
        // Different glyphs can change visible spacing without changing TextView height.
        title.getParent().requestLayout();
        playPauseButton.setText(snapshot.isPlaying() ? "Pause" : "Play");
        boolean deezerLyrics = DeezerLyricsPolicy.available(snapshot.packageName());
        lyricsButton.setVisibility(deezerLyrics ? VISIBLE : GONE);
        setControlEnabled(lyricsButton, deezerLyrics);
        updateControlSpacing();

        setControlEnabled(previousButton, snapshot.canPrevious());
        setControlEnabled(playPauseButton, snapshot.canPlayPause());
        setControlEnabled(nextButton, snapshot.canNext());

        updateProgress(snapshot, SystemClock.elapsedRealtime());
        if (snapshot.isPlaying()) {
            startTicker();
        }
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        subscribeQueue();
    }

    private void subscribeQueue() {
        if (unsubscribeQueue == null && isAttachedToWindow())
            unsubscribeQueue = ShieldNowPlayingManager.get(getContext()).queue().subscribe(this::updateQueueButton);
    }

    private void updateQueueButton(DeezerQueueController.State queue) {
        boolean show = queue.visible && snapshot != null && snapshot.sessionId() == queue.session;
        if (!show && queueButton.hasFocus()) sourceButton.requestFocus();
        queueButton.setVisibility(show ? VISIBLE : GONE);
        queueButton.setFocusable(show);
        queueButton.setEnabled(show);
        updateControlSpacing();
    }

    @Override protected void onDetachedFromWindow() {
        if (unsubscribeQueue != null) unsubscribeQueue.run();
        unsubscribeQueue = null;
        stopTicker();
        super.onDetachedFromWindow();
    }

    private void updateProgress(NowPlayingSnapshot current, long nowElapsedRealtimeMs) {
        long duration = current.durationMs();
        if (duration <= 0L) {
            progress.setProgress(0);
            progress.setVisibility(INVISIBLE);
            progress.setFocusable(false);
            return;
        }
        progress.setVisibility(VISIBLE);
        progress.setEnabled(current.canSeek());
        progress.setFocusable(current.canSeek());
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

    private void addControl(LinearLayout row, View button) {
        row.addView(button, new LinearLayout.LayoutParams(0, dp(CONTROL_HEIGHT_DP), 1f));
    }

    private void updateControlSpacing() {
        boolean first = true;
        for (int i = 0; i < controls.getChildCount(); i++) {
            View button = controls.getChildAt(i);
            LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) button.getLayoutParams();
            int gap = first || button.getVisibility() == GONE ? 0 : dp(CONTROL_GAP_DP);
            if (p.leftMargin != gap) { p.leftMargin = gap; button.setLayoutParams(p); }
            if (button.getVisibility() != GONE) first = false;
        }
    }

    private boolean handleProgressKey(View v, int keyCode, KeyEvent event) {
        if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) return false;
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (callbacks != null) callbacks.onNowPlayingSeekBy(-10_000L);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (callbacks != null) callbacks.onNowPlayingSeekBy(10_000L);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP && subtitle.isFocusable()) {
            return subtitle.requestFocus();
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            playPauseButton.requestFocus();
            return true;
        }
        return false;
    }

    private boolean handleArtistKey(View v, int keyCode, KeyEvent event) {
        if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) return false;
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) return artwork.requestFocus();
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            return (lyricsButton.getVisibility() == VISIBLE ? lyricsButton : sourceButton).requestFocus();
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (progress.isFocusable()) return progress.requestFocus();
            return (playPauseButton.isFocusable() ? playPauseButton : artwork).requestFocus();
        }
        return false;
    }

    private void installEdgeFocusNavigation() {
        artwork.setOnKeyListener((v, keyCode, event) -> event != null
                && event.getAction() == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && subtitle.isFocusable()
                && subtitle.requestFocus());
        View.OnKeyListener upToProgress = (v, keyCode, event) -> {
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_DPAD_UP && progress.isFocusable()) {
                progress.requestFocus();
                return true;
            }
            return false;
        };
        previousButton.setOnKeyListener(upToProgress);
        playPauseButton.setOnKeyListener(upToProgress);
        nextButton.setOnKeyListener((v, keyCode, event) -> {
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_DPAD_UP && progress.isFocusable()) {
                progress.requestFocus();
                return true;
            }
            if (event != null
                    && event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                (favouriteButton.getVisibility() == VISIBLE ? favouriteButton
                        : lyricsButton.getVisibility() == VISIBLE ? lyricsButton : sourceButton).requestFocus();
                return true;
            }
            return false;
        });
        favouriteButton.setOnKeyListener((v, keyCode, event) -> {
            if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) { nextButton.requestFocus(); return true; }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                (lyricsButton.getVisibility() == VISIBLE ? lyricsButton : sourceButton).requestFocus();
                return true;
            }
            return upToProgress.onKey(v, keyCode, event);
        });
        lyricsButton.setOnKeyListener((v, keyCode, event) -> {
            if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                (favouriteButton.getVisibility() == VISIBLE ? favouriteButton : nextButton).requestFocus();
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                (queueButton.getVisibility() == VISIBLE ? queueButton : sourceButton).requestFocus(); return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP && progress.isFocusable()) { progress.requestFocus(); return true; }
            return false;
        });
        queueButton.setOnKeyListener((v, keyCode, event) -> {
            if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) { lyricsButton.requestFocus(); return true; }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) { sourceButton.requestFocus(); return true; }
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP && progress.isFocusable()) { progress.requestFocus(); return true; }
            return false;
        });
        sourceButton.setOnKeyListener((v, keyCode, event) -> {
            if (event != null
                    && event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                (queueButton.getVisibility() == VISIBLE ? queueButton
                        : lyricsButton.getVisibility() == VISIBLE ? lyricsButton
                        : favouriteButton.getVisibility() == VISIBLE ? favouriteButton : nextButton).requestFocus();
                return true;
            }
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_DPAD_UP && progress.isFocusable()) {
                progress.requestFocus();
                return true;
            }
            return false;
        });
    }

    private TextView controlButton(String label, Runnable action) {
        TextView button = actionButton(label);
        button.setOnClickListener(v -> {
            if (v.isEnabled()) action.run();
        });
        return button;
    }

    private TextView actionButton(String label) {
        TextView view = text(14, Color.WHITE);
        view.setText(label);
        view.setSingleLine(true);
        view.setEllipsize(TextUtils.TruncateAt.END);
        view.setGravity(Gravity.CENTER);
        view.setFocusable(true);
        view.setClickable(true);
        view.setPadding(dp(4), 0, dp(4), 0);
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
                v.setForeground(focused ? FocusChrome.artworkOutline(getContext(), ARTWORK_CORNER_DP) : null);
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
        view.setIncludeFontPadding(false);
        view.setTextColor(colour);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        return view;
    }

    /** Distribute the visible text and control edges, excluding invisible font leading. */
    private static final class OpticalStack extends LinearLayout {
        OpticalStack(Context context) { super(context); }

        private Rect ink(View child) {
            Rect bounds = new Rect(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
            if (child instanceof TextView) {
                TextView label = (TextView) child;
                String value = label.getText().toString();
                if (value.isEmpty()) value = "Ag";
                label.getPaint().getTextBounds(value, 0, value.length(), bounds);
                bounds.offset(0, label.getBaseline());
            }
            return bounds;
        }

        @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            int used = 0, count = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child.getVisibility() != GONE) { used += ink(child).height(); count++; }
            }
            float gap = count > 1 ? Math.max(0, (getHeight() - used) / (float) (count - 1)) : 0;
            float cursor = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == GONE) continue;
                Rect bounds = ink(child);
                int y = Math.round(cursor - bounds.top);
                child.layout(0, y, child.getMeasuredWidth(), y + child.getMeasuredHeight());
                cursor += bounds.height() + gap;
            }
        }
    }

    private GradientDrawable artworkBackground() {
        return FocusChrome.filled(getContext(), Color.rgb(28, 28, 28), ARTWORK_CORNER_DP, false);
    }

    private GradientDrawable cardBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(16, 16, 16));
        background.setCornerRadius(dp(14));
        background.setStroke(dp(1), Color.rgb(48, 48, 48));
        return background;
    }

    private GradientDrawable buttonBackground(boolean focused) {
        return FocusChrome.filled(getContext(), Color.rgb(34, 34, 34), 9, focused);
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
