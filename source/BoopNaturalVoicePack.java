package com.boop.alpha1;

import android.content.Context;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.function.BooleanSupplier;

final class BoopNaturalVoicePack {
    static final String VERIFY_ERROR = "Download didn't verify. Try again.";
    private static final String ROOT_NAME = "boop-natural-voices";
    private static final String ARCHIVE_NAME = "natural-voices.tar.bz2.part";
    private static final String VERSION_MARKER = ".pack-version";

    static final class VerificationException extends IOException {
        VerificationException(String message) { super(message); }
    }

    private final BoopNaturalVoiceManifest manifest;
    private final File root;
    private final File archive;
    private final File staging;
    private final File active;
    private final File backup;

    BoopNaturalVoicePack(Context context, BoopNaturalVoiceManifest manifest) {
        this.manifest = manifest;
        this.root = new File(context.getNoBackupFilesDir(), ROOT_NAME);
        this.archive = new File(root, ARCHIVE_NAME);
        this.staging = new File(root, "staging");
        this.active = new File(root, "active");
        this.backup = new File(root, "backup");
    }

    synchronized boolean isInstalled() {
        try {
            return validateRequiredFiles(active) && manifest.version().equals(readVersion(active));
        } catch (IOException error) {
            return false;
        }
    }

    synchronized File activeDirectory() {
        return active;
    }

    synchronized File archiveFile() throws IOException {
        ensureRoot();
        return archive;
    }

    synchronized long usableSpace() throws IOException {
        ensureRoot();
        return root.getUsableSpace();
    }

    synchronized void clearIncomplete() {
        deleteRecursively(staging);
        if (archive.exists()) archive.delete();
    }

    synchronized void deleteArchive() {
        if (archive.exists()) archive.delete();
    }

    synchronized void installVerifiedArchive(File downloaded, BooleanSupplier cancelled) throws IOException {
        if (downloaded == null || !downloaded.isFile()) {
            throw new VerificationException(VERIFY_ERROR);
        }
        if (downloaded.length() != manifest.archiveSizeBytes()) {
            throw new VerificationException(VERIFY_ERROR);
        }
        if (!manifest.sha256().equals(sha256(downloaded))) {
            throw new VerificationException(VERIFY_ERROR);
        }
        if (cancelled != null && cancelled.getAsBoolean()) {
            throw new IOException("Natural voice download cancelled");
        }

        deleteRecursively(staging);
        if (!staging.mkdirs() && !staging.isDirectory()) {
            throw new IOException("Could not prepare natural voice staging folder");
        }
        extract(downloaded, cancelled);
        writeVersion(staging);
        if (!validateRequiredFiles(staging)) {
            deleteRecursively(staging);
            throw new VerificationException(VERIFY_ERROR);
        }
        if (cancelled != null && cancelled.getAsBoolean()) {
            deleteRecursively(staging);
            throw new IOException("Natural voice download cancelled");
        }
        activateStaging();
    }

