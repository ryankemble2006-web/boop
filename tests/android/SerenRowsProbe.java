package local.boop.serenaudit;

import android.app.*;import android.content.*;import android.content.res.*;import android.graphics.*;
import android.os.*;import android.view.*;import android.widget.*;
import java.lang.reflect.*;import java.util.*;

/** Uses real installed Home classes with isolated preferences, no network or playback. */
public final class SerenRowsProbe extends Activity {
    View home; String selected="", roomSelected=""; Throwable creationFailure;
    final List<String> failures=new ArrayList<>();
    Method bind; Constructor<?> episodeConstructor; List<Object> episodes;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);getWindow().getDecorView().setSystemUiVisibility(5894);
        try {
            Context installed=createPackageContext(getIntent().getStringExtra("target_package"),CONTEXT_INCLUDE_CODE|CONTEXT_IGNORE_SECURITY);
            ClassLoader loader=installed.getClassLoader();Configuration cfg=new Configuration(installed.getResources().getConfiguration());
            cfg.densityDpi=256;cfg.fontScale=1.3f;
            Context layout=installed.createConfigurationContext(cfg);
            Context isolated=new ContextWrapper(this){
                public Resources getResources(){return layout.getResources();}
                public AssetManager getAssets(){return layout.getAssets();}
                public ClassLoader getClassLoader(){return loader;}
            };
            Class<?> type=loader.loadClass("com.boop.shieldhome.ShieldHomeView");
            Class<?> episodeType=loader.loadClass("com.boop.shieldhome.SerenEpisode");
            Class<?> callbacksType=loader.loadClass("com.boop.shieldhome.ShieldHomeView$Callbacks");
            Constructor<?> constructor=episodeType.getDeclaredConstructor(String.class,String.class,String.class,String.class);constructor.setAccessible(true);
            episodeConstructor=constructor;
            episodes=new ArrayList<>();for(int i=1;i<=12;i++)episodes.add(constructor.newInstance("Example show "+i,"01x02 Next episode","plugin://plugin.video.seren/?action=getSources&action_args="+i,""));
            Object callbacks=Proxy.newProxyInstance(loader,new Class<?>[]{callbacksType},(p,m,args)->{
                if(m.getName().equals("onSerenEpisodeSelected")){Field f=episodeType.getDeclaredField("file");f.setAccessible(true);selected=(String)f.get(args[0]);}
                if(m.getName().equals("onRoomDeviceSelected"))roomSelected=(String)args[1];return null;
            });
            home=(View)type.getConstructor(Context.class).newInstance(isolated);
            type.getMethod("setSerenEnabled",boolean.class).invoke(home,true);
            bind=type.getDeclaredMethod("setSeren",List.class,String.class,loader.loadClass("com.boop.shieldhome.SerenPosterLoader"));bind.setAccessible(true);bind.invoke(home,episodes,"",null);
            type.getMethod("render",List.class,List.class,callbacksType).invoke(home,List.of(),List.of(),callbacks);
            Class<?> area=loader.loadClass("com.boop.shieldoverlay.AreaInfo"),entity=loader.loadClass("com.boop.shieldoverlay.EntityCard"),phase=loader.loadClass("com.boop.shieldoverlay.RoomPanelController$Phase"),stateType=loader.loadClass("com.boop.shieldoverlay.RoomPanelController$State");
            Object room=area.getConstructor(String.class,String.class).newInstance("test_room","Home Assistant");
            Object lamp=entity.getConstructor(String.class,String.class,String.class,String.class,boolean.class,String.class).newInstance("light.test","test_room","Test lamp","off",false,null);
            Constructor<?> sc=stateType.getDeclaredConstructor(long.class,area,phase,List.class,String.class,String.class);sc.setAccessible(true);
            type.getMethod("setRoomPanelState",stateType,boolean.class).invoke(home,sc.newInstance(1L,room,phase.getField("LIVE").get(null),List.of(lamp),null,null),true);
            setContentView(home);
        } catch(Throwable e){creationFailure=e;TextView error=new TextView(this);error.setText(e.toString());setContentView(error);}
    }
    static View find(View view,String text){
        if(text.equals(String.valueOf(view.getContentDescription()))||(view instanceof TextView&&text.equals(((TextView)view).getText().toString())))return view;
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){View result=find(((ViewGroup)view).getChildAt(i),text);if(result!=null)return result;}return null;
    }
    static boolean full(View v){Rect r=new Rect();return v!=null&&v.getGlobalVisibleRect(r)&&r.height()>=v.getHeight()-2&&r.width()>=v.getWidth()-2;}
    public static final class ProbeInstrumentation extends Instrumentation {
        Bundle args;
        public void onCreate(Bundle args){this.args=args;start();}
        public void onStart(){Bundle result=new Bundle();try{
            setInTouchMode(false);
            SerenRowsProbe a=(SerenRowsProbe)startActivitySync(new Intent(getTargetContext(),SerenRowsProbe.class).putExtra("target_package",args.getString("target_package","com.boop.shieldoverlay")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            SystemClock.sleep(1800);waitForIdleSync();
            if(a.creationFailure!=null)throw new AssertionError("Cannot render Seren row",a.creationFailure);
            runOnMainSync(()->{
                find(a.home,"Add favourites").requestFocus();
                View card=find(a.home,"Example show 1: 01x02 Next episode");
                if(!full(card))a.failures.add("First poster is not fully visible on initial screen");
                Rect room=new Rect();View lamp=find(a.home,"Test lamp, Off");
                if(lamp==null)a.failures.add("HA controls missing");else if(lamp.getGlobalVisibleRect(room))a.failures.add("HA should start off screen");
            });
            capture("seren-initial");
            step(a,KeyEvent.KEYCODE_DPAD_DOWN,"Example show 1: 01x02 Next episode");
            step(a,KeyEvent.KEYCODE_DPAD_RIGHT,"Example show 2: 01x02 Next episode");
            sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);waitForIdleSync();
            if(!a.selected.endsWith("action_args=2"))a.failures.add("Clicked wrong episode");
            capture("seren-focused");
            step(a,KeyEvent.KEYCODE_DPAD_DOWN,"Test lamp, Off");
            sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);waitForIdleSync();
            if(!a.roomSelected.equals("light.test"))a.failures.add("HA action inaccessible");
            capture("seren-ha");
            step(a,KeyEvent.KEYCODE_DPAD_UP,"Example show 2: 01x02 Next episode");
            step(a,KeyEvent.KEYCODE_DPAD_UP,"Add favourites");capture("seren-returned");
            runOnMainSync(()->{try{
                find(a.home,"Example show 2: 01x02 Next episode").requestFocus();
                a.episodes.set(1,a.episodeConstructor.newInstance("Example show 2","01x03 Following episode","plugin://plugin.video.seren/?action=getSources&action_args=advanced",""));
                a.bind.invoke(a.home,a.episodes,"",null);
                View advanced=find(a.home,"Example show 2: 01x03 Following episode");
                if(advanced==null||!advanced.hasFocus())a.failures.add("Advancing episode lost row focus");
                a.bind.invoke(a.home,List.of(),"You're caught up",null);
                View caughtUp=find(a.home,"You're caught up · Open Kodi");
                if(caughtUp==null||!caughtUp.hasFocus())a.failures.add("Empty refresh lost row focus");
                a.bind.invoke(a.home,List.of(),"Open Kodi to load Next Up",null);
                if(find(a.home,"Open Kodi to load Next Up · Open Kodi")==null)a.failures.add("Unchanged empty list did not update status");
            }catch(Exception e){throw new RuntimeException(e);}});
            artworkRetry(a);
            String verdict=a.failures.isEmpty()?"PASS Seren initial posters, offscreen HA, D-pad traversal and exact selection":"FAIL "+a.failures;
            result.putString("stream",verdict+"\n");finish(a.failures.isEmpty()?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);
        }catch(Throwable t){result.putString("stream","FAIL "+android.util.Log.getStackTraceString(t));finish(Activity.RESULT_CANCELED,result);}}
        void step(SerenRowsProbe a,int key,String expected){sendKeyDownUpSync(key);SystemClock.sleep(450);waitForIdleSync();runOnMainSync(()->{View v=find(a.home,expected);if(v==null||!v.hasFocus()||!full(v))a.failures.add("Focus/visibility: "+expected);});}
        void artworkRetry(SerenRowsProbe a)throws Exception{
            java.util.concurrent.atomic.AtomicInteger requests=new java.util.concurrent.atomic.AtomicInteger();
            Bitmap pixel=Bitmap.createBitmap(4,6,Bitmap.Config.ARGB_8888);pixel.eraseColor(Color.GREEN);
            java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();pixel.compress(Bitmap.CompressFormat.PNG,100,out);pixel.recycle();byte[] png=out.toByteArray();
            try(java.net.ServerSocket server=new java.net.ServerSocket(0,2,java.net.InetAddress.getByName("127.0.0.1"))){
                server.setSoTimeout(10000);
                Thread responder=new Thread(()->{try{for(int i=0;i<2;i++)try(java.net.Socket s=server.accept()){
                    java.io.BufferedReader in=new java.io.BufferedReader(new java.io.InputStreamReader(s.getInputStream()));String line;
                    while((line=in.readLine())!=null&&!line.isEmpty()){}
                    boolean ok=i==1;byte[] header=("HTTP/1.1 "+(ok?"200 OK":"503 Unavailable")+"\r\nContent-Length: "+(ok?png.length:0)+"\r\nConnection: close\r\n\r\n").getBytes(java.nio.charset.StandardCharsets.US_ASCII);
                    s.getOutputStream().write(header);if(ok)s.getOutputStream().write(png);s.getOutputStream().flush();requests.incrementAndGet();
                }}catch(Exception ignored){}});responder.start();
                Class<?> loaderType=a.bind.getParameterTypes()[2];Constructor<?> c=loaderType.getDeclaredConstructor(Context.class);c.setAccessible(true);Object loader=c.newInstance(a);
                List<Object> art=List.of(a.episodeConstructor.newInstance("Retry poster","Test episode","plugin://plugin.video.seren/?action=getSources&action_args=retry","http://127.0.0.1:"+server.getLocalPort()+"/poster.png"));
                runOnMainSync(()->{try{a.bind.invoke(a.home,art,"",loader);}catch(Exception e){throw new RuntimeException(e);}});
                long end=SystemClock.elapsedRealtime()+5000;while(requests.get()<1&&SystemClock.elapsedRealtime()<end)SystemClock.sleep(50);
                SystemClock.sleep(350);waitForIdleSync();
                runOnMainSync(()->{try{a.bind.invoke(a.home,art,"",loader);}catch(Exception e){throw new RuntimeException(e);}});
                end=SystemClock.elapsedRealtime()+5000;while(requests.get()<2&&SystemClock.elapsedRealtime()<end)SystemClock.sleep(50);
                SystemClock.sleep(350);waitForIdleSync();
                runOnMainSync(()->{View card=find(a.home,"Retry poster: Test episode");ImageView image=card instanceof ViewGroup?(ImageView)((ViewGroup)card).getChildAt(1):null;
                    if(requests.get()!=2||image==null||image.getDrawable()==null)a.failures.add("Unchanged feed did not retry transient artwork failure: requests="+requests.get()+", image="+(image!=null)+", visible="+(image!=null&&full(image))+", tag="+(image==null?null:image.getTag()));});
                capture("seren-art-retry");
                Method close=loaderType.getDeclaredMethod("close");close.setAccessible(true);close.invoke(loader);responder.join(1000);
            }
        }
        void capture(String name)throws Exception{Bitmap b=getUiAutomation().takeScreenshot();try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(getTargetContext().getExternalFilesDir(null),name+".png"))){b.compress(Bitmap.CompressFormat.PNG,100,out);}b.recycle();}
    }
}
