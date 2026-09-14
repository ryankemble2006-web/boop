package local.johnnycastaway.shield;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.view.KeyEvent;
import android.widget.Toast;
public final class PreviewActivity extends Activity {
 private JohnnyView player;
 private final DeezerAlbumBrowser albumBrowser=new DeezerAlbumBrowser();
 private final Handler handler=new Handler(Looper.getMainLooper());
 @Override public void onCreate(Bundle b){
  super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894);
  player=new JohnnyView(this);setContentView(player);player.start();
 }
 @Override public boolean dispatchKeyEvent(KeyEvent e){
  if(e.getAction()!=KeyEvent.ACTION_DOWN||e.getRepeatCount()!=0)return super.dispatchKeyEvent(e);
  int key=e.getKeyCode();
  if(key==KeyEvent.KEYCODE_VOLUME_UP||key==KeyEvent.KEYCODE_VOLUME_DOWN||key==KeyEvent.KEYCODE_VOLUME_MUTE)return super.dispatchKeyEvent(e);
  if(key==KeyEvent.KEYCODE_DPAD_UP&&player!=null&&player.focusAlbumArt())return true;
  if((key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER)&&player!=null&&player.albumArtFocused()){
   albumBrowser.open(this,player.nowPlayingView(),player.currentTrack(),this::openAlbumAndClose);return true;
  }
  finish();return true;
 } private void openAlbumAndClose(long albumId){
  if(player!=null)player.stop();finish();
  Intent album=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.deezer.com/album/"+albumId)).setPackage("deezer.android.app").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  handler.postDelayed(()->{try{getApplicationContext().startActivity(album);}catch(RuntimeException unavailable){Toast.makeText(getApplicationContext(),"Deezer couldn't open that album.",Toast.LENGTH_SHORT).show();}},180);
 }
 @Override protected void onStop(){albumBrowser.cancel();if(player!=null)player.stop();super.onStop();finish();}
}