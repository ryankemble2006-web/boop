# Current handoff: v161 Lab scale-zero repair verified in GitHub

Updated 2026-09-13. Ryan said "do it" after the GitHub-only speed review identified the embedded Lab's remaining Android-scale dependency. This continuation reproduced that specific failure in host-side tests, made the minimal integration repair, and produced a permanent-signed v161 APK. Runtime and physical acceptance remain separate and pending. No laptop, emulator or physical-device action was attempted.

## Current source, build and artifact

Owner: `boop-unified-eye-sync-safe-v159`. Starting checkpoint: `983e2c866fe05b64b9c3265e269afbfa223de67f`. Current engineering/build commit: `0b6ee6f91e05f00138a94ec2c9fd846117020754`, tree `ad86f60be7b4405ee249c6cc079a0c378821faa7`.

Version: `161 / 1.2.161-lab-scale-independent`, package `com.boop.alpha1`. The v160 clock, four saved speeds, hue integration and authored animations are retained; this is not a replacement implementation. The version increase distinguishes the actual Lab code repair from the preserved v160 APK.

GitHub timing run `34770388848`: SUCCESS. Appearance run `34770388845`: SUCCESS. Full build `34770388933`, job `103758794713`: SUCCESS, including all mandatory pre-signing timing, materialized-source, colour, ownership and preservation gates, permanent signing, package/archive verification and artifact upload.

Artifact `10321956422` / `BOOP-Unified`, created `2026-09-13T17:06:36Z`:
- APK SHA256: `c68b81be9b7d3e10883d2aea52c05dc7b8c187fcd835eaddf73e25e91cb4acd6`.
- ZIP SHA256: `623805381b04c2ce61a8a5bfcb56e663a9a2fc8eddbc1b9eb9b98ed987ccdf3b`.
- Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The downloaded archive and extracted APK hashes match their receipts. All 13 APK asset entries match the previous v160 APK byte-for-byte. Package/version, built-commit, signer and verification receipts were read. Artifact inspection ran in the chat sandbox only; it was not an Android launch or local application build/test.

Read `docs/handoffs/2026-09-13-lab-scale-zero-verification.md` for complete red/green evidence and limits.

## Reproduced cause and minimal fix

The Lab's `onResume` set `reducedMotion` when Android `ANIMATOR_DURATION_SCALE` was zero. `doFrame` then selected fixed/end poses even though BOOP's own clock advanced. In the new regression, the sign pose failed specifically at Android scale0, BOOP speed0.5, sign style0; nonzero control scales passed.

Test-first commit `fc9e6633ca7b578b7729cc5ee0e134294969075a` is preserved at `wip/boop-lab-scale-zero-red-fc9e663`. Timing run `34770049893` failed only the new sixth test; five existing tests passed. Full build `34770049896` reproduced the same failure against both raw and fully materialized Lab callbacks, and stopped before signer preparation. Both failing Lab files had identical SHA256 `c97c3b0bae3cbd150472b668bfdee96140ee1aefe9f2b15aa93900e5fcf1f652`.

The only behavior change is in `source/BoopCanonicalAnimationActivity.java`: remove the Android Settings import and make reduced motion depend only on the existing power-saving condition. Manual Pause motion, Slow review, explicit freeze, focus/pause/resume, the frame-clock expression and artwork are unchanged. `unified/app-build.gradle` changes only the version number/name.

The full green build ran all six timing test functions. Existing harnesses passed 160720 and 1157272 numerical checks. The new Lab harness passed 20920 checks for raw callbacks and another 20920 for materialized callbacks; both fixed files had SHA256 `266c7e5284843dda3e441b1e0c55c7a645260dbcc17306be438b7cd6ea9c97dc`. Build receipts report 235 Unified and 68 Shield functional tests, zero failures/errors/skips. These are numerical/source/compile checks, not Android callback scheduling or visible rendering proof.

## Accepted colour and device boundary

Ryan's Wall -> Shield colour acceptance remains valid. Prior physical captures also demonstrated Shield -> Pixel7 and Pixel7 -> Shield. Do not reopen colour repair or reset historical fixtures. Last recorded physical state, not queried in this continuation: Shield and Pixel7 on v159, APK `80e86119d4771624ff47617373df2cebc03c0454aa47f10a0996a6d507b68353`, sharing on, user hue2. Physical Pixel10 remains excluded and untouched.

No APK was installed in this continuation. Older main laptop emulators were previously found on v160; current reachability/state is not established. Historical dirty worktree contents and private runtime captures were left alone and are not claimed synchronized.

## Next permitted step

The source-level Lab scale-zero defect is repaired and tested; do not restart that diagnosis from the older v160 note. Actual on-screen speed acceptance remains pending. When permitted and within Ryan's scope, validate this exact v161 artifact on the existing laptop emulators: all four speeds, changes mid-clip, exact original1x, sleep/wake completion, signs/eyes, pause/resume and zero Android animation scales. Then authorized Shield/Pixel7 device-specific checks. Physical deployment stays held until runtime gates pass. Do not move Android runtime/visual tests to GitHub or route around the earlier denied laptop request.

A deliberate two-device colour offline/reconnect cycle is still separate coverage, not a reason to reopen accepted live delivery. Preserve approved masters, shaders, authored motion, working Wall hue controls, accepted Shield polish, voice/media behavior and single-face ownership. No permissions, signing keys, platform installs or unrelated branches were changed.

## Provenance

The earlier v160 build `34769075927`, artifact `10321686042`, source `1d8bf3d39a0858aa0c4f2b435fc92b1b16ef486a`, and original v160 implementation `d149cb509ec376779daf84c50f621d8adcbacd24` remain preserved. Prior receipts: `docs/handoffs/2026-09-13-speed-github-verification.md`, `docs/handoffs/2026-09-13-colour-accepted-wall-shield.md`, and the historical colour-failure continuation. Main remained `b7d3eb6ea5e1189b45bd3ed4ecf685723613464d` when checked; no shared ownership/product contract changed, so main context was not rewritten. This continuation is primary. No queued device inputs or scheduled monitoring exists.
