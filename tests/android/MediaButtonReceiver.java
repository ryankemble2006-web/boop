package androidx.media.session;
public final class MediaButtonReceiver extends android.content.BroadcastReceiver {
 public void onReceive(android.content.Context c,android.content.Intent i){
  android.view.KeyEvent e=i.getParcelableExtra(android.content.Intent.EXTRA_KEY_EVENT);
  if(e!=null && e.getAction()==android.view.KeyEvent.ACTION_DOWN && e.getKeyCode()==android.view.KeyEvent.KEYCODE_MEDIA_PLAY)
   c.startForegroundService(new android.content.Intent().setClassName(c,"deezer.android.app.data.mediasession.service.DeezerSdkMediaService"));
 }
}
