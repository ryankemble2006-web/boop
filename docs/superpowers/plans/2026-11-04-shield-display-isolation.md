# Shield display isolation implementation plan

Goal: restore EastEnders fullscreen and prevent BOOP from changing the global display on resume, wake or permission recovery.

Architecture: Keep the verified v415 APK and its app-local BoopTvChrome sizing. Remove the two global display writers in ShieldSelfGrant. Preserve every other APK entry and every unrelated method in classes3.dex. Use the existing GitHub signing key.

Evidence: iPlayer's window was fullscreen at 3839x2160 but content was centered with borders. Resetting size was immediately undone while BOOP ran. Stopping BOOP and using 1920x1080 / stock density restored fullscreen; screenshot confirmed playback. HDMI remains 3840x2160.

- [x] Capture before state and restore fullscreen.
- [ ] Add regression gate; demonstrate failure against v415 source/bytecode.
- [ ] Prepare v416 source and minimal deterministic DEX patch; check source correspondence, unchanged APK entries and unrelated classes/methods.
- [ ] Review, build once locally, sync branch, push for permanent GitHub signing.
- [ ] Verify signature/version, install without starting Home or changing display, check playback and stable settings.

Constraints: no playback navigation or display changes while user watches; no replacement signing key; preserve shared checkout changes; native 4K and wake-cycle hardware acceptance remain explicit if untested.
