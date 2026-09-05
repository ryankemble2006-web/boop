# BOOP Status

## Current
Routines v1 is wired end to end on the existing authenticated Home Assistant WebSocket.
The current compatibility correction restores the original BOOP contract: Home Assistant automations, scripts and scenes all appear as `Routine`, and automations run through exact-target `automation.trigger`. Fresh CI and Shield sofa verification are still required.

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

## Next
- Run the full source/unit/build gates in fresh Shield CI for the automation-compatible commit
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

## Latest CI-Green Code Head Before Automation Compatibility Correction
- 9cc9b68 — feat: wire Shield routines dashboard

## Latest Checkpoints
- checkpoint-shield-home-f8e8135 — physically verified Shield Home
- checkpoint-shield-routines-design-57652fd — approved Routines design
- checkpoint-shield-routines-plan-775f3fd — approved Routines implementation plan
