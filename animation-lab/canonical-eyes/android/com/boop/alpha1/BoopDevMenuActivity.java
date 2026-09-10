package com.boop.alpha1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Choreographer;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.FrameLayout;
import com.boop.eyes.*;

/** Existing lab identity; no production services, permissions or network access. */
public final class BoopDevMenuActivity extends Activity implements Choreographer.FrameCallback {
    private GLSurfaceView surface;
    private CanonicalEyeRenderer renderer;
    private FrameLayout stage;
    private NotificationSignView sign;
    private boolean signActive;
    private int signStyle;
    private double signStart;
    private TextView label;
    private EyeMotion.Controller controller;
    private boolean resumed,focused,running,slow,motionOff;
    private long lastFrame;
    private double clock;
    private int freeze=-1;
    private boolean reducedMotion;
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        controller=new EyeMotion.Controller(EyeCatalogue.find("idle"),0,20260910);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.BLACK);
        label=new TextView(this);label.setTextColor(Color.WHITE);label.setTextSize(20);label.setPadding(24,12,24,8);
        root.addView(label,new LinearLayout.LayoutParams(-1,-2));
        surface=new GLSurfaceView(this);surface.setEGLContextClientVersion(2);surface.setPreserveEGLContextOnPause(true);
        renderer=new CanonicalEyeRenderer(getAssets(),detail->runOnUiThread(()->label.setText("Renderer error: "+detail)));
        surface.setRenderer(renderer);surface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        stage=new FrameLayout(this);stage.addView(surface,new FrameLayout.LayoutParams(-1,-1));
        sign=new NotificationSignView(this);sign.setVisibility(View.GONE);
        stage.addView(sign,new FrameLayout.LayoutParams(-1,-1));
        stage.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->resizeEyes());
        root.addView(stage,new LinearLayout.LayoutParams(-1,0,1));
        for(int row=0;row<2;row++){
            HorizontalScrollView scroll=new HorizontalScrollView(this);LinearLayout strip=new LinearLayout(this);
            for(int i=row*13;i<Math.min((row+1)*13,EyeCatalogue.ALL.length);i++){
                final EyeMotion.Clip clip=EyeCatalogue.ALL[i];Button button=button(clip.label);
                button.setOnClickListener(v->select(clip.id));strip.addView(button,new LinearLayout.LayoutParams(dp(180),dp(62)));
            }
            scroll.addView(strip);root.addView(scroll,new LinearLayout.LayoutParams(-1,-2));
        }
        LinearLayout controls=new LinearLayout(this);
        Button speed=button("Slow review");speed.setOnClickListener(v->{slow=!slow;speed.setText(slow?"Normal speed":"Slow review");});controls.addView(speed);
        Button motion=button("Pause motion");motion.setOnClickListener(v->{motionOff=!motionOff;motion.setText(motionOff?"Resume motion":"Pause motion");});controls.addView(motion);
        root.addView(controls);
        HorizontalScrollView signScroll=new HorizontalScrollView(this);LinearLayout signButtons=new LinearLayout(this);
        String[] signNames={"WhatsApp sign","Gmail sign","Facebook sign","X sign","Freddie"};
        for(int i=0;i<signNames.length;i++){final int style=i;Button b=button(signNames[i]);b.setOnClickListener(v->showSign(style));signButtons.addView(b,new LinearLayout.LayoutParams(dp(200),dp(54)));}
        signScroll.addView(signButtons);root.addView(signScroll);setContentView(root);
        // Content is attached before applying immersive flags (v0.4 lifecycle lesson).
        root.post(()->getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY));
        readIntent(getIntent());
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private Button button(String title){Button b=new Button(this);b.setText(title);b.setTextSize(16);b.setFocusable(true);return b;}
    private void select(String id){
        signActive=false;if(sign!=null){sign.setVisibility(View.GONE);resizeEyes();}
        freeze=-1;EyeMotion.Clip c=EyeCatalogue.find(id);
        controller.select(c,(long)clock,(id.equals("blink")||id.equals("double_blink")||id.equals("wake"))?0:160);
        label.setText("BOOP • "+c.label+"  |  Canonical eye code • v12 • Ryan review");
        Log.i("BOOPEyes","clip="+c.id+" time="+(long)clock);
    }
    private void resizeEyes(){
        int h=signActive?Math.max(1,(int)(stage.getHeight()*0.64f)):-1;
        if(surface.getLayoutParams().height!=h){FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,h);surface.setLayoutParams(p);}
    }
    private void showSign(int style){
        signStyle=Math.floorMod(style,5);signStart=clock;signActive=true;freeze=-1;
        sign.setVisibility(View.VISIBLE);resizeEyes();
        label.setText("BOOP • Puppet show  |  "+new String[]{"WhatsApp","Gmail","Facebook","X","Freddie"}[signStyle]+" • v12 • Demo only");
        Log.i("BOOPEyes","sign="+signStyle+" time="+(long)clock);
    }
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);readIntent(intent);}
    private void readIntent(Intent intent){
        String id=intent==null?null:intent.getStringExtra("clip");select(id==null?"idle":id);
        if(intent!=null&&intent.hasExtra("sign"))showSign(intent.getIntExtra("sign",0));
        if(intent!=null){freeze=intent.getIntExtra("freeze_ms",-1);slow=intent.getBooleanExtra("slow",false);}
    }
    @Override protected void onResume(){super.onResume();resumed=true;surface.onResume();
        PowerManager power=(PowerManager)getSystemService(POWER_SERVICE);
        reducedMotion=Settings.Global.getFloat(getContentResolver(),Settings.Global.ANIMATOR_DURATION_SCALE,1f)==0f||(power!=null&&power.isPowerSaveMode());
        updateLoop();}
    @Override protected void onPause(){resumed=false;updateLoop();surface.onPause();super.onPause();}
    @Override public void onWindowFocusChanged(boolean hasFocus){super.onWindowFocusChanged(hasFocus);focused=hasFocus;if(surface!=null)updateLoop();}
    private void updateLoop(){
        boolean shouldRun=resumed&&focused;
        if(shouldRun&&!running){running=true;lastFrame=0;Choreographer.getInstance().postFrameCallback(this);Log.i("BOOPEyes","clock-resumed");}
        else if(!shouldRun&&running){running=false;Choreographer.getInstance().removeFrameCallback(this);lastFrame=0;Log.i("BOOPEyes","clock-paused");}
    }
    @Override public void doFrame(long time){
        if(!running)return;
        if(lastFrame!=0&&!motionOff)clock+=Math.min(100,(time-lastFrame)/1000000.0)*(slow?0.15:1);
        lastFrame=time;
        EyeMotion.Clip clip=controller.clip();
        if(signActive){
            double elapsed=freeze>=0?freeze:reducedMotion?10000:clock-signStart;
            SignMotion.Pose p=signStyle==4?FreddieMotion.sample(elapsed):SignMotion.sample(elapsed,signStyle);
            renderer.pose=p.eyes;if(signStyle==4)sign.showFreddie(p);else sign.show(p,signStyle);
        }else renderer.pose=freeze>=0?clip.sample(freeze):reducedMotion?clip.sample(clip.loop?0:clip.duration):controller.sample((long)clock);
        surface.requestRender();Choreographer.getInstance().postFrameCallback(this);
    }
}
