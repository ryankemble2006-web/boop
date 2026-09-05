# BOOP Status

## Current
BOOP Wall resurrection is physically green on branch `boop-wall-resurrection`
at code head
`595e1daa43393882a0e5de43967545ac526b8b66`. It restores the Alpha 6.5.5
puppet and local-first control lineage, preserves all 33 natural wake phrases,
prefers the current Home Assistant conversation agent named `BOOP`, retains
direct media control, and integrates the separate Shield QR companion plus the
stable BOOP development signer. Timed voice routines remain deliberately absent.

Local source verification: 122 Python tests pass. GitHub Actions run
`33992704568` passed every job, including Android unit tests, exact package and
version inspection, stable signing, real wake-microphone capture, launch
survival, and the separate Shield pairing return route. The exact signed APK
then passed the complete Pixel 7 Pro acceptance gate on 2026-09-05.

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
- Eyes, tap-to-talk, all representative natural wake phrases, continuous wake-plus-command speech, and wake rearming physically verified
- Ordinary questions physically verified through the Home Assistant conversation agent named `BOOP`
- Local room-scoped lights plus play, pause and skip physically verified
- With OpenCode stopped, ordinary conversation failed plainly while local lights and media remained fully functional; OpenCode then restarted and conversation recovered
- Timed speech failed plainly without the retired `Once or recurring?` follow-up
- Fresh-data Shield QR pairing, phone login, room selection and return to BOOP physically verified
- Shake-to-eye-bounce physically verified at the current firm threshold

## Next
- Refine shake sensitivity so removal from the magnetic base is enough to trigger
  the visual response, then explore further eye animations
- Keep timed voice routines excluded pending a separate redesign
- Preserve the physically verified BOOP Wall checkpoint before animation or
  sensor-threshold experiments

## Do Not Touch
- checkpoint-shield-home-f8e8135
- checkpoint-shield-routines-3fa18c6
- checkpoint-boop-wall-595e1da
- HomeAssistantRepository unless a failing regression proves a change is required
- FocusCardView unless a failing regression proves a change is required
- BoopOverlayService / protected overlay runtime behaviour
- Do not create a second Home Assistant socket for Routines

## Active Branch
boop-wall-resurrection

## BOOP Wall CI-Green Head
- 595e1daa43393882a0e5de43967545ac526b8b66 — ci: select supplied-audio recognizer for wake gate
- Version: `versionCode 29`, `versionName 0.4.9-alpha6.5.6-wall`
- Package: `com.boop.alpha1`
- Workflow: https://github.com/ryankemble2006-web/boop/actions/runs/33992704568
- APK SHA-256: `79ac40677687c4225989fa095644d5d97d36876150afe704e80eb6091d55530b`
- Signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Physical status: **GREEN on Pixel 7 Pro (2026-09-05)**

## Latest Physically Green BOOP Wall Code Head
- 595e1da — ci: select supplied-audio recognizer for wake gate

## Latest Physically Green Routines Code Head
- 3fa18c6 — fix: confirm Shield automation starts

## Latest Checkpoints
- checkpoint-boop-wall-595e1da — signed and physically verified BOOP Wall resurrection
- checkpoint-shield-home-f8e8135 — physically verified Shield Home
- checkpoint-shield-routines-3fa18c6 — signed and physically verified Shield Routines v1
- checkpoint-shield-routines-design-57652fd — approved Routines design
- checkpoint-shield-routines-plan-775f3fd — approved Routines implementation plan
