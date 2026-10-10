package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

/** Cached native text layout with frame-paced motion; the audio clock chooses the active cue. */
public final class LyricsLinesView extends View {
    private final TextPaint paint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG | TextPaint.SUBPIXEL_TEXT_FLAG);
    private final List<StaticLayout> layouts = new ArrayList<>();
    private final List<Float> offsets = new ArrayList<>();
    private final List<byte[]> inkRows = new ArrayList<>();
    private final int accent;
    private DeezerLyricsDocument document;
    private long position = -1;
    private int active = -1;
    private int anchor = -1;
    private float scroll;
    private float from;
    private float target;
    private float opticalFrom, opticalTarget;
    private float highlightCenterY = Float.NaN;
    private float unit = 1f;
    private long movedAt;
    private boolean snap = true;

    public LyricsLinesView(Context context, int accent) {
        super(context);
        this.accent = accent;
        setFocusable(false);
        paint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    }
    /** Screen-space optical centre of the active lyric, aligned to the album bottom. */
    public void setHighlightCenterY(float y) {
        if (Math.abs(highlightCenterY - y) < .01f) return;
        highlightCenterY = y;
        snap = true;
        setPosition(position, true);
    }
    public void setDocument(DeezerLyricsDocument document) {
        if (this.document == document) return;
        this.document = document;
        active = anchor = -1;
        snap = true;
        rebuild();
    }
    public void setPosition(long position, boolean discontinuity) {
        this.position = position;
        if (document == null || layouts.isEmpty()) return;
        int nextActive = document.activeIndex(position);
        int nextAnchor = document.anchorIndex(position);
        long now = SystemClock.uptimeMillis();
        scroll = interpolated(now);
        if (nextAnchor != anchor || snap || discontinuity) {
            float destination = offsets.get(nextAnchor) + layouts.get(nextAnchor).getHeight() * 0.5f;
            opticalFrom = interpolatedOptical(now);
            opticalTarget = opticalShift(destination);
            from = scroll;
            target = destination;
            movedAt = now;
            if (snap || discontinuity) {
                from = scroll = target;
                opticalFrom = opticalTarget;
                setTranslationY(opticalTarget);
            }
            anchor = nextAnchor;
            snap = false;
        }
        if (active != nextActive) {
            active = nextActive;
            setContentDescription(active < 0 ? "Instrumental" : document.lines().get(active).text());
        }
        invalidate();
    }
    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        unit = Math.max(0.5f, h / 550f);
        snap = true;
        rebuild();
    }
    private void rebuild() {
        layouts.clear();
        offsets.clear();
        inkRows.clear();
        setTranslationY(0f);
        if (getWidth() <= 0 || document == null) { invalidate(); return; }
        float fontScale = getResources().getDisplayMetrics().scaledDensity
                / getResources().getDisplayMetrics().density;
        paint.setTextSize(38f * unit * Math.min(1.5f, Math.max(1f, fontScale)));
        float top = 0f;
        for (DeezerLyricsDocument.Line line : document.lines()) {
            String text = line.text().isEmpty() ? "· · ·" : line.text();
            StaticLayout layout = StaticLayout.Builder.obtain(text, 0, text.length(), paint, getWidth())
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setIncludePad(true).setUseLineSpacingFromFallbacks(true)
                    .setLineSpacing(3f * unit, 1.02f)
                    .setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY)
                    .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE).build();
            offsets.add(top);
            layouts.add(layout);
            inkRows.add(measureInkRows(layout));
            top += layout.getHeight() + 23f * unit;
        }
        setPosition(position, true);
    }
    /** Cache actual glyph alpha once, including wrapping, fallback fonts and accents. */
    private byte[] measureInkRows(StaticLayout layout) {
        int width = layout.getWidth(), height = layout.getHeight();
        android.graphics.Bitmap mask = android.graphics.Bitmap.createBitmap(width, height,
                android.graphics.Bitmap.Config.ALPHA_8);
        paint.setColor(Color.WHITE); paint.setAlpha(255);
        layout.draw(new Canvas(mask));
        byte[] rows = new byte[height];
        int[] pixels = new int[width];
        for (int y = 0; y < height; y++) {
            mask.getPixels(pixels, 0, width, 0, y, width, 1);
            int maximum = 0;
            for (int pixel : pixels) maximum = Math.max(maximum, Color.alpha(pixel));
            rows[y] = (byte) maximum;
        }
        mask.recycle();
        return rows;
    }
    private int lineAlpha(float top, float height) {
        float midpoint = top + height * .5f;
        float edge = Math.min(midpoint, getHeight() - midpoint) / (90f * unit);
        return Math.round(255f * Math.min(1f, Math.max(0f, edge)));
    }
    /** Use the active cue's ink extent, including all wrapped rows, without font padding. */
    private float opticalShift(float position) {
        if (!(getParent() instanceof View) || ((View)getParent()).getHeight() <= 0) return 0;
        int cue = document.anchorIndex(this.position);
        byte[] rows = inkRows.get(cue);
        int first = rows.length, last = 0;
        for (int y = 0; y < rows.length; y++) {
            if ((rows[y] & 255) <= 16) continue;
            first = Math.min(first, y);
            last = y + 1;
        }
        float desired = Float.isNaN(highlightCenterY)
                ? ((View)getParent()).getHeight() * .5f : highlightCenterY;
        float top = getHeight() * .44f + offsets.get(cue) - position;
        return last > first ? desired - getTop() - top - (first + last) * .5f : 0;
    }
    private float interpolatedOptical(long now) {
        float t = Math.min(1f, Math.max(0f, (now - movedAt) / 420f));
        float eased = t * t * (3f - 2f * t);
        return opticalFrom + (opticalTarget - opticalFrom) * eased;
    }
    private float interpolated(long now) {
        float t = Math.min(1f, Math.max(0f, (now - movedAt) / 420f));
        float eased = t * t * (3f - 2f * t);
        return from + (target - from) * eased;
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (document == null || layouts.isEmpty()) return;
        scroll = interpolated(SystemClock.uptimeMillis());
        float center = getHeight() * 0.44f;
        // Both optical offset and lyric scrolling share the same continuous easing curve.
        float optical = interpolatedOptical(SystemClock.uptimeMillis());
        if (Math.abs(getTranslationY() - optical) > .01f) setTranslationY(optical);
        canvas.save();
        canvas.clipRect(0, 0, getWidth(), getHeight());
        for (int i = 0; i < layouts.size(); i++) {
            StaticLayout layout = layouts.get(i);
            float top = center + offsets.get(i) - scroll;
            float bottom = top + layout.getHeight();
            if (bottom < 0 || top > getHeight()) continue;
            int alpha = lineAlpha(top, layout.getHeight());
            if (i == active) paint.setColor(accent);
            else paint.setColor(i < anchor ? Color.rgb(130, 143, 151) : Color.rgb(213, 221, 225));
            paint.setAlpha(alpha);
            canvas.save();
            canvas.translate(0, top);
            layout.draw(canvas);
            canvas.restore();
        }
        paint.setAlpha(255);
        canvas.restore();
    }
}
