package com.boop.alpha1;
import android.content.Context;
import com.boop.eyes.StyleState;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Uses the already paired HA OAuth identity; it never sends credentials over discovery. */
final class BoopPuppetHaMirror {
    private final Context context;
    private String base, refresh, access;
    private volatile boolean available;
    private static final String ENTITY="sensor.boop_shared_character";
    BoopPuppetHaMirror(Context context){this.context=context.getApplicationContext();}
    boolean available(){return available;}
    boolean exchange(StyleState state) throws Exception {
        available=false;
        SecureTokenStore store=BoopVoiceTokenStore.create(context);
        if(!store.hasConnection())return false;
        String nextBase=store.getBaseUrl(),nextRefresh=store.getRefreshToken();
        if(nextRefresh==null)return false;
        if(!nextBase.equals(base)||!nextRefresh.equals(refresh)){base=nextBase;refresh=nextRefresh;access=null;}
        if(access==null)access=new HomeAssistantAuth(context,store).freshAccessToken();
        JSONObject remote=request("GET",null);
        boolean changed=false;
        String old="";
        if(remote!=null) {
            JSONObject attrs=remote.optJSONObject("attributes");
            if(attrs==null || attrs.optInt("boop_style_protocol",0)!=1)return false;
            old=attrs.optString("register",""); changed=state.merge(old);
        }
        String current=state.encode();
        if(!current.equals(old)) {
            JSONObject attrs=new JSONObject().put("friendly_name","BOOP shared character")
                    .put("boop_style_protocol",1).put("register",current)
                    .put("eye_hue",state.value("hue")).put("voice_pitch",state.value("pitch"))
                    .put("voice_speed",state.value("rate")).put("animation_speed",state.value("speed"));
            request("POST",new JSONObject().put("state","shared").put("attributes",attrs));
        }
        available=true;return changed;
    }
    private JSONObject request(String method,JSONObject body) throws Exception {
        HttpURLConnection c=(HttpURLConnection)new URL(base+"/api/states/"+ENTITY).openConnection();
        c.setInstanceFollowRedirects(false);c.setConnectTimeout(2500);c.setReadTimeout(2500);
        c.setRequestMethod(method);c.setRequestProperty("Authorization","Bearer "+access);
        c.setRequestProperty("Accept","application/json");
        try {
            if(body!=null){
                byte[] bytes=body.toString().getBytes(StandardCharsets.UTF_8);
                c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");c.setFixedLengthStreamingMode(bytes.length);
                try(OutputStream out=c.getOutputStream()){out.write(bytes);}
            }
            int status=c.getResponseCode();
            if(status==404 && method.equals("GET"))return null;
            if(status==401 || status==403){access=null;throw new IOException("HA sharing authorization needs refresh");}
            if(status<200 || status>=300)throw new IOException("HA sharing unavailable");
            try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                byte[] block=new byte[512];int n;
                while((n=in.read(block))!=-1){if(out.size()+n>16384)throw new IOException("Oversized HA style response");out.write(block,0,n);}
                return new JSONObject(out.toString("UTF-8"));
            }
        } finally {c.disconnect();}
    }
}
