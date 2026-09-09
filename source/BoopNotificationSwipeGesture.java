package com.boop.alpha1;

import android.view.MotionEvent;
import android.view.View;

final class BoopNotificationSwipeGesture {
    private static final float MIN_DISMISS_DISTANCE_DP = 72f;
    private static final float MIN_HORIZONTAL_DOMINANCE = 1.5f;

    private BoopNotificationSwipeGesture() { }

    static boolean isDismiss(
            float startX,
            float startY,
            float endX,
            float endY,
            float densityScale) {
        float density = densityScale > 0f ? densityScale : 1f;
        float dx = endX - startX;
        float dy = endY - startY;
        float horizontal = Math.abs(dx);
        float vertical = Math.abs(dy);
        return horizontal / density >= MIN_DISMISS_DISTANCE_DP
                && horizontal >= vertical * MIN_HORIZONTAL_DOMINANCE;
    }

    static void attach(View view, Runnable onDismiss) {
        if (view == null || onDismiss == null) return;
        final float density = view.getResources().getDisplayMetrics().density;
        view.setOnTouchListener(new View.OnTouchListener() {
            private float startX;
            private float startY;
            private boolean tracking;

            @Override
            public boolean onTouch(View touched, MotionEvent event) {
                if (event == null) return false;
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        tracking = true;
                        return false;
                    case MotionEvent.ACTION_UP:
                        if (!tracking) return false;
                        tracking = false;
                        if (!isDismiss(
                                startX,
                                startY,
                                event.getX(),
                                event.getY(),
                                density)) {
                            return false;
                        }
                        touched.setPressed(false);
                        onDismiss.run();
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        tracking = false;
                        return false;
                    default:
                        return false;
                }
            }
        });
    }
}
