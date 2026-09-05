# BOOP Status

## Current
Routines v1 is wired end to end on the existing authenticated Home Assistant WebSocket.
The compatibility correction restores the original BOOP contract: Home Assistant automations, scripts and scenes all appear as `Routine`, and automations run through exact-target `automation.trigger`. Routines v1 is physically green on signed Shield build `3fa18c6`: all three available automations discover and run, feedback shows `Running…` -> `Done` -> the normal `Routine` row, remote navigation is correct, and the protected Home favourite path remains healthy. This Home Assistant currently exposes no script or scene routines, and three rows do not require scrolling, so those physical cases were not applicable.

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
- Return to the Pixel Alpha and rebuild its setup around the newer QR pairing/authentication flow

## Do Not Touch
- checkpoint-shield-home-f8e8135
- checkpoint-shield-routines-3fa18c6
- HomeAssistantRepository unless a failing regression proves a change is required
- FocusCardView unless a failing regression proves a change is required
- BoopOverlayService / protected overlay runtime behaviour
- Do not create a second Home Assistant socket for Routines

## Active Branch
boop-shield-home-implementation

## Latest Physically Green Routines Code Head
- 3fa18c6 — fix: confirm Shield automation starts

## Latest Checkpoints
- checkpoint-shield-home-f8e8135 — physically verified Shield Home
- checkpoint-shield-routines-3fa18c6 — signed and physically verified Shield Routines v1
- checkpoint-shield-routines-design-57652fd — approved Routines design
- checkpoint-shield-routines-plan-775f3fd — approved Routines implementation plan
