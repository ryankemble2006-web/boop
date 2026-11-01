package com.boop.shieldoverlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class SecureCredentialStore {
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "boop_ha_refresh_v1";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String PREFS_NAME = "boop_ha_credential";
    private static final String PREF_BLOB = "credential_blob_v1";
    private static final int GCM_TAG_BITS = 128;

    private final SharedPreferences preferences;

    public SecureCredentialStore(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context is required");
        }
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void save(StoredHomeAssistantCredential credential)
            throws GeneralSecurityException, IOException {
        if (credential == null) {
            throw new IllegalArgumentException("Credential is required");
        }

        final String plaintext;
        try {
            JSONObject json = new JSONObject();
            json.put("base_url", credential.baseUrl());
            json.put("client_id", credential.clientId());
            json.put("refresh_credential", credential.refreshToken());
            plaintext = json.toString();
        } catch (JSONException e) {
            throw new IOException("Could not encode Home Assistant credential", e);
        }

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        String blob = encode(cipher.getIV()) + "." + encode(ciphertext);
        if (!preferences.edit().putString(PREF_BLOB, blob).commit()) {
            throw new IOException("Could not save Home Assistant credential");
        }
    }

    public StoredHomeAssistantCredential load()
            throws GeneralSecurityException, IOException {
        String blob = preferences.getString(PREF_BLOB, null);
        if (blob == null || blob.trim().isEmpty()) {
            return null;
        }

        int separator = blob.indexOf('.');
        if (separator <= 0 || separator == blob.length() - 1 || blob.indexOf('.', separator + 1) >= 0) {
            throw new IOException("Stored Home Assistant credential is invalid");
        }

        byte[] iv;
        byte[] ciphertext;
        try {
            iv = Base64.decode(blob.substring(0, separator), Base64.NO_WRAP);
            ciphertext = Base64.decode(blob.substring(separator + 1), Base64.NO_WRAP);
        } catch (IllegalArgumentException e) {
            throw new IOException("Stored Home Assistant credential is invalid", e);
        }

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] plaintext = cipher.doFinal(ciphertext);

        try {
            JSONObject json = new JSONObject(new String(plaintext, StandardCharsets.UTF_8));
            return new StoredHomeAssistantCredential(
                    required(json, "base_url"),
                    required(json, "client_id"),
                    required(json, "refresh_credential"));
        } catch (JSONException | IllegalArgumentException e) {
            throw new IOException("Stored Home Assistant credential is invalid", e);
        }
    }

    public void clear() throws IOException {
        if (!preferences.edit().remove(PREF_BLOB).commit()) {
            throw new IOException("Could not clear Home Assistant credential");
        }
    }

    private static SecretKey getOrCreateKey() throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        if (keyStore.containsAlias(KEY_ALIAS)) {
            java.security.Key key = keyStore.getKey(KEY_ALIAS, null);
            if (!(key instanceof SecretKey)) {
                throw new GeneralSecurityException("Home Assistant credential key is invalid");
            }
            return (SecretKey) key;
        }

        KeyGenerator generator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEY_STORE);
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build();
        generator.init(spec);
        return generator.generateKey();
    }

    private static String required(JSONObject json, String key) throws JSONException {
        String value = json.getString(key);
        if (value == null || value.trim().isEmpty()) {
            throw new JSONException("missing credential field");
        }
        return value;
    }

    private static String encode(byte[] value) {
        return Base64.encodeToString(value, Base64.NO_WRAP);
    }
}
