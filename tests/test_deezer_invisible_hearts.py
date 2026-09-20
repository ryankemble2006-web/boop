from pathlib import Path
import subprocess
ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'unified/deezer-bridge/DeezerHeartRules.java'
def test_native_heart_receipts_and_row_selection(tmp_path):
    assert SOURCE.exists(), 'Invisible native heart rules have not been implemented'
    probe=tmp_path/'HeartRulesProbe.java'
    probe.write_text('''package com.boop.bridge;
public final class HeartRulesProbe {
 static void yes(boolean b){if(!b)throw new AssertionError();}
 static int[] shape(boolean filled){int[] a=new int[41*41];java.util.Arrays.fill(a,0xff192128);
  for(int y=8;y<=29;y++)for(int x=9;x<=31;x++)if(filled || x<11 || x>29 || y<10 || y>27)a[y*41+x]=filled?0xffbb66ff:0xffeeeeee; return a;}
 public static void main(String[] args){
  yes(DeezerHeartRules.classify(shape(true),41,41)==1);
  yes(DeezerHeartRules.classify(shape(false),41,41)==0);
  yes(DeezerHeartRules.classify(new int[41*41],41,41)==-1);
  int[] solid=new int[41*41];java.util.Arrays.fill(solid,0xffbb66ff);
  yes(DeezerHeartRules.classify(solid,41,41)==-1);
  java.util.Arrays.fill(solid,0xffffffff);yes(DeezerHeartRules.classify(solid,41,41)==-1);
  yes(DeezerHeartRules.classify(null,41,41)==-1);
  yes(DeezerHeartRules.classify(new int[40],41,41)==-1);
  int[][] row={{64,398,112,446},{120,398,168,446},{520,398,568,446},{584,398,632,446},{648,398,696,446},{712,398,760,446}};
  yes(DeezerHeartRules.heartIndex(row,422,false)==1);
  yes(DeezerHeartRules.heartIndex(row,422,true)==0);
  yes(DeezerHeartRules.heartIndex(row,100,false)==-1);
  yes(DeezerHeartRules.heartIndex(new int[][]{{64,398,112,446},{120,398,168,446}},422,false)==-1);
  int[][] bad=row.clone();bad[1]=new int[]{420,398,468,446};yes(DeezerHeartRules.heartIndex(bad,422,false)==-1);
  yes(DeezerHeartRules.requestAllowed("read",-1));
  yes(DeezerHeartRules.requestAllowed("toggle",0));yes(DeezerHeartRules.requestAllowed("toggle",1));
  yes(DeezerHeartRules.requestAllowed("toggle",-1));yes(DeezerHeartRules.requestAllowed("dislike",-1));
  yes(!DeezerHeartRules.requestAllowed("dislike;rm",0));yes(!DeezerHeartRules.requestAllowed("toggle",2));
  yes(DeezerHeartRules.sameTrack("abc","abc"));yes(!DeezerHeartRules.sameTrack("",""));yes(!DeezerHeartRules.sameTrack("abc","def"));
  yes(DeezerHeartRules.shouldToggle(0,-1));yes(DeezerHeartRules.shouldToggle(1,1));
  yes(!DeezerHeartRules.shouldToggle(0,1));yes(!DeezerHeartRules.shouldToggle(-1,-1));
  System.out.println("Native heart rules: 27 checks passed");
 }
}''',encoding='utf-8')
    subprocess.run(['javac','-d',str(tmp_path),str(SOURCE),str(probe)],check=True)
    subprocess.run(['java','-cp',str(tmp_path),'com.boop.bridge.HeartRulesProbe'],check=True)
def test_two_toggle_fills_and_outlined_dislike_follow_runtime_accent():
    src=ROOT/'unified/shield-home/src/main/java/com/boop/shieldhome'
    button=(src/'DeezerFavouriteButton.java').read_text(encoding='utf-8')
    lyrics=(src/'ShieldLyricsView.java').read_text(encoding='utf-8')
    assert 'DISLIKE' in button, 'Left heart is not yet a dislike action'
    assert 'controller.dislike(snapshot)' in button
    assert 'int accent = FocusChrome.accentColor(getContext());' in button
    assert 'saved && mode == TOGGLE' in button
    assert 'saved && mode == TOGGLE ? accent' in button
    assert 'DeezerFavouriteButton.DISLIKE' in lyrics
    assert 'DeezerFavouriteButton.TOGGLE' in lyrics
    assert 'DeezerFavouriteButton.ADD' not in lyrics
    assert 'DeezerFavouriteButton.REMOVE' not in lyrics

