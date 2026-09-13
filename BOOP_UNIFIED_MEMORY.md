# Unified memory: accepted colour, v161 Lab scale-zero repair

2026-09-13. Ryan's latest "do it" authorized continuing the focused Lab check within the previously requested GitHub-only scope. GitHub owns source edits, numerical/logic tests, builds and permanent signing. No laptop/emulator/device execution occurred in this continuation, and the previous denied laptop request was not retried or moved into a hosted Android test.

## Current verified candidate

Owner branch `boop-unified-eye-sync-safe-v159`; engineering/build commit `0b6ee6f91e05f00138a94ec2c9fd846117020754`, version161 / `1.2.161-lab-scale-independent`, package `com.boop.alpha1`.

The v160 speed implementation is preserved. Only Lab `onResume` stops interpreting zero Android animator scale as reduced motion; the existing power-saver condition remains. The unused Settings import is removed, a comment explains the clock boundary, and app-build.gradle changes version only. No frame-clock/keyframe/blink/renderer/colour/artwork/permission/signing changes.

Standalone timing `34770388848`, appearance `34770388845` and full permanent-signed build `34770388933` succeeded. Artifact `10321956422`, APK SHA256 `c68b81be9b7d3e10883d2aea52c05dc7b8c187fcd835eaddf73e25e91cb4acd6`, unchanged signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Detailed receipt: `docs/handoffs/2026-09-13-lab-scale-zero-verification.md`.

## Why and what was proved

Original Lab source and final materialized source both set reducedMotion from Android ANIMATOR_DURATION_SCALE==0. That forced fixed/end poses instead of the advancing BOOP clock. Test-first `fc9e6633ca7b578b7729cc5ee0e134294969075a` failed the new Lab sign assertion at scale0/speed0.5/style0; nonzero control scales passed. Timing `34770049893`: one failed, five passed. Full `34770049896` reproduced both source paths and stopped before signer preparation. Red checkpoint preserved at `wip/boop-lab-scale-zero-red-fc9e663`.

New host harness extracts actual field/callback bodies verbatim into test-only platform adapters and runs real motion/sign math. Green full build passed 20920 numeric checks on raw callbacks and 20920 on materialized callbacks, along with the existing 160720 timing and 1157272 edge checks. All six speed test functions, materialized source identity, approved-master preservation and 235 Unified/68 Shield functional tests passed. Archive/APK hashes and all 13 asset-entry bytes were checked against receipts/v160 in the chat sandbox.

This is Java control-flow and pose-data evidence, not real Android scheduling, GL rendering, on-screen motion quality or physical acceptance. Do not replace pending runtime checks with these counts. Existing original1x, four speeds, pause/resume, manual Pause motion, Slow review, explicit freeze and power-saver behavior are retained by the scoped repair/tests.

## Accepted colour and devices

Ryan said "it works btw, i just tested eye colour :) from wall to shield". That acceptance supersedes the old colour failure. Prior controlled captures proved Shield -> Pixel7 purple at260 and Pixel7 -> Shield green at122; later read-only physical receipt found the user's newer hue2 with sharing on. Leave newer choices alone, never restore test fixtures or disable sharing after acceptance. Deliberate two-device offline/reconnect coverage remains separate.

Last recorded physical Shield/Pixel7 run v159 source `0a4134ebfe8049254378b4706d2ee1df73cd7e87`, APK SHA256 `80e86119d4771624ff47617373df2cebc03c0454aa47f10a0996a6d507b68353`. This turn neither queried nor changed them. Older main laptop emulators had v160, but their current state is unknown. Physical Pixel10 remains excluded. No v161 deployment occurred.

## Next and preservation

Next is permitted runtime/visual validation of exact v161 on laptop emulators, then authorized Shield/Pixel7 checks: all four rates, mid-clip changes, sleep/wake completion, hands/eyes, pause/resume and Android scales zero. Physical speed deployment remains held until those gates pass. Do not reopen the repaired Lab source concern or accepted colour without new contrary evidence.

Preserve approved eye/hand masters, shaders, all coded clips, exact1x, boop_eyes/hue_degrees range0..359/default190, existing Wall controls, accepted v156 Shield polish and single-face ownership. Colour sharing stays opt-in/authenticated HA; animation speed stays device-local. No permissions, locks, data clears, keys, private credentials or unrelated lyrics/Johnny work are in scope.

Keep prior v160 source `d149cb509ec376779daf84c50f621d8adcbacd24`, signed gate build source `1d8bf3d39a0858aa0c4f2b435fc92b1b16ef486a`, build `34769075927`, artifact `10321686042`, APK `a584381d10ebb220ab686acec22f7668384e240f39ade052e4177583aa4adc7b`, and all earlier acceptance/research receipts. The old speed-signing-gate red branch is separate from this Lab red branch.

This session is primary. Historical dirty laptop worktree and private runtime captures remain untouched and are not claimed synced. Shared main context is unchanged because no ownership/product contract changed. Publish current branch handoff/status/memory after material work, verify live GitHub HEAD, and do not imply queued inputs or background monitoring.
