package uk.local.casualty;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

public final class MainActivity extends Activity {
    private static final String SETUP_PREFIX="SETUP_REQUIRED:";
    private boolean launched;
    private TextView setupStatus;

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
            showLocalDebuggingSetup("This shortcut needs Shield USB debugging and Network debugging once so it can stop BBC iPlayer cleanly without showing the Force stop screen.");
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
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(dp(110),dp(70),dp(110),dp(70));
        root.setBackgroundColor(Color.rgb(18,18,18));

        TextView title=new TextView(this);
        title.setText("One-time iPlayer shortcut setup");
        title.setTextColor(Color.WHITE);
        title.setTextSize(34);
        root.addView(title,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView body=new TextView(this);
        body.setText(detail+"\n\n1. Open Shield debugging settings and enable USB debugging and Network debugging.\n2. Return to this shortcut and choose Authorise.\n3. Accept Android's one-time debugging trust prompt for Casualty.\n\nNo BBC login, password or profile name is stored by this shortcut.");
        body.setTextColor(Color.LTGRAY);
        body.setTextSize(22);
        LinearLayout.LayoutParams bodyParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        bodyParams.topMargin=dp(28);
        root.addView(body,bodyParams);

        setupStatus=new TextView(this);
        setupStatus.setText("Ready for setup.");
        setupStatus.setTextColor(Color.WHITE);
        setupStatus.setTextSize(20);
        LinearLayout.LayoutParams statusParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        statusParams.topMargin=dp(24);
        root.addView(setupStatus,statusParams);

        LinearLayout actions=new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams actionsParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        actionsParams.topMargin=dp(30);
        root.addView(actions,actionsParams);

        Button settingsButton=new Button(this);
        settingsButton.setText("Open debugging settings");
        settingsButton.setOnClickListener(v -> openDebuggingSettings());
        actions.addView(settingsButton,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));

        Button authoriseButton=new Button(this);
        authoriseButton.setText("Authorise");
        authoriseButton.setOnClickListener(v -> {
            setupStatus.setText("Waiting for Android debugging approval…");
            authoriseButton.setEnabled(false);
            settingsButton.setEnabled(false);
            openProgramme();
        });
        LinearLayout.LayoutParams authParams=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f);
        authParams.leftMargin=dp(18);
        actions.addView(authoriseButton,authParams);

        Button cancelButton=new Button(this);
        cancelButton.setText("Cancel");
        cancelButton.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams cancelParams=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f);
        cancelParams.leftMargin=dp(18);
        actions.addView(cancelButton,cancelParams);

        setContentView(root);
        settingsButton.requestFocus();
    }

    private void openDebuggingSettings() {
        Intent settings=new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        if(getPackageManager().resolveActivity(settings,0)==null) settings=new Intent(Settings.ACTION_SETTINGS);
        try { startActivity(settings); }
        catch(RuntimeException rejected) { Toast.makeText(this,"Open Shield Developer options and enable USB debugging and Network debugging.",Toast.LENGTH_LONG).show(); }
        finish();
    }

    private int dp(int value) {
        return Math.round(value*getResources().getDisplayMetrics().density);
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
