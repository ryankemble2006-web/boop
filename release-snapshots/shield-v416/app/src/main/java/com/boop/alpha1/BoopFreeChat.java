package com.boop.alpha1;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.PersistableBundle;

import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;

import java.util.Collections;

/** A visible browser handoff, never a substitute API or a hidden WebView session. */
final class BoopFreeChat {
    private static final Uri CHAT_URL = Uri.parse("https://chatgpt.com/");
    private BoopFreeChat() { }

    static boolean open(Activity activity, String question) {
        try {
            String provider = CustomTabsClient.getPackageName(activity, Collections.emptyList());
            Intent intent;
            if (provider != null) {
                CustomTabsIntent tab = new CustomTabsIntent.Builder()
                        .setColorScheme(CustomTabsIntent.COLOR_SCHEME_DARK)
                        .setDefaultColorSchemeParams(new CustomTabColorSchemeParams.Builder()
                                .setToolbarColor(Color.BLACK)
                                .setNavigationBarColor(Color.BLACK)
                                .build())
                        .setShowTitle(false)
                        .setUrlBarHidingEnabled(true)
                        .build();
                intent = tab.intent.setPackage(provider).setData(CHAT_URL);
            } else {
                // Explicitly choose a browser, not an installed app claiming ChatGPT links.
                Intent probe = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER);
                ResolveInfo browser = activity.getPackageManager().resolveActivity(probe, PackageManager.MATCH_DEFAULT_ONLY);
                if (browser == null || browser.activityInfo == null) return false;
                intent = new Intent(Intent.ACTION_VIEW, CHAT_URL)
                        .addCategory(Intent.CATEGORY_BROWSABLE)
                        .setPackage(browser.activityInfo.packageName);
            }

            boolean copied = copyQuestion(activity, question);
            activity.startActivity(intent);
            BoopFreeChatNotice.show(activity, copied);
            return true;
        } catch (RuntimeException unavailable) {
            // Missing/disabled browsers and policy restrictions must not crash the puppet.
            return false;
        }
    }

    private static boolean copyQuestion(Context context, String question) {
        if (question == null || question.isBlank()) return false;
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard == null) return false;
            ClipData clip = ClipData.newPlainText("BOOP question", question);
            PersistableBundle extras = new PersistableBundle();
            extras.putBoolean("android.content.extra.IS_SENSITIVE", true);
            clip.getDescription().setExtras(extras);
            clipboard.setPrimaryClip(clip);
            return true;
        } catch (RuntimeException unavailable) {
            return false;
        }
    }
}
