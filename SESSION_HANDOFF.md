# Current handoff: v160 GitHub timing gate and signed build verified

Updated 2026-09-13. Ryan explicitly asked to continue in GitHub, not on the laptop. This continuation performed GitHub source review, test edits, CI/build changes and permanent-signed artifact verification only. No laptop command, device input, emulator launch, installation or permission change was attempted. The earlier blocked laptop request was not retried or routed through GitHub.

## Current source and result

Owner remains `boop-unified-eye-sync-safe-v159`. Engineering/build commit: `1d8bf3d39a0858aa0c4f2b435fc92b1b16ef486a`. The app implementation remains the published `d149cb509ec376779daf84c50f621d8adcbacd24`, v160 / `1.2.160-colour-animation-speed`. This continuation changed tests and added five lines to the build workflow, not app code.

Full GitHub build `34769075927`, job `103755257333`: SUCCESS, including the new mandatory speed/source gate, permanent signing, package/archive verification and artifact upload. Appearance run `34769075918`: SUCCESS. Artifact `10321686042` / `BOOP-Unified` is tied to the exact engineering commit above.

APK SHA256: `a584381d10ebb220ab686acec22f7668384e240f39ade052e4177583aa4adc7b`. It is byte-identical to the previously recorded v160 candidate. Signer receipt: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. The downloaded artifact archive and extracted APK hashes were independently matched to their receipts in the chat sandbox, not on Ryan's laptop. Artifact verification reports 235 Unified and 68 Shield functional tests, with zero failures, errors or skips.

Read `docs/handoffs/2026-09-13-speed-github-verification.md` for the red/green evidence, exact changes, artifact identity, limits and next steps. Original speed CI/build evidence and original artifact `10318393790` remain preserved; this is not a new implementation or replacement signing key.

## What changed

The full APK build previously did not require the separate speed suite before signing. Test-first commit `ca6cd34af50c42dfb5606053b5d27fde7128eaea` produced the expected missing-gate failure in timing run `34768979364`: one failed, four passed. The red checkpoint is preserved at `wip/boop-speed-gate-red-ca6cd34`.

The five-line workflow repair makes the existing and expanded numerical speed checks plus materialized-library source identity checks mandatory before signer preparation. Expanded coverage compares original 1x and scaled authored poses, irregular fractional time, one-shot completion boundaries, notification hand/eye channels and pause/rate/resume compatibility. No visual judgement or Android runtime test runs in GitHub.

## Accepted colour and protected device state

Ryan confirmed: "it works btw, i just tested eye colour :) from wall to shield". Wall -> Shield remains physically accepted. Prior controlled captures also showed Shield -> Pixel 7 purple at hue260 and Pixel 7 -> Shield green at hue122. Do not reopen colour repair or restore old fixtures.

Last recorded physical state, not re-queried this turn: Shield and Pixel 7 run v159 / `1.2.159-shared-eye-colour`, source `0a4134ebfe8049254378b4706d2ee1df73cd7e87`, APK SHA256 `80e86119d4771624ff47617373df2cebc03c0454aa47f10a0996a6d507b68353`; both had sharing on and user hue2. Physical Pixel 10 remains excluded and untouched.

## Remaining work and next safe step

Speed is implemented and freshly GitHub-verified, NOT runtime/physically accepted. Physical v160 deployment remains held. When permitted and within Ryan's current scope, validate the same candidate on the existing laptop emulators: four speeds, mid-clip changes, sleep/wake, notification hand/eye timing, pause/resume and zero Android animation scales. Then authorized Shield/Pixel 7 device-specific checks. Do not substitute hosted visual tests or physical deployment for a blocked laptop request.

Source-review follow-up: the raw embedded Lab activity still derives reducedMotion from Android ANIMATOR_DURATION_SCALE==0, whereas the current scale-independence contract only checks Wall. Trace the materialized Lab and add a focused regression before claiming Lab scale-zero independence or changing its integration. This is a source-level concern, not a reproduced device failure; authored motion must remain untouched.

A deliberate two-device colour offline/reconnect cycle remains separate coverage, not grounds to reopen accepted live delivery. Existing narrower Shield-to-HA rejoin evidence is preserved.

## Continuity and boundaries

Preserve approved eye/hand masters, shaders, authored animation, exact 1x, working Wall hue controls, accepted v156 Shield polish and single-face ownership. No unrelated lyrics/Johnny merges, permission changes, lock bypass or signing substitutions. This session is primary. Main remained `b7d3eb6ea5e1189b45bd3ed4ecf685723613464d` when checked; no shared ownership change requires a main update.

The historical dirty laptop worktree was not read or changed this turn and is not claimed synchronized. Earlier private runtime captures stay local. Older detailed handovers remain at `docs/handoffs/2026-09-13-colour-accepted-wall-shield.md` and `docs/handoffs/2026-09-13-colour-failure-continuation.md`; the latter is research/provenance, not current acceptance state. No queued device input or scheduled monitoring exists.
