package com.boop.alpha1;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class BoopNaturalVoiceManifest {
    static final String ASSET_PATH = "boop-natural-voices/manifest.json";

    static final class VoiceSpec {
        private final String name;
        private final String key;
        private final int sid;

        VoiceSpec(String name, String key, int sid) {
            this.name = name;
            this.key = key;
            this.sid = sid;
        }

        String name() { return name; }
        String key() { return key; }
        int sid() { return sid; }
    }

    private final String version;
    private final String archiveRoot;
    private final String url;
    private final String sha256;
    private final long archiveSizeBytes;
    private final long minimumFreeBytes;
    private final List<String> requiredFiles;
    private final List<VoiceSpec> voices;

    private BoopNaturalVoiceManifest(
            String version,
            String archiveRoot,
            String url,
            String sha256,
            long archiveSizeBytes,
            long minimumFreeBytes,
            List<String> requiredFiles,
            List<VoiceSpec> voices) {
        this.version = version;
        this.archiveRoot = archiveRoot;
        this.url = url;
        this.sha256 = sha256;
        this.archiveSizeBytes = archiveSizeBytes;
        this.minimumFreeBytes = minimumFreeBytes;
        this.requiredFiles = Collections.unmodifiableList(new ArrayList<>(requiredFiles));
        this.voices = Collections.unmodifiableList(new ArrayList<>(voices));
    }

    static BoopNaturalVoiceManifest load(Context context) throws IOException {
        try (InputStream input = context.getAssets().open(ASSET_PATH);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) >= 0) {
                output.write(buffer, 0, count);
            }
            return parseJson(output.toString(StandardCharsets.UTF_8.name()));
        }
    }

    static BoopNaturalVoiceManifest parseJson(String json) {
        try {
            JSONObject root = new JSONObject(json);
            String version = requiredString(root, "version");
            String archiveRoot = requiredString(root, "archiveRoot");
            String url = requiredString(root, "url");
            String sha256 = requiredString(root, "sha256").toLowerCase();
            long archiveSizeBytes = root.getLong("archiveSizeBytes");
            long minimumFreeBytes = root.getLong("minimumFreeBytes");

            if (!url.startsWith("https://")) {
                throw new IllegalArgumentException("Natural voice pack URL must use HTTPS");
            }
            if (!sha256.matches("[0-9a-f]{64}")) {
                throw new IllegalArgumentException("Natural voice pack SHA-256 is invalid");
            }
            if (archiveSizeBytes <= 0 || minimumFreeBytes <= archiveSizeBytes) {
                throw new IllegalArgumentException("Natural voice pack size policy is invalid");
            }

            JSONArray requiredArray = root.getJSONArray("requiredFiles");
            List<String> requiredFiles = new ArrayList<>();
            for (int i = 0; i < requiredArray.length(); i++) {
                String value = requiredArray.getString(i).trim();
                if (value.isEmpty()) {
                    throw new IllegalArgumentException("Natural voice required path is empty");
                }
                requiredFiles.add(value);
            }

            JSONArray voicesArray = root.getJSONArray("voices");
            List<VoiceSpec> voices = new ArrayList<>();
            for (int i = 0; i < voicesArray.length(); i++) {
                JSONObject voice = voicesArray.getJSONObject(i);
                voices.add(new VoiceSpec(
                        requiredString(voice, "name"),
                        requiredString(voice, "key"),
                        voice.getInt("sid")));
            }
            if (voices.size() != 4) {
                throw new IllegalArgumentException("BOOP natural voices must contain exactly four choices");
            }

            return new BoopNaturalVoiceManifest(
                    version,
                    archiveRoot,
                    url,
                    sha256,
                    archiveSizeBytes,
                    minimumFreeBytes,
                    requiredFiles,
                    voices);
        } catch (JSONException error) {
            throw new IllegalArgumentException("Natural voice manifest is invalid", error);
        }
    }

    private static String requiredString(JSONObject object, String key) throws JSONException {
        String value = object.getString(key).trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Natural voice manifest field is empty: " + key);
        }
        return value;
    }

    String version() { return version; }
    String archiveRoot() { return archiveRoot; }
    String url() { return url; }
    String sha256() { return sha256; }
    long archiveSizeBytes() { return archiveSizeBytes; }
    long minimumFreeBytes() { return minimumFreeBytes; }
    List<String> requiredFiles() { return requiredFiles; }
    List<VoiceSpec> voices() { return voices; }

    VoiceSpec findVoice(String key) {
        if (key == null) return null;
        for (VoiceSpec voice : voices) {
            if (key.equals(voice.key())) return voice;
        }
        return null;
    }
}
