# Unified memory: accepted colour and preserved v160 speed

2026-09-13. Ryan redirected continuation to GitHub, not local work. Source/tests/build/permanent signing proceeded through the GitHub connector. No laptop command or device test was attempted in this continuation. The prior denied local request is not permission to move device execution into a GitHub workflow.

Colour is accepted: Ryan said "it works btw, i just tested eye colour :) from wall to shield". This supersedes the old failure blocker. Earlier controlled screenshots show Shield -> Pixel 7 purple at260 and Pixel 7 -> Shield green at122. Last read-only physical receipt showed both on user hue2 with sharing on. Never reset those to historical fixture values or turn sharing off after acceptance. Offline/reconnect coverage remains separate, not a reapproval loop.

Branch `boop-unified-eye-sync-safe-v159`; v160 app code remains `d149cb509ec376779daf84c50f621d8adcbacd24`. Latest verified engineering/build commit `1d8bf3d39a0858aa0c4f2b435fc92b1b16ef486a` changes only tests and a five-line build gate. APK bytes match the prior v160 candidate exactly.

New evidence: full build `34769075927` and appearance `34769075918` SUCCESS. Artifact `10321686042` / `BOOP-Unified`, APK SHA256 `a584381d10ebb220ab686acec22f7668384e240f39ade052e4177583aa4adc7b`, unchanged permanent signer. Build verification reports 235 Unified and 68 Shield functional tests, zero failures/errors/skips. Expanded numerical and materialized-library preservation gates also passed. See `docs/handoffs/2026-09-13-speed-github-verification.md`.

The gate was test-first: `ca6cd34af50c42dfb5606053b5d27fde7128eaea`, run `34768979364`, failed only because full signing did not require timing/source tests. Four other tests passed. Red evidence preserved on `wip/boop-speed-gate-red-ca6cd34`. Fix makes timing and copied-source checks mandatory before preparing the signer; it does not change keys or permissions. The superseded red-commit full build was cancelled before signing.

Numerical scope: exact v156 baseline poses, .5/1/1.5/2 rates, fractional/irregular monotonic time, phase-continuous changes, one-shot boundaries, four sign styles with all hand/eye channels, and pause/rate/resume baseline compatibility. These are not Android lifecycle or visual tests. Runtime and physical speed acceptance remain pending, and no v160 physical deployment occurred.

Outstanding source-review concern: raw BoopCanonicalAnimationActivity still consults Android animator-duration-scale for reducedMotion; the current scale-independence test only checks Wall. Trace the built Lab path and use a focused failing regression before any integration fix. Preserve authored animation and exact1x rather than silently weakening the zero-scale requirement.

Physical Shield/Pixel 7 remain last recorded on v159 source `0a4134ebfe8049254378b4706d2ee1df73cd7e87`, APK SHA256 `80e86119d4771624ff47617373df2cebc03c0454aa47f10a0996a6d507b68353`. Existing main emulators were previously found on v160; their current running/reachability state was not queried. Physical Pixel10 stays excluded.

Preserve eye/hand masters, shaders, coded clips, exact1x, local hue store boop_eyes/hue_degrees0..359/default190, accepted Wall controls, accepted v156 Shield polish and single-face ownership. Sharing remains opt-in and authenticated HA, speed device-local. No permission changes, lock bypass, data clear, signing replacement, regenerated images or unrelated lyrics/Johnny merge.

This session remains primary. The historical dirty laptop worktree was left alone and is not claimed synced. Earlier raw device captures remain private/local. Publish material handoff/status/memory updates and verify live GitHub HEAD. Main needs no ordinary-progress update. No queued device inputs, recurring monitors or background assistant work are implied.
