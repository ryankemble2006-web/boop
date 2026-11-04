# Shield display isolation implementation plan

Goal: restore EastEnders fullscreen and prevent BOOP from changing the global display on resume, wake or permission recovery.

Architecture: Keep the verified v415 APK and its app-local BoopTvChrome sizing. Remove the two global display writers in ShieldSelfGrant. Preserve every other APK entry and every unrelated method in classes3.dex. Use the existing GitHub signing key.

Evidence: iPlayer's window was fullscreen at 3839x2160 but content was centered with borders. Resetting size was immediately undone while BOOP ran. Stopping BOOP and using 1920x1080 / stock density restored fullscreen; screenshot confirmed playback. HDMI remains 3840x2160.

- [x] Capture before state and restore fullscreen.
- [x] Add regression gate; demonstrate failure against v415 source/bytecode.
- [x] Prepare v416 source and minimal deterministic DEX patch; check source correspondence, unchanged APK entries and unrelated classes/methods.
- [x] Review, build locally, sync branch, push for permanent GitHub signing. Initial DEX audit exposed formatting-only blank-line differences; audit normalization corrected before successful build. Independent review found no Critical/Important issue.
- [x] Verify signature/version, install without starting Home or changing display, check playback and stable settings.
- [ ] Hardware acceptance after viewing: Home/resume, sleep/wake, reboot, and native 4K UI. These checks intentionally require a later viewing break.

Constraints: no playback navigation or display changes while user watches; no replacement signing key; preserve shared checkout changes; native 4K and wake-cycle hardware acceptance remain explicit if untested.

Deployment: commit fe4ef8f5135993cce7f03826fd335490dc5c3c3f, GitHub Actions run 41775265923 succeeded. Signed payload matches all 373 unsigned APK entries; existing certificate verified. Installed 1.2.416-shield at device time 2026-11-04 04:48:54. iPlayer PID 25678 remained unchanged and focused; fresh screenshot confirmed full picture after update. BOOP accessibility services rebound, while 1920x1080 logical UI / stock 320 DPI remained stable and HDMI stayed 3840x2160.

Signed APK SHA-256: 6ea89379ce13e10268263c380caeba0340e8af318442b8d99420ff1917c56937.

## Correction: playback froze after v416 installation

The initial playback-continuity claim was incorrect: unchanged PID/focus and a fullscreen screenshot did not establish advancing video. The user reported frozen playback and no navigation. Captures were identical across minutes. Logs showed background spectrum revival at 04:48:56.346, iPlayer onPause at .365, MediaProjection permission activity at .462 and decoder release at .503. This background recovery behavior was inherited from v415 and triggered by updating the app.

Force-stopped BOOP to terminate capture, restarted iPlayer without display changes, and selected the existing profile. User confirmed physical remote navigation works. A subsequent screenshot showed the user had resumed EastEnders fullscreen; projection was null.

v417 removes only StartupCleanupBootReceiver.reviveSpectrumIfWanted's automatic activity launch; permission and cleanup recovery remain. Home retains spectrum startup. Three receiver regression tests pass (two failed against baseline), both display isolation tests pass, 814 DEX classes and all other APK entries audited. Independent review found no Critical/Important issue. Boot accessibility rearming remains and may bring Home forward on a new boot; that separate behavior is not claimed fixed.

v417 deployment: e4e43aff47690129083794202dd0687f04ed76a5, GitHub Actions 41776004943 succeeded. Permanent certificate matched; all 373 signed payload entries matched the audited unsigned APK. Installed 1.2.417-shield at 04:56:22. iPlayer PID 30115 stayed unchanged and focused. Distinct, visually inspected fullscreen episode frames after install prove advancement; MediaProjection stayed null. No capture/projection launch or iPlayer pause matched the install log. UI remains 1920x1080 / stock 320 DPI, HDMI 3840x2160. Physical remote was confirmed by user during recovery before v417 install; no synthetic navigation/playback keys were sent during this installation.

Signed v417 SHA-256: 0d4c5bbfea19c3d04fa1959e72fe901f2822b5cd5d2803d5b74b69efc64dd1b9.

## Superseding display configuration acceptance

User subsequently reported tiny system Settings/Recents after reboot, while BOOP retained correct chunky proportions. Explicit requirements: preserve font_scale=1.3 and BOOP's chunky appearance; match NVIDIA's stable system setup. Investigation confirmed the earlier native-4K experiment's extended display developer option was still enabled. Disabled it in official Settings, cleared legacy saved overrides before its required reboot, then verified normal Settings/Recents, unchanged chunky BOOP, 4K HDMI / NVIDIA-managed 1080p UI / stock 320 DPI / font1.3. A full asleep/off then awake/on cycle retained those settings. Saved global size and secure density overrides remain empty; runtime wm1920 override is NVIDIA-managed and must not be reset back to4K. No launcher code or APK change was necessary. See `docs/SHIELD_DISPLAY_BASELINE.md` for the current owning setup; earlier native-4K-UI instructions are superseded.
