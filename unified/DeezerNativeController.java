package com.boop.alpha1;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import org.json.*;

/** Native media-session operation, only on the hardware behind the exposed room TV. */
final class DeezerNativeController {
    private static final Object LOCK=new Object();
    private static final AtomicLong EPOCH=new AtomicLong();
    private final String base,token,entity;
    private final DeezerArtistClient.Http http;
    private final BoopRoom room;
    private final BoopRoomSource rooms;
    private long epoch;
    DeezerNativeController(String base,String token,String entity,DeezerArtistClient.Http http,DeezerArtistClient.Delay delay,BoopRoom room,BoopRoomSource rooms) {
        this.base=base; this.token=token; this.entity=entity; this.http=http; this.room=room; this.rooms=rooms;
    }
    void play(JSONArray remoteMacs,DeezerCatalogue.Selection selection) throws Exception {
        String url=selection.flow?"https://www.deezer.com/flow":selection.url;
        if(!url.matches("https://www[.]deezer[.]com/(flow|(artist|track)/[1-9][0-9]*)")) throw new IOException("Invalid media link");
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
            String result=bridge("play",url);
            if(hasResult(result,"BOOP_MEDIA_NEEDS_PREPARE")) {
                if(!hasResult(bridge("prepare",url),"BOOP_MEDIA_READY"))throw new IOException("Native media control unavailable");
                // shell() rechecks room/epoch after preparation, before playback.
                result=bridge("play",url);
            }
            if(!hasResult(result,"BOOP_MEDIA_REQUESTED"))throw new IOException("Native media control unavailable");
        }
    }
    private static boolean hasResult(String result,String expected) {
        return Arrays.asList(result.split("\\r?\\n")).contains(expected);
    }
    private String bridge(String mode,String url)throws Exception {
            String path="/data/local/tmp/boop_media_"+UUID.randomUUID().toString().replace("-","")+".jar";
            // Source-built payload, unique private file, checked before execution,
            // removed on exit. No installed app/service or persistent helper.
            String command="umask 077; trap 'rm -f "+path+"' EXIT; printf '%s' '"+DeezerBridgePayload.BASE64
                +"' | base64 -d > "+path+" && echo '"+DeezerBridgePayload.SHA256+"  "+path
                +"' | sha256sum -c - >/dev/null && chmod 400 "+path+" && CLASSPATH="+path
                +" app_process /system/bin com.boop.bridge.DeezerMediaBridge "+mode+" "+url;
            return shell(command);
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
