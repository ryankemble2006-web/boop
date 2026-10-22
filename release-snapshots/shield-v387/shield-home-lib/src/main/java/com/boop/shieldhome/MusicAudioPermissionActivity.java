package com.boop.shieldhome;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

/** An explicit, music-only permission entry. Granting access does not start audio. */
public final class MusicAudioPermissionActivity extends Activity {
    private static final int REQUEST_AUDIO = 16101;
    private static final int REQUEST_SETTINGS = 16102;
    private static final String STATE_PENDING = "music_audio_request_pending";
    private static final String STATE_PHASE = "music_audio_phase";
    private static final int EXPLAIN = 0;
    private static final int DENIED = 1;
    private static final int IN_SETTINGS = 2;

    private MusicAudioPermissionFlow flow;
    private AlertDialog dialog;
    private int phase = EXPLAIN;

    public static boolean hasAudioAccess(Context context) {
        return context != null && context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setResult(RESULT_CANCELED);
        flow = new MusicAudioPermissionFlow(state != null && state.getBoolean(STATE_PENDING));
        phase = state == null ? EXPLAIN : state.getInt(STATE_PHASE, EXPLAIN);

        TextView background = new TextView(this);
        background.setBackgroundColor(Color.BLACK);
        background.setTextColor(Color.WHITE);
        background.setTextSize(24);
        background.setGravity(Gravity.CENTER);
        background.setText("Music audio access");
        setContentView(background);

        MusicAudioPermissionFlow.Action action = flow.begin(hasAudioAccess(this));
        if (action == MusicAudioPermissionFlow.Action.READY) {
            finishAllowed();
        } else if (action == MusicAudioPermissionFlow.Action.WAIT || phase == IN_SETTINGS) {
            // Android owns the in-flight dialog/result across activity recreation.
        } else if (phase == DENIED) {
            showDenied();
        } else {
            showExplanation();
        }
    }

    private void showExplanation() {
        if (!canShowDialog()) return;
        phase = EXPLAIN;
        showDialog(new AlertDialog.Builder(this)
                .setTitle("Music audio access")
                .setMessage("Android calls this microphone access. It is also needed to read music loudness. "
                        + "This permission step does not start microphone listening or change BOOP's animations.")
                .setNegativeButton("Not now", (d, which) -> finish())
                .setPositiveButton("Continue", (d, which) -> requestAudioAccess()));
    }

    private void requestAudioAccess() {
        MusicAudioPermissionFlow.Action action = flow.continueRequest(hasAudioAccess(this));
        if (action == MusicAudioPermissionFlow.Action.READY) {
            finishAllowed();
        } else if (action == MusicAudioPermissionFlow.Action.REQUEST) {
            try {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
            } catch (RuntimeException unavailable) {
                flow.result(false);
                // The current dialog is dismissed by Android after its button callback.
                getWindow().getDecorView().post(this::showDenied);
            }
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_AUDIO || flow == null || isFinishing() || isDestroyed()) return;
        if (flow.result(hasAudioAccess(this)) == MusicAudioPermissionFlow.Action.READY) {
            finishAllowed();
        } else {
            showDenied();
        }
    }

    private void showDenied() {
        phase = DENIED;
        if (!canShowDialog()) return;
        showDialog(new AlertDialog.Builder(this)
                .setTitle("Audio access wasn't enabled")
                .setMessage("BOOP can carry on as before. You can leave this off, or open BOOP's Android "
                        + "permissions and allow Microphone for music audio access. Nothing starts listening here.")
                .setNegativeButton("Not now", (d, which) -> finish())
                .setPositiveButton("Open settings", (d, which) -> openAppSettings()));
    }

    private void openAppSettings() {
        if (hasAudioAccess(this)) {
            finishAllowed();
            return;
        }
        phase = IN_SETTINGS;
        try {
            startActivityForResult(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName())), REQUEST_SETTINGS);
        } catch (RuntimeException unavailable) {
            Toast.makeText(this, "BOOP's Android permission settings aren't available here.",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_SETTINGS || flow == null || isFinishing() || isDestroyed()) return;
        if (hasAudioAccess(this)) finishAllowed();
        else finish(); // Do not reopen a request after the user leaves Android settings.
    }

    private boolean canShowDialog() {
        return !isFinishing() && !isDestroyed() && (dialog == null || !dialog.isShowing());
    }

    private void showDialog(AlertDialog.Builder builder) {
        AlertDialog next = builder.create();
        dialog = next;
        next.setOnCancelListener(ignored -> finish());
        next.setOnDismissListener(ignored -> { if (dialog == next) dialog = null; });
        next.setOnShowListener(ignored -> next.getButton(AlertDialog.BUTTON_NEGATIVE).requestFocus());
        next.show();
    }

    private void finishAllowed() {
        setResult(RESULT_OK);
        Toast.makeText(this, "Music audio access is allowed.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putBoolean(STATE_PENDING, flow != null && flow.isPending());
        state.putInt(STATE_PHASE, phase);
        super.onSaveInstanceState(state);
    }

    @Override protected void onDestroy() {
        if (dialog != null) {
            dialog.setOnCancelListener(null);
            dialog.dismiss();
            dialog = null;
        }
        super.onDestroy();
    }
}
