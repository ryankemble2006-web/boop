package com.boop.alpha1;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

final class SecureTokenStore {
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "boop-ha-refresh-v1";
    private static final String TRANSFORM = "AES/GCM/NoPadding";
    private static final String PREFS = "boop-ha";
    private static final String PREF_BASE = "base_url";
    private static final String PREF_CIPHER = "refresh_ciphertext";
    private static final String PREF_IV = "refresh_iv";

    private final SharedPreferences prefs;

    SecureTokenStore(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    synchronized void saveConnection(String baseUrl, String refreshToken) throws Exception {
        SecretKey key = getOrCreateKey();
        Cipher cipher = Cipher.getInstance(TRANSFORM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(refreshToken.getBytes(StandardCharsets.UTF_8));

        prefs.edit()
                .putString(PREF_BASE, HomeAssistantAuthUrls.trim(baseUrl))
                .putString(PREF_CIPHER, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .putString(PREF_IV, Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP))
                .apply();
    }

    String getBaseUrl() {
        return prefs.getString(PREF_BASE, null);
    }

    synchronized String getRefreshToken() throws Exception {
        String ciphertext = prefs.getString(PREF_CIPHER, null);
        String iv = prefs.getString(PREF_IV, null);
        if (ciphertext == null || iv == null) {
            return null;
        }

        KeyStore keyStore = KeyStore.getInstance(KEYSTORE);
        keyStore.load(null);
        SecretKey key = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
        if (key == null) {
            return null;
        }

        Cipher cipher = Cipher.getInstance(TRANSFORM);
        cipher.init(
                Cipher.DECRYPT_MODE,
                key,
                new GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)));
        byte[] plain = cipher.doFinal(Base64.decode(ciphertext, Base64.NO_WRAP));
        return new String(plain, StandardCharsets.UTF_8);
    }

    boolean hasConnection() {
        return getBaseUrl() != null
                && prefs.contains(PREF_CIPHER)
                && prefs.contains(PREF_IV);
    }

    void clear() {
        prefs.edit().clear().apply();
    }

    private SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE);
        keyStore.load(null);
        SecretKey existing = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
        if (existing != null) {
            return existing;
        }

        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE);
        generator.init(new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());
        return generator.generateKey();
    }
}
