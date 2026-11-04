# Shield native Recents first-open fix

Goal: open native task manager on the first attempt with all animations off, preserving font1.3, chunky BOOP and NVIDIA-managed 1080p UI / 4K HDMI.

Evidence: native Recents focused/drawn but black; task-card children exist yet accessibility tree has no visible cards. Installed SystemUI initializes cards transparent for Home entry, and reveals them on its entry completion path. Back/reopen works. First opening after a controlled reboot using held native App Switch key showed cards with animation scales still0. This supports a first-entry preparation/timing fault.

- [x] Capture failing screen and installed SystemUI code; confirm mode/scale intact.
- [x] Test cold native key preload with animations0 after reboot.
- [x] Add bounded serialized loopback-ADB native key dispatcher; preserve accessibility fallback and existing key mappings. Test duplicate requests, missing identity and ADB failure with Java harness; verify source routing. Review additionally found and fixed Home double-tap's direct native bypass and suppressed fallback after ambiguous submission. Both regressions were reproduced against prior code and pass with the fixes.
- [x] Local build and DEX audit passed: 814 existing classes plus exactly 3 helper classes; only manifest/classes3.dex payload changes. Source gates3, existing display2/capture3 and worker harness scenarios passed. Label-offset/debug/NOP normalization applies only to the two changed methods; other methods remain exact.
- [ ] Build v418 once locally from pinned v417, audit existing classes/methods and APK payload, review, sync/push for existing permanent signing.
- [ ] Verify signed payload/certificate, install, then cold-boot first-open through BOOP mapping and repeat native Recents checks. Leave animations0 and font1.3.

Scope: no SystemUI patching, animation-setting changes, layout resizing, custom task manager, forced resolution/density, playback actions or unrelated remapping changes. If cold native preload does not resolve consistently, retain hypothesis status and re-investigate.
