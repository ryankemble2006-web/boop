package com.boop.launcher;
import android.app.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.os.*;
import android.provider.Settings;
import android.view.*;
public final class ReturnService extends Service {
 private WindowManager manager; private View strip; private BroadcastReceiver screen;
 public android.os.IBinder onBind(Intent i){return null;}
 public int onStartCommand(Intent i,int flags,int id){
  if(i!=null && "stop".equals(i.getAction())){stopSelf();return START_NOT_STICKY;}
  if(!Settings.canDrawOverlays(this)){stopSelf();return START_NOT_STICKY;}
  NotificationManager nm=getSystemService(NotificationManager.class);nm.createNotificationChannel(new NotificationChannel("return","BOOP return gesture",NotificationManager.IMPORTANCE_LOW));
  Intent home=new Intent(this,MainActivity.class).setAction("com.boop.launcher.RETURN").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
  PendingIntent back=PendingIntent.getActivity(this,1,home,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
  PendingIntent stop=PendingIntent.getService(this,2,new Intent(this,ReturnService.class).setAction("stop"),PendingIntent.FLAG_IMMUTABLE);
  startForeground(31,new Notification.Builder(this,"return").setSmallIcon(android.R.drawable.ic_menu_revert).setContentTitle("BOOP return gesture is on").setContentText("Swipe left from the right edge to return home.").setContentIntent(back).addAction(new Notification.Action.Builder(null,"Return home",back).build()).addAction(new Notification.Action.Builder(null,"Stop",stop).build()).setOngoing(true).build());
  if(strip==null){manager=getSystemService(WindowManager.class);strip=new View(this);strip.setBackgroundColor(0x88777777);strip.setContentDescription("Swipe left to return to BOOP Launcher");WindowManager.LayoutParams p=new WindowManager.LayoutParams((int)(20*getResources().getDisplayMetrics().density),(int)(180*getResources().getDisplayMetrics().density),WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,PixelFormat.TRANSLUCENT);p.gravity=Gravity.RIGHT|Gravity.CENTER_VERTICAL;strip.setOnTouchListener(new View.OnTouchListener(){float x;public boolean onTouch(View v,android.view.MotionEvent e){if(e.getAction()==0)x=e.getRawX();if(e.getAction()==1 && x-e.getRawX()>40*getResources().getDisplayMetrics().density){startActivity(home);stopSelf();}return true;}});try{manager.addView(strip,p);}catch(RuntimeException e){strip=null;stopSelf();}
   screen=new BroadcastReceiver(){public void onReceive(Context c,Intent e){stopSelf();}};registerReceiver(screen,new IntentFilter(Intent.ACTION_SCREEN_OFF));
  }return START_NOT_STICKY;
 }
 public void onDestroy(){if(strip!=null){manager.removeView(strip);strip=null;}if(screen!=null)unregisterReceiver(screen);super.onDestroy();}
}
