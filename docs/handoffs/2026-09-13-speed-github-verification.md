# v160 speed: GitHub-only verification receipt

Date: 2026-09-13. Repository `ryankemble2006-web/boop`, owner branch `boop-unified-eye-sync-safe-v159`.

## Scope and result

Ryan asked to continue in GitHub rather than local execution. This work reviewed the preserved v160 implementation, expanded host-side numerical/source checks, made them mandatory in the full APK workflow, and obtained a fresh permanent-signed artifact. No app implementation, artwork, animation, colour code, permission or signing-key change was made. No laptop/emulator/physical-device action was attempted.

This is a verified GitHub build/test checkpoint, not runtime or physical speed acceptance. The earlier local safety block was not retried or bypassed. GitHub did not launch Android or inspect screenshots.

## Exact provenance

- Starting live branch: `ca81d73c386732124ec8b320b59df9a546931de0`.
- Preserved application implementation: `d149cb509ec376779daf84c50f621d8adcbacd24`, version160 / `1.2.160-colour-animation-speed`, package `com.boop.alpha1`.
- Test-first commit: `ca6cd34af50c42dfb5606053b5d27fde7128eaea`.
- Engineering/build commit: `1d8bf3d39a0858aa0c4f2b435fc92b1b16ef486a`.
- Engineering tree: `ccc0273138fd1e64e24b45c7f90d02eb5067d2f8`.
- Shared main remained `b7d3eb6ea5e1189b45bd3ed4ecf685723613464d` when checked.

The final documentation commit is a successor of the engineering commit. Fetch LIVE branch HEAD before new work; do not mistake the v159 branch name for the APK version.

## Discovered gap and test-first repair

The separate timing workflow exercised speed tests, but the full signed APK workflow did not invoke them. A green signed build therefore did not itself establish a speed-suite pass for its own source commit.

Three test files were changed/added first:
- `tests/test_animation_speed.py`: existing tests retained, three tests added.
- `tests/java/BoopAnimationSpeedEdgeHarness.java`: new host-only numerical harness.
- `tests/test_materialized_speed.py`: copied animation-library/asset identity checks after materialization.

Timing run `34768979364`, job `103754958464`, at the test-first commit reported exactly one failure and four passes. The only failure was `test_signed_apk_requires_timing_and_materialized_source_checks`: the full workflow lacked the required gate. Other numerical, wiring and authored-byte checks passed. Red checkpoint is retained at `wip/boop-speed-gate-red-ca6cd34`.

The repair was exactly five added workflow lines before `Prepare permanent BOOP signer`:

```yaml
      - name: Test exact BOOP animation speed before signing
        run: |
          python tests/test_animation_speed.py
          python tests/test_materialized_speed.py
```

GitHub commit diff was read back and compared with the starting checkpoint. Only the three test files and this workflow changed; no app source changed. Review was direct source/diff review, not an independent subagent review.

## Fresh GitHub results

At repair commit `1d8bf3d39a0858aa0c4f2b435fc92b1b16ef486a`:
- Appearance run `34769075918`: SUCCESS.
- Full build `34769075927`, job `103755257333`: SUCCESS.
- New exact-speed/materialized-source gate: SUCCESS before signer preparation.
- All configured integration, ownership, approved-assets, Shield behaviour, colour, compile, permanent-signing, package and archive checks: SUCCESS.
- Uploaded verification receipt: 235 Unified functional tests and 68 Shield functional tests, zero failures/errors/skips.

The repair changed only the build workflow, so the standalone timing workflow's path filter did not start another timing-only run for it. The full build directly executed all five timing test functions for the exact repair commit. Do not mislabel the earlier red run as green or claim an unobserved timing-only run.

The first test-commit full run `34768979321` was superseded/cancelled by normal workflow concurrency before signing; it is not the accepted build and needs no retry. Original v160 successful runs `34760362361`, `34760362356`, `34760362417` and original artifact `10318393790` remain preserved historical evidence.

## Artifact and integrity

Artifact: `10321686042`, name `BOOP-Unified`, full build `34769075927`, created `2026-09-13T16:40:41Z`. GitHub metadata and artifact `built-commit.txt` both identify `1d8bf3d39a0858aa0c4f2b435fc92b1b16ef486a`.

- Artifact ZIP SHA256: `b96b72f142b3c73494b96c007fc1bad1d362c8d3da7b75d2420bd5eb8c45334e`.
- APK SHA256: `a584381d10ebb220ab686acec22f7668384e240f39ade052e4177583aa4adc7b`.
- Permanent signer SHA256 receipt: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- APK bytes: `155241246`.

The GitHub artifact was downloaded through the connector. In the chat sandbox, its ZIP SHA256 matched GitHub metadata; the actual extracted APK SHA256 matched apk-sha256.txt; built-commit, signer and verification receipts were read. This was artifact integrity checking, not a local app build/test or device operation. The APK hash matches the earlier v160 candidate exactly. Package and signature verification were performed by the successful GitHub job; no replacement key was used.

## Numerical coverage, without visual claims

Existing comparisons retain the exact accepted v156 reference at `a901c1e9f31e55c710e31ac7ff4f5924c9769d56`. Added coverage uses an independent integer half-millisecond oracle under irregular rate/frame changes, checks phase continuity and fractional deadlines, compares all notification sign hand/eye float channels at four rates/styles, tests one-shot return immediately before/at/after the scaled endpoint, and compares pause/rate/resume output against v156.

EyeMotion.java, EyeCatalogue.java and SignMotion.java are byte-compared against that accepted baseline. Materialized animation-library speed/binding/renderer/motion files, shaders/catalogue and approved eye/hand masters are byte-compared against reviewed source. These prove those source/numerical properties, not visible iris output, Android callback ordering or real-device motion quality.

## Acceptance and remaining checks

Wall -> Shield colour is user-accepted. Earlier actual physical captures also established both directions. Last recorded Shield/Pixel7 had shared user hue2 and v159; this continuation did not query or change them. Physical Pixel10 was untouched and remains excluded. Do not reset user colours to test fixtures.

Speed runtime validation remains pending: all four rates, mid-clip changes, sleep/wake completion, notification hands/eyes, pause/resume, default1x and zero Android animation scales. Keep physical v160 deployment held until permitted runtime gates pass. No visual/emulator tests were moved into GitHub.

Specific source-review follow-up: `source/BoopCanonicalAnimationActivity.java` still sets reducedMotion when Android ANIMATOR_DURATION_SCALE is zero. The current `tests/test_unified_animation_scale_independence.py` checks Wall only. The inspected final single-face materializer does not modify that Lab expression. A complete materialized-path and focused regression/runtime check is still required before calling all-surface scale-zero independence verified or repairing integration. This is not a newly reproduced physical bug and must not trigger rewriting authored motion.

The separate deliberate two-device colour offline/reconnect test also remains coverage, not a reason to reopen accepted live delivery. Preserve all prior research and acceptance notes. No laptop checkout was synchronized; its historical dirty recovery work remains untouched. Update these receipts after further material work and verify live GitHub HEAD.
