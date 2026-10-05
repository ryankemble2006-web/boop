"""Exercise the production parser and real loopback transport, including fragmented frames."""
from pathlib import Path
import os, subprocess
from test_deezer_native_receipts import json_dependency

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome'

HARNESS = r'''
package com.boop.shieldhome;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;
public class SerenProbe {
  static int checks;
  static void check(boolean b,String what){checks++;if(!b)throw new AssertionError(what);}
  static final String LINK="plugin://plugin.video.seren/?action=getSources&action_args=%257B%2522trakt_id%2522%253A12%257D";
  static JSONObject episode(String url) throws Exception {
    return new JSONObject().put("file",url).put("filetype","file").put("type","episode")
      .put("showtitle","A show").put("label","[COLOR red]A show[/COLOR]: 02x03 The & episode")
      .put("season",2).put("episode",3).put("art",new JSONObject()
        .put("tvshow.poster","image://https%3A%2F%2Fimage.tmdb.org%2Ft%2Fp%2Fw500%2Fshow.jpg/")
        .put("poster","https://example.test/season.jpg"));
  }
  public static void main(String[] args) throws Exception {
    JSONArray files=new JSONArray().put(episode(LINK)).put(episode(LINK))
      .put(episode("plugin://other/?action=getSources"))
      .put(episode("plugin://plugin.video.seren/?action=clearCache"))
      .put(episode(LINK).put("filetype","directory"));
    List<SerenEpisode> list=SerenEpisode.parse(new JSONObject().put("files",files));
    check(list.size()==1,"only one distinct playable Seren episode");
    SerenEpisode e=list.get(0);
    check(e.title.equals("A show"),"show identity");
    check(e.detail.contains("02x03")&&!e.detail.contains("[COLOR"),"clean episode label");
    check(e.poster.equals("https://image.tmdb.org/t/p/w500/show.jpg"),"series poster first");
    check(e.file.equals(LINK),"opaque plugin arguments preserved byte for byte");
    check(SerenEpisode.playbackParams(e).getJSONObject("item").getString("file").equals(LINK+"&forceresumeon=true"),"no extra resume click");
    check(SerenEpisode.artwork("image://https%3A%2F%2Fx.test%2Fa%2Bb.jpg/").equals("https://x.test/a+b.jpg"),"art plus preserved");
    check(SerenEpisode.artwork("image://%2Fstorage%2Fprivate/").isEmpty(),"no inaccessible local image path");
    check(SerenEpisode.artwork("https://user:pass@x.test/a.jpg").isEmpty(),"no artwork credential forwarding");
    check(SerenEpisode.parse(new JSONObject().put("files",new JSONArray())).isEmpty(),"empty feed is valid");
    boolean rejected=false;try{SerenEpisode.parse(new JSONObject());}catch(IOException expected){rejected=true;}
    check(rejected,"missing files is failure not empty cache");
    JSONObject request=SerenEpisode.directoryParams();
    check(request.getString("directory").equals("plugin://plugin.video.seren/?action=showsNextUp"),"same Next Up directory");
    try(ServerSocket server=new ServerSocket(0,1,InetAddress.getLoopbackAddress())) {
      ExecutorService pool=Executors.newSingleThreadExecutor();
      Future<String> actual=pool.submit(()->{
        try(Socket s=server.accept()) {
          JSONObject in=new JSONObject(KodiJsonRpc.readObject(new InputStreamReader(s.getInputStream(),StandardCharsets.UTF_8)));
          String response="{\"jsonrpc\":\"2.0\",\"method\":\"notice\",\"params\":{\"x\":\"}\\\"{\"}}"+
            new JSONObject().put("jsonrpc","2.0").put("id",in.getInt("id")).put("result",new JSONObject().put("ok","split é frame")).toString();
          byte[] bytes=response.getBytes(StandardCharsets.UTF_8);
          for(byte b:bytes){s.getOutputStream().write(b);s.getOutputStream().flush();}
          return in.getString("method");
        }
      });
      try(KodiJsonRpc rpc=new KodiJsonRpc("127.0.0.1",server.getLocalPort())) {
        JSONObject result=rpc.call("Files.GetDirectory",request,4000).getJSONObject("result");
        check(result.getString("ok").equals("split é frame"),"fragmented UTF-8 and ignored notification");
      }
      check(actual.get(5,TimeUnit.SECONDS).equals("Files.GetDirectory"),"actual wire request");pool.shutdownNow();
    }
    try(ServerSocket server=new ServerSocket(0,1,InetAddress.getLoopbackAddress())) {
      ExecutorService pool=Executors.newSingleThreadExecutor();
      Future<Integer> calls=pool.submit(()->{
        try(Socket s=server.accept()) {
          JSONObject in=new JSONObject(KodiJsonRpc.readObject(new InputStreamReader(s.getInputStream(),StandardCharsets.UTF_8)));
          check(in.getString("method").equals("Player.Open"),"single playback request");
          s.getOutputStream().write(new JSONObject().put("id",in.getInt("id")).put("error",new JSONObject().put("message","No source")).toString().getBytes(StandardCharsets.UTF_8));
        }
        server.setSoTimeout(250);try(Socket extra=server.accept()){return 2;}catch(SocketTimeoutException expected){return 1;}
      });
      boolean failed=false;
      try(KodiJsonRpc rpc=new KodiJsonRpc("127.0.0.1",server.getLocalPort())){rpc.call("Player.Open",SerenEpisode.playbackParams(e),2000);}catch(IOException expected){failed=true;}
      check(failed,"RPC errors surface to user");check(calls.get(5,TimeUnit.SECONDS)==1,"never replay failed playback");pool.shutdownNow();
    }
    boolean eof=false;try{KodiJsonRpc.readObject(new StringReader("{\"incomplete\":"));}catch(IOException expected){eof=true;}
    check(eof,"truncated frame fails");
    System.out.println(checks+" Seren parser/transport assertions passed");
  }
}
'''

def test_seren_feed_and_single_dispatch_transport(tmp_path):
    sources=[SRC/'SerenEpisode.java',SRC/'KodiJsonRpc.java']
    assert all(p.exists() for p in sources), 'Seren feed and one-click transport are not implemented'
    jar=json_dependency(tmp_path)
    harness=tmp_path/'SerenProbe.java';harness.write_text(HARNESS,encoding='utf-8')
    subprocess.run(['javac','-encoding','UTF-8','-cp',str(jar),'-d',str(tmp_path),*map(str,sources),str(harness)],check=True)
    subprocess.run(['java','-cp',str(tmp_path)+os.pathsep+str(jar),'com.boop.shieldhome.SerenProbe'],check=True,timeout=20)
