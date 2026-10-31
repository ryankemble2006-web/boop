# Native 4K launcher implementation plan

**Goal:** Actual4Krendering with the same apparent sizes, plus a GitHub-signed APK correcting partial resize handling.
**Architecture:** Existing675dpheight-based launcher density preserves physical proportions. NVIDIA native HDMI mode enables exact3840x2160instead of the old1080pcap. Sourcev409 rebuilds the current page on configuration changes instead of only laying out old constructor-sized views. The established v408 binary is the deterministic baseline; only its launcher method dex and version manifest are rebuilt, preserving other entries. GitHub holds the signing key.

- [x] Capture before screenshots/UI bounds and source/device identities.
- [x] Enable native NVIDIA4Kmode through official Settings; verify exact launcher/Kodi buffers and persistence after reboot.
- [x] Compare Home bounds: same apparent sizes,1–5outputpixel rounding/font differences at4K.
- [ ] Implement/source-review page rebuild; add guarded deterministic GitHub patch build and artifact integrity checks.
- [ ] Push v409 source, run GitHub build/sign, download and verify version/certificate.
- [ ] Install signed APK on Shield; verify native4Klayout, test a live resolution change for stale text/artwork, restore native4Kand leave launcher ready.
- [ ] Publish signed APK on GitHub with native4Ksetup and verification receipt.
