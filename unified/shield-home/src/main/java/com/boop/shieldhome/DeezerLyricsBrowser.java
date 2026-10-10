package com.boop.shieldhome;
import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;
/** Open the presentation immediately; it owns lookup, retry and spectrum fallback. */
final class DeezerLyricsBrowser {
 void cancel() { }
 void destroy() { }
 void onHostPaused() { }
 void onTrackChanged(ShieldNowPlayingManager manager) { }
 void open(Activity activity, ShieldNowPlayingManager manager, NowPlayingSnapshot requested) {
  if(activity==null || manager==null || requested==null
    || !DeezerLyricsPolicy.available(requested.packageName())
    || activity.isFinishing() || activity.isDestroyed() || !activity.hasWindowFocus()) return;
  try {
   activity.startActivity(new Intent(activity,ShieldLyricsActivity.class));
   activity.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
  } catch(RuntimeException unavailable) {
   Toast.makeText(activity,"Couldn't open lyrics just now.",Toast.LENGTH_SHORT).show();
  }
 }
}
