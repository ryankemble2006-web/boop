# v161 embedded Lab scale-zero repair: verification receipt

Date: 2026-09-13. Repository `ryankemble2006-web/boop`; owner `boop-unified-eye-sync-safe-v159`. Current user scope: GitHub source/tests/build/signing only.

## Result and limits

A host-side regression reproduced the embedded Lab freezing its sign pose when Android animator scale is zero, including after complete source materialization. The minimal Lab integration repair passes the new regression and existing preservation gates. A fresh v161 APK is permanently signed and verified. No laptop command, emulator launch, Android runtime/visual test or physical-device action ran. This is not on-screen speed acceptance.

## Exact commits

- Starting live checkpoint: `983e2c866fe05b64b9c3265e269afbfa223de67f`.
- Test-first commit: `fc9e6633ca7b578b7729cc5ee0e134294969075a`; tree `efa942b071ae037a73ea60f001a3f09398e23059`.
- Preserved red branch: `wip/boop-lab-scale-zero-red-fc9e663`.
- Repair/build commit: `0b6ee6f91e05f00138a94ec2c9fd846117020754`; tree `ad86f60be7b4405ee249c6cc079a0c378821faa7`.
- APK version: `161 / 1.2.161-lab-scale-independent`, package `com.boop.alpha1`.

Final handoff/status/memory documentation is a successor of this engineering commit. Fetch live branch HEAD, not a stale local ref, before new work. Main remained `b7d3eb6ea5e1189b45bd3ed4ecf685723613464d` when checked. No shared ownership change required rewriting main.

## Cause and path traced

`source/BoopCanonicalAnimationActivity.java:onResume` combined Android ANIMATOR_DURATION_SCALE==0 with power-saving mode to derive reducedMotion. In doFrame, reducedMotion selects a fixed sign time10000 or a fixed eye endpoint, even while BOOP's own clock advances. This was an integration condition, not a bad speed multiplier or damaged artwork.

`scripts/materialize-android.sh` copies source Java into `boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/`; the complete Unified materialization and final single-face patches leave this Lab callback behavior present. The red full build actually compiled/exercised the copied callback block, rather than assuming the raw file was what shipped. Raw and materialized old Lab SHA256 both equalled `c97c3b0bae3cbd150472b668bfdee96140ee1aefe9f2b15aa93900e5fcf1f652`.

## Test-first evidence

Four test files changed before any app implementation edit:
1. `tests/boop_lab_scale_checks.py`: extracts actual Lab fields and complete onResume/onPause/focus/updateLoop/doFrame bodies verbatim, compiles them with real motion classes in a host harness, and reports source identity.
2. `tests/java/BoopLabScaleHarness.java.in`: test-only platform/output adapters; observes numerical poses, not screenshots. Covers four BOOP speeds and Android animator values1/.5/2/0, five Lab sign styles including Freddie, pause/focus/resume, manual pause, Slow review, explicit freeze, null power manager and existing power-saver behavior.
3. `tests/test_animation_speed.py`: retains five existing tests and adds the callback regression. In a materialized build it runs both paths even when the first fails.
4. `tests/test_materialized_speed.py`: additionally verifies copied Lab bytes and FreddieMotion identity; original library/assets/master checks remain.

Standalone timing `34770049893`, job `103757866871`: FAILURE, exactly one failed and five passed. The new harness compiled, passed nonzero-scale controls, then failed `Lab sign at Android scale=0.0, rate=0.5, style=0`.

Full red build `34770049896`, job `103757866953`: FAILURE at the mandatory pre-signing speed step. Both raw and fully materialized callbacks produced the same zero-scale sign failure. All prior configured stages passed. Signer preparation, APK signing, verification and upload were skipped. This demonstrates the existing gate prevented publication of the known failing candidate.

## Minimal repair and review

`source/BoopCanonicalAnimationActivity.java` removes the Settings import and changes only the reduced-motion condition to `power!=null&&power.isPowerSaveMode()`, with a short explanatory comment. The existing frame-delta expression, speed bindings, clock, controls, freeze behavior, authored clips and renderers are unchanged. `unified/app-build.gradle` changes only versionCode160->161 and versionName.

The exact repair diff was fetched and reviewed before publication. Comparison from the starting checkpoint shows only these two production/version files and the four test files. No workflow, signing, permissions, colour, media/voice or artwork change. Review was direct source/diff review, not an independent subagent review.

## Green GitHub results

For exact repair commit `0b6ee6f91e05f00138a94ec2c9fd846117020754`:
- Timing run `34770388848`, job `103758794408`: SUCCESS.
- Appearance run `34770388845`, job `103758794360`: SUCCESS.
- Full build `34770388933`, job `103758794713`: SUCCESS through signing, package/archive checks, upload and cleanup.

