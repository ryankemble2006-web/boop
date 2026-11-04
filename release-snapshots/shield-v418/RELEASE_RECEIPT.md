# BOOP Shield v418 native Recents preload

BOOP's mapped Recents action now sends one serialized native App Switch key with its preload interval through the existing trusted loopback ADB identity. This preserves native SystemUI task manager and avoids cold first-entry cards remaining transparent. Bounded/no-prompt connection and accessibility fallback retained; duplicate requests are suppressed while pending.

All animations remain0, font scale1.3, chunky launcher unchanged. NVIDIA extended modes remain off, 4K HDMI / NVIDIA-managed1080p UI / stock320DPI. No display or layout code changed. v416/v417 fixes retained.

Evidence: captured black focused native Recents with instantiated card children and invisible accessibility cards; installed SystemUI code prepares Home-entry cards at alpha0 then reveals them in entry completion. Back/reopen works. Held native App Switch showed cards on first opening after a controlled reboot with all animation scales still0. This supports preparation/timing fault; final acceptance requires BOOP's actual physical mapping after install.

Build `python build_recents_preload.py --tool <apktool-2.12.1.jar> --sdk <AndroidSDK>`. Pins baseline417, Apktool, Android36 API and D8. Existing DEX methods are audited, only the Recents case changes, and three helper classes are added. Only manifest/classes3.dex change within the APK; existing GitHub signer supplies permanent signature.

Tests: `python test_recents_preload.py .` and `python test_recents_runtime.py shield-home-lib/src/main/java/com/boop/shieldhome/RecentsPreload.java`. Runtime harness exercises native dispatch, duplicate suppression, bounded trusted/no-prompt ADB, offline fallback and missing-key fallback. No animation setting writes are added.
