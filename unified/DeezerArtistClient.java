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
    DeezerArtistClient() {
        this(DeezerArtistClient::request, new HomeAssistantEntityDiscoveryClient()::allowedEntityIds,
                Thread::sleep);
    }
    DeezerArtistClient(Http http, Exposure exposure, Delay delay) {
        this.http=http; this.exposure=exposure; this.delay=delay;
    }

    CommandOutcome process(String base, String token, String text, BoopRoom room,
            BoopRoomSource rooms) throws HomeAssistantAuth.AuthRejectedException {
        MediaRequest request = MediaRequest.parse(text);
        if (request == null || (request.kind != MediaRequest.Kind.DEEZER_SEARCH && request.kind != MediaRequest.Kind.DEEZER_FLOW)) return null;
        if (request.query.length() > 160) return request.explicitProvider
                ? reply("Say an artist or song name to play on Deezer.") : null;
        DeezerCatalogue.Selection resolved=null;
        if(!request.explicitProvider) {
            // "Play a game" and other conversation must retain the existing routing.
            // Even missing TVs or an offline catalogue cannot consume an unconfirmed artist request.
            try { resolved=DeezerCatalogue.resolve(http,request); }
            catch(Exception unavailable) { return null; }
            if(resolved==null) return null;
        }
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
            if(targets.isEmpty()) return reply("I can't find an exposed Android TV in " + room.name() + ".");
            if(targets.size()!=1) return reply("More than one Android TV is in " + room.name() + ". Choose a room with one TV first.");
            JSONObject target=targets.get(0);
            String media=target.getString("media"), remote=target.getString("remote");
            JSONObject state=new JSONObject(http.request(base+"/api/states/"+media, token, null));
            if(unavailable(state)) return reply("The TV in " + room.name() + " is unavailable.");
            JSONObject attrs=state.optJSONObject("attributes");
            String name=attrs==null ? "TV" : attrs.optString("friendly_name","TV");

            DeezerCatalogue.Selection selection=resolved==null ? DeezerCatalogue.resolve(http,request) : resolved;
            if(selection==null) return reply("I couldn't find a matching Deezer artist or song for " + request.query + ". Try the song title and artist.");
            JSONArray adb=target.optJSONArray("adb");
            if(adb==null || adb.length()!=1 || !adb.optString(0).matches("media_player[.][a-z0-9_]+"))
                return reply("Set up Android Debug Bridge for this TV in Home Assistant and put it in " + room.name() + " so I can select Deezer music.");
            String adbEntity=adb.getString(0);
            JSONObject adbState=new JSONObject(http.request(base+"/api/states/"+adbEntity,token,null));
            if(unavailable(adbState)) return reply("The TV's music controls are unavailable. Check its Android Debug Bridge connection.");
            new DeezerNativeController(base,token,adbEntity,http,delay,room,rooms).play(target.optJSONArray("macs"),selection);
            return reply("Requested " + selection.name + " on " + name + ".");
        } catch(HomeAssistantAuth.AuthRejectedException e) { throw e;
        } catch(InterruptedException e) { Thread.currentThread().interrupt(); return reply("Music request cancelled.");
        } catch(Exception e) { return reply("I couldn't send that Deezer request. Check the TV and try again."); }
    }

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
    private static String request(String url,String token,JSONObject payload) throws Exception {
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
