# BOOP unified handoff

Updated 2026-09-08. Owner `boop-unified`, package `com.boop.alpha1`, permanent signer unchanged. Fresh main owns shared contracts; this handoff owns implementation/evidence.

## New Shield blocker: UI progressively shrinks on reopening (2026-09-08)

Ryan reports that each opening of BOOP on Shield makes its UI smaller, now unreadable. The installed APK/version has not been confirmed in this report, and whether other apps are affected has not yet been answered. Do not infer physical acceptance of v44 from this report.

Read-only code investigation used live `boop-unified@acdbd8da221ba529718bba35948f83965a2882fc` and main `dd38cfc72eb5d00bc42121f88c633cb237805009`. Strong source-level causal match: `unified/UnifiedApplication.java` registers `onActivityPreCreated` for the Shield profile, reads the activity's CURRENT `resources.getConfiguration().densityDpi`, multiplies it by `SHIELD_UI_SCALE = 0.80f`, and calls `resources.updateConfiguration(...)`. When resources reuse that already-modified configuration, every subsequent activity creation reduces it again instead of applying a fixed target. `scripts/materialize-unified.sh` copies this application source into the built app. An ordinary Shield launch passes through multiple activities, so do not describe the rate as exactly one reduction per user opening.

Arithmetic-only reproduction of the existing formula with a hypothetical initial density of 320: `320 -> 256 -> 205 -> 164 -> 131 -> 105 -> 84 -> 67 -> 54 -> 43 -> 34`. This confirms the compounding calculation, NOT an Android runtime or physical-device reproduction. The original APK on Ryan's Shield was not inspected via ADB.

Next safe implementation: make Shield sizing idempotent, deriving its target from an unmodified baseline rather than repeatedly multiplying current resources. Preserve the intended one-time scale unless Ryan changes that requirement. Consider resource sharing and real display-configuration changes; do not change system-wide Shield density/resolution or unrelated phone/wake behaviour. Keep validation non-visual and focused on repeatability/state; Ryan remains the appearance/D-pad/device acceptance authority.

Suggested temporary diagnostic only: manually force-stop BOOP from Android Settings, then reopen once. A fresh process may clear the accumulated in-memory resource changes, but the present code can reintroduce them. No force-stop, restart, installation, data clearing, permission grant or physical test was performed here. Do not clear BOOP's data or HA pairing to investigate this.

This session changed documentation only to preserve the finding for concurrent work. No app-code fix, new build, signing change, wake-word change, automated visual check or Windows synchronization is claimed. Existing v44 code/artifact references below are unchanged; the shrinking regression is unresolved.

## Current result: signed v44 test candidate

Built code `4044ee55b5a39e2a220ee897de0393b796c29a5f`, versionCode 44 / `1.1.1-unified-eyes-wake-home`. Workflow `34192698906`, job `101953848892`, completed successfully. Artifact `BOOP-Unified` / `10042812867` was downloaded and its ZIP digest, built commit and extracted APK checksum verified.

APK SHA-256: `5c60b904d06d8a94ad3ab117e2da86c5726c91ff8f2ca40845a0f2266e9966f6`, 142048925 bytes. ZIP SHA-256: `ea5b6225c45a1cf1e4b22d4497ba181f8757f3de2a36cbe986c2263e373490f4`. Full receipt and verification scope: `docs/BOOP-V44-BUILD-RECEIPT.md`.

CI passed 52 focused Shield and 59 focused unified/wake tests, no failures/errors/skips, plus non-visual integration contracts, Launcher lint, compilation, signing and package/archive checks. No emulator, visual or physical acceptance was run. This is ready for Ryan's physical testing, not a physically accepted replacement checkpoint.

## Concurrency and included changes

The code is a non-forced descendant of concurrent `dcc7acf32acd2cf44d25e1f88fce7d388695e030`. Its room/iris improvements were preserved rather than overwritten. Predecessor `848a997` passed run `34190645576`; shared main was read at `382d7b75d7db27d533ecf6079859f549397568e7`.

