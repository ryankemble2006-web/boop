package com.boop.alpha1;
import org.junit.Test;
import static org.junit.Assert.*;
import org.json.*;
import java.util.*;

public final class LocalPlayerCloseClientTest {
    private static final String NONCE="0123456789abcdef0123456789abcdef";
    private static final class Fake implements DeezerArtistClient.Http {
        String response="";
        boolean stale, cancelAfterIdentity, valid=true;
        int closes;
        Set<String> matching=new HashSet<>();
        public String request(String url,String token,JSONObject body) throws Exception {
            if(url.endsWith("/api/template")) return "[\"media_player.a\",\"media_player.b\"]";
            if(url.endsWith("/adb_command")) {
                String cmd=body.getString("command");
                String receipt=cmd.substring(5,cmd.indexOf(';'));
                if(cmd.contains("am force-stop")) { closes++; response=receipt+"\nCLOSED_"+NONCE; }
                else {
                    response=receipt+"\n"+(matching.contains(body.getString("entity_id"))?NONCE:"");
                    if(cancelAfterIdentity) valid=false;
                }
                return "[]";
            }
            return new JSONObject().put("attributes",new JSONObject().put("adb_response",stale?"old receipt":response)).toString();
        }
    }
    private void run(Fake fake) throws Exception {
        new LocalPlayerCloseClient(fake).close("http://test.invalid","test-token",
                new LocalPlayerCloseGate(1,"deezer.android.app",NONCE),()->fake.valid);
    }
    @Test public void exactLocalMarkerRequired() throws Exception {
        Fake f=new Fake(); f.matching.add("media_player.b"); run(f); assertEquals(1,f.closes);
    }
    @Test public void wrongHardwareNeverCloses() throws Exception {
        Fake f=new Fake(); try { run(f); fail(); } catch(java.io.IOException expected) { }
        assertEquals(0,f.closes);
    }
    @Test public void ambiguousHardwareNeverCloses() throws Exception {
        Fake f=new Fake(); f.matching.addAll(Arrays.asList("media_player.a","media_player.b"));
        try { run(f); fail(); } catch(java.io.IOException expected) { }
        assertEquals(0,f.closes);
    }
    @Test public void staleResponseNeverCloses() throws Exception {
        Fake f=new Fake(); f.stale=true;
        try { run(f); fail(); } catch(java.io.IOException expected) { }
        assertEquals(0,f.closes);
    }
    @Test public void selectionChangeDuringDiscoveryNeverCloses() throws Exception {
        Fake f=new Fake(); f.matching.add("media_player.a"); f.cancelAfterIdentity=true;
        try { run(f); fail(); } catch(java.io.IOException expected) { }
        assertEquals(0,f.closes);
    }
}
