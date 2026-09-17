#!/usr/bin/env python3
"""Wire BOOP's selected-room Home Assistant controls into the Shield launcher."""
from pathlib import Path

MARKER = "BOOP_SHIELD_ROOM_PANEL_V211"


def once(text, old, new, label):
    if new in text:
        return text
    if text.count(old) != 1:
        raise ValueError(f"{label}: expected one anchor, found {text.count(old)}")
    return text.replace(old, new, 1)


def patch_home(text):
    if MARKER in text:
        return text
    text = once(
        text,
        "public final class ShieldHomeView extends LinearLayout {\n",
        "public final class ShieldHomeView extends LinearLayout {\n"
        "    // BOOP_SHIELD_ROOM_PANEL_V211\n"
        "    private static final int MASCOT_RESERVED_DP = 336;\n",
        "home marker")
    text = once(
        text,
        "    private Callbacks activeCallbacks;\n",
        "    private Callbacks activeCallbacks;\n"
        "    private ShieldRoomControlsView roomControlsView;\n"
        "    private FrameLayout.LayoutParams roomControlsParams;\n"
        "    private com.boop.shieldoverlay.AreaInfo roomControlsRoom;\n"
        "    private com.boop.shieldoverlay.HomeDashboardController.ViewState roomControlsState;\n"
        "    private boolean roomControlsEnabled = true;\n",
        "room panel fields")
    text = once(
        text,
        "        weatherView = null;\n        homeAssistantPuppet = null;\n        nowPlayingSpacer = null;\n",
        "        weatherView = null;\n        roomControlsView = null;\n        roomControlsParams = null;\n"
        "        homeAssistantPuppet = null;\n        nowPlayingSpacer = null;\n",
        "room panel reset")
    text = once(
        text,
        "        homeStage.addView(stageContent, new FrameLayout.LayoutParams(\n"
        "                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.TOP));\n\n"
        "        homeAssistantPuppet = new ShieldNowPlayingPuppetView(getContext());\n",
        "        homeStage.addView(stageContent, new FrameLayout.LayoutParams(\n"
        "                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.TOP));\n\n"
        "        roomControlsView = new ShieldRoomControlsView(getContext());\n"
        "        roomControlsView.bind(roomControlsRoom, roomControlsState);\n"
        "        roomControlsParams = new FrameLayout.LayoutParams(\n"
        "                LayoutParams.MATCH_PARENT, dp(150), Gravity.START | Gravity.BOTTOM);\n"
        "        roomControlsParams.bottomMargin = dp(2);\n"
        "        homeStage.addView(roomControlsView, roomControlsParams);\n"
        "        roomControlsView.setVisibility(roomControlsEnabled ? VISIBLE : GONE);\n\n"
        "        homeAssistantPuppet = new ShieldNowPlayingPuppetView(getContext());\n",
        "room panel host")
    text = once(
        text,
        "        homeAssistantPuppet.setPresentationOwner(com.boop.shared.BoopState.Owner.NONE);\n"
        "        homeAssistantPuppet.setSnapshot(idleAssistantSnapshot());\n",
        "        homeAssistantPuppet.setPresentationOwner(com.boop.shared.BoopState.Owner.NONE);\n"
        "        homeAssistantPuppet.setSnapshot(idleAssistantSnapshot());\n"
        "        homeAssistantPuppet.setVisibilityListener(this::syncRoomPanelMascotSpace);\n",
        "mascot visibility binding")
    anchor = "    @Override public boolean dispatchKeyEvent(KeyEvent event) {\n"
    methods = '''    public void setRoomControls(
            com.boop.shieldoverlay.AreaInfo room,
            com.boop.shieldoverlay.HomeDashboardController.ViewState state,
            boolean enabled) {
        roomControlsRoom = room;
        roomControlsState = state;
        roomControlsEnabled = enabled;
        if (roomControlsView != null) {
            roomControlsView.bind(room, state);
            roomControlsView.setVisibility(enabled ? VISIBLE : GONE);
        }
        boolean mascotVisible = homeAssistantPuppet != null
                && homeAssistantPuppet.getVisibility() == VISIBLE;
        syncRoomPanelMascotSpace(mascotVisible);
    }

    private void syncRoomPanelMascotSpace(boolean boopVisible) {
        if (roomControlsView == null || roomControlsParams == null) return;
        int right = roomControlsEnabled && boopVisible ? dp(MASCOT_RESERVED_DP) : 0;
        if (roomControlsParams.rightMargin == right) return;
        roomControlsParams.rightMargin = right;
        roomControlsView.setLayoutParams(roomControlsParams);
    }

'''
    text = once(text, anchor, methods + anchor, "room panel update methods")
    return text


