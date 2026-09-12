# Canonical Production Animation Design

## Goal
Make the finished BOOP Animation Lab the production animation engine inside Unified. Preserve the lab catalogue, renderer, timing curves, locked eye master, yellow-hands/sign work, Freddie motion, and approved assets exactly. Do not redraw, regenerate, reinterpret, or replace artwork.

## Current problem
Unified contains the finished canonical Animation Lab and all 26 labelled clips, but production routing only reaches a subset. The lab currently behaves mainly as a developer-preview activity. Production BOOP therefore falls back to older bespoke animation/resource paths, including the broken Now Playing puppet path.

## Architecture
1. Treat `unified/animation/` plus its approved assets as the canonical animation implementation.
2. Add one production-facing controller/router that owns clip selection and exposes the full catalogue by stable clip ID.
3. Route real Unified states/events into canonical clips instead of duplicating eye/motion logic in feature-specific code.
4. Preserve the developer Animation Lab as a manual preview surface, but make it consume the same canonical engine as production.
5. Keep non-animation application logic separate. Media, wake/voice, notifications, Startup Manager, HA control, launcher behavior, signing, and permissions must not change except where they invoke animation states.

## Production state mapping
Use existing labels and meanings without inventing new animation content:
- ambient/neutral -> `idle` and ambient blink behavior
- explicit blink gestures -> `blink`, `double_blink`, `wink_left`, `wink_right`
- lifecycle -> `wake`, `sleep`, `reset`
- voice/thought -> `listening`, `reading`, `thinking`
- Berry -> `berry_remember`, `berry_curious`, `berry_cheeky`
- media -> `music`, `media_pause`, `track_change`, `cinema`
- reactions -> `shake_reaction`, `curious`, `skeptical`, `success`, `confused`
- directional attention -> `attention_left`, `attention_right`
- notification presentation -> `notification` plus existing sign/Freddie renderer paths

Where Unified has no legitimate existing event for a clip, expose it through the canonical production controller and developer preview but do not invent product behavior merely to force usage. The requirement is that every clip is production-capable and no clip is stranded in a separate lab-only engine.

## Media / Now Playing
Replace the old bespoke Now Playing animation/resource route with the canonical media clips. Music playback uses `music`; pause uses `media_pause`; track changes trigger `track_change`; video/cinema state uses `cinema` where the existing media state can distinguish it. Preserve approved art bytes and existing playback functionality.

## Data flow
Existing app event/state -> canonical production animation router -> shared `EyeMotion.Controller` / sign or Freddie motion -> canonical renderer -> locked approved eye/hand assets.

One-shot clips return to the appropriate steady state after completion. Looping clips remain active only while their owning runtime state remains active. Lifecycle pause/resume freezes canonical animation time rather than catching up.

## Compatibility and safety
- Package remains `com.boop.alpha1`.
- Permanent GitHub signing workflow is unchanged.
- No new permissions, network calls, cloud dependencies, microphone behavior, or HA behavior.
- Preserve Startup Manager work already on the base branch.
- Preserve exact locked eye master and approved notification hands hashes.
- No automated visual/golden tests. Ryan owns physical visual acceptance.

## Testing
Use TDD for routing/controller behavior. Add non-visual tests that prove:
- all 26 stable clip IDs are reachable through the canonical production controller;
- expected runtime states map to canonical IDs;
- media transitions select music/pause/track-change/cinema correctly;
- one-shot clips return to the prior/appropriate steady state;
- pause/resume preserves animation time semantics;
- materialization puts the canonical engine and assets into the signed Unified APK;
- no old production path can silently override the canonical selection for the migrated states.

Then run the existing Unified unit/build/integrity gates and GitHub permanent-signing workflow. Physical appearance, navigation, and animation quality remain manual Shield acceptance.

## Delivery
Develop on isolated branch `boop-canonical-production-animation`, preserving concurrent worktrees. Before deployment, re-read the installed Shield version, preserve its rollback APK, choose a higher version, build/sign through GitHub, verify package/version/signer/hash, install, launch, and hand Ryan the physical acceptance step. Update handoff/status/memory with exact receipts and verify live remote HEAD before claiming sync.
