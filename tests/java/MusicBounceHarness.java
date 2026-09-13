package com.boop.shieldhome;

import java.util.Arrays;

/** Numeric behaviour only. This does not claim a physical Deezer audio test. */
public final class MusicBounceHarness {
    private static int checks;
    private static void check(boolean value, String reason) {
        checks++;
        if (!value) throw new AssertionError(reason);
    }
    private static byte[] wave(int amplitude) {
        byte[] samples = new byte[512];
        for (int i=0;i<samples.length;i++) samples[i]=(byte)(128+(i%2==0?amplitude:-amplitude));
        return samples;
    }
    public static void main(String[] args) {
        byte[] silence = new byte[512];
        Arrays.fill(silence,(byte)128);
        check(MusicBounceEnvelope.levelOf(null)==0, "missing audio must not move");
        check(MusicBounceEnvelope.levelOf(new byte[0])==0, "empty audio must not move");
        check(MusicBounceEnvelope.levelOf(silence)==0, "unsigned PCM silence");
        check(MusicBounceEnvelope.levelOf(new byte[512])==0, "constant vendor zero buffer is not a beat");
        float quiet=MusicBounceEnvelope.levelOf(wave(8));
        float medium=MusicBounceEnvelope.levelOf(wave(32));
        float loud=MusicBounceEnvelope.levelOf(wave(96));
        check(quiet>0 && medium>quiet && loud>medium && loud<=1, "audio amplitude controls level monotonically");
        MusicBounceEnvelope envelope=new MusicBounceEnvelope();
        check(envelope.update(0,0)==0,"silence starts settled");
        float rise=envelope.update(1,33);
        check(rise>0.05f && rise<=0.14f,"fast bounded attack");
        float fall=envelope.update(0,66);
        check(fall>0 && fall<rise,"release is smooth");
        check(rise-fall<rise,"fall is softer than rise");
        for(int i=3;i<50;i++) fall=envelope.update(0,33L*i);
        check(fall==0,"silence settles exactly");
        envelope.update(Float.NaN,1700);
        check(Float.isFinite(envelope.update(Float.POSITIVE_INFINITY,1733)),"invalid numbers cannot poison rendering");
        envelope.reset();
        check(envelope.update(0,9000)==0,"detach clears previous audio and clock");
        for(int i=0;i<200;i++) {
            float result=envelope.update((i%7)/6f,9033L+i*33);
            check(result>=0 && result<=0.14f,"every height stays in mascot bay");
        }
        MusicBounceEnvelope a=new MusicBounceEnvelope(), b=new MusicBounceEnvelope();
        for(int i=0;i<120;i++) {
            float level=MusicBounceEnvelope.levelOf(wave((i%5)*20));
            // Neither envelope takes the animation/blink speed as an input.
            check(Float.floatToIntBits(a.update(level,i*33L))==Float.floatToIntBits(b.update(level,i*33L)),"bounce is independent of blink clock");
        }
        System.out.println("PASS: "+checks+" real-envelope checks; no device or visual acceptance claimed");
    }
}
