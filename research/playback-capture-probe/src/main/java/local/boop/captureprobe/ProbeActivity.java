package local.boop.captureprobe;
import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.*;
import android.view.*;
import android.widget.*;
public final class ProbeActivity extends Activity {
 private TextView status;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final Runnable tick=new Runnable(){public void run(){status.setText(ProbeService.status);handler.postDelayed(this,250);}};
 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(60,50,60,30);
  TextView title=new TextView(this);title.setText("BOOP playback capture test");title.setTextSize(28);root.addView(title);
  TextView explanation=new TextView(this);explanation.setText("One-minute Deezer audio test. Audio is measured in memory, not saved. No microphone or screen frames are captured. Keep Deezer playing.");explanation.setTextSize(20);root.addView(explanation);
  Button start=new Button(this);start.setText("Start audio test");root.addView(start);
  start.setOnClickListener(v->begin());start.requestFocus();
  Button stop=new Button(this);stop.setText("Stop test");root.addView(stop);stop.setOnClickListener(v->stopService(new Intent(this,ProbeService.class)));
  status=new TextView(this);status.setTextSize(22);root.addView(status);setContentView(root);
 }
 private void begin(){
  if(ProbeService.running)return;
  if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},1);return;}
  startActivityForResult(getSystemService(MediaProjectionManager.class).createScreenCaptureIntent(),2);
 }
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] grants){super.onRequestPermissionsResult(r,p,grants);if(r==1&&grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED)begin();}
 @Override public void onActivityResult(int r,int result,Intent data){
  super.onActivityResult(r,result,data);
  if(r==2&&result==RESULT_OK&&data!=null)startForegroundService(new Intent(this,ProbeService.class).putExtra("result",result).putExtra("consent",data));
 }
 @Override public void onResume(){super.onResume();handler.post(tick);}
 @Override public void onPause(){handler.removeCallbacks(tick);super.onPause();}
}
