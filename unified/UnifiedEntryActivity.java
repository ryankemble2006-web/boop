package com.boop.alpha1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public final class UnifiedEntryActivity extends Activity {
    private static final int REQ_ASSISTANT_FIRST_RUN = 2400;
    private boolean waitingForAssistantChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        route();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        route();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ASSISTANT_FIRST_RUN) {
            waitingForAssistantChoice = false;
            route();
        }
    }

    private void route() {
        BoopDeviceProfile.Mode mode = BoopDeviceProfile.resolve(this);
        boolean homeIntent = getIntent() != null
                && getIntent().hasCategory(Intent.CATEGORY_HOME);
        ShieldEntryRoute.Target destination = ShieldEntryRoute.resolve(mode, homeIntent);

        if (destination == ShieldEntryRoute.Target.SHIELD_PUPPET
                && BoopAssistantPreference.load(this) == null) {
            if (!waitingForAssistantChoice) {
                waitingForAssistantChoice = true;
                startActivityForResult(
                        new Intent().setClassName(getPackageName(),
                                "com.boop.alpha1.BoopAssistantSetupActivity"),
                        REQ_ASSISTANT_FIRST_RUN);
            }
            return;
        }

        Intent targetIntent = new Intent(Intent.ACTION_MAIN)
                .setClassName(getPackageName(), destination.className())
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(targetIntent);
        if (destination.suppressEntryTransition()) {
            overridePendingTransition(0, 0);
        }
        finish();
    }
}
