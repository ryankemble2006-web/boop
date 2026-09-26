"""Run real notification listener/runtime/lock code with deterministic Android boundaries.

These probes verify our callbacks and state, not Android's actual window/keyguard behavior.
"""
from pathlib import Path
import subprocess
import pytest

ROOT = Path(__file__).resolve().parents[1]
STUBS = {
    'android/content/Context.java': '''package android.content;
public class Context {
 public static final String POWER_SERVICE="power",KEYGUARD_SERVICE="keyguard"; public static final int RECEIVER_NOT_EXPORTED=4;
 public static android.app.Application app; public static String packageName="com.boop.alpha1"; public String getPackageName(){return packageName;} public Context getApplicationContext(){return app;}
 public Object getSystemService(String name){return name.equals(POWER_SERVICE)?new android.os.PowerManager():android.app.KeyguardManager.instance;}
 public android.content.pm.PackageManager getPackageManager(){return new android.content.pm.PackageManager();}
 public void startActivity(Intent i){} public Object registerReceiver(BroadcastReceiver r,IntentFilter f){return null;}
 public Object registerReceiver(BroadcastReceiver r,IntentFilter f,int flags){return null;} public void unregisterReceiver(BroadcastReceiver r){}
}''',
    'android/content/Intent.java': '''package android.content; public class Intent {
 public static final String ACTION_USER_PRESENT="present"; public static final int FLAG_ACTIVITY_NEW_TASK=1,FLAG_ACTIVITY_CLEAR_TOP=2,FLAG_ACTIVITY_SINGLE_TOP=4;
 public Intent(){} public Intent(Context c,Class<?> t){} public Intent addFlags(int f){return this;} public String getAction(){return "";}
}''',
    'android/content/IntentFilter.java': 'package android.content; public class IntentFilter {public IntentFilter(String a){}}',
    'android/content/BroadcastReceiver.java': 'package android.content; public abstract class BroadcastReceiver {public abstract void onReceive(Context c,Intent i);}',
    'android/content/pm/ApplicationInfo.java': 'package android.content.pm; public class ApplicationInfo {}',
    'android/content/pm/PackageManager.java': '''package android.content.pm; public class PackageManager {
 public static class NameNotFoundException extends Exception {} public ApplicationInfo getApplicationInfo(String p,int f)throws NameNotFoundException{return new ApplicationInfo();}
 public CharSequence getApplicationLabel(ApplicationInfo i){return "Chat";}}''',
    'android/app/Application.java': 'package android.app; public class Application extends android.content.Context {public Application(){app=this;} public android.content.Context getApplicationContext(){return this;}}',
    'android/app/Activity.java': '''package android.app; public class Activity extends android.content.Context {
 public boolean finished,destroyed; protected void onCreate(android.os.Bundle b){} protected void onNewIntent(android.content.Intent i){}
 protected void onStart(){} protected void onStop(){} protected void onDestroy(){destroyed=true;}
 public void finish(){finished=true;} public boolean isFinishing(){return finished;} public boolean isDestroyed(){return destroyed;}
 public void setShowWhenLocked(boolean b){} public void setTurnScreenOn(boolean b){} public void setIntent(android.content.Intent i){}
 public android.view.Window getWindow(){return new android.view.Window();} public void setContentView(Object v){}
}''',
    'android/app/KeyguardManager.java': '''package android.app; public class KeyguardManager {
 public static final KeyguardManager instance=new KeyguardManager(); public boolean locked=true; public KeyguardDismissCallback pending;
 public boolean isKeyguardLocked(){return locked;} public void requestDismissKeyguard(Activity a,KeyguardDismissCallback c){pending=c;}
 public static class KeyguardDismissCallback {public void onDismissSucceeded(){} public void onDismissCancelled(){} public void onDismissError(){}}
}''',
    'android/app/PendingIntent.java': '''package android.app; public class PendingIntent {public int sends; public boolean cancelled;
 public static class CanceledException extends Exception {} public void send()throws CanceledException{if(cancelled)throw new CanceledException();sends++;}
 public void send(android.content.Context c,int n,Object a,Object b,Object d,Object e,android.os.Bundle f)throws CanceledException{send();}}
''',
    'android/app/ActivityOptions.java': '''package android.app; public class ActivityOptions {
 public static final int MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE=2,MODE_BACKGROUND_ACTIVITY_START_ALLOWED=1;
 public static ActivityOptions makeBasic(){return new ActivityOptions();} public void setPendingIntentBackgroundActivityStartMode(int m){}
 public android.os.Bundle toBundle(){return new android.os.Bundle();}}''',
    'android/app/Notification.java': '''package android.app; public class Notification {
 public static final int FLAG_AUTO_CANCEL=16,FLAG_ONLY_ALERT_ONCE=8,FLAG_GROUP_SUMMARY=512;
 public static final String EXTRA_TITLE="title",EXTRA_TEXT="text"; public int flags; public android.os.Bundle extras=new android.os.Bundle();
 public PendingIntent contentIntent=new PendingIntent(); public String channelId="messages"; public String getChannelId(){return channelId;}}
''',
    'android/app/NotificationChannel.java': '''package android.app; public class NotificationChannel {
 public String getId(){return "messages";} public CharSequence getName(){return "Messages";} public Object getSound(){return new Object();} public boolean shouldVibrate(){return true;}}
''',
    'android/os/Bundle.java': '''package android.os; public class Bundle {private java.util.Map<String,CharSequence> data=new java.util.HashMap<>();
 public int reads; public CharSequence getCharSequence(String k){reads++;return data.get(k);} public void putCharSequence(String k,CharSequence v){data.put(k,v);}}
''',
    'android/os/Build.java': 'package android.os; public class Build {public static class VERSION {public static int SDK_INT=36;} public static class VERSION_CODES {public static final int TIRAMISU=33;}}',
    'android/os/Looper.java': 'package android.os; public class Looper {public static Looper getMainLooper(){return new Looper();}}',
    'android/os/PowerManager.java': 'package android.os; public class PowerManager {public boolean isInteractive(){return true;}}',
    'android/os/Handler.java': '''package android.os; public class Handler {public static final java.util.List<Runnable> delayed=new java.util.ArrayList<>();
 public Handler(Looper l){} public boolean post(Runnable r){r.run();return true;} public boolean postDelayed(Runnable r,long t){delayed.add(r);return true;}
 public void removeCallbacks(Runnable r){delayed.removeIf(v->v==r);} public static void fire(){java.util.List<Runnable> copy=new java.util.ArrayList<>(delayed);delayed.clear();for(Runnable r:copy)r.run();}}
''',
    'android/graphics/Color.java': 'package android.graphics; public class Color {public static final int BLACK=0;}',
    'android/view/Window.java': 'package android.view; public class Window {public Window getDecorView(){return this;} public void setBackgroundColor(int c){} public void addFlags(int f){} public void clearFlags(int f){}}',
    'android/view/WindowManager.java': 'package android.view; public class WindowManager {public static class LayoutParams {public static final int FLAG_KEEP_SCREEN_ON=1;}}',
    'android/widget/Toast.java': 'package android.widget; public class Toast {public static final int LENGTH_SHORT=0;public static Toast makeText(android.content.Context c,String s,int t){return new Toast();}public void show(){}}',
    'android/service/notification/StatusBarNotification.java': '''package android.service.notification; public class StatusBarNotification {
 public String key,pkg="com.chat"; public android.app.Notification notification; public StatusBarNotification(String k,android.app.Notification n){key=k;notification=n;}
 public String getKey(){return key;}public String getPackageName(){return pkg;}public android.app.Notification getNotification(){return notification;}public long getPostTime(){return 100;}}
''',
    'android/service/notification/NotificationListenerService.java': '''package android.service.notification; public class NotificationListenerService extends android.content.Context {
 public int cancellations; public StatusBarNotification[] active=new StatusBarNotification[0]; public void onListenerConnected(){}public void onListenerDisconnected(){}
 public void onNotificationPosted(StatusBarNotification s,RankingMap r){}public void onNotificationPosted(StatusBarNotification s){}public void onNotificationRemoved(StatusBarNotification s){}
 public StatusBarNotification[] getActiveNotifications(){return active;}public RankingMap getCurrentRanking(){return new RankingMap();} public void cancelNotification(String k){cancellations++;}
 public static class RankingMap {public boolean getRanking(String key,Ranking r){return true;}} public static class Ranking {public android.app.NotificationChannel getChannel(){return new android.app.NotificationChannel();}}}
''',
    'com/boop/alpha1/BoopNotificationSettingsStore.java': '''package com.boop.alpha1; import java.util.*; class BoopNotificationSettingsStore {
 static BoopNotificationSettingsState state=new BoopNotificationSettingsState(true,8000,Set.of("com.chat"),Set.of(BoopNotificationSettingsCodec.channelKey("com.chat","messages")));
 BoopNotificationSettingsStore(android.content.Context c){} BoopNotificationSettingsState load(){return state;} Set<BoopNotificationChannelInfo> observedChannels(){return Set.of();}void recordObservedChannel(BoopNotificationChannelInfo c){}
}''',
    'com/boop/alpha1/BoopNotificationPermissionState.java': '''package com.boop.alpha1; class BoopNotificationPermissionState {static boolean granted=true; static boolean hasListenerAccess(android.content.Context c){return granted;}}''',
    'com/boop/alpha1/BoopDeviceProfile.java': 'package com.boop.alpha1; class BoopDeviceProfile {enum Mode {WALL,LAUNCHER,SHIELD} static Mode resolve(android.content.Context c){return Mode.WALL;}}',
    'com/boop/alpha1/BoopNotificationOverlayController.java': '''package com.boop.alpha1; class BoopNotificationOverlayController implements BoopNotificationHost {
 BoopNotificationOverlayController(android.content.Context c,BoopNotificationRuntime r){}public void show(BoopNotificationPresentation p,long t){}public void update(BoopNotificationPresentation p,long t){}public void hide(){}}
''',
    'com/boop/alpha1/BoopNotificationCue.java': 'package com.boop.alpha1; class BoopNotificationCue {BoopNotificationCue(android.content.Context c){}void play(){}}',
    'com/boop/alpha1/BoopNotificationInboxActivity.java': 'package com.boop.alpha1; class BoopNotificationInboxActivity extends android.app.Activity {}',
    'com/boop/alpha1/BoopNotificationPuppetView.java': '''package com.boop.alpha1; class BoopNotificationPuppetView {
 static Callback last;interface Callback {void onOpen(String k);void onOpenBundle();void onDismiss();}
 BoopNotificationPuppetView(android.content.Context c,BoopNotificationPresentation p,Callback f){last=f;}void updatePresentation(BoopNotificationPresentation p){}
}''',
}

