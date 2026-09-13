package com.boop.shieldhome;

import java.lang.reflect.Method;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/** Functional data tests only. Invented phrases; no screenshots or visual assertions. */
public final class NativeLyricsCoreCheck {
    private static Class<?> document;
    private static int checks;
    private static void equal(Object expected, Object actual, String why) {
        if (!java.util.Objects.equals(expected, actual))
            throw new AssertionError(why + ": expected " + expected + ", got " + actual);
        checks++;
    }
    private static Object call(Object target, String name, Class<?>[] types, Object... args) throws Exception {
        Method method = target.getClass().getMethod(name, types);
        return method.invoke(target, args);
    }
    private static Object get(Object target, String name) throws Exception {
        return call(target, name, new Class<?>[0]);
    }
    private static Object parse(String body, String id) throws Exception {
        return document.getMethod("parse", String.class, String.class).invoke(null, body, id);
    }
    private static String status(Object doc) throws Exception { return get(doc, "status").toString(); }
    private static int index(Object doc, String name, long position) throws Exception {
        return (Integer) call(doc, name, new Class<?>[]{long.class}, position);
    }
    private static List<?> lines(Object doc) throws Exception { return (List<?>) get(doc, "lines"); }
    private static JSONObject line(Object start, Object duration, String text) throws Exception {
        return new JSONObject().put("milliseconds", start).put("duration", duration).put("line", text);
    }
    private static String response(Object list, Object words) throws Exception {
        JSONObject lyric = new JSONObject().put("synchronizedLines", list)
                .put("synchronizedWordByWordLines", words).put("copyright", "Test writer")
                .put("licence", "Test lyric provider");
        return new JSONObject().put("data", new JSONObject().put("track", new JSONObject()
                .put("id", "123").put("lyrics", lyric))).toString();
    }
    private static String standard() throws Exception {
        return response(new JSONArray().put(line(1000, 1000, "First invented phrase"))
                .put(line(3000, 1000, "Second invented phrase")), JSONObject.NULL);
    }
    public static void main(String[] args) throws Exception {
        try { document = Class.forName("com.boop.shieldhome.DeezerLyricsDocument"); }
        catch (ClassNotFoundException absent) {
            throw new AssertionError("Native timed lyrics document is missing; v156 cannot render timed data itself.");
        }
        Object doc = parse(standard(), "123");
        equal("AVAILABLE", status(doc), "Timed recording parses");
        equal("123", get(doc, "trackId"), "Exact recording retained");
        equal(2, lines(doc).size(), "Both timed lines retained");
        equal("First invented phrase", get(lines(doc).get(0), "text"), "Actual text retained");
        equal(1000L, get(lines(doc).get(0), "startMs"), "Start is milliseconds");
        equal(2000L, get(lines(doc).get(0), "endMs"), "Duration becomes exclusive end");
        equal(true, get(doc, "credit").toString().contains("Test lyric provider"), "Provider attribution retained");
        equal(-1, index(doc, "activeIndex", -1), "Unknown position cannot highlight");
        equal(-1, index(doc, "activeIndex", 999), "Intro has no active lyric");
        equal(0, index(doc, "activeIndex", 1000), "Start boundary inclusive");
        equal(0, index(doc, "activeIndex", 1999), "Line remains active before end");
        equal(-1, index(doc, "activeIndex", 2000), "End boundary exclusive");
        equal(-1, index(doc, "activeIndex", 2500), "Instrumental gap is not singing");
        equal(1, index(doc, "activeIndex", 3000), "Next line follows clock");
        equal(0, index(doc, "activeIndex", 1500), "Backward seek returns to earlier lyric");
        equal(-1, index(doc, "activeIndex", 5000), "Outro has no active lyric");
        equal(0, index(doc, "anchorIndex", 0), "Intro anchor bounded");
        equal(1, index(doc, "anchorIndex", 999999), "Outro anchor bounded");
        equal("UNKNOWN", status(parse(standard(), "124")), "Different recording rejected");
        for (String id : new String[]{null,"","0","-1","123;456"," 123","123/4"})
            equal("UNKNOWN", status(parse(standard(), id)), "Invalid recording identifier rejected");
        for (String body : new String[]{null,"","oops","{}","{\"data\":null}",
                "{\"data\":{\"track\":{\"id\":\"123\"}}}",
                "{\"data\":{\"track\":{\"id\":\"123\",\"lyrics\":{}}}}"})
            equal("UNKNOWN", status(parse(body,"123")), "Malformed response is not no lyrics");
        equal("UNAVAILABLE", status(parse("{\"data\":{\"track\":{\"id\":\"123\",\"lyrics\":null}}}","123")), "Explicitly absent lyric");
        equal("UNAVAILABLE", status(parse(response(new JSONArray(),JSONObject.NULL),"123")), "Explicitly empty timed formats");
        JSONObject error = new JSONObject(standard()).put("errors",new JSONArray().put(new JSONObject().put("message","Temporary failure")));
        equal("UNKNOWN",status(parse(error.toString(),"123")),"GraphQL failure is not no lyrics");
        for (JSONObject bad : new JSONObject[]{line(-1,1000,"bad"), line(1000,-1,"bad"),
                line("1000",1000,"bad"), line(1.5,1000,"bad"), line(1000,Double.MAX_VALUE,"bad")})
            equal("UNKNOWN",status(parse(response(new JSONArray().put(bad),JSONObject.NULL),"123")),"Invalid timing rejected");
        equal("UNKNOWN",status(parse(response("not a list",JSONObject.NULL),"123")),"Wrong timed list type");
        equal("UNKNOWN",status(parse(response(new JSONArray().put(JSONObject.NULL),JSONObject.NULL),"123")),"Null line rejected");
        Object reordered = parse(response(new JSONArray().put(line(3000,1000,"Later"))
                .put(line(1000,9000,"Earlier")),JSONObject.NULL),"123");
        equal("AVAILABLE",status(reordered),"Explicit timestamps can be sorted");
        equal("Earlier",get(lines(reordered).get(0),"text"),"Timestamp ordering");
        equal(3000L,get(lines(reordered).get(0),"endMs"),"Overlap ends at next line");
        Object duplicate = parse(response(new JSONArray().put(line(1000,1000,"First voice"))
                .put(line(1000,2000,"Second voice")),JSONObject.NULL),"123");
        equal(1,lines(duplicate).size(),"Simultaneous lines share one cue");
        equal("First voice\nSecond voice",get(lines(duplicate).get(0),"text"),"Neither simultaneous voice lost");
        Object silence = parse(response(new JSONArray().put(line(1000,1000,"Singing"))
                .put(line(2000,1000,"")),JSONObject.NULL),"123");
        equal(-1,index(silence,"activeIndex",2500),"Blank timed cue is silence");
        JSONObject wordLine = new JSONObject().put("start",2000).put("end",3000)
                .put("words",new JSONArray().put(new JSONObject().put("start",2000).put("end",2300).put("word","Synthetic"))
                        .put(new JSONObject().put("start",2400).put("end",3000).put("word","words")));
        Object wordDoc = parse(response(JSONObject.NULL,new JSONArray().put(wordLine)),"123");
        equal("AVAILABLE",status(wordDoc),"Word-only timestamps supported");
        equal(2000L,get(lines(wordDoc).get(0),"startMs"),"Word timing is also milliseconds");
        equal(3000L,get(lines(wordDoc).get(0),"endMs"),"Word line end retained");
        equal("Synthetic words",get(lines(wordDoc).get(0),"text"),"Word-only line assembled");
        equal("UNKNOWN",status(parse("x".repeat(262145),"123")),"Response bound enforced");
        try { lines(doc).clear(); throw new AssertionError("Document lines must be immutable"); }
        catch (UnsupportedOperationException expected) { checks++; }
        Class<?> gateType;
        try { gateType=Class.forName("com.boop.shieldhome.LyricsRequestGate"); }
        catch(ClassNotFoundException absent) { throw new AssertionError("Native lyrics request ownership gate is missing"); }
        Object gate=gateType.getConstructor().newInstance();
        long first=(Long)call(gate,"begin",new Class<?>[]{String.class},"session:track-A");
        equal(true,call(gate,"accepts",new Class<?>[]{long.class,String.class},first,"session:track-A"),"Current result accepted");
        long second=(Long)call(gate,"begin",new Class<?>[]{String.class},"session:track-B");
        equal(false,call(gate,"accepts",new Class<?>[]{long.class,String.class},first,"session:track-A"),"Late previous track discarded");
        equal(false,call(gate,"accepts",new Class<?>[]{long.class,String.class},second,"session:track-A"),"Wrong identity discarded");
        call(gate,"cancel",new Class<?>[0]);
        equal(false,call(gate,"accepts",new Class<?>[]{long.class,String.class},second,"session:track-B"),"Hidden/destroyed owner discards results");
        long third=(Long)call(gate,"begin",new Class<?>[]{String.class},"session:track-B");
        equal(false,call(gate,"accepts",new Class<?>[]{long.class,String.class},second,"session:track-B"),"Reopened same track rejects old generation");
        equal(true,call(gate,"accepts",new Class<?>[]{long.class,String.class},third,"session:track-B"),"Reopened owner accepts fresh result");
        System.out.println("PASS: "+checks+" native lyrics data/timing/ownership checks. No visual checks performed.");
    }
}
