package com.boop.alpha1;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

final class BoopDevNotificationIconDrawable extends Drawable {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF050505;
    private static final int FACEBOOK = 0xFF1877F2;
    private static final int WHATSAPP = 0xFF25D366;
    private static final int GMAIL_RED = 0xFFEA4335;
    private static final int YOUTUBE = 0xFFFF0000;
    private static final int MESSENGER = 0xFF0084FF;
    private static final int INSTAGRAM = 0xFFE1306C;
    private static final int DISCORD = 0xFF5865F2;
    private static final int SPOTIFY = 0xFF1DB954;
    private static final int REDDIT = 0xFFFF4500;

    private final BoopDevNotificationIdentity.Spec spec;
    private int alpha = 255;
    private ColorFilter colorFilter;

    BoopDevNotificationIconDrawable(BoopDevNotificationIdentity.Spec spec) {
        if (spec == null) throw new IllegalArgumentException("spec required");
        this.spec = spec;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        float size = Math.min(bounds.width(), bounds.height());
        if (size <= 0f) return;
        float left = bounds.exactCenterX() - size / 2f;
        float top = bounds.exactCenterY() - size / 2f;
        RectF box = new RectF(left, top, left + size, top + size);

        switch (spec.mark()) {
            case FACEBOOK:
                roundBox(canvas, box, FACEBOOK, size * 0.19f);
                centeredText(canvas, "f", box, WHITE, size * 0.88f, size * 0.03f);
                return;
            case WHATSAPP:
                canvas.drawCircle(box.centerX(), box.centerY(), size * 0.48f, paint(WHATSAPP, Paint.Style.FILL, 0f));
                canvas.drawCircle(box.centerX(), box.centerY(), size * 0.31f, paint(WHITE, Paint.Style.STROKE, size * 0.07f));
                canvas.drawArc(new RectF(
                                box.left + size * 0.32f,
                                box.top + size * 0.31f,
                                box.right - size * 0.29f,
                                box.bottom - size * 0.30f),
                        122f, 136f, false, paint(WHITE, Paint.Style.STROKE, size * 0.065f));
                return;
            case GMAIL:
                roundBox(canvas, box, WHITE, size * 0.16f);
                Path mail = new Path();
                mail.moveTo(box.left + size * 0.13f, box.top + size * 0.28f);
                mail.lineTo(box.centerX(), box.top + size * 0.58f);
                mail.lineTo(box.right - size * 0.13f, box.top + size * 0.28f);
                mail.lineTo(box.right - size * 0.13f, box.bottom - size * 0.18f);
                mail.moveTo(box.left + size * 0.13f, box.top + size * 0.28f);
                mail.lineTo(box.left + size * 0.13f, box.bottom - size * 0.18f);
                canvas.drawPath(mail, paint(GMAIL_RED, Paint.Style.STROKE, size * 0.105f));
                return;
            case X:
                roundBox(canvas, box, BLACK, size * 0.18f);
                Paint xPaint = paint(WHITE, Paint.Style.STROKE, size * 0.09f);
                canvas.drawLine(box.left + size * 0.25f, box.top + size * 0.20f,
                        box.right - size * 0.22f, box.bottom - size * 0.20f, xPaint);
                canvas.drawLine(box.right - size * 0.25f, box.top + size * 0.20f,
                        box.left + size * 0.22f, box.bottom - size * 0.20f, xPaint);
                return;
            case YOUTUBE:
                roundBox(canvas, box, YOUTUBE, size * 0.22f);
                Path play = new Path();
                play.moveTo(box.left + size * 0.40f, box.top + size * 0.30f);
                play.lineTo(box.left + size * 0.40f, box.bottom - size * 0.30f);
                play.lineTo(box.right - size * 0.28f, box.centerY());
                play.close();
                canvas.drawPath(play, paint(WHITE, Paint.Style.FILL, 0f));
                return;
            case MESSENGER:
                canvas.drawCircle(box.centerX(), box.centerY(), size * 0.48f, paint(MESSENGER, Paint.Style.FILL, 0f));
                Path bolt = new Path();
                bolt.moveTo(box.left + size * 0.22f, box.centerY() + size * 0.10f);
                bolt.lineTo(box.left + size * 0.43f, box.centerY() - size * 0.13f);
                bolt.lineTo(box.centerX() + size * 0.02f, box.centerY() - size * 0.01f);
                bolt.lineTo(box.right - size * 0.22f, box.centerY() - size * 0.18f);
                bolt.lineTo(box.right - size * 0.43f, box.centerY() + size * 0.13f);
                bolt.lineTo(box.centerX() - size * 0.02f, box.centerY() + size * 0.01f);
                bolt.close();
                canvas.drawPath(bolt, paint(WHITE, Paint.Style.FILL, 0f));
                return;
            case INSTAGRAM:
                roundBox(canvas, box, INSTAGRAM, size * 0.23f);
                RectF camera = inset(box, size * 0.19f);
                canvas.drawRoundRect(camera, size * 0.16f, size * 0.16f,
                        paint(WHITE, Paint.Style.STROKE, size * 0.07f));
                canvas.drawCircle(box.centerX(), box.centerY(), size * 0.16f,
                        paint(WHITE, Paint.Style.STROKE, size * 0.065f));
                canvas.drawCircle(box.right - size * 0.31f, box.top + size * 0.31f,
                        size * 0.045f, paint(WHITE, Paint.Style.FILL, 0f));
                return;
            case DISCORD:
                roundBox(canvas, box, DISCORD, size * 0.22f);
                RectF smile = new RectF(
                        box.left + size * 0.22f,
                        box.top + size * 0.27f,
                        box.right - size * 0.22f,
                        box.bottom - size * 0.21f);
                canvas.drawArc(smile, 200f, 140f, false,
                        paint(WHITE, Paint.Style.STROKE, size * 0.105f));
                canvas.drawCircle(box.centerX() - size * 0.13f, box.centerY(), size * 0.055f,
                        paint(WHITE, Paint.Style.FILL, 0f));
                canvas.drawCircle(box.centerX() + size * 0.13f, box.centerY(), size * 0.055f,
                        paint(WHITE, Paint.Style.FILL, 0f));
                return;
            case SPOTIFY:
                canvas.drawCircle(box.centerX(), box.centerY(), size * 0.48f, paint(SPOTIFY, Paint.Style.FILL, 0f));
                Paint wave = paint(BLACK, Paint.Style.STROKE, size * 0.055f);
                RectF waveBox = inset(box, size * 0.20f);
                canvas.drawArc(waveBox, 205f, 130f, false, wave);
                RectF waveMid = inset(box, size * 0.27f);
                canvas.drawArc(waveMid, 205f, 130f, false, wave);
                RectF waveInner = inset(box, size * 0.34f);
                canvas.drawArc(waveInner, 205f, 130f, false, wave);
                return;
            case REDDIT:
                canvas.drawCircle(box.centerX(), box.centerY(), size * 0.48f, paint(REDDIT, Paint.Style.FILL, 0f));
                canvas.drawCircle(box.centerX(), box.centerY() + size * 0.06f, size * 0.29f, paint(WHITE, Paint.Style.FILL, 0f));
                canvas.drawCircle(box.centerX() - size * 0.10f, box.centerY() + size * 0.02f, size * 0.035f, paint(BLACK, Paint.Style.FILL, 0f));
                canvas.drawCircle(box.centerX() + size * 0.10f, box.centerY() + size * 0.02f, size * 0.035f, paint(BLACK, Paint.Style.FILL, 0f));
                Paint antenna = paint(WHITE, Paint.Style.STROKE, size * 0.045f);
                canvas.drawLine(box.centerX() + size * 0.05f, box.centerY() - size * 0.22f,
                        box.centerX() + size * 0.16f, box.top + size * 0.17f, antenna);
                canvas.drawCircle(box.centerX() + size * 0.20f, box.top + size * 0.15f,
                        size * 0.055f, paint(WHITE, Paint.Style.STROKE, size * 0.035f));
                return;
            default:
                throw new IllegalStateException("Unknown dev notification identity: " + spec.mark());
        }
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = Math.max(0, Math.min(255, alpha));
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        this.colorFilter = colorFilter;
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return 128;
    }

    @Override
    public int getIntrinsicHeight() {
        return 128;
    }

    private void roundBox(Canvas canvas, RectF box, int color, float radius) {
        canvas.drawRoundRect(box, radius, radius, paint(color, Paint.Style.FILL, 0f));
    }

    private void centeredText(
            Canvas canvas,
            String value,
            RectF box,
            int color,
            float textSize,
            float verticalNudge) {
        Paint text = paint(color, Paint.Style.FILL, 0f);
        text.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(textSize);
        Paint.FontMetrics metrics = text.getFontMetrics();
        float baseline = box.centerY() - (metrics.ascent + metrics.descent) / 2f + verticalNudge;
        canvas.drawText(value, box.centerX(), baseline, text);
    }

    private Paint paint(int color, Paint.Style style, float strokeWidth) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(style);
        paint.setStrokeWidth(strokeWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setAlpha(alpha);
        paint.setColorFilter(colorFilter);
        return paint;
    }

    private static RectF inset(RectF source, float amount) {
        return new RectF(
                source.left + amount,
                source.top + amount,
                source.right - amount,
                source.bottom - amount);
    }
}
