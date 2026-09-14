package local.johnnycastaway.shield;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
public final class MainActivity extends Activity {
 private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
 private TextView label(String text,int size){TextView v=new TextView(this);v.setText(text);v.setTextSize(size);v.setTextColor(Color.WHITE);v.setGravity(Gravity.CENTER);return v;}
 private Button action(String text,View.OnClickListener click){Button v=new Button(this);v.setAllCaps(false);v.setText(text);v.setTextSize(18);v.setGravity(Gravity.CENTER);v.setPadding(dp(12),dp(8),dp(12),dp(8));v.setOnClickListener(click);return v;}
 private void openSaverSettings(){Intent[] options={new Intent(Settings.ACTION_DREAM_SETTINGS),new Intent().setClassName("com.android.tv.settings","com.android.tv.settings.device.display.daydream.DaydreamActivity"),new Intent(Settings.ACTION_SETTINGS)};for(Intent i:options){try{startActivity(i);return;}catch(android.content.ActivityNotFoundException|SecurityException e){/* Try the next supported settings screen. */}}Toast.makeText(this,"Open Settings from the Shield home screen.",Toast.LENGTH_LONG).show();}
 private void openMusicAccess(){Intent[] options={new Intent().setClassName("com.android.tv.settings","com.android.tv.settings.privacy.NotificationAccessActivity"),new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)};for(Intent i:options){try{startActivity(i);return;}catch(android.content.ActivityNotFoundException|SecurityException e){/* Try the next supported screen. */}}Toast.makeText(this,"Open Settings, Apps, Special app access, Notification access.",Toast.LENGTH_LONG).show();}
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894);
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(Color.rgb(5,13,24));
  LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);box.setPadding(dp(28),dp(20),dp(28),dp(20));scroll.addView(box,new ScrollView.LayoutParams(-1,-2));
  box.addView(label("Johnny Castaway",28));
  TextView subtitle=label("His little island. Your big screen.",17);subtitle.setTextColor(Color.LTGRAY);box.addView(subtitle);
  ImageView art=new ImageView(this);art.setImageResource(getResources().getIdentifier("preview","drawable",getPackageName()));art.setScaleType(ImageView.ScaleType.FIT_CENTER);
  LinearLayout.LayoutParams picture=new LinearLayout.LayoutParams(dp(260),dp(125));picture.topMargin=dp(10);picture.bottomMargin=dp(10);box.addView(art,picture);
  int width=Math.min(dp(400),getResources().getDisplayMetrics().widthPixels-dp(72));
  Button watch=action("Watch now",v->startActivity(new Intent(this,PreviewActivity.class)));
  box.addView(watch,new LinearLayout.LayoutParams(width,dp(56)));
  Button settings=action("Screen saver settings",v->openSaverSettings());
  LinearLayout.LayoutParams second=new LinearLayout.LayoutParams(width,dp(56));second.topMargin=dp(6);box.addView(settings,second);
  Button music=action("Enable music information",v->openMusicAccess());
  LinearLayout.LayoutParams third=new LinearLayout.LayoutParams(width,dp(56));third.topMargin=dp(6);box.addView(music,third);
  TextView help=label("Choose Johnny HA Lab in Screen saver settings.\nPress a remote button to leave the island.\nOriginal scenes · Living-room state from BOOP · Silent\nEnable music information once to show Now Playing.",14);help.setTextColor(Color.LTGRAY);help.setPadding(0,dp(12),0,0);box.addView(help);
  setContentView(scroll);watch.requestFocus();
 }
}
