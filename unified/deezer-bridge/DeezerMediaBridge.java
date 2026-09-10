package com.boop.bridge;

import android.content.Context;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Looper;
import android.os.SystemClock;
import java.util.concurrent.TimeUnit;

/** Short-lived ADB-shell helper. No installed app, service, credentials or UI inspection. */
public final class DeezerMediaBridge {
    public static void main(String[] args) {
        try {
            if(args.length!=2 || !(args[0].equals("play")||args[0].equals("prepare")) || !args[1].matches("https://www[.]deezer[.]com/(flow|(artist|track)/[1-9][0-9]*)"))
                throw new IllegalArgumentException("Invalid native media link");
            Looper.prepareMainLooper();
            // app_process runs under the already-authorized shell UID. Use its
            // real package context; do not impersonate a provider/browser client.
            Class<?> activityThread=Class.forName("android.app.ActivityThread");
            Object thread=activityThread.getMethod("systemMain").invoke(null);
            Context system=(Context)activityThread.getMethod("getSystemContext").invoke(thread);
            Context shell=system.createPackageContext("com.android.shell",0);
            initializeMediaFramework();
            MediaSessionManager sessions=(MediaSessionManager)shell.getSystemService(Context.MEDIA_SESSION_SERVICE);
            MediaController controller=find(sessions);
            if(args[0].equals("play")&&!ready(controller)) {
                System.out.println("BOOP_MEDIA_NEEDS_PREPARE");System.exit(0);return;
            }
            if(args[0].equals("prepare")) {
                if(controller==null) {
                    Process launch=new ProcessBuilder("am","start","-a","android.intent.action.MAIN","-c","android.intent.category.LEANBACK_LAUNCHER",
                        "-n","deezer.android.app/.navigation.ui.MainNavigationActivity").redirectErrorStream(true).start();
                    if(!launch.waitFor(4,TimeUnit.SECONDS)) {launch.destroy();throw new IllegalStateException("Launch timeout");}
                    if(launch.exitValue()!=0)throw new IllegalStateException("Launch failed");
                }
                long deadline=SystemClock.elapsedRealtime()+4000;
                while(!ready(controller)&&SystemClock.elapsedRealtime()<deadline){Thread.sleep(100);controller=find(sessions);}
                if(!ready(controller))throw new IllegalStateException("Native URI control unavailable");
                System.out.println("BOOP_MEDIA_READY");System.exit(0);return;
            }
            controller.getTransportControls().playFromUri(Uri.parse(args[1]),null);
            System.out.println("BOOP_MEDIA_REQUESTED");
            System.exit(0);
        } catch(Exception unavailable) {
            System.out.println("BOOP_MEDIA_UNAVAILABLE");
            System.exit(1);
        }
    }
    private static void initializeMediaFramework()throws Exception {
        // Newer Android media modules need process-local bootstrap when launched
        // via app_process. This does not register a service or change device settings.
        Class<?> initializer;
        try {initializer=Class.forName("android.media.MediaFrameworkPlatformInitializer");}
        catch(ClassNotFoundException olderAndroid){return;}
        if(initializer.getMethod("getMediaServiceManager").invoke(null)==null) {
            Class<?> manager=Class.forName("android.media.MediaServiceManager");
            initializer.getMethod("setMediaServiceManager",manager).invoke(null,manager.getConstructor().newInstance());
        }
    }
    private static boolean ready(MediaController controller) {
        PlaybackState state=controller==null?null:controller.getPlaybackState();
        return state!=null&&(state.getActions()&PlaybackState.ACTION_PLAY_FROM_URI)!=0;
    }
    private static MediaController find(MediaSessionManager sessions) {
        MediaController found=null;
        for(MediaController controller:sessions.getActiveSessions(null)) {
            if(!"deezer.android.app".equals(controller.getPackageName()))continue;
            if(found!=null)throw new IllegalStateException("Ambiguous Deezer sessions");
            found=controller;
        }
        return found;
    }
}
