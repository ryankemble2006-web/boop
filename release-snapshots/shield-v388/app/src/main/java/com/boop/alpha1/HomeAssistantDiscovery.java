package com.boop.alpha1;

import android.content.Context;
import com.boop.shieldoverlay.DiscoveredHomeAssistant;

/**
 * Discovers local Home Assistant instances via mDNS.
 * Delegated to the canonical unified implementation in shield-lib.
 */
final class HomeAssistantDiscovery {
    interface Listener {
        void onFound(String displayName, String baseUrl);
        void onUnavailable(String reason);
    }

    private final com.boop.shieldoverlay.HomeAssistantDiscovery delegate;

    HomeAssistantDiscovery(Context context) {
        delegate = new com.boop.shieldoverlay.HomeAssistantDiscovery(context);
    }

    void start(Listener listener) {
        if (listener == null) {
            delegate.stop();
            return;
        }
        delegate.start(new com.boop.shieldoverlay.HomeAssistantDiscovery.Listener() {
            @Override
            public void onDiscovered(DiscoveredHomeAssistant homeAssistant) {
                if (homeAssistant != null) {
                    listener.onFound(homeAssistant.name(), homeAssistant.baseUrl());
                }
            }

            @Override
            public void onError(String message) {
                listener.onUnavailable(message);
            }
        });
    }

    void stop() {
        delegate.stop();
    }
}
