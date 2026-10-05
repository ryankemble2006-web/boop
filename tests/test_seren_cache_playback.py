"""Offline cache and double-click/uncertain-dispatch behaviour, with production classes."""
from pathlib import Path
import os, subprocess
from test_deezer_native_receipts import json_dependency
ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/'unified/shield-home/src/main/java/com/boop/shieldhome'
PROBE=r'''
package com.boop.shieldhome;
import java.io.*;import java.util.*;import java.util.concurrent.*;import org.json.*;
public class SerenCacheProbe {
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception {
   File file=new File(args[0],"cache.json");
   JSONObject good=new JSONObject("{\"files\":[{\"filetype\":\"file\",\"showtitle\":\"Futurama\",\"label\":\"Futurama: 11x09\",\"file\":\"plugin://plugin.video.seren/?action=getSources&action_args=opaque\"}]}");
   SerenNextUpCache cache=new SerenNextUpCache(file);
   check(cache.load().isEmpty(),"first run has no fake episodes");
   cache.accept(good);check(new SerenNextUpCache(file).load().get(0).title.equals("Futurama"),"survives new process");
   try{cache.accept(new JSONObject());throw new AssertionError("bad accepted");}catch(IOException expected){}
   check(cache.load().size()==1,"bad refresh cannot erase good row");
   try{cache.accept(new JSONObject().put("files",new JSONArray().put(new JSONObject())));throw new AssertionError("malformed row accepted");}catch(IOException expected){}
   check(cache.load().size()==1,"nonempty malformed feed retains good cache");
   cache.accept(new JSONObject().put("files",new JSONArray()));check(cache.load().isEmpty(),"valid empty feed clears stale episodes");
   java.nio.file.Files.writeString(file.toPath(),"broken JSON");check(cache.load().isEmpty(),"corrupt cache does not crash Home");
   List<Runnable> queue=new ArrayList<>();int[] launches={0},opens={0},errors={0};
   SerenPlayback playback=new SerenPlayback(queue::add,new SerenPlayback.Port(){
     public void awaitReady(){} public void open(SerenEpisode e)throws Exception{opens[0]++;throw new IOException("uncertain");}
   });
   SerenEpisode episode=new SerenEpisode("a","b","plugin://plugin.video.seren/?action=getSources&action_args=x","");
   check(playback.start(episode,()->{launches[0]++;return true;},m->errors[0]++),"first click accepted");
   check(!playback.start(episode,()->{launches[0]++;return true;},m->errors[0]++),"double click rejected");
   check(launches[0]==1&&queue.size()==1,"double click neither relaunches nor enqueues");
   queue.remove(0).run();check(opens[0]==1&&errors[0]==1,"uncertain open fails once without replay");
   check(playback.start(episode,()->false,m->errors[0]++),"failed app launch handled");
   check(queue.isEmpty()&&opens[0]==1,"no dispatch after failed app launch");
   playback.close();check(!playback.start(episode,()->true,m->{}),"closed activity cannot start playback");
   System.out.println("Seren persistent cache and playback concurrency passed");
 }
}
'''
def test_offline_cache_and_no_duplicate_playback(tmp_path):
    sources=[SRC/(n+'.java') for n in ('SerenEpisode','SerenNextUpCache','SerenPlayback')]
    assert all(p.exists() for p in sources), 'Cached one-click playback is not implemented'
    jar=json_dependency(tmp_path);probe=tmp_path/'SerenCacheProbe.java';probe.write_text(PROBE,encoding='utf-8')
    subprocess.run(['javac','-encoding','UTF-8','-cp',str(jar),'-d',str(tmp_path),*map(str,sources),str(probe)],check=True)
    subprocess.run(['java','-cp',str(tmp_path)+os.pathsep+str(jar),'com.boop.shieldhome.SerenCacheProbe',str(tmp_path)],check=True,timeout=20)
