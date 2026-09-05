# BOOP Status

## Current
BOOP Wall resurrection is implemented on branch `boop-wall-resurrection` at
`c9a7d07f60d0ed7cd2c96247c28552c1d3da10f3`. It restores the Alpha 6.5.5
puppet and local-first control lineage, preserves all 33 natural wake phrases,
prefers the current Home Assistant conversation agent named `BOOP`, retains
direct media control, and integrates the separate Shield QR companion plus the
stable BOOP development signer. Timed voice routines remain deliberately absent.

Local source verification: 121 Python tests pass. GitHub CI has not yet run for
this branch. Physical Pixel 7 Pro status: **NOT YET TESTED**.

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

## Next
- Push `boop-wall-resurrection` and require the complete named GitHub Actions
  workflow to pass before presenting its signed APK for installation
- Install the exact CI-green APK on the Pixel 7 Pro and run the physical Wall
  acceptance checklist
- Keep timed voice routines excluded pending a separate redesign
- Do not create a BOOP Wall checkpoint until the signed APK passes the physical
  acceptance gate on the Pixel 7 Pro

## Do Not Touch
- checkpoint-shield-home-f8e8135
- checkpoint-shield-routines-3fa18c6
- HomeAssistantRepository unless a failing regression proves a change is required
- FocusCardView unless a failing regression proves a change is required
- BoopOverlayService / protected overlay runtime behaviour
- Do not create a second Home Assistant socket for Routines

## Active Branch
boop-wall-resurrection

## BOOP Wall Implementation Head
- c9a7d07f60d0ed7cd2c96247c28552c1d3da10f3 — ci: require actual wake microphone capture before launch passes
- Version: `versionCode 29`, `versionName 0.4.9-alpha6.5.6-wall`
- Physical status: **NOT YET TESTED**

## Latest Physically Green Routines Code Head
- 3fa18c6 — fix: confirm Shield automation starts

## Latest Checkpoints
- checkpoint-shield-home-f8e8135 — physically verified Shield Home
- checkpoint-shield-routines-3fa18c6 — signed and physically verified Shield Routines v1
- checkpoint-shield-routines-design-57652fd — approved Routines design
- checkpoint-shield-routines-plan-775f3fd — approved Routines implementation plan
