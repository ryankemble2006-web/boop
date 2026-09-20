package com.boop.alpha1;

import android.content.Context;
import android.util.Base64;
import com.boop.shieldoverlay.HomeAssistantAuthClient;
import com.boop.shieldoverlay.HomeAssistantSession;
import com.boop.shieldoverlay.SecureCredentialStore;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import okhttp3.OkHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

/** Reuses the existing authenticated local HA/ADB route, without launching an Activity.
 * The nonce in OUR app's private files proves which Shield is receiving the command and
 * disappears immediately on cancellation. Deezer credentials are never read.
 */
public final class BoopDeezerHeartBackend {
    private static final Map<String,File> MARKERS=new ConcurrentHashMap<>();
    private static volatile String cachedBase="",cachedEntity="";
    private BoopDeezerHeartBackend(){ }
    public static void cancel(String nonce){File file=MARKERS.remove(nonce);if(file!=null)file.delete();}
    public static Map<String,String> execute(Context context,Map<String,String> values,BooleanSupplier current)throws Exception {
        if(!"com.boop.shieldoverlay".equals(context.getPackageName()))throw new IOException("Shield only");
        String nonce=values.get("nonce"),operation=values.get("operation");
        if(nonce==null||!nonce.matches("[a-f0-9]{32}")||!("read".equals(operation)||"toggle".equals(operation)||"dislike".equals(operation)))
            throw new IOException("Invalid heart operation");
        check(current);File marker=new File(context.getFilesDir(),"boop-heart-"+nonce);
        MARKERS.put(nonce,marker);
        String phase="marker";
        try {
            try(FileOutputStream out=new FileOutputStream(marker)){out.write(nonce.getBytes(StandardCharsets.US_ASCII));}
            check(current);
            phase="existing-ha-session";
            HomeAssistantSession session=new HomeAssistantSession(new SecureCredentialStore(context),
                    new HomeAssistantAuthClient(new OkHttpClient.Builder().build()));
            HomeAssistantSession.Access access=session.ensureAccessToken();
            String base=access.baseUrl(),token=access.accessToken();
            String identity="run-as com.boop.shieldoverlay cat files/boop-heart-"+nonce+" 2>/dev/null";
            phase="hardware-binding";
            String entity=resolve(base,token,identity,nonce,current);
            check(current);
            JSONObject request=new JSONObject();
            for(String key:new String[]{"nonce","operation","title","artist","album","media_id"})request.put(key,values.get(key));
            request.put("duration",Long.parseLong(values.get("duration")));
            request.put("expected_saved",Integer.parseInt(values.get("expected_saved")));
            String encoded=Base64.encodeToString(request.toString().getBytes(StandardCharsets.UTF_8),Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING);
            String file="/data/local/tmp/boop_heart_"+nonce+".jar";
            String command="umask 077; trap 'rm -f "+file+"' EXIT; if [ \"$("+identity+")\" = '"+nonce+"' ]; then "
                    +"printf '%s' '"+DeezerBridgePayload.BASE64+"' | base64 -d > "+file+" && echo '"+DeezerBridgePayload.SHA256+"  "+file
                    +"' | sha256sum -c - >/dev/null && chmod 400 "+file+" && CLASSPATH="+file
                    +" app_process /system/bin com.boop.bridge.DeezerHeartBridge "+encoded+"; fi";
            phase="native-operation";
            String output=shell(base,token,entity,command,current);
            check(current);
            JSONObject reply=null;
            for(String line:output.split("\\r?\\n"))if(line.startsWith("BOOP_HEART_RESULT=")) {
                if(reply!=null)throw new IOException("Ambiguous heart receipt");reply=new JSONObject(line.substring("BOOP_HEART_RESULT=".length()));
            }
            if(reply==null||!nonce.equals(reply.optString("nonce"))||!operation.equals(reply.optString("operation")))throw new IOException("No matching native receipt");
            int saved=reply.optInt("saved",-1);String status=reply.optString("status");
            if(!Arrays.asList("OK","STALE_STATE","DISLIKED","UNCONFIRMED","UNAVAILABLE","TIMEOUT").contains(status)
                    ||saved< -1||saved>1||("OK".equals(status)&&saved<0))throw new IOException("Malformed native receipt");
            android.util.Log.i("BOOPHeart","operation="+operation+" status="+status+" saved="+saved+" stage="+reply.optString("stage")+" reason="+reply.optString("reason"));
            Map<String,String> result=new HashMap<>();result.put("status",status);result.put("saved",Integer.toString(saved));
            result.put("nonce",nonce);result.put("operation",operation);
            result.put("target_saved",Integer.toString(reply.optInt("target_saved",-1)));
            // A late repaint or hidden native control is not permission to toggle twice.
            return DeezerHeartReceiptRecovery.resolve(result,()->{
                check(current);
                Map<String,String> readValues=new HashMap<>(values);
                readValues.put("operation","read");
                readValues.put("nonce",UUID.randomUUID().toString().replace("-",""));
                readValues.put("expected_saved","-1");
                Map<String,String> observed=execute(context,readValues,current);
                check(current);
                android.util.Log.i("BOOPHeart","readback status="+observed.get("status")+" saved="+observed.get("saved"));
                return observed;
            });
        }catch(Exception unavailable){
            android.util.Log.w("BOOPHeart","operation="+operation+" failed at "+phase+" ("+unavailable.getClass().getSimpleName()+")");
            throw unavailable;
        }finally{cancel(nonce);marker.delete();}
    }
    private static String resolve(String base,String token,String identity,String nonce,BooleanSupplier current)throws Exception {
        String candidate=cachedBase.equals(base)?cachedEntity:"";
        if(!candidate.isEmpty()) {
            try {if(nonce.equals(shell(base,token,candidate,identity,current)))return candidate;}
            catch(IOException unavailable){check(current);}
            cachedEntity="";
        }
        JSONArray ids=new JSONArray(DeezerArtistClient.request(base+"/api/template",token,new JSONObject()
                .put("template","{{ integration_entities('androidtv') | select('match', '^media_player[.]') | list | to_json }}")));
        if(ids.length()>12)throw new IOException("Too many candidate ADB players");
        Set<String> matches=new HashSet<>();
        for(int i=0;i<ids.length();i++) {
            check(current);String id=ids.optString(i);if(!id.matches("media_player[.][a-z0-9_]+"))continue;
            try {if(nonce.equals(shell(base,token,id,identity,current)))matches.add(id);}
            catch(IOException unavailable){check(current);}
        }
        if(matches.size()!=1)throw new IOException("This Shield's ADB route is not identified");
        String id=matches.iterator().next();cachedBase=base;cachedEntity=id;return id;
    }
    private static String shell(String base,String token,String entity,String command,BooleanSupplier current)throws Exception {
        check(current);String receipt="BOOP_HEART_"+UUID.randomUUID().toString().replace("-","");
        String serviceReply=DeezerArtistClient.request(base+"/api/services/androidtv/adb_command",token,new JSONObject()
                .put("entity_id",entity).put("command","echo "+receipt+"; "+command));
        check(current);
        String output=AdbCommandReceipt.fromService(serviceReply,entity,receipt);
        if(output==null) {
            JSONObject state=new JSONObject(DeezerArtistClient.request(base+"/api/states/"+entity,token,null));
            check(current);
            output=AdbCommandReceipt.fromState(state,entity,receipt);
        }
        if(output==null)throw new IOException("Stale ADB receipt");
        return output;
    }
    private static void check(BooleanSupplier current)throws IOException {
        if(Thread.currentThread().isInterrupted()||!current.getAsBoolean())throw new IOException("Heart request cancelled");
    }
}