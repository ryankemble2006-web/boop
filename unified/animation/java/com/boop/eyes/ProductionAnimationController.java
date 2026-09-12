package com.boop.eyes;

/** Production owner for the finished canonical animation catalogue. */
public final class ProductionAnimationController {
    private final EyeMotion.Controller motion;
    private String steadyClipId;
    private String activeClipId;
    private long transientStartedMs = -1L;
    private float transientReturnBlendMs;

    public ProductionAnimationController(String initialClipId, long nowMs, long seed) {
        EyeMotion.Clip initial = require(initialClipId);
        steadyClipId = initial.id;
        activeClipId = initial.id;
        motion = new EyeMotion.Controller(initial, nowMs, seed);
    }

    public static String[] clipIds() {
        String[] ids = new String[EyeCatalogue.ALL.length];
        for (int i = 0; i < EyeCatalogue.ALL.length; i++) ids[i] = EyeCatalogue.ALL[i].id;
        return ids;
    }

    public static boolean supports(String id) {
        if (id == null) return false;
        for (EyeMotion.Clip clip : EyeCatalogue.ALL) if (clip.id.equals(id)) return true;
        return false;
    }

    public void setState(String id, long nowMs, float blendMs) {
        EyeMotion.Clip clip = require(id);
        steadyClipId = clip.id;
        activeClipId = clip.id;
        transientStartedMs = -1L;
        motion.select(clip, nowMs, blendMs);
    }

    public void trigger(String id, long nowMs, float blendMs) {
        EyeMotion.Clip clip = require(id);
        if (clip.loop) throw new IllegalArgumentException("One-shot clip required: " + id);
        activeClipId = clip.id;
        transientStartedMs = nowMs;
        transientReturnBlendMs = blendMs;
        motion.select(clip, nowMs, blendMs);
    }

    public EyeMotion.Pose sample(long nowMs) {
        if (transientStartedMs >= 0L) {
            EyeMotion.Clip transientClip = require(activeClipId);
            if (nowMs - transientStartedMs >= transientClip.duration) {
                activeClipId = steadyClipId;
                transientStartedMs = -1L;
                motion.select(require(steadyClipId), nowMs, transientReturnBlendMs);
            }
        }
        return motion.sample(nowMs);
    }

    public void pause(long nowMs) { motion.pause(nowMs); }
    public void resume(long nowMs) { motion.resume(nowMs); }
    public void setAmbientBlinkEnabled(boolean enabled) { motion.setAmbientBlinkEnabled(enabled); }
    public String activeClipId() { return activeClipId; }
    public String steadyClipId() { return steadyClipId; }

    private static EyeMotion.Clip require(String id) {
        if (id != null) {
            for (EyeMotion.Clip clip : EyeCatalogue.ALL) if (clip.id.equals(id)) return clip;
        }
        throw new IllegalArgumentException("Unknown canonical clip: " + id);
    }
}
