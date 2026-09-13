package com.boop.alpha1;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Choreographer;
import android.view.View;
import android.widget.*;
import com.boop.eyes.*;
import com.boop.lab.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BoopDevMenuActivity extends Activity implements Choreographer.FrameCallback {
    private GLSurfaceView surface; private CanonicalEyeRenderer renderer; private FrameLayout stage;
    private NotificationSignView sign; private boolean signActive; private int signStyle; private double signStart;
    private TextView label,peerLabel; private Button receiverButton; private EyeMotion.Controller controller;
    private boolean resumed,focused,running,motionOff,receiverEnabled; private double speedMultiplier=1.0;
    private float androidAnimatorScale=1f; private long lastFrame; private double clock; private int freeze=-1;
    private boolean reducedMotion; private LabLanClient.Peer peer; private final ExecutorService net=Executors.newSingleThreadExecutor();

    @Override public void onCreate(Bundle state){
        super.onCreate(state);controller=new EyeMotion.Controller(EyeCatalogue.find("idle"),0,20260910);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.BLACK);
        label=new TextView(this);label.setTextColor(Color.WHITE);label.setTextSize(20);label.setPadding(24,12,24,8);root.addView(label,new LinearLayout.LayoutParams(-1,-2));
        surface=new GLSurfaceView(this);surface.setEGLContextClientVersion(2);surface.setPreserveEGLContextOnPause(true);
        renderer=new CanonicalEyeRenderer(getAssets(),detail->runOnUiThread(()->label.setText("Renderer error: "+detail)));
        surface.setRenderer(renderer);surface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        stage=new FrameLayout(this);stage.addView(surface,new FrameLayout.LayoutParams(-1,-1));sign=new NotificationSignView(this);sign.setVisibility(View.GONE);
        stage.addView(sign,new FrameLayout.LayoutParams(-1,-1));stage.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->resizeEyes());
        root.addView(stage,new LinearLayout.LayoutParams(-1,0,1));addAnimationRows(root);addSpeedRow(root);addSignRow(root);addLanRow(root);
        setContentView(root);root.post(()->getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY));
        receiverEnabled=getSharedPreferences("lab",MODE_PRIVATE).getBoolean("receiver",false);updateReceiverButton();if(receiverEnabled)startReceiverService();readIntent(getIntent());
    }
    private void addAnimationRows(LinearLayout root){for(int row=0;row<2;row++){HorizontalScrollView scroll=new HorizontalScrollView(this);LinearLayout strip=new LinearLayout(this);
        for(int i=row*13;i<Math.min((row+1)*13,EyeCatalogue.ALL.length);i++){final EyeMotion.Clip clip=EyeCatalogue.ALL[i];Button b=button(clip.label);b.setOnClickListener(v->select(clip.id));strip.addView(b,new LinearLayout.LayoutParams(dp(180),dp(62)));}
        scroll.addView(strip);root.addView(scroll,new LinearLayout.LayoutParams(-1,-2));}}
    private void addSpeedRow(LinearLayout root){LinearLayout controls=new LinearLayout(this);for(double value:new double[]{0.5,1.0,1.5,2.0}){final double speed=value;Button b=button(speed+"x");b.setOnClickListener(v->{speedMultiplier=speed;label.setText("BOOP | Animation speed "+speed+"x | Android UI scale "+androidAnimatorScale+"x");});controls.addView(b);}
        Button motion=button("Pause motion");motion.setOnClickListener(v->{motionOff=!motionOff;motion.setText(motionOff?"Resume motion":"Pause motion");});controls.addView(motion);root.addView(controls);}
    private void addSignRow(LinearLayout root){HorizontalScrollView scroll=new HorizontalScrollView(this);LinearLayout strip=new LinearLayout(this);String[] names={"WhatsApp sign","Gmail sign","Facebook sign","X sign"};
        for(int i=0;i<names.length;i++){final int style=i;Button b=button(names[i]);b.setOnClickListener(v->showSign(style));strip.addView(b,new LinearLayout.LayoutParams(dp(200),dp(54)));}scroll.addView(strip);root.addView(scroll);}
    private void addLanRow(LinearLayout root){HorizontalScrollView scroll=new HorizontalScrollView(this);LinearLayout strip=new LinearLayout(this);
        receiverButton=button("Test receiver: Off");receiverButton.setOnClickListener(v->toggleReceiver());strip.addView(receiverButton,new LinearLayout.LayoutParams(dp(230),dp(58)));
        Button discover=button("Discover device");discover.setOnClickListener(v->discoverPeer());strip.addView(discover,new LinearLayout.LayoutParams(dp(220),dp(58)));
        peerLabel=new TextView(this);peerLabel.setTextColor(Color.CYAN);peerLabel.setTextSize(15);peerLabel.setGravity(android.view.Gravity.CENTER_VERTICAL);peerLabel.setText("No target");strip.addView(peerLabel,new LinearLayout.LayoutParams(dp(300),dp(58)));
        for(String type:new String[]{"BASIC","MESSAGE","PRIVATE","ACTIONABLE"}){Button b=button(type);b.setOnClickListener(v->sendTest(type));strip.addView(b,new LinearLayout.LayoutParams(dp(190),dp(58)));}
        scroll.addView(strip);root.addView(scroll,new LinearLayout.LayoutParams(-1,-2));}
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private Button button(String title){Button b=new Button(this);b.setText(title);b.setTextSize(16);b.setFocusable(true);return b;}
    private String deviceId(){return Build.MANUFACTURER+"-"+Build.MODEL+"-"+Build.DEVICE;}
    private String deviceName(){return Build.MANUFACTURER+" "+Build.MODEL;}

    private void toggleReceiver(){
        if(!receiverEnabled&&Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},4100);return;}
        if(receiverEnabled){stopService(new Intent(this,LabNotificationReceiverService.class));receiverEnabled=false;}
        else{receiverEnabled=true;startReceiverService();}
        getSharedPreferences("lab",MODE_PRIVATE).edit().putBoolean("receiver",receiverEnabled).apply();updateReceiverButton();
    }
    private void startReceiverService(){Intent i=new Intent(this,LabNotificationReceiverService.class);if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);}
    private void updateReceiverButton(){if(receiverButton!=null)receiverButton.setText(receiverEnabled?"Test receiver: On":"Test receiver: Off");}
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grants){super.onRequestPermissionsResult(requestCode,permissions,grants);if(requestCode==4100&&grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED){receiverEnabled=true;getSharedPreferences("lab",MODE_PRIVATE).edit().putBoolean("receiver",true).apply();startReceiverService();updateReceiverButton();}else if(requestCode==4100)label.setText("BOOP | Notification permission is needed for Pixel test receiver");}

    private void discoverPeer(){peer=null;peerLabel.setText("Searching...");net.submit(()->{try{LabLanClient.Peer found=LabLanClient.discover(deviceId(),deviceName());runOnUiThread(()->{peer=found;peerLabel.setText(found==null?"No BOOP Lab found":found.name);});}catch(Exception e){runOnUiThread(()->peerLabel.setText("Discovery failed"));Log.e("BOOPLabLAN","discovery failed",e);}});}
    private void sendTest(String type){
        LabLanClient.Peer target=peer;if(target==null){peerLabel.setText("Discover a device first");return;}
        String title="BOOP Lab "+type.toLowerCase()+" test";String text="Real Android notification sent from "+deviceName();
        net.submit(()->{try{LabLanClient.send(target,type,title,text);runOnUiThread(()->peerLabel.setText("Sent "+type+" to "+target.name));}catch(Exception e){runOnUiThread(()->peerLabel.setText("Send failed"));Log.e("BOOPLabLAN","send failed",e);}});
    }

    private void select(String id){signActive=false;if(sign!=null){sign.setVisibility(View.GONE);resizeEyes();}freeze=-1;EyeMotion.Clip c=EyeCatalogue.find(id);controller.select(c,(long)clock,(id.equals("blink")||id.equals("double_blink")||id.equals("wake"))?0:160);label.setText("BOOP | "+c.label+" | Canonical eye code | v13 | Ryan review");Log.i("BOOPEyes","clip="+c.id+" time="+(long)clock);}
    private void resizeEyes(){int h=signActive?Math.max(1,(int)(stage.getHeight()*0.64f)):-1;if(surface.getLayoutParams().height!=h){FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,h);surface.setLayoutParams(p);}}
    private void showSign(int style){signStyle=Math.floorMod(style,4);signStart=clock;signActive=true;freeze=-1;sign.setVisibility(View.VISIBLE);resizeEyes();label.setText("BOOP | Sign show | "+new String[]{"WhatsApp","Gmail","Facebook","X"}[signStyle]+" | v13 | Demo only");Log.i("BOOPEyes","sign="+signStyle+" time="+(long)clock);}
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);readIntent(intent);}
    private void readIntent(Intent intent){String id=intent==null?null:intent.getStringExtra("clip");select(id==null?"idle":id);if(intent!=null&&intent.hasExtra("sign"))showSign(intent.getIntExtra("sign",0));if(intent!=null){freeze=intent.getIntExtra("freeze_ms",-1);if(intent.getBooleanExtra("slow",false))speedMultiplier=0.15;}}
    @Override protected void onResume(){super.onResume();resumed=true;surface.onResume();PowerManager power=(PowerManager)getSystemService(POWER_SERVICE);androidAnimatorScale=Settings.Global.getFloat(getContentResolver(),Settings.Global.ANIMATOR_DURATION_SCALE,1f);reducedMotion=MotionPolicy.shouldReduce(power!=null&&power.isPowerSaveMode(),androidAnimatorScale);updateLoop();}
    @Override protected void onPause(){resumed=false;updateLoop();surface.onPause();super.onPause();}
    @Override protected void onDestroy(){net.shutdownNow();super.onDestroy();}
    @Override public void onWindowFocusChanged(boolean hasFocus){super.onWindowFocusChanged(hasFocus);focused=hasFocus;if(surface!=null)updateLoop();}
    private void updateLoop(){boolean shouldRun=resumed&&focused;if(shouldRun&&!running){running=true;lastFrame=0;Choreographer.getInstance().postFrameCallback(this);Log.i("BOOPEyes","clock-resumed");}else if(!shouldRun&&running){running=false;Choreographer.getInstance().removeFrameCallback(this);lastFrame=0;Log.i("BOOPEyes","clock-paused");}}
    @Override public void doFrame(long time){if(!running)return;if(lastFrame!=0&&!motionOff)clock+=MotionPolicy.scaledDelta(Math.min(100,(time-lastFrame)/1000000.0),speedMultiplier);lastFrame=time;EyeMotion.Clip clip=controller.clip();if(signActive){SignMotion.Pose p=SignMotion.sample(freeze>=0?freeze:reducedMotion?9000:clock-signStart,signStyle);renderer.pose=p.eyes;sign.show(p,signStyle);}else renderer.pose=freeze>=0?clip.sample(freeze):reducedMotion?clip.sample(clip.loop?0:clip.duration):controller.sample((long)clock);surface.requestRender();Choreographer.getInstance().postFrameCallback(this);}
}
