package uk.local.casualty;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

/** Launch only after the shared controller has verified a real iPlayer force-stop. */
public final class MainActivity extends Activity {
    private boolean launched;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if(state==null) openProgramme(); else finish();
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent); setIntent(intent); launched=false; openProgramme();
    }
    private void openProgramme() {
        if(!WatchNowService.ready()) {
            fail("Enable Casualty auto-play in Shield Accessibility settings first."); return;
        }
        Intent player=new Intent(Intent.ACTION_VIEW,Uri.parse(Routes.TV_URL))
                .setPackage(PlayerReset.PLAYER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(getPackageManager().resolveActivity(player,0)==null) { fail("Official Shield BBC iPlayer is not available."); return; }
        WatchNowService.prepareColdStart((ok,error) -> {
            if(isFinishing() || isDestroyed()) { WatchNowService.cancel(); return; }
            if(!ok) { fail(error); return; }
            try { launched=true; startActivity(player); finish(); }
            catch(RuntimeException rejected) { launched=false; WatchNowService.cancel(); fail("BBC iPlayer could not open."); }
        });
    }
    private void fail(String message) {
        Toast.makeText(this,message,Toast.LENGTH_LONG).show(); finish();
    }
    @Override protected void onDestroy() {
        if(!launched) WatchNowService.cancel();
        super.onDestroy();
    }
}
