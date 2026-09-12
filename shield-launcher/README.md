# BOOP Launcher

A standalone Nvidia Shield Home app with the existing BOOP launcher and advanced
tools. Package com.boop.shieldlauncher is separate from Unified and older Home.

## On another Shield

Install the permanent-signed BOOP-Launcher.apk. Open Launcher Settings and enable
Media access for Now Playing. Use BOOP with the Home button opens the Shield's
accessibility setup; Choose default Home app opens the OS chooser where supported.
Each is an explicit user action. No access or default role is silently enabled.
Startup Manager has its own connection/approval flow for local advanced tools.
The launcher/drawer remain usable without that connection; native Close Player
requires it. No Home Assistant login or Unified installation is needed.
Use BOOP defaults opens a review, not an immediate disable operation. The receiving
Shield saves its own original settings for Undo. Keep that app data until managed
changes have been restored; deleting app data also deletes its private receipts.
The historical defaults preset has not had full second-device Apply/Undo/reboot
acceptance. Read the exact build receipt before treating this as physically tested.

## Build

From repository root: python scripts/materialize-shield-launcher.py
Then: gradle -p shield-launcher :app:assembleDebug
Requires existing Java 17, Gradle 9.6.0 and Android SDK 36/build tools 36.0.0.
Without the configured permanent signer this only produces an unsigned local APK.
Use the Build BOOP Shield Launcher GitHub workflow for the distributable signed APK.
Functional gates: tests/test_shield_launcher_split.py, scripts/test-launcher-close.py,
scripts/test-startup-manager.py and scripts/test-startup-defaults.py.
Actual APK verification: scripts/verify-shield-launcher-apk.py <apk>.
Visual/device acceptance remains manual. No artwork generation or visual CI.
