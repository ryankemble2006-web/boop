package com.boop.alpha1;

import com.boop.shared.MediaRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Native Deezer playback on the hardware behind the room's exposed TV. */
final class DeezerArtistClient {
    interface Http { String request(String url, String token, JSONObject body) throws Exception; }
    interface Exposure { Set<String> allowed(String base, String token) throws Exception; }
    interface Delay { void sleep(long millis) throws InterruptedException; }
    private final Http http;
    private final Exposure exposure;
    private final Delay delay;
    private final java.util.function.LongSupplier clock;
    private DeezerCatalogue.Selection pending;
    private String pendingBase,pendingConnection,pendingRoom;
    private long pendingUntil;
    private long generation;
    DeezerArtistClient() {
        this(DeezerArtistClient::request, new HomeAssistantEntityDiscoveryClient()::allowedEntityIds,
                Thread::sleep);
    }
    DeezerArtistClient(Http http, Exposure exposure, Delay delay) {
        this(http,exposure,delay,()->System.nanoTime()/1000000);
    }
    DeezerArtistClient(Http http, Exposure exposure, Delay delay, java.util.function.LongSupplier clock) {
        this.http=http; this.exposure=exposure; this.delay=delay;this.clock=clock;
    }

    CommandOutcome process(String base, String token, String text, BoopRoom room,
            BoopRoomSource rooms) throws HomeAssistantAuth.AuthRejectedException {
        return processForConnection(base,token,"instance",text,room,rooms);
    }
    CommandOutcome processForConnection(String base,String token,String connection,String text,BoopRoom room,
            BoopRoomSource rooms) throws HomeAssistantAuth.AuthRejectedException {
        DeezerCatalogue.Selection resolved=null;
        final long operation;
        synchronized(this) {
        operation=++generation;
        if(pending!=null && (clock.getAsLong()>=pendingUntil || !Objects.equals(base,pendingBase)
                || !Objects.equals(connection,pendingConnection) || !roomKey(room).equals(pendingRoom))) clearPending();
        if(pending!=null) {
            String answer=text==null?"":normal(text).replaceAll("[.!?]+$","");
            if(answer.matches("(cancel|never mind|nevermind|stop|no)( please)?")) {clearPending();return reply("Cancelled.");}
            resolved=DeezerCatalogue.answer(pending,answer);
            if(resolved==null && answer.matches("(play )?(the )?(artist|band|song|track)( please)?"))
                return CommandOutcome.localQuestion(pending.question+" Please include the artist's name.");
            clearPending();
        }
        }
        MediaRequest request = MediaRequest.parse(text);
        if (resolved==null && (request == null || (request.kind != MediaRequest.Kind.DEEZER_SEARCH && request.kind != MediaRequest.Kind.DEEZER_FLOW))) return null;
        if (resolved==null && request.query.length() > 160) return request.explicitProvider
                ? reply("Failed") : null;
        if(resolved==null) {
            // "Play a game" and other conversation must retain the existing routing.
            // Even missing TVs or an offline catalogue cannot consume an unconfirmed artist request.
            try { resolved=DeezerCatalogue.resolve(http,request); }
            catch(Exception unavailable) { diagnostic("catalogue_unavailable");return request.explicitProvider?reply("I couldn't reach the music catalogue."):null; }
            if(resolved==null) {diagnostic("catalogue_no_match");return request.explicitProvider?reply("I couldn't find that music. Please say the title and artist."):null;}
        }
        synchronized(this) {
        if(operation!=generation)return reply("Cancelled.");
        if(resolved.ambiguous()) {
            pending=resolved;pendingBase=base;pendingConnection=connection;pendingRoom=roomKey(room);pendingUntil=clock.getAsLong()+60000;
            diagnostic("clarification");return CommandOutcome.localQuestion(resolved.question);
        }
        }
        diagnostic(resolved.flow?"resolved_flow":resolved.track?"resolved_track":"resolved_artist");
        String stage="target_discovery";
        try {
            JSONArray rows = new JSONArray(http.request(base + "/api/template", token,
                    new JSONObject().put("template", targetsTemplate(room.name()))));
            Set<String> allowed = exposure.allowed(base, token);
            List<JSONObject> targets = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            for(int i=0;i<rows.length();i++) {
                JSONObject row=rows.optJSONObject(i);
                if(row==null) continue;
                String media=row.optString("media"), remote=row.optString("remote");
                if(media.startsWith("media_player.") && remote.startsWith("remote.")
                        && allowed.contains(media) && seen.add(remote)) targets.add(row);
            }
            if(targets.isEmpty()) {diagnostic("no_room_tv");return reply("I couldn't find an available TV in this room.");}
            if(targets.size()!=1) {diagnostic("ambiguous_room_tv");return reply("More than one TV matches this room.");}
            JSONObject target=targets.get(0);
            String media=target.getString("media"), remote=target.getString("remote");
            JSONObject state=new JSONObject(http.request(base+"/api/states/"+media, token, null));
            if(unavailable(state)) {diagnostic("tv_unavailable");return reply("The TV in this room is unavailable.");}

            DeezerCatalogue.Selection selection=resolved;
            JSONArray adb=target.optJSONArray("adb");
            if(adb==null || adb.length()!=1 || !adb.optString(0).matches("media_player[.][a-z0-9_]+"))
                return reply("Failed");
            String adbEntity=adb.getString(0);
            JSONObject adbState=new JSONObject(http.request(base+"/api/states/"+adbEntity,token,null));
            if(unavailable(adbState)) return reply("Failed");
            synchronized(this) {if(operation!=generation)return reply("Cancelled.");}
            stage="native_playback";
            new DeezerNativeController(base,token,adbEntity,http,delay,room,rooms).play(target.optJSONArray("macs"),selection);
            diagnostic("playback_requested");
            return CommandOutcome.musicPlaybackAccepted();
        } catch(HomeAssistantAuth.AuthRejectedException e) { throw e;
        } catch(InterruptedException e) { Thread.currentThread().interrupt(); return reply("Failed");
        } catch(Exception e) {diagnostic(stage+"_failed");return reply("Failed"); }
    }
    private void clearPending() {pending=null;pendingBase=null;pendingConnection=null;pendingRoom=null;pendingUntil=0;}
    synchronized void cancelClarification() {generation++;clearPending();}
    synchronized void prepareTurn(String text) {
        generation++;
        if(pending==null)return;
        String answer=text==null?"":normal(text).replaceAll("[.!?]+$","");
        if(DeezerCatalogue.answer(pending,answer)==null
                && !answer.matches("(play )?(the )?(artist|band|song|track)( please)?")
                && !answer.matches("(cancel|never mind|nevermind|stop|no)( please)?"))clearPending();
    }
    private static String roomKey(BoopRoom room) {return room.id()+"\n"+room.name();}
    private static void diagnostic(String stage) {System.out.println("BOOP_MUSIC stage="+stage);}

