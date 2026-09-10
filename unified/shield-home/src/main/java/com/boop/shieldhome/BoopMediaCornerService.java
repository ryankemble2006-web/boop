package com.boop.shieldhome;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;
import com.boop.shared.BoopState;

/** A non-touchable media-only host; it owns no microphone, playback, or HA connection. */
public final class BoopMediaCornerService extends Service {
    private static BoopMediaCornerService active;
    private WindowManager windows;
    private ShieldNowPlayingPuppetView view;
    private Runnable unsubscribe;
    public static void hideNow() { if(active != null) active.remove(); }
    @Override public void onCreate() {
        super.onCreate(); active=this;
        NotificationManager notifications=getSystemService(NotificationManager.class);
        notifications.createNotificationChannel(new NotificationChannel("boop_media_corner","BOOP media",NotificationManager.IMPORTANCE_LOW));
        startForeground(1092,new Notification.Builder(this,"boop_media_corner")
                .setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("BOOP media puppet")
                .setContentText("Shown only while media is playing").build());
        windows=getSystemService(WindowManager.class);
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        if(!Settings.canDrawOverlays(this) || BoopState.INSTANCE.snapshot().owner != BoopState.Owner.MEDIA_CORNER) {
            remove(); stopSelf(); return START_NOT_STICKY;
        }
        if(view == null) {
            view=new ShieldNowPlayingPuppetView(this);
            view.setPresentationOwner(BoopState.Owner.MEDIA_CORNER);
            int width=Math.round(260*getResources().getDisplayMetrics().density);
            int height=Math.round(180*getResources().getDisplayMetrics().density);
            WindowManager.LayoutParams p=new WindowManager.LayoutParams(width,height,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT);
            p.gravity=Gravity.TOP|Gravity.RIGHT;
            try { windows.addView(view,p); }
            catch(RuntimeException unavailable) { view=null; stopSelf(); return START_NOT_STICKY; }
            unsubscribe=ShieldNowPlayingManager.get(this).state().subscribe(snapshot -> { if(view != null) view.setSnapshot(snapshot); });
        }
        return START_NOT_STICKY;
    }
    private void remove() {
        if(unsubscribe != null) { unsubscribe.run(); unsubscribe=null; }
        if(view != null) { windows.removeViewImmediate(view); view=null; }
    }
    @Override public void onDestroy() { remove(); if(active==this) active=null; super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
