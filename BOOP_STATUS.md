# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Package: `com.boop.shieldhome`
- Unified/AIO `com.boop.alpha1`: separate and untouched
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Physically accepted Apps drawer: 0.9 floating square icons
- Physically-good HOME geometry/chrome: 0.9.4
- Notification Listener on real Shield: **physical PASS, Now Playing appeared immediately**
- v0.10.3 collision layout: physical **much better**
- v0.10.4 spacing: physical **awesome spacing**
- v0.10.5 album art: **physical PASS**, appeared immediately without skipping track
- v0.10.6 focus outline: CI/signer green, physical judgement still pending unless Ryan explicitly accepts it
- Current candidate: code 22 / `0.10.7-puppet-bay`
- Build source: `8d6486ba51e747286847b7453189981266f13243`
- Workflow: `34288943710` SUCCESS
- Artifact ID: `10080598609`
- APK SHA-256: `5bb188d520fcf0ed73a871c6ef60007a58d782226349113377fcf58f406c2ae9`
- Artifact ZIP SHA-256: `a7b453e6922aa35fd227337318e9a371379bd2f03c7e5fbcd8d5b6f40b97332c`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Functional/build/signer/package green; puppet position/size physically pending Ryan

## v0.10.7 puppet bay

Ryan approved putting headphones BOOP into the open right-hand Now Playing space and deferring the full Shield/Tegra animation engine until the placement is settled.

- `ShieldNowPlayingView` now owns `ShieldNowPlayingPuppetView` directly inside the existing 230dp mascot bay.
- The former Activity-root puppet overlay and its manual page-position lifecycle are gone.
- Existing approved headphones artwork, groove/acknowledgement motion and power-saving behavior are preserved.
- Puppet remains non-focusable, non-clickable and excluded from accessibility navigation.
- The stage clips motion to the reserved bay.
- No GPU/OpenGL/skeletal renderer was introduced yet; the card-owned stage is the future swap point for richer Tegra puppetry.

TDD RED: `980b2d698032124b81a477e54c538171dbee1142`, workflow `34288555747`, 73 tests with exactly 1 expected host-contract failure. Production transplant through `17dcc253a25344ab424a01110666fe0b67529e51` then passed the fast lane. Final release source is `8d6486ba51e747286847b7453189981266f13243`.

## Fast CI rule

Ryan owns real-device visual acceptance. CI sets `BOOP_SKIP_MANUAL_VISUAL_TESTS=1`; no screenshot, golden, emulator appearance, layout, focus-scale or animation visual acceptance runs. Functional logic, signed assembly, exact package/version, manifest/service/resource presence, permanent signer, APK integrity and upload remain checked.

## Locked behavior

Preserve single Home -> BOOP; double Home -> native Recent Apps; reboot rearm; stock Android TV Home recovery; banners/grab/reorder; accepted Apps drawer; Back behavior; Shield Settings access; volume/CEC/system shortcuts; Now Playing permission path and physically-green album art.

## Next gate

Install/update v0.10.7. Physically judge BOOP inside the Now Playing bay: size, perch and clipping. Confirm he never overlaps media UI and remote navigation is unchanged. Minor positioning/scale adjustments can follow after use. Do not start the full Tegra/GPU puppetry pass until Ryan decides the stage position is worth locking.

Real Shield behavior is authority. Do not merge into unified until Ryan explicitly approves the standalone behavior.
