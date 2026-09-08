package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/** Non-visual ownership contract for the Now Playing puppet stage. */
public final class NowPlayingPuppetHostContractTest {
    @Test public void nowPlayingCardOwnsPuppetInsteadOfActivityOverlay() throws Exception {
        assertEquals(
                ShieldNowPlayingPuppetView.class,
                ShieldNowPlayingView.class.getDeclaredField("puppetView").getType());

        try {
            ShieldLauncherActivity.class.getDeclaredField("nowPlayingPuppetView");
            fail("Activity must not own a separate Now Playing puppet overlay");
        } catch (NoSuchFieldException expected) {
            // Correct: the puppet belongs to the card's clipped stage.
        }
    }
}
