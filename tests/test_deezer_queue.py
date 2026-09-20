"""Native queue behaviour and integration. No Deezer login or UI polling in tests."""
from pathlib import Path
import subprocess, runpy
ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/'unified/shield-home/src/main/java/com/boop/shieldhome'

def test_queue_state_selection_and_flow_exclusion(tmp_path):
    source=SRC/'DeezerQueueController.java'
    assert source.exists(), 'The native queue controller is not implemented'
    stubs=dict(runpy.run_path(str(ROOT/'tests/favourite_android_boundary.py'))['STUBS'])
    stubs['android/media/MediaDescription.java']='''package android.media;
public final class MediaDescription {
 public final String title,artist,description;
 public MediaDescription(String title,String artist,String desc){this.title=title;this.artist=artist;description=desc;}
 public CharSequence getTitle(){return title;} public CharSequence getSubtitle(){return artist;}
 public CharSequence getDescription(){return description;} public String getMediaId(){return null;}
}'''
    stubs['android/media/session/MediaSession.java']=stubs['android/media/session/MediaSession.java'].replace('public static final class Token','''public static final class QueueItem {
 public static final long UNKNOWN_ID=-1;final long id;final android.media.MediaDescription desc;
 public QueueItem(android.media.MediaDescription d,long id){this.id=id;desc=d;}
 public long getQueueId(){return id;} public android.media.MediaDescription getDescription(){return desc;}
 } public static final class Token''')
    p=stubs['android/media/session/PlaybackState.java']
    p=p.replace('public long actions;', 'public static final long ACTION_SKIP_TO_QUEUE_ITEM=4096; public int state=3;public long active;public int getState(){return state;} public long getActiveQueueItemId(){return active;} public long actions;')
    stubs['android/media/session/PlaybackState.java']=p
    c=stubs['android/media/session/MediaController.java']
    c=c.replace('public MediaMetadata metadata;', 'public String pkg="deezer.android.app";public int queueReads;public java.util.List<MediaSession.QueueItem> queue=new java.util.ArrayList<>();public final java.util.List<Long> skips=new java.util.ArrayList<>();public MediaMetadata metadata;')
    c=c.replace('private final Session s;', 'private final Session s;private MediaSession.Token token;')
    c=c.replace('s=sessions.get(token.id);', 'this.token=token;s=sessions.get(token.id);')
    c=c.replace('public MediaMetadata getMetadata()', 'public MediaSession.Token getSessionToken(){return token;} public String getPackageName(){return s.pkg;} public CharSequence getQueueTitle(){return null;} public java.util.List<MediaSession.QueueItem> getQueue(){s.queueReads++;return s.queue;} public MediaMetadata getMetadata()')
    c=c.replace('public void setRating(Rating rating)', 'public void skipToQueueItem(long id){if(s.throwing)throw new IllegalStateException();s.skips.add(id);} public void setRating(Rating rating)')
    stubs['android/media/session/MediaController.java']=c
    for name,text in stubs.items():
        path=tmp_path/name;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(text,encoding='utf-8')
    # The inherited manager double has unrelated snapshot dependencies; this test uses the real queue controller directly.
    (tmp_path/'com/boop/shieldhome/ShieldNowPlayingManager.java').unlink()
    harness=tmp_path/'com/boop/shieldhome/QueueProbe.java'
    harness.write_text(PROBE,encoding='utf-8')
    subprocess.run(['javac','-encoding','UTF-8','-d',str(tmp_path),str(source),str(SRC/'NowPlayingSelectionPolicy.java'),*map(str,tmp_path.rglob('*.java'))],check=True)
    subprocess.run(['java','-cp',str(tmp_path),'com.boop.shieldhome.QueueProbe'],check=True,timeout=15)

