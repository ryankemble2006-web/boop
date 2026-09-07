package com.boop.alpha1;

/** Deliberate, single-pointer three-second hold, independent of the playful hold. */
final class BoopChatModeHold {
    static final long HOLD_MS = 3_000L;
    private boolean pending;
    private long startedAt;
    private float startX, startY, slop;

    void begin(long now, float x, float y, float touchSlop) {
        startedAt = now;
        startX = x;
        startY = y;
        slop = Math.max(0f, touchSlop);
        pending = Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(slop);
    }

    void move(float x, float y, int pointers) {
        if (pointers != 1 || !Float.isFinite(x) || !Float.isFinite(y)
                || Math.abs(x - startX) > slop || Math.abs(y - startY) > slop) {
            cancel();
        }
    }

    boolean tryOpen(long now) {
        if (!pending || now < startedAt || now - startedAt < HOLD_MS) return false;
        pending = false;
        return true;
    }

    void cancel() { pending = false; }
}
