package com.boop.alpha1;

import android.app.*;
import android.content.*;
import android.os.*;
import com.boop.eyes.PuppetPreferences;
import com.boop.eyes.StyleState;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/** Appearance-only sharing on a trusted LAN, with an optional HA state mirror. */
public final class BoopPuppetSyncService extends Service {
    private static final int PORT=49322, ID=49323;
    private static final String CHANNEL="boop_style_sharing";
    private final Handler main=new Handler(Looper.getMainLooper());
    private final ScheduledExecutorService haWorker=Executors.newSingleThreadScheduledExecutor();
    private volatile boolean running, applying;
    private volatile DatagramSocket socket;
    private StyleState state;
    private SharedPreferences storage;
    private Runnable unwatch;
    private BoopPuppetHaMirror ha;
    public static void startFromActivity(Activity activity) {
        if(!PuppetPreferences.syncEnabled(activity))return;
        try { activity.startForegroundService(new Intent(activity,BoopPuppetSyncService.class)); }
        catch(RuntimeException unavailable) { android.util.Log.w("BOOPStyle","Sharing start deferred until the app is active"); }
    }
    @Override public IBinder onBind(Intent intent){return null;}
    @Override public void onCreate() {
        super.onCreate();
        if(!PuppetPreferences.syncEnabled(this)){stopSelf();return;}
        storage=getSharedPreferences("boop_style_register",MODE_PRIVATE);
        String id=storage.getString("device_id",null);
        if(id==null){id=UUID.randomUUID().toString();storage.edit().putString("device_id",id).apply();}
        state=new StyleState(id);
        state.seed("hue",PuppetPreferences.hue(this));state.seed("pitch",PuppetPreferences.pitch(this));
        state.seed("rate",PuppetPreferences.rate(this));state.seed("speed",PuppetPreferences.speed(this));
        try {String saved=storage.getString("state",null);if(saved!=null)state.merge(saved);}catch(IllegalArgumentException corrupt){/* Keep valid local controls. */}
        NotificationManager nm=getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel(CHANNEL,"BOOP device sharing",NotificationManager.IMPORTANCE_MIN));
        PendingIntent stop=PendingIntent.getService(this,ID,new Intent(this,BoopPuppetSyncService.class).setAction("STOP"),PendingIntent.FLAG_IMMUTABLE);
        PendingIntent open=PendingIntent.getActivity(this,ID,new Intent(this,BoopPuppetSettingsActivity.class),PendingIntent.FLAG_IMMUTABLE);
        Notification notice=new Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_menu_share)
                .setContentTitle("BOOP device sharing").setContentText("Eyes and voice stay together on your home network")
                .setContentIntent(open).setOngoing(true).setShowWhen(false)
                .addAction(new Notification.Action.Builder(null,"Stop sharing",stop).build()).build();
        startForeground(ID,notice,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
        running=true; applyMerged();
        unwatch=PuppetPreferences.watch(this,this::localChanged);
        new Thread(this::networkLoop,"boop-style-lan").start();
        ha=new BoopPuppetHaMirror(this);
        haWorker.scheduleWithFixedDelay(this::mirrorHa,0,4,TimeUnit.SECONDS);
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        if(intent!=null && "STOP".equals(intent.getAction()))PuppetPreferences.syncEnabled(this,false);
        if(!PuppetPreferences.syncEnabled(this)){stopSelf();return START_NOT_STICKY;}
        return START_STICKY;
    }
    private void localChanged() {
        if(!PuppetPreferences.syncEnabled(this)){stopSelf();return;}
        if(applying || state==null)return;
        boolean changed=state.edit("hue",PuppetPreferences.hue(this));
        changed|=state.edit("pitch",PuppetPreferences.pitch(this));changed|=state.edit("rate",PuppetPreferences.rate(this));
        changed|=state.edit("speed",PuppetPreferences.speed(this));
        if(changed){persist();android.util.Log.i("BOOPStyle","local character change queued");}
    }
    private void persist(){storage.edit().putString("state",state.encode()).apply();}
    private void applyMerged() {
        main.post(()->{
            if(!running)return;
            applying=true;
            try {
                PuppetPreferences.hue(this,(int)state.value("hue"));PuppetPreferences.pitch(this,(float)state.value("pitch"));
                PuppetPreferences.rate(this,(float)state.value("rate"));PuppetPreferences.speed(this,(float)state.value("speed"));persist();
            } finally {applying=false;}
            android.util.Log.i("BOOPStyle","shared character state applied");
        });
    }
    private void status(String message) {
        if(!running)return;
        SharedPreferences p=PuppetPreferences.prefs(this,"boop_puppet");
        if(!message.equals(p.getString("sync_status","")))p.edit().putString("sync_status",message).apply();
    }
    private void mirrorHa() {
        if(!running)return;
        try {if(ha.exchange(state)){applyMerged();}status(ha.available()?"Sharing through Home Assistant and your home network":"Sharing on your home network");}
        catch(Exception unavailable){status("Sharing on your home network; Home Assistant is not available");}
    }
    private void networkLoop() {
        while(running) {
            try(DatagramSocket endpoint=new DatagramSocket(PORT)) {
                socket=endpoint;endpoint.setBroadcast(true);endpoint.setSoTimeout(600);
                long sent=0, lastAccepted=0;
                while(running) {
                    long now=SystemClock.elapsedRealtime();
                    if(now-sent>=1800){broadcast(endpoint);sent=now;}
                    byte[] data=new byte[2049];DatagramPacket packet=new DatagramPacket(data,data.length);
                    try { endpoint.receive(packet); }catch(SocketTimeoutException waiting){continue;}
                    if(packet.getLength()>2048 || !packet.getAddress().isSiteLocalAddress())continue;
                    if(now-lastAccepted<25)continue;
                    lastAccepted=now;
                    try {
                        String incoming=new String(packet.getData(),packet.getOffset(),packet.getLength(),StandardCharsets.UTF_8);
                        if(state.merge(incoming))applyMerged();
                    } catch(IllegalArgumentException invalid){/* No commands or unknown fields are accepted. */}
                }
            } catch(Exception unavailable) {
                if(running){status("Waiting for the home network");try{Thread.sleep(1800);}catch(InterruptedException stop){return;}}
            } finally {socket=null;}
        }
    }
    private void broadcast(DatagramSocket endpoint) throws Exception {
        android.net.ConnectivityManager cm=getSystemService(android.net.ConnectivityManager.class);
        android.net.NetworkCapabilities net=cm.getNetworkCapabilities(cm.getActiveNetwork());
        if(net==null || (!net.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                && !net.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)))return;
        byte[] data=state.encode().getBytes(StandardCharsets.UTF_8);
        Set<InetAddress> targets=new HashSet<>();
        Enumeration<NetworkInterface> interfaces=NetworkInterface.getNetworkInterfaces();
        while(interfaces!=null && interfaces.hasMoreElements()) {
            NetworkInterface iface=interfaces.nextElement();
            if(!iface.isUp() || iface.isLoopback())continue;
            for(InterfaceAddress address:iface.getInterfaceAddresses())
                if(address.getBroadcast()!=null && address.getAddress().isSiteLocalAddress())targets.add(address.getBroadcast());
        }
        targets.add(InetAddress.getByName("255.255.255.255"));
        for(InetAddress target:targets) {
            try{endpoint.send(new DatagramPacket(data,data.length,target,PORT));}catch(java.io.IOException ignored){/* Retry with the next gossip. */}
        }
    }
    @Override public void onDestroy() {
        running=false;
        if(socket!=null)socket.close();
        if(unwatch!=null){unwatch.run();unwatch=null;}
        main.removeCallbacksAndMessages(null);haWorker.shutdownNow();
        stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy();
    }
}
