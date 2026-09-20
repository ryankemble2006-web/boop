"""Flow replaces Close player without moving the accepted media controls."""
from pathlib import Path
import runpy
import subprocess

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome'


def test_flow_replaces_close_in_the_same_slot_and_keeps_close_media():
    view = (SRC / 'ShieldNowPlayingView.java').read_text(encoding='utf-8')
    callbacks = (SRC / 'ShieldHomeView.java').read_text(encoding='utf-8')
    activity = (SRC / 'ShieldLauncherActivity.java').read_text(encoding='utf-8')
    manager = (SRC / 'ShieldNowPlayingManager.java').read_text(encoding='utf-8')
    assert 'sourceButton = actionButton("Flow");' in view, 'Close player has not been replaced by Flow'
    block = view[view.index('sourceButton = actionButton('):view.index('titleRow.addView(sourceButton, sourceParams);')]
    assert 'callbacks.onNowPlayingFlow();' in block
    assert 'onCloseNowPlayingSource()' not in block
    assert 'Start your Deezer Flow' in block
    assert 'new LinearLayout.LayoutParams(dp(130), dp(44))' in block
    assert 'sourceParams.leftMargin = dp(12);' in block
    assert 'sourceParams.rightMargin = dp(8);' in block
    assert 'default void onNowPlayingFlow()' in callbacks
    assert '@Override public void onNowPlayingFlow()' in activity
    assert 'nowPlayingManager.playDeezerFlow()' in activity
    assert 'flowController.request(selectedController, state.current(),' in manager
    assert 'Looper.myLooper() != Looper.getMainLooper()' in manager
    assert 'public boolean closeMediaApps(Activity activity)' in manager
    assert 'onCloseMediaApps()' in activity
    for keep in ['controls.setTranslationX(-dp(4));', 'progressParams.rightMargin = dp(8);',
                 'private static final int CONTROL_GAP_DP = 10;', 'favouriteButton.setSnapshot(snapshot);']:
        assert keep in view