PROBE='''package com.boop.shieldhome;
import android.content.Context;import android.media.*;import android.media.session.*;import android.os.*;import java.util.*;
public final class QueueProbe {
 static int checks;
 static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 static MediaSession.QueueItem item(long id,String title){return new MediaSession.QueueItem(new MediaDescription(title,"Artist","Album"),id);}
 static MediaMetadata meta(String mode,String id,String title){MediaMetadata m=new MediaMetadata();m.data.put(DeezerQueueController.CONTEXT_TYPE,mode);m.data.put(DeezerQueueController.CONTEXT_ID,id);m.data.put("title",title);m.data.put("artist","Artist");m.data.put("album","Album");return m;}
 public static void main(String[] args){
  MediaController.Session p=new MediaController.Session();p.metadata=meta("album_partner","42","One");p.queue=Arrays.asList(item(0,"One"),item(1,"Two"),item(2,"Three"));p.playback.active=0;
  MediaController.sessions.put(9L,p);MediaController player=new MediaController(new Context(),new MediaSession.Token(9));
  long[] version={301000101L};DeezerQueueController q=new DeezerQueueController(()->version[0]);int[] events={0};Runnable stop=q.subscribe(s->events[0]++);
  q.update(player,9);DeezerQueueController.State s=q.current();
  check(s.visible&&s.canSelect&&s.rows.size()==3,"real album queue visible");check(s.activeId==0,"current item");
  check(s.rows.get(2).id==2&&s.rows.get(2).title.equals("Three"),"native rows preserved");
  int reads=p.queueReads,notices=events[0];for(int i=0;i<30;i++)q.update(player,9);
  check(p.queueReads==reads,"progress callbacks do not reread queue");check(events[0]==notices,"no redundant UI notification");
  check(q.select(s,s.rows.get(1))==DeezerQueueController.Result.REQUESTED,"pinned native queue handler accepted");
  check(p.skips.equals(Arrays.asList(1L)),"numeric native queue ID dispatched");check(q.current().activeId==0&&q.current().pending,"no optimistic selection");
  check(q.select(q.current(),s.rows.get(2))==DeezerQueueController.Result.BUSY,"duplicate pending rejected");
  p.playback.active=1;q.update(player,9);check(q.current().pending,"queue ID alone cannot confirm wrong metadata");
  p.metadata=meta("album_partner","42","Two");q.update(player,9);check(!q.current().pending&&q.current().activeId==1,"coherent provider receipt");
  s=q.current();check(q.select(s,s.rows.get(1))==DeezerQueueController.Result.CURRENT,"current row no restart");check(p.skips.size()==1,"no second native request");
  q.suppressForFlow();check(!q.current().visible,"Flow request hides immediately");q.update(player,9);check(!q.current().visible,"stale album metadata cannot unhide");
  p.metadata=meta("flow_partner","flow","Two");q.update(player,9);check(!q.current().visible&&q.current().rows.isEmpty(),"Flow history excluded");
  p.metadata=meta("radio_partner","radio","Two");q.update(player,9);check(!q.current().visible,"radio excluded");
  p.metadata=meta("","","Two");q.update(player,9);check(!q.current().visible,"missing mode not guessed from length");
  p.metadata=meta("album_partner","43","New album track");q.update(player,9);check(!q.current().visible,"new metadata old queue race hidden");
  p.queue=Arrays.asList(item(0,"First"),item(1,"New album track"),item(2,"Third"));q.queueChanged(player,9,p.queue);check(q.current().visible,"coherent new album visible");
  check(q.select(s,s.rows.get(2))==DeezerQueueController.Result.STALE,"prior album row rejected");
  s=q.current();p.queue=Arrays.asList(item(0,"First"),item(1,"New album track"),item(2,"Reordered"));
  check(q.select(s,s.rows.get(2))==DeezerQueueController.Result.STALE,"unannounced queue change rechecked before command");
  p.queue=Arrays.asList(item(0,"First"),item(1,"New album track"),item(1,"Duplicate"));q.queueChanged(player,9,p.queue);check(!q.current().visible,"duplicate IDs fail closed");
  p.queue=Arrays.asList(item(0,"First"),item(1,"New album track"),item(2,"Third"));q.queueChanged(player,9,p.queue);
  version[0]=301000102L;s=q.current();check(q.select(s,s.rows.get(2))==DeezerQueueController.Result.UNAVAILABLE,"updated provider cannot use old unadvertised assumption");
  p.playback.actions=4096;q.update(player,9);s=q.current();check(q.select(s,s.rows.get(2))==DeezerQueueController.Result.REQUESTED,"advertised queue jump allowed");
  Handler.advance(4000);check(!q.current().pending&&q.current().activeId==1,"timeout does not mark new track playing");check(!q.current().message.isEmpty(),"timeout visible to queue panel");
  p.throwing=true;s=q.current();check(q.select(s,s.rows.get(2))==DeezerQueueController.Result.UNAVAILABLE,"exception not treated as success");check(!q.current().pending,"exception clears pending");p.throwing=false;
  p.metadata=meta("playlist_partner","55","New album track");q.update(player,9);check(q.current().visible,"playlist is eligible");
  p.pkg="com.google.android.apps.mediashell";q.update(player,9);check(!q.current().visible,"Cast never treated as native");p.pkg="deezer.android.app";
  q.update(player,9);s=q.current();q.update(player,10);check(q.select(s,s.rows.get(2))==DeezerQueueController.Result.STALE,"new session rejects old view");
  q.clear();check(!q.current().visible&&!q.current().pending,"access revoke clears queue");
  int count=events[0];stop.run();q.update(player,9);check(events[0]==count,"unsubscribed screen not retained");
  System.out.println("Native queue: "+checks+" assertions passed");
 }
}'''

def test_queue_entry_dialog_and_callbacks_preserve_existing_controls():
    view=(SRC/'ShieldNowPlayingView.java').read_text(encoding='utf-8')
    assert 'queueButton = actionButton("Queue")' in view, 'Queue entry not present'
    assert 'onNowPlayingQueue()' in view
    assert view.index('titleRow.addView(lyricsButton') < view.index('titleRow.addView(queueButton') < view.index('titleRow.addView(sourceButton')
    manager=(SRC/'ShieldNowPlayingManager.java').read_text(encoding='utf-8')
    assert 'onQueueChanged' in manager and 'queueController.queueChanged' in manager
    assert 'queueController.suppressForFlow()' in manager
    assert 'queueController.clear()' in manager
    dialog=(SRC/'ShieldQueueDialog.java').read_text(encoding='utf-8')
    assert 'FocusChrome.accentColor' in dialog
    assert 'setBackgroundDrawable' in dialog and 'Color.rgb(24, 24, 24)' in dialog
    assert 'onStop()' in dialog and 'unsubscribe.run()' in dialog
    assert 'controller.select(clicked.boundState, clicked.boundRow)' in dialog
    assert 'startActivity' not in dialog and 'requestAudioFocus' not in dialog

def test_queue_rows_allow_font_scaling_and_use_portable_labels():
    dialog=(SRC/'ShieldQueueDialog.java').read_text(encoding='utf-8')
    assert 'row.setMinimumHeight(dp(72))' in dialog, 'Fixed queue row clips scaled artist text'
    assert 'new android.widget.AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)' in dialog
    assert all(ord(c)<128 for c in dialog), 'Static Queue labels must be portable across source-writing encodings'
