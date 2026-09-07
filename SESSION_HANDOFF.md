# BOOP Launcher Alpha 2 handoff — 2026-09-07

Owner: Ryan's Launcher work. Authoritative branch: `boop-launcher-alpha2`.
Project: `launcher/`; package remains `com.boop.launcher`.

## Latest verified state

Ryan physically installed the first Alpha 2 baseline (`0.2.0` / code `3`) and confirmed the core direction works: pure black launcher canvas and a functioning swipe-up app drawer. The physical screenshot then exposed two concrete problems: Android's status bar and 3-button navigation bar remained visible over the launcher, and the bottom drawer row could sit underneath the navigation area.

Ryan explicitly approved immersive Home + drawer. The launcher should hide status and navigation bars, allow Android to reveal them transiently with an edge swipe, then return to the uninterrupted black canvas. The drawer must retain safe bottom space so its final row is not clipped.

Implementation source commit: `92c34d47b19e6d2191891e9eb9ffe5a329394cf1` (`feat: make Alpha 2 immersive and protect drawer bottom`).

GitHub Actions run `34080350338` completed successfully. Unit tests, the immersive regression tests, Android lint, permanent-signer release build, artifact upload and Android 16 signed-APK launch/survival smoke all passed.

Signed artifact: `BOOP-Launcher-Alpha2-signed`, artifact ID `10003492030`.
APK SHA-256: `1dfcd87412b8308704325db9d1045f08294942e01ff53d62cddd20cf21d8435c`.

Package receipt for this build:
- `com.boop.launcher`
- versionName `0.2.1`
- versionCode `4`
- minSdk `29`
- target/compile SDK `36`
- existing permanent BOOP signing identity unchanged

This build is CI-green and launch-smoke-green, but the new immersive behavior is not yet physically accepted on Ryan's Pixel. Do not call the immersive fix physically green until he installs it.

## Immersive implementation

`EdgeToEdge` now hides `WindowInsets.Type.systemBars()` on Android 11+ and uses `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`, preserving an Android escape gesture while keeping Home visually clean. Legacy devices use immersive-sticky fullscreen/navigation flags. `MainActivity` reapplies hidden bars whenever launcher window focus returns.

`AllAppsView` now uses `clipToPadding(false)` and reserves bottom padding based on the navigation-bar inset ignoring visibility, so the last app row stays usable even if system navigation is transiently revealed.

The version was bumped from `0.2.0`/3 to `0.2.1`/4 for a clean update installation.

## Core Alpha 2 direction

Ryan rejected Alpha 1's old-fashioned visual/interaction direction and approved a clean-sheet launcher inspired by current Pixel Launcher interaction patterns without copying Google proprietary code or assets. Alpha 1 remains preserved on `boop-launcher-alpha1` as historical fallback only.

Approved design: `docs/superpowers/specs/2026-09-07-boop-launcher-alpha2-design.md`.
Implementation plan: `docs/superpowers/plans/2026-09-07-boop-launcher-alpha2.md`.

Alpha 2 deliberately removes permanent clock, At a Glance, Google search pill, dock/hotseat, introduction/editor furniture, BOOP return-strip overlay, foreground service, Internet permission and microphone permission. Home is a black canvas; swipe up opens All Apps; native icons/labels are used; search is contextual; apps can be pinned, persisted, moved and removed; Back flows Search -> All Apps -> Home.

## Still incomplete

- Drawer transition is not yet Launcher3-quality direct-finger/spring physics.
- Widget picker/config plumbing exists but widget views are not yet fully rendered/movable/resizable on the workspace.
- Dynamic multi-page workspace behavior is incomplete.
- Rotation/process-death and real widget flows still need physical checks.
- The new immersive system-bar behavior and drawer safe-bottom fix need Ryan's physical acceptance.

## Next physical step

Install the signed `0.2.1` / code `4` APK over the existing launcher. Confirm first that Home is genuinely uninterrupted black, the clock/Wi-Fi/battery and 3-button nav are hidden during normal use, an edge swipe can temporarily recover Android's bars, and the final drawer row is no longer clipped. Then continue collecting the feel/bug list, especially drawer physics, spacing and icon scale.

## Cross-app boundaries

Wall remains `com.boop.alpha1`; Launcher remains `com.boop.launcher`; Shield remains `com.boop.shieldoverlay`. Keep apps independent. Main owns shared cross-project contracts; this branch owns Launcher implementation state. Preserve the existing signing identity and Alpha 1 fallback. CI-green, launch-smoke-green and physically accepted are separate states.
