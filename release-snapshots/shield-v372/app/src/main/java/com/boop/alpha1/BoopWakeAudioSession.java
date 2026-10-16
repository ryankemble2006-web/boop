package com.boop.alpha1;

import android.os.ParcelFileDescriptor;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

final class BoopWakeAudioSession implements AutoCloseable {
    private final ParcelFileDescriptor source;
    private final Runnable finishCapture;
    private final AtomicBoolean captureFinished = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    BoopWakeAudioSession(ParcelFileDescriptor source, Runnable finishCapture) {
        this.source = source;
        this.finishCapture = finishCapture;
    }

    ParcelFileDescriptor audioSource() {
        return source;
    }

    void finishCapture() {
        if (captureFinished.compareAndSet(false, true)) {
            finishCapture.run();
        }
    }

    @Override
    public void close() {
        finishCapture();
        if (closed.compareAndSet(false, true)) {
            try {
                source.close();
            } catch (IOException ignored) {
                // Capture teardown is best-effort; recognition owns the user-facing error path.
            }
        }
    }
}