Home owns the actionable Room picker and supported controllable devices; Settings contains configuration only. No Favourites, helpers or diagnostic clutter. Keep fixed label padding, D-pad access and per-device focus retained through HA confirmation. `HaEntityCategory` handles keyed/array/literal category metadata, unknown values fail closed, and `RoomDeviceControls` admits available on/off lights, switches and fans. Local room filtering retains target expansion/device-inherited area membership without HA configuration writes.

`BoopIrisTint`/`BoopIrisTintMath` with `patch-unified-iris-cache.py` restrict phone hue to the original iris ring, keep default original blue and reuse a bitmap/small tile. Existing animation methods remain intact. Whites/reflections/outline appearance is for Ryan to accept.

Existing model-type-aware UNIGRAM tokenizer and custom-stream creation fallback are included, unlike delivered `6cd9c67`. Their tests/presence do not prove the exact cause of the physical wake failure or actual acoustic success.

Shield now uses the approved phone PNG and canonical `BoopEyeLayout`/`BoopIdleBlink` helpers, copied into the generated Shield namespace by `patch-unified-shield-presentation.py`. TV corner framing uniformly fits the pair without independent stretching. The blink shares 183 ms duration, curve and 3-7 second scheduler. TV eligibility uses visibility, active display, eyes mode and power/animation state rather than window focus; hide/display-off/mode-change/detach cancels blink work. No input-focus ownership, microphone or HA socket was added. Locked headphones renderer and placement are untouched.

Build through `bash scripts/materialize-unified.sh`, not a hand-copied unpatched Shield tree. The dashboard adapter invokes the room/cached-iris adapters; the final presentation adapter shares phone eyes/blink. Local Python syntax and exact-full-source patch anchors were checked against Git blob `039f5ac1cf3dedb4b4bd00465d0e52271546ab98`; missing/duplicate anchors fail closed. These are code integration checks, not visual tests.

## Standing CI/manual split

Ryan broadened manual acceptance to remove automated device tests he can perform himself. Unified CI has NO emulator install/launch, screenshot/golden-image test, appearance/geometry/animation judgement or aesthetic source guard. The old optional post-upload smoke is removed too. Do not re-enable without his explicit reversal. Keep focused functional/integration tests, lint/compilation, package/signer/security/integrity checks and immediate artifact upload. Documentation-only pushes do not rebuild or cancel the candidate. Historical test files remain available but are not selected.

## Physical evidence and next step

On delivered `6cd9c67`, Ryan saw and liked cyan Shield Settings, but devices were in Settings/unreachable, and Home focus was old/shifted labels. Pixel 7 survived his Android 17 update with media/blink/hue working, but neither custom nor BOOP wake worked after rename. Dock state/logs were not supplied. Do not generalise to complete OS compatibility or a proven stream exception.

Next is Ryan's v44 hardware test: Home room selection/actions; all Settings rows and D-pad focus/scroll; exact eyes/blink/iris; Enable/access-screen flow; typed/verbal rename/reset plus BOOP fallback foreground on wireless dock. Undocked is deliberately tap-to-talk. Heat/mic release/acoustic accuracy and permission-window behaviour remain unverified physically. No automatic installation or grants.

## Rollback/provenance

Last delivered v43 was `6cd9c67a03c639a20acde892e2d57186652e13d5`, run `34125882296`, artifact `10020439707`, APK SHA-256 `603e72b6f3a83eca429e90a11559454ca2d9c140eee69bff9bcd537d9a276a4e`, ZIP SHA-256 `455cd406beb111ac6d5d1d974b20bd54735a65222aa702612476929efd4ad285`. It is not an accepted wake-name release.

Protected physical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`. Original history is in `docs/history/unified-v43/`, `unified/SOURCE_HEADS.md` and `docs/BOOP-UNIFIED-WAKE-NAME-RECHECK.md`. No checkpoint was repointed. Documentation after the code commit does not change the APK.

No microphone policy, permissions, target SDK, application/HA/pairing/signing identity or launcher behaviour changed in v44. Windows checkout/receipt paths are not mounted here; connected GitHub publication does not mean Windows synchronization. No unattended monitoring was established.