def test_flow_sends_only_the_real_native_uri_and_guards_repeated_or_wrong_players(tmp_path):
    source = SRC / 'DeezerFlowController.java'
    assert source.exists(), 'Native Flow control is not implemented yet'
    boundary = runpy.run_path(str(ROOT / 'tests/favourite_android_boundary.py'))
    stubs = dict(boundary['STUBS'])
    stubs['android/net/Uri.java'] = '''package android.net;
public final class Uri { private final String text; private Uri(String t){text=t;}
 public static Uri parse(String t){return new Uri(t);} public String toString(){return text;} }'''
    stubs['android/media/session/PlaybackState.java'] = stubs['android/media/session/PlaybackState.java'].replace(
        'public long actions;', 'public static final long ACTION_PLAY_FROM_URI=8192; public long actions;')
    text = stubs['android/media/session/MediaController.java']
    text = text.replace('public boolean throwing;', 'public boolean throwing; public String pkg="deezer.android.app"; public android.os.Bundle lastExtras;')
    text = text.replace('public MediaMetadata getMetadata()', 'public String getPackageName(){return s.pkg;} public MediaSession.Token getSessionToken(){return new MediaSession.Token(System.identityHashCode(s));}\n public MediaMetadata getMetadata()')
    text = text.replace('public final class TransportControls {', '''public final class TransportControls {
  public void playFromUri(android.net.Uri uri,android.os.Bundle extras){if(s.throwing)throw new IllegalStateException();s.commands.add(uri.toString());s.lastExtras=extras;}''')
    stubs['android/media/session/MediaController.java'] = text
    for name, content in stubs.items():
        target = tmp_path / name
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content, encoding='utf-8')
    harness = tmp_path / 'com/boop/shieldhome/FlowProbe.java'
    harness.write_text('''package com.boop.shieldhome;
import android.content.*;import android.media.session.*;
public final class FlowProbe {
 static int checks;
 static void yes(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
 static NowPlayingSnapshot snapshot(String pkg,int playback){return new NowPlayingSnapshot(9,pkg,"Song","Artist",playback,8192,0,180000,1,1,null,"","Album");}
 public static void main(String[] args){
  DeezerFlowController flow=new DeezerFlowController();
  MediaController.Session session=new MediaController.Session();session.playback.actions=8192;MediaController.sessions.put(9L,session);
  MediaController c=new MediaController(new Context(),new MediaSession.Token(9));
  NowPlayingSnapshot song=snapshot("deezer.android.app",3);
  yes(flow.request(null,song,0)==DeezerFlowController.Result.UNAVAILABLE,"null controller");
  yes(flow.request(c,null,0)==DeezerFlowController.Result.UNAVAILABLE,"no owner");
  yes(flow.request(c,snapshot("com.google.android.apps.mediashell",3),0)==DeezerFlowController.Result.UNAVAILABLE,"not Cast");
  session.pkg="com.google.android.youtube.tv";
  yes(flow.request(c,song,0)==DeezerFlowController.Result.UNAVAILABLE,"not another provider");session.pkg="deezer.android.app";
  session.playback.actions=512;
  yes(flow.request(c,song,0)==DeezerFlowController.Result.UNAVAILABLE,"requires fresh URI capability");
  session.playback.actions=8192;
  yes(flow.request(c,snapshot("deezer.android.app",0),0)==DeezerFlowController.Result.UNAVAILABLE,"reject no playback");
  yes(session.commands.isEmpty(),"no command from rejected requests");
  yes(flow.request(c,song,100)==DeezerFlowController.Result.REQUESTED,"native direct dispatch");
  yes(session.commands.size()==1&&"https://www.deezer.com/flow".equals(session.commands.get(0)),"exact existing Flow URI");
  yes(session.lastExtras!=null,"provider receives nonnull extras");
  yes(flow.request(c,song,101)==DeezerFlowController.Result.IGNORED,"debounce duplicate click");
  yes(session.commands.size()==1,"single dispatch");
  yes(flow.request(c,song,1300)==DeezerFlowController.Result.REQUESTED,"later deliberate request accepted");
  session.throwing=true;
  yes(flow.request(c,song,2600)==DeezerFlowController.Result.UNAVAILABLE,"provider exception is not success");session.throwing=false;
  yes(flow.request(c,song,2601)==DeezerFlowController.Result.REQUESTED,"failed dispatch does not consume retry");
  yes(flow.request(c,snapshot("deezer.android.app",2),3901)==DeezerFlowController.Result.REQUESTED,"start Flow while paused");
  MediaController.Session newer=new MediaController.Session();newer.playback.actions=8192;MediaController.sessions.put(10L,newer);
  yes(flow.request(new MediaController(new Context(),new MediaSession.Token(10)),song,3902)==DeezerFlowController.Result.REQUESTED,"new session not blocked by old debounce");
  session.playback=null;yes(flow.request(c,song,5000)==DeezerFlowController.Result.UNAVAILABLE,"destroyed state");
  System.out.println("Native Flow: "+checks+" behavioural assertions passed");
 }
}''', encoding='utf-8')
    production = [source, *[SRC / (name + '.java') for name in ['NowPlayingSnapshot', 'NowPlayingState', 'NowPlayingActionPolicy', 'NowPlayingSelectionPolicy']]]
    subprocess.run(['javac', '-encoding', 'UTF-8', '-d', str(tmp_path), *map(str, production), *map(str, tmp_path.rglob('*.java'))], check=True)
    subprocess.run(['java', '-cp', str(tmp_path), 'com.boop.shieldhome.FlowProbe'], check=True)
    body = source.read_text(encoding='utf-8')
    for forbidden in ['startActivity', 'skipToNext', 'force-stop', 'Runtime.getRuntime', 'screenrecord', 'UiAutomation']:
        assert forbidden not in body, forbidden