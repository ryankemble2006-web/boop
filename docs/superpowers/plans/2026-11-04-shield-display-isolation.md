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
