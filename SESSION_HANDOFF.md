# BOOP unified handoff

Updated 2026-09-08. Owner `boop-unified`, package `com.boop.alpha1`, existing permanent signer. Fresh main owns shared contracts; this handoff owns current implementation/evidence.

## Current candidate: v44, build in progress

Code commit `4044ee55b5a39e2a220ee897de0393b796c29a5f`, versionCode 44 / `1.1.1-unified-eyes-wake-home`. Workflow `34192698906`, job `101953848892`. At this update the non-visual integration contracts, unified materialization, SDK and Gradle setup passed; compilation/tests/signing/artifact verification are not yet claimed complete.

This commit is a non-forced descendant of concurrent room/iris repair `dcc7acf32acd2cf44d25e1f88fce7d388695e030`. Its 19-file improvements were preserved, not overwritten. Earlier base `848a99725b6d64d4fb80d74fc466bdff807dd41e` passed run `34190645576`, but is not the latest requested candidate. Shared main was read at `382d7b75d7db27d533ecf6079859f549397568e7`.

## Included changes and implementation path

- Home owns the actionable Room picker and supported room devices. Settings is configuration-only. No Favourites or helper/diagnostic clutter. Keep stable per-device rows, fixed label padding, D-pad movement and focus retention while HA confirms actions.
- `HaEntityCategory` handles HA's keyed compact category table, arrays and literal categories; unknown metadata fails closed. `RoomDeviceControls` admits available on/off lights, switches and fans. Local area filtering preserves HA target expansion/device-inherited membership. No area moves, entity renames or HA configuration writes.
- `BoopIrisTint`/`BoopIrisTintMath` and `patch-unified-iris-cache.py` restrict the phone hue transform to the original iris ring, preserve unselected pixels/default original blue, and reuse a tinted bitmap/small tile. Existing animation methods stay intact. Physical whites/highlights remain Ryan's acceptance check.
- Existing model-type-aware UNIGRAM tokenizer and custom stream creation fallback remain included. These were absent from the delivered v43 APK. Their tests/presence do not prove acoustic wake success or establish the exact cause of Ryan's reported failure.
- NEW: Shield eyes use the same approved PNG and canonical `BoopEyeLayout`/`BoopIdleBlink` helpers as the accepted phone body. `patch-unified-shield-presentation.py` copies helpers into the generated Shield namespace and replaces its independent eye layout path. The TV corner slot frames the canonical pair uniformly; no independent eye stretching or new artwork. Existing headphones renderer and placement are untouched.
- Shield idle blink uses the phone's 183 ms curve and 3-7 second scheduler. TV eligibility uses visibility, active display, eyes-only mode and power/animation policy, not window focus. Stop callbacks/animator on hide, display-off, mode switch or detach. Overlay stays non-focusable/non-touchable with no microphone or HA socket.

Run `bash scripts/materialize-unified.sh`, not an unpatched hand-copied library. The dashboard adapter invokes room-control and cached-iris integration; the final presentation adapter adds the canonical Shield eyes/blink. Python adapter syntax and exact-full-source patch anchors were checked locally. The source copy matched Git blob `039f5ac1cf3dedb4b4bd00465d0e52271546ab98`; duplicate/missing anchors fail closed. This is code wiring evidence, not visual acceptance or an Android runtime test.

## CI versus manual acceptance

Ryan expanded the manual-acceptance request to remove automated device tests he can perform himself. The unified workflow now has NO emulator install/launch, screenshot/golden-image test, appearance/geometry/animation judgement or aesthetic source-string guard. The older optional post-upload smoke is removed too. Do not re-enable without Ryan reversing this direction. Keep focused non-visual integration/control/wake tests, Launcher lint, compilation, package/signature/archive/security checks and immediate artifact upload. Documentation-only pushes do not rebuild or cancel this candidate.

## Latest physical evidence

Ryan tested delivered `6cd9c67`: new cyan Shield Settings was visible and liked, but device rows belonged on Home and were unreachable; Home had old focus styling and shifted labels. His Pixel 7 survived Android 17 with media/blink/colour controls working, but neither custom nor BOOP wake worked after rename. Dock state and device logs were not provided. Do not claim full Android 17 compatibility, a proven stream exception or a physical fix.

Required on v44: every Home/Settings row, room selection and device actions; stable focus/scroll; exact eyes and blink; iris-only colour; permission/access-screen flow; typed/verbal rename/reset and BOOP fallback while foreground and wirelessly docked. Undocked is deliberately tap-to-talk. Mic release/heat/dock, acoustic accuracy, appearance and installation remain manual. The previously reported Enable/access-window behaviour has not been physically resolved here.

## Rollback and provenance

Last delivered v43: `6cd9c67a03c639a20acde892e2d57186652e13d5`, run `34125882296`, artifact `10020439707`, APK SHA-256 `603e72b6f3a83eca429e90a11559454ca2d9c140eee69bff9bcd537d9a276a4e`, ZIP SHA-256 `455cd406beb111ac6d5d1d974b20bd54735a65222aa702612476929efd4ad285`. It is historical test evidence, not an accepted wake-name release.

Last physically accepted unified rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`. Preserve protected branches/tags. Original detailed history lives in `docs/history/unified-v43/`, `unified/SOURCE_HEADS.md` and `docs/BOOP-UNIFIED-WAKE-NAME-RECHECK.md`.

No microphone policy, permissions, Android target, application/HA/pairing/signing identity or launcher behaviour was changed in v44. No user device was installed or granted access. The Windows checkout/receipt paths are not mounted here; connected GitHub publication does not imply Windows synchronization. No unattended background monitoring is established.

Next safe step: inspect run `34192698906`, fix only evidenced build failures, verify downloaded artifact/built-commit/signature/checksum and deliver v44. Then record the exact final receipt here and in status/memory. Do not substitute an older APK.
