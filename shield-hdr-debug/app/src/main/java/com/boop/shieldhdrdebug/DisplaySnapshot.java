package com.boop.shieldhdrdebug;

import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.util.Locale;

final class DisplaySnapshot {
    final String device;
    final String display;
    final String mode;
    final String window;
    final String hdr;
    final String luminance;
    final String supportedModes;

    private DisplaySnapshot(String device, String display, String mode, String window,
                            String hdr, String luminance, String supportedModes) {
        this.device = device;
        this.display = display;
        this.mode = mode;
        this.window = window;
        this.hdr = hdr;
        this.luminance = luminance;
        this.supportedModes = supportedModes;
    }

    static DisplaySnapshot capture(WindowManager windowManager) {
        Display display = windowManager.getDefaultDisplay();
        Display.Mode currentMode = display.getMode();
        Display.HdrCapabilities capabilities = display.getHdrCapabilities();

        int windowWidth;
        int windowHeight;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Rect bounds = windowManager.getCurrentWindowMetrics().getBounds();
            windowWidth = bounds.width();
            windowHeight = bounds.height();
        } else {
            DisplayMetrics metrics = new DisplayMetrics();
            display.getRealMetrics(metrics);
            windowWidth = metrics.widthPixels;
            windowHeight = metrics.heightPixels;
        }

        StringBuilder modes = new StringBuilder();
        Display.Mode[] supported = display.getSupportedModes();
        for (int i = 0; i < supported.length; i++) {
            if (i > 0) modes.append(' ');
            Display.Mode item = supported[i];
            modes.append(item.getModeId()).append(':')
                    .append(item.getPhysicalWidth()).append('x').append(item.getPhysicalHeight())
                    .append('@').append(String.format(Locale.US, "%.2f", item.getRefreshRate()));
            if (modes.length() > 150) {
                modes.append(" …");
                break;
            }
        }

        String deviceLine = Build.MANUFACTURER + " " + Build.MODEL
                + " Android " + Build.VERSION.RELEASE + " API " + Build.VERSION.SDK_INT;
        String displayLine = "id=" + display.getDisplayId()
                + " name=" + display.getName()
                + " state=" + display.getState()
                + " rot=" + display.getRotation()
                + " colorMode=" + display.getColorMode();
        String modeLine = "modeId=" + currentMode.getModeId()
                + " " + currentMode.getPhysicalWidth() + "x" + currentMode.getPhysicalHeight()
                + " @" + String.format(Locale.US, "%.3fHz", currentMode.getRefreshRate());
        String windowLine = "window=" + windowWidth + "x" + windowHeight;
        String hdrLine = "HDR=" + hdrTypes(capabilities.getSupportedHdrTypes());
        String luminanceLine = String.format(Locale.US, "lum max=%.1f avg=%.1f min=%.4f",
                capabilities.getDesiredMaxLuminance(),
                capabilities.getDesiredMaxAverageLuminance(),
                capabilities.getDesiredMinLuminance());

        return new DisplaySnapshot(deviceLine, displayLine, modeLine, windowLine,
                hdrLine, luminanceLine, "modes " + modes);
    }

    String signature() {
        return display + " | " + mode + " | " + window + " | " + hdr;
    }

    String compact() {
        return mode + " " + window + " " + hdr;
    }

    private static String hdrTypes(int[] types) {
        if (types == null || types.length == 0) {
            return "none";
        }
        StringBuilder out = new StringBuilder();
        for (int type : types) {
            if (out.length() > 0) out.append(',');
            switch (type) {
                case Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION:
                    out.append("DV");
                    break;
                case Display.HdrCapabilities.HDR_TYPE_HDR10:
                    out.append("HDR10");
                    break;
                case Display.HdrCapabilities.HDR_TYPE_HLG:
                    out.append("HLG");
                    break;
                case Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS:
                    out.append("HDR10+");
                    break;
                default:
                    out.append(type);
            }
        }
        return out.toString();
    }
}
