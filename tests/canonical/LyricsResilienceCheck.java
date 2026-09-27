package com.boop.shieldhome;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import org.json.JSONObject;

public final class LyricsResilienceCheck {
    static final Queue<Object> replies = new ConcurrentLinkedQueue<>();
    static final List<String> requests = new CopyOnWriteArrayList<>();
    static int checks, failures;
    static DeezerLyricsClient.Call cancelAtResponse;
    static long delay;
    static String row(String artist, int duration, boolean timed) {
        return new JSONObject().put("trackName", "Gonna Make You a Star")
            .put("artistName", artist).put("duration", duration)
            .put("syncedLyrics", timed ? "[00:01.00]Test line\n[00:03.00]Next line" : "")
            .put("plainLyrics", "Untimed text").toString();
    }
    static class Response {int code; String body; Response(int c,String b){code=c;body=b;}}
    static Response ok(String body){return new Response(200,body);}
    static Response miss(){return new Response(404,"");}
    static void reset(Object... values) {replies.clear();requests.clear();cancelAtResponse=null;delay=0;Collections.addAll(replies,values);}
    static void expect(boolean value,String why){checks++;if(!value){failures++;System.err.println("FAIL: "+why);}}
    static DeezerLyricsDocument lookup(long budget) {
        return new LrclibLyricsClient().load(new NowPlayingSnapshot(),"123",new DeezerLyricsClient.Call(),DeezerLyricsClient.nowMs()+budget);
    }
    static DeezerLyricsDocument load(String id) throws Exception {
        return load(id, false);
    }
    static DeezerLyricsDocument load(String id, boolean refresh) throws Exception {
        NativeLyricsLoader loader=new NativeLyricsLoader(); CountDownLatch done=new CountDownLatch(1);
        DeezerLyricsDocument[] result=new DeezerLyricsDocument[1];
        java.util.function.Consumer<DeezerLyricsDocument> callback=d->{result[0]=d;done.countDown();};
        if (refresh) {
            try {NativeLyricsLoader.class.getDeclaredMethod("reload",NowPlayingSnapshot.class,String.class,String.class,java.util.function.Consumer.class)
                .invoke(loader,new NowPlayingSnapshot(),id,id,callback);}
            catch(NoSuchMethodException missing){throw new AssertionError("Manual lookup must support bypassing the previous successful cache",missing);}
        } else loader.load(new NowPlayingSnapshot(),id,id,callback);
        try {if(!done.await(5,TimeUnit.SECONDS))throw new AssertionError("loader callback missing");return result[0];}
        finally {loader.destroy();}
    }
    public static void main(String[] args) throws Exception {
        URL.setURLStreamHandlerFactory(protocol -> new URLStreamHandler(){
            protected URLConnection openConnection(URL url) {
                requests.add(url.toString()); Object next=replies.poll();
                if(next==null)throw new AssertionError("Unexpected request "+url);
                return new HttpURLConnection(url){
                    public void connect(){} public void disconnect(){} public boolean usingProxy(){return false;}
                    public int getResponseCode() throws IOException {
                        if(delay>0)try{Thread.sleep(delay);}catch(InterruptedException e){throw new InterruptedIOException();}
                        if(cancelAtResponse!=null)cancelAtResponse.cancel();
                        if(next instanceof IOException)throw (IOException)next;
                        return ((Response)next).code;
                    }
                    public InputStream getInputStream(){return new ByteArrayInputStream(((Response)next).body.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
                    public OutputStream getOutputStream(){return new ByteArrayOutputStream();}
                };
            }
        });
        String valid=row("David Essex",218,true);
        reset(new Response(503,""),ok(valid));
        expect(lookup(6000).status()==DeezerLyricsDocument.Status.AVAILABLE,"503 then success must recover");
        expect(requests.size()==2,"retry temporary server failure once");
        reset(new SocketTimeoutException("temporary"),ok(valid));
        expect(lookup(6000).status()==DeezerLyricsDocument.Status.AVAILABLE,"timeout then success must recover");
        reset(new Response(503,""),new Response(503,""));
        expect(lookup(6000).status()==DeezerLyricsDocument.Status.UNKNOWN,"repeated server failure remains uncertain");
        expect(requests.size()==2,"retries stay bounded");
        reset(new Response(400,"")); lookup(6000);
        expect(requests.size()==1,"permanent HTTP errors are not retried");
        reset(miss(),miss(),ok("["+valid+"]"));
        expect(lookup(6000).status()==DeezerLyricsDocument.Status.AVAILABLE,"exact misses must reach matching search recording");
        reset(ok(row("David Essex",218,false)),ok(row("David Essex",218,false)),ok("["+valid+"]"));
        expect(lookup(6000).status()==DeezerLyricsDocument.Status.AVAILABLE,"untimed exact result must reach timed search");
        reset(miss(),miss(),ok("["+row("Other Artist",218,true)+","+row("David Essex",230,true)+"]"));
        expect(lookup(6000).status()==DeezerLyricsDocument.Status.UNAVAILABLE,"wrong artist or recording duration must not match");
        reset(new Response(503,"")); delay=25;
        expect(lookup(10).status()==DeezerLyricsDocument.Status.UNKNOWN,"deadline expiry is uncertainty");
        expect(requests.size()==1,"deadline expiry must prevent retry");
        reset(new Response(503,"")); cancelAtResponse=new DeezerLyricsClient.Call();
        new LrclibLyricsClient().load(new NowPlayingSnapshot(),"123",cancelAtResponse,DeezerLyricsClient.nowMs()+6000);
        expect(requests.size()==1,"cancellation prevents retry");
        reset(ok(valid));
        expect(load("101").status()==DeezerLyricsDocument.Status.AVAILABLE,"LRCLIB success should complete lookup");
        expect(requests.size()==1 && requests.get(0).contains("lrclib.net"),"LRCLIB success must never contact Deezer");
        reset();
        expect(load("101").status()==DeezerLyricsDocument.Status.AVAILABLE && requests.isEmpty(),"Normal screen entry reuses the successful lookup");
        reset(ok(valid));
        expect(load("101",true).status()==DeezerLyricsDocument.Status.AVAILABLE && requests.size()==1,"Explicit lookup must contact provider even when previous lyrics are cached");
        reset(new Response(503,""),new Response(503,""),ok("{\"jwt\":\"a.b.c\"}"),ok("{\"data\":{\"track\":{\"id\":\"102\",\"lyrics\":{\"synchronizedLines\":[{\"line\":\"Test\",\"milliseconds\":1000,\"duration\":2000}],\"synchronizedWordByWordLines\":[]}}}}"));
        expect(load("102").status()==DeezerLyricsDocument.Status.AVAILABLE,"Deezer must rescue LRCLIB failure");
        expect(requests.size()==4 && requests.get(2).contains("auth.deezer.com"),"Deezer is only contacted after LRCLIB retry");
        reset(new Response(503,""),new Response(503,""),ok("{\"jwt\":\"a.b.c\"}"),ok("{\"data\":{\"track\":{\"id\":\"103\",\"lyrics\":null}}}"));
        expect(load("103").status()==DeezerLyricsDocument.Status.UNKNOWN,"one failed provider plus one miss is not confirmed absence");
        reset(miss(),miss(),ok("[]"),ok("{\"jwt\":\"a.b.c\"}"),ok("{\"data\":{\"track\":{\"id\":\"104\",\"lyrics\":null}}}"));
        expect(load("104").status()==DeezerLyricsDocument.Status.UNAVAILABLE,"two clean misses report no lyrics");
        if(failures>0)throw new AssertionError(failures+" of "+checks+" checks failed");
        System.out.println("PASS: "+checks+" production lyrics resilience checks");
    }
}
