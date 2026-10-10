package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.view.Choreographer;
import android.widget.TextView;

/** Public-API marquee with a stable timeline and an exact 5% speed increase. */
final class SerenMarqueeTextView extends TextView implements Choreographer.FrameCallback {
    private final SerenMarqueeState progress = new SerenMarqueeState();
    private final float density;
    private boolean framePending, visible;

    SerenMarqueeTextView(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        setSingleLine(true);
        setPaintFlags(getPaintFlags() | android.graphics.Paint.SUBPIXEL_TEXT_FLAG);
    }

    // Android's single-line layout can scroll independently when text is aligned
    // or relaid out. The custom marquee is the sole horizontal scroll owner.
    @Override public void scrollTo(int x, int y) { super.scrollTo(0, y); }

    void showText(String value) {
        if (!progress.setText(value)) return;
        setText(value);
        invalidate();
        scheduleFrame();
    }

    @Override public void setSelected(boolean selected) {
        if (selected == isSelected()) return;
        super.setSelected(selected);
        updateAnimation();
    }

    @Override public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        visible = isVisible;
        updateAnimation();
    }

    @Override protected void onDetachedFromWindow() {
        stopFrames();
        super.onDetachedFromWindow();
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        scheduleFrame();
    }

    private boolean shouldScroll() {
        Layout layout = getLayout();
        return isAttachedToWindow() && visible && isSelected() && layout != null
                && layout.getLineWidth(0) > getWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight();
    }

    private void updateAnimation() {
        if (shouldScroll()) scheduleFrame(); else stopFrames();
        invalidate();
    }

    private void scheduleFrame() {
        if (!framePending && shouldScroll()) {
            framePending = true;
            Choreographer.getInstance().postFrameCallback(this);
        }
    }

    private void stopFrames() {
        Choreographer.getInstance().removeFrameCallback(this);
        framePending = false;
        progress.pause();
    }

    @Override public void doFrame(long frameTimeNanos) {
        framePending = false;
        if (!shouldScroll()) { progress.pause(); return; }
        progress.tick(frameTimeNanos / 1000000L);
        invalidate();
        scheduleFrame();
    }

    @Override protected void onDraw(Canvas canvas) {
        Layout layout = getLayout();
        if (layout == null || !isSelected()) { super.onDraw(canvas); return; }
        int left = getCompoundPaddingLeft();
        int width = getWidth() - left - getCompoundPaddingRight();
        float lineWidth = layout.getLineWidth(0);
        if (lineWidth <= width) { super.onDraw(canvas); return; }
        float offset = progress.position(lineWidth, width, density);
        getPaint().setColor(getCurrentTextColor());
        int saved = canvas.save();
        canvas.clipRect(left, getCompoundPaddingTop(), left + width, getHeight() - getCompoundPaddingBottom());
        canvas.translate(left - layout.getLineLeft(0) - offset, getBaseline() - layout.getLineBaseline(0));
        layout.draw(canvas);
        if (lineWidth > width) {
            canvas.translate(lineWidth + width / 3f, 0);
            layout.draw(canvas);
        }
        canvas.restoreToCount(saved);
        scheduleFrame();
    }
}
