package com.boop.alpha1;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.os.Bundle;

/** Activity-based Android assistant. Keeps Android's existing recognition provider. */
public final class BoopAssistantActivity extends Activity {
    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        RoleManager roles=getSystemService(RoleManager.class);
        boolean held=roles!=null && roles.isRoleAvailable(RoleManager.ROLE_ASSISTANT)
                && roles.isRoleHeld(RoleManager.ROLE_ASSISTANT);
        boolean chosen=BoopAssistantPreference.load(this)==BoopAssistantIntegrationPolicy.Choice.USE_BOOP;
        if(BoopAssistantLaunchPolicy.shouldLaunch(getIntent().getAction(),chosen,held,saved!=null)) {
            Intent target=new Intent(this,MainActivity.class)
                    .putExtra(BoopVoiceInteractionSession.EXTRA_ONE_SHOT_ASSIST,true)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            if(getIntent().hasExtra(Intent.EXTRA_ASSIST_INPUT_DEVICE_ID)) {
                target.putExtra(Intent.EXTRA_ASSIST_INPUT_DEVICE_ID,
                        getIntent().getIntExtra(Intent.EXTRA_ASSIST_INPUT_DEVICE_ID,-1));
            }
            startActivity(target);
        }
        finish();
    }
}
