package com.boop.shieldhome;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Standalone Shield launcher surface. */
public final class ShieldLauncherActivity extends Activity {
    public static final long PAGE_TRANSITION_MS = 140L;
    private static final String SETUP_PREFS = "boop_shield_home_setup_v1";
    private static final String KEY_HOME_PROMPT_SHOWN = "home_prompt_shown";

    public enum FavouriteEdit {
        MOVE_LEFT,
        MOVE_RIGHT,
        REMOVE
    }

    private enum Page {
        HOME,
        APPS,
        SETTINGS
    }

    private TvAppRepository repository;
    private ShieldHomeStore store;
    private ExecutorService executor;
    private FrameLayout root;
    private View currentView;

    private List<TvAppEntry> installedApps = List.of();
    private List<String> favouriteComponents = List.of();
    private Page currentPage = Page.HOME;

    private BroadcastReceiver packageReceiver;
    private boolean receiverRegistered;
    private boolean destroyed;
    private int optionalGeneration;

    private final BackPressGesture backPressGesture = new BackPressGesture();
    private Handler inputHandler;
    private Runnable backHoldRunnable;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);

        inputHandler = new Handler(Looper.getMainLooper());
        backHoldRunnable = () -> {
            if (destroyed || !backPressGesture.onHoldTriggered()) {
                return;
            }
            if (currentView instanceof ShieldHomeView) {
                ((ShieldHomeView) currentView).resetToFirstFavourite();
            }
            openSystemSettings();
        };

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        setContentView(root, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        repository = new TvAppRepository(this);
        store = new ShieldHomeStore(this);
        executor = Executors.newSingleThreadExecutor();

        registerPackageReceiver();
        showHome();
        reloadApps();
        root.post(this::maybePromptForHomeRole);
    }

    private void reloadApps() {
        if (destroyed || executor == null || executor.isShutdown()) {
            return;
        }
        executor.execute(() -> {
            List<TvAppEntry> apps;
            List<String> favourites;
            try {
                apps = repository.load();
                List<String> saved = store.loadOrSeedFavourites(apps);
                favourites = FavouriteOrder.reconcile(saved, apps);
                if (!favourites.equals(saved)) {
                    store.saveFavourites(favourites);
                }
            } catch (RuntimeException ignored) {
                return;
            }

            List<TvAppEntry> loadedApps = List.copyOf(apps);
            List<String> loadedFavourites = List.copyOf(favourites);
            runOnUiThread(() -> {
                if (destroyed) {
                    return;
                }
                installedApps = loadedApps;
                favouriteComponents = loadedFavourites;
                showCurrentPage();
            });
        });
    }

    private void showCurrentPage() {
        switch (currentPage) {
            case APPS:
                showApps();
                break;
            case SETTINGS:
                showSettings();
                break;
            case HOME:
            default:
                showHome();
                break;
        }
    }

    private void showHome() {
        showHome(false);
    }

    private void showHome(boolean focusFirstFavourite) {
        currentPage = Page.HOME;
        int generation = ++optionalGeneration;
        List<TvAppEntry> favourites = favouriteEntries();

        ShieldHomeView view = new ShieldHomeView(this);
        ShieldHomeView.Callbacks callbacks = homeCallbacks();
        view.render(favourites, List.of(), callbacks);
        transitionTo(view);
        if (focusFirstFavourite) {
            view.post(view::resetToFirstFavourite);
        }

        List<HomeRowProvider> providers;
        try {
            providers = OptionalRowRegistry.loadEnabled(
                    store::rowEnabled,
                    TvProviderRows.factory(this));
        } catch (RuntimeException ignored) {
            return;
        }
        if (providers.isEmpty() || executor == null || executor.isShutdown()) {
            return;
        }

        executor.execute(() -> {
            ArrayList<HomeRow> rows = new ArrayList<>();
            for (HomeRowProvider provider : providers) {
                if (provider == null) {
                    continue;
                }
                try {
                    List<HomeRow> loaded = provider.load();
                    if (loaded == null) {
                        continue;
                    }
                    for (HomeRow row : loaded) {
                        if (row != null && row.cards() != null && !row.cards().isEmpty()) {
                            rows.add(row);
                        }
                    }
                } catch (RuntimeException ignored) {
                    // Optional content is fail-closed. Favourite apps remain usable.
                }
            }

            List<HomeRow> readyRows = List.copyOf(rows);
            runOnUiThread(() -> {
                if (destroyed
                        || currentPage != Page.HOME
                        || generation != optionalGeneration
                        || currentView != view) {
                    return;
                }
                view.render(favouriteEntries(), readyRows, homeCallbacks());
                if (focusFirstFavourite) {
                    view.post(view::resetToFirstFavourite);
                }
            });
        });
    }

    private ShieldHomeView.Callbacks homeCallbacks() {
        return new ShieldHomeView.Callbacks() {
            @Override public void onAppSelected(TvAppEntry entry) {
                launchApp(entry);
            }

            @Override public void onFavouriteOrderCommitted(List<String> components) {
                List<String> stable = components == null ? List.of() : List.copyOf(components);
                if (!stable.equals(favouriteComponents)) {
                    saveFavouriteEdit(stable);
                    showHome();
                }
            }

            @Override public void onOpenApps() {
                showApps();
            }

            @Override public void onOpenHomeRows() {
                showSettings();
            }

            @Override public void onOpenSystemSettings() {
                openSystemSettings();
            }

            @Override public void onContentSelected(HomeContentCard card) {
                launchContent(card);
            }
        };
    }

    private void showApps() {
        currentPage = Page.APPS;
        ++optionalGeneration;

        ShieldAppsView view = new ShieldAppsView(this);
        view.render(installedApps, new HashSet<>(favouriteComponents), new ShieldAppsView.Callbacks() {
            @Override public void onAppSelected(TvAppEntry entry) {
                launchApp(entry);
            }

            @Override public void onToggleFavourite(TvAppEntry entry) {
                toggleFavourite(entry);
            }
        });
        transitionTo(view);
    }

    private void showSettings() {
        currentPage = Page.SETTINGS;
        ++optionalGeneration;

        boolean playNext = store.rowEnabled(OptionalRowRegistry.Key.PLAY_NEXT);
        boolean appChannels = store.rowEnabled(OptionalRowRegistry.Key.APP_CHANNELS);
        ShieldHomeSettingsView view = new ShieldHomeSettingsView(this);
        view.render(playNext, appChannels, new ShieldHomeSettingsView.Callbacks() {
            @Override public void onSetRowEnabled(OptionalRowRegistry.Key key, boolean enabled) {
                store.setRowEnabled(key, enabled);
                showSettings();
            }

            @Override public void onChooseHomeApp() {
                openHomeSettings();
            }

            @Override public void onMakeBoopHome() {
                requestHomeRole();
            }

            @Override public void onRetireStockHome() {
                openStockHomeAppInfo();
            }

            @Override public void onRestoreStockHome() {
                restoreStockHome();
            }

            @Override public void onBackHome() {
                showHome();
            }
        });
        transitionTo(view);
    }

    private void toggleFavourite(TvAppEntry entry) {
        if (entry == null || entry.component().isEmpty()) {
            return;
        }
        List<String> next = favouriteComponents.contains(entry.component())
                ? FavouriteOrder.remove(favouriteComponents, entry.component())
                : FavouriteOrder.add(favouriteComponents, entry.component());
        saveFavouriteEdit(next);
        showApps();
    }

    static List<String> applyFavouriteEdit(
            List<String> current,
            String component,
            FavouriteEdit edit) {
        if (edit == null) {
            return current == null ? List.of() : new ArrayList<>(current);
        }
        switch (edit) {
            case MOVE_LEFT:
                return FavouriteOrder.move(current, component, -1);
            case MOVE_RIGHT:
                return FavouriteOrder.move(current, component, 1);
            case REMOVE:
            default:
                return FavouriteOrder.remove(current, component);
        }
    }

    private void saveFavouriteEdit(List<String> next) {
        List<String> stable = next == null ? List.of() : List.copyOf(next);
        if (stable.equals(favouriteComponents)) {
            return;
        }
        favouriteComponents = stable;
        store.saveFavourites(stable);
    }

    private List<TvAppEntry> favouriteEntries() {
        if (favouriteComponents.isEmpty() || installedApps.isEmpty()) {
            return List.of();
        }
        Map<String, TvAppEntry> byComponent = new HashMap<>();
        for (TvAppEntry entry : installedApps) {
            if (entry != null) {
                byComponent.put(entry.component(), entry);
            }
        }
        ArrayList<TvAppEntry> out = new ArrayList<>();
        for (String component : favouriteComponents) {
            TvAppEntry entry = byComponent.get(component);
            if (entry != null) {
                out.add(entry);
            }
        }
        return out;
    }

    private void launchApp(TvAppEntry entry) {
        if (entry == null) {
            return;
        }
        ComponentName component = ComponentName.unflattenFromString(entry.component());
        if (component == null) {
            staleAppRecovery();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_MAIN)
                .setComponent(component);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException ignored) {
            staleAppRecovery();
        }
    }

    private void staleAppRecovery() {
        currentPage = Page.HOME;
        showHome();
        reloadApps();
    }

    private void launchContent(HomeContentCard card) {
        if (card == null || card.intentUri() == null || card.intentUri().isEmpty()) {
            return;
        }
        try {
            Intent intent = Intent.parseUri(card.intentUri(), 0);
            startActivity(intent);
        } catch (URISyntaxException | ActivityNotFoundException | SecurityException ignored) {
            // Optional content can disappear independently. HOME stays usable.
        }
    }

    static String systemSettingsAction() {
        return Settings.ACTION_SETTINGS;
    }

    private void openSystemSettings() {
        try {
            startActivity(new Intent(systemSettingsAction()));
        } catch (ActivityNotFoundException | SecurityException ignored) {
            // System Settings is OS-owned. HOME remains usable if firmware omits the route.
        }
    }

    private void maybePromptForHomeRole() {
        SharedPreferences prefs = getSharedPreferences(SETUP_PREFS, MODE_PRIVATE);
        boolean alreadyShown = prefs.getBoolean(KEY_HOME_PROMPT_SHOWN, false);
        if (!HomeReplacementPolicy.shouldAutoPrompt(isBoopDefaultHome(), alreadyShown)) {
            return;
        }
        prefs.edit().putBoolean(KEY_HOME_PROMPT_SHOWN, true).apply();
        requestHomeRole();
    }

    private void requestHomeRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roles = getSystemService(RoleManager.class);
            if (roles != null && roles.isRoleAvailable(RoleManager.ROLE_HOME)) {
                if (roles.isRoleHeld(RoleManager.ROLE_HOME)) {
                    return;
                }
                try {
                    startActivity(roles.createRequestRoleIntent(RoleManager.ROLE_HOME));
                    return;
                } catch (ActivityNotFoundException | SecurityException ignored) {
                    // Fall through to the system HOME chooser.
                }
            }
        }
        openHomeSettings();
    }

    private boolean isBoopDefaultHome() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roles = getSystemService(RoleManager.class);
            if (roles != null && roles.isRoleAvailable(RoleManager.ROLE_HOME)) {
                return roles.isRoleHeld(RoleManager.ROLE_HOME);
            }
        }

        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolved = getPackageManager().resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY);
        return resolved != null
                && resolved.activityInfo != null
                && getPackageName().equals(resolved.activityInfo.packageName);
    }

    private String findStockHomePackage() {
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolved;
        try {
            resolved = getPackageManager().queryIntentActivities(
                    home, PackageManager.MATCH_DISABLED_COMPONENTS);
        } catch (RuntimeException ignored) {
            return null;
        }

        ArrayList<HomeReplacementPolicy.Candidate> candidates = new ArrayList<>();
        for (ResolveInfo info : resolved) {
            if (info == null || info.activityInfo == null || info.activityInfo.applicationInfo == null) {
                continue;
            }
            ApplicationInfo app = info.activityInfo.applicationInfo;
            boolean system = (app.flags
                    & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
            boolean enabled = app.enabled && info.activityInfo.enabled;
            candidates.add(new HomeReplacementPolicy.Candidate(
                    info.activityInfo.packageName, system, enabled));
        }
        return HomeReplacementPolicy.selectStockHome(candidates, getPackageName());
    }

    private void openStockHomeAppInfo() {
        String stockPackage = findStockHomePackage();
        if (stockPackage == null) {
            openHomeSettings();
            return;
        }
        openPackageDetails(stockPackage);
    }

    private void restoreStockHome() {
        String stockPackage = findStockHomePackage();
        if (stockPackage == null) {
            openHomeSettings();
            return;
        }
        if (isPackageDisabled(stockPackage)) {
            openPackageDetails(stockPackage);
            return;
        }
        openHomeSettings();
    }

    private boolean isPackageDisabled(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            int state = pm.getApplicationEnabledSetting(packageName);
            if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
                return true;
            }
            if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                return false;
            }
            ApplicationInfo app = pm.getApplicationInfo(
                    packageName, PackageManager.MATCH_DISABLED_COMPONENTS);
            return !app.enabled;
        } catch (PackageManager.NameNotFoundException | IllegalArgumentException ignored) {
            return false;
        }
    }

    private void openPackageDetails(String packageName) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", packageName, null));
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException ignored) {
            openSystemSettings();
        }
    }

    private void openHomeSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
            return;
        } catch (ActivityNotFoundException | SecurityException ignored) {
            // Fall through to general settings on firmware without a HOME chooser surface.
        }
        openSystemSettings();
    }

    private void registerPackageReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");

        packageReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                String action = intent == null ? null : intent.getAction();
                if (PackageRefreshPolicy.shouldReload(action)) {
                    reloadApps();
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(packageReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(packageReceiver, filter);
        }
        receiverRegistered = true;
    }

    private void transitionTo(View next) {
        if (root == null || next == null) {
            return;
        }
        View previous = currentView;
        currentView = next;

        next.setAlpha(0f);
        next.setTranslationX(dp(24));
        root.addView(next, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        next.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(PAGE_TRANSITION_MS)
                .start();

        if (previous != null && previous != next) {
            previous.animate()
                    .alpha(0f)
                    .translationX(-dp(24))
                    .setDuration(PAGE_TRANSITION_MS)
                    .withEndAction(() -> {
                        if (previous.getParent() == root) {
                            root.removeView(previous);
                        }
                    })
                    .start();
        }
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.getRepeatCount() == 0) {
                    backPressGesture.onDown();
                    if (inputHandler != null && backHoldRunnable != null) {
                        inputHandler.removeCallbacks(backHoldRunnable);
                        inputHandler.postDelayed(
                                backHoldRunnable,
                                ViewConfiguration.getLongPressTimeout());
                    }
                }
                return true;
            }

            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (inputHandler != null && backHoldRunnable != null) {
                    inputHandler.removeCallbacks(backHoldRunnable);
                }
                if (event.isCanceled()) {
                    backPressGesture.cancel();
                } else if (backPressGesture.onUpShouldRunShortBack()) {
                    handleShortBack();
                }
                return true;
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void handleShortBack() {
        if (currentPage == Page.HOME && currentView instanceof ShieldHomeView) {
            ((ShieldHomeView) currentView).resetToFirstFavourite();
            return;
        }
        showHome(true);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override public void onBackPressed() {
        handleShortBack();
    }

    @Override protected void onDestroy() {
        destroyed = true;
        ++optionalGeneration;
        if (inputHandler != null && backHoldRunnable != null) {
            inputHandler.removeCallbacks(backHoldRunnable);
        }
        backPressGesture.cancel();
        if (receiverRegistered && packageReceiver != null) {
            try {
                unregisterReceiver(packageReceiver);
            } catch (IllegalArgumentException ignored) {
                // Receiver may already have been detached by framework teardown.
            }
            receiverRegistered = false;
        }
        if (executor != null) {
            executor.shutdownNow();
        }
        super.onDestroy();
    }
}
