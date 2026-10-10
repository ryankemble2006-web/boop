package local.boop.serensize;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.lang.reflect.*;
import java.util.*;
public final class SerenSizeProbe extends Activity {
  String result="NOT RUN";
  public void onCreate(Bundle state) {
    super.onCreate(state);
    try {
      Context installed=createPackageContext("com.boop.shieldoverlay",CONTEXT_INCLUDE_CODE|CONTEXT_IGNORE_SECURITY);
      Configuration cfg=new Configuration(installed.getResources().getConfiguration());cfg.densityDpi=256;
      Context layout=installed.createConfigurationContext(cfg);
      ClassLoader loader=installed.getClassLoader();
      Context isolated=new ContextWrapper(this) {
        public Resources getResources(){return layout.getResources();}
        public AssetManager getAssets(){return layout.getAssets();}
        public ClassLoader getClassLoader(){return loader;}
      };
      Class<?> type=loader.loadClass("com.boop.shieldhome.SerenNextUpView");
      Constructor<?> vc=type.getDeclaredConstructor(Context.class);vc.setAccessible(true);
      LinearLayout widget=(LinearLayout)vc.newInstance(isolated);
      Class<?> et=loader.loadClass("com.boop.shieldhome.SerenEpisode");
      Constructor<?> ec=et.getDeclaredConstructor(String.class,String.class,String.class,String.class);ec.setAccessible(true);
      Method bind=type.getDeclaredMethod("bind",List.class,String.class,loader.loadClass("com.boop.shieldhome.SerenPosterLoader"),java.util.function.Consumer.class,Runnable.class);bind.setAccessible(true);
      Field rf=type.getDeclaredField("row");rf.setAccessible(true);LinearLayout row=(LinearLayout)rf.get(widget);
      bind.invoke(widget,List.of(ec.newInstance("Initial","01x01","one","")),"Saved Next Up",null,null,null);
      int w=row.getChildAt(0).getLayoutParams().width,h=row.getChildAt(0).getLayoutParams().height;
      float density=layout.getResources().getDisplayMetrics().density;
      List<String> errors=new ArrayList<>();
      try {
        for(float d:new float[]{2.0f,1.6f,1.25f,2.0f}) {
          layout.getResources().getDisplayMetrics().density=d;
          bind.invoke(widget,List.of(ec.newInstance("Refreshed "+d,"01x02","two"+d,"")),"Saved Next Up",null,null,null);
          widget.measure(View.MeasureSpec.makeMeasureSpec(1800,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(400,View.MeasureSpec.AT_MOST));
          widget.layout(0,0,1800,widget.getMeasuredHeight());
          View tile=row.getChildAt(0);tile.requestFocus();tile.clearFocus();
          if(tile.getLayoutParams().width!=w||tile.getLayoutParams().height!=h)errors.add(d+":"+tile.getLayoutParams().width+"x"+tile.getLayoutParams().height);
        }
      } finally {layout.getResources().getDisplayMetrics().density=density;}
      result=(errors.isEmpty()?"PASS":"FAIL")+" refresh/layout/focus across density changes; baseline="+w+"x"+h+"; changed="+errors;
    } catch(Throwable e){result="ERROR "+e;}
  }
  public static final class ProbeInstrumentation extends Instrumentation {
    public void onCreate(Bundle b){super.onCreate(b);start();}
    public void onStart(){
      SerenSizeProbe a=(SerenSizeProbe)startActivitySync(new Intent(getTargetContext(),SerenSizeProbe.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      waitForIdleSync();Bundle result=new Bundle();result.putString("result",a.result);
      runOnMainSync(a::finish);finish(a.result.startsWith("PASS")?Activity.RESULT_OK:Activity.RESULT_CANCELED,result);
    }
  }
}