def patch_puppet(text):
    if MARKER in text:
        return text
    text = once(
        text,
        "public final class ShieldNowPlayingPuppetView extends FrameLayout {\n",
        "public final class ShieldNowPlayingPuppetView extends FrameLayout {\n"
        "    // BOOP_SHIELD_ROOM_PANEL_V211\n"
        "    public interface VisibilityListener {\n"
        "        void onPuppetVisibilityChanged(boolean visible);\n"
        "    }\n",
        "puppet marker")
    text = once(
        text,
        "    private Runnable unsubscribeShared;\n",
        "    private Runnable unsubscribeShared;\n"
        "    private VisibilityListener visibilityListener;\n"
        "    private boolean reportedVisible;\n\n"
        "    public void setVisibilityListener(VisibilityListener listener) {\n"
        "        visibilityListener = listener;\n"
        "        if (listener != null) listener.onPuppetVisibilityChanged(reportedVisible);\n"
        "    }\n",
        "puppet visibility fields")
    text = once(
        text,
        "            setVisibility(GONE);\n            return;\n        }\n\n        setVisibility(VISIBLE);\n",
        "            updatePuppetVisibility(false);\n            return;\n        }\n\n        updatePuppetVisibility(true);\n",
        "puppet visibility reporting")
    text = once(
        text,
        "    private void renderCanonicalFrame(long now) {\n",
        "    private void updatePuppetVisibility(boolean visible) {\n"
        "        setVisibility(visible ? VISIBLE : GONE);\n"
        "        if (reportedVisible == visible) return;\n"
        "        reportedVisible = visible;\n"
        "        VisibilityListener listener = visibilityListener;\n"
        "        if (listener != null) listener.onPuppetVisibilityChanged(visible);\n"
        "    }\n\n"
        "    private void renderCanonicalFrame(long now) {\n",
        "puppet visibility helper")
    return text


