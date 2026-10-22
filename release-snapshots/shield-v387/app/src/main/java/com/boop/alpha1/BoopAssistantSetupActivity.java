package com.boop.alpha1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.role.RoleManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public final class BoopAssistantSetupActivity extends Activity {
    private static final int REQ_ASSISTANT_ROLE = 2401;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        showChoice();
    }

    private void showChoice() {
        new AlertDialog.Builder(this)
                .setTitle("Shield microphone button")
                .setMessage("Choose what the Shield remote microphone button should call. Android will ask separately before changing the default assistant.")
                .setPositiveButton("Use BOOP for the microphone button", (dialog, which) -> chooseBoop())
                .setNegativeButton("Keep my current assistant", (dialog, which) -> keepCurrent())
                .setOnCancelListener(dialog -> finish())
                .show();
    }

    private void chooseBoop() {
        BoopAssistantPreference.save(this, BoopAssistantIntegrationPolicy.Choice.USE_BOOP);
        if (Build.VERSION.SDK_INT < 29) {
            openVoiceSettings();
            return;
        }
        RoleManager roles = getSystemService(RoleManager.class);
        if (roles == null || !roles.isRoleAvailable(RoleManager.ROLE_ASSISTANT)) {
            openVoiceSettings();
            return;
        }
        if (roles.isRoleHeld(RoleManager.ROLE_ASSISTANT)) {
            Toast.makeText(this, "BOOP is already the selected assistant.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        startActivityForResult(roles.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT), REQ_ASSISTANT_ROLE);
    }

    private void keepCurrent() {
        BoopAssistantPreference.save(this, BoopAssistantIntegrationPolicy.Choice.KEEP_CURRENT);
        if (isBoopRoleHolder()) {
            openVoiceSettings();
        } else {
            finish();
        }
    }

    private boolean isBoopRoleHolder() {
        if (Build.VERSION.SDK_INT < 29) return false;
        RoleManager roles = getSystemService(RoleManager.class);
        return roles != null && roles.isRoleAvailable(RoleManager.ROLE_ASSISTANT)
                && roles.isRoleHeld(RoleManager.ROLE_ASSISTANT);
    }

    private void openVoiceSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
        } catch (RuntimeException unavailable) {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (RuntimeException ignored) {
                Toast.makeText(this, "Open Android assistant settings to change the microphone button.", Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ASSISTANT_ROLE) {
            if (!isBoopRoleHolder()) {
                Toast.makeText(this, "Assistant choice was not changed.", Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }
}
