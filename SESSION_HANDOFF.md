# BOOP Launcher Alpha 2 handoff — 2026-09-07

Owner: Ryan's Launcher work. Authoritative branch: `boop-launcher-alpha2`.
Project: `launcher/`; package remains `com.boop.launcher`.

## Latest verified state

Ryan physically installed the first Alpha 2 baseline and confirmed the core direction works: black launcher canvas plus swipe-up app drawer. The first immersive follow-up (`0.2.1` / code `4`) still left the Pixel status-bar clock visible in real HOME use even though the normal WindowInsets hide path was present.

Ryan approved a stronger Pixel-specific follow-up using the window-level fullscreen flag while preserving the existing transient system-bar recovery behavior.

Current application source: `324ef2e8246ca787a93e39cf29aeb3325d13d148` (`build: bump launcher to 0.2.2`), containing the functional fullscreen commit `14139c82a8dbba61ac14ff2aa30cfb4fbe152f7c`.

GitHub Actions run `34081679120` completed successfully. Unit tests, fullscreen/immersive regression tests, Android lint, permanent-signer release build, artifact upload and Android 16 signed-APK install/launch/survival smoke all passed.

Signed artifact: `BOOP-Launcher-Alpha2-signed`, artifact ID `10003901065`.
APK SHA-256: `987d0e9d39af44599ef648a7f3075b1cb38415cf0d9f883e42464a6c16bb5e89`.

Package receipt:
- `com.boop.launcher`
- versionName `0.2.2`
- versionCode `5`
- minSdk `29`
- target/compile SDK `36`
- existing permanent BOOP signing identity unchanged

This build is CI-green and launch-smoke-green. The stronger status-bar suppression is not physically accepted until Ryan verifies it on the Pixel 10 Pro XL.

## Current system-bar implementation

`EdgeToEdge.apply()` now adds `WindowManager.LayoutParams.FLAG_FULLSCREEN` before applying the existing edge-to-edge configuration. `hideBars()` reapplies that fullscreen window flag whenever launcher focus returns, then also uses `WindowInsetsController` on Android 11+ to hide `systemBars()` with `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`. Legacy devices retain immersive-sticky flags.

The drawer safe-bottom fix remains: navigation-bar inset contributes bottom padding and `clipToPadding(false)` prevents the last row being guillotined.

## Core Alpha 2 direction

Ryan rejected Alpha 1's old-fashioned visual/interaction direction and approved a clean-sheet launcher inspired by current Pixel Launcher interaction patterns without copying Google proprietary code or assets. Alpha 1 remains preserved on `boop-launcher-alpha1` as historical fallback only.

Approved design: `docs/superpowers/specs/2026-09-07-boop-launcher-alpha2-design.md`.
Implementation plan: `docs/superpowers/plans/2026-09-07-boop-launcher-alpha2.md`.

Alpha 2 deliberately removes permanent launcher furniture: no launcher clock, At a Glance, Google search pill or dock/hotseat. Home is intended to be a black canvas; swipe up opens All Apps; native icons/labels are used; search is contextual; apps can be pinned, persisted, moved and removed; Back flows Search -> All Apps -> Home.

## Still incomplete

- Pixel 10 Pro XL must physically confirm that `0.2.2` finally suppresses the stubborn Android status-bar clock in HOME use.
- Drawer transition is not yet Launcher3-quality direct-finger/spring physics.
- Widget picker/config plumbing exists but widget views are not yet fully rendered/movable/resizable on the workspace.
- Dynamic multi-page workspace behavior is incomplete.
- Rotation/process-death and real widget flows still need physical checks.

## Next physical step

Install the signed `0.2.2` / code `5` APK over the existing launcher. First check whether the top clock/Wi-Fi/battery bar is genuinely gone in normal HOME use. Then check transient edge-swipe system-bar recovery and confirm the final drawer row remains clear. The Pixel physical result outranks emulator screenshots for this behavior.

## Cross-app boundaries

Wall remains `com.boop.alpha1`; Launcher remains `com.boop.launcher`; Shield remains `com.boop.shieldoverlay`. Keep apps independent. Main owns shared cross-project contracts; this branch owns Launcher implementation state. Preserve the existing signing identity and Alpha 1 fallback. CI-green, smoke-green and physically accepted are separate states.
