package com.boop.lab;

import android.app.*;
import android.content.*;
import android.os.Build;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LabNotificationReceiverService extends Service {
    public static final int PORT=49321;
    public static final String RECEIVER_CHANNEL="boop_lab_receiver";
    public static final String TEST_CHANNEL="boop_lab_tests";
    private final AtomicBoolean running=new AtomicBoolean(false);
    private DatagramSocket socket; private Thread worker;

    @Override public void onCreate(){super.onCreate();createChannels();startForeground(49320,receiverNotification());startSocket();}
    @Override public int onStartCommand(Intent intent,int flags,int startId){return START_STICKY;}
    @Override public android.os.IBinder onBind(Intent intent){return null;}
    @Override public void onDestroy(){running.set(false);if(socket!=null)socket.close();super.onDestroy();}

    private void createChannels(){
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel(RECEIVER_CHANNEL,"BOOP Lab receiver",NotificationManager.IMPORTANCE_MIN));
        nm.createNotificationChannel(new NotificationChannel(TEST_CHANNEL,"BOOP Lab test notifications",NotificationManager.IMPORTANCE_HIGH));
    }
    private Notification receiverNotification(){return new Notification.Builder(this,RECEIVER_CHANNEL)
        .setSmallIcon(android.R.drawable.stat_notify_sync).setContentTitle("BOOP Lab receiver running")
        .setContentText("Ready for Shield notification tests").setOngoing(true).build();}
    private String deviceId(){return Build.MANUFACTURER+"-"+Build.MODEL+"-"+Build.DEVICE;}
    private String deviceName(){return Build.MANUFACTURER+" "+Build.MODEL;}
    private void startSocket(){
        running.set(true);worker=new Thread(()->{
            try{
                socket=new DatagramSocket(null);socket.setReuseAddress(true);socket.bind(new InetSocketAddress(PORT));
                byte[] buf=new byte[4096];
                while(running.get()){
                    DatagramPacket packet=new DatagramPacket(buf,buf.length);socket.receive(packet);
                    String raw=new String(packet.getData(),packet.getOffset(),packet.getLength(),StandardCharsets.UTF_8);
                    LabPacket message;try{message=LabPacket.parse(raw);}catch(IllegalArgumentException ignored){continue;}
                    if("DISCOVER".equals(message.kind)){
                        byte[] out=LabPacket.here(deviceId(),deviceName()).encode().getBytes(StandardCharsets.UTF_8);
                        socket.send(new DatagramPacket(out,out.length,packet.getAddress(),packet.getPort()));
                    }else if("NOTIFY".equals(message.kind))postTest(message);
                }
            }catch(Exception e){if(running.get())android.util.Log.e("BOOPLabLAN","receiver failed",e);}
        },"boop-lab-receiver");worker.start();
    }

    private void postTest(LabPacket p){
        Notification.Builder b=new Notification.Builder(this,TEST_CHANNEL).setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(p.title).setContentText(p.text).setAutoCancel(true).setWhen(System.currentTimeMillis());
        if("MESSAGE".equals(p.testType))b.setStyle(new Notification.MessagingStyle("You").addMessage(p.text,System.currentTimeMillis(),"BOOP Test"));
        if("PRIVATE".equals(p.testType))b.setVisibility(Notification.VISIBILITY_PRIVATE)
            .setPublicVersion(new Notification.Builder(this,TEST_CHANNEL).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("BOOP private test").setContentText("Unlock to view").build());
        if("ACTIONABLE".equals(p.testType)){
            Intent open=new Intent(this,com.boop.alpha1.BoopDevMenuActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pi=PendingIntent.getActivity(this,49322,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            b.setContentIntent(pi).addAction(new Notification.Action.Builder(android.R.drawable.ic_media_play,"Open BOOP Lab",pi).build());
        }
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(50000+Math.abs(p.requestId.hashCode()%10000),b.build());
        android.util.Log.i("BOOPLabLAN","posted real test notification type="+p.testType+" request="+p.requestId);
    }
}
