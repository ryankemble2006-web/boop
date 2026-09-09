package com.boop.alpha1;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

final class BoopNaturalVoiceDownloader {
    interface Listener {
        void onStatus(String status);
        void onProgress(long downloadedBytes, long totalBytes);
        void onInstallProgress(int percent);
        void onReady();
        void onCancelled();
        void onError(String message);
    }

    private static final String GENERIC_ERROR = "Natural voices didn't download. Try again.";
    private static final String SPACE_ERROR = "I need more free space for natural voices.";

    private static final class DownloadReceipt {
        final long downloadedBytes;
        final String sha256;

        DownloadReceipt(long downloadedBytes, String sha256) {
            this.downloadedBytes = downloadedBytes;
            this.sha256 = sha256;
        }
    }

    private final OkHttpClient client;
    private final BoopNaturalVoiceManifest manifest;
    private final BoopNaturalVoicePack pack;
    private final Object lock = new Object();

    private Call activeCall;
    private AtomicBoolean cancelled;
    private AtomicBoolean terminal;
    private Listener listener;
    private boolean running;

    BoopNaturalVoiceDownloader(
            OkHttpClient client,
            BoopNaturalVoiceManifest manifest,
            BoopNaturalVoicePack pack) {
        this.client = client;
        this.manifest = manifest;
        this.pack = pack;
    }

    boolean start(Listener requestedListener) {
        if (requestedListener == null) return false;
        final AtomicBoolean runCancelled = new AtomicBoolean(false);
        final AtomicBoolean runTerminal = new AtomicBoolean(false);
        final Call call;
        try {
            URI uri = URI.create(manifest.url());
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                requestedListener.onError(GENERIC_ERROR);
                return false;
            }
            if (pack.usableSpace() < manifest.minimumFreeBytes()) {
                requestedListener.onError(SPACE_ERROR);
                return false;
            }
            pack.clearIncomplete();
            Request request = new Request.Builder().url(manifest.url()).get().build();
            call = client.newCall(request);
        } catch (Throwable error) {
            requestedListener.onError(GENERIC_ERROR);
            return false;
        }

        synchronized (lock) {
            if (running) return false;
            running = true;
            activeCall = call;
            cancelled = runCancelled;
            terminal = runTerminal;
            listener = requestedListener;
        }

