package com.boop.shieldhome;
import android.content.*;import android.media.*;import android.media.session.*;import android.os.*;import com.boop.alpha1.BoopDeezerHeartBackend;
public final class HomeHeartProbe {
 static DeezerFavouriteController.State state;
 static void yes(boolean b,String why){if(!b)throw new AssertionError(why);}
 static void waitFor(java.util.function.BooleanSupplier done)throws Exception{long end=System.currentTimeMillis()+2000;while(!done.getAsBoolean()&&System.currentTimeMillis()<end)Thread.sleep(5);yes(done.getAsBoolean(),"deadline");}
 static MediaMetadata meta(String title){MediaMetadata m=new MediaMetadata();m.data.put("title",title);m.data.put("artist","Artist");m.data.put("album","Album");m.data.put("duration",180000L);m.data.put("id",title);return m;}
 static NowPlayingSnapshot song(String title){return new NowPlayingSnapshot(9,"deezer.android.app",title,"Artist",3,128,0,180000L,1,1,null,"","Album");}
 static void reply(String status,int saved)throws Exception{BoopDeezerHeartBackend.reply(status,saved);waitFor(()->!state.pending);Thread.sleep(20);}
 public static void main(String[] args)throws Exception {
  Context c=new Context();ShieldNowPlayingManager manager=ShieldNowPlayingManager.get(c);MediaController.Session p=new MediaController.Session();p.metadata=meta("Song");MediaController.sessions.put(9L,p);NowPlayingSnapshot song=song("Song");manager.state().update(song);
  DeezerFavouriteController controller=DeezerFavouriteController.get(c);Runnable end=controller.subscribe(s->state=s);waitFor(()->BoopDeezerHeartBackend.calls==1);
  switch(args[0]) {
   case "unknown":
    reply("UNAVAILABLE",-1);DeezerFavouriteController.State unknown=state;
    controller.change(song,unknown,null);waitFor(()->BoopDeezerHeartBackend.calls==2);
    yes("read".equals(BoopDeezerHeartBackend.input.get("operation")),"Unknown outline must read, never remove a saved track");
    reply("OK",1);controller.change(song,unknown,null);Thread.sleep(30);
    yes(BoopDeezerHeartBackend.calls==2&&state.saved==1,"An old unknown button cannot toggle a newly resolved heart");break;
   case "handoff":
    reply("OK",1);end.run();end=controller.subscribe(s->state=s);waitFor(()->BoopDeezerHeartBackend.calls==2);
    yes(state.saved==1,"Home rebuild must retain the confirmed filled heart while refreshing");
    reply("UNAVAILABLE",-1);yes(state.saved==1,"A read failure must not erase a confirmed heart for the same recording");break;
   case "retry":
    for(int calls=1;calls<=3;calls++) {final int n=calls;waitFor(()->BoopDeezerHeartBackend.calls==n);reply("UNAVAILABLE",-1);Handler.advance(1000);}
    Thread.sleep(30);yes(BoopDeezerHeartBackend.calls==3,"Automatic read retries are bounded");
    for(int i=0;i<20;i++)p.actionsChanged();Handler.advance(10000);yes(BoopDeezerHeartBackend.calls==3,"Playback ticks cannot loop failed reads");break;
   case "stale":
    reply("OK",0);DeezerFavouriteController.State old=state;controller.change(song,state,null);waitFor(()->BoopDeezerHeartBackend.calls==2);reply("OK",1);
    controller.change(song,old,null);Thread.sleep(30);yes(BoopDeezerHeartBackend.calls==2&&state.saved==1,"An old empty heart cannot remove a now-saved track");break;
   case "inflight":
    BoopDeezerHeartBackend.holdCancellation=true;end.run();end=controller.subscribe(s->state=s);
    BoopDeezerHeartBackend.reply("OK",0);waitFor(()->BoopDeezerHeartBackend.calls==2);
    yes(state.saved==-1&&state.pending,"Cancelled read cannot populate rebuilt Home");reply("OK",1);yes(state.saved==1,"Rebuilt Home revalidates after cancelled worker exits");break;
   case "timeout":
    reply("OK",1);end.run();end=controller.subscribe(s->state=s);waitFor(()->BoopDeezerHeartBackend.calls==2);Handler.advance(30000);Thread.sleep(30);
    yes(state.saved==1,"Read watchdog must preserve the last confirmed heart");break;
   case "track":
    reply("UNAVAILABLE",-1);p.metadata=meta("Next");manager.state().update(song("Next"));waitFor(()->BoopDeezerHeartBackend.calls==2);reply("OK",1);Handler.advance(1000);Thread.sleep(30);
    yes(BoopDeezerHeartBackend.calls==2&&state.saved==1,"Old-track retry cannot run on a new track");break;
  }
  end.run();Handler.advance(10000);System.out.println("PASS Home heart "+args[0]);System.exit(0);
 }
}
