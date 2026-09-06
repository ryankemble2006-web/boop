# Launcher branch current status

Launcher Alpha 1 installs on Ryan's phone and can pin apps. A physical hold/drag
regression was reported; the old signed APK reproduced its premature menu in run
34060777148. Alpha 1.1 signed build, lint, 10 unit tests and 15 emulator checks pass at
source 41f250d, run 34062128943. Physical acceptance of this update is pending.
See SESSION_HANDOFF.md for current Launcher state. The inherited Shield status
below is historical reference, not Launcher work or permission to change Shield.

---

# BOOP Status

## Current
Routines v1 implementation in progress.
Task 5 TV routines list is the active task.

## Green
- Shield Home launch/pairing/room discovery
- Real Home Assistant favourite discovery
- Remote navigation
- Physical Home Assistant control
- Real state-change confirmation
- Tasks 1–4 of Routines v1

## Next
- Finish Task 5 TV routines list
- Task 6 Activity wiring
- Task 7 fresh CI + Shield sofa verification

## Do Not Touch
- checkpoint-shield-home-f8e8135
- HomeAssistantRepository unless explicitly required
- FocusCardView unless a regression proves it is necessary
- BoopOverlayService protected overlay behaviour

## Latest Checkpoint
checkpoint-shield-routines-plan-775f3fd
