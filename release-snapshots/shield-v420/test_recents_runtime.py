"""Exercise the actual worker against minimal Android/ADB test doubles."""
from pathlib import Path
import subprocess
import sys
import tempfile

helper = Path(sys.argv[1]).resolve()
files = {
'android/content/Context.java': '''package android.content; public abstract class Context { public abstract java.io.File getNoBackupFilesDir(); }''',
'android/accessibilityservice/AccessibilityService.java': '''package android.accessibilityservice; public abstract class AccessibilityService extends android.content.Context { public static final int GLOBAL_ACTION_RECENTS=3; public abstract boolean performGlobalAction(int action); }''',
'android/util/Log.java': '''package android.util; public class Log { public static int i(String t,String m){return 0;} public static int w(String t,String m,Throwable e){return 0;} }''',
'com/boop/shieldturbo/power/AdbWire.java': '''package com.boop.shieldturbo.power;
import java.io.*; import java.security.*; import java.util.concurrent.*; import java.util.concurrent.atomic.*;
public class AdbWire implements Closeable {
 public static volatile boolean fail,block,executeFail; public static volatile boolean prompt; public static volatile int port,connectTimeout,executeTimeout;
 public static AtomicInteger count=new AtomicInteger(); public static volatile String command;
 public static CountDownLatch entered=new CountDownLatch(1), release=new CountDownLatch(1);
 public static class Result { public int exitCode=0; }
 public static KeyPair identity(File f){return null;}
 public void connect(int p,KeyPair k,int timeout,Runnable callback,boolean allowPrompt) throws IOException {port=p;connectTimeout=timeout;prompt=allowPrompt;if(fail)throw new IOException("offline");}
 public Result execute(String c,int timeout) throws Exception {command=c;executeTimeout=timeout;count.incrementAndGet();entered.countDown();if(block&&!release.await(2,TimeUnit.SECONDS))throw new IOException("test latch timeout");if(executeFail)throw new IOException("lost completion after submission");return new Result();}
 public void close(){}
}''',
'com/boop/shieldhome/RecentsPreloadHarness.java': '''package com.boop.shieldhome;
import java.io.*; import java.nio.file.*; import java.util.concurrent.*; import java.util.concurrent.atomic.*;
import com.boop.shieldturbo.power.AdbWire;
public class RecentsPreloadHarness {
 static class Service extends android.accessibilityservice.AccessibilityService {
  File dir; AtomicInteger fallbacks=new AtomicInteger(); Service(File d){dir=d;}
  public File getNoBackupFilesDir(){return dir;}
  public boolean performGlobalAction(int a){check(a==GLOBAL_ACTION_RECENTS,"wrong fallback action");fallbacks.incrementAndGet();return true;}
 }
 static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
 static void idle() throws Exception {
  java.lang.reflect.Field f=RecentsPreload.class.getDeclaredField("PENDING");f.setAccessible(true);
  AtomicBoolean pending=(AtomicBoolean)f.get(null);long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(3);
  while(pending.get()&&System.nanoTime()<end)Thread.sleep(5);
  check(!pending.get(),"worker did not release pending request");
 }
 public static void main(String[] args) throws Exception {
  Service service=new Service(Files.createTempDirectory("recents-test-").toFile());
  File key=new File(service.dir,"boop-unified-local-adb.key");key.createNewFile();
  RecentsPreload.open(null);check(AdbWire.count.get()==0,"null request dispatched");
  AdbWire.block=true;RecentsPreload.open(service);
  check(AdbWire.entered.await(2,TimeUnit.SECONDS),"worker never entered ADB");
  RecentsPreload.open(service);check(AdbWire.count.get()==1,"duplicate request toggled recents twice");
  AdbWire.release.countDown();idle();
  check(AdbWire.count.get()==1,"duplicate queued after completion");
  check("input keyevent --longpress 187".equals(AdbWire.command),"native preload command missing");
  check(AdbWire.port==5555&&!AdbWire.prompt,"must use trusted loopback key without prompt");
  check(AdbWire.connectTimeout==2000&&AdbWire.executeTimeout==3000,"unbounded native dispatch");
  check(service.fallbacks.get()==0,"unnecessary fallback double-toggle");
  AdbWire.executeFail=true;RecentsPreload.open(service);idle();
  check(service.fallbacks.get()==0&&AdbWire.count.get()==2,"ambiguous dispatch toggled twice");
  AdbWire.executeFail=false;AdbWire.fail=true;RecentsPreload.open(service);idle();
  check(service.fallbacks.get()==1&&AdbWire.count.get()==2,"ADB failure fallback incorrect");
  AdbWire.fail=false;key.delete();RecentsPreload.open(service);idle();
  check(service.fallbacks.get()==2&&AdbWire.count.get()==2,"missing key should not create identity or prompt");
  service.dir.delete();System.out.println("PASS: native dispatch, duplicate suppression, bounded/no-prompt ADB, offline fallback, missing-key fallback");System.exit(0);
 }
}'''
}
with tempfile.TemporaryDirectory(prefix='recents-harness-') as tmp:
    root=Path(tmp)
    for relative,text in files.items():
        path=root/relative
        path.parent.mkdir(parents=True,exist_ok=True)
        path.write_text(text,encoding='utf-8')
    sources=[str(root/name) for name in files]+[str(helper)]
    subprocess.run(['javac','-d',str(root/'classes'),*sources],check=True,capture_output=True,text=True)
    subprocess.run(['java','-cp',str(root/'classes'),'com.boop.shieldhome.RecentsPreloadHarness'],check=True,timeout=12)
