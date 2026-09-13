package com.boop.shieldhome;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.io.IOException;
import org.json.JSONObject;

/** Exercises the real client with bounded invented transport responses, never a real account. */
public final class NativeLyricsTransportCheck {
    private static int checks;
    private static final String RESPONSE = "{\"data\":{\"track\":{\"id\":\"123\",\"lyrics\":{\"synchronizedLines\":[{\"line\":\"Invented transport test\",\"milliseconds\":1000,\"duration\":1000}],\"synchronizedWordByWordLines\":null}}}}";
    private static void equal(Object expected, Object actual, String why) {
        if (!java.util.Objects.equals(expected, actual)) throw new AssertionError(why+": "+actual);
        checks++;
    }
    private static final class Transport implements DeezerLyricsClient.Transport {
        int auth, queries;
        boolean fail;
        String response = RESPONSE;
        public String request(String url, String bearer, String body, DeezerLyricsClient.Call call, long deadline) throws Exception {
            if (DeezerLyricsClient.AUTH_URL.equals(url)) {
                auth++;
                equal(null,bearer,"Anonymous request carries no account bearer");
                equal(null,body,"Anonymous request carries no account data");
                return "{\"jwt\":\"test.guest.session\"}";
            }
            equal(DeezerLyricsClient.API_URL,url,"Only approved data endpoint");
            queries++;
            equal("test.guest.session",bearer,"Only fresh guest bearer used");
            JSONObject request = new JSONObject(body);
            equal("123",request.getJSONObject("variables").getString("trackId"),"Exact recording requested");
            String query = request.getString("query");
            equal(true,query.contains("milliseconds") && query.contains("duration") && query.contains("line"),"Actual timed lines requested");
            equal(true,query.contains("copyright") && query.contains("licence"),"Attribution requested");
            if (fail) throw new IOException("Synthetic unavailable transport");
            return response;
        }
    }
    public static void main(String[] args) throws Exception {
        Class<?> clientType;
        try { clientType=Class.forName("com.boop.shieldhome.DeezerTimedLyricsClient"); }
        catch (ClassNotFoundException absent) { throw new AssertionError("Native timed-data client is missing; availability-only is insufficient."); }
        Constructor<?> constructor = clientType.getDeclaredConstructor(DeezerLyricsClient.Transport.class);
        constructor.setAccessible(true);
        Method load = clientType.getDeclaredMethod("load",String.class,DeezerLyricsClient.Call.class,long.class);
        load.setAccessible(true);
        Transport transport = new Transport();
        Object client = constructor.newInstance(transport);
        DeezerLyricsDocument first=(DeezerLyricsDocument)load.invoke(client,"123",new DeezerLyricsClient.Call(),DeezerLyricsClient.nowMs()+2500);
        equal(DeezerLyricsDocument.Status.AVAILABLE,first.status(),"Client returns actual timed document");
        equal("Invented transport test",first.lines().get(0).text(),"Words survive real client path");
        load.invoke(client,"123",new DeezerLyricsClient.Call(),DeezerLyricsClient.nowMs()+2500);
        equal(1,transport.auth,"Guest token reused briefly in memory");
        equal(2,transport.queries,"Each distinct load requests data");
        DeezerLyricsClient.Call cancelled = new DeezerLyricsClient.Call(); cancelled.cancel();
        equal(DeezerLyricsDocument.Status.UNKNOWN,((DeezerLyricsDocument)load.invoke(client,"123",cancelled,DeezerLyricsClient.nowMs()+2500)).status(),"Cancelled is unknown");
        equal(DeezerLyricsDocument.Status.UNKNOWN,((DeezerLyricsDocument)load.invoke(client,"123",new DeezerLyricsClient.Call(),DeezerLyricsClient.nowMs()-1)).status(),"Expired deadline is unknown");
        equal(DeezerLyricsDocument.Status.UNKNOWN,((DeezerLyricsDocument)load.invoke(client,"bad",new DeezerLyricsClient.Call(),DeezerLyricsClient.nowMs()+2500)).status(),"Invalid ID is unknown");
        equal(2,transport.queries,"Cancelled, expired and invalid requests do not reach network");
        transport.fail=true;
        equal(DeezerLyricsDocument.Status.UNKNOWN,((DeezerLyricsDocument)load.invoke(client,"123",new DeezerLyricsClient.Call(),DeezerLyricsClient.nowMs()+2500)).status(),"Network failure is not no lyrics");
        transport.fail=false;
        transport.response=RESPONSE.replace("\"id\":\"123\"","\"id\":\"124\"");
        equal(DeezerLyricsDocument.Status.UNKNOWN,((DeezerLyricsDocument)load.invoke(client,"123",new DeezerLyricsClient.Call(),DeezerLyricsClient.nowMs()+2500)).status(),"Different returned recording rejected");
        equal(2,transport.auth,"Failure forces a fresh anonymous token");
        System.out.println("PASS: "+checks+" native lyrics transport checks. No real tokens or lyrics logged.");
    }
}
