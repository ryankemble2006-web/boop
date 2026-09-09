package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;
import org.junit.Test;

public final class NowPlayingPuppetBlinkTest {
    @Test public void blinkClosesQuicklyAndReopensSoftly() {
        assertEquals(1f, openness(0f), 0.0001f);
        assertTrue(openness(0.2f) < 0.55f);
        assertTrue(openness(0.4f) <= 0.06f);
        assertTrue(openness(0.7f) > openness(0.4f));
        assertEquals(1f, openness(1f), 0.0001f);
    }

    @Test public void nextBlinkDelayStaysInsideNaturalWindow() {
        Random random = new Random(42L);
        for (int i = 0; i < 200; i++) {
            long delay = nextDelayMillis(random);
            assertTrue(delay >= 3_000L);
            assertTrue(delay <= 7_000L);
        }
    }

    @Test public void doubleBlinkIsOccasionalRatherThanConstant() {
        Random random = new Random(99L);
        int doubles = 0;
        for (int i = 0; i < 1_000; i++) {
            if (shouldDoubleBlink(random)) doubles++;
        }
        assertTrue(doubles >= 120);
        assertTrue(doubles <= 240);
    }

    private static float openness(float progress) {
        return (Float) invoke("openness", new Class<?>[]{float.class}, progress);
    }

    private static long nextDelayMillis(Random random) {
        return (Long) invoke("nextDelayMillis", new Class<?>[]{Random.class}, random);
    }

    private static boolean shouldDoubleBlink(Random random) {
        return (Boolean) invoke("shouldDoubleBlink", new Class<?>[]{Random.class}, random);
    }

    private static Object invoke(String name, Class<?>[] parameterTypes, Object... args) {
        try {
            Class<?> type = Class.forName("com.boop.shieldhome.NowPlayingPuppetBlink");
            Method method = type.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method.invoke(null, args);
        } catch (ClassNotFoundException | NoSuchMethodException error) {
            throw new AssertionError("Natural blink behavior is not implemented yet", error);
        } catch (IllegalAccessException | InvocationTargetException error) {
            throw new AssertionError("Natural blink behavior could not be invoked", error);
        }
    }
}
