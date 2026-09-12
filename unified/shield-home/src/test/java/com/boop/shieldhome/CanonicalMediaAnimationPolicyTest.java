package com.boop.shieldhome;

public final class CanonicalMediaAnimationPolicyTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check("music".equals(CanonicalMediaAnimationPolicy.steadyClip(
                NowPlayingPuppetPolicy.Mode.GROOVE)), "playing uses music");
        check("media_pause".equals(CanonicalMediaAnimationPolicy.steadyClip(
                NowPlayingPuppetPolicy.Mode.UPSET)), "paused uses media_pause");
        check("idle".equals(CanonicalMediaAnimationPolicy.steadyClip(
                NowPlayingPuppetPolicy.Mode.REST)), "rest uses idle");
        check("idle".equals(CanonicalMediaAnimationPolicy.steadyClip(
                NowPlayingPuppetPolicy.Mode.HIDDEN)), "hidden falls back idle");
        check("track_change".equals(CanonicalMediaAnimationPolicy.trackChangeClip()),
                "track change clip");
        System.out.println("CanonicalMediaAnimationPolicyTest PASS");
    }
}
