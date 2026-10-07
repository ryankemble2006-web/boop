package local.boop.serenaudit;

import android.app.*;import android.content.*;import android.content.res.*;import android.graphics.*;
import android.os.*;import android.view.*;import android.widget.*;
import java.lang.reflect.*;import java.util.*;

/** Uses real installed Home classes with isolated preferences, no network or playback. */
public final class SerenRowsProbe extends Activity {
    View home, originalHome; String selected="", roomSelected=""; int roomPickerOpens; Context isolatedContext; ClassLoader installedLoader; Object originalRoomState; Throwable creationFailure;
    final List<String> failures=new ArrayList<>();
    Method bind; Constructor<?> episodeConstructor; List<Object> episodes;
    int originalTileWidth, originalTileHeight;
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
            isolatedContext=isolated; installedLoader=loader;
            Class<?> storeType=loader.loadClass("com.boop.shieldhome.ShieldHomeStore");
            Object preferences=storeType.getConstructor(Context.class).newInstance(isolated);
            storeType.getMethod("setAccentHue",int.class).invoke(preferences,20);
            Class<?> type=loader.loadClass("com.boop.shieldhome.ShieldHomeView");
            Class<?> episodeType=loader.loadClass("com.boop.shieldhome.SerenEpisode");
            Class<?> callbacksType=loader.loadClass("com.boop.shieldhome.ShieldHomeView$Callbacks");
            Constructor<?> constructor=episodeType.getDeclaredConstructor(String.class,String.class,String.class,String.class);constructor.setAccessible(true);
            episodeConstructor=constructor;
            episodes=new ArrayList<>();for(int i=1;i<=12;i++)episodes.add(constructor.newInstance("Example show "+i,"01x02 Next episode","plugin://plugin.video.seren/?action=getSources&action_args="+i,""));
            Object callbacks=Proxy.newProxyInstance(loader,new Class<?>[]{callbacksType},(p,m,args)->{
                if(m.getName().equals("onSerenEpisodeSelected")){Field f=episodeType.getDeclaredField("file");f.setAccessible(true);selected=(String)f.get(args[0]);}
                if(m.getName().equals("onRoomHeadingSelected"))roomPickerOpens++;
                if(m.getName().equals("onRoomDeviceSelected"))roomSelected=(String)args[1];return null;
            });
            home=(View)type.getConstructor(Context.class).newInstance(isolated);
            type.getMethod("setSerenEnabled",boolean.class).invoke(home,true);
            bind=type.getDeclaredMethod("setSeren",List.class,String.class,loader.loadClass("com.boop.shieldhome.SerenPosterLoader"));bind.setAccessible(true);bind.invoke(home,episodes,"",null);
            type.getMethod("render",List.class,List.class,callbacksType).invoke(home,List.of(),List.of(),callbacks);
            Class<?> snapshotType=loader.loadClass("com.boop.shieldhome.NowPlayingSnapshot");
            Bitmap cover=Bitmap.createBitmap(154,154,Bitmap.Config.ARGB_8888);cover.eraseColor(Color.DKGRAY);
            Object snapshot=snapshotType.getConstructor(long.class,String.class,String.class,String.class,int.class,long.class,long.class,long.class,float.class,long.class,Bitmap.class)
                .newInstance(999L,"deezer.android.app","Test track","Layout check",2,518L,30000L,180000L,1f,0L,cover);
            type.getMethod("setNowPlaying",snapshotType).invoke(home,snapshot);
            Class<?> area=loader.loadClass("com.boop.shieldoverlay.AreaInfo"),entity=loader.loadClass("com.boop.shieldoverlay.EntityCard"),phase=loader.loadClass("com.boop.shieldoverlay.RoomPanelController$Phase"),stateType=loader.loadClass("com.boop.shieldoverlay.RoomPanelController$State");
            Object room=area.getConstructor(String.class,String.class).newInstance("test_room","Test room");
            List<Object> devices=new ArrayList<>();
            for(int i=0;i<4;i++)devices.add(entity.getConstructor(String.class,String.class,String.class,String.class,boolean.class,String.class)
                    .newInstance(i==0?"light.test":"light.test"+i,"test_room",i==0?"Test lamp":"Test lamp "+i,"off",false,null));
            Constructor<?> sc=stateType.getDeclaredConstructor(long.class,area,phase,List.class,String.class,String.class);sc.setAccessible(true);
            Object roomState=sc.newInstance(1L,room,phase.getField("LIVE").get(null),devices,null,null); originalRoomState=roomState;
            type.getMethod("setRoomPanelState",stateType,boolean.class).invoke(home,roomState,true);
            // The unchanged pre-Seren path supplies the real measured sizing reference.
            originalHome=(View)type.getConstructor(Context.class).newInstance(isolated);
            type.getMethod("render",List.class,List.class,callbacksType).invoke(originalHome,List.of(),List.of(),callbacks);
            type.getMethod("setNowPlaying",snapshotType).invoke(originalHome,snapshot);
            type.getMethod("setRoomPanelState",stateType,boolean.class).invoke(originalHome,roomState,true);
            setContentView(originalHome);
        } catch(Throwable e){creationFailure=e;TextView error=new TextView(this);error.setText(e.toString());setContentView(error);}
    }
    static View find(View view,String text){
        if(text.equals(String.valueOf(view.getContentDescription()))||(view instanceof TextView&&text.equals(((TextView)view).getText().toString())))return view;
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){View result=find(((ViewGroup)view).getChildAt(i),text);if(result!=null)return result;}return null;
    }
    static boolean full(View v){Rect r=new Rect();return v!=null&&v.getGlobalVisibleRect(r)&&r.height()>=v.getHeight()-2&&r.width()>=v.getWidth()-2;}
    static void remeasure(View v){v.forceLayout();if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)remeasure(((ViewGroup)v).getChildAt(i));}
    public static final class ProbeInstrumentation extends Instrumentation {
        Bundle args;
        public void onCreate(Bundle args){this.args=args;start();}
        public void onStart(){Bundle result=new Bundle();try{
            setInTouchMode(false);
            SerenRowsProbe a=(SerenRowsProbe)startActivitySync(new Intent(getTargetContext(),SerenRowsProbe.class).putExtra("target_package",args.getString("target_package","com.boop.shieldoverlay")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            SystemClock.sleep(1800);waitForIdleSync();
            if(a.creationFailure!=null)throw new AssertionError("Cannot render Seren row",a.creationFailure);
            runOnMainSync(()->{
                try {
                    Field f=a.originalHome.getClass().getDeclaredField("roomPanelView");f.setAccessible(true);View panel=(View)f.get(a.originalHome);
                    FrameLayout.LayoutParams p=(FrameLayout.LayoutParams)panel.getLayoutParams();
                    int width=((View)panel.getParent()).getWidth()-p.leftMargin-p.rightMargin;
                    // The legacy overlay is initially GONE; measure its now-final layout parameters.
                    for(int pass=0;pass<2;pass++) {
                        remeasure(panel);panel.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(p.height,View.MeasureSpec.EXACTLY));
                        panel.layout(0,p.topMargin,width,p.topMargin+p.height);
                    }
                }catch(Exception e){throw new RuntimeException(e);}
            });
            SystemClock.sleep(200);waitForIdleSync();
            runOnMainSync(()->{
                View original=find(a.originalHome,"Test lamp, Off");
                a.originalTileWidth=original.getWidth();a.originalTileHeight=original.getHeight();
                a.setContentView(a.home);
            });
            SystemClock.sleep(1800);waitForIdleSync();
            runOnMainSync(()->{
                find(a.home,"Add favourites").requestFocus();
                try {
                    Field np=a.home.getClass().getDeclaredField("nowPlayingView");np.setAccessible(true);View media=(View)np.get(a.home);
                    Field art=media.getClass().getDeclaredField("artwork"),spectrum=media.getClass().getDeclaredField("puppetView"),progress=media.getClass().getDeclaredField("progress");
                    art.setAccessible(true);spectrum.setAccessible(true);progress.setAccessible(true);
                    View cover=(View)art.get(media),bars=(View)spectrum.get(media),track=(View)progress.get(media);
                    int[] c=new int[2],b=new int[2],p=new int[2],m=new int[2];cover.getLocationOnScreen(c);bars.getLocationOnScreen(b);track.getLocationOnScreen(p);media.getLocationOnScreen(m);
                    float density=media.getResources().getDisplayMetrics().density;
                    if(Math.abs(c[1]+cover.getHeight()-b[1]-bars.getHeight())>1)a.failures.add("Spectrum base is not aligned with album cover");
                    if(Math.abs(p[0]+track.getWidth()-m[0]-Math.round(607*density))>2)a.failures.add("Track does not end at third favourites centre");
                    if(bars.getWidth()<Math.round(400*density))a.failures.add("Spectrum did not fill available space");
                    for(String key:new String[]{"lyricsButton","sourceButton"}){Field bf=media.getClass().getDeclaredField(key);bf.setAccessible(true);View button=(View)bf.get(media);int[] xy=new int[2];button.getLocationOnScreen(xy);if(xy[0]+button.getWidth()>p[0]+track.getWidth()+1||xy[1]<p[1]+track.getHeight())a.failures.add("Auxiliary button overlaps progress or spectrum");}
                }catch(Exception e){throw new RuntimeException(e);}
                View card=find(a.home,"Example show 1: 01x02 Next episode");
                if(!full(card))a.failures.add("First poster is not fully visible on initial screen");
                Rect room=new Rect();View lamp=find(a.home,"Test lamp, Off");
                if(lamp==null)a.failures.add("HA controls missing");else if(lamp.getGlobalVisibleRect(room))a.failures.add("HA should start off screen");
                if(lamp!=null){
                    String sizes="original="+a.originalTileWidth+"x"+a.originalTileHeight+", below Seren="+lamp.getWidth()+"x"+lamp.getHeight();
                    android.util.Log.i("SerenSizeAudit",sizes);
                    if(lamp.getWidth()!=a.originalTileWidth||lamp.getHeight()!=a.originalTileHeight)a.failures.add("HA button size changed: "+sizes);
                }
            });
            assertWholePosters(a);
            capture("seren-initial");
            step(a,KeyEvent.KEYCODE_DPAD_DOWN,"Example show 1: 01x02 Next episode");
            step(a,KeyEvent.KEYCODE_DPAD_RIGHT,"Example show 2: 01x02 Next episode");
            sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);waitForIdleSync();
            if(!a.selected.endsWith("action_args=2"))a.failures.add("Clicked wrong episode");
            assertWholePosters(a);
            for(int i=0;i<10;i++)step(a,KeyEvent.KEYCODE_DPAD_RIGHT,"Example show "+(i+3)+": 01x02 Next episode");
            assertWholePosters(a);
            for(int i=0;i<10;i++)sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
            SystemClock.sleep(450);waitForIdleSync();
            resizedPosterWindows(a);
            runOnMainSync(()->{try{Field f=a.home.getClass().getDeclaredField("roomPanelView");f.setAccessible(true);View panel=(View)f.get(a.home);if(panel.getAlpha()!=0f)a.failures.add("Room panel peeks while Seren focused");}catch(Exception e){throw new RuntimeException(e);}});
            capture("seren-focused");
            step(a,KeyEvent.KEYCODE_DPAD_DOWN,"Test room");
            runOnMainSync(()->{try{
                TextView title=(TextView)find(a.home,"Test room");
                Class<?> chrome=a.installedLoader.loadClass("com.boop.shieldhome.FocusChrome");
                Method accent=chrome.getDeclaredMethod("accentColor",Context.class);accent.setAccessible(true);
                if(title.getCurrentTextColor()!=(Integer)accent.invoke(null,a.isolatedContext))a.failures.add("Room heading does not use chosen accent");
            }catch(Exception e){throw new RuntimeException(e);}});
            sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);waitForIdleSync();
            if(a.roomPickerOpens!=1)a.failures.add("Room heading click does not open picker");
            capture("seren-room-heading");
            runOnMainSync(()->{try{Field f=a.home.getClass().getDeclaredField("roomPanelView");f.setAccessible(true);View panel=(View)f.get(a.home);if(panel.getAlpha()!=1f)a.failures.add("Room panel was not revealed on scroll");}catch(Exception e){throw new RuntimeException(e);}});
            persistentRoom(a);SystemClock.sleep(350);waitForIdleSync();
            step(a,KeyEvent.KEYCODE_DPAD_DOWN,"Test lamp, Off");
            sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);waitForIdleSync();
            if(!a.roomSelected.equals("light.test"))a.failures.add("HA action inaccessible");
            capture("seren-ha");
            step(a,KeyEvent.KEYCODE_DPAD_UP,"Test room");
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
        void assertWholePosters(SerenRowsProbe a){
            runOnMainSync(()->{try{
                Field f=a.home.getClass().getDeclaredField("serenView");f.setAccessible(true);View seren=(View)f.get(a.home);
                Field sf=seren.getClass().getDeclaredField("scroll"),df=seren.getClass().getDeclaredField("detail");sf.setAccessible(true);df.setAccessible(true);
                View sc=(View)sf.get(seren),detail=(View)df.get(seren);
                if(sc.getWidth()!=seren.getWidth())a.failures.add("Seren does not fill row width");
                View first=find(a.home,"Example show 1: 01x02 Next episode"),fav=find(a.home,"Add favourites");
                if(first!=null&&first.getWidth()!=fav.getWidth())a.failures.add("Seren tile width differs from favourites");
                int[] textXY=new int[2],tileXY=new int[2];detail.getLocationOnScreen(textXY);first.getLocationOnScreen(tileXY);
                if(textXY[1]<tileXY[1]+first.getHeight())a.failures.add("Episode title is above tiles");
                if((((TextView)detail).getGravity()&Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK)!=Gravity.START)a.failures.add("Episode title is not left aligned");
            }catch(Exception e){throw new RuntimeException(e);}});
        }
        void resizedPosterWindows(SerenRowsProbe a)throws Exception{
            Field f=a.home.getClass().getDeclaredField("serenView");f.setAccessible(true);View seren=(View)f.get(a.home);
            int original=seren.getWidth();
            for(int delta:new int[]{17,57,111}){
                runOnMainSync(()->{seren.getLayoutParams().width=original-delta;seren.requestLayout();});
                SystemClock.sleep(200);waitForIdleSync();
                capture("seren-width-"+delta);assertWholePosters(a);
            }
            runOnMainSync(()->{seren.getLayoutParams().width=ViewGroup.LayoutParams.MATCH_PARENT;seren.requestLayout();});
            SystemClock.sleep(250);waitForIdleSync();assertWholePosters(a);
        }
        void persistentRoom(SerenRowsProbe a)throws Exception{
            ClassLoader loader=a.installedLoader;
            Class<?> sessionType=loader.loadClass("com.boop.shieldoverlay.RoomPanelSession");
            Class<?> listener=loader.loadClass("com.boop.shieldoverlay.RoomPanelController$Listener");
            Class<?> area=loader.loadClass("com.boop.shieldoverlay.AreaInfo");
            Class<?> picker=loader.loadClass("com.boop.shieldhome.ShieldRoomPickerDialog");
            Class<?> rooms=loader.loadClass("com.boop.shieldhome.ShieldRoomPickerDialog$Rooms");
            Class<?> callback=loader.loadClass("com.boop.shieldoverlay.HomeAssistantRepository$AreasCallback");
            Class<?> state=loader.loadClass("com.boop.shieldoverlay.RoomPanelController$State");
            Class<?> phase=loader.loadClass("com.boop.shieldoverlay.RoomPanelController$Phase");
            Object sink=Proxy.newProxyInstance(loader,new Class<?>[]{listener},(p,m,args)->null);
            Object[] session=new Object[1];AlertDialog[] dialog=new AlertDialog[1];
            Object living=area.getConstructor(String.class,String.class).newInstance("test_room","Test room");
            Object kitchen=area.getConstructor(String.class,String.class).newInstance("kitchen","Kitchen");
            Method show=picker.getDeclaredMethod("show",Context.class,rooms);show.setAccessible(true);
            Object source=Proxy.newProxyInstance(loader,new Class<?>[]{rooms},(p,m,args)->{
                if(m.getName().equals("selectedRoom"))return sessionType.getMethod("selectedRoom").invoke(session[0]);
                if(m.getName().equals("selectRoom")){
                    sessionType.getMethod("selectRoom",area).invoke(session[0],args[0]);
                    Constructor<?> c=state.getDeclaredConstructor(long.class,area,phase,List.class,String.class,String.class);c.setAccessible(true);
                    Object panelState=args[0]==living?a.originalRoomState:c.newInstance(2L,args[0],phase.getField("LIVE").get(null),List.of(),null,null);
                    a.home.getClass().getMethod("setRoomPanelState",state,boolean.class).invoke(a.home,panelState,true);
                    return null;
                }
                if(m.getName().equals("loadRooms")){
                    callback.getMethod("onResult",List.class,String.class).invoke(args[0],List.of(living,kitchen),null);
                    return (Runnable)()->{};
                }
                return null;
            });
            runOnMainSync(()->{try{
                session[0]=sessionType.getConstructor(Context.class,listener).newInstance(a.isolatedContext,sink);
                sessionType.getMethod("selectRoom",area).invoke(session[0],living);
                dialog[0]=(AlertDialog)show.invoke(null,a,source);
            }catch(Exception e){throw new RuntimeException(e);}});
            SystemClock.sleep(300);waitForIdleSync();capture("seren-room-picker");
            sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);waitForIdleSync();
            sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);waitForIdleSync();
            runOnMainSync(()->{try{
                if(dialog[0].isShowing()||find(a.home,"Kitchen")==null)a.failures.add("Room picker did not replace Home room");
                sessionType.getMethod("close").invoke(session[0]);
                session[0]=sessionType.getConstructor(Context.class,listener).newInstance(a.isolatedContext,sink);
                Object saved=sessionType.getMethod("selectedRoom").invoke(session[0]);
                if(!"kitchen".equals(area.getMethod("id").invoke(saved)))a.failures.add("Room selection did not persist across sessions");
                dialog[0]=(AlertDialog)show.invoke(null,a,source);
            }catch(Exception e){throw new RuntimeException(e);}});
            SystemClock.sleep(300);waitForIdleSync();
            sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);waitForIdleSync();
            sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);waitForIdleSync();
            runOnMainSync(()->{try{
                if(dialog[0].isShowing()||find(a.home,"Test room")==null)a.failures.add("Could not swap room back");
                Object saved=sessionType.getMethod("selectedRoom").invoke(session[0]);
                if(!"test_room".equals(area.getMethod("id").invoke(saved)))a.failures.add("Swap back was not saved");
                sessionType.getMethod("close").invoke(session[0]);
            }catch(Exception e){throw new RuntimeException(e);}});
        }
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
