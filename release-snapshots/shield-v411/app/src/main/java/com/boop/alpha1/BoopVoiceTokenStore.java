package com.boop.alpha1;

import android.content.Context;
import com.boop.shieldoverlay.SecureCredentialStore;
import com.boop.shieldoverlay.StoredHomeAssistantCredential;

/** Unified credential store shared across Shield, Wall, and Voice. */
final class BoopVoiceTokenStore {
    static SecureTokenStore create(Context context) {
        HomeAssistantSavedConnection connection = null;
        try {
            StoredHomeAssistantCredential saved = new SecureCredentialStore(context).load();
            if (saved != null) {
                connection = new HomeAssistantSavedConnection(
                        saved.baseUrl(), saved.clientId(), saved.refreshToken());
            }
        } catch (Exception unavailable) {
            android.util.Log.w("BOOP-Assist", "House connection unavailable");
        }
        // A changed server must not reuse another house's voice registration.
        String namespace = "boop-house-voice-" + (connection == null ? "unpaired"
                : java.util.UUID.nameUUIDFromBytes(connection.baseUrl().getBytes(
                        java.nio.charset.StandardCharsets.UTF_8)).toString());
        return new SecureTokenStore(context, connection, namespace);
    }
}