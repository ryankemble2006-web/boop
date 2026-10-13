package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Locale;

/** Borderless TV music composition. No dialog chrome, external player or audio-focus ownership. */
public final class ShieldLyricsView extends FrameLayout {
    public interface Controls {
        void previous(); void playPause(); void next(); void seek(long milliseconds); void close(); void browseAlbum(); void browseArtist();
        default void queue() { }
        default void lookup() { }
        default void flow() { }
    }
    private final int accent;
    private final Controls controls;
    private final ImageView artwork;
    private final SerenMarqueeTextView title;
    private final TextView artist, status, elapsed, duration;
    private final PrecisionIcons.Control queueButton;
    private final PrecisionIcons.Control lookupButton;
    private final PrecisionIcons.Control flowButton;
    private final LyricsLinesView lyrics;
    private final ShieldSpectrumView spectrum;
    private final PrecisionIcons.Control lyricsModeButton, spectrumModeButton;
    private final LyricsSpectrumMode spectrumMode = new LyricsSpectrumMode();
    private final PrecisionIcons.Control[] buttons = new PrecisionIcons.Control[3];
    private final PositionBar progress;
    private final DeezerFavouriteButton removeHeart, addHeart;
    private NowPlayingSnapshot snapshot;
    private boolean clockKnown;
    private boolean running;
    private boolean posted;
    private boolean snapClock = true;
    private float unit = 1f;
    private int geometryWidth = -1, geometryHeight = -1;
    private long displayedSecond = Long.MIN_VALUE;
    private final Runnable frame = new Runnable() {
        @Override public void run() {
            posted = false;
            if (!running || !isAttachedToWindow() || getWindowVisibility() != VISIBLE) return;
            long position = snapshot != null && clockKnown
                    ? snapshot.estimatedPositionMs(SystemClock.elapsedRealtime()) : -1;
            lyrics.setPosition(position, snapClock);
            snapClock = false;
            progress.fraction = snapshot != null && snapshot.durationMs() > 0 && position >= 0
                    ? Math.min(1f, position / (float) snapshot.durationMs()) : 0f;
            progress.invalidate();
            long second = position < 0 ? -1 : position / 1000;
            if (second != displayedSecond) { elapsed.setText(time(position)); displayedSecond = second; }
            posted = true;
            postOnAnimation(this);
        }
    };
    public ShieldLyricsView(Context context, int accent, Controls controls) {
        super(context);
        this.accent = accent;
        this.controls = controls;
        setClipChildren(true);
        setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(11, 18, 22), Color.rgb(3, 5, 7), Color.BLACK}));
        artwork = new ImageView(context) {
            private final Paint focusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            @Override protected void onFocusChanged(boolean gain, int direction, android.graphics.Rect previous) {
                super.onFocusChanged(gain, direction, previous);
                invalidate();
            }
            private final Path artworkClip = new Path();
            private final RectF artworkBounds = new RectF();
            @Override protected void onDraw(Canvas canvas) {
                float stroke = 3f * unit + 2f;
                float inset = hasFocus() ? stroke : 0f;
                artworkBounds.set(inset, inset, getWidth() - inset, getHeight() - inset);
                artworkClip.reset();
                float radius = Math.max(0f, 9f * unit - inset);
                artworkClip.addRoundRect(artworkBounds, radius, radius, Path.Direction.CW);
                int clipped = canvas.save();
                canvas.clipPath(artworkClip);
                super.onDraw(canvas);
                canvas.restoreToCount(clipped);
                if (hasFocus()) {
                    FocusChrome.drawArtworkOutline(canvas, new RectF(0, 0, getWidth(), getHeight()),
                            9f * unit, stroke, accent);
                }
            }
        };
        artwork.setOnClickListener(v -> { if (v.isEnabled()) controls.browseAlbum(); });
        artwork.setScaleType(ImageView.ScaleType.CENTER_CROP);
        artwork.setBackground(FocusChrome.filled(context, Color.rgb(16, 24, 29), 9, false));
        artwork.setClipToOutline(false);
        addView(artwork);
        title = new SerenMarqueeTextView(context);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        title.setTextColor(Color.WHITE);
        title.setIncludeFontPadding(false);
        title.setSelected(true);
        title.setRepeatScroll(false);
        addView(title);
        artist = label("", 18, Color.WHITE, false);
        title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        artist.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        artist.setSingleLine(true);
        artist.setEllipsize(TextUtils.TruncateAt.END);
        artist.setFocusable(true);
        artist.setClickable(true);
        BoopTvChrome.useTextOnlyFocus(artist);
        artist.setOnClickListener(v -> {
            if (v.isEnabled()) controls.browseArtist();
        });
        lyrics = new LyricsLinesView(context, accent);
        addView(lyrics);
        spectrum = new ShieldSpectrumView(context);
        spectrum.setVisibility(GONE);
        addView(spectrum);
        lyricsModeButton = new PrecisionIcons.Control(context);
        spectrumModeButton = new PrecisionIcons.Control(context);
        flowButton = new PrecisionIcons.Control(context);
        PrecisionIcons.bindControl(flowButton, "Flow");
        flowButton.setContentDescription("Start Deezer Flow");
        flowButton.setOnClickListener(v -> { if (v.isEnabled()) controls.flow(); });
        PrecisionIcons.bindControl(lyricsModeButton, "Lyrics");
        PrecisionIcons.bindControl(spectrumModeButton, "Spectrum");
        for (PrecisionIcons.Control button : new PrecisionIcons.Control[]{lyricsModeButton, spectrumModeButton, flowButton}) {
            button.setFocusable(true); button.setClickable(true); addView(button);
        }
        lyricsModeButton.setOnClickListener(v -> { spectrumMode.chooseSpectrum(false);updateSpectrumMode(); });
        spectrumModeButton.setOnClickListener(v -> { spectrumMode.chooseSpectrum(true);updateSpectrumMode(); });
        status = label("", 24, Color.rgb(155, 174, 184), false);
        status.setGravity(Gravity.CENTER_VERTICAL);
        status.setMaxLines(3);
        elapsed = label("0:00", 12, Color.rgb(161, 177, 187), false);
        duration = label("", 12, Color.rgb(161, 177, 187), false);
        duration.setGravity(Gravity.END);
        progress = new PositionBar(context);
        artist.setOnKeyListener((v, key, event) -> {
            if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (key == KeyEvent.KEYCODE_DPAD_UP)
                return artwork.isFocusable() && artwork.requestFocus();
            if (key == KeyEvent.KEYCODE_DPAD_DOWN)
                return progress.isFocusable() ? progress.requestFocus() : focusTransport();
            return false;
        });
        progress.setFocusable(true);
        progress.setContentDescription("Track position. Left and right seek ten seconds.");
        progress.setOnKeyListener((v, key, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (key == KeyEvent.KEYCODE_DPAD_LEFT) { controls.seek(-10000L); return true; }
            if (key == KeyEvent.KEYCODE_DPAD_RIGHT) { controls.seek(10000L); return true; }
            if (key == KeyEvent.KEYCODE_DPAD_UP)
                return artist.isFocusable() ? artist.requestFocus()
                        : artwork.isFocusable() && artwork.requestFocus();
            if (key == KeyEvent.KEYCODE_DPAD_DOWN) return focusTransport();
            return false;
        });
        addView(progress);
        artwork.setOnKeyListener((v, key, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN || key != KeyEvent.KEYCODE_DPAD_DOWN) return false;
            return artist.isFocusable() ? artist.requestFocus()
                    : progress.isFocusable() ? progress.requestFocus() : focusTransport();
        });

        removeHeart = new DeezerFavouriteButton(context, DeezerFavouriteButton.DISLIKE, accent);
        addHeart = new DeezerFavouriteButton(context, DeezerFavouriteButton.TOGGLE, accent);
        removeHeart.useStandaloneHeart();
        addHeart.useStandaloneHeart();
        removeHeart.setOnKeyListener((v, key, event) -> handleFavouriteRowKey(0, key, event));
        addHeart.setOnKeyListener((v, key, event) -> handleFavouriteRowKey(4, key, event));
        addView(removeHeart);
        addView(addHeart);

        String[] descriptions = {"Previous track", "Pause", "Next track"};
        for (int i = 0; i < buttons.length; i++) {
            final int kind = i;
            PrecisionIcons.Control button = new PrecisionIcons.Control(context);
            PrecisionIcons.bindControl(button,new String[]{"Prev","Pause","Next"}[i]);
            buttons[i] = button;
            button.setFocusable(true);
            button.setClickable(true);
            button.setContentDescription(descriptions[i]);
            button.setOnClickListener(v -> {
                if (!v.isEnabled()) return;
                switch (kind) {
                    case 0: controls.previous(); break;
                    case 1: controls.playPause(); break;
                    case 2: controls.next(); break;
                    default: break;
                }
            });
            button.setOnKeyListener((v, key, event) -> handleFavouriteRowKey(kind + 1, key, event));
            addView(button);
        }
        lookupButton = new PrecisionIcons.Control(context);
        queueButton = new PrecisionIcons.Control(context);
        PrecisionIcons.bindControl(lookupButton,"Lookup");
        PrecisionIcons.bindControl(queueButton,"Queue");
        addView(lookupButton);
        addView(queueButton);
        queueButton.setContentDescription("Browse the current album or playlist queue");

        queueButton.setOnClickListener(v -> { if (v.isEnabled()) controls.queue(); });
        queueButton.setOnKeyListener((v, key, event) -> {
            if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (key == KeyEvent.KEYCODE_DPAD_UP) return focusTransport();
            if (key == KeyEvent.KEYCODE_DPAD_RIGHT && lookupButton.isShown()) return lookupButton.requestFocus();
            return key == KeyEvent.KEYCODE_DPAD_DOWN || key == KeyEvent.KEYCODE_DPAD_LEFT
                    || key == KeyEvent.KEYCODE_DPAD_RIGHT;
        });

        lookupButton.setContentDescription("Edit title and artist to look up lyrics");

        lookupButton.setOnClickListener(v -> { if (v.isEnabled()) controls.lookup(); });
        lookupButton.setOnKeyListener((v, key, event) -> {
            if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (key == KeyEvent.KEYCODE_DPAD_UP) return buttons[0].requestFocus();
            if (key == KeyEvent.KEYCODE_DPAD_LEFT)
                return queueButton.isShown() ? queueButton.requestFocus() : focusTransport();
            if (key == KeyEvent.KEYCODE_DPAD_RIGHT) return lyricsModeButton.requestFocus();
            return key == KeyEvent.KEYCODE_DPAD_DOWN;
        });
        spectrum.setLeftNavigation(spectrumModeButton);
        lyricsModeButton.setOnKeyListener((v,key,event)-> {
            if(event.getAction()!=KeyEvent.ACTION_DOWN)return false;
            if(key==KeyEvent.KEYCODE_DPAD_LEFT || key==KeyEvent.KEYCODE_DPAD_DOWN)
                return lookupButton.isShown() ? lookupButton.requestFocus() : focusTransport();
            if(key==KeyEvent.KEYCODE_DPAD_RIGHT)return spectrumModeButton.requestFocus();
            if(key==KeyEvent.KEYCODE_DPAD_UP)return buttons[1].requestFocus();
            return false;
        });
        spectrumModeButton.setOnKeyListener((v,key,event)-> {
            if(event.getAction()!=KeyEvent.ACTION_DOWN)return false;
            if(key==KeyEvent.KEYCODE_DPAD_LEFT)return lyricsModeButton.requestFocus();
            if(key==KeyEvent.KEYCODE_DPAD_DOWN)return lookupButton.isShown() ? lookupButton.requestFocus() : focusTransport();
            if(key==KeyEvent.KEYCODE_DPAD_UP)return buttons[2].requestFocus();
            if(key==KeyEvent.KEYCODE_DPAD_RIGHT && flowButton.isFocusable())return flowButton.requestFocus();
            if(key==KeyEvent.KEYCODE_DPAD_RIGHT && spectrum.isShown())return spectrum.focusControl();
            return key==KeyEvent.KEYCODE_DPAD_RIGHT;
        });
        flowButton.setOnKeyListener((v,key,event)-> {
            if(event.getAction()!=KeyEvent.ACTION_DOWN)return false;
            if(key==KeyEvent.KEYCODE_DPAD_LEFT)return spectrumModeButton.requestFocus();
            if(key==KeyEvent.KEYCODE_DPAD_UP)return buttons[2].isFocusable()
                    ? buttons[2].requestFocus() : focusTransport();
            if(key==KeyEvent.KEYCODE_DPAD_RIGHT && spectrum.isShown())return spectrum.focusControl();
            return key==KeyEvent.KEYCODE_DPAD_RIGHT || key==KeyEvent.KEYCODE_DPAD_DOWN;
        });
        setQueueAvailable(false);
        post(() -> { if (buttons[1].isFocusable()) buttons[1].requestFocus(); });
    }
    public void setSnapshot(NowPlayingSnapshot next, boolean knownClock) {
        long now = SystemClock.elapsedRealtime();
        if (snapshot == null || next == null || snapshot.sessionId() != next.sessionId()
                || !snapshot.trackKey().equals(next.trackKey())
                || Math.abs(snapshot.estimatedPositionMs(now) - next.estimatedPositionMs(now)) > 1500L) snapClock = true;
        if(snapshot == null || next == null || snapshot.sessionId()!=next.sessionId()
                || !snapshot.trackKey().equals(next.trackKey())) spectrumMode.newTrack();
        String nextTitle = next == null || next.title().isEmpty() ? "Now Playing" : next.title();
        boolean titleChanged = !TextUtils.equals(title.getText(), nextTitle);
        snapshot = next;
        spectrum.setSnapshot(next);
        boolean canFlow = next != null && "deezer.android.app".equals(next.packageName())
                && NowPlayingSelectionPolicy.eligible(next.playbackState());
        if (!canFlow && flowButton.hasFocus()) spectrumModeButton.requestFocus();
        flowButton.setEnabled(canFlow);
        flowButton.setFocusable(canFlow);
        flowButton.setClickable(canFlow);
        flowButton.setAlpha(canFlow ? 1f : .35f);
        updateSpectrumMode();
        boolean canLookup = next != null && DeezerLyricsPolicy.available(next.packageName());
        if (!canLookup && lookupButton.hasFocus()) focusTransport();
        lookupButton.setVisibility(canLookup ? VISIBLE : GONE);
        lookupButton.setFocusable(canLookup);
        lookupButton.setEnabled(canLookup);
        lookupButton.setClickable(canLookup);
        removeHeart.setSnapshot(next);
        addHeart.setSnapshot(next);
        clockKnown = knownClock;
        artwork.setImageBitmap(next == null ? null : next.artwork());
        boolean albumAvailable = next != null && NowPlayingSelectionPolicy.eligible(next.playbackState());
        artwork.setEnabled(albumAvailable);
        artwork.setFocusable(albumAvailable);
        artwork.setClickable(albumAvailable);
        artwork.setContentDescription(next != null && "deezer.android.app".equals(next.packageName())
                ? "Browse album in Deezer" : "Open source player");
        title.showText(nextTitle);
        title.setContentDescription(nextTitle);
        if (titleChanged) title.getParent().requestLayout();
        setLabelIfChanged(artist, next == null ? "" : next.subtitle());
        boolean artistAvailable = next != null && !next.subtitle().isEmpty() && !next.title().isEmpty();
        artist.setEnabled(artistAvailable);
        artist.setFocusable(artistAvailable);
        artist.setClickable(artistAvailable);
        artist.setContentDescription(artistAvailable ? "Browse artist " + next.subtitle() + " in Deezer" : "Artist unavailable");
        setLabelIfChanged(duration, next == null ? "" : time(next.durationMs()));
        boolean seek = next != null && next.canSeek() && next.durationMs() > 0 && knownClock;
        progress.setFocusable(seek);
        progress.setEnabled(seek);
        boolean[] enabled = {next != null && next.canPrevious(),
                next != null && next.canPlayPause(), next != null && next.canNext()};
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setEnabled(enabled[i]);
            buttons[i].setFocusable(enabled[i]);
            buttons[i].setAlpha(enabled[i] ? 1f : 0.35f);
            buttons[i].invalidate();
        }
        PrecisionIcons.bindControl(buttons[1],next != null && next.isPlaying() ? "Pause" : "Play");
        displayedSecond = Long.MIN_VALUE;
        schedule();
    }
    public void setQueueAvailable(boolean available) {
        if (!available && queueButton.hasFocus()) focusTransport();
        queueButton.setVisibility(available ? VISIBLE : GONE);
        queueButton.setFocusable(available);
        queueButton.setEnabled(available);
        queueButton.setClickable(available);
    }
    private boolean handleFavouriteRowKey(int slot, int key, KeyEvent event) {
        if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) return false;
        if (key == KeyEvent.KEYCODE_DPAD_DOWN && slot == 2) return lyricsModeButton.requestFocus();
        if (key == KeyEvent.KEYCODE_DPAD_DOWN && slot == 4 && flowButton.isFocusable()) return flowButton.requestFocus();
        if (key == KeyEvent.KEYCODE_DPAD_DOWN && slot >= 3) return spectrumModeButton.requestFocus();
        if (key == KeyEvent.KEYCODE_DPAD_DOWN && slot == 1 && lookupButton.isShown()) return lookupButton.requestFocus();
        if (key == KeyEvent.KEYCODE_DPAD_DOWN && queueButton.isShown()) return queueButton.requestFocus();
        if (key == KeyEvent.KEYCODE_DPAD_DOWN && lookupButton.isShown()) return lookupButton.requestFocus();
        if (key == KeyEvent.KEYCODE_DPAD_UP)
            return progress.isFocusable() ? progress.requestFocus()
                    : artist.isFocusable() ? artist.requestFocus()
                    : artwork.isFocusable() && artwork.requestFocus();
        if (key != KeyEvent.KEYCODE_DPAD_LEFT && key != KeyEvent.KEYCODE_DPAD_RIGHT) return false;
        View[] row = {removeHeart, buttons[0], buttons[1], buttons[2], addHeart};
        int direction = key == KeyEvent.KEYCODE_DPAD_LEFT ? -1 : 1;
        for (int i = slot + direction; i >= 0 && i < row.length; i += direction) {
            View candidate = row[i];
            if (candidate.getVisibility() == VISIBLE && candidate.isFocusable() && candidate.isEnabled())
                return candidate.requestFocus();
        }
        if (direction > 0) return lyricsModeButton.requestFocus();
        return true;
    }
    private boolean focusTransport() {
        if (buttons[1].isFocusable()) return buttons[1].requestFocus();
        for (PrecisionIcons.Control button : buttons) if (button.isFocusable()) return button.requestFocus();
        return false;
    }
    public void setDocument(DeezerLyricsDocument document) {
        lyrics.setDocument(document);
        if(document!=null && document.status()==DeezerLyricsDocument.Status.AVAILABLE) spectrumMode.available();
        updateSpectrumMode();
        snapClock = true;
        schedule();
    }
    public void showSpectrumFallback() { spectrumMode.failed();setStatus("");updateSpectrumMode(); }
    private void updateSpectrumMode() {
        boolean show=spectrumMode.showSpectrum();
        spectrum.setVisibility(show ? VISIBLE : GONE);
        spectrum.setActive(running && show);
        lyrics.setVisibility(show ? GONE : VISIBLE);
        status.setVisibility(!show && status.length()>0 ? VISIBLE : GONE);
        // Preserve the focus colour state list installed by useTextOnlyFocus.
        // Selection describes the current mode; focus alone owns the visible accent.
        lyricsModeButton.setSelected(!show);
        spectrumModeButton.setSelected(show);

    }
    public void setStatus(String message) {
        status.setText(message == null ? "" : message);
        status.setVisibility(spectrumMode.showSpectrum() || message == null || message.isEmpty() ? GONE : VISIBLE);
    }
    public void setRunning(boolean running) {
        this.running = running;
        spectrum.setActive(running && spectrumMode.showSpectrum());
        removeCallbacks(frame);
        posted = false;
        snapClock = true;
        if (running) schedule();
    }
    private void schedule() {
        if (!posted && running && isAttachedToWindow() && getWindowVisibility() == VISIBLE) {
            posted = true;
            postOnAnimation(frame);
        }
    }
    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); schedule(); }
    @Override protected void onDetachedFromWindow() {
        removeCallbacks(frame); posted = false; super.onDetachedFromWindow();
    }
    @Override protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) schedule(); else { removeCallbacks(frame); posted = false; }
    }
    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        // Child constraints must exist BEFORE FrameLayout measures its children.
        // Setting them from onSizeChanged leaves the first pass window-sized.
        if (width > 0 && height > 0 && (geometryWidth != width || geometryHeight != height)) {
            geometryWidth = width; geometryHeight = height;
            measureGeometry(width, height);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        alignMusicControls();
        layoutMusicColumn();
    }
    /** Optical centres and edge gaps keep the controls balanced around the fixed pause. */
    private void alignMusicControls() {
        FrameLayout.LayoutParams pause = (FrameLayout.LayoutParams)buttons[1].getLayoutParams();
        float center = pause.leftMargin + buttons[1].inkBounds().exactCenterX();
        float pitch = 88f * unit;
        PrecisionIcons.Control[] lower = {lookupButton, lyricsModeButton, spectrumModeButton, flowButton};
        for (int i = 0; i < lower.length; i++) alignInkCenter(lower[i], center + (i - 1.5f) * pitch);
        alignInkCenter(queueButton, center - 2.5f * pitch);
        alignInkCenter(buttons[0], center - pitch);
        alignInkCenter(buttons[2], center + pitch);
        android.graphics.Rect prev = buttons[0].inkBounds(), next = buttons[2].inkBounds();
        android.graphics.Rect paused = buttons[1].inkBounds();
        float pauseLeft = pause.leftMargin + paused.left;
        float pauseRight = pause.leftMargin + paused.right;
        float prevRight = inkX(buttons[0], prev.right), nextLeft = inkX(buttons[2], next.left);
        float leftGap = pauseLeft - prevRight, rightGap = nextLeft - pauseRight;
        android.graphics.Rect dislike = removeHeart.inkBounds(), heart = addHeart.inkBounds();
        FrameLayout.LayoutParams left = (FrameLayout.LayoutParams)removeHeart.getLayoutParams();
        FrameLayout.LayoutParams right = (FrameLayout.LayoutParams)addHeart.getLayoutParams();
        removeHeart.setTranslationX(inkX(buttons[0], prev.left) - leftGap - left.leftMargin - dislike.right);
        addHeart.setTranslationX(inkX(buttons[2], next.right) + rightGap - right.leftMargin - heart.left);
    }
    private float inkX(View view, float x) {
        return ((FrameLayout.LayoutParams)view.getLayoutParams()).leftMargin + view.getTranslationX() + x;
    }
    private void alignInkCenter(PrecisionIcons.Control control, float x) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)control.getLayoutParams();
        control.setTranslationX(x - params.leftMargin - control.inkBounds().exactCenterX());
    }
    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        lyrics.setHighlightCenterY(artwork.getBottom() + artwork.getTranslationY());
    }
    /** Distribute equal black gaps around the visible pixels of each now-playing group. */
    private void layoutMusicColumn() {
        float artHeight = artwork.getMeasuredHeight();
        float[] artistInk = textInk(artist);
        float[] titleInk = textInk(title);
        float[] progressInk = progressInk();
        float[] transportInk = iconInk(removeHeart, buttons[0], buttons[1], buttons[2], addHeart);
        float[] lowerInk = iconInk(queueButton, lookupButton, lyricsModeButton,
                spectrumModeButton, flowButton);
        float[] heights = {artHeight, artistInk[1] - artistInk[0], titleInk[1] - titleInk[0],
                progressInk[1] - progressInk[0], transportInk[1] - transportInk[0],
                lowerInk[1] - lowerInk[0]};
        float contentHeight = 0f;
        for (float height : heights) contentHeight += height;
        float gap = Math.max(0f, (getMeasuredHeight() - contentHeight) / 7f);

        float targetTop = gap;
        setTranslation(artwork, targetTop - topMargin(artwork));
        targetTop += artHeight + gap;
        setTranslation(artist, targetTop - topMargin(artist) - artistInk[0]);
        targetTop += heights[1] + gap;
        setTranslation(title, targetTop - topMargin(title) - titleInk[0]);
        targetTop += heights[2] + gap;
        float progressShift = targetTop - progressInk[0];
        for (View item : new View[]{progress, elapsed, duration}) item.setTranslationY(progressShift);
        targetTop += heights[3] + gap;
        translateGroup(new View[]{removeHeart, buttons[0], buttons[1], buttons[2], addHeart},
                targetTop, transportInk[0]);
        targetTop += heights[4] + gap;
        translateGroup(new View[]{queueButton, lookupButton, lyricsModeButton,
                spectrumModeButton, flowButton}, targetTop, lowerInk[0]);
    }

    private float[] textInk(TextView view) {
        CharSequence value = view.getText();
        if (TextUtils.isEmpty(value)) return new float[]{0f, 0f};
        android.graphics.Rect bounds = new android.graphics.Rect();
        String text = value.toString();
        view.getPaint().getTextBounds(text, 0, text.length(), bounds);
        int baseline = view.getBaseline();
        if (bounds.isEmpty()) {
            android.graphics.Paint.FontMetricsInt metrics = view.getPaint().getFontMetricsInt();
            bounds.set(0, metrics.top, 0, metrics.bottom);
        }
        return new float[]{baseline + bounds.top, baseline + bounds.bottom};
    }

    private float[] progressInk() {
        float top = topMargin(progress);
        float halfStroke = (progress.hasFocus() ? 4f : 2f) * unit * .5f;
        float center = top + progress.getMeasuredHeight() * .5f;
        float bottom = center + halfStroke;
        top = center - halfStroke;
        if (!TextUtils.isEmpty(elapsed.getText())) {
            float[] elapsedInk = textInk(elapsed);
            top = Math.min(top, topMargin(elapsed) + elapsedInk[0]);
            bottom = Math.max(bottom, topMargin(elapsed) + elapsedInk[1]);
        }
        if (!TextUtils.isEmpty(duration.getText())) {
            float[] durationInk = textInk(duration);
            top = Math.min(top, topMargin(duration) + durationInk[0]);
            bottom = Math.max(bottom, topMargin(duration) + durationInk[1]);
        }
        return new float[]{top, bottom};
    }

    private float[] iconInk(View... views) {
        float top = Float.MAX_VALUE;
        float bottom = Float.MIN_VALUE;
        for (View view : views) {
            if (view.getVisibility() != VISIBLE) continue;
            android.graphics.Rect ink;
            if (view instanceof PrecisionIcons.Control) ink = ((PrecisionIcons.Control) view).inkBounds();
            else if (view instanceof DeezerFavouriteButton) ink = ((DeezerFavouriteButton) view).inkBounds();
            else ink = new android.graphics.Rect(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            if (ink.isEmpty()) continue;
            top = Math.min(top, topMargin(view) + ink.top);
            bottom = Math.max(bottom, topMargin(view) + ink.bottom);
        }
        if (top == Float.MAX_VALUE) return new float[]{0f, 0f};
        return new float[]{top, bottom};
    }

    private float topMargin(View view) {
        return ((FrameLayout.LayoutParams) view.getLayoutParams()).topMargin;
    }

    private void setTranslation(View view, float translation) {
        view.setTranslationY(translation);
    }

    private void translateGroup(View[] views, float targetTop, float currentTop) {
        float shift = targetTop - currentTop;
        for (View view : views) view.setTranslationY(shift);
    }
    private void measureGeometry(int w, int h) {
        unit = Math.min(w / 1280f, h / 720f);
        float left = 66f * unit;
        float progressWidth = 397f * unit;
        float progressCenter = left + progressWidth * 0.5f;
        float artSize = 302f * unit;
        float artLeft = progressCenter - artSize * 0.5f;
        float transportSpan = (3f * 54f + 2f * 17f) * unit;
        float transportLeft = progressCenter - transportSpan * 0.5f;
        float artistHeight = 35f * unit;
        float titleHeight = 42f * unit;
        float progressAndTimeHeight = 42f * unit;
        float transportHeight = 54f * unit;
        float lowerControlsHeight = 54f * unit;
        float gap = Math.max(0f, (h - artSize - artistHeight - titleHeight
                - progressAndTimeHeight - transportHeight - lowerControlsHeight) / 7f);
        float artTop = gap;
        float artistTop = artTop + artSize + gap;
        float titleTop = artistTop + artistHeight + gap;
        float progressTop = titleTop + titleHeight + gap;
        float transportTop = progressTop + progressAndTimeHeight + gap;
        float lowerControlsTop = transportTop + transportHeight + gap;
        place(artwork, artLeft, artTop, artSize, artSize);
        place(artist, left, artistTop, progressWidth, artistHeight);
        place(title, left, titleTop, progressWidth, titleHeight);
        float lyricsX = 590f * unit;
        place(lyrics, lyricsX, 52f * unit, w - lyricsX - 66f * unit, 550f * unit);
        float spectrumWidth = w - lyricsX - 66f * unit;
        // Mirrored visible bars are symmetric about this screen-centred axis at every level.
        place(spectrum, lyricsX, (h - 270f * unit) * .5f, spectrumWidth, 270f * unit);
        place(status, lyricsX, 180f * unit, w - lyricsX - 85f * unit, 260f * unit);
        place(progress, left, progressTop, progressWidth, 18f * unit);
        place(elapsed, left, progressTop + 18f * unit, 80f * unit, 24f * unit);
        place(duration, left + 317f * unit, progressTop + 18f * unit, 80f * unit, 24f * unit);
        for (int i = 0; i < buttons.length; i++)
            place(buttons[i], transportLeft + i * 71f * unit, transportTop, 54f * unit, 54f * unit);
        // Hearts flank the accepted transport geometry; none of the original three moves.
        place(removeHeart, transportLeft - 71f * unit, transportTop, 54f * unit, 54f * unit);
        place(addHeart, transportLeft + 213f * unit, transportTop, 54f * unit, 54f * unit);
        size(title, 28); size(artist, 19); size(status, 24);
size(elapsed, 12); size(duration, 12);
        place(queueButton, transportLeft - 71f * unit, lowerControlsTop, 54f * unit, 54f * unit);
        place(lookupButton, transportLeft, lowerControlsTop, 54f * unit, 54f * unit);
        place(lyricsModeButton, transportLeft + 71f * unit, lowerControlsTop, 54f * unit, 54f * unit);
        place(spectrumModeButton, transportLeft + 142f * unit, lowerControlsTop, 54f * unit, 54f * unit);

        place(flowButton, transportLeft + 213f * unit, lowerControlsTop, 54f * unit, 54f * unit);

        artwork.invalidate();
    }
    private void place(View view, float x, float y, float w, float h) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.width = Math.max(1, Math.round(w)); params.height = Math.max(1, Math.round(h));
        params.leftMargin = Math.round(x); params.topMargin = Math.round(y);
    }
    private static void setLabelIfChanged(TextView label, String value) {
        if (!TextUtils.equals(label.getText(), value)) label.setText(value);
    }

    private void size(TextView text, float pixels) {
        float scale = getResources().getDisplayMetrics().scaledDensity / getResources().getDisplayMetrics().density;
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixels * unit * Math.min(1.4f, Math.max(1f, scale)));
    }
    private TextView label(String value, int pixels, int color, boolean medium) {
        TextView view = new TextView(getContext());
        view.setText(value); view.setTextColor(color);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, pixels);
        view.setTypeface(Typeface.create(medium ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
        view.setIncludeFontPadding(true);
        addView(view);
        return view;
    }
    private static String time(long millis) {
        if (millis < 0) return "–:––";
        long seconds = millis / 1000;
        return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
    }
    private final class PositionBar extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float fraction;
        PositionBar(Context context) { super(context); }
        @Override protected void onFocusChanged(boolean gain, int direction, android.graphics.Rect previous) {
            super.onFocusChanged(gain, direction, previous); invalidate();
        }
        @Override protected void onDraw(Canvas canvas) {
            float y = getHeight() * 0.5f;
            paint.setStrokeWidth((hasFocus() ? 4f : 2f) * unit);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(Color.rgb(43, 57, 65));
            canvas.drawLine(2f * unit, y, getWidth() - 2f * unit, y, paint);
            paint.setColor(accent);
            float x = 2f * unit + (getWidth() - 4f * unit) * fraction;
            canvas.drawLine(2f * unit, y, x, y, paint);
            if (hasFocus()) canvas.drawCircle(x, y, 5f * unit, paint);
        }
    }
}
