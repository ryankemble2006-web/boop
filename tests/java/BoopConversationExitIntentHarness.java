package com.boop.alpha1;

public final class BoopConversationExitIntentHarness {
    public static void main(String[] args) {
        expect("You're welcome.", "thanks");
        expect("You're welcome.", "thank you Boop");
        expect("You're welcome.", "cheers");
        expect("You're welcome.", "ta");
        expect("You're welcome.", "much appreciated");
        expect("You're welcome.", "lovely, thanks Boop");

        expect("Goodnight.", "goodnight");
        expect("Goodnight.", "good night Boop");
        expect("Goodnight.", "night night");
        expect("Goodnight.", "go to sleep");
        expect("Goodnight.", "back to sleep now");
        expect("Goodnight.", "bedtime Boop");

        expect("Bye.", "bye");
        expect("Bye.", "goodbye Boop");
        expect("Okay.", "go away");
        expect("Okay.", "leave me alone");
        expect("Okay.", "off you go");
        expect("Okay.", "you can go now");
        expect("Okay.", "that's all");
        expect("Okay.", "stop listening");
        expect("Okay.", "fuck off");
        expect("Okay.", "piss off");

        expect(null, "turn the lights on");
        expect(null, "turn the lights off thanks");
        expect(null, "set the lights blue please");
        expect(null, "pause the music");
        expect(null, "what time is it");
    }

    private static void expect(String expected, String transcript) {
        String actual = BoopConversationExitIntent.replyFor(transcript);
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(
                    "For '" + transcript + "' expected " + expected + " but got " + actual);
        }
    }
}
