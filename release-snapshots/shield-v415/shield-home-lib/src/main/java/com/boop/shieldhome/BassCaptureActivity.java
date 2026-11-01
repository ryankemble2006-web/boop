package com.boop.shieldhome;
import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.*;
import android.view.*;
import android.widget.*;
public final class BassCaptureActivity extends Activity {
 public static final String EXTRA_AUTO_START = "auto_start";
 private TextView status;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final Runnable tick=new Runnable(){public void run(){status.setText(BassCaptureService.status);handler.postDelayed(this,250);}};
 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  boolean autoStart = getIntent() != null && getIntent().getBooleanExtra(EXTRA_AUTO_START, false);
  if (autoStart) {
   begin();
  }
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(60,50,60,30);
  TextView title=new TextView(this);title.setText("Music spectrum");title.setTextSize(28);root.addView(title);
  TextView explanation=new TextView(this);explanation.setText("Frequency bars follow real playback audio. Your Home colour sets the flowing multicolour palette. No music is saved. Android will ask for capture access.");explanation.setTextSize(20);root.addView(explanation);
  Button start=new Button(this);start.setText("Enable spectrum");root.addView(start);
  start.setOnClickListener(v->begin());start.requestFocus();
  Button stop=new Button(this);stop.setText("Stop spectrum");root.addView(stop);stop.setOnClickListener(v->{
   getSharedPreferences("boop_shield_home_v1", MODE_PRIVATE).edit().putBoolean("spectrum_enabled", false).apply();
   startService(new Intent(this, BassCaptureService.class).setAction("stop"));
   stopService(new Intent(this, BassCaptureService.class));
   finish();
  });
  status=new TextView(this);status.setTextSize(22);root.addView(status);setContentView(root);
 }
 private void begin(){
  if(BassCaptureState.running)return;
  if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},1);return;}
  startActivityForResult(getSystemService(MediaProjectionManager.class).createScreenCaptureIntent(),2);
 }
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] grants){super.onRequestPermissionsResult(r,p,grants);if(r==1&&grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED)begin();}
 @Override public void onActivityResult(int r,int result,Intent data){
  super.onActivityResult(r,result,data);
  if(r==2&&result==RESULT_OK&&data!=null){
   startForegroundService(new Intent(this,BassCaptureService.class).putExtra("result",result).putExtra("consent",data));
   finish();
  } else if (r == 2) {
   finish();
  }
 }
 @Override public void onResume(){super.onResume();handler.post(tick);}
 @Override public void onPause(){handler.removeCallbacks(tick);super.onPause();}
}
