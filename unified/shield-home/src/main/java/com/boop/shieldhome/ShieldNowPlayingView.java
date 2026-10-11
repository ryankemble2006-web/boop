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
    private static final int CONTROL_GAP_DP = 6;
    private static final int ARTWORK_GAP_DP = 20;
    private static final int CONTROL_HEIGHT_DP = 44;
    private static final int ARTWORK_CORNER_DP = 10;
    static final int MASCOT_BAY_DP = 230;


    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ImageView artwork;
    private final SerenMarqueeTextView title;
    private final TextView subtitle;
    private final TextView stateLabel;
    private final ProgressBar progress;
    private final PrecisionIcons.Control lyricsButton;
    private final PrecisionIcons.Control sourceButton;
    private final PrecisionIcons.Control queueButton;
    private Runnable unsubscribeQueue;
    private final PrecisionIcons.Control previousButton;
    private final PrecisionIcons.Control playPauseButton;
    private final PrecisionIcons.Control nextButton;
    private final DeezerFavouriteButton favouriteButton;
    private final DeezerFavouriteButton dislikeButton;
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
        setPadding(0, dp(14), 0, dp(14));
        setBackground(null);

        LinearLayout row = new CenteredPlaybackRow(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setClipChildren(false);
        addView(row, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        artwork = new FocusArtworkView(context);
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
        artParams.rightMargin = dp(ARTWORK_GAP_DP);
        row.addView(artwork, artParams);

        LinearLayout details = new OpticalStack(context);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setGravity(Gravity.TOP);
        details.setClipChildren(false);
        details.setClipToPadding(false);
        row.addView(details, new LinearLayout.LayoutParams(0, dp(154)));

        title = new SerenMarqueeTextView(context);
        title.setIncludeFontPadding(false);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
        title.setSelected(true);
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
        stateLabel.setVisibility(GONE);

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

        controls = new GroupedControls(context);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setBaselineAligned(false);
        controls.setGravity(Gravity.CENTER);
        controls.setClipChildren(false);
        controls.setClipToPadding(false);
        controls.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams controlsParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
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

        favouriteButton = new DeezerFavouriteButton(context, DeezerFavouriteButton.TOGGLE,
                FocusChrome.accentColor(context));
        favouriteButton.useStandaloneHeart();
        controls.addView(favouriteButton, new LinearLayout.LayoutParams(
                dp(CONTROL_HEIGHT_DP), dp(CONTROL_HEIGHT_DP)));
        dislikeButton = new DeezerFavouriteButton(context, DeezerFavouriteButton.DISLIKE, FocusChrome.accentColor(context));
        dislikeButton.useStandaloneHeart();
        controls.addView(dislikeButton, new LinearLayout.LayoutParams(dp(CONTROL_HEIGHT_DP), dp(CONTROL_HEIGHT_DP)));
        addControl(controls, previousButton);
        addControl(controls, playPauseButton);
        addControl(controls, nextButton);
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
        spectrumParams.leftMargin = dp(ARTWORK_GAP_DP);
        row.addView(puppetBay, spectrumParams);
        puppetView = new ShieldSpectrumView(context);
        puppetView.setLeftNavigation(sourceButton);
        puppetBay.addView(puppetView, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        setVisibility(GONE);
    }

    public void bind(NowPlayingSnapshot snapshot, ShieldHomeView.Callbacks callbacks) {
        this.callbacks = callbacks;
        this.snapshot = snapshot;
        favouriteButton.setSnapshot(snapshot);
        dislikeButton.setSnapshot(snapshot);
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
        String nextTitle = snapshot.title().isEmpty() ? "Now Playing" : snapshot.title();
        boolean titleChanged = !TextUtils.equals(title.getText(), nextTitle);
        title.showText(nextTitle);
        if (!TextUtils.equals(subtitle.getText(), snapshot.subtitle())) subtitle.setText(snapshot.subtitle());
        boolean artistAvailable = !snapshot.subtitle().isEmpty() && !snapshot.title().isEmpty();
        subtitle.setEnabled(artistAvailable);
        subtitle.setFocusable(artistAvailable);
        subtitle.setClickable(artistAvailable);
        subtitle.setContentDescription("Browse artist " + snapshot.subtitle() + " in Deezer");
        String state = stateText(snapshot.playbackState());
        if (!TextUtils.equals(stateLabel.getText(), state)) stateLabel.setText(state);
        // Different glyphs can change visible spacing without changing TextView height.
        if (titleChanged) title.getParent().requestLayout();
        PrecisionIcons.bindControl(playPauseButton, snapshot.isPlaying() ? "Pause" : "Play");
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
        row.addView(button, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    /** The shared screen centre is the text/bar right edge; spectrum consumes the remainder. */
    private final class CenteredPlaybackRow extends LinearLayout {
        CenteredPlaybackRow(Context context) { super(context); }
        @Override protected void onMeasure(int widthSpec,int heightSpec) {
            int width=MeasureSpec.getSize(widthSpec)-getPaddingLeft()-getPaddingRight();
            if(getChildCount()==3) {
                LinearLayout.LayoutParams album=(LinearLayout.LayoutParams)getChildAt(0).getLayoutParams();
                LinearLayout.LayoutParams details=(LinearLayout.LayoutParams)getChildAt(1).getLayoutParams();
                details.width=Math.max(0,width/2-album.width-album.rightMargin);
                LinearLayout.LayoutParams spectrum=(LinearLayout.LayoutParams)getChildAt(2).getLayoutParams();
                Rect artistInk=new Rect();String artist=subtitle.getText().toString();
                if(!artist.isEmpty())subtitle.getPaint().getTextBounds(artist,0,artist.length(),artistInk);
                spectrum.leftMargin=album.rightMargin+subtitle.getCompoundPaddingLeft()+artistInk.left;
            }
            // Home transport ink matches the artist's capital C. Touch targets stay generous.
            Rect capital = new Rect();
            subtitle.getPaint().getTextBounds("C", 0, 1, capital);
            for (int i = 0; i < controls.getChildCount(); i++) {
                View button = controls.getChildAt(i);
                if (button instanceof PrecisionIcons.Control)
                    ((PrecisionIcons.Control)button).setVisibleHeight(capital.height());
                else if (button instanceof DeezerFavouriteButton)
                    ((DeezerFavouriteButton)button).setVisibleHeight(capital.height());
            }
            super.onMeasure(widthSpec,heightSpec);
        }
    }

    /** Fixed transport centre with independent edge-aligned action groups. */
    private final class GroupedControls extends LinearLayout {
        GroupedControls(Context context) { super(context); }
        @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed,l,t,r,b);
            if(favouriteButton.getVisibility()!=GONE) positionGroup(-buttonInk(favouriteButton).left, favouriteButton, dislikeButton);
            int width = groupWidth(previousButton, playPauseButton, nextButton);
            positionGroup((getWidth()-width)/2, previousButton, playPauseButton, nextButton);
            width = groupWidth(lyricsButton, queueButton, sourceButton);
            positionGroup(getWidth()-width + sourceButton.getMeasuredWidth()-buttonInk(sourceButton).right, lyricsButton, queueButton, sourceButton);
        }
        private int groupWidth(View... buttons) {
            int width=0,count=0;
            for(View button:buttons) if(button.getVisibility()!=GONE) { width+=button.getMeasuredWidth();count++; }
            return width + Math.max(0,count-1)*dp(CONTROL_GAP_DP);
        }
        Rect inkBounds() {
            int visibleHeight=0;
            for(int i=0;i<getChildCount();i++) { View button=getChildAt(i);
                if(button.getVisibility()!=GONE) visibleHeight=Math.max(visibleHeight,buttonInk(button).height());
            }
            int top=(getMeasuredHeight()-visibleHeight)/2;
            return new Rect(0,top,getMeasuredWidth(),top+visibleHeight);
        }
        private Rect buttonInk(View button) {
            return button instanceof PrecisionIcons.Control ? ((PrecisionIcons.Control)button).inkBounds()
                    : ((DeezerFavouriteButton)button).inkBounds();
        }
        private void positionGroup(int left, View... buttons) {
            int bottom=inkBounds().bottom;
            for(View button:buttons) if(button.getVisibility()!=GONE) {
                int top=bottom-buttonInk(button).bottom;
                button.layout(left,top,left+button.getMeasuredWidth(),top+button.getMeasuredHeight());
                left+=button.getMeasuredWidth()+dp(CONTROL_GAP_DP);
            }
        }
    }

    private void updateControlSpacing() {
        boolean first = true;
        for (int i = 0; i < controls.getChildCount(); i++) {
            View button = controls.getChildAt(i);
            LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) button.getLayoutParams();
            // A visible queue adds an eighth control; narrow targets without shrinking the glyphs.
            int targetWidth=dp(queueButton.getVisibility()==VISIBLE ? 38 : CONTROL_HEIGHT_DP);
            boolean widthChanged=p.width!=targetWidth;
            p.width=targetWidth;
            int gap = first || button.getVisibility() == GONE ? 0 : dp(CONTROL_GAP_DP);
            if (widthChanged || p.leftMargin != gap) { p.leftMargin = gap; button.setLayoutParams(p); }
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
            if (favouriteButton.getVisibility() == VISIBLE && favouriteButton.isFocusable()) favouriteButton.requestFocus();
            else playPauseButton.requestFocus();
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
                && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                && (favouriteButton.getVisibility() == VISIBLE ? favouriteButton : previousButton).requestFocus());
        View.OnKeyListener upToProgress = (v, keyCode, event) -> {
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_DPAD_UP && progress.isFocusable()) {
                progress.requestFocus();
                return true;
            }
            return false;
        };
        previousButton.setOnKeyListener((v, keyCode, event) -> {
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_DPAD_LEFT && favouriteButton.getVisibility() == VISIBLE)
                return dislikeButton.requestFocus();
            return upToProgress.onKey(v, keyCode, event);
        });
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
                (lyricsButton.getVisibility() == VISIBLE ? lyricsButton
                        : queueButton.getVisibility() == VISIBLE ? queueButton : sourceButton).requestFocus();
                return true;
            }
            return false;
        });
        favouriteButton.setOnKeyListener((v, keyCode, event) -> {
            if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) return artwork.requestFocus();
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) return dislikeButton.requestFocus();
            return upToProgress.onKey(v, keyCode, event);
        });
        dislikeButton.setOnKeyListener((v, keyCode, event) -> {
            if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) return favouriteButton.requestFocus();
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) return previousButton.requestFocus();
            return upToProgress.onKey(v, keyCode, event);
        });
        lyricsButton.setOnKeyListener((v, keyCode, event) -> {
            if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                return nextButton.requestFocus();
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
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                return puppetView.focusControl();
            }
            if (event != null
                    && event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                (queueButton.getVisibility() == VISIBLE ? queueButton
                        : lyricsButton.getVisibility() == VISIBLE ? lyricsButton : nextButton).requestFocus();
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

    private PrecisionIcons.Control controlButton(String label, Runnable action) {
        PrecisionIcons.Control button = actionButton(label);
        button.setOnClickListener(v -> {
            if (v.isEnabled()) action.run();
        });
        return button;
    }

    private PrecisionIcons.Control actionButton(String label) {
        PrecisionIcons.Control view = new PrecisionIcons.Control(getContext());

        PrecisionIcons.bindControl(view, label);


        view.setMinimumWidth(dp(44));
        view.setMinimumHeight(dp(CONTROL_HEIGHT_DP));

        view.setFocusable(true);
        view.setClickable(true);
        view.setPadding(dp(8), dp(2), dp(8), dp(2));

        view.setBackground(null);
        installFocusPop(view);
        return view;
    }

    private void setControlEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        view.setFocusable(enabled);
        view.setAlpha(enabled ? 1f : 0.34f);
    }

    private void installFocusPop(View view) {
        view.setOnFocusChangeListener((v, focused) -> {
            if (v == artwork) {
                v.setForeground(focused ? FocusChrome.artworkOutline(getContext(), ARTWORK_CORNER_DP) : null);
            } else if (v instanceof PrecisionIcons.Control) {
                v.setBackground(null);
                v.invalidate();
            }
            v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
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
            if (child instanceof GroupedControls) return ((GroupedControls)child).inkBounds();
            if (child instanceof TextView) {
                TextView label = (TextView) child;
                String value = label.getText().toString();
                if (value.isEmpty()) value = "Ag";
                label.getPaint().getTextBounds(value, 0, value.length(), bounds);
                if (child instanceof SerenMarqueeTextView) {
                    Rect capital = new Rect();
                    label.getPaint().getTextBounds("H", 0, 1, capital);
                    bounds.top = capital.top; // Capital top anchors to art; retain actual descenders below.
                }
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
            int inset = 4;
            float gap = count > 1 ? Math.max(0, (getHeight() - inset * 2 - used) / (float) (count - 1)) : 0;
            float cursor = inset;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == GONE) continue;
                Rect bounds = ink(child);
                float nudge = (child instanceof ProgressBar) ? -4f : ((child instanceof TextView) && !(child instanceof SerenMarqueeTextView)) ? -8f : 0f;
                int y = Math.round(cursor + nudge - bounds.top);
                // MATCH_PARENT marquee text can measure wider than its column.
                // Its actual viewport must share the progress and control right edge.
                child.layout(0, y, getWidth(), y + child.getMeasuredHeight());
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
        return focused ? FocusChrome.outline(getContext(), 9) : null;
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
