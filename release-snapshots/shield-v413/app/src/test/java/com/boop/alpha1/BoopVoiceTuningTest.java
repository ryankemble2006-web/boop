package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BoopVoiceTuningTest {
    @Test
    public void defaultsRoundTripThroughSliders() {
        int pitchProgress = BoopVoiceTuning.progressFromPitch(BoopVoiceTuning.DEFAULT_PITCH);
        int rateProgress = BoopVoiceTuning.progressFromRate(BoopVoiceTuning.DEFAULT_RATE);

        assertEquals(BoopVoiceTuning.DEFAULT_PITCH,
                BoopVoiceTuning.pitchFromProgress(pitchProgress), 0.002f);
        assertEquals(BoopVoiceTuning.DEFAULT_RATE,
                BoopVoiceTuning.rateFromProgress(rateProgress), 0.002f);
    }

    @Test
    public void sliderEndpointsStayInsidePuppetBounds() {
        assertEquals(BoopVoiceTuning.MIN_PITCH, BoopVoiceTuning.pitchFromProgress(-100), 0.0001f);
        assertEquals(BoopVoiceTuning.MAX_PITCH, BoopVoiceTuning.pitchFromProgress(5000), 0.0001f);
        assertEquals(BoopVoiceTuning.MIN_RATE, BoopVoiceTuning.rateFromProgress(-100), 0.0001f);
        assertEquals(BoopVoiceTuning.MAX_RATE, BoopVoiceTuning.rateFromProgress(5000), 0.0001f);
    }

    @Test
    public void mappingIsMonotonic() {
        assertTrue(BoopVoiceTuning.pitchFromProgress(250) < BoopVoiceTuning.pitchFromProgress(750));
        assertTrue(BoopVoiceTuning.rateFromProgress(250) < BoopVoiceTuning.rateFromProgress(750));
    }
}
