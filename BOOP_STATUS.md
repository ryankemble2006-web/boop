# BOOP status: v206 UI accepted; Voice prerequisite remains open

Updated 2026-09-16. Owner: `boop-hand-colour-v191`.

## Current installed identities, freshly read

| Target | Installed package | Version | APK SHA-256 |
| --- | --- | --- | --- |
| Nvidia Shield | `com.boop.alpha1` | `206` / `1.2.206-idle-home-corner` | `b5f7b0570eccb171bcda7a2ad4e58e79cb397768ec12851e110f04b238713bca` |
| Pixel 7 Pro | `com.boop.alpha1` | `191` / `1.2.191-hand-colour` | `bfc1204c5da030a94991af021a0d0c4f85adb01b22d28825ce68fa7a065843f1` |

Physical Pixel 10 was not targeted. No install, uninstall, data clear, setup navigation, permission grant or Home reassignment was performed in this review.

## Evidence levels

**Source/build:** latest built application source `9d57019d9370dbe3f47061b6e8b0ce8ed5134715`. Signed run `35099151524` was rechecked: success for all reported steps, including functional regressions, Android compilation, permanent signer and APK identity. Artifact `10447197742`, `BOOP-Unified-v206-Idle-Home-Corner`. No new build/test was run for this documentation-only reconciliation.

**Installation:** fresh Shield APK hash matches the v206 build/install receipt. Pixel 7 is still v191, so current Voice work is not established there by these readings.

**Human acceptance:** Ryan explicitly confirms v206 as the working baseline and its UI as accepted. Preserve the accepted layout and controls without another UI/focus acceptance gate. Earlier voice testing reported natural voices present and Pitch/Cadence affecting speech, but long Try Emma / TEST VOICE delays. Current v206 voice response-delay acceptance and cross-device voice-sharing verification remain open; UI sign-off does not establish either. Do not reset that earlier voice progress or claim a new audio test happened here.

## Split status

Authorized next task, gated by completion, joint acceptance and preservation of current voice/UI work. No split source edits or replacement APKs were produced in this review. Both devices retain their existing setup; neither is at newcomer setup step one.

Future Wall: `com.boop.alpha1` on Pixel 7 Pro. Future Shield: `com.boop.shieldoverlay` on Nvidia Shield. Preserve latest combined source and genuinely shared assistant functionality. No old standalone-code rollback and no device/profile chooser. Details: `docs/handoffs/2026-09-16-wall-shield-split-prerequisites.md`.

## Uninstall safety still pending

Shield HOME currently resolves to Unified; the only other enabled HOME activity queried was Android settings `FallbackHome`. Do not call that a verified replacement launcher or silently assign another Home. Both replacement APKs, suitable recovery APKs and a usable recovery route are required before removing anything.

## Preservation

The local owning worktree is stale and has dirty v203 documentation; it remains untouched. Complete prior status and memory stacks are archived without byte changes in `docs/handoffs/2026-09-16-pre-split-archive/`. Follow `SESSION_HANDOFF.md` for current evidence and dated receipts for history.
