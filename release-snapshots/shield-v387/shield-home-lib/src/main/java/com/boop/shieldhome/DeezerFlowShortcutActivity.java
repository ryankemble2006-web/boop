package com.boop.shieldhome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/** Legacy Android shortcut offered to Button Mapper, with no visible player UI. */
public final class DeezerFlowShortcutActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
            Intent launch = new Intent(this, DeezerFlowShortcutActivity.class)
                    .setAction("com.boop.action.DEEZER_FLOW");
            setResult(RESULT_OK, new Intent()
                    .putExtra(Intent.EXTRA_SHORTCUT_NAME, "Deezer Flow (background)")
                    .putExtra(Intent.EXTRA_SHORTCUT_INTENT, launch)
                    .putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource.fromContext(this, R.drawable.boop_icon_shuffle)));
        } else {
            ShieldNowPlayingManager.get(this).playDeezerFlow();
        }
        finish();
        overridePendingTransition(0, 0);
    }
}
