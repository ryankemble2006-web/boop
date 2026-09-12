package com.boop.rally;
import android.view.Surface;
final class NativeBridge {
    static {System.loadLibrary("retro");System.loadLibrary("booprally");}
    static native void initialize();
    static native String run(String gamePath,String saveDirectory,String systemDirectory,String gameId);
    static native void surface(Surface surface);
    static native void key(int key,boolean down);
    static native void pause(boolean paused);
    static native void stop();
    static native String stats();
}
