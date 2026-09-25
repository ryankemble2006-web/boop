# Pocket BOOP polish implementation plan

> Execute inline with systematic debugging, test-driven development and one independent final review.

**Goal:** Fix reproducible clipping, accept natural music requests without memorised phrasing, and validate portable notification presentation.

**Architecture:** Preserve the split Shield/Wall shells and production artwork. Improve shared layouts and MediaRequest/DeezerCatalogue only where reproducible defects exist. Exercise the existing listener/runtime/overlay and foreground notification lab before changing notification policy.

**Approval:** Ryan approved autonomous emulator-first work, UI then commands then notifications, on 25 September. No repeated design checkpoints requested; proceed with routine implementation decisions and report material limitations.

## Constraints
- Work from live notification branch 2a983a1752d6ddb9d5e9b92d5ddafbb21433180c in isolated boop-pocket-polish-v251.
- Use Yoga emulators; no commands, installations or playback on the physical Shield/AV system during TV use.
- Preserve accepted felt artwork, five fingers, colours, voice settings and existing working playback.
- No general guessing of artists, alternate recordings or app commands. Genuine ambiguity must ask.
- Real-phone notification grants and physical acceptance are separate from emulator coverage.
- Do not read or recreate retired personality transfer files.

## Task 1: UI audit and clipping fixes
- [ ] Install accepted baseline in TV/Pixel emulators and record reachable screen inventory.
- [ ] Capture relevant screens at normal/large text and narrow/landscape sizes; reproduce clipped or unreachable controls.
- [ ] Fix source layout constraints, with behavioural regression checks where worthwhile. Verify real rendered screens and DPAD/Back navigation.
- [ ] Commit scoped changes and evidence record.

## Task 2: Natural commands
Files: unified/shared/MediaRequest.java, unified/DeezerCatalogue.java, tests/test_music_voice_selection.py, tests/java/MusicVoiceSelectionProbe.java and focused shared parser tests.
- [ ] Add failing tests for conversational requests, provider variants, artist/title order and artist possessives across unrelated catalogue fixtures; pin non-media rejection and ambiguity.
- [ ] Fix parsing/resolution at the earliest failing boundary, preserving catalogue identity verification, recording qualifiers, no replay, room/auth guards and existing Flow.
- [ ] Run actual Java production paths against deterministic metadata and playback boundaries. Use no physical playback.
- [ ] Commit independently.

## Task 3: Pocket notifications
Files: source/BoopNotification*.java, notification-lab, related tests and materialization scripts.
- [ ] Pair emulator sender/receiver and exercise 12 previews, Run all, Stop, background and re-entry.
- [ ] Test real Android notification intake from a disposable fixture app on the emulator, including overlay, grouping, removal, opening, lock privacy and disabled state.
- [ ] Fix reproduced runtime/permission/layout defects with regression coverage. Make supported portable settings discoverable without automatically granting live-phone access.
- [ ] Document Android limits and any physical acceptance remaining.

## Delivery
- [ ] Independent branch review; fix important findings and run appropriate checks.
- [ ] Signed GitHub build, verify source/signature/assets, install and retest emulators.
- [ ] Publish PR and updated factual handoff with evidence and remaining limitations.

## Ledger
- Full reset redeemed successfully; usage refreshed to 0% used.
- Native managed worktree creation cannot target the BOOP repo from this projectless task; used git worktree in task work/ directory, preserving existing dirty root.
- Baseline advanced explicitly from stale local tracking 8f27ddd4 to live 2a983a17 before edits.