def test_offscreen_async_controller_shared_receipts_and_no_progress_polling(tmp_path):
    import runpy
    boundary=runpy.run_path(str(ROOT/'tests/favourite_android_boundary.py'))
    stubs=dict(boundary['STUBS'])
    stubs['com/boop/alpha1/BoopDeezerHeartBackend.java']='''package com.boop.alpha1;
import android.content.Context; import java.util.*; import java.util.concurrent.*; import java.util.function.BooleanSupplier;
public final class BoopDeezerHeartBackend {
 public static volatile int calls,cancels; public static volatile Map<String,String> input;
 public static final BlockingQueue<Map<String,String>> replies=new LinkedBlockingQueue<>();
 public static void cancel(String nonce){cancels++;}
 public static Map<String,String> execute(Context c,Map<String,String> request,BooleanSupplier current)throws Exception {
  input=new HashMap<>(request);calls++;
  Map<String,String> response=replies.take();response.put("nonce",request.get("nonce"));response.put("operation",request.get("operation"));return response;
 }
 public static void reply(String status,int saved){Map<String,String> m=new HashMap<>();m.put("status",status);m.put("saved",Integer.toString(saved));replies.add(m);}
}'''
    for name,text in stubs.items():
        p=tmp_path/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text,encoding='utf-8')
    code='''package com.boop.shieldhome;
import android.content.*;import android.media.*;import android.media.session.*;import com.boop.alpha1.BoopDeezerHeartBackend;
public final class InvisibleControllerProbe {
 static void yes(boolean v,String name){if(!v)throw new AssertionError(name);}
 static void waitFor(java.util.function.BooleanSupplier done)throws Exception{long end=System.currentTimeMillis()+2500;while(!done.getAsBoolean()&&System.currentTimeMillis()<end)Thread.sleep(5);yes(done.getAsBoolean(),"deadline");}
 static MediaMetadata meta(String title,String id){MediaMetadata m=new MediaMetadata();m.data.put("title",title);m.data.put("artist","Artist");m.data.put("album","Album");m.data.put("duration",180000L);m.data.put("id",id);return m;}
 static NowPlayingSnapshot song(String title){return new NowPlayingSnapshot(9,"deezer.android.app",title,"Artist",3,128,0,180000L,1,1,null,"","Album");}
 public static void main(String[] args)throws Exception {
  Context c=new Context();ShieldNowPlayingManager manager=ShieldNowPlayingManager.get(c);
  MediaController.Session p=new MediaController.Session();p.metadata=meta("Song","100");MediaController.sessions.put(9L,p);
  NowPlayingSnapshot a=song("Song");manager.state().update(a);
  DeezerFavouriteController controller=DeezerFavouriteController.get(c);
  DeezerFavouriteController.State[] left={null},right={null};
  Runnable end1=controller.subscribe(s->left[0]=s);waitFor(()->BoopDeezerHeartBackend.calls==1);
  yes(left[0].saved==-1&&left[0].pending,"unknown until native reply");
  for(int i=0;i<15;i++)p.actionsChanged();yes(BoopDeezerHeartBackend.calls==1,"no queries from progress events");
  yes("read".equals(BoopDeezerHeartBackend.input.get("operation")),"first request read-only");
  BoopDeezerHeartBackend.reply("OK",1);waitFor(()->left[0].saved==1&&!left[0].pending);
  Runnable end2=controller.subscribe(s->right[0]=s);
  yes(right[0].saved==1&&BoopDeezerHeartBackend.calls==1,"second screen shares same provider receipt");
  controller.change(a,left[0],null);waitFor(()->BoopDeezerHeartBackend.calls==2);
  yes("toggle".equals(BoopDeezerHeartBackend.input.get("operation")),"actual toggle");
  yes("1".equals(BoopDeezerHeartBackend.input.get("expected_saved")),"guards stale native state");
  yes(left[0].saved==1&&right[0].pending,"no optimistic fill change");
  BoopDeezerHeartBackend.reply("OK",0);waitFor(()->left[0].saved==0&&!left[0].pending);
  yes(right[0].saved==0,"both screens receive unsaved receipt");
  controller.dislike(a);waitFor(()->BoopDeezerHeartBackend.calls==3);
  yes("dislike".equals(BoopDeezerHeartBackend.input.get("operation")),"dislike separate from unfavourite");
  BoopDeezerHeartBackend.reply("DISLIKED",-1);waitFor(()->!left[0].pending);
  controller.change(a,left[0],null);waitFor(()->BoopDeezerHeartBackend.calls==4);
  p.metadata=meta("New Song","101");manager.state().update(song("New Song"));
  waitFor(()->BoopDeezerHeartBackend.cancels>0);waitFor(()->BoopDeezerHeartBackend.calls==5);
  yes(left[0].saved==-1,"old favourite never bleeds into new track");
  BoopDeezerHeartBackend.reply("OK",1);waitFor(()->left[0].saved==1&&!left[0].pending);
  end1.run();end2.run();yes(p.callbacks.isEmpty(),"idle detaches provider observers");
  System.out.println("Invisible controller: shared state, query coalescing, toggle, dislike and cancellation passed");
 }
}'''
    p=tmp_path/'com/boop/shieldhome/InvisibleControllerProbe.java';p.write_text(code,encoding='utf-8')
    source=ROOT/'unified/shield-home/src/main/java/com/boop/shieldhome'
    files=[source/(name+'.java') for name in ['DeezerFavouritePolicy','DeezerFavouriteRequest','DeezerFavouriteController','NowPlayingSnapshot','NowPlayingState','NowPlayingActionPolicy','NowPlayingSelectionPolicy']]
    subprocess.run(['javac','-encoding','UTF-8','-d',str(tmp_path),*map(str,files),*map(str,tmp_path.rglob('*.java'))],check=True)
    subprocess.run(['java','-cp',str(tmp_path),'com.boop.shieldhome.InvisibleControllerProbe'],check=True,timeout=15)

