package com.boop.alpha1;

import com.boop.eyes.EyeCatalogue;
import com.boop.eyes.EyeMotion;

/** Lab-only timeline. Samples the real catalogue without changing production clocks or preferences. */
public final class AnimationReviewTimeline {
    private EyeMotion.Clip clip = EyeCatalogue.find("idle");
    private double position;
    private boolean paused, slow;
    public void select(String id) { clip = EyeCatalogue.find(id); position = 0; paused = false; }
    public void seek(double milliseconds) {
        position = Double.isFinite(milliseconds) ? Math.max(0, Math.min(clip.duration, milliseconds)) : 0;
        paused = true;
    }
    public void step(double milliseconds) { seek(position + milliseconds); }
    public void halfBlink() { select("blink"); seek(36.6); }
    public void advance(double milliseconds) {
        if (paused || !Double.isFinite(milliseconds) || milliseconds <= 0) return;
        position += milliseconds * (slow ? .15 : 1);
        if (clip.loop) position %= clip.duration;
        else if (position >= clip.duration) { position = clip.duration; paused = true; }
    }
    public EyeMotion.Pose pose() { return clip.sample(position); }
    public EyeMotion.Clip clip() { return clip; }
    public double positionMs() { return position; }
    public boolean isPaused() { return paused; }
    public boolean isSlow() { return slow; }
    public void setPaused(boolean value) { paused = value; }
    public void setSlow(boolean value) { slow = value; }
}
