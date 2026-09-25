package local.boop.notificationfixture;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

/** Disposable offline emulator source of real Android notifications, never a BOOP preview. */
public final class NotificationFixture extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        showOpenedNotification();
    }

    @Override public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        showOpenedNotification();
    }

    private void showOpenedNotification() {
        TextView label = new TextView(this);
        label.setText("Opened fixture notification " + getIntent().getIntExtra("id", 0));
        label.setTextSize(26); label.setTextColor(Color.WHITE); label.setBackgroundColor(Color.BLACK);
        setContentView(label);
    }

    public static final class Commands extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            int id = intent.getIntExtra("id", 1);
            String action = intent.getStringExtra("command");
            if ("clear".equals(action)) { manager.cancelAll(); return; }
            if ("remove".equals(action)) { manager.cancel(id); return; }
            String channel = intent.getStringExtra("channel");
            if (channel == null) channel = "messages";
            NotificationChannel category = new NotificationChannel(channel, channel, NotificationManager.IMPORTANCE_DEFAULT);
            category.setSound(null, null); category.enableVibration(false);
            manager.createNotificationChannel(category);
            PendingIntent open = PendingIntent.getActivity(context, id,
                    new Intent(context, NotificationFixture.class).putExtra("id", id),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            String body = intent.getStringExtra("body");
            Notification.Builder builder = new Notification.Builder(context, channel)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Pocket fixture " + id)
                    .setContentText(body == null ? "Private synthetic message" : body)
                    .setContentIntent(open).setAutoCancel(true)
                    .setOnlyAlertOnce(intent.getBooleanExtra("only_once", false))
                    .setOngoing(intent.getBooleanExtra("ongoing", false));
            if (intent.getBooleanExtra("group", false)) builder.setGroup("test-bundle")
                    .setGroupSummary(intent.getBooleanExtra("summary", false));
            manager.notify(id, builder.build());
        }
    }
}
