package com.boop.lyricstest;

import android.os.Build;

/** Test artifacts must never run on Shield or a physical phone. */
final class EmulatorOnly {
    static void require() {
        if (!"ranchu".equals(Build.HARDWARE) && !"goldfish".equals(Build.HARDWARE))
            throw new SecurityException("This is an Android emulator-only test fixture.");
    }
}
