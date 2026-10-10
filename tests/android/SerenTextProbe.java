package local.boop.serentext;
import android.app.*;import android.content.*;import android.content.res.*;import android.os.*;
import android.view.*;import android.widget.*;import java.lang.reflect.*;import java.util.*;
public final class SerenTextProbe extends Activity {
  LinearLayout widget,row;TextView detail;Method bind;Constructor<?> episode;
  List<Object> entries;Throwable error;
  public void onCreate(Bundle state){super.onCreate(state);
    try {
      Context installed=createPackageContext("com.boop.shieldoverlay",CONTEXT_INCLUDE_CODE|CONTEXT_IGNORE_SECURITY);
      Configuration cfg=new Configuration(installed.getResources().getConfiguration());cfg.densityDpi=256;
      Context layout=installed.createConfigurationContext(cfg);ClassLoader loader=installed.getClassLoader();
      Context isolated=new ContextWrapper(this){public Resources getResources(){return layout.getResources();}public AssetManager getAssets(){return layout.getAssets();}public ClassLoader getClassLoader(){return loader;}};
      Class<?> type=loader.loadClass("com.boop.shieldhome.SerenNextUpView");Constructor<?> ctor=type.getDeclaredConstructor(Context.class);ctor.setAccessible(true);
      widget=(LinearLayout)ctor.newInstance(isolated);
      Class<?> et=loader.loadClass("com.boop.shieldhome.SerenEpisode");episode=et.getDeclaredConstructor(String.class,String.class,String.class,String.class,String.class);episode.setAccessible(true);
      String plot="A long episode description keeps moving across every refresh. ".repeat(24)+" END_MARKER";
      entries=List.of(episode.newInstance("Long show","01x02 Long episode","one","",plot),episode.newInstance("Other show","01x01","two","","Other episode"));
      bind=type.getDeclaredMethod("bind",List.class,String.class,loader.loadClass("com.boop.shieldhome.SerenPosterLoader"),java.util.function.Consumer.class,Runnable.class);bind.setAccessible(true);
      bind.invoke(widget,entries,"Saved Next Up",null,null,null);
      Field f=type.getDeclaredField("row");f.setAccessible(true);row=(LinearLayout)f.get(widget);
      f=type.getDeclaredField("detail");f.setAccessible(true);detail=(TextView)f.get(widget);
      setContentView(widget);row.getChildAt(0).requestFocus();
    }catch(Throwable e){error=e;}
  }
  static long elapsed(TextView view)throws Exception{Field f=view.getClass().getDeclaredField("progress");f.setAccessible(true);Object state=f.get(view);f=state.getClass().getDeclaredField("elapsedMs");f.setAccessible(true);return f.getLong(state);}
  public static final class ProbeInstrumentation extends Instrumentation {
    public void onCreate(Bundle b){super.onCreate(b);start();}
    public void onStart(){String result;SerenTextProbe a=null;
      try {
        a=(SerenTextProbe)startActivitySync(new Intent(getTargetContext(),SerenTextProbe.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        SystemClock.sleep(1600);runOnMainSync(()->{});if(a.error!=null)throw new AssertionError(a.error);
        if(!a.detail.getText().toString().endsWith("END_MARKER"))throw new AssertionError("description was cut off before its ending; displayed characters="+a.detail.length());
        SerenTextProbe activity=a;long[] before=new long[1];
        runOnMainSync(()->{try{
          before[0]=elapsed(activity.detail);
          if(before[0]<500)throw new AssertionError("animation did not advance");
          List<Object> refreshed=List.of(activity.entries.get(0),activity.episode.newInstance("Changed other show","02x01","two-new","","New metadata"));
          activity.bind.invoke(activity.widget,refreshed,"Saved Next Up",null,null,null);
        }catch(Exception e){throw new RuntimeException(e);}});
        SystemClock.sleep(250);runOnMainSync(()->{});long after=elapsed(a.detail);
        if(after<before[0])throw new AssertionError("refresh restarted description: "+before[0]+" -> "+after);
        if(!a.detail.isSelected())throw new AssertionError("same-episode focus was not restored");
        result="PASS full description="+a.detail.length()+" characters; refresh preserved timeline="+before[0]+" -> "+after+"ms; focus retained";

      }catch(Throwable e){result="FAIL "+e;}
      Bundle b=new Bundle();b.putString("result",result);if(a!=null){SerenTextProbe activity=a;runOnMainSync(activity::finish);}finish(result.startsWith("PASS")?Activity.RESULT_OK:Activity.RESULT_CANCELED,b);
    }
  }
}

