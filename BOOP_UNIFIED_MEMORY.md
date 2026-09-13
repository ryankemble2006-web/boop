# Current Unified continuity, 2026-09-13

The Shield sweep is finished and accepted through v156 `a901c1e9f31e55c710e31ac7ff4f5924c9769d56`. Do not ask Ryan to repeat the accepted corners, 250 ms hold, seek/navigation/bay, favourites or artist-link checks.

Priority order: shared eye colour, then animation speed. Preserve Wall's existing `boop_eyes/hue_degrees` (0..359, default 190), existing slider behavior, iris-only shader and approved masters. Never replace animation definitions or shorten baseline 1x timings.

Use GitHub as source of truth and for build/test/signing. Local emulators/Shield handle runtime/visual checks. Pixel 10 stays untouched. Do not treat transient response-resource IDs as durable file addresses; use live refs and commit-pinned paths.

Research and interrupted drafts are preserved in `wip/boop-colour-v158-recovery@484d292f` and `wip/boop-colour-v159-recovery@7ea7d26d`. These are NOT deployable. v159 has protocol/state tests and lifecycle colour bindings, but a truncated runtime and no finished settings/app wiring. The old puppet-integration UDP transport was unauthenticated and also retimed blink/voice; do not merge it. Prefer existing authenticated HA with explicit opt-in, no startup default overwrites, offline local fallback and no new Android permissions.

See SESSION_HANDOFF.md for exact acceptance/build receipts. Earlier detailed memory remains in `a901c1e9:BOOP_UNIFIED_MEMORY.md` and Git history. No app code was changed for this update.
