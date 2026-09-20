package com.boop.bridge;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.UiAutomation;
import android.content.*;
import android.graphics.*;
import android.hardware.display.*;
import android.media.*;
import android.media.session.*;
import android.os.*;
import android.util.Base64;
import android.view.accessibility.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONObject;

/** One bounded, explicitly requested native operation on an owned OFFSCREEN display.
 * No recording, main-display input, private-provider data, credential access or installed service.
 */
public final class DeezerHeartBridge {
    private static final String PACKAGE="deezer.android.app";
    private static final String COMPONENT=PACKAGE+"/.navigation.ui.MainNavigationActivity";
    private final Object pixelsLock=new Object();
    private final AtomicBoolean ended=new AtomicBoolean();
    private final JSONObject request;
    private final String nonce,operation;
    private Context context;
    private ImageReader reader;
    private Image image;
    private long frameSequence,frameReceivedAt;
    private int targetSaved=-1;
    private VirtualDisplay display;
    private UiAutomation ui;
    private MediaController player;
    private long deadline;
    private int displayId;
    private boolean actionSent;
    private static final String CONTEXT_TYPE="com.deezer.METADATA_KEY_STREAM_CONTEXT_TYPE";
    private static final String CONTEXT_ID="com.deezer.METADATA_KEY_STREAM_CONTEXT_ID";
    private String playerContextType="",playerContextId="";
    private volatile String stage="initialization",failureReason="";
    private DeezerHeartBridge(JSONObject request)throws Exception {
        this.request=request;nonce=request.getString("nonce");operation=request.getString("operation");
        if(!nonce.matches("[a-f0-9]{32}") || !DeezerHeartRules.requestAllowed(operation,request.getInt("expected_saved"))
                || request.getString("title").isEmpty() || request.getString("artist").isEmpty()
                || request.getString("title").length()>1024 || request.getString("artist").length()>1024)
            throw new IllegalArgumentException("Invalid native heart request");
    }
    public static void main(String[] args) {
        try {
            if(args.length!=1 || args[0].length()>18000 || !args[0].matches("[A-Za-z0-9_-]+"))
                throw new IllegalArgumentException("Invalid request encoding");
            JSONObject request=new JSONObject(new String(Base64.decode(args[0],Base64.URL_SAFE|Base64.NO_WRAP),"UTF-8"));
            DeezerHeartBridge bridge=new DeezerHeartBridge(request);
            Looper.prepareMainLooper(); Handler main=new Handler(Looper.getMainLooper());
            bridge.deadline=SystemClock.elapsedRealtime()+12000;
            main.postDelayed(()->bridge.end("TIMEOUT",-1),12000);
            bridge.initialize(main);
            new Thread(()->{
                try(RandomAccessFile file=new RandomAccessFile("/data/local/tmp/boop-heart-operation.lock","rw");
                        FileChannel channel=file.getChannel();FileLock lock=channel.tryLock()) {
                    if(lock==null)throw new IOException("BUSY");
                    bridge.perform();
                }catch(Exception unavailable){bridge.failureReason=unavailable.getClass().getSimpleName();bridge.end(bridge.actionSent?"UNCONFIRMED":"UNAVAILABLE",-1);}
            },"BOOP-native-heart").start();
            Looper.loop();
        }catch(Exception unavailable){System.out.println("BOOP_HEART_INVALID");System.exit(1);}
    }
    private void initialize(Handler main)throws Exception {
        if(Build.VERSION.SDK_INT<30)throw new IOException("Unsupported Android");
        Class<?> at=Class.forName("android.app.ActivityThread");Object thread=at.getMethod("systemMain").invoke(null);
        Context system=(Context)at.getMethod("getSystemContext").invoke(thread);
        context=system.createPackageContext("com.android.shell",0);
        try {
            Class<?> init=Class.forName("android.media.MediaFrameworkPlatformInitializer");
            if(init.getMethod("getMediaServiceManager").invoke(null)==null){
                Class<?> service=Class.forName("android.media.MediaServiceManager");
                init.getMethod("setMediaServiceManager",service).invoke(null,service.getConstructor().newInstance());
            }
        }catch(ClassNotFoundException olderAndroid){ }
        if(context.getPackageManager().getPackageInfo(PACKAGE,0).getLongVersionCode()!=301000101L
                || !context.getPackageManager().hasSystemFeature("android.software.activities_on_secondary_displays"))
            throw new IOException("Provider or display support changed");
        reader=ImageReader.newInstance(1280,720,PixelFormat.RGBA_8888,4);
        reader.setOnImageAvailableListener(r->{synchronized(pixelsLock){
            if(ended.get())return;
            try {Image next=r.acquireLatestImage();if(next!=null){if(image!=null)image.close();image=next;frameSequence++;frameReceivedAt=SystemClock.elapsedRealtime();pixelsLock.notifyAll();}}
            catch(IllegalStateException closing){ }
        }},main);
    }
    private void perform()throws Exception {
        stage="ownership";checkOwner();player=findPlayer();
        MediaMetadata initial=player.getMetadata();
        if(initial==null)throw new IOException("No native metadata");
        playerContextType=text(initial,CONTEXT_TYPE);playerContextId=text(initial,CONTEXT_ID);
        checkTrack();
        stage="display";
        int flags=1|2|8|64|128|256; // public, presentation, own content, touch, rotation, DESTROY on removal
        if(Build.VERSION.SDK_INT>=33)flags|=1024|2048|4096|8192;
        if(Build.VERSION.SDK_INT>=34)flags|=16384|32768;
        display=((DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE)).createVirtualDisplay(
                "BOOP-native-heart-"+nonce,1280,720,160,reader.getSurface(),flags);
        if(display==null)throw new IOException("Display unavailable");displayId=display.getDisplay().getDisplayId();
        Intent intent=new Intent().setComponent(ComponentName.unflattenFromString(COMPONENT));
        if(displayId<=0 || !((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE))
                .isActivityStartAllowedOnDisplay(context,displayId,intent))throw new IOException("No offscreen launch");
        stage="launch";run("am","start","--display",String.valueOf(displayId),"-f","0x18000000","-n",COMPONENT);
        Thread.sleep(850);checkOwner();
        stage="accessibility";openUi();
        stage="navigation";List<AccessibilityNodeInfo> nodes=scan();
        try {
            // Version-pinned nav rail: identify its geometry and one NOW PLAYING entry, never tap coordinates.
            AccessibilityNodeInfo now=null;boolean home=false,search=false,favourites=false;Rect b=new Rect();
            for(AccessibilityNodeInfo n:nodes){if(!usable(n))continue;n.getBoundsInScreen(b);
                home|=b.equals(new Rect(16,16,72,72));search|=b.equals(new Rect(16,76,72,132));
                favourites|=b.equals(new Rect(16,136,72,192));
                if(b.equals(new Rect(16,580,72,644))){if(now!=null)throw new IOException("Ambiguous player entry");now=n;}}
            if(!home||!search||!favourites||now==null)throw new IOException("Navigation changed");
            checkTrack();if(!now.performAction(AccessibilityNodeInfo.ACTION_CLICK))throw new IOException("Player unavailable");
        }finally{recycle(nodes);}
        Thread.sleep(600);nodes=scan();
        try {
            stage="heart-read";AccessibilityNodeInfo heart=selectHeart(nodes,false);int saved=state(heart);
            if(saved<0)throw new IOException("Unknown native heart");
            checkOwner();checkTrack();
            if("read".equals(operation)){end("OK",saved);return;}
            if("toggle".equals(operation)&&!DeezerHeartRules.shouldToggle(saved,request.getInt("expected_saved"))){end("STALE_STATE",saved);return;}
            AccessibilityNodeInfo target="dislike".equals(operation)?selectHeart(nodes,true):heart;
            checkOwner();checkTrack();stage="heart-action";
            Rect heartBounds=new Rect();heart.getBoundsInScreen(heartBounds);
            long beforeFrame; synchronized(pixelsLock){beforeFrame=frameSequence;}
            targetSaved="toggle".equals(operation)?1-saved:-1;
            actionSent=true;
            if(!target.performAction(AccessibilityNodeInfo.ACTION_CLICK))throw new IOException("Action not delivered");
            stage="confirmation";
            if("dislike".equals(operation)){
                Thread.sleep(1200);
                // A changed native queue item after the actual ban click confirms skip, not a substituted Next command.
                if(matches(player.getMetadata())){end("UNCONFIRMED",-1);return;}
                end("DISLIKED",-1);return;
            }
            // A network-backed native mutation need not repaint within one fixed 1200ms sample.
            int after=DeezerHeartConfirmation.awaitChange(saved,beforeFrame,
                    Math.min(deadline-700,SystemClock.elapsedRealtime()+6000),
                    (cursor,until)->nextHeartFrame(heartBounds,cursor,until));
            checkOwner();checkTrack();
            if(after!=1-saved || !heart.refresh() || !usable(heart) || state(heart)!=after){
                end("UNCONFIRMED",-1);return;
            }
            end("OK",after);
        }finally{recycle(nodes);}
    }
    private MediaController findPlayer()throws IOException {
        MediaController found=null;
        for(MediaController c:((MediaSessionManager)context.getSystemService(Context.MEDIA_SESSION_SERVICE)).getActiveSessions(null))
            if(PACKAGE.equals(c.getPackageName())){if(found!=null)throw new IOException("Ambiguous session");found=c;}
        if(found==null)throw new IOException("No native playback");return found;
    }
    private void checkTrack()throws IOException {
        MediaController current=findPlayer();
        if(!player.getSessionToken().equals(current.getSessionToken()) || !matches(current.getMetadata())
                || !playerContextId.equals(text(current.getMetadata(),CONTEXT_ID))
                || !playerContextType.equals(text(current.getMetadata(),CONTEXT_TYPE)))throw new IOException("Track or context changed");
    }
    private static String text(MediaMetadata m,String key){CharSequence s=m.getText(key);return s==null?"":s.toString().trim();}
    private boolean matches(MediaMetadata m){
        if(m==null)return false;String title=text(m,MediaMetadata.METADATA_KEY_TITLE);
        if(title.isEmpty())title=text(m,MediaMetadata.METADATA_KEY_DISPLAY_TITLE);
        String artist=text(m,MediaMetadata.METADATA_KEY_ARTIST);
        if(artist.isEmpty())artist=text(m,MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE);
        if(artist.isEmpty())artist=text(m,MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
        return title.equals(request.optString("title"))&&artist.equals(request.optString("artist"))
                &&text(m,MediaMetadata.METADATA_KEY_ALBUM).equals(request.optString("album"))
                &&m.getLong(MediaMetadata.METADATA_KEY_DURATION)==request.optLong("duration")
                &&text(m,MediaMetadata.METADATA_KEY_MEDIA_ID).equals(request.optString("media_id"));
    }
    private void checkOwner()throws Exception {
        if(ended.get()||SystemClock.elapsedRealtime()>deadline)throw new IOException("Expired");
        String marker=run("run-as","com.boop.shieldoverlay","cat","files/boop-heart-"+nonce).trim();
        if(!nonce.equals(marker))throw new IOException("Cancelled or wrong hardware");
    }
    private void openUi()throws Exception {
        Class<?> binder=Class.forName("android.app.UiAutomationConnection");
        Class<?> contract=Class.forName("android.app.IUiAutomationConnection");
        java.lang.reflect.Constructor<?> constructor=UiAutomation.class.getDeclaredConstructor(Looper.class,contract);
        constructor.setAccessible(true);ui=(UiAutomation)constructor.newInstance(Looper.getMainLooper(),binder.getConstructor().newInstance());
        // Preserve existing user accessibility services. This constant is 1, not FLAG_DONT_USE_ACCESSIBILITY (2).
        UiAutomation.class.getMethod("connect",int.class).invoke(ui,UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
        AccessibilityServiceInfo info=ui.getServiceInfo();
        info.flags|=AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS|AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        ui.setServiceInfo(info);
    }
    private List<AccessibilityNodeInfo> scan()throws IOException {
        List<AccessibilityNodeInfo> result=new ArrayList<>();
        List<AccessibilityWindowInfo> windows=ui.getWindowsOnAllDisplays().get(displayId);
        if(windows==null)throw new IOException("No offscreen window");
        try {for(AccessibilityWindowInfo window:windows){AccessibilityNodeInfo root=window.getRoot();if(root!=null)collect(root,result);}}
        catch(RuntimeException e){recycle(result);throw e;}
        for(AccessibilityWindowInfo window:windows)window.recycle();
        return result;
    }
    private void collect(AccessibilityNodeInfo node,List<AccessibilityNodeInfo> result){
        if(result.size()>=350){node.recycle();return;}result.add(node);
        for(int i=0;i<node.getChildCount();i++){AccessibilityNodeInfo child=node.getChild(i);if(child!=null)collect(child,result);}
    }
    private static boolean provider(AccessibilityNodeInfo n){return PACKAGE.contentEquals(n.getPackageName()==null?"":n.getPackageName());}
    private static boolean usable(AccessibilityNodeInfo n){return provider(n)&&n.isVisibleToUser()&&n.isEnabled()&&n.isClickable();}
    private AccessibilityNodeInfo selectHeart(List<AccessibilityNodeInfo> nodes,boolean dislike)throws IOException {
        boolean title=false,artist=false;Rect lyrics=null;
        for(AccessibilityNodeInfo n:nodes){if(!provider(n))continue;String t=n.getText()==null?"":n.getText().toString();
            title|=t.equals(request.optString("title"));artist|=t.equals(request.optString("artist"));
            if("Lyrics".contentEquals(n.getContentDescription()==null?"":n.getContentDescription())){lyrics=new Rect();n.getBoundsInScreen(lyrics);}}
        if(!title||!artist||lyrics==null)throw new IOException("Native player identity changed");
        TreeMap<Integer,AccessibilityNodeInfo> row=new TreeMap<>();Rect b=new Rect();
        for(AccessibilityNodeInfo n:nodes){if(!usable(n))continue;n.getBoundsInScreen(b);
            if(Math.abs(b.centerY()-lyrics.centerY())<=3&&b.width()>=40&&b.width()<=70)row.putIfAbsent(b.left,n);}
        List<AccessibilityNodeInfo> buttons=new ArrayList<>(row.values());int[][] geometry=new int[buttons.size()][];
        for(int i=0;i<buttons.size();i++){buttons.get(i).getBoundsInScreen(b);geometry[i]=new int[]{b.left,b.top,b.right,b.bottom};}
        int index=DeezerHeartRules.heartIndex(geometry,lyrics.centerY(),dislike,playerContextType);
        if(index<0)throw new IOException("Native transport layout changed");return buttons.get(index);
    }
    private DeezerHeartConfirmation.Frame nextHeartFrame(Rect bounds,long after,long until)throws Exception {
        synchronized(pixelsLock){
            while(!ended.get() && frameSequence<=after){
                long remaining=until-SystemClock.elapsedRealtime();
                if(remaining<=0)return null;
                pixelsLock.wait(remaining);
            }
            if(ended.get() || SystemClock.elapsedRealtime()>until)return null;
            return new DeezerHeartConfirmation.Frame(frameSequence,frameReceivedAt,state(bounds));
        }
    }
    private int state(AccessibilityNodeInfo heart)throws Exception {
        Rect b=new Rect();heart.getBoundsInScreen(b);return state(b);
    }
    private int state(Rect b)throws Exception {
        int[] crop=new int[41*41];
        synchronized(pixelsLock){
            if(image==null || SystemClock.elapsedRealtime()-frameReceivedAt>2500L)throw new IOException("No fresh offscreen frame");
            Image.Plane plane=image.getPlanes()[0];ByteBuffer bytes=plane.getBuffer();
            if(plane.getPixelStride()!=4 || b.centerX()<20||b.centerY()<20||b.centerX()+20>=image.getWidth()||b.centerY()+20>=image.getHeight())throw new IOException("Invalid native glyph");
            for(int y=0;y<41;y++)for(int x=0;x<41;x++){
                int offset=(b.centerY()-20+y)*plane.getRowStride()+(b.centerX()-20+x)*4;
                crop[y*41+x]=0xff000000|((bytes.get(offset)&255)<<16)|((bytes.get(offset+1)&255)<<8)|(bytes.get(offset+2)&255);
            }
        }
        return DeezerHeartRules.classify(crop,41,41);
    }
    private static void recycle(List<AccessibilityNodeInfo> nodes){for(AccessibilityNodeInfo n:nodes)n.recycle();}
    private String run(String... args)throws Exception {
        java.lang.Process p=new ProcessBuilder(args).redirectErrorStream(true).start();
        if(!p.waitFor(2500,TimeUnit.MILLISECONDS)){p.destroy();throw new IOException("Subcommand deadline");}
        StringBuilder out=new StringBuilder();try(BufferedReader r=new BufferedReader(new InputStreamReader(p.getInputStream()))){String line;while((line=r.readLine())!=null&&out.length()<8192)out.append(line).append('\n');}
        if(p.exitValue()!=0)throw new IOException("Subcommand rejected");return out.toString();
    }
    private void end(String status,int saved){
        if(!ended.compareAndSet(false,true))return;
        try {
            if(ui!=null)try{UiAutomation.class.getMethod("disconnect").invoke(ui);}catch(Exception ignored){ }
            synchronized(pixelsLock){if(image!=null){image.close();image=null;}if(display!=null)display.release();if(reader!=null)reader.close();}
            JSONObject response=new JSONObject().put("nonce",nonce).put("operation",operation).put("status",status).put("saved",saved).put("target_saved",targetSaved).put("stage",stage).put("reason",failureReason);
            System.out.println("BOOP_HEART_RESULT="+response);
        }catch(Exception ignored){System.out.println("BOOP_HEART_CLEANUP_FAILED");}
        finally{System.exit("OK".equals(status)||"DISLIKED".equals(status)||"STALE_STATE".equals(status)?0:1);}
    }
}