package com.boop.alpha1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;
import com.boop.eyes.PuppetPreferences;
import java.util.Locale;

/** The same touch/D-pad controls on phone, tablet and TV. No second puppet renderer. */
public final class BoopPuppetSettingsActivity extends Activity {
    private LinearLayout content;
    private TextView status,hueLabel,pitchLabel,rateLabel;
    private SeekBar hue,pitch,rate;
    private Switch share;
    private Runnable unwatch;
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(Color.BLACK);
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(24),dp(16),dp(24),dp(24));
        scroll.addView(content);setContentView(scroll);
        text("BOOP puppet controls",30);
        text("One character across your BOOP devices",17);
        text("Eye colour",23);hueLabel=text("",18);
        hue=slider(360,(value)->PuppetPreferences.hue(this,value));
        LinearLayout colours=row();
        for(int value:new int[]{190,285,120,25,0})addButton(colours,value==190?"Blue":value==285?"Purple":value==120?"Green":value==25?"Orange":"Red",()->PuppetPreferences.hue(this,value));
        text("Animation speed",23);LinearLayout speeds=row();
        for(float value:new float[]{0,.5f,1,1.5f,2})addButton(speeds,value==0?"Still":String.format(Locale.ROOT,"%.1fx",value),()->PuppetPreferences.speed(this,value));
        text("Voice character",23);text("Centre is normal. Move further for deeper, higher, slower or faster puppet voices.",17);
        pitchLabel=text("",18);pitch=slider(1000,value->PuppetPreferences.pitch(this,BoopVoiceTuning.pitchFromProgress(value)));
        rateLabel=text("",18);rate=slider(1000,value->PuppetPreferences.rate(this,BoopVoiceTuning.rateFromProgress(value)));
        addButton(content,"Normal voice",()->{PuppetPreferences.pitch(this,1);PuppetPreferences.rate(this,1);});
        addButton(content,"Choose or preview a voice",()->openMain("com.boop.alpha1.OPEN_VOICE_SETTINGS"));
        share=new Switch(this);share.setTextColor(Color.WHITE);share.setTextSize(19);
        share.setText("Share eyes and voice on this home network");share.setChecked(PuppetPreferences.syncEnabled(this));
        share.setOnCheckedChangeListener((button,on)->{
            PuppetPreferences.syncEnabled(this,on);
            if(on)BoopPuppetSyncService.startFromActivity(this);else stopService(new Intent(this,BoopPuppetSyncService.class));
        });content.addView(share);
        text("Use sharing only on a trusted home network. No account details or notification content are shared.",16);
        status=text("",16);
        addButton(content,"Notification setup",()->startActivity(new Intent(this,BoopNotificationSettingsActivity.class)));
        addButton(content,"Animation demos",()->openMain("com.boop.alpha1.OPEN_DEVELOPER_MENU"));
        addButton(content,"Done",this::finish);
        unwatch=PuppetPreferences.watch(this,this::refresh);refresh();
    }
    @Override protected void onResume(){super.onResume();BoopPuppetSyncService.startFromActivity(this);if(status!=null)refresh();}
    @Override protected void onDestroy(){if(unwatch!=null)unwatch.run();super.onDestroy();}
    private void refresh() {
        int colour=PuppetPreferences.hue(this);float p=PuppetPreferences.pitch(this),r=PuppetPreferences.rate(this);
        hue.setProgress(colour);pitch.setProgress(BoopVoiceTuning.progressFromPitch(p));rate.setProgress(BoopVoiceTuning.progressFromRate(r));
        hueLabel.setText("Iris colour: "+colour+" degrees");
        pitchLabel.setText(String.format(Locale.ROOT,"Pitch: %.2fx",p));rateLabel.setText(String.format(Locale.ROOT,"Voice speed: %.2fx",r));
        boolean enabled=PuppetPreferences.syncEnabled(this);share.setChecked(enabled);
        status.setText(enabled?PuppetPreferences.prefs(this,"boop_puppet").getString("sync_status","Starting home-network sharing"):"Sharing off. This device keeps its current character.");
    }
    private void openMain(String action) {
        startActivity(new Intent(this,MainActivity.class).setAction(action).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }
    private interface ProgressAction { void set(int progress); }
    private SeekBar slider(int max,ProgressAction action) {
        SeekBar bar=new SeekBar(this);bar.setMax(max);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar b,int progress,boolean user){if(user)action.set(progress);}
            public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}
        });content.addView(bar,new LinearLayout.LayoutParams(-1,dp(52)));return bar;
    }
    private LinearLayout row() {
        HorizontalScrollView scroll=new HorizontalScrollView(this);LinearLayout row=new LinearLayout(this);
        scroll.addView(row);content.addView(scroll);return row;
    }
    private TextView text(String value,int size) {
        TextView label=new TextView(this);label.setText(value);label.setTextColor(Color.WHITE);label.setTextSize(size);
        label.setPadding(0,dp(10),0,dp(6));content.addView(label);return label;
    }
    private void addButton(LinearLayout parent,String label,Runnable action) {
        Button b=new Button(this);b.setText(label);b.setTextSize(18);b.setFocusable(true);b.setOnClickListener(v->action.run());
        parent.addView(b,new LinearLayout.LayoutParams(parent==content?-1:-2,dp(56)));
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
}
