package com.boop.alpha1;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;
import com.boop.shared.MediaRequest;

final class BoopLocalMedia {
    private BoopLocalMedia() { }
    static boolean handle(Activity activity, String text, java.util.function.Consumer<String> reply) {
        MediaRequest request = MediaRequest.parse(text);
        if (request == null) return false;
        if (request.kind == MediaRequest.Kind.DEEZER_SEARCH) {
            Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)
                    .setPackage("deezer.android.app")
                    .putExtra(android.app.SearchManager.QUERY,request.query)
                    .putExtra(MediaStore.EXTRA_MEDIA_FOCUS,"vnd.android.cursor.item/audio");
            try {
                // Explicit provider-only dispatch: no Google Assistant detour and no fabricated success.
                activity.startActivity(intent);
            } catch (RuntimeException unavailable) {
                reply.accept("Deezer cannot play searches on this device. Open Deezer and choose the music there.");
            }
            return true;
        }
        return com.boop.shieldhome.ShieldNowPlayingManager.get(activity).requestTransport(request.kind);
    }
}
