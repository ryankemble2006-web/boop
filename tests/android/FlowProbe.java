package local.boop.precision;
import android.app.*;import android.content.*;import android.os.*;
public final class FlowProbe extends Instrumentation {
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){String result;try {
 Activity home=startActivitySync(new Intent().setClassName("com.boop.shieldoverlay","com.boop.shieldhome.ShieldLauncherActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();
 runOnMainSync(()->{try{Class<?> type=home.getClassLoader().loadClass("com.boop.shieldhome.ShieldNowPlayingManager");home.getClassLoader().loadClass("com.boop.shieldturbo.power.AdbWire").getMethod("identity",java.io.File.class).invoke(null,new java.io.File(home.getNoBackupFilesDir(),"boop-unified-local-adb.key")); Object manager=type.getMethod("get",Context.class).invoke(null,home); /* Instrumentation suppresses cold notification-service attachment; deliver its authority callback after real access is granted. */ java.lang.reflect.Method connected=type.getDeclaredMethod("onListenerConnected");connected.setAccessible(true);connected.invoke(manager);if(!(Boolean)type.getMethod("playDeezerFlow").invoke(manager))throw new AssertionError("Flow request rejected");}catch(Exception e){throw new RuntimeException(e);}});
 Thread.sleep(6500);waitForIdleSync();
 Class<?> type=home.getClassLoader().loadClass("com.boop.shieldhome.ShieldNowPlayingManager");Object manager=type.getMethod("get",Context.class).invoke(null,home);Object state=type.getMethod("state").invoke(manager);Object snapshot=state.getClass().getMethod("current").invoke(state);
 if(snapshot==null || !"Background Flow".equals(snapshot.getClass().getMethod("title").invoke(snapshot))){java.lang.reflect.Field connected=type.getDeclaredField("listenerConnected");connected.setAccessible(true);java.lang.reflect.Field bindings=type.getDeclaredField("bindings");bindings.setAccessible(true);throw new AssertionError("Background Flow missing from Now Playing: "+snapshot+" listener="+connected.get(manager)+" bindings="+bindings.get(manager));}
 if(!home.hasWindowFocus())throw new AssertionError("Deezer took foreground");
 result="PASS force-closed private service wakes, Flow appears on Home without Activity";
 }catch(Throwable e){result="FAIL "+e;}Bundle out=new Bundle();out.putString("result",result);finish(result.startsWith("PASS")?-1:0,out);}
}