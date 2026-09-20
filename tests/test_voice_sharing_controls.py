"""Exercise the Voice screen binding at the Android UI and HA boundaries."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def test_shared_profile_updates_open_sliders_without_echo_or_retaining_closed_screen(tmp_path):
    source = ROOT / "source/BoopVoiceSharingControls.java"
    assert source.is_file(), "Voice Settings has no live sharing controls"
    stubs = {
        "android/content/Context.java": "package android.content; public class Context {public android.content.res.Resources getResources(){return new android.content.res.Resources();}}",
        "android/content/res/Resources.java": "package android.content.res; public class Resources {public android.util.DisplayMetrics getDisplayMetrics(){return new android.util.DisplayMetrics();}}",
        "android/util/DisplayMetrics.java": "package android.util; public class DisplayMetrics {public float density=1;}",
        "android/app/Activity.java": "package android.app; public class Activity extends android.content.Context {}",
        "android/app/AlertDialog.java": '''package android.app; public class AlertDialog {
 public static Runnable positive; public interface Click {void onClick(Object d,int w);}
 public static class Builder {public Builder(Activity a){}public Builder setTitle(String s){return this;}public Builder setMessage(String s){return this;}public Builder setNegativeButton(String s,Click c){return this;}public Builder setPositiveButton(String s,Click c){positive=()->c.onClick(null,0);return this;}public void show(){}}}''',
        "android/graphics/Color.java": "package android.graphics; public class Color {public static final int WHITE=-1,LTGRAY=-2;public static int rgb(int r,int g,int b){return r;}}",
        "android/view/Gravity.java": "package android.view; public class Gravity {public static final int CENTER=1;}",
        "android/view/View.java": '''package android.view; import java.util.*; public class View {
 public static final int VISIBLE=0,GONE=8;public int visibility;boolean attached;public CharSequence description;
 public interface OnClickListener {void onClick(View v);} OnClickListener click;
 public interface OnAttachStateChangeListener {void onViewAttachedToWindow(View v);void onViewDetachedFromWindow(View v);}
 public final List<OnAttachStateChangeListener> listeners=new ArrayList<>();
 public void setOnClickListener(OnClickListener c){click=c;}public void performClick(){click.onClick(this);}
 public void setContentDescription(CharSequence s){description=s;}public void setVisibility(int v){visibility=v;}
 public void setPadding(int a,int b,int c,int d){}public void setBackgroundColor(int c){}
 public void addOnAttachStateChangeListener(OnAttachStateChangeListener l){listeners.add(l);}public boolean isAttachedToWindow(){return attached;}
 public void attach(){attached=true;for(var l:listeners)l.onViewAttachedToWindow(this);}public void detach(){attached=false;for(var l:listeners)l.onViewDetachedFromWindow(this);}}''',
        "android/widget/TextView.java": '''package android.widget; public class TextView extends android.view.View {public CharSequence text="";public TextView(android.content.Context c){}public void setText(CharSequence s){text=s;}public CharSequence getText(){return text;}public void setTextColor(int c){}public void setTextSize(float f){}public void setGravity(int g){}}''',
        "android/widget/Button.java": "package android.widget; public class Button extends TextView {public Button(android.content.Context c){super(c);}}",
        "android/widget/LinearLayout.java": '''package android.widget; import java.util.*; public class LinearLayout extends android.view.View {public final List<android.view.View> children=new ArrayList<>();public LinearLayout(android.content.Context c){}public void addView(android.view.View v){children.add(v);}public void addView(android.view.View v,LayoutParams p){children.add(v);}public static class LayoutParams {public static final int MATCH_PARENT=-1,WRAP_CONTENT=-2;public LayoutParams(int w,int h){}public void setMargins(int a,int b,int c,int d){}}}''',
        "android/widget/SeekBar.java": '''package android.widget; public class SeekBar extends android.view.View {public int progress;public SeekBar(android.content.Context c){}public interface OnSeekBarChangeListener {void onProgressChanged(SeekBar s,int p,boolean user);void onStartTrackingTouch(SeekBar s);void onStopTrackingTouch(SeekBar s);}public OnSeekBarChangeListener listener;public void setOnSeekBarChangeListener(OnSeekBarChangeListener l){listener=l;}public void setProgress(int p){progress=p;if(listener!=null)listener.onProgressChanged(this,p,false);}public int getProgress(){return progress;}public void user(int p){progress=p;listener.onProgressChanged(this,p,true);}}''',
        "com/boop/alpha1/BoopVoiceController.java": '''package com.boop.alpha1; class BoopVoiceController {float pitch=1.45f,rate=1.25f;int writes;boolean usable=true;String name="Emma";
 float pitch(){return pitch;}float speechRate(){return rate;}void setPitch(float p){pitch=p;writes++;}void setSpeechRate(float r){rate=r;writes++;}boolean naturalBackendSelectedAndUsable(){return usable;}NaturalVoice selectedNaturalVoice(){return new NaturalVoice(name);}static class NaturalVoice {String n;NaturalVoice(String s){n=s;}String name(){return n;}}}''',
        "com/boop/alpha1/BoopSharedVoiceProfileRuntime.java": '''package com.boop.alpha1; import java.util.*;class BoopSharedVoiceProfileRuntime {static final BoopSharedVoiceProfileRuntime instance=new BoopSharedVoiceProfileRuntime();boolean enabled,ready;String status="Offline";final Set<Runnable> observers=new LinkedHashSet<>();static BoopSharedVoiceProfileRuntime get(android.content.Context c){return instance;}boolean enabled(){return enabled;}boolean ready(){return ready;}String status(){return status;}void setEnabled(boolean v){enabled=v;emit();}Runnable observe(Runnable r){observers.add(r);r.run();return ()->observers.remove(r);}void emit(){for(var r:new ArrayList<>(observers))r.run();}}''',
    }
    harness = '''package com.boop.alpha1;
import android.app.*;import android.widget.*;import android.view.*;
public class VoiceSharingControlsHarness {
 static void check(boolean v,String why){if(!v)throw new AssertionError(why);}
 static Button button(LinearLayout c,String prefix){return (Button)c.children.stream().filter(v->v instanceof Button && ((Button)v).text.toString().startsWith(prefix)).findFirst().orElseThrow();}
 static String labels(LinearLayout c){return c.children.stream().filter(v->v instanceof TextView && !(v instanceof Button)).map(v->((TextView)v).text.toString()).reduce("",(a,b)->a+"\\n"+b);}
 public static void main(String[] args){
  Activity a=new Activity();LinearLayout c=new LinearLayout(a);SeekBar pitch=new SeekBar(a),rate=new SeekBar(a);
  BoopVoiceController voice=new BoopVoiceController();var sharing=BoopSharedVoiceProfileRuntime.instance;
  TextView naturalStatus=new TextView(a);naturalStatus.setText("Downloaded. Pick a voice below.");
  BoopVoiceSharingControls.install(a,c,voice,pitch,rate,naturalStatus);
  check(sharing.observers.isEmpty(),"Unattached screen retained");c.attach();
  check(sharing.observers.size()==1,"Attached screen must observe once");
  check(pitch.progress==326 && rate.progress==306 && voice.writes==0,"Initial sliders or write echo");
  check(labels(c).contains("Emma"),"Selected voice missing");
  check(naturalStatus.text.toString().contains("Emma"),"Natural selection label stale on open");
  naturalStatus.setText("Trying Isabella...");sharing.emit();
  check(naturalStatus.text.toString().equals("Trying Isabella..."),"Unrelated event erased preview status");
  button(c,"Share voice profile").performClick();check(!sharing.enabled,"Sharing enabled before confirmation");
  AlertDialog.positive.run();check(sharing.enabled,"Confirmed sharing did not enable");
  sharing.ready=true;sharing.status="Connected";voice.pitch=.75f;voice.rate=.70f;voice.name="George";sharing.emit();
  check(pitch.progress==0 && rate.progress==0 && voice.writes==0,"Remote profile did not update silently");
  check(labels(c).contains("George") && labels(c).contains("Connected"),"Remote voice/status stale");
  check(naturalStatus.text.toString().contains("George"),"Remote voice left old Selected label behind");
  check(button(c,"Retry voice sharing").visibility==View.GONE,"Retry still visible when ready");
  pitch.listener.onStartTrackingTouch(pitch);pitch.user(500);sharing.emit();
  check(Math.abs(voice.pitch-1.825f)<.0001f && voice.writes==1,"Touch change not saved");
  voice.pitch=2.90f;sharing.emit();check(pitch.progress==500,"Remote event moved active drag");
  pitch.listener.onStopTrackingTouch(pitch);check(pitch.progress==1000,"Drag release did not refresh");
  rate.user(500);check(Math.abs(voice.rate-1.60f)<.0001f,"DPAD cadence change not saved");
  sharing.ready=false;sharing.emit();check(button(c,"Retry voice sharing").visibility==View.VISIBLE,"Offline retry missing");
  button(c,"Share voice profile").performClick();check(!sharing.enabled,"Disable failed");
  voice.usable=false;sharing.emit();check(labels(c).contains("Android"),"Unavailable natural voice misrepresented");
  c.detach();check(sharing.observers.isEmpty(),"Closed screen leaked observer");
  voice.pitch=.75f;sharing.emit();check(pitch.progress==1000,"Detached screen updated");
  c.attach();check(pitch.progress==0 && sharing.observers.size()==1,"Reopened screen stale or double observer");
  c.detach();System.out.println("voice sharing controls: passed");
 }
}'''
    for name, body in {**stubs, "com/boop/alpha1/VoiceSharingControlsHarness.java": harness}.items():
        target = tmp_path / name
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(body, encoding="utf-8")
    sources = [*tmp_path.rglob("*.java"), source, ROOT / "source/BoopVoiceTuning.java"]
    subprocess.run(["javac", "-encoding", "UTF-8", "-d", str(tmp_path), *map(str, sources)], check=True)
    subprocess.run(["java", "-cp", str(tmp_path), "com.boop.alpha1.VoiceSharingControlsHarness"], check=True)