The full build log records all six speed functions passing. Existing `BoopAnimationSpeedHarness`: 160720 checks across26 authored clips at .5/1/1.5/2, including exact original1x. Existing edge harness: 1157272 checks of fractional/irregular time, sign channels, one-shot boundaries and pause/rate/resume compatibility. New Lab harness: 20920 checks for raw callbacks and 20920 for materialized callbacks, including the previously failing zero-scale case. Both fixed Lab files report SHA256 `266c7e5284843dda3e441b1e0c55c7a645260dbcc17306be438b7cd6ea9c97dc`.

Materialized Lab, speed/binding, colour renderer, authored-motion and approved-master byte checks passed before signer preparation. All configured integration, ownership, Shield polish and colour checks passed. Verification receipt reports 235 Unified and68 Shield functional tests with zero failures, errors or skips. Build logs include existing tool/Gradle deprecation warnings; no claim of warning-free compilation or proactive tool upgrade is made.

The host harness does not instantiate a real Android Activity or GL surface. It supplies controlled platform inputs and records the pose data emitted by the actual callback bodies. It proves this control-flow regression under those inputs, not Android callback ordering, real Choreographer scheduling, physical performance, UI preference delivery or visible pixels.

## Artifact identity and independent receipt checks

Artifact `10321956422`, name `BOOP-Unified`, full build `34770388933`, created `2026-09-13T17:06:36Z`.

- Artifact ZIP bytes: `71263887`.
- ZIP SHA256: `623805381b04c2ce61a8a5bfcb56e663a9a2fc8eddbc1b9eb9b98ed987ccdf3b`.
- APK bytes: `155241242`.
- APK SHA256: `c68b81be9b7d3e10883d2aea52c05dc7b8c187fcd835eaddf73e25e91cb4acd6`.
- Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- `built-commit.txt`: `0b6ee6f91e05f00138a94ec2c9fd846117020754`.

Downloaded through the GitHub connector. Chat-sandbox inspection confirmed archive SHA256 against GitHub metadata, archive/APK ZIP integrity, actual extracted APK SHA256 against its receipt, exact built commit, version/package badging and unchanged signer receipt. GitHub's apksigner stage performed cryptographic signature verification. No local app build or runtime test was substituted.

All13 APK asset entries have the same names and identical bytes as the previous verified v160 APK. In particular, approved eyes remain `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22` and approved hands remain `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`. Catalogue, eye shaders and lid rig also match. The full APK is intentionally a different hash due to code/version changes.

## Preserved acceptance and remaining runtime work

Wall -> Shield colour is user-accepted; prior physical captures showed both directions. Last recorded real Shield/Pixel7 had v159, hue2 and sharing on. None was queried or changed here; do not treat those historical values as license to overwrite newer user choices. Physical Pixel10 remains excluded. No APK deployment occurred.

The preserved v160 candidate remains at `1d8bf3d39a0858aa0c4f2b435fc92b1b16ef486a`, build34769075927/artifact10321686042/APK SHA256 `a584381d10ebb220ab686acec22f7668384e240f39ade052e4177583aa4adc7b`; its original implementation and successful tests remain in history. Do not erase it or revive older drafts.

Next is permitted on-screen validation of this exact v161 artifact on existing laptop emulators: all four rates, mid-clip changes, original1x, sleep/wake, notifications/eyes, pause/resume and zero Android animation scales, followed by authorized Shield/Pixel7 checks. Physical deployment remains held until runtime gates pass. The previous blocked laptop request was not retried or routed around. GitHub did not launch Android or judge screenshots. Two-device colour offline/reconnect remains separate outstanding coverage, not a colour reapproval requirement.

Historical dirty local recovery work and private screenshots remain untouched. No local synchronization or running/queued device automation is claimed. Older detailed handoffs remain preserved; this receipt supersedes only the prior unresolved Lab source concern.

## Primary references checked for the scoped decision

- Android Settings.Global ANIMATOR_DURATION_SCALE: https://developer.android.com/reference/android/provider/Settings.Global#ANIMATOR_DURATION_SCALE
- Android PowerManager.isPowerSaveMode: https://developer.android.com/reference/android/os/PowerManager#isPowerSaveMode()
- Android Choreographer.FrameCallback: https://developer.android.com/reference/android/view/Choreographer.FrameCallback

These explain platform inputs and frame timing; actual repair evidence is the red/green callback execution above. No unrelated API migration, permission grant or system animation-setting write was made.
