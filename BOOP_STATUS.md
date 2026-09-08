# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Package: `com.boop.shieldhome`
- Unified/AIO `com.boop.alpha1`: separate and untouched
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Physically accepted Apps drawer: 0.9 floating square icons
- Physically-good HOME geometry/chrome: 0.9.4
- Notification Listener manually enabled on real Shield: **physical PASS, Now Playing appeared immediately**
- v0.10.3 collision layout: physical **much better**
- v0.10.4 spacing: physical **awesome spacing**
- v0.10.5 album art: **physical PASS**, appeared immediately without skipping track
- Current candidate: code 21 / `0.10.6-focus-outline`
- Build source: `7ff4e64a0cdb1ca02dd4b4af5391ba1095b2569d`
- Workflow: `34287673372` SUCCESS
- Artifact ID: `10080137191`
- APK SHA-256: `048ca7fa179c21b4f307774f07eb7eabb6eb706444b228fa4b32391a9c746ea1`
- Artifact ZIP SHA-256: `cedef689f8658a3d646579246c31fd6531246580e68c813610bfb4ab90d885cf`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Functional/build/signer/package green; 3dp focus outline physically pending Ryan

## v0.10.6 focus candidate

Ryan requested clearer remote focus across HOME, Apps drawer and Launcher Settings.

- 3dp cyan/blue outline is additive to the existing scale/pop animation.
- HOME favourite banner outline sits on the artwork, preserving the accepted no-black-plate look.
- Apps drawer cards retain the existing selection plate and gain the outline.
- HOME action buttons, Now Playing artwork/controls and Launcher Settings rows use the same focus colour.
- `FocusChrome` and the Now Playing progress fill both resolve the same Android `colorControlActivated` value.
- No accepted layout dimensions or focus-scale values were intentionally changed.

## Fast CI rule

Ryan owns real-device visual acceptance. CI sets `BOOP_SKIP_MANUAL_VISUAL_TESTS=1`; no screenshot, golden, emulator appearance, layout, focus-scale or animation visual acceptance runs. Functional logic, signed assembly, exact package/version, manifest/service/resource presence, permanent signer, APK integrity and upload remain checked.

## Locked behavior

Preserve single Home -> BOOP; double Home -> native Recent Apps; reboot rearm; stock Android TV Home recovery; banners/grab/reorder; accepted Apps drawer; Back behavior; Shield Settings access; volume/CEC/system shortcuts; Now Playing permission path, album art and puppetry.

## Next gate

Install/update v0.10.6 and physically judge the 3dp focus border on HOME, Apps drawer, Now Playing controls/artwork and Launcher Settings. Confirm the border colour visually matches the Now Playing progress bar and does not disturb the accepted scale/spacing.

Real Shield behavior is authority. Do not merge into unified until Ryan explicitly approves the standalone behavior.
