# BOOP Shield v417 background recovery fix

Preserves the v416 display-isolation fix. Background boot/package recovery no longer opens BassCaptureActivity or its MediaProjection prompt over a foreground player. User-driven Home spectrum startup and spectrum preference remain unchanged. Permission recovery and startup cleanup remain unchanged.

Evidence: at 04:48:56 the v416 install was followed by BassCaptureActivity launch and iPlayer onPause/decoder release. Screenshot and unchanged PID were insufficient proof of playback continuity. Force-stopping capture and restarting iPlayer restored UI response; user confirmed physical remote control.

Build: `python build_background_recovery.py --tool <apktool-2.12.1.jar>`. Pins v416 unsigned APK and tool, audits 814 classes, all unrelated methods and unchanged APK payload entries. GitHub supplies the permanent signature.

Regression: `python test_spectrum_background.py capture-verified.smali shield-home-lib/src/main/java/com/boop/shieldhome/StartupCleanupBootReceiver.java` checks background receiver cannot launch capture UI while permission/cleanup recovery remains.

Hardware wake/reboot and native 4K UI acceptance remain pending. No further display/navigation changes during user viewing. Installation must verify advancing frames and input response, not merely a static image.
