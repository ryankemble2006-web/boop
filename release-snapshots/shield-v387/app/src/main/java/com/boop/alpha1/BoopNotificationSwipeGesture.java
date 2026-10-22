package com.boop.alpha1;

import android.view.MotionEvent;
import android.view.View;

final class BoopNotificationSwipeGesture {
    private static final float MIN_DISMISS_DISTANCE_DP = 72f;
    private static final float MIN_DOMINANCE = 1.25f;

    private BoopNotificationSwipeGesture() { }

    static boolean isDismiss(
            float startX,
            float startY,
            float endX,
            float endY,
            float densityScale) {
        float density = densityScale > 0f ? densityScale : 1f;
        float dx = Math.abs(endX - startX);
        float dy = Math.abs(endY - startY);
        float dominant = Math.max(dx, dy);
        float perpendicular = Math.min(dx, dy);
        return dominant / density >= MIN_DISMISS_DISTANCE_DP
                && dominant > perpendicular * MIN_DOMINANCE;
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
