package uk.local.casualty;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import java.io.File;

public final class MainActivity extends Activity {
    private static final String SETUP_PREFIX="SETUP_REQUIRED:";
    private boolean launched;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if(state==null) begin(); else finish();
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        launched=false;
        begin();
    }

    private void begin() {
        if(!WatchNowService.ready()) {
            fail("Enable Casualty auto-play in Shield Accessibility settings first.");
            return;
        }
        if(!new File(getNoBackupFilesDir(),"iplayer-local-adb.key").isFile()) {
            showLocalDebuggingSetup("This shortcut needs Shield Network debugging once so it can stop BBC iPlayer cleanly without showing the Force stop screen.");
            return;
        }
        openProgramme();
    }

    private void openProgramme() {
        Intent player=new Intent(Intent.ACTION_VIEW,Uri.parse(Routes.TV_URL))
                .setPackage(PlayerReset.PLAYER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(getPackageManager().resolveActivity(player,0)==null) {
            fail("Official Shield BBC iPlayer is not available.");
            return;
        }
        WatchNowService.prepareColdStart((ok,error) -> {
            if(isFinishing() || isDestroyed()) { WatchNowService.cancel(); return; }
            if(!ok) {
                if(error!=null && error.startsWith(SETUP_PREFIX))
                    showLocalDebuggingSetup(error.substring(SETUP_PREFIX.length()).trim());
                else fail(error);
                return;
            }
            try {
                launched=true;
                startActivity(player);
                finish();
            } catch(RuntimeException rejected) {
                launched=false;
                WatchNowService.cancel();
                fail("BBC iPlayer could not open.");
            }
        });
    }

    private void showLocalDebuggingSetup(String detail) {
        new AlertDialog.Builder(this)
                .setTitle("One-time iPlayer shortcut setup")
                .setMessage(detail+"\n\nEnable Network debugging in Shield Developer options. Then return here and choose Authorise. Android will ask you once to trust this Casualty shortcut.")
                .setPositiveButton("Authorise",(dialog,which) -> openProgramme())
                .setNeutralButton("Open debugging settings",(dialog,which) -> openDebuggingSettings())
                .setNegativeButton("Cancel",(dialog,which) -> finish())
                .setOnCancelListener(dialog -> finish())
                .show();
    }

    private void openDebuggingSettings() {
        Intent settings=new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        if(getPackageManager().resolveActivity(settings,0)==null) settings=new Intent(Settings.ACTION_SETTINGS);
        try { startActivity(settings); }
        catch(RuntimeException rejected) { Toast.makeText(this,"Open Shield Developer options and enable Network debugging.",Toast.LENGTH_LONG).show(); }
        finish();
    }

    private void fail(String message) {
        Toast.makeText(this,message==null ? "Shortcut setup failed." : message,Toast.LENGTH_LONG).show();
        finish();
    }

    @Override protected void onDestroy() {
        if(!launched) WatchNowService.cancel();
        super.onDestroy();
    }
}
