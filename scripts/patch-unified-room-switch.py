#!/usr/bin/env python3
from pathlib import Path

path = Path('boop-build/BOOP-Alpha1/shield-lib/src/main/java/com/boop/shieldoverlay/BoopHomeActivity.java')
text = path.read_text(encoding='utf-8')
old = '''    private void rememberRoomAndContinue(AreaInfo area) {
        if (area == null || preferences == null) {
            return;
        }
        preferences.setSelectedRoom(area);
        closeHomeSocket();
        firstRun.afterPairingSuccess(true);
        showHomeShell();
    }
'''
new = '''    private void rememberRoomAndContinue(AreaInfo area) {
        if (area == null || preferences == null) {
            return;
        }
        // The newly selected room becomes authoritative before any new dashboard
        // can be created. Tear down every previous-room owner, then rebuild Home.
        clearNavigationShellState();
        closeSettingsPage();
        closeRoutinesController();
        closeHomeSocket();
        dashboardController = null;
        dashboardState = null;
        homeView = null;
        preferences.setSelectedRoom(area);
        firstRun.afterPairingSuccess(true);
        showHomeShell();
    }
'''
if text.count(old) != 1:
    raise SystemExit(f'room switch lifecycle: expected one anchor, found {text.count(old)}')
path.write_text(text.replace(old, new, 1), encoding='utf-8')
print('Room switch now tears down previous-room owners before rebuilding Home')
