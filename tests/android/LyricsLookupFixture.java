package com.boop.shieldhome;

import android.app.Activity;
import android.os.Bundle;

/** Emulator-only UI fixture. No media session or playback commands. */
public final class LyricsLookupFixture extends Activity {
    private ShieldLyricsView view;
    private final NowPlayingSnapshot track=new NowPlayingSnapshot(7,"deezer.android.app",
            "Example Song (Remastered 2020)","Example Artist",3,~0L,42000L,218000L,1f,0L,null,"","Example Album");
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(5894);
        view=new ShieldLyricsView(this,0xff56cde8,new ShieldLyricsView.Controls(){
            public void previous(){} public void playPause(){} public void next(){} public void seek(long ms){}
            public void close(){finish();} public void browseAlbum(){} public void browseArtist(){}
            public void lookup(){edit();}
        });
        setContentView(view);view.setSnapshot(track,true);view.setQueueAvailable(true);
        view.setStatus("No lyrics for this track.");
        if(getIntent().getBooleanExtra("editor",false))view.post(this::edit);
    }
    private void edit(){
        new LyricsLookupDialog(this,track,"",query->{
            if(!track.title().equals("Example Song (Remastered 2020)"))throw new AssertionError("Original metadata changed");
            view.setStatus("Search: "+query.title()+" / "+query.subtitle());
            android.util.Log.i("LyricsLookupFixture","QUERY "+query.title()+" | "+query.subtitle());
        }).show();
    }
}
