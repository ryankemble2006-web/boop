package local.johnnycastaway.shield;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
/** HA lab preview; scene triggers come only from the state observer. */
public final class HaLabActivity extends Activity {
 private JohnnyView player;
 @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().getDecorView().setSystemUiVisibility(5894);player=new JohnnyView(this);setContentView(player);player.start();}
 @Override public boolean dispatchKeyEvent(KeyEvent e){if(e.getKeyCode()==KeyEvent.KEYCODE_BACK&&e.getAction()==KeyEvent.ACTION_DOWN){finish();return true;}return super.dispatchKeyEvent(e);}
 @Override protected void onStop(){if(player!=null)player.stop();super.onStop();finish();}
}
