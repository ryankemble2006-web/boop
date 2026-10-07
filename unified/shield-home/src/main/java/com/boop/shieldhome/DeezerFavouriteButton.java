package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.View;

/** Shared native favourites toggle and separate outlined dislike-and-skip action. */
final class DeezerFavouriteButton extends View {
    static final int DISLIKE = -1, TOGGLE = 0;
    private final int mode;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path heart = new Path();
    private final DeezerFavouriteController controller;
    private DeezerFavouriteController.State state;
    private NowPlayingSnapshot snapshot;
    private Runnable unsubscribe;
    private boolean subscribed;
    private boolean standaloneHeart;

    DeezerFavouriteButton(Context context, int mode, int accent) {
        super(context);
        this.mode = mode;
        controller = DeezerFavouriteController.get(context);
        setFocusable(true); setClickable(true);
        setOnClickListener(v -> {
            if (mode == DISLIKE) controller.dislike(snapshot);
            else controller.change(snapshot, state, null);
        });
        updateDescription();
    }
    void useStandaloneHeart() {
        standaloneHeart = true;
        setBackground(null);
    }
    void setSnapshot(NowPlayingSnapshot next) {
        snapshot = next;
        boolean visible = next != null && "deezer.android.app".equals(next.packageName());
        setVisibility(visible ? VISIBLE : GONE);
        if (state != null && !state.matches(next)) state = null;
        updateDescription(); invalidate();
        updateSubscription();
    }
    private void updateSubscription() {
        boolean observe = isAttachedToWindow() && isShown() && getWindowVisibility() == VISIBLE;
        if (observe && !subscribed) {
            subscribed = true;
            unsubscribe = controller.subscribe(next -> { state = next; updateDescription(); invalidate(); });
        } else if (!observe && subscribed) {
            subscribed = false;
            if (unsubscribe != null) unsubscribe.run();
            unsubscribe = null;
        }
    }
    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); updateSubscription(); }
    @Override protected void onDetachedFromWindow() {
        subscribed = false;
        if (unsubscribe != null) unsubscribe.run();
        unsubscribe = null;
        super.onDetachedFromWindow();
    }
    @Override protected void onVisibilityChanged(View changed, int visibility) {
        super.onVisibilityChanged(changed, visibility);
        if (controller != null) updateSubscription();
    }
    @Override protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (controller != null) updateSubscription();
    }
    @Override protected void onFocusChanged(boolean gain, int direction, Rect previous) {
        super.onFocusChanged(gain, direction, previous);
        invalidate();
    }
    private boolean known() { return state != null && state.matches(snapshot); }
    private void updateDescription() {
        String label = mode == DISLIKE ? "Dislike this track and skip in Deezer"
                : known() && state.saved == 1 ? "Remove from Deezer favourites" : "Add to Deezer favourites";
        if (known() && state.pending) label += ". Waiting for Deezer";
        else if (mode == TOGGLE && (!known() || state.saved == -1)) label = "Check Deezer favourite status";
        setContentDescription(label);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int accent = FocusChrome.accentColor(getContext());
        boolean known = known();
        boolean saved = known && state.saved == 1;
        boolean pending = known && state.pending;
        boolean available = known && !pending && (mode == DISLIKE ? controller.canDislike()
                : state.saved == 1 ? state.canRemove : state.canAdd);
        if (standaloneHeart) {
            drawStandaloneHeart(canvas, accent, known, saved, pending, available);
            return;
        }
        float scale = Math.min(getWidth(), getHeight()) / 54f;
        canvas.save();
        canvas.translate(getWidth() * 0.5f, getHeight() * 0.5f);
        canvas.scale(scale, scale);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(hasFocus() ? Color.rgb(22, 49, 58) : Color.rgb(14, 23, 28));
        canvas.drawCircle(0, 0, 25, paint);
        if (hasFocus()) {
            paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(1.6f); paint.setColor(accent);
            canvas.drawCircle(0, 0, 25, paint);
        }
        int ink = pending ? Color.rgb(151, 169, 178) : hasFocus() ? accent
                : saved && mode == TOGGLE ? accent
                : available ? Color.WHITE : Color.rgb(91, 107, 116);
        paint.setColor(ink);
        paint.setStrokeWidth(1.8f); paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(saved && mode == TOGGLE ? Paint.Style.FILL : Paint.Style.STROKE);
        heart.reset();
        heart.moveTo(0, 12);
        heart.cubicTo(-3, 9, -13, 2, -13, -5);
        heart.cubicTo(-13, -14, -4, -15, 0, -8);
        heart.cubicTo(4, -15, 13, -14, 13, -5);
        heart.cubicTo(13, 2, 3, 9, 0, 12);
        heart.close(); canvas.drawPath(heart, paint);
        if (mode == DISLIKE) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(ink); paint.setStrokeWidth(2f); paint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine(-14, 13, 14, -14, paint);
        } else if (!known || state.saved == -1) {
            paint.setStyle(Paint.Style.FILL); paint.setColor(ink); paint.setTextSize(13);
            paint.setTextAlign(Paint.Align.CENTER); canvas.drawText("?", 0, 3, paint);
        }
        if (pending) {
            paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(2); paint.setColor(accent);
            canvas.drawArc(-20, -20, 20, 20, -90, 250, false, paint);
        }
        canvas.restore();
    }

    private void drawStandaloneHeart(Canvas canvas, int accent, boolean known,
            boolean saved, boolean pending, boolean available) {
        float density = getResources().getDisplayMetrics().density;
        float stroke = (hasFocus() ? 2f : 1.2f) * density;
        float inset = stroke * 0.5f;
        float w = getWidth() - stroke, h = getHeight() - stroke;
        canvas.save();
        canvas.translate(inset, inset);
        heart.reset();
        heart.moveTo(w * .5f, h);
        heart.cubicTo(w * .4f, h * .9f, 0, h * .6f, 0, h * .3f);
        heart.cubicTo(0, h * .04f, w * .15f, 0, w * .25f, 0);
        heart.cubicTo(w * .36f, 0, w * .46f, h * .06f, w * .5f, h * .17f);
        heart.cubicTo(w * .54f, h * .06f, w * .64f, 0, w * .75f, 0);
        heart.cubicTo(w * .85f, 0, w, h * .04f, w, h * .3f);
        heart.cubicTo(w, h * .6f, w * .6f, h * .9f, w * .5f, h);
        heart.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(saved ? accent : Color.rgb(34, 34, 34));
        canvas.drawPath(heart, paint);
        int ink = hasFocus() ? (saved ? Color.WHITE : accent)
                : pending ? Color.rgb(151, 169, 178)
                : available ? Color.WHITE : Color.rgb(91, 107, 116);
        {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(saved && !hasFocus() && !pending ? accent : ink);
            canvas.drawPath(heart, paint);
        }
        if (!known || state.saved == -1) {
            paint.setStyle(Paint.Style.FILL); paint.setColor(ink);
            paint.setTextSize(h * .4f); paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("?", w * .5f, h * .61f, paint);
        }
        if (pending) {
            paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(density);
            paint.setColor(accent);
            canvas.drawArc(w * .25f, h * .23f, w * .75f, h * .73f, -90, 250, false, paint);
        }
        canvas.restore();
    }
}
