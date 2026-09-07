# BOOP Status

## Current
BOOP Wall resurrection remains physically green on protected checkpoint
`checkpoint-boop-wall-595e1da`, code
`595e1daa43393882a0e5de43967545ac526b8b66`, version 29 /
`0.4.9-alpha6.5.6-wall`, package `com.boop.alpha1`.

The current scoped eye-colour candidate lives separately on
`boop-wall-eye-hue-wip`. It was branched from live resurrection head
`3a702f89b7f317649d267f25c34c6c9655edcff8` and does not replace the physical
checkpoint. Candidate version is 31 / `0.4.11-wall-eye-hue`.

The candidate adds exactly one `Eye colour` hue slider beside the existing voice
settings. It spans 0–359 degrees, previews live, persists `hue_degrees` in
SharedPreferences and restores it when `BoopFaceView` is reconstructed. Both eyes
use the same existing `boop_eyes` bitmap and shared Paint. Default hue 190 uses
no ColorFilter at all, preserving the accepted cyan/blue pixels. Non-default
values apply hue rotation only. No artwork, geometry, crops, background, mouth,
voice behavior, wake/sleep behavior, thinking/shake/Member Berry behavior,
hitboxes or gesture code was redesigned.

GitHub Actions run `34089134515` passed for code head
`fc7391740c2d4e04b8fd357bcfe93dcb83e120b8`: 125 source/regression tests,
representative hue harness checks (orange/green/pink/purple/cyan plus default),
launcher-swipe/shake harnesses, bridge regression, Android unit tests, source
scope guards, signed build and signer verification.

Signed v31 candidate from that run:
- APK SHA-256: `39d91cd46cfbff815abf4ec4681f5fb3a3be971d6cec6f1ccf2e3bb9398a028d`
- signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- artifact: `BOOP-Wall-Eye-Hue-v31`
- physical status: **PENDING**

## Green
- Shield Home launch/pairing/room discovery
- Real Home Assistant favourite discovery
- Remote navigation
- Physical Home Assistant control
- Real `state_changed` confirmation
- Routines Task 1: whole-house automation/script/scene discovery
- Routines Task 2: scene execution
- Routines Task 3: truthful script completion using exact-target `on -> off`
- Routines Task 4: controller state, timers, timeout and concurrency
- Routines Task 5: scrollable remote-first TV routines list implemented
- Routines Task 6: Activity composition on the existing authenticated Home Assistant WebSocket
- Routines automation compatibility: all three existing Home Assistant automations physically discovered and activated from the Shield UI
- Exact-target automation start confirmation and `Running…` -> `Done` -> normal-row feedback physically verified on Shield build `3fa18c6`
- Routines remote navigation physically verified: Up/Down traversal, Left return to the Routines rail item, and stable row ordering all work without issue
- Protected Home regression physically verified: correct favourite changes, its real state appears almost immediately, and the 10-second confirmation safety window has ample margin
- BOOP Wall signed update installed without uninstalling or losing app data
- Eyes, tap-to-talk, all representative natural wake phrases, continuous wake-plus-command speech, and wake rearming physically verified on protected v29
- Ordinary questions physically verified through the Home Assistant conversation agent named `BOOP`
- Local room-scoped lights plus play, pause and skip physically verified
- With OpenCode stopped, ordinary conversation failed plainly while local lights and media remained fully functional; OpenCode then restarted and conversation recovered
- Timed speech failed plainly without the retired `Once or recurring?` follow-up
- Fresh-data Shield QR pairing, phone login, room selection and return to BOOP physically verified
- Shake-to-eye-bounce physically verified at the current firm threshold
- Wall v30 launcher swipe is emulator-green: left opens Launcher; right and vertical stay in Wall
- Wall v31 eye-hue candidate is CI-green with original artwork/default no-filter path and stable signing

## Next
- Physical Pixel 7 Pro acceptance for v31 eye-colour candidate:
  1. default blue/cyan appearance must match accepted Wall;
  2. drag slider through orange, pink and green and confirm both eyes preview live;
  3. leave a non-default hue selected, force-stop/restart app and confirm it persists;
  4. regression-check wake/sleep, thinking, shake, Member Berry, tap/hold and launcher swipe.
- Physical acceptance for v30 Wall-to-Launcher swipe remains folded into the same
  device pass because v31 is based on that exact v30 head.
- Refine shake sensitivity only in a later isolated experiment if still desired.
- Keep timed voice routines excluded pending a separate redesign.
- Preserve the physically verified BOOP Wall checkpoint before further animation
  or sensor-threshold experiments.

## Do Not Touch
- checkpoint-shield-home-f8e8135
- checkpoint-shield-routines-3fa18c6
- checkpoint-boop-wall-595e1da
- HomeAssistantRepository unless a failing regression proves a change is required
- FocusCardView unless a failing regression proves a change is required
- BoopOverlayService / protected overlay runtime behaviour
- Do not create a second Home Assistant socket for Routines
- Do not promote `boop-wall-eye-hue-wip` to the physical Wall checkpoint until the physical acceptance pass is green

## Active Branch
`boop-wall-eye-hue-wip` for this experiment.
`boop-wall-resurrection` remains the protected Wall lineage.

## BOOP Wall Eye Hue CI-Green Candidate
- Code head tested: `fc7391740c2d4e04b8fd357bcfe93dcb83e120b8`
- Version: `versionCode 31`, `versionName 0.4.11-wall-eye-hue`
- Package: `com.boop.alpha1`
- Workflow run: `34089134515`
- APK SHA-256: `39d91cd46cfbff815abf4ec4681f5fb3a3be971d6cec6f1ccf2e3bb9398a028d`
- Signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Physical status: **PENDING**

## BOOP Wall Physically Green Head
- 595e1daa43393882a0e5de43967545ac526b8b66 — ci: select supplied-audio recognizer for wake gate
- Version: `versionCode 29`, `versionName 0.4.9-alpha6.5.6-wall`
- Package: `com.boop.alpha1`
- Workflow: `33992704568`
- APK SHA-256: `79ac40677687c4225989fa095644d5d97d36876150afe704e80eb6091d55530b`
- Signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Physical status: **GREEN on Pixel 7 Pro (2026-09-05)**

## Latest Physically Green Routines Code Head
- 3fa18c6 — fix: confirm Shield automation starts

## Latest Checkpoints
- checkpoint-boop-wall-595e1da — signed and physically verified BOOP Wall resurrection
- checkpoint-shield-home-f8e8135 — physically verified Shield Home
- checkpoint-shield-routines-3fa18c6 — signed and physically verified Shield Routines v1
- checkpoint-shield-routines-design-57652fd — approved Routines design
- checkpoint-shield-routines-plan-775f3fd — approved Routines implementation plan