    static String sha256(File file) throws IOException {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException impossible) {
            throw new IOException("SHA-256 unavailable", impossible);
        }
        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[64 * 1024];
            int count;
            while ((count = input.read(buffer)) >= 0) {
                if (count > 0) digest.update(buffer, 0, count);
            }
        }
        StringBuilder result = new StringBuilder(64);
        for (byte value : digest.digest()) {
            result.append(String.format(Locale.ROOT, "%02x", value & 0xff));
        }
        return result.toString();
    }

    static String safeEntryName(String entryName, String archiveRoot) {
        if (entryName == null) throw new IllegalArgumentException("Archive entry has no name");
        String name = entryName.replace('\\', '/');
        if (new File(name).isAbsolute() || name.startsWith("/")) {
            throw new IllegalArgumentException("Absolute archive path rejected");
        }
        while (name.startsWith("./")) name = name.substring(2);
        if (name.equals(archiveRoot) || name.equals(archiveRoot + "/")) return "";
        String prefix = archiveRoot + "/";
        if (!name.startsWith(prefix)) {
            throw new IllegalArgumentException("Archive entry is outside the expected root");
        }
        String relative = name.substring(prefix.length());
        if (relative.isEmpty()) return "";
        for (String part : relative.split("/")) {
            if (part.equals("..") || part.equals(".")) {
                throw new IllegalArgumentException("Archive traversal rejected");
            }
        }
        File relativeFile = new File(relative);
        if (relativeFile.isAbsolute() || relative.startsWith("../") || relative.contains("/../")) {
            throw new IllegalArgumentException("Archive traversal rejected");
        }
        return relative;
    }

    private void extract(File downloaded, BooleanSupplier cancelled) throws IOException {
        File stagingCanonical = staging.getCanonicalFile();
        String stagingPrefix = stagingCanonical.getPath() + File.separator;
        try (InputStream file = new BufferedInputStream(new FileInputStream(downloaded));
             BZip2CompressorInputStream bzip = new BZip2CompressorInputStream(file, true);
             TarArchiveInputStream tar = new TarArchiveInputStream(bzip)) {
            TarArchiveEntry entry;
            byte[] buffer = new byte[64 * 1024];
            while ((entry = tar.getNextTarEntry()) != null) {
                if (cancelled != null && cancelled.getAsBoolean()) {
                    throw new IOException("Natural voice download cancelled");
                }
                if (entry.isSymbolicLink() || entry.isLink()) {
                    throw new IOException("Archive link rejected");
                }
                String relative;
                try {
                    relative = safeEntryName(entry.getName(), manifest.archiveRoot());
                } catch (IllegalArgumentException unsafe) {
                    throw new IOException("Unsafe natural voice archive", unsafe);
                }
                if (relative.isEmpty()) continue;

                File target = new File(stagingCanonical, relative).getCanonicalFile();
                if (!target.getPath().startsWith(stagingPrefix)) {
                    throw new IOException("Archive path traversal rejected");
                }
                if (entry.isDirectory()) {
                    if (!target.mkdirs() && !target.isDirectory()) {
                        throw new IOException("Could not create archive directory");
                    }
                    continue;
                }
                if (!entry.isFile()) {
                    throw new IOException("Unsupported archive entry rejected");
                }
                File parent = target.getParentFile();
                if (parent != null && !parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Could not create archive parent");
                }
                try (OutputStream output = new BufferedOutputStream(new FileOutputStream(target))) {
                    int count;
                    while ((count = tar.read(buffer)) >= 0) {
                        if (cancelled != null && cancelled.getAsBoolean()) {
                            throw new IOException("Natural voice download cancelled");
                        }
                        if (count > 0) output.write(buffer, 0, count);
                    }
                }
            }
        }
    }

    private boolean validateRequiredFiles(File directory) throws IOException {
        if (directory == null || !directory.isDirectory()) return false;
        File canonicalRoot = directory.getCanonicalFile();
        String prefix = canonicalRoot.getPath() + File.separator;
        for (String required : manifest.requiredFiles()) {
            File file = new File(canonicalRoot, required).getCanonicalFile();
            if (!file.getPath().startsWith(prefix) || !file.exists()) return false;
            if ("espeak-ng-data".equals(required)) {
                String[] names = file.list();
                if (!file.isDirectory() || names == null || names.length == 0) return false;
            } else if (!file.isFile() || file.length() <= 0) {
                return false;
            }
        }
        return true;
    }

    private void activateStaging() throws IOException {
        deleteRecursively(backup);
        boolean movedOld = false;
        try {
            if (active.exists()) {
                move(active, backup);
                movedOld = true;
            }
            move(staging, active);
            deleteRecursively(backup);
        } catch (IOException error) {
            if (!active.exists() && movedOld && backup.exists()) {
                try { move(backup, active); } catch (IOException ignored) { }
            }
            throw error;
        }
    }

    private static void move(File from, File to) throws IOException {
        File parent = to.getParentFile();
        if (parent != null && !parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("Could not prepare voice pack parent");
        }
        try {
            Files.move(from.toPath(), to.toPath(),
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException notAtomic) {
            Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void writeVersion(File directory) throws IOException {
        Files.write(new File(directory, VERSION_MARKER).toPath(),
                manifest.version().getBytes(StandardCharsets.UTF_8));
    }

    private static String readVersion(File directory) throws IOException {
        File marker = new File(directory, VERSION_MARKER);
        if (!marker.isFile()) return null;
        return new String(Files.readAllBytes(marker.toPath()), StandardCharsets.UTF_8).trim();
    }

    private void ensureRoot() throws IOException {
        if (!root.mkdirs() && !root.isDirectory()) {
            throw new IOException("Could not prepare natural voice storage");
        }
    }

    private static void deleteRecursively(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) deleteRecursively(child);
            }
        }
        file.delete();
    }
}