PROBE = '''package com.boop.alpha1;
import android.app.*;import android.os.*;import android.service.notification.*;
public class NotificationReliabilityProbe {
 static void check(boolean b,String why){if(!b)throw new AssertionError(why);}
 public static void main(String[] args){
  Application app=new Application();BoopNotificationRuntime runtime=BoopNotificationRuntime.get(app);
  BoopNotificationListenerService listener=new BoopNotificationListenerService();listener.onListenerConnected();
  Notification n=new Notification();n.flags=Notification.FLAG_AUTO_CANCEL|Notification.FLAG_ONLY_ALERT_ONCE;
  n.extras.putCharSequence(Notification.EXTRA_TEXT,"private old");StatusBarNotification sbn=new StatusBarNotification("one",n);
  if(args[0].startsWith("overlay-") || args[0].equals("android-other")){
   BoopNotificationSettingsStore.state=BoopNotificationSettingsStore.state.withAllAppsEnabled(true);runtime.refreshSettings();
   if(args[0].equals("overlay-shield"))android.content.Context.packageName="com.boop.shieldoverlay";
   sbn.pkg="android";n.channelId="com.android.server.wm.AlertWindowNotification - "+android.content.Context.packageName;
   boolean own=args[0].equals("overlay-wall")||args[0].equals("overlay-shield");
   if(args[0].equals("overlay-other"))n.channelId="com.android.server.wm.AlertWindowNotification - com.other.app";
   if(args[0].equals("android-other"))n.channelId="system-update";
   listener.onNotificationPosted(sbn,null);
   check(runtime.activeNotifications().size()==(own?0:1),"overlay status exclusion is missing or too broad");
   check((n.extras.reads==0)==own,"own overlay status must be filtered before rich extras");return;
  }
  if(args[0].equals("summary")){n.flags|=Notification.FLAG_GROUP_SUMMARY;listener.onNotificationPosted(sbn);check(runtime.activeNotifications().isEmpty(),"group summary must not duplicate child cards");return;}
  listener.onNotificationPosted(sbn);
  if(args[0].equals("once")){
   runtime.onPresentationDismissed();n.extras.putCharSequence(Notification.EXTRA_TEXT,"private updated");listener.onNotificationPosted(sbn);
   check(runtime.visibleBundle().isEmpty(),"ONLY_ALERT_ONCE update interrupted again");check(runtime.activeNotifications().get(0).text().equals("private updated"),"update must refresh inbox");return;
  }
  if(args[0].equals("ordinary")){runtime.onPresentationDismissed();n.flags=0;listener.onNotificationPosted(sbn);check(runtime.visibleBundle().size()==1,"ordinary update may alert");return;}
  if(args[0].equals("detach")){listener.onListenerDisconnected();check(runtime.activeNotifications().isEmpty()&&runtime.record("one")==null,"disconnect retained rich content");return;}
  if(args[0].equals("revoked")){BoopNotificationPermissionState.granted=false;runtime.openNotification(app,"one");check(n.contentIntent.sends==0,"revoked access still opens cached intent");check(runtime.record("one")==null,"revocation retained rich content");return;}
  if(args[0].equals("tap")){runtime.openNotification(app,"one");check(n.contentIntent.sends==1,"tap not sent");check(listener.cancellations==0,"unconfirmed send cancelled source notification");return;}
  BoopNotificationLockActivity lock=new BoopNotificationLockActivity();lock.onCreate(new Bundle());lock.onStart();
  if(args[0].equals("remove")){runtime.remove("one");check(lock.finished,"removal left lock mirror visible");return;}
  if(args[0].equals("disable")){BoopNotificationSettingsStore.state=BoopNotificationSettingsStore.state.withMasterEnabled(false);runtime.refreshSettings();check(lock.finished,"disable left lock mirror visible");return;}
  BoopNotificationPuppetView.last.onOpen("one");
  if(args[0].equals("authentication")){Handler.fire();check(!lock.finished,"presentation expired during authentication");KeyguardManager.instance.pending.onDismissCancelled();Handler.fire();check(lock.finished,"cancelled authentication lost timeout");return;}
  if(args[0].equals("destroyed")){lock.onDestroy();KeyguardManager.instance.pending.onDismissSucceeded();check(n.contentIntent.sends==0,"destroyed activity acted on late authentication");return;}
  throw new AssertionError("unknown probe");
 }
}'''

