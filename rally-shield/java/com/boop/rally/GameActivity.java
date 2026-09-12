package com.boop.rally;

import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;

/** Owns a separate :game process so two DOS instances can never share globals. */
public final class GameActivity extends Activity implements SurfaceHolder.Callback {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private Controls controls;
    private GameSpec game;
    private AlertDialog menu;
    private TextView hint;
    private boolean started,closing,active,windowFocused,focusGranted;
    private AudioManager audio;
    private AudioFocusRequest focusRequest;
    @Override public void onCreate(Bundle state){
        super.onCreate(state);game=GameSpec.find(getIntent().getStringExtra("game"));
        if(game==null){finish();return;}
        try{NativeBridge.initialize();}catch(UnsatisfiedLinkError e){new AlertDialog.Builder(this).setTitle("Rally engine could not start").setMessage("This APK requires an ARM64 Android device.").setPositiveButton("Back",(d,w)->finish()).show();return;}
        controls=new Controls(NativeBridge::key);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);TvUi.immersive(this);
        FrameLayout root=new FrameLayout(this);root.setBackgroundColor(0xff000000);
        SurfaceView screen=new SurfaceView(this);
        android.util.DisplayMetrics dm=new android.util.DisplayMetrics();getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        int height=Math.min(dm.heightPixels,dm.widthPixels*3/4),width=height*4/3;
        FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(width,height,Gravity.CENTER);root.addView(screen,sp);
        screen.getHolder().addCallback(this);
        hint=TvUi.text(this,"Starting "+game.title+"...   Back / Start opens the menu",16,0xffffffff);hint.setPadding(16,12,16,12);hint.setBackgroundColor(0xaa000000);
        root.addView(hint,new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));setContentView(root);
        audio=(AudioManager)getSystemService(AUDIO_SERVICE);
        focusRequest=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setOnAudioFocusChangeListener(change->{focusGranted=change==AudioManager.AUDIOFOCUS_GAIN;updatePause();},handler).build();
    }
    @Override public void surfaceCreated(SurfaceHolder holder){
        NativeBridge.surface(holder.getSurface());
        if(started || controls==null)return;started=true;
        File base=getExternalFilesDir(null);
        if(base==null){fail("Game storage is not available.");return;}
        File pack=new File(base,"games/"+game.id+".zip"),saves=new File(base,"saves/"+game.id),system=new File(getFilesDir(),"system");
        if((!saves.isDirectory()&&!saves.mkdirs()) || (!system.isDirectory()&&!system.mkdirs())){fail("Game storage could not be opened.");return;}
        new Thread(()->{
            String result;
            try{GameBundle.validate(pack,game);result=NativeBridge.run(pack.getAbsolutePath(),saves.getAbsolutePath(),system.getAbsolutePath(),game.id);}
            catch(Exception e){result=e.getMessage();}
            final String message=result;
            handler.post(()->{if(closing || message==null || message.isEmpty())finishGame();else fail(message);});
        },"Rally-emulation").start();
        handler.postDelayed(()->{if(!closing)hint.setVisibility(View.GONE);},7000);
        handler.postDelayed(diagnostics,5000);
    }
    private final Runnable diagnostics=new Runnable(){public void run(){if(!closing){android.util.Log.i("BoopRally",game.id+" "+NativeBridge.stats());handler.postDelayed(this,5000);}}};
    @Override public void surfaceChanged(SurfaceHolder h,int format,int width,int height){}
    @Override public void surfaceDestroyed(SurfaceHolder holder){if(controls!=null){controls.clear();NativeBridge.pause(true);NativeBridge.surface(null);}}
    private void updatePause(){
        if(controls==null)return;
        boolean paused=closing || !active || !windowFocused || !focusGranted || (menu!=null && menu.isShowing());
        if(paused)controls.clear();NativeBridge.pause(paused);
    }
    @Override public void onResume(){super.onResume();active=true;TvUi.immersive(this);if(audio!=null)focusGranted=audio.requestAudioFocus(focusRequest)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED;updatePause();}
    @Override public void onPause(){active=false;updatePause();if(audio!=null)audio.abandonAudioFocusRequest(focusRequest);super.onPause();}
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);windowFocused=focused;updatePause();}
    @Override public boolean dispatchKeyEvent(KeyEvent e){
        if(controls==null || closing || (menu!=null&&menu.isShowing()))return super.dispatchKeyEvent(e);
        int code=e.getKeyCode();
        if(code==KeyEvent.KEYCODE_BACK || code==KeyEvent.KEYCODE_BUTTON_START || code==KeyEvent.KEYCODE_MENU){if(e.getAction()==KeyEvent.ACTION_DOWN && e.getRepeatCount()==0)showMenu();return true;}
        if((e.getAction()==KeyEvent.ACTION_DOWN || e.getAction()==KeyEvent.ACTION_UP) && controls.key(code,e.getAction()==KeyEvent.ACTION_DOWN))return true;
        return super.dispatchKeyEvent(e);
    }
    @Override public boolean onGenericMotionEvent(MotionEvent e){
        if(controls!=null && !closing && (menu==null || !menu.isShowing()) && (e.getSource()&InputDevice.SOURCE_JOYSTICK)==InputDevice.SOURCE_JOYSTICK){
            float x=e.getAxisValue(MotionEvent.AXIS_X),y=e.getAxisValue(MotionEvent.AXIS_Y);
            if(Math.abs(e.getAxisValue(MotionEvent.AXIS_HAT_X))>.3f)x=e.getAxisValue(MotionEvent.AXIS_HAT_X);
            if(Math.abs(e.getAxisValue(MotionEvent.AXIS_HAT_Y))>.3f)y=e.getAxisValue(MotionEvent.AXIS_HAT_Y);
            controls.axes(x,y,Math.max(e.getAxisValue(MotionEvent.AXIS_LTRIGGER),e.getAxisValue(MotionEvent.AXIS_BRAKE)),Math.max(e.getAxisValue(MotionEvent.AXIS_RTRIGGER),e.getAxisValue(MotionEvent.AXIS_GAS)));return true;
        }return super.onGenericMotionEvent(e);
    }
    @Override public void onBackPressed(){showMenu();}
    private void showMenu(){
        if(closing || controls==null || (menu!=null&&menu.isShowing()))return;
        controls.clear();NativeBridge.pause(true);
        menu=new AlertDialog.Builder(this).setTitle(game.title+"  |  Paused")
            .setItems(new String[]{"Resume race","Press Enter / select","Press Escape / game back","Keyboard keys","Return to collection"},(d,n)->{
                if(n==4){returnToCollection();return;}
                if(n==3){handler.post(this::keyboard);return;}
                if(n==1 || n==2){final int key=n==1?13:27;handler.postDelayed(()->tap(key),150);}
            }).create();
        menu.setOnDismissListener(d->{menu=null;TvUi.immersive(this);updatePause();});menu.show();
    }
    private void tap(int key){if(closing)return;NativeBridge.key(key,true);handler.postDelayed(()->NativeBridge.key(key,false),170);}
    private void keyboard(){
        String[] labels={"Enter","Escape","Space","Tab","F1","F2","F3","F4","F5","F6","F7","F8","F9","F10","Y","N"};
        int[] keys={13,27,32,9,282,283,284,285,286,287,288,289,290,291,121,110};
        menu=new AlertDialog.Builder(this).setTitle("Press a game key").setItems(labels,(d,n)->handler.postDelayed(()->tap(keys[n]),150)).setNegativeButton("Back",null).create();
        menu.setOnDismissListener(d->{menu=null;updatePause();});menu.show();updatePause();
    }
    private void returnToCollection(){
        if(closing)return;closing=true;controls.clear();NativeBridge.pause(false);NativeBridge.stop();hint.setText("Saving game files and returning...");hint.setVisibility(View.VISIBLE);
        handler.postDelayed(()->{if(!isFinishing()){hint.setText("The game is taking longer to close. Press Home to leave safely.");}},5000);
    }
    private void fail(String message){NativeBridge.pause(true);new AlertDialog.Builder(this).setTitle("Rally could not start").setMessage(message==null?"Please check the game pack.":message).setCancelable(false).setPositiveButton("Return to collection",(d,w)->finishGame()).show();}
    private void finishGame(){closing=true;if(audio!=null)audio.abandonAudioFocusRequest(focusRequest);finish();handler.postDelayed(()->android.os.Process.killProcess(android.os.Process.myPid()),250);}
    @Override public void onDestroy(){closing=true;handler.removeCallbacks(diagnostics);if(controls!=null){controls.clear();NativeBridge.stop();NativeBridge.surface(null);}super.onDestroy();}
}
