package com.boop.alpha1;

import org.junit.Test;
import static org.junit.Assert.*;

public final class BoopAssistantLaunchPolicyTest {
    @Test public void chosenRoleHolderAcceptsAssistAndVoiceAssist() {
        assertTrue(BoopAssistantLaunchPolicy.shouldLaunch("android.intent.action.ASSIST",true,true,false));
        assertTrue(BoopAssistantLaunchPolicy.shouldLaunch("android.intent.action.VOICE_ASSIST",true,true,false));
    }
    @Test public void unchosenOrUnassignedAppCannotStartListening() {
        assertFalse(BoopAssistantLaunchPolicy.shouldLaunch("android.intent.action.ASSIST",false,true,false));
        assertFalse(BoopAssistantLaunchPolicy.shouldLaunch("android.intent.action.ASSIST",true,false,false));
    }
    @Test public void recreationCannotReplayListening() {
        assertFalse(BoopAssistantLaunchPolicy.shouldLaunch("android.intent.action.ASSIST",true,true,true));
    }
    @Test public void unrelatedOrMissingActionsDoNotStartListening() {
        assertFalse(BoopAssistantLaunchPolicy.shouldLaunch(null,true,true,false));
        assertFalse(BoopAssistantLaunchPolicy.shouldLaunch("android.intent.action.MAIN",true,true,false));
    }
}