@pytest.fixture(scope='module')
def notification_probe(tmp_path_factory):
    work=tmp_path_factory.mktemp('notification-reliability')
    for name, code in STUBS.items():
        path=work/name; path.parent.mkdir(parents=True,exist_ok=True);path.write_text(code,encoding='utf-8')
    probe=work/'com/boop/alpha1/NotificationReliabilityProbe.java';probe.write_text(PROBE,encoding='utf-8')
    names=('Runtime','ListenerService','Coordinator','Envelope','LockActivity','TapLauncher','TapPolicy','Host','Presentation','Surface','SurfaceSelector','SettingsState','SettingsCodec','Policy','IntakePolicy','ChannelInfo','CuePolicy')
    sources=[ROOT/'source'/f'BoopNotification{name}.java' for name in names]
    classes=work/'classes';classes.mkdir()
    subprocess.run(['javac','-encoding','UTF-8','-d',str(classes),*map(str,work.rglob('*.java')),*map(str,sources)],check=True)
    return classes

@pytest.mark.parametrize('scenario',['once','ordinary','summary','detach','revoked','tap','remove','disable','authentication','destroyed','overlay-wall','overlay-shield','overlay-other','android-other'])
def test_notification_reliability(notification_probe,scenario):
    subprocess.run(['java','-cp',str(notification_probe),'com.boop.alpha1.NotificationReliabilityProbe',scenario],check=True)
