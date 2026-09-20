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
        void previous(); void playPause(); void next(); void seek(long milliseconds); void close(); void browseAlbum();
    }
    private final int accent;
    private final Controls controls;
    private final ImageView artwork;
    private final TextView eyebrow, title, artist, status, elapsed, duration;
    private final LyricsLinesView lyrics;
    private final TransportButton[] buttons = new TransportButton[5];
    private final PositionBar progress;
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
        eyebrow = label("NOW PLAYING", 14, Color.rgb(151, 169, 178), true);
        eyebrow.setLetterSpacing(0.18f);
        artwork = new ImageView(context) {
            private final Paint focusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            @Override protected void onFocusChanged(boolean gain, int direction, android.graphics.Rect previous) {
                super.onFocusChanged(gain, direction, previous);
                invalidate();
            }
            private final Path artworkClip = new Path();
            private final RectF artworkBounds = new RectF();
            @Override protected void onDraw(Canvas canvas) {
                artworkBounds.set(0f, 0f, getWidth(), getHeight());
                artworkClip.reset();
                artworkClip.addRoundRect(artworkBounds, 9f * unit, 9f * unit, Path.Direction.CW);
                int clipped = canvas.save();
                canvas.clipPath(artworkClip);
                super.onDraw(canvas);
                canvas.restoreToCount(clipped);
                if (hasFocus()) {
                    float inset = 2f * unit;
                    focusPaint.setStyle(Paint.Style.STROKE);
                    focusPaint.setStrokeWidth(3f * unit);
                    focusPaint.setColor(accent);
                    canvas.drawRoundRect(inset, inset, getWidth() - inset, getHeight() - inset,
                            9f * unit, 9f * unit, focusPaint);
                }
            }
        };
        artwork.setOnClickListener(v -> { if (v.isEnabled()) controls.browseAlbum(); });
        artwork.setScaleType(ImageView.ScaleType.CENTER_CROP);
        artwork.setBackground(FocusChrome.filled(context, Color.rgb(16, 24, 29), 9, false));
        artwork.setClipToOutline(false);
        addView(artwork);
        title = label("", 29, Color.WHITE, true);
        title.setSingleLine(true);
        title.setHorizontallyScrolling(true);
        title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        title.setMarqueeRepeatLimit(1);
        title.setSelected(true);
        artist = label("", 19, Color.rgb(162, 179, 189), false);
        artist.setSingleLine(true);
        artist.setEllipsize(TextUtils.TruncateAt.END);
        lyrics = new LyricsLinesView(context, accent);
        addView(lyrics);
        status = label("", 24, Color.rgb(155, 174, 184), false);
        status.setGravity(Gravity.CENTER_VERTICAL);
        status.setMaxLines(3);
        elapsed = label("0:00", 12, Color.rgb(161, 177, 187), false);
        duration = label("", 12, Color.rgb(161, 177, 187), false);
        duration.setGravity(Gravity.END);
        progress = new PositionBar(context);
        progress.setFocusable(true);
        progress.setContentDescription("Track position. Left and right seek ten seconds.");
        progress.setOnKeyListener((v, key, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (key == KeyEvent.KEYCODE_DPAD_LEFT) { controls.seek(-10000L); return true; }
            if (key == KeyEvent.KEYCODE_DPAD_RIGHT) { controls.seek(10000L); return true; }
            if (key == KeyEvent.KEYCODE_DPAD_UP) return artwork.isFocusable() && artwork.requestFocus();
            if (key == KeyEvent.KEYCODE_DPAD_DOWN) return focusTransport();
            return false;
        });
        addView(progress);
        artwork.setOnKeyListener((v, key, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN || key != KeyEvent.KEYCODE_DPAD_DOWN) return false;
            return progress.isFocusable() ? progress.requestFocus() : focusTransport();
        });

        String[] descriptions = {"Previous track", "Rewind ten seconds", "Pause", "Forward ten seconds", "Next track"};
        for (int i = 0; i < buttons.length; i++) {
            final int kind = i;
            TransportButton button = new TransportButton(context, i);
            buttons[i] = button;
            button.setFocusable(true);
            button.setClickable(true);
            button.setContentDescription(descriptions[i]);
            button.setOnClickListener(v -> {
                if (!v.isEnabled()) return;
                switch (kind) {
                    case 0: controls.previous(); break;
                    case 1: controls.seek(-10000L); break;
                    case 2: controls.playPause(); break;
                    case 3: controls.seek(10000L); break;
                    case 4: controls.next(); break;
                    default: break;
                }
            });
            button.setOnKeyListener((v, key, event) -> event.getAction() == KeyEvent.ACTION_DOWN
                    && key == KeyEvent.KEYCODE_DPAD_UP
                    && (progress.isFocusable() ? progress.requestFocus()
                            : artwork.isFocusable() && artwork.requestFocus()));
            addView(button);
        }
        post(() -> { if (buttons[2].isFocusable()) buttons[2].requestFocus(); });
    }
    public void setSnapshot(NowPlayingSnapshot next, boolean knownClock) {
        long now = SystemClock.elapsedRealtime();
        if (snapshot == null || next == null || snapshot.sessionId() != next.sessionId()
                || !snapshot.trackKey().equals(next.trackKey())
                || Math.abs(snapshot.estimatedPositionMs(now) - next.estimatedPositionMs(now)) > 1500L) snapClock = true;
        snapshot = next;
        clockKnown = knownClock;
        artwork.setImageBitmap(next == null ? null : next.artwork());
        boolean albumAvailable = next != null && NowPlayingSelectionPolicy.eligible(next.playbackState());
        artwork.setEnabled(albumAvailable);
        artwork.setFocusable(albumAvailable);
        artwork.setClickable(albumAvailable);
        artwork.setContentDescription(next != null && "deezer.android.app".equals(next.packageName())
                ? "Browse album in Deezer" : "Open source player");
        title.setText(next == null ? "Now Playing" : next.title());
        artist.setText(next == null ? "" : next.subtitle());
        duration.setText(next == null ? "" : time(next.durationMs()));
        boolean seek = next != null && next.canSeek() && next.durationMs() > 0 && knownClock;
        progress.setFocusable(seek);
        progress.setEnabled(seek);
        boolean[] enabled = {next != null && next.canPrevious(), seek,
                next != null && next.canPlayPause(), seek, next != null && next.canNext()};
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setEnabled(enabled[i]);
            buttons[i].setFocusable(enabled[i]);
            buttons[i].invalidate();
        }
        buttons[2].setContentDescription(next != null && next.isPlaying() ? "Pause" : "Play");
        displayedSecond = Long.MIN_VALUE;
        schedule();
    }
    private boolean focusTransport() {
        if (buttons[2].isFocusable()) return buttons[2].requestFocus();
        for (TransportButton button : buttons) if (button.isFocusable()) return button.requestFocus();
        return false;
    }
    public void setDocument(DeezerLyricsDocument document) {
        lyrics.setDocument(document);
        snapClock = true;
        schedule();
    }
    public void setStatus(String message) {
        status.setText(message == null ? "" : message);
        status.setVisibility(message == null || message.isEmpty() ? GONE : VISIBLE);
    }
    public void setRunning(boolean running) {
        this.running = running;
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
    }
    private void measureGeometry(int w, int h) {
        unit = Math.min(w / 1280f, h / 720f);
        float left = 66f * unit;
        float artSize = 302f * unit;
        place(eyebrow, left, 54f * unit, 365f * unit, 30f * unit);
        place(artwork, left, 116f * unit, artSize, artSize);
        place(title, left, 448f * unit, 440f * unit, 42f * unit);
        place(artist, left, 532f * unit, 440f * unit, 35f * unit);
        float lyricsX = 590f * unit;
        place(lyrics, lyricsX, 52f * unit, w - lyricsX - 66f * unit, 550f * unit);
        place(status, lyricsX, 180f * unit, w - lyricsX - 85f * unit, 260f * unit);
        place(progress, left, 583f * unit, 397f * unit, 18f * unit);
        place(elapsed, left, 601f * unit, 80f * unit, 24f * unit);
        place(duration, left + 317f * unit, 601f * unit, 80f * unit, 24f * unit);
        for (int i = 0; i < buttons.length; i++) place(buttons[i], left + i * 71f * unit, 632f * unit, 54f * unit, 54f * unit);
        size(eyebrow, 13); size(title, 28); size(artist, 19); size(status, 24);
size(elapsed, 12); size(duration, 12);
        artwork.invalidate();
    }
    private void place(View view, float x, float y, float w, float h) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.width = Math.max(1, Math.round(w)); params.height = Math.max(1, Math.round(h));
        params.leftMargin = Math.round(x); params.topMargin = Math.round(y);
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
    private final class TransportButton extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();
        private final RectF arc = new RectF(-15, -15, 15, 15);
        private final int kind;
        TransportButton(Context context, int kind) { super(context); this.kind = kind; }
        @Override protected void onFocusChanged(boolean gain, int direction, android.graphics.Rect previous) {
            super.onFocusChanged(gain, direction, previous); invalidate();
        }
        private void triangle(Canvas canvas, float x) {
            path.reset(); path.moveTo(x - 6, -9); path.lineTo(x + 7, 0); path.lineTo(x - 6, 9); path.close(); canvas.drawPath(path, paint);
        }
        @Override protected void onDraw(Canvas canvas) {
            canvas.save();
            canvas.translate(getWidth() * 0.5f, getHeight() * 0.5f);
            canvas.scale(unit, unit);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(hasFocus() ? Color.rgb(22, 49, 58) : Color.rgb(14, 23, 28));
            canvas.drawCircle(0, 0, 25, paint);
            if (hasFocus()) {
                paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(1.6f); paint.setColor(accent);
                canvas.drawCircle(0, 0, 25, paint);
            }
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(isEnabled() ? Color.WHITE : Color.rgb(69, 83, 92));
            if (kind == 2) {
                if (snapshot != null && snapshot.isPlaying()) {
                    canvas.drawRoundRect(-7, -9, -2, 9, 1, 1, paint);
                    canvas.drawRoundRect(2, -9, 7, 9, 1, 1, paint);
                } else triangle(canvas, 1);
            } else if (kind == 0 || kind == 4) {
                canvas.save();
                if (kind == 0) canvas.scale(-1, 1);
                triangle(canvas, -1); canvas.drawRect(8, -9, 11, 9, paint);
                canvas.restore();
            } else {
                canvas.save();
                if (kind == 1) canvas.scale(-1, 1);
                paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(1.8f); paint.setStrokeCap(Paint.Cap.ROUND);
                canvas.drawArc(arc, -60, 285, false, paint);
                paint.setStyle(Paint.Style.FILL);
                path.reset(); path.moveTo(8, -18); path.lineTo(9, -9); path.lineTo(16, -14); path.close(); canvas.drawPath(path, paint);
                canvas.restore();
                paint.setTextSize(11); paint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                paint.setTextAlign(Paint.Align.CENTER); canvas.drawText("10", 0, 4, paint);
            }
            canvas.restore();
        }
    }
}
