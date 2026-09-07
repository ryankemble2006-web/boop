package com.boop.alpha1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Build;

final class BoopDockModeController implements SensorEventListener {
    interface Listener {
        void onWirelessDockChanged(boolean docked);
        void onPresenceNudge();
    }

    private final Context context;
    private final Listener listener;
    private final SensorManager sensorManager;
    private final Sensor proximity;
    private final BroadcastReceiver batteryReceiver;

    private boolean resumed;
    private boolean receiverRegistered;
    private boolean wirelessDocked;
    private boolean wasNear;

    BoopDockModeController(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        proximity = sensorManager == null ? null : sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ignored, Intent intent) {
                applyBattery(intent);
            }
        };
    }

    boolean isWirelessDocked() {
        return wirelessDocked;
    }

    void onResume() {
        if (resumed) {
            return;
        }
        resumed = true;
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent sticky;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            sticky = context.registerReceiver(
                    batteryReceiver,
                    filter,
                    Context.RECEIVER_NOT_EXPORTED);
        } else {
            sticky = context.registerReceiver(batteryReceiver, filter);
        }
        receiverRegistered = true;
        if (sticky != null) {
            applyBattery(sticky);
        } else {
            updateWirelessDocked(false);
        }
        syncProximity();
    }

    void onPause() {
        if (!resumed) {
            return;
        }
        resumed = false;
        if (receiverRegistered) {
            try {
                context.unregisterReceiver(batteryReceiver);
            } catch (IllegalArgumentException ignored) {
                // Already unregistered by Android during teardown.
            }
            receiverRegistered = false;
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        wasNear = false;
    }

    private void applyBattery(Intent intent) {
        int plugged = intent == null
                ? 0
                : intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        updateWirelessDocked(BoopDockPower.isWireless(plugged));
    }

    private void updateWirelessDocked(boolean docked) {
        if (wirelessDocked == docked) {
            return;
        }
        wirelessDocked = docked;
        wasNear = false;
        syncProximity();
        if (listener != null) {
            listener.onWirelessDockChanged(docked);
        }
    }

    private void syncProximity() {
        if (sensorManager == null || proximity == null) {
            return;
        }
        sensorManager.unregisterListener(this, proximity);
        if (resumed && wirelessDocked) {
            sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!resumed
                || !wirelessDocked
                || proximity == null
                || event == null
                || event.sensor != proximity
                || event.values == null
                || event.values.length == 0) {
            return;
        }
        boolean near = event.values[0] < proximity.getMaximumRange();
        if (near && !wasNear && listener != null) {
            listener.onPresenceNudge();
        }
        wasNear = near;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
