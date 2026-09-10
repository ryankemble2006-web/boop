package com.boop.alpha1;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;
import com.boop.shieldhome.NowPlayingSnapshot;
import com.boop.shieldhome.ShieldNowPlayingManager;
import com.boop.shieldoverlay.HomeAssistantSession;
import com.boop.shieldoverlay.HomeAssistantAuthClient;
import com.boop.shieldoverlay.SecureCredentialStore;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import okhttp3.OkHttpClient;

/** Explicit, non-exported user action. Back cancels any pending native close. */
public final class BoopClosePlayerActivity extends Activity {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private ShieldNowPlayingManager manager;
    private LocalPlayerCloseGate gate;
    private File marker;
    private Thread worker;
    private Runnable unsubscribe;
    private volatile boolean cancelled;
    private long sessionId;
    private String player;
    private boolean allMediaApps;
    private android.media.session.MediaSession.Token originalToken;
    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        if(saved!=null || BoopDeviceProfile.resolve(this)!=BoopDeviceProfile.Mode.SHIELD) {
            finish(); return;
        }
        manager=ShieldNowPlayingManager.get(this);
        allMediaApps=getIntent().getBooleanExtra("all_media_apps",false);
        sessionId=getIntent().getLongExtra("session",0);
        player=getIntent().getStringExtra("player");
        if(!current()) { result(false); return; }
        if(!allMediaApps) {
            originalToken=manager.sessionToken(sessionId);
            if(originalToken==null) { result(false); return; }
        }
        TextView status=new TextView(this);
        status.setText((allMediaApps ? "Closing media apps…" : "Closing player…")
                +"\nPress Back to cancel"); status.setTextSize(24);
        status.setGravity(Gravity.CENTER); setContentView(status);
        handler.postDelayed(() -> result(false),40000);
        if(allMediaApps && !manager.stopCastForCleanup()) { result(false); return; }
        if(!allMediaApps && "com.google.android.apps.mediashell".equals(player)) {
            if(!manager.stopSelectedCast(sessionId)) { result(false); return; }
            awaitRemoved(30);
            return;
        }
        try {
            String nonce=UUID.randomUUID().toString().replace("-","");
            gate=allMediaApps ? LocalPlayerCloseGate.allMediaApps(nonce)
                    : new LocalPlayerCloseGate(sessionId,player,nonce);
            marker=new File(getFilesDir(),gate.filename());
            try(FileOutputStream out=new FileOutputStream(marker)) {
                out.write(gate.nonce.getBytes(StandardCharsets.US_ASCII));
            }
        } catch(Exception unavailable) { result(false); return; }
        if(!allMediaApps) unsubscribe=manager.state().subscribe(selected -> {
            if(!current()) {
                gate.cancel();
                marker.delete();
            }
        });
        worker=new Thread(() -> {
            try {
                HomeAssistantSession session=new HomeAssistantSession(new SecureCredentialStore(this),
                        new HomeAssistantAuthClient(new OkHttpClient.Builder().build()));
                HomeAssistantSession.Access access=session.ensureAccessToken();
                new LocalPlayerCloseClient().close(access.baseUrl(),access.accessToken(),gate,this::current);
                handler.post(() -> awaitRemoved(30));
            } catch(Exception unavailable) { handler.post(() -> result(false)); }
        },"BOOP-close-player");
        worker.start();
    }
    private boolean current() {
        if(cancelled || manager==null) return false;
        if(allMediaApps) return BoopDeviceProfile.resolve(this)==BoopDeviceProfile.Mode.SHIELD;
        NowPlayingSnapshot selected=manager.state().current();
        return selected!=null && selected.sessionId()==sessionId && player!=null
                && player.equals(selected.packageName());
    }
    private void awaitRemoved(int attempts) {
        if(cancelled || isFinishing()) return;
        if(allMediaApps ? manager.confirmsMediaCleanup() : manager.confirmsSessionGone(originalToken)) {
            result(true); return;
        }
        if(attempts<=0) { result(false); return; }
        handler.postDelayed(() -> awaitRemoved(attempts-1),100);
    }
    private void result(boolean closed) {
        if(cancelled || isFinishing()) return;
        Toast.makeText(this,closed ? "Done" : "Failed",Toast.LENGTH_SHORT).show();
        finish();
    }
    private void cancelPending() {
        cancelled=true;
        if(gate!=null) gate.cancel();
        if(marker!=null) marker.delete();
        if(worker!=null) worker.interrupt();
        if(unsubscribe!=null) unsubscribe.run();
        handler.removeCallbacksAndMessages(null);
    }
    @Override public void finish() {
        cancelPending();
        super.finish();
    }
    @Override public void onBackPressed() {
        cancelPending();
        super.onBackPressed();
    }
    @Override protected void onDestroy() {
        cancelPending();
        super.onDestroy();
    }
}