        requestedListener.onStatus("Downloading natural voices…");
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call failedCall, IOException error) {
                if (runCancelled.get()) {
                    finishCancelled(runCancelled, runTerminal, requestedListener);
                } else {
                    finishError(runCancelled, runTerminal, requestedListener, GENERIC_ERROR);
                }
            }

            @Override
            public void onResponse(Call completedCall, Response response) {
                try (Response ignored = response) {
                    if (!response.isSuccessful()) {
                        throw new IOException("HTTP " + response.code());
                    }
                    ResponseBody body = response.body();
                    if (body == null) throw new IOException("Empty voice-pack response");
                    File destination = pack.archiveFile();
                    DownloadReceipt receipt = downloadBody(
                            body, destination, runCancelled, requestedListener);
                    if (runCancelled.get()) {
                        finishCancelled(runCancelled, runTerminal, requestedListener);
                        return;
                    }

                    // The exact downloaded bytes were hashed while they were written,
                    // so this verification is immediate rather than a second 350 MB read.
                    requestedListener.onStatus("Verifying natural voices…");
                    if (receipt.downloadedBytes != manifest.archiveSizeBytes()
                            || !manifest.sha256().equals(receipt.sha256)) {
                        throw new BoopNaturalVoicePack.VerificationException(
                                BoopNaturalVoicePack.VERIFY_ERROR);
                    }
                    if (runCancelled.get()) {
                        finishCancelled(runCancelled, runTerminal, requestedListener);
                        return;
                    }

                    requestedListener.onStatus("Installing natural voices…");
                    pack.installVerifiedArchive(
                            destination,
                            receipt.sha256,
                            runCancelled::get,
                            requestedListener::onInstallProgress);
                    if (runCancelled.get()) {
                        finishCancelled(runCancelled, runTerminal, requestedListener);
                        return;
                    }
                    pack.deleteArchive();
                    finishReady(runCancelled, runTerminal, requestedListener);
                } catch (BoopNaturalVoicePack.VerificationException verify) {
                    finishError(runCancelled, runTerminal, requestedListener,
                            BoopNaturalVoicePack.VERIFY_ERROR);
                } catch (IOException error) {
                    if (runCancelled.get()) {
                        finishCancelled(runCancelled, runTerminal, requestedListener);
                    } else {
                        finishError(runCancelled, runTerminal, requestedListener, GENERIC_ERROR);
                    }
                } catch (Throwable error) {
                    if (runCancelled.get()) {
                        finishCancelled(runCancelled, runTerminal, requestedListener);
                    } else {
                        finishError(runCancelled, runTerminal, requestedListener, GENERIC_ERROR);
                    }
                }
            }
        });
        return true;
    }

    void cancel() {
        AtomicBoolean runCancelled;
        Listener runListener;
        Call call;
        synchronized (lock) {
            if (!running) return;
            runCancelled = cancelled;
            runListener = listener;
            call = activeCall;
        }
        if (runCancelled != null) runCancelled.set(true);
        if (runListener != null) runListener.onStatus("Cancelling natural voices…");
        if (call != null) call.cancel();
        // Do not call pack cleanup here. During extraction the pack monitor is
        // owned by the worker; synchronous cleanup would block the UI on Cancel.
        // The worker observes runCancelled and performs terminal cleanup itself.
    }

    boolean isRunning() {
        synchronized (lock) {
            return running;
        }
    }

    private DownloadReceipt downloadBody(
            ResponseBody body,
            File destination,
            AtomicBoolean runCancelled,
            Listener runListener) throws IOException {
        long total = body.contentLength() > 0 ? body.contentLength() : manifest.archiveSizeBytes();
        long downloaded = 0L;
        long lastReported = -1L;
        MessageDigest digest = newSha256Digest();
        try (InputStream input = new BufferedInputStream(body.byteStream());
             OutputStream output = new BufferedOutputStream(new FileOutputStream(destination))) {
            byte[] buffer = new byte[64 * 1024];
            int count;
            while ((count = input.read(buffer)) >= 0) {
                if (runCancelled.get()) throw new IOException("Natural voice download cancelled");
                if (count == 0) continue;
                output.write(buffer, 0, count);
                digest.update(buffer, 0, count);
                downloaded += count;
                if (lastReported < 0 || downloaded - lastReported >= 1024 * 1024L) {
                    lastReported = downloaded;
                    runListener.onProgress(downloaded, total);
                }
            }
        }
        runListener.onProgress(downloaded, total);
        return new DownloadReceipt(downloaded, hex(digest.digest()));
    }

    private static MessageDigest newSha256Digest() throws IOException {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException impossible) {
            throw new IOException("SHA-256 unavailable", impossible);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            result.append(String.format(Locale.ROOT, "%02x", value & 0xff));
        }
        return result.toString();
    }

    private void finishReady(
            AtomicBoolean runCancelled,
            AtomicBoolean runTerminal,
            Listener runListener) {
        if (!runTerminal.compareAndSet(false, true)) return;
        clearRun(runCancelled, runTerminal, runListener);
        runListener.onReady();
    }

    private void finishCancelled(
            AtomicBoolean runCancelled,
            AtomicBoolean runTerminal,
            Listener runListener) {
        if (!runTerminal.compareAndSet(false, true)) return;
        runCancelled.set(true);
        pack.clearIncomplete();
        clearRun(runCancelled, runTerminal, runListener);
        runListener.onCancelled();
    }

    private void finishError(
            AtomicBoolean runCancelled,
            AtomicBoolean runTerminal,
            Listener runListener,
            String message) {
        if (!runTerminal.compareAndSet(false, true)) return;
        pack.clearIncomplete();
        clearRun(runCancelled, runTerminal, runListener);
        runListener.onError(message);
    }

    private void clearRun(
            AtomicBoolean runCancelled,
            AtomicBoolean runTerminal,
            Listener runListener) {
        synchronized (lock) {
            if (cancelled != runCancelled || terminal != runTerminal || listener != runListener) return;
            activeCall = null;
            cancelled = null;
            terminal = null;
            listener = null;
            running = false;
        }
    }
}
