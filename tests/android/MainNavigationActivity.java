package deezer.android.app.navigation.ui;
public final class MainNavigationActivity extends android.app.Activity {
 public void onCreate(android.os.Bundle b){super.onCreate(b);android.widget.TextView text=new android.widget.TextView(this);text.setText("Hidden player fixture");setContentView(text);startForegroundService(new android.content.Intent().setClassName(this,"deezer.android.app.data.mediasession.service.DeezerSdkMediaService"));}
}
