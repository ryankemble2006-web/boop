package com.boop.shieldhdrdebug;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EventRingTest {
    @Test
    public void keepsOnlyNewestEvents() {
        String value = "one\ntwo\nthree";
        assertEquals("three\nfour", EventRing.append(value, "four", 2));
    }
}
