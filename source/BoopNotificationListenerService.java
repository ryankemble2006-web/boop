package com.boop.alpha1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.ArrayList;
import java.util.List;

public final class BoopNotificationListenerService extends NotificationListenerService {
    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        BoopNotificationRuntime runtime = runtimeOrNull();
        if (runtime == null) {
            return;
        }
        runtime.attachListener(this);
        rebuildActiveNotifications();
    }

    @Override
    public void onListenerDisconnected() {
        BoopNotificationRuntime runtime = runtimeOrNull();
        if (runtime != null) {
            runtime.detachListener(this);
        }
        super.onListenerDisconnected();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        handlePosted(sbn, rankingMap);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        handlePosted(sbn, getCurrentRanking());
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        BoopNotificationRuntime runtime = runtimeOrNull();
        if (runtime != null && sbn != null) {
            runtime.remove(sbn.getKey());
        }
    }

    void rebuildActiveNotifications() {
        BoopNotificationRuntime runtime = runtimeOrNull();
        if (runtime == null) return;

        StatusBarNotification[] active;
        try {
            active = getActiveNotifications();
        } catch (SecurityException failure) {
            runtime.rebuild(List.of());
            return;
        }

        RankingMap rankingMap = getCurrentRanking();
        List<BoopNotificationRuntime.RuntimeRecord> records = new ArrayList<>();
        if (active != null) {
            for (StatusBarNotification sbn : active) {
                Prepared prepared = prepareMetadata(sbn, rankingMap, runtime);
                if (prepared == null
                        || prepared.mode != BoopNotificationIntakePolicy.Mode.READ_RICH_CONTENT) {
                    continue;
                }
                BoopNotificationRuntime.RuntimeRecord record = readRichRecord(prepared);
                if (record != null) records.add(record);
            }
        }
        runtime.rebuild(records);
    }

    private void handlePosted(StatusBarNotification sbn, RankingMap rankingMap) {
        BoopNotificationRuntime runtime = runtimeOrNull();
        if (runtime == null) return;

        Prepared prepared = prepareMetadata(sbn, rankingMap, runtime);
        if (prepared == null
                || prepared.mode != BoopNotificationIntakePolicy.Mode.READ_RICH_CONTENT) {
            return;
        }
        BoopNotificationRuntime.RuntimeRecord record = readRichRecord(prepared);
        if (record != null) {
            runtime.post(record, System.currentTimeMillis());
        }
    }

    private Prepared prepareMetadata(
            StatusBarNotification sbn,
            RankingMap rankingMap,
            BoopNotificationRuntime runtime) {
        if (sbn == null || sbn.getNotification() == null) return null;

        String packageName = safe(sbn.getPackageName());
        String key = safe(sbn.getKey());
        Notification notification = sbn.getNotification();
        NotificationChannel channel = rankingChannel(key, rankingMap);
        String channelId = channel != null
                ? safe(channel.getId())
                : safe(notification.getChannelId());
        if (channelId.isEmpty()) {
            channelId = "default";
        }

        BoopNotificationChannelInfo channelInfo;
        if (channel != null) {
            CharSequence name = channel.getName();
            channelInfo = new BoopNotificationChannelInfo(
                    packageName,
                    channelId,
                    name == null ? channelId : name.toString(),
                    true,
                    channel.getSound() != null,
                    channel.shouldVibrate(),
                    System.currentTimeMillis());
        } else {
            channelInfo = new BoopNotificationChannelInfo(
                    packageName,
                    channelId,
                    channelId,
                    false,
                    false,
                    false,
                    System.currentTimeMillis());
        }

        runtime.observeChannel(channelInfo);
        BoopNotificationIntakePolicy.Mode mode = BoopNotificationIntakePolicy.decide(
                runtime.settings(), packageName, channelId);
        return new Prepared(sbn, notification, channelInfo, packageName, key, channelId, mode);
    }

    private BoopNotificationRuntime.RuntimeRecord readRichRecord(Prepared prepared) {
        Notification notification = prepared.notification;

        // Privacy boundary: notification extras are not inspected until policy allows this package+channel.
        Bundle extras = notification.extras;
        String title = null;
        String text = null;
        if (extras != null) {
            CharSequence titleValue = extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence textValue = extras.getCharSequence(Notification.EXTRA_TEXT);
            title = titleValue == null ? null : titleValue.toString();
            text = textValue == null ? null : textValue.toString();
        }

        String appLabel = appLabel(prepared.packageName);
        boolean autoCancel = (notification.flags & Notification.FLAG_AUTO_CANCEL) != 0;
        BoopNotificationEnvelope envelope = new BoopNotificationEnvelope(
                prepared.key,
                prepared.packageName,
                appLabel,
                prepared.channelId,
                prepared.channelInfo.channelName(),
                title,
                text,
                prepared.sbn.getPostTime(),
                autoCancel);
        PendingIntent intent = notification.contentIntent;
        return new BoopNotificationRuntime.RuntimeRecord(envelope, intent, prepared.channelInfo);
    }

    private NotificationChannel rankingChannel(String key, RankingMap rankingMap) {
        if (rankingMap == null || key == null || key.isEmpty()) return null;
        Ranking ranking = new Ranking();
        if (!rankingMap.getRanking(key, ranking)) return null;
        return ranking.getChannel();
    }

    private String appLabel(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            CharSequence label = pm.getApplicationLabel(info);
            return label == null ? packageName : label.toString();
        } catch (PackageManager.NameNotFoundException failure) {
            return packageName;
        }
    }

    private BoopNotificationRuntime runtimeOrNull() {
        if (!BoopNotificationRuntime.shouldInitializeForMode(BoopDeviceProfile.resolve(this))) {
            return null;
        }
        try {
            return BoopNotificationRuntime.get(this);
        } catch (IllegalStateException failure) {
            return null;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static final class Prepared {
        final StatusBarNotification sbn;
        final Notification notification;
        final BoopNotificationChannelInfo channelInfo;
        final String packageName;
        final String key;
        final String channelId;
        final BoopNotificationIntakePolicy.Mode mode;

        Prepared(
                StatusBarNotification sbn,
                Notification notification,
                BoopNotificationChannelInfo channelInfo,
                String packageName,
                String key,
                String channelId,
                BoopNotificationIntakePolicy.Mode mode) {
            this.sbn = sbn;
            this.notification = notification;
            this.channelInfo = channelInfo;
            this.packageName = packageName;
            this.key = key;
            this.channelId = channelId;
            this.mode = mode;
        }
    }
}
