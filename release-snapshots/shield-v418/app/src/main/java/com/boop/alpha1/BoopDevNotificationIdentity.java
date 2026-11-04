package com.boop.alpha1;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

final class BoopDevNotificationIdentity {
    enum Mark {
        FACEBOOK,
        WHATSAPP,
        GMAIL,
        X,
        YOUTUBE,
        MESSENGER,
        INSTAGRAM,
        DISCORD,
        SPOTIFY,
        REDDIT
    }

    static final class Spec {
        private final String label;
        private final Mark mark;

        Spec(String label, Mark mark) {
            this.label = label;
            this.mark = mark;
        }

        String label() { return label; }
        Mark mark() { return mark; }
    }

    private static final Map<String, Spec> SPECS;

    static {
        Map<String, Spec> specs = new LinkedHashMap<>();
        specs.put("boop.dev.facebook", new Spec("Facebook", Mark.FACEBOOK));
        specs.put("boop.dev.whatsapp", new Spec("WhatsApp", Mark.WHATSAPP));
        specs.put("boop.dev.gmail", new Spec("Gmail", Mark.GMAIL));
        specs.put("boop.dev.x", new Spec("X / Twitter", Mark.X));
        specs.put("boop.dev.youtube", new Spec("YouTube", Mark.YOUTUBE));
        specs.put("boop.dev.messenger", new Spec("Messenger", Mark.MESSENGER));
        specs.put("boop.dev.instagram", new Spec("Instagram", Mark.INSTAGRAM));
        specs.put("boop.dev.discord", new Spec("Discord", Mark.DISCORD));
        specs.put("boop.dev.spotify", new Spec("Spotify", Mark.SPOTIFY));
        specs.put("boop.dev.reddit", new Spec("Reddit", Mark.REDDIT));
        SPECS = Collections.unmodifiableMap(specs);
    }

    private BoopDevNotificationIdentity() { }

    static Spec forPackage(String packageName) {
        if (packageName == null) return null;
        return SPECS.get(packageName);
    }
}
