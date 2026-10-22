package com.boop.alpha1;

import android.app.Activity;
import com.boop.shared.MediaRequest;

final class BoopLocalMedia {
    private BoopLocalMedia() { }
    static boolean handle(Activity activity, String text, java.util.function.Consumer<String> reply) {
        MediaRequest request = MediaRequest.parse(text);
        if (request == null) return false;
        // Artist requests belong to the authenticated HA room/TV route, not this phone.
        if (request.kind == MediaRequest.Kind.DEEZER_SEARCH || request.kind == MediaRequest.Kind.DEEZER_FLOW) return false;
        return com.boop.shieldhome.ShieldNowPlayingManager.get(activity).requestTransport(request.kind);
    }
}