def test_album_one_heart_never_mistakes_shuffle_for_favourite_or_dislike(tmp_path):
    probe=tmp_path/'AlbumHeartProbe.java'
    probe.write_text('''package com.boop.bridge;
import java.lang.reflect.*;
public final class AlbumHeartProbe {
 static int n;
 static void yes(boolean b,String why){n++;if(!b)throw new AssertionError(why);}
 static int index(int[][] row,boolean dislike,String context)throws Exception {
  try{return (Integer)DeezerHeartRules.class.getMethod("heartIndex",int[][].class,int.class,boolean.class,String.class).invoke(null,row,422,dislike,context);}
  catch(NoSuchMethodException old){return DeezerHeartRules.heartIndex(row,422,dislike);}
 }
 public static void main(String[] args)throws Exception {
  int[][] album={{64,398,112,446},{488,398,536,446},{552,398,600,446},{616,398,664,446},{680,398,728,446},{744,398,792,446}};
  int[][] flow={{64,398,112,446},{120,398,168,446},{520,398,568,446},{584,398,632,446},{648,398,696,446},{712,398,760,446}};
  yes(index(album,false,"album_partner")==0,"The real album favourite is the first control, not Shuffle");
  yes(index(album,false,"playlist_partner")==0,"Recognised finite playlist layout");
  yes(index(album,true,"album_partner")==-1,"No native dislike in an album");
  yes(index(album,true,"playlist_partner")==-1,"Never substitute a playlist heart for dislike");
  yes(index(flow,false,"flow_partner")==1,"Flow keeps second favourite");
  yes(index(flow,true,"flow_partner")==0,"Flow keeps first dislike");
  yes(index(album,false,"flow_partner")==-1,"Mode and native geometry must agree");
  yes(index(flow,false,"album_partner")==-1,"Album never assumes two hearts");
  yes(index(album,false,"")==-1,"Unknown mode must not accept finite layout");
  yes(index(null,false,"album_partner")==-1,"Null layout fails closed");
  yes(index(java.util.Arrays.copyOf(album,5),false,"album_partner")==-1,"Missing controls fail closed");
  int[][] changed=album.clone();changed[1]=new int[]{120,398,168,446};
  yes(index(changed,false,"album_partner")==-1,"A second left control changes the layout");
  changed=album.clone();changed[3]=new int[]{616,438,664,486};
  yes(index(changed,false,"album_partner")==-1,"Misaligned controls rejected");
  changed=album.clone();changed[2]=new int[]{488,398,536,446};
  yes(index(changed,false,"album_partner")==-1,"Duplicate controls rejected");
  changed=album.clone();changed[0]=null;
  yes(index(changed,false,"album_partner")==-1,"Missing geometry rejected");
  System.out.println("Album native heart: "+n+" assertions passed");
 }
}''',encoding='utf-8')
    subprocess.run(['javac','-encoding','UTF-8','-d',str(tmp_path),str(SOURCE),str(probe)],check=True)
    subprocess.run(['java','-cp',str(tmp_path),'com.boop.bridge.AlbumHeartProbe'],check=True)

def test_native_heart_verifies_context_before_reusing_control_geometry():
    bridge=(ROOT/'unified/deezer-bridge/DeezerHeartBridge.java').read_text(encoding='utf-8')
    assert 'lyrics.centerY(),dislike,playerContextType)' in bridge
    assert 'playerContextId.equals(text(current.getMetadata(),CONTEXT_ID))' in bridge
    assert 'playerContextType.equals(text(current.getMetadata(),CONTEXT_TYPE))' in bridge
