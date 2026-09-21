package com.boop.alpha1;
import org.json.*;import java.io.*;import java.util.*;
/** Synthetic concurrent responses only: no credentials or device access. */
public final class DeezerReceiptRaceProbe {
 static int assertions;
 static void yes(boolean value,String reason){assertions++;if(!value)throw new AssertionError(reason);}
 static final class Rig implements DeezerArtistClient.Http {
  String mode,latest="";int posts,gets,plays,reads;boolean roomChanged,failedGet;
  Map<String,String> durable=new HashMap<>();
  Rig(String mode){this.mode=mode;}
  public String request(String url,String token,JSONObject body)throws Exception {
   if(body==null){gets++;
    if(mode.equals("get-timeout") && plays==1 && !failedGet){failedGet=true;throw new IOException("synthetic read timeout");}
    return state("media_player.shield",latest).toString();}
   posts++;String command=body.getString("command");String nonce=command.substring(5,command.indexOf(';'));
   if(command.contains("cat /data/local/tmp/boop_receipt_") && !command.contains("mkdir ")) {
    reads++;
    String recovered=durable.getOrDefault(nonce,"");
    if(mode.equals("incomplete"))recovered=recovered.replace("\n"+nonce+"_DONE","");
    latest=mode.equals("empty") || mode.equals("prefix") || mode.equals("wrong-entity")
       || (mode.equals("read-race")&&reads==1)?"BOOP_HEART_other\nOK":recovered;
    return new JSONArray().put(state("media_player.shield",latest)).toString();
   }
   boolean play=command.contains("app_process");if(play)plays++;
   String result=nonce+"\n"+(play?(mode.equals("provider-failure")?"BOOP_MEDIA_UNAVAILABLE":"BOOP_MEDIA_REQUESTED"):"02:00:00:00:00:01")+"\n"+nonce+"_DONE";
   durable.put(nonce,result);
   if(play && mode.equals("post-timeout"))throw new IOException("synthetic post timeout after execution");
   latest=result;
   if(mode.equals("legacy"))return "[]";
   if(mode.equals("room-change")&&play)roomChanged=true;
   if(play && Arrays.asList("durable-race","read-race","incomplete","get-timeout").contains(mode)) {
    latest="BOOP_HEART_displaced_both\nOK";return "[]";
   }
   if(mode.equals("empty")) {latest="BOOP_HEART_someone_else\nOK";return "[]";}
   if(mode.equals("prefix"))result=nonce+"_OTHER\nBOOP_MEDIA_REQUESTED";
   String entity=mode.equals("wrong-entity")?"media_player.other":"media_player.shield";
   JSONArray response=new JSONArray().put(state(entity,result));
   if(mode.equals("unrelated"))response.put(state("media_player.other","unrelated"));
   if(mode.equals("conflicting")&&play)response.put(state(entity,nonce+"\nBOOP_MEDIA_UNAVAILABLE"));
   if(mode.equals("identical"))response.put(state(entity,result));
   // Native playback triggers another device's heart lookup before the later GET.
   if(play || !mode.equals("race")) latest="BOOP_HEART_a_different_request\n{\"status\":\"OK\"}";
   return response.toString();
  }
  JSONObject state(String entity,String output){return new JSONObject().put("entity_id",entity).put("state","playing").put("attributes",new JSONObject().put("adb_response",output));}
  boolean run(){
   BoopRoom room=new BoopRoom("lounge","Lounge");
   try{new DeezerNativeController("http://ha.invalid","test-token","media_player.shield",this,ms->{throw new AssertionError("No delay/replay");},room,()->roomChanged?new BoopRoom("other","Other"):room)
     .play(new JSONArray().put("02:00:00:00:00:01"),new DeezerCatalogue.Selection());return true;}
   catch(Exception failed){System.out.println("REJECTED mode="+mode+" type="+failed.getClass().getSimpleName());return false;}
  }
 }
 public static void main(String[] args){
  Rig raced=new Rig("race");boolean success=raced.run();yes(raced.plays==1,"Playback really executed before checking the response");yes(success,"Successful native playback must not become Failed when heart overwrites latest HA response");
  yes(raced.posts==2&&raced.plays==1,"Only identity plus ONE playback command");yes(raced.gets==0,"No overwritten latest-state read after own receipt");
  for(String mode:Arrays.asList("unrelated","identical")){Rig r=new Rig(mode);yes(r.run(),"Own exact receipt accepted "+mode);yes(r.plays==1,"No duplicated playback");}
  Rig legacy=new Rig("legacy");yes(legacy.run(),"Older response fallback remains");yes(legacy.gets==2&&legacy.plays==1,"Legacy performs exact GET only");
  for(String mode:Arrays.asList("durable-race","read-race","get-timeout","post-timeout")) {
   Rig r=new Rig(mode);yes(r.run(),"Command-owned receipt survives overwritten service AND state: "+mode);
   yes(r.plays==1,"Receipt recovery never replays music");yes(r.reads>0&&r.reads<=3,"Recovery is bounded and read-only");
  }
  for(String mode:Arrays.asList("empty","prefix","wrong-entity","provider-failure","room-change","conflicting","incomplete")){
   Rig r=new Rig(mode);yes(!r.run(),"Must reject "+mode);yes(r.plays<=1,"Never replay uncertain command "+mode);
  }
  System.out.println("Music receipt race: "+assertions+" checks passed");
 }
}
