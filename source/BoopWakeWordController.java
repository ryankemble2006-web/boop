package com.boop.alpha1;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;

final class BoopWakeWordController {
    interface Listener {
        void onWakeDetected(BoopWakeAudioSession session, long detectedAtMs);
        void onWakeFailure(String message);
    }

    private static final String TAG = "BOOP-Wake";
    private static final int SAMPLE_RATE_HZ = 16_000;
    private static final int READ_SAMPLES = 1_600;
    private static final int PRE_ROLL_SAMPLES = 16_000;
    private static final long COMMAND_WINDOW_MS = 3_000L;

    private final Context appContext;
    private final Listener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final BoopWakeTriggerGate triggerGate = new BoopWakeTriggerGate();
    private final Object lock = new Object();

    private BoopSherpaWakeSpotter spotter;
    private BoopPcmRingBuffer preRoll;
    private AudioRecord audioRecord;
    private Thread worker;
    private ParcelFileDescriptor.AutoCloseOutputStream commandWriter;
    private volatile boolean running;
    private volatile boolean commandCapture;
    private volatile long commandDeadlineMs = -1L;
    private boolean failureReported;

    BoopWakeWordController(Context context, Listener listener) {
        this.appContext = context.getApplicationContext();
        this.listener = listener;
    }

    boolean arm() {
        synchronized (lock) {
            if (running) {
                return true;
            }
        }

        AudioRecord createdRecord = null;
        try {
            synchronized (lock) {
                if (spotter == null) {
                    spotter = new BoopSherpaWakeSpotter(appContext);
                }
                preRoll = new BoopPcmRingBuffer(PRE_ROLL_SAMPLES);
            }

            int minBytes = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE_HZ,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            if (minBytes <= 0) {
                throw new IllegalStateException("Unsupported wake microphone format");
            }

            int recordBufferBytes = Math.max(minBytes, READ_SAMPLES * 4);
            createdRecord = new AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    SAMPLE_RATE_HZ,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    recordBufferBytes);
            if (createdRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                throw new IllegalStateException("Wake microphone did not initialize");
            }

            createdRecord.startRecording();
            if (createdRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                throw new IllegalStateException("Wake microphone did not start");
            }

            Thread createdWorker;
            synchronized (lock) {
                if (running) {
                    stopAndRelease(createdRecord);
                    return true;
                }
                audioRecord = createdRecord;
                commandCapture = false;
                commandDeadlineMs = -1L;
                failureReported = false;
                running = true;
                createdWorker = new Thread(this::audioLoop, "boop-wake-audio");
                worker = createdWorker;
            }
            createdWorker.start();
            return true;
        } catch (Throwable error) {
            stopAndRelease(createdRecord);
            cleanupAfterArmFailure();
            reportFailureOnce("Local wake word unavailable", error);
            return false;
        }
    }

    void suspendAll() {
        Thread threadToJoin;
        AudioRecord recordToStop;
        synchronized (lock) {
            running = false;
            commandCapture = false;
            commandDeadlineMs = -1L;
            closeCommandWriterLocked();
            recordToStop = audioRecord;
            threadToJoin = worker;
        }

        stopRecording(recordToStop);
        if (threadToJoin != null && threadToJoin != Thread.currentThread()) {
            try {
                threadToJoin.join(350L);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    void reloadSensitivity() {
        suspendAll();
        BoopSherpaWakeSpotter spotterToClose;
        synchronized (lock) {
            spotterToClose = spotter;
            spotter = null;
            preRoll = null;
        }
        if (spotterToClose != null) {
            try {
                spotterToClose.close();
            } catch (Throwable error) {
                Log.w(TAG, "Wake sensitivity reload failed", error);
            }
        }
    }

    void shutdown() {
        suspendAll();
        BoopSherpaWakeSpotter spotterToClose;
        synchronized (lock) {
            spotterToClose = spotter;
            spotter = null;
        }
        if (spotterToClose != null) {
            try {
                spotterToClose.close();
            } catch (Throwable error) {
                Log.w(TAG, "Wake spotter shutdown failed", error);
            }
        }
    }

    private void audioLoop() {
        short[] buffer = new short[READ_SAMPLES];
        boolean activationLogged = false;
        try {
            while (running) {
                AudioRecord record = audioRecord;
                if (record == null) {
                    break;
                }

                int count = record.read(buffer, 0, buffer.length);
                if (!running) {
                    break;
                }
                if (count == AudioRecord.ERROR_DEAD_OBJECT
                        || count == AudioRecord.ERROR_BAD_VALUE
                        || count == AudioRecord.ERROR_INVALID_OPERATION
                        || count == AudioRecord.ERROR) {
                    throw new IllegalStateException("Wake microphone read failed: " + count);
                }
                if (count <= 0) {
                    continue;
                }
                if (!activationLogged) {
                    Log.i(TAG, "Wake microphone armed");
                    activationLogged = true;
                }

                if (commandCapture) {
                    if (SystemClock.elapsedRealtime() >= commandDeadlineMs) {
                        finishCommandCapture();
                        break;
                    }
                    writeCommandPcm(buffer, count);
                    continue;
                }

                BoopPcmRingBuffer ring = preRoll;
                BoopSherpaWakeSpotter localSpotter = spotter;
                if (ring == null || localSpotter == null) {
                    continue;
                }

                ring.write(buffer, count);
                if (localSpotter.accept(buffer, count)) {
                    long detectedAtMs = SystemClock.elapsedRealtime();
                    if (triggerGate.accept(detectedAtMs)) {
                        beginCommandCapture(detectedAtMs, ring.snapshot());
                    }
                }
            }
        } catch (Throwable error) {
            if (running) {
                reportFailureOnce("Local wake word unavailable", error);
            }
        } finally {
            AudioRecord toRelease;
            synchronized (lock) {
                running = false;
                commandCapture = false;
                commandDeadlineMs = -1L;
                closeCommandWriterLocked();
                toRelease = audioRecord;
                audioRecord = null;
                worker = null;
            }
            stopAndRelease(toRelease);
        }
    }

    private void beginCommandCapture(long detectedAtMs, short[] preRollSnapshot) throws IOException {
        ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
        ParcelFileDescriptor readSide = pipe[0];
        ParcelFileDescriptor.AutoCloseOutputStream writer =
                new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1]);
        boolean handedOff = false;
        try {
            writeLittleEndianPcm(writer, preRollSnapshot, preRollSnapshot.length);
            synchronized (lock) {
                if (!running) {
                    return;
                }
                commandWriter = writer;
                commandCapture = true;
                commandDeadlineMs = detectedAtMs + COMMAND_WINDOW_MS;
            }

            BoopWakeAudioSession session = new BoopWakeAudioSession(
                    readSide,
                    this::finishCommandCapture);
            handedOff = true;
            mainHandler.post(() -> listener.onWakeDetected(session, detectedAtMs));
        } finally {
            if (!handedOff) {
                try {
                    readSide.close();
                } catch (IOException ignored) {
                    // Nothing owns the read side yet.
                }
                synchronized (lock) {
                    if (commandWriter == writer) {
                        commandWriter = null;
                    }
                }
                try {
                    writer.close();
                } catch (IOException ignored) {
                    // Best effort during failed handoff.
                }
            }
        }
    }

    private void writeCommandPcm(short[] samples, int count) throws IOException {
        ParcelFileDescriptor.AutoCloseOutputStream writer;
        synchronized (lock) {
            writer = commandWriter;
        }
        if (writer == null) {
            finishCommandCapture();
            return;
        }
        writeLittleEndianPcm(writer, samples, count);
    }

    private static void writeLittleEndianPcm(
            ParcelFileDescriptor.AutoCloseOutputStream writer,
            short[] samples,
            int count) throws IOException {
        int bounded = Math.min(count, samples.length);
        byte[] bytes = new byte[bounded * 2];
        for (int i = 0; i < bounded; i++) {
            short sample = samples[i];
            bytes[i * 2] = (byte) (sample & 0xff);
            bytes[i * 2 + 1] = (byte) ((sample >>> 8) & 0xff);
        }
        writer.write(bytes);
    }

    private void finishCommandCapture() {
        AudioRecord recordToStop;
        synchronized (lock) {
            commandCapture = false;
            commandDeadlineMs = -1L;
            running = false;
            closeCommandWriterLocked();
            recordToStop = audioRecord;
        }
        stopRecording(recordToStop);
    }

    private void cleanupAfterArmFailure() {
        synchronized (lock) {
            running = false;
            commandCapture = false;
            commandDeadlineMs = -1L;
            audioRecord = null;
            worker = null;
            closeCommandWriterLocked();
        }
    }

    private void reportFailureOnce(String message, Throwable error) {
        synchronized (lock) {
            if (failureReported) {
                return;
            }
            failureReported = true;
        }
        Log.e(TAG, message, error);
        mainHandler.post(() -> listener.onWakeFailure(message));
    }

    private void closeCommandWriterLocked() {
        ParcelFileDescriptor.AutoCloseOutputStream writer = commandWriter;
        commandWriter = null;
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ignored) {
                // Closing the writer only supplies EOF to Android recognition.
            }
        }
    }

    private static void stopRecording(AudioRecord record) {
        if (record == null) {
            return;
        }
        try {
            if (record.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                record.stop();
            }
        } catch (IllegalStateException ignored) {
            // Another teardown path may already have stopped it.
        }
    }

    private static void stopAndRelease(AudioRecord record) {
        if (record == null) {
            return;
        }
        stopRecording(record);
        try {
            record.release();
        } catch (Throwable ignored) {
            // Release is idempotent from BOOP's point of view.
        }
    }
}
