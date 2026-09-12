package com.boop.shieldhome;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeezerLyricsTransportTest {
    private static final class Fake implements DeezerLyricsClient.Transport {
        final List<String> urls = new ArrayList<>(), bearers = new ArrayList<>();
        String api = "{\"data\":{\"track\":{\"id\":\"123\",\"lyrics\":null}}}";
        String guest = "{\"jwt\":\"guest.only.test\"}";
        String payload;
        boolean cancelAfterAuth;
        @Override public String request(String url, String bearer, String body,
                DeezerLyricsClient.Call call, long deadline) throws Exception {
            urls.add(url); bearers.add(bearer); payload = body;
            if (cancelAfterAuth && url.equals(DeezerLyricsClient.AUTH_URL)) call.cancel();
            if (api == null) throw new IOException("offline");
            return url.equals(DeezerLyricsClient.AUTH_URL) ? guest : api;
        }
    }
    @Test public void usesGuestThenExactIdWithoutAccountCredentials() throws Exception {
        Fake f = new Fake(); DeezerLyricsClient c = new DeezerLyricsClient(f);
        assertEquals(DeezerLyricsClient.Result.UNAVAILABLE, c.check("123", new DeezerLyricsClient.Call(), DeezerLyricsClient.nowMs()+2500));
        assertEquals(2, f.urls.size()); assertNull(f.bearers.get(0));
        assertEquals("guest.only.test", f.bearers.get(1));
        org.json.JSONObject request = new org.json.JSONObject(f.payload);
        assertEquals("123", request.getJSONObject("variables").getString("trackId"));
        assertFalse(request.getString("query").matches("(?s).*\\b(text|line|word)\\b.*"));
    }
    @Test public void reusesOnlyTheInMemoryGuestSession() {
        Fake f = new Fake(); DeezerLyricsClient c = new DeezerLyricsClient(f);
        c.check("123", new DeezerLyricsClient.Call(), DeezerLyricsClient.nowMs()+2500);
        c.check("123", new DeezerLyricsClient.Call(), DeezerLyricsClient.nowMs()+2500);
        assertEquals(3, f.urls.size());
    }
    @Test public void cancellationInvalidIdentityAndExpiredDeadlineDoNoNetworkWork() {
        Fake f = new Fake(); DeezerLyricsClient c = new DeezerLyricsClient(f);
        DeezerLyricsClient.Call call = new DeezerLyricsClient.Call(); call.cancel();
        assertEquals(DeezerLyricsClient.Result.UNKNOWN, c.check("123", call, DeezerLyricsClient.nowMs()+2500));
        assertEquals(DeezerLyricsClient.Result.UNKNOWN, c.check("", new DeezerLyricsClient.Call(), DeezerLyricsClient.nowMs()+2500));
        assertEquals(DeezerLyricsClient.Result.UNKNOWN, c.check("123", new DeezerLyricsClient.Call(), DeezerLyricsClient.nowMs()-1));
        assertTrue(f.urls.isEmpty());
    }
    @Test public void networkAndAuthenticationFailureAreUnknown() {
        Fake f = new Fake(); f.api = null;
        assertEquals(DeezerLyricsClient.Result.UNKNOWN, new DeezerLyricsClient(f).check("123", new DeezerLyricsClient.Call(), DeezerLyricsClient.nowMs()+2500));
        f = new Fake(); f.guest = "{}";
        assertEquals(DeezerLyricsClient.Result.UNKNOWN, new DeezerLyricsClient(f).check("123", new DeezerLyricsClient.Call(), DeezerLyricsClient.nowMs()+2500));
        assertEquals(1, f.urls.size());
    }
    @Test public void cancellationDuringGuestLoginPreventsTheTrackRequest() {
        Fake f=new Fake(); f.cancelAfterAuth=true;
        assertEquals(DeezerLyricsClient.Result.UNKNOWN,new DeezerLyricsClient(f).check("123",new DeezerLyricsClient.Call(),DeezerLyricsClient.nowMs()+2500));
        assertEquals(1,f.urls.size());
    }
    @Test public void malformedGuestBearerNeverReachesTheApi() {
        Fake f=new Fake(); f.guest="{\"jwt\":\"not-a-jwt\"}";
        assertEquals(DeezerLyricsClient.Result.UNKNOWN,new DeezerLyricsClient(f).check("123",new DeezerLyricsClient.Call(),DeezerLyricsClient.nowMs()+2500));
        assertEquals(1,f.urls.size());
    }
}
