package com.boop.alpha1;

import com.boop.shared.DeezerScreen;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import org.json.*;

/** Bounded native UI operation, only on the hardware behind the exposed room TV. */
final class DeezerNativeController {
    private static final Object LOCK=new Object();
    private static final AtomicLong EPOCH=new AtomicLong();
    private static final String COMPONENT="deezer.android.app/.navigation.ui.MainNavigationActivity";
    private static final String FOREGROUND="dumpsys activity activities | grep -m 1 mResumedActivity | grep -Fq 'deezer.android.app/'";
    private final String base,token,entity;
    private final DeezerArtistClient.Http http;
    private final DeezerArtistClient.Delay delay;
    private final BoopRoom room;
    private final BoopRoomSource rooms;
    private long epoch;
    DeezerNativeController(String base,String token,String entity,DeezerArtistClient.Http http,DeezerArtistClient.Delay delay,BoopRoom room,BoopRoomSource rooms) {
        this.base=base; this.token=token; this.entity=entity; this.http=http; this.delay=delay; this.room=room; this.rooms=rooms;
    }
    void play(JSONArray remoteMacs,DeezerCatalogue.Selection selection) throws Exception {
        epoch=EPOCH.incrementAndGet();
        synchronized(LOCK) {
            checkCurrent();
            String identity=shell("ip link; settings get secure bluetooth_address").toLowerCase(Locale.ROOT);
            boolean matched=false;
            if(remoteMacs!=null) for(int i=0;i<remoteMacs.length();i++) {
                String mac=remoteMacs.optString(i).toLowerCase(Locale.ROOT);
                if(mac.matches("[0-9a-f]{2}(:[0-9a-f]{2}){5}") && Pattern.compile("(?<![0-9a-f:])"+Pattern.quote(mac)+"(?![0-9a-f:])").matcher(identity).find()) matched=true;
            }
            if(!matched) throw new IOException("ADB does not match the exposed TV");
            shell("am force-stop deezer.android.app");
            delay.sleep(1000);
            String launch="am start ";
            if(selection.flow) launch+="-a android.intent.action.MAIN -c android.intent.category.LEANBACK_LAUNCHER ";
            else {
                if(!selection.url.matches("https://www[.]deezer[.]com/(artist|album)/[1-9][0-9]*")) throw new IOException("Invalid media link");
                launch+="-a android.intent.action.VIEW -d "+selection.url+" ";
            }
            // A clean process avoids stale deep links and stuck repeated Home loads.
            String launched=shell(launch+"-n "+COMPONENT);
            if(!launched.contains("Starting: Intent") || launched.contains("Error") || launched.contains("Exception")) throw new IOException("Native launch failed");
            delay.sleep(3000);
            boolean pageSeen=false;
            for(int attempt=0;attempt<25;attempt++) {
                DeezerScreen screen=screen();
                if(!screen.isDeezer()) throw new IOException("Deezer is no longer in front");
                if(screen.hasText("There is no content")) throw new IOException("Deezer has no content");
                boolean page=selection.flow ? screen.hasText("An infinite mix of favorites and new tracks") : screen.hasText(selection.pageTitle);
                if(page) pageSeen=true;
                DeezerScreen.Target target=page?screen.target(selection.label):null;
                if(target!=null && target.focused) {
                    guarded("input keyevent 127"); // Pause before selecting a potentially toggling control.
                    delay.sleep(350);
                    DeezerScreen fresh=screen();
                    boolean samePage=selection.flow ? fresh.hasText("An infinite mix of favorites and new tracks") : fresh.hasText(selection.pageTitle);
                    DeezerScreen.Target confirmed=samePage?fresh.target(selection.label):null;
                    if(confirmed==null || !confirmed.focused || confirmed.x!=target.x || confirmed.y!=target.y) throw new IOException("Selected control changed");
                    guarded("input keyevent KEYCODE_DPAD_CENTER");
                    return;
                }
                if(!selection.flow && !pageSeen) {
                    if(attempt>=5) throw new IOException("Requested page did not load");
                    delay.sleep(500); continue;
                }
                // Read after each small scroll; labels, never row numbers, authorize selection.
                guarded("input keyevent KEYCODE_DPAD_DOWN");
                delay.sleep(150);
            }
            throw new IOException("Requested native control was not found");
        }
    }
    private DeezerScreen screen() throws Exception {
        String path="/sdcard/boop_native_music_"+UUID.randomUUID().toString().replace("-","")+".xml";
        String xml=shell("uiautomator dump "+path+" >/dev/null && cat "+path+"; rm -f "+path);
        int start=xml.indexOf("<?xml");
        if(start<0) throw new IOException("No fresh UI hierarchy");
        return DeezerScreen.parse(xml.substring(start).trim());
    }
    private void guarded(String command) throws Exception {
        if(!shell("if "+FOREGROUND+"; then "+command+" && echo BOOP_ACTION_DONE; fi").contains("BOOP_ACTION_DONE")) throw new IOException("Foreground changed");
    }
    private String shell(String command) throws Exception {
        checkCurrent();
        String nonce="BOOP_"+UUID.randomUUID().toString().replace("-","");
        http.request(base+"/api/services/androidtv/adb_command",token,new JSONObject().put("entity_id",entity).put("command","echo "+nonce+"; "+command));
        JSONObject state=new JSONObject(http.request(base+"/api/states/"+entity,token,null));
        checkCurrent();
        JSONObject attrs=state.optJSONObject("attributes");
        String output=attrs==null?"":attrs.optString("adb_response");
        if(!output.startsWith(nonce)) throw new IOException("Stale ADB response");
        return output.substring(nonce.length()).trim();
    }
    private void checkCurrent() throws IOException {
        BoopRoom current=rooms.currentRoom();
        if(Thread.currentThread().isInterrupted() || epoch!=EPOCH.get() || !room.id().equals(current.id()) || !room.name().equals(current.name())) throw new IOException("Music request superseded");
    }
}
