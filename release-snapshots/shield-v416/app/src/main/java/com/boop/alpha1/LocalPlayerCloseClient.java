package com.boop.alpha1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

/** Uses the existing authenticated HA ADB route; proves local hardware by a private marker. */
final class LocalPlayerCloseClient {
    interface Current { boolean valid(); }
    private final DeezerArtistClient.Http http;
    LocalPlayerCloseClient() { this(DeezerArtistClient::request); }
    LocalPlayerCloseClient(DeezerArtistClient.Http http) { this.http=http; }
    void close(String base, String token, LocalPlayerCloseGate gate, Current current) throws Exception {
        check(current);
        JSONArray ids=new JSONArray(http.request(base+"/api/template",token,new JSONObject()
                .put("template","{{ integration_entities('androidtv') | select('match', '^media_player[.]') | list | to_json }}")));
        List<String> matches=new ArrayList<>();
        if(ids.length()>24) throw new IOException("Too many candidate players");
        for(int i=0;i<ids.length();i++) {
            String id=ids.optString(i);
            if(!id.matches("media_player[.][a-z0-9_]+")) continue;
            check(current);
            if(gate.nonce.equals(shell(base,token,id,gate.identityCommand()))) {
                if(!matches.contains(id)) matches.add(id);
            }
        }
        if(matches.size()!=1) throw new IOException("Could not identify this Shield");
        check(current);
        String result=shell(base,token,matches.get(0),gate.closeCommand());
        if(!("CLOSED_"+gate.nonce).equals(result)) throw new IOException("Player close was not confirmed");
    }
    private static void check(Current current) throws IOException {
        if(Thread.currentThread().isInterrupted() || !current.valid()) throw new IOException("Close cancelled");
    }
    private String shell(String base,String token,String id,String command) throws Exception {
        String receipt="BOOP_CLOSE_"+UUID.randomUUID().toString().replace("-","");
        http.request(base+"/api/services/androidtv/adb_command",token,new JSONObject()
                .put("entity_id",id).put("command","echo "+receipt+"; "+command));
        JSONObject state=new JSONObject(http.request(base+"/api/states/"+id,token,null));
        JSONObject attrs=state.optJSONObject("attributes");
        String response=attrs==null ? "" : attrs.optString("adb_response");
        String[] lines=response.split("\\r?\\n",2);
        if(lines.length==0 || !receipt.equals(lines[0])) throw new IOException("Stale close response");
        return lines.length==1 ? "" : lines[1].trim();
    }
}
