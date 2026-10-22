package com.boop.shieldhome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TvAppRepository {
    private final Context context;

    public TvAppRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public List<TvAppEntry> load() {
        List<TvAppEntry> leanback = query(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<TvAppEntry> launcher = query(Intent.CATEGORY_LAUNCHER);
        return merge(context.getPackageName(), leanback, launcher);
    }

    private List<TvAppEntry> query(String category) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(category);
        List<ResolveInfo> resolved = pm.queryIntentActivities(intent, 0);
        ArrayList<TvAppEntry> out = new ArrayList<>();
        for (ResolveInfo info : resolved) {
            ActivityInfo activity = info.activityInfo;
            if (activity == null || activity.packageName == null || activity.name == null) {
                continue;
            }
            ComponentName component = new ComponentName(activity.packageName, activity.name);
            CharSequence label = info.loadLabel(pm);
            out.add(new TvAppEntry(
                    component.flattenToString(),
                    activity.packageName,
                    label == null ? activity.packageName : label.toString()));
        }
        return out;
    }

    public static List<TvAppEntry> merge(
            String ownPackage,
            List<TvAppEntry> leanback,
            List<TvAppEntry> launcher) {
        Map<String, TvAppEntry> deduped = new LinkedHashMap<>();
        append(deduped, ownPackage, leanback);
        append(deduped, ownPackage, launcher);

        ArrayList<TvAppEntry> out = new ArrayList<>(deduped.values());
        out.sort(Comparator
                .comparing(TvAppEntry::label, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(TvAppEntry::component, String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    private static void append(
            Map<String, TvAppEntry> out,
            String ownPackage,
            List<TvAppEntry> entries) {
        if (entries == null) {
            return;
        }
        for (TvAppEntry entry : entries) {
            if (entry == null
                    || entry.component().isEmpty()
                    || entry.packageName().equals(ownPackage)) {
                continue;
            }
            out.putIfAbsent(entry.component(), entry);
        }
    }
}
