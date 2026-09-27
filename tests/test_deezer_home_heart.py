from pathlib import Path
import subprocess, runpy
ROOT=Path(__file__).resolve().parents[1]

def test_home_unknown_refresh_and_screen_handoff(tmp_path):
    stubs=dict(runpy.run_path(str(ROOT/'tests/favourite_android_boundary.py'))['STUBS'])
    stubs['com/boop/alpha1/BoopDeezerHeartBackend.java']='''package com.boop.alpha1;
import android.content.Context;import java.util.*;import java.util.concurrent.*;import java.util.function.BooleanSupplier;
public final class BoopDeezerHeartBackend {
 public static volatile boolean holdCancellation;public static volatile int calls;public static volatile Map<String,String> input;
 public static final BlockingQueue<Map<String,String>> replies=new LinkedBlockingQueue<>();
 public static void cancel(String nonce){}
 public static Map<String,String> execute(Context c,Map<String,String> request,BooleanSupplier current)throws Exception {
  input=new HashMap<>(request);calls++;Map<String,String> reply;try{reply=replies.take();}catch(InterruptedException cancelled){if(!holdCancellation)throw cancelled;reply=replies.take();}reply.put("nonce",request.get("nonce"));reply.put("operation",request.get("operation"));return reply;
 }
 public static void reply(String status,int saved){Map<String,String> reply=new HashMap<>();reply.put("status",status);reply.put("saved",Integer.toString(saved));replies.add(reply);}
}'''
    for name,text in stubs.items():
        p=tmp_path/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text,encoding='utf-8')
    source=ROOT/'unified/shield-home/src/main/java/com/boop/shieldhome'
    files=[source/(name+'.java') for name in ['DeezerFavouritePolicy','DeezerFavouriteRequest','DeezerFavouriteController','NowPlayingSnapshot','NowPlayingState','NowPlayingActionPolicy','NowPlayingSelectionPolicy']]
    subprocess.run(['javac','-encoding','UTF-8','-d',str(tmp_path),*map(str,files),str(ROOT/'tests/java/HomeHeartProbe.java'),*map(str,tmp_path.rglob('*.java'))],check=True)
    for case in ['unknown','handoff','retry','stale','track','inflight','timeout']:
        subprocess.run(['java','-cp',str(tmp_path),'com.boop.shieldhome.HomeHeartProbe',case],check=True,timeout=12)
