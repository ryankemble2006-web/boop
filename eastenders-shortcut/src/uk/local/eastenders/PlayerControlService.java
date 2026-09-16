package uk.local.eastenders;
import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Shared serial controller. Trusted callers can only reset the official Shield iPlayer. */
public final class PlayerControlService extends Service {
    private final BridgeSession sessions=new BridgeSession();
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private IBinder ownerReply;
    private volatile AdbWire active;
    private volatile boolean destroyed;
    private final Messenger incoming=new Messenger(new Handler(Looper.getMainLooper()) {
        @Override public void handleMessage(Message message) {
            final int uid=message.sendingUid;
            final Messenger reply=message.replyTo;
            if(reply==null || !BridgeTrust.callerTrusted(PlayerControlService.this,uid)) return;
            int op=message.what;
            long supplied=message.getData().getLong("session");
            if(op==1) {
                long id=sessions.begin(uid); ownerReply=reply.getBinder();
                respond(reply,10,id,"");
                worker.execute(() -> reset(uid,id,reply,true));
            } else if(reply.getBinder().equals(ownerReply) && op==2 && sessions.current(uid,supplied)) {
                worker.execute(() -> reset(uid,supplied,reply,false));
            } else if(reply.getBinder().equals(ownerReply) && op==3) {
                // Zero only occurs when this exact client's cancellation beats its ACK.
                if(supplied==0) sessions.begin(-1); else sessions.cancel(uid,supplied);
            } else if(op==2) respond(reply,12,supplied,"A newer programme already owns iPlayer");
        }
    });
    @Override public IBinder onBind(Intent intent) { return incoming.getBinder(); }
    private void reset(int uid,long id,Messenger reply,boolean allowApproval) {
        if(destroyed || !sessions.current(uid,id)) return;
        try(AdbWire adb=new AdbWire()) {
            active=adb;
            File key=new File(getNoBackupFilesDir(),"iplayer-local-adb.key");
            if(!allowApproval && !key.isFile()) throw new IOException("Open a shortcut to authorise cleanup first");
            adb.connect(5555,AdbWire.identity(key),allowApproval ? 45000 : 5000,
                    () -> respond(reply,13,id,""),allowApproval);
            if(!sessions.current(uid,id) || destroyed) return;
            String shellUid=checked(adb,"id -u").trim();
            if(!"2000".equals(shellUid) && !"0".equals(shellUid)) throw new IOException("Cleanup requires an authorised local debugging connection");
            if(!sessions.current(uid,id) || destroyed) return;
            PlayerReset.reset(command -> checked(adb,command));
            Log.i("IPlayerCleanup","Verified iPlayer stopped, including background processes");
            if(sessions.current(uid,id) && !destroyed) respond(reply,11,id,"");
        } catch(Exception failure) {
            if(sessions.current(uid,id) && !destroyed) {
                Log.w("IPlayerCleanup","Cleanup failed: "+failure.getClass().getSimpleName());
                respond(reply,12,id,"Could not cleanly stop iPlayer. Check Shield network debugging and accept its approval prompt.");
            }
        } finally { active=null; }
    }
    private String checked(AdbWire adb,String command) throws Exception {
        AdbWire.Result result=adb.execute(command,8000);
        if(result.exitCode!=0) throw new IOException("Local cleanup command failed");
        return result.output;
    }
    private void respond(Messenger reply,int what,long session,String error) {
        try {
            Message response=Message.obtain(null,what);
            Bundle data=new Bundle(); data.putLong("session",session); data.putString("error",error); response.setData(data);
            reply.send(response);
        } catch(RemoteException gone) { }
    }
    @Override public void onDestroy() {
        destroyed=true; worker.shutdownNow();
        try { if(active!=null) active.close(); } catch(Exception ignored) { }
        super.onDestroy();
    }
}
