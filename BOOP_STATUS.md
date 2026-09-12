# BOOP defaults status

Branch `boop-shield-defaults`. Version `143 / 1.2.143-boop-shield-defaults`.

Implementation is complete locally: Use BOOP defaults, selectable review,
verified Apply, pre-preset Undo, partial/cancel recovery and device/input checks.
14 exact entries: nine disables, nine boot choices, six background-limit pairs.
No personal branding, new permissions, new artwork or lyrics integration.

Final Android compilation passed; 277 Unified tests passed with zero failures,
errors or skips. Existing 15 Startup suites and Android 11 linkage passed.
New coordinator suite: 27 scenarios; safety suite: eight scenarios; profile and
journal structure checks passed. Canonical shared-state/media tests passed.

## Signed candidate receipt, 2026-09-12

- Version: `143 / 1.2.143-boop-shield-defaults`, package `com.boop.alpha1`.
- Exact build source: `696a1b2249b2549c12c4832baf7bc2c30b913aab`.
- Successful permanent-signed workflow run: `34676381862`.
- Artifact: `BOOP-Unified`, ID `10291778955`.
- APK SHA256: `4c91bf3b54675afb11f3a0c2f574b1eb0da5b7747d2919016e3d3145cb77f2a1`.
- Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- GitHub-reported artifact archive digest: `sha256:677de8185388cab4b418318a32f29e3057c70a5dbcd6de9028dc8dc371489a9d`.

The signed download was independently checked against its included APK hash and
built-commit receipt. Package/version, permanent signer and ZIP integrity passed.
The APK contains the preset coordinator/review screen and both BOOP-branded
buttons; the unfinished Lyrics browser is absent. GitHub passed the existing
nonvisual gates, including 68 Shield and 217 Unified focused tests with zero
failures/errors/skips, plus the new defaults behavior suites.

**Not installed, not applied, not merged into ongoing work.** No live package,
permission, media or default-HOME changes were made in this feature task.
The preset's real-device layout, Apply/Undo round trip and firmware behavior
remain unverified; signed/tested does not imply physical acceptance. Obtain
separate deployment approval, inspect the shared device's current build first,
and preserve its rollback. Do not overwrite newer unrelated branch work merely
because this isolated candidate has versionCode143.

Read SESSION_HANDOFF.md for the implementation and recovery boundaries.
