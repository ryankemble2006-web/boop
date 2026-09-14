package com.boop.shieldhome;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import com.boop.shieldturbo.power.AdbWire;
import java.io.File;
import java.io.IOException;

/** Fixed read-only diagnostic via this app's already-approved loopback ADB identity. */
final class DirectMusicSource {
    private final File identityFile;
    private volatile AdbWire active;
    private volatile boolean closed;
    private long retryAt;
    private boolean reported;

    DirectMusicSource(Context context) {
        identityFile = new File(context.getNoBackupFilesDir(), "boop-unified-local-adb.key");
    }

    float read() {
        if (closed || SystemClock.uptimeMillis() < retryAt) return -1f;
        try {
            if (!identityFile.isFile()) return -1f; // Never generate a key or trigger an approval prompt.
            AdbWire adb = active;
            if (adb == null) {
                adb = new AdbWire();
                active = adb;
                if (closed) { disconnect(); return -1f; }
                adb.connect(5555, AdbWire.identity(identityFile), 1500, () -> {}, false);
                AdbWire.Result uid = adb.execute("id -u", 1000);
                if (uid.exitCode != 0 || !uid.output.trim().equals("2000"))
                    throw new IOException("Not an ADB shell");
            }
            long start = SystemClock.uptimeMillis();
            AdbWire.Result dump = adb.execute("dumpsys media.audio_flinger", 1000);
            if (closed) return -1f;
            if (dump.exitCode != 0) throw new IOException("Audio diagnostic unavailable");
            float level = DirectMusicLevels.parse(dump.output, System.currentTimeMillis());
            if (level >= 0f && !reported) {
                reported = true;
                Log.i("BOOP-MusicBounce", "Fresh direct-output power received; readMs="
                        + (SystemClock.uptimeMillis() - start));
            }
            return level;
        } catch (Exception failure) {
            disconnect();
            retryAt = SystemClock.uptimeMillis() + 5000L;
            return -1f;
        }
    }

    void close() { closed = true; disconnect(); }
    private void disconnect() {
        AdbWire old = active;
        active = null;
        if (old != null) try { old.close(); } catch (IOException ignored) { }
    }
}
