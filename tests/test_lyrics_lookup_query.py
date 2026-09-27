from pathlib import Path
import subprocess
import tempfile

ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/'unified/shield-home/src/main/java/com/boop/shieldhome'

def test_search_edits_preserve_playback_and_do_not_mutate_original_metadata():
    stubs={
        'android/graphics/Bitmap.java':'package android.graphics; public class Bitmap {}',
        'android/media/session/PlaybackState.java':'''package android.media.session; public class PlaybackState {
            public static final int STATE_PLAYING=3,STATE_PAUSED=2;
            public static final long ACTION_SKIP_TO_PREVIOUS=1,ACTION_REWIND=2,ACTION_PAUSE=4,ACTION_PLAY_PAUSE=8,ACTION_PLAY=16,ACTION_FAST_FORWARD=32,ACTION_SKIP_TO_NEXT=64,ACTION_SEEK_TO=128;
        }''',
        'com/boop/shieldhome/LookupQueryCheck.java':'''package com.boop.shieldhome;
        public class LookupQueryCheck {
          static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
          public static void main(String[] args)throws Exception{
            NowPlayingSnapshot original=new NowPlayingSnapshot(7,"deezer.android.app","Song (Remastered 2020)","Artist",3,255,42000,218000,1f,1000,null,"cast","Album");
            Class<?> type;try{type=Class.forName("com.boop.shieldhome.LyricsLookupQuery");}catch(ClassNotFoundException missing){throw new AssertionError("Editable lyrics query is missing",missing);}
            java.lang.reflect.Method edit=type.getDeclaredMethod("edit",NowPlayingSnapshot.class,String.class,String.class);
            NowPlayingSnapshot query=(NowPlayingSnapshot)edit.invoke(null,original," Song "," Correct Artist ");
            check(query.title().equals("Song")&&query.subtitle().equals("Correct Artist"),"trimmed edits reach query");
            check(original.title().equals("Song (Remastered 2020)")&&original.subtitle().equals("Artist"),"original metadata remains untouched");
            check(query.sessionId()==7&&query.durationMs()==218000&&query.positionMs()==42000&&query.actions()==255&&query.playbackState()==3&&query.updateTimeMs()==1000&&query.playbackSpeed()==1f,"playback identity and timing preserved");
            check(query.album().isEmpty(),"original album must not constrain a corrected query");
            for(String invalid:new String[]{"", "   ",new String(new char[257]).replace('\\0','a')}){
              try{edit.invoke(null,original,invalid,"Artist");throw new AssertionError("invalid title accepted");}
              catch(java.lang.reflect.InvocationTargetException rejected){check(rejected.getCause() instanceof IllegalArgumentException,"input rejection");}
            }
            System.out.println("PASS edited lookup metadata and playback preservation");
          }
        }'''
    }
    with tempfile.TemporaryDirectory() as folder:
        files=[]
        for name,body in stubs.items():
            p=Path(folder)/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(body);files.append(str(p))
        files += [str(SRC/n) for n in ('NowPlayingSnapshot.java','NowPlayingActionPolicy.java')]
        if (SRC/'LyricsLookupQuery.java').exists():files.append(str(SRC/'LyricsLookupQuery.java'))
        subprocess.run(['javac','-d',folder,*files],check=True)
        subprocess.run(['java','-cp',folder,'com.boop.shieldhome.LookupQueryCheck'],check=True)
