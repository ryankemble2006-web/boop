package com.boop.alpha1;

import android.app.Activity;
import android.graphics.Color;
import android.net.nsd.*;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

public final class NotificationSenderActivity extends Activity {
    private final Handler ui = new Handler(Looper.getMainLooper());
    private ExecutorService worker;
    private LinearLayout devices;
    private TextView status;
    private EditText pin, address;
    private NsdManager nsd;
    private NsdManager.DiscoveryListener discovery;
    private final Map<String, Button> found = new HashMap<>();
    private final Queue<NsdServiceInfo> resolveQueue = new ArrayDeque<>();
    private boolean resolving, active;
    private final BoopPreviewSendGate gate = new BoopPreviewSendGate();
    private String host, selectedName;
    private int port, session, sequence;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL); column.setPadding(dp(36), dp(18), dp(36), dp(24));
        column.setBackgroundColor(Color.rgb(18,18,18)); scroll.addView(column); setContentView(scroll);
        TextView title = text("BOOP Test Sender", 26); column.addView(title);
        column.addView(text("Open Notification Test on your phone, choose it below, then enter its code.", 16));
        devices = new LinearLayout(this); devices.setOrientation(LinearLayout.VERTICAL); column.addView(devices);
        pin = new EditText(this); pin.setHint("Six-digit phone code"); pin.setTextColor(Color.WHITE);
        pin.setSingleLine(true); pin.setInputType(InputType.TYPE_CLASS_NUMBER);
        pin.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(6)});
        pin.setContentDescription("Phone pairing code"); column.addView(pin);
        button(column,"Connect",()->send("PING",false,0));
        status = text("Looking for a phone on this network…",17); column.addView(status);
        LinearLayout actions = row(column);
        button(actions,"Run all (12 previews)",()->{
            if (!gate.paired()) { message("Connect to the phone first"); return; }
            if (gate.busy()) { message("Waiting for the phone…"); return; }
            int run = ++sequence; runNext(run,0);
        });
        button(actions,"Stop",()->{sequence++; if(gate.queueStopIfBusy())message("Stopping…");else send("STOP",false,0);});
        for(int i=0;i<BoopPreviewProtocol.SCENARIOS.size();i+=4) {
            LinearLayout row=row(column);
            for(int j=i;j<Math.min(i+4,BoopPreviewProtocol.SCENARIOS.size());j++) {
                final String command=BoopPreviewProtocol.SCENARIOS.get(j);
                button(row,pretty(command),()->{sequence++; send(command,false,0);});
            }
        }
        Button manual=button(column,"Phone not listed? Enter its address",()->{
            address.setVisibility(address.getVisibility()==View.VISIBLE ? View.GONE : View.VISIBLE);
            if(address.getVisibility()==View.VISIBLE) address.requestFocus();
        });
        address=new EditText(this); address.setHint("Phone address, e.g. 192.168.1.20:43210");
        address.setSingleLine(true); address.setTextColor(Color.WHITE); address.setVisibility(View.GONE);
        address.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        column.addView(address);
        column.addView(text("Synthetic previews only • each lasts 8 seconds • Run all ends after one pass",14));
    }
    @Override public void onStart() {
        super.onStart(); active=true; session++; gate.close();
        worker=Executors.newSingleThreadExecutor(); nsd=getSystemService(NsdManager.class);
        final int current=session;
        discovery=new NsdManager.DiscoveryListener() {
            public void onDiscoveryStarted(String type) { }
            public void onDiscoveryStopped(String type) { }
            public void onStartDiscoveryFailed(String type,int error) { ui.post(()->{if(active&&session==current)message("Discovery unavailable. Enter the address shown on the phone.");}); }
            public void onStopDiscoveryFailed(String type,int error) { }
            public void onServiceFound(NsdServiceInfo service) { ui.post(()->{if(active&&session==current){resolveQueue.add(service);resolveNext(current);}}); }
            public void onServiceLost(NsdServiceInfo service) { ui.post(()->{
                if(!active||session!=current)return;
                Button old=found.remove(service.getServiceName()); if(old!=null)devices.removeView(old);
                if(service.getServiceName().equals(selectedName)){gate.select();sequence++;message("Phone test closed. Reopen it to reconnect.");}
            }); }
        };
        try { nsd.discoverServices(BoopPreviewProtocol.SERVICE_TYPE,NsdManager.PROTOCOL_DNS_SD,discovery); }
        catch(RuntimeException e){message("Enter the address shown on the phone.");}
    }
    private void resolveNext(int current) {
        if(resolving||resolveQueue.isEmpty()||!active||current!=session)return;
        resolving=true;
        nsd.resolveService(resolveQueue.remove(),new NsdManager.ResolveListener(){
            public void onResolveFailed(NsdServiceInfo info,int error){ui.post(()->{if(current==session){resolving=false;resolveNext(current);}});}
            public void onServiceResolved(NsdServiceInfo info){ui.post(()->{
                if(!active||current!=session)return;
                resolving=false;
                if(info.getHost()!=null && !found.containsKey(info.getServiceName())) {
                    Button b=button(devices,info.getServiceName(),()->{
                        sequence++;gate.select();host=info.getHost().getHostAddress();port=info.getPort();
                        selectedName=info.getServiceName();address.setText("");
                        message("Selected "+selectedName+". Enter its code and Connect.");pin.requestFocus();
                    }); found.put(info.getServiceName(),b);
                }
                resolveNext(current);
            });}
        });
    }
    private void runNext(int run,int index) {
        if(!active||run!=sequence)return;
        if(index>=BoopPreviewProtocol.SCENARIOS.size()){message("All 12 previews finished");return;}
        send(BoopPreviewProtocol.SCENARIOS.get(index),true,index);
    }
    private void send(String command,boolean all,int index) {
        if(!active)return;
        if(gate.busy()){message("Waiting for the phone…");return;}
        if("PING".equals(command)) {
            sequence++; gate.select();
            String manual=address.getText().toString().trim();
            if(!manual.isEmpty()) {
                try { int split=manual.lastIndexOf(':'); host=manual.substring(0,split);port=Integer.parseInt(manual.substring(split+1));
                    if(port<1||port>65535||!host.matches("[0-9.]+"))throw new IllegalArgumentException(); selectedName="Phone";
                } catch(Exception bad){message("Use the complete address shown on the phone");return;}
            }
        } else if(!gate.paired()){message("Connect to the phone first");return;}
        final String code=pin.getText().toString();
        if(host==null||port<1||!BoopPreviewProtocol.validPin(code)){message("Choose a phone and enter its six-digit code");return;}
        final String target=host; final int targetPort=port,current=session,run=sequence;
        final BoopPreviewSendGate.Ticket ticket=gate.begin(command);
        message("Sending "+pretty(command)+"…");
        worker.execute(()->{
            String result;
            try { result=BoopPreviewClient.send(target,targetPort,code,command); }
            catch(Exception failure){result="ERROR connection";}
            final String response=result;
            ui.post(()->{
                if(!active||current!=session)return;
                if(!gate.finish(ticket,response.equals("OK "+command)))return;
                if(!response.equals("OK "+command)) {
                    sequence++;
                    message(response.equals("ERROR auth")?"Code changed or incorrect. Check the phone.":"Phone did not confirm. Check its test screen and reconnect.");return;
                }
                if("PING".equals(command)){message("Connected to "+selectedName);}
                else message("STOP".equals(command)?"Stopped":"Phone accepted "+pretty(command));
                if(gate.takeStop()){send("STOP",false,0);return;}
                if(all&&run==sequence)ui.postDelayed(()->runNext(run,index+1),9000);
            });
        });
    }
    private String pretty(String value){return value.equals("X")?"X / Twitter":value.substring(0,1)+value.substring(1).toLowerCase(Locale.ROOT);}
    private void message(String value){status.setText(value);}
    private TextView text(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(Color.WHITE);t.setPadding(0,dp(4),0,dp(4));return t;}
    private LinearLayout row(LinearLayout parent){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.HORIZONTAL);parent.addView(r);return r;}
    private Button button(LinearLayout parent,String label,Runnable action){
        Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(16);b.setFocusable(true);
        b.setOnClickListener(v->action.run());
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(parent.getOrientation()==LinearLayout.HORIZONTAL?0:-1,dp(56),parent.getOrientation()==LinearLayout.HORIZONTAL?1:0);
        p.setMargins(dp(3),dp(3),dp(3),dp(3));parent.addView(b,p);return b;
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    @Override public void onStop(){
        active=false;session++;sequence++;gate.close();ui.removeCallbacksAndMessages(null);
        if(discovery!=null){try{nsd.stopServiceDiscovery(discovery);}catch(RuntimeException ignored){}discovery=null;}
        if(worker!=null)worker.shutdownNow();
        found.clear();devices.removeAllViews();resolveQueue.clear();resolving=false;
        super.onStop();
    }
}