    static JSONObject exactArtist(JSONArray values,String query) {
        if(values==null) return null;
        JSONObject match=null;
        for(int i=0;i<values.length();i++) {
            JSONObject artist=values.optJSONObject(i);
            if(artist==null || artist.optLong("id",0)<=0 || !normal(query).equals(normal(artist.optString("name")))) continue;
            if(match!=null && match.optLong("id")!=artist.optLong("id")) return null;
            match=artist;
        }
        return match;
    }
    private static String normal(String value) { return value.trim().replaceAll("\\s+"," ").toLowerCase(Locale.ROOT); }
    private static boolean unavailable(JSONObject state) {
        return "unavailable".equals(state.optString("state")) || "unknown".equals(state.optString("state"))
                || state.optString("state").isEmpty();
    }
    private static CommandOutcome reply(String text) { return CommandOutcome.localReply(text); }
    static String targetsTemplate(String room) {
        // Pair remote and media player by HA device identity, not similar names or suffixes.
        return "{% set ns=namespace(rows=[],adb=[]) %}{% set ids=integration_entities('androidtv_remote') %}"
                + "{% for a in integration_entities('androidtv') %}{% if a.startswith('media_player.') and area_name(a)=="+JSONObject.quote(room)+" %}{% set ns.adb=ns.adb+[a] %}{% endif %}{% endfor %}"
                + "{% for id in ids %}{% if id.startswith('remote.') and area_name(id)=="+JSONObject.quote(room)+" %}"
                + "{% set players=device_entities(device_id(id)) | select('in',ids) | select('match','^media_player[.]') | list %}"
                + "{% set hw=namespace(macs=[]) %}{% for c in device_attr(id,'connections') or [] %}{% if c[0]=='mac' %}{% set hw.macs=hw.macs+[c[1]] %}{% endif %}{% endfor %}"
                + "{% if players|count==1 %}{% set ns.rows=ns.rows+[{'remote':id,'media':players[0],'macs':hw.macs,'adb':ns.adb}] %}{% endif %}"
                + "{% endif %}{% endfor %}{{ ns.rows | to_json }}";
    }
    static String request(String url,String token,JSONObject payload) throws Exception {
        HttpURLConnection connection=(HttpURLConnection)new URL(url).openConnection();
        try {
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(5000); connection.setReadTimeout(url.contains("/androidtv/adb_command") ? 15000 : 5000);
            connection.setRequestProperty("Accept","application/json");
            if(token!=null) connection.setRequestProperty("Authorization","Bearer "+token);
            if(payload!=null) {
                connection.setRequestMethod("POST"); connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type","application/json; charset=UTF-8");
                byte[] bytes=payload.toString().getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(bytes.length);
                try(OutputStream output=connection.getOutputStream()) { output.write(bytes); }
            }
            int code=connection.getResponseCode();
            if(token!=null && (code==401 || code==403)) throw new HomeAssistantAuth.AuthRejectedException("Home Assistant authorization expired");
            if(code<200 || code>=300) throw new IOException("Media request HTTP "+code);
            try(Reader reader=new InputStreamReader(connection.getInputStream(),StandardCharsets.UTF_8)) {
                StringBuilder result=new StringBuilder(); char[] buffer=new char[4096]; int n;
                while((n=reader.read(buffer))!=-1) {
                    if(result.length()+n>1048576) throw new IOException("Media response too large");
                    result.append(buffer,0,n);
                }
                return result.toString();
            }
        } finally { connection.disconnect(); }
    }
}
