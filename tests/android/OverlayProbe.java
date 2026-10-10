package local.boop.precision;
import android.app.*;import android.content.*;import android.os.*;import java.io.*;
public final class OverlayProbe extends Instrumentation {
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){String result;try {
 Activity home=startActivitySync(new Intent().setClassName("com.boop.shieldoverlay","com.boop.shieldhome.ShieldLauncherActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();
 runOnMainSync(()->{if(!home.setTranslucent(true))throw new AssertionError("unable to simulate Recents conversion");});
 runOnMainSync(()->{try{java.lang.reflect.Method resume=home.getClass().getDeclaredMethod("onResume");resume.setAccessible(true);resume.invoke(home);}catch(Exception e){throw new RuntimeException(e);}});waitForIdleSync();Thread.sleep(300);
 String state;try(ParcelFileDescriptor fd=getUiAutomation().executeShellCommand("dumpsys activity activities");FileInputStream in=new FileInputStream(fd.getFileDescriptor())){ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)bytes.write(buffer,0,n);state=bytes.toString("UTF-8");}
 int name=state.indexOf("com.boop.shieldhome.ShieldLauncherActivity",state.indexOf("* Hist #"));if(name<0)throw new AssertionError("launcher record missing");int end=state.indexOf("* Hist #",name+1);String record=state.substring(name,end<0?state.length():end);
 java.util.regex.Matcher opaque=java.util.regex.Pattern.compile("occludesParent=(true|false)").matcher(record); if(!opaque.find() || !"true".equals(opaque.group(1)))throw new AssertionError("Home remained translucent after child return");
 result="PASS Home recovers opaque window after Recents translucency";
 }catch(Throwable e){result="FAIL "+e;}Bundle out=new Bundle();out.putString("result",result);finish(result.startsWith("PASS")?-1:0,out);}
}