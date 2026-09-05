# BOOP Status

## Current
Routines v1 implementation is at the Task 5 -> Task 6 boundary.
The real scrollable TV routines list exists and its compatibility fix is in place; Activity composition on the existing authenticated Home Assistant WebSocket is the next production task.

## Green
- Shield Home launch/pairing/room discovery
- Real Home Assistant favourite discovery
- Remote navigation
- Physical Home Assistant control
- Real `state_changed` confirmation
- Routines Task 1: whole-house script/scene discovery
- Routines Task 2: scene execution
- Routines Task 3: truthful script completion using exact-target `on -> off`
- Routines Task 4: controller state, timers, timeout and concurrency
- Routines Task 5: scrollable remote-first TV routines list implemented

## Next
- Task 6: wire RoutinesRepository + RoutinesController + TvRoutinesView into BoopHomeActivity using the existing authenticated Home Assistant WebSocket
- Run the full local/source/unit/build gates
- Task 7: fresh Shield CI for the exact implementation commit
- Install the fresh APK and complete the Shield sofa verification checklist
- Create a Routines functional checkpoint only after physical green
- Then return to the Pixel Alpha and rebuild its setup around the newer QR pairing/authentication flow

## Do Not Touch
- checkpoint-shield-home-f8e8135
- HomeAssistantRepository unless a failing regression proves a change is required
- FocusCardView unless a failing regression proves a change is required
- BoopOverlayService / protected overlay runtime behaviour
- Do not create a second Home Assistant socket for Routines

## Active Branch
boop-shield-home-implementation

## Latest Code Head Before Task 6
- a7db366 — fix: preserve routines view shell compatibility

## Latest Checkpoints
- checkpoint-shield-home-f8e8135 — physically verified Shield Home
- checkpoint-shield-routines-design-57652fd — approved Routines design
- checkpoint-shield-routines-plan-775f3fd — approved Routines implementation plan
