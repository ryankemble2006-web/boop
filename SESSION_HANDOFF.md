# BOOP Launcher Alpha 2 handoff — 2026-09-07

Owner: Ryan's Launcher work. Authoritative branch: `boop-launcher-alpha2`.
Project: `launcher/`; package remains `com.boop.launcher`.

## Latest state

Ryan physically confirmed the stronger `0.2.2` fullscreen build works on the Pixel: the stubborn Android status-bar clock is gone. Pure-black HOME plus swipe-up All Apps are therefore physically useful.

Ryan then requested one small navigation adjustment: while All Apps is open, a downward swipe from the top of the drawer should close it back to HOME. He explicitly wants to perform the physical gesture test himself rather than spend time on emulator validation.

Current application source: `5b18bb983de284a7773f30cc1a5897a7cb002c35` (`build: bump launcher to 0.2.3`). Gesture implementation commits immediately before it are `24c002e93bf600338559fe867917eb3d6e117550` and `6048495237a0614acb95f266c0912ab9d8d42c14`.

Signed build:
- package `com.boop.launcher`
- versionName `0.2.3`
- versionCode `6`
- GitHub Actions run `34082550615`
- signed artifact `BOOP-Launcher-Alpha2-signed`
- artifact ID `10004259850`
- APK SHA-256 `8fc6c4b212d3fca7cddd98ec420724b8f38c7bb0cf79afdf57fc325d3c6ec05e`
- existing permanent BOOP signing identity unchanged

The normal signing workflow's compile/lint gate completed before artifact upload. Do not treat swipe-down as physically accepted until Ryan tries it on the Pixel. No emulator result is required for this tiny gesture pass; Ryan is the acceptance test.

## Swipe-down behavior

`AllAppsView` records a downward gesture beginning while the drawer is at its top position. A downward travel over the small threshold calls `closeDrawer()`. `MainActivity` then returns state to HOME using the existing drawer-dismiss animation. Starting the gesture while the app list is already scrolled down continues to behave as list scrolling rather than unexpectedly closing the drawer.

## Current system-bar behavior

The physically accepted fullscreen implementation uses the window-level fullscreen flag plus the WindowInsets immersive path. System bars are reapplied hidden when launcher focus returns; Android retains transient recovery behavior. Drawer bottom-safe padding remains so the last app row is not guillotined.

## Core Alpha 2 direction

Ryan rejected Alpha 1's old-fashioned visual/interaction direction and approved a clean-sheet launcher inspired by current Pixel Launcher interaction patterns without copying Google proprietary code or assets. Alpha 1 remains preserved on `boop-launcher-alpha1` as historical fallback only.

Approved design: `docs/superpowers/specs/2026-09-07-boop-launcher-alpha2-design.md`.
Implementation plan: `docs/superpowers/plans/2026-09-07-boop-launcher-alpha2.md`.

Alpha 2 deliberately removes permanent launcher furniture: no launcher clock, At a Glance, Google search pill or dock/hotseat. Home is intended to be a black canvas; swipe up opens All Apps; native icons/labels are used; search is contextual; apps can be pinned, persisted, moved and removed; Back flows Search -> All Apps -> Home.

## Still incomplete

- Swipe-down drawer close is signed but awaiting Ryan's physical acceptance.
- Drawer transition is not yet Launcher3-quality direct-finger/spring physics.
- Widget picker/config plumbing exists but widget views are not yet fully rendered/movable/resizable on the workspace.
- Dynamic multi-page workspace behavior is incomplete.
- Rotation/process-death and real widget flows still need physical checks.

## Next physical step

Install signed `0.2.3` / code `6`. Open All Apps with swipe up, then swipe down from the top of the drawer and confirm it returns naturally to the black HOME canvas. Ryan's physical feel result is authoritative.

## Cross-app boundaries

Wall remains `com.boop.alpha1`; Launcher remains `com.boop.launcher`; Shield remains `com.boop.shieldoverlay`. Keep apps independent. Main owns shared cross-project contracts; this branch owns Launcher implementation state. Preserve the existing signing identity and Alpha 1 fallback. Signed, CI-green and physically accepted are separate states.