def patch_activity(text):
    if MARKER in text:
        return text
    text = once(
        text,
        "public final class ShieldLauncherActivity extends Activity {\n",
        "public final class ShieldLauncherActivity extends Activity {\n"
        "    // BOOP_SHIELD_ROOM_PANEL_V211\n",
        "launcher marker")
    text = once(
        text,
        "    private WeatherSnapshot weatherSnapshot;\n",
        "    private WeatherSnapshot weatherSnapshot;\n"
        "    private com.boop.shieldoverlay.LauncherRoomControlsSession roomControlsSession;\n"
        "    private com.boop.shieldoverlay.AreaInfo roomControlsRoom;\n"
        "    private com.boop.shieldoverlay.HomeDashboardController.ViewState roomControlsState;\n",
        "launcher room fields")
    text = once(
        text,
        "        weatherRepository = new ShieldWeatherRepository(this);\n\n        registerPackageReceiver();\n",
        "        weatherRepository = new ShieldWeatherRepository(this);\n"
        "        roomControlsSession = new com.boop.shieldoverlay.LauncherRoomControlsSession(\n"
        "                this, (room, roomState) -> {\n"
        "                    if (destroyed) return;\n"
        "                    roomControlsRoom = room;\n"
        "                    roomControlsState = roomState;\n"
        "                    if (currentPage == Page.HOME && currentView instanceof ShieldHomeView) {\n"
        "                        ((ShieldHomeView) currentView).setRoomControls(\n"
        "                                room, roomState, store.smartHomePanelEnabled());\n"
        "                    }\n"
        "                });\n\n"
        "        registerPackageReceiver();\n",
        "launcher room session")
    text = once(
        text,
        "        refreshWeather();\n        if (root != null && store != null && currentPage == Page.SETTINGS) {\n",
        "        refreshWeather();\n        refreshRoomControls();\n"
        "        if (root != null && store != null && currentPage == Page.SETTINGS) {\n",
        "room refresh on resume")
    text = once(
        text,
        "    private void reloadApps() {\n",
        "    private void refreshRoomControls() {\n"
        "        if (roomControlsSession == null || store == null) return;\n"
        "        if (!store.smartHomePanelEnabled()) {\n"
        "            roomControlsSession.stop();\n"
        "            roomControlsRoom = null;\n"
        "            roomControlsState = null;\n"
        "            if (currentPage == Page.HOME && currentView instanceof ShieldHomeView) {\n"
        "                ((ShieldHomeView) currentView).setRoomControls(null, null, false);\n"
        "            }\n"
        "            return;\n"
        "        }\n"
        "        roomControlsSession.refresh();\n"
        "    }\n\n"
        "    private void reloadApps() {\n",
        "room refresh method")
    text = once(
        text,
        "        view.setWeather(weatherSnapshot);\n        transitionTo(view);\n",
        "        view.setWeather(weatherSnapshot);\n"
        "        view.setRoomControls(roomControlsRoom, roomControlsState, store.smartHomePanelEnabled());\n"
        "        transitionTo(view);\n",
        "initial room panel bind")
    text = once(
        text,
        "                view.render(favouriteEntries(), readyRows, nowPlayingSnapshot, homeCallbacks());\n"
        "                if (focusComponent != null) {\n",
        "                view.render(favouriteEntries(), readyRows, nowPlayingSnapshot, homeCallbacks());\n"
        "                view.setRoomControls(roomControlsRoom, roomControlsState, store.smartHomePanelEnabled());\n"
        "                if (focusComponent != null) {\n",
        "rerender room panel bind")
    text = once(
        text,
        "        boolean appChannels = store.rowEnabled(OptionalRowRegistry.Key.APP_CHANNELS);\n"
        "        boolean homeOverrideEnabled = isHomeOverrideEnabled();\n",
        "        boolean appChannels = store.rowEnabled(OptionalRowRegistry.Key.APP_CHANNELS);\n"
        "        boolean smartHomePanel = store.smartHomePanelEnabled();\n"
        "        boolean homeOverrideEnabled = isHomeOverrideEnabled();\n",
        "settings toggle state")
    text = once(
        text,
        "                nowPlayingAccess,\n                playerLabel,\n                new ShieldHomeSettingsView.Callbacks() {\n",
        "                nowPlayingAccess,\n                playerLabel,\n                smartHomePanel,\n                new ShieldHomeSettingsView.Callbacks() {\n",
        "settings render argument")
    text = once(
        text,
        "            @Override public void onSetRowEnabled(OptionalRowRegistry.Key key, boolean enabled) {\n"
        "                store.setRowEnabled(key, enabled);\n"
        "                showSettings();\n"
        "            }\n\n"
        "            @Override public void onChooseHomeApp() {\n",
        "            @Override public void onSetRowEnabled(OptionalRowRegistry.Key key, boolean enabled) {\n"
        "                store.setRowEnabled(key, enabled);\n"
        "                showSettings();\n"
        "            }\n\n"
        "            @Override public void onSetSmartHomePanelEnabled(boolean enabled) {\n"
        "                store.setSmartHomePanelEnabled(enabled);\n"
        "                refreshRoomControls();\n"
        "                showSettings();\n"
        "            }\n\n"
        "            @Override public void onSetDeviceRoom() {\n"
        "                startActivity(new Intent()\n"
        "                        .setClassName(getPackageName(), \"com.boop.shieldoverlay.BoopHomeActivity\")\n"
        "                        .putExtra(\"boop_choose_room\", true));\n"
        "            }\n\n"
        "            @Override public void onChooseHomeApp() {\n",
        "settings callbacks")
    text = once(
        text,
        "        if (executor != null) {\n            executor.shutdownNow();\n        }\n        super.onDestroy();\n",
        "        if (roomControlsSession != null) {\n"
        "            roomControlsSession.close();\n"
        "            roomControlsSession = null;\n"
        "        }\n"
        "        if (executor != null) {\n            executor.shutdownNow();\n        }\n        super.onDestroy();\n",
        "room session destroy")
    text = once(
        text,
        "        com.boop.shared.BoopState.INSTANCE.homeVisible(false);\n        super.onPause();\n",
        "        com.boop.shared.BoopState.INSTANCE.homeVisible(false);\n"
        "        if (roomControlsSession != null) roomControlsSession.stop();\n"
        "        super.onPause();\n",
        "room session pause")
    return text


def patch_file(path, transform):
    if not path.is_file():
        return False
    original = path.read_text(encoding="utf-8")
    updated = transform(original)
    if updated != original:
        path.write_text(updated, encoding="utf-8")
    return True


def main():
    roots = [
        Path("unified/shield-home/src/main/java/com/boop/shieldhome"),
        Path("boop-build/BOOP-Alpha1/shield-home-lib/src/main/java/com/boop/shieldhome"),
    ]
    patched = 0
    for root in roots:
        patched += patch_file(root / "ShieldHomeView.java", patch_home)
        patched += patch_file(root / "ShieldNowPlayingPuppetView.java", patch_puppet)
        patched += patch_file(root / "ShieldLauncherActivity.java", patch_activity)
    if patched == 0:
        raise SystemExit("No Shield Home source tree was available for room-panel patching")
    print(f"Shield adaptive room panel wired in {patched} source file(s)")


if __name__ == "__main__":
    main()
