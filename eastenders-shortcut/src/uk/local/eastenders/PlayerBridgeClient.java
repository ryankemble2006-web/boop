package uk.local.eastenders;

import android.content.*;
import android.os.*;
import android.widget.Toast;

/** App-private bridge to this APK's local iPlayer cleanup service. */
final class PlayerBridgeClient {
    interface Callback { void done(boolean ok,String error); }
    private final Context context;
    private Messenger remote;
    private long session;
    private boolean bound,closed,connectedOnce;
    private final Runnable connectionLost;
    private Callback callback;
    private final Handler timer=new Handler(Looper.getMainLooper());
    private final Runnable timeout=() -> fail("SETUP_REQUIRED: Local debugging did not respond. Check Shield Network debugging and approve this shortcut.");
    private final Messenger replies=new Messenger(new Handler(Looper.getMainLooper()) {
        @Override public void handleMessage(Message message) {
            if(closed) return;
            if(message.what==10) { session=message.getData().getLong("session"); return; }
            if(message.what==13) {
                Toast.makeText(context,"Approve the Android debugging prompt for the EastEnders shortcut.",Toast.LENGTH_LONG).show();
                return;
            }
            if(message.what!=11 && message.what!=12) return;
            timer.removeCallbacks(timeout);
            PlayerBridgeClient.Callback result=callback;
            callback=null;
            if(result!=null) result.done(message.what==11,message.getData().getString("error","Cleanup failed"));
        }
    });
    private final ServiceConnection connection=new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name,IBinder binder) {
            if(closed) return;
            if(connectedOnce) { disconnect("iPlayer cleanup connection restarted; launch the shortcut again"); return; }
            connectedOnce=true;
            remote=new Messenger(binder);
            send(1);
        }
        @Override public void onServiceDisconnected(ComponentName name) { remote=null; disconnect("iPlayer cleanup disconnected"); }
        @Override public void onNullBinding(ComponentName name) { disconnect("iPlayer cleanup unavailable"); }
        @Override public void onBindingDied(ComponentName name) { disconnect("iPlayer cleanup changed; try again"); }
    };

    PlayerBridgeClient(Context context,Runnable connectionLost) {
        this.context=context.getApplicationContext();
        this.connectionLost=connectionLost;
    }

    void prepare(Callback result) {
        callback=result;
        timer.postDelayed(timeout,155000);
        try {
            Intent intent=new Intent().setComponent(new ComponentName("uk.local.eastenders","uk.local.eastenders.PlayerControlService"));
            bound=context.bindService(intent,connection,Context.BIND_AUTO_CREATE);
            if(!bound) fail("iPlayer cleanup helper is unavailable");
        } catch(RuntimeException rejected) { fail("iPlayer cleanup could not connect"); }
    }

    void stopPlayer(Callback result) {
        callback=result;
        timer.postDelayed(timeout,35000);
        if(remote==null || session==0) { fail("No authorised iPlayer cleanup session"); return; }
        send(2);
    }

    private void send(int operation) {
        try {
            Message message=Message.obtain(null,operation);
            Bundle data=new Bundle();
            data.putLong("session",session);
            message.setData(data);
            message.replyTo=replies;
            remote.send(message);
        } catch(Exception disconnected) { fail("iPlayer cleanup connection failed"); }
    }

    private void disconnect(String error) {
        if(closed) return;
        PlayerBridgeClient.Callback result=callback;
        close();
        if(result!=null) result.done(false,error); else connectionLost.run();
    }

    private void fail(String error) {
        timer.removeCallbacks(timeout);
        PlayerBridgeClient.Callback result=callback;
        callback=null;
        if(result!=null && !closed) result.done(false,error);
    }

    void close() {
        if(closed) return;
        closed=true;
        callback=null;
        timer.removeCallbacks(timeout);
        if(remote!=null) send(3);
        if(bound) { context.unbindService(connection); bound=false; }
        remote=null;
    }
}
