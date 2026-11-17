package com.boop.shieldhome;

import java.util.ArrayList;
import java.util.List;

public final class OptionalRowRegistry {
    private OptionalRowRegistry() {}

    public enum Key {
        PLAY_NEXT("row_play_next"),
        APP_CHANNELS("row_app_channels");

        private final String preferenceKey;

        Key(String preferenceKey) {
            this.preferenceKey = preferenceKey;
        }

        public String preferenceKey() {
            return preferenceKey;
        }
    }

    @FunctionalInterface
    public interface EnabledLookup {
        boolean isEnabled(Key key);
    }

    @FunctionalInterface
    public interface ProviderFactory {
        HomeRowProvider create(Key key);
    }

    public static List<HomeRowProvider> loadEnabled(
            EnabledLookup enabled,
            ProviderFactory factory) {
        ArrayList<HomeRowProvider> providers = new ArrayList<>();
        for (Key key : Key.values()) {
            if (!enabled.isEnabled(key)) {
                continue;
            }
            providers.add(factory.create(key));
        }
        return providers;
    }
}
