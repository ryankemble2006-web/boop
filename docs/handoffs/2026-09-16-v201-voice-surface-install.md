# v201 Voice surface fix: build and joint-test continuation

Date: 2026-09-16. Owner: `boop-hand-colour-v191`.

## Scope and authorization

Ryan explicitly requested removal of the eyes obstructing Voice settings and reinstallation on Shield so joint voice testing can resume. His subsequent message asked whether the interrupted session had identified the culprit. Continue in Chat using the existing authorized bridge. No phone operations, permission changes, data clear, signer replacement, new voice features or app split are authorized by this work.

## Evidence and minimal correction

The installed v200 APK SHA-256 was directly measured as `bd0b80ab96e858ed12bcaa1c43b132f4b55a43c078630a7cebb80a9db34d5bf5`, matching the artifact from signed run `35076396821`, built source `572e8d6cb4726951df0604e18a9a8e614d93a543`. This closes the earlier v200 source/hash provenance gap.

Private runtime captures reproduce the obstruction with the canonical face wrapper GONE but its child GLSurfaceView VISIBLE. The top-layer graphics surface still displays the eyes over Voice settings. The focused regression at `1759fd76516bca357472d6af82cccf37ee47bcff` failed at `voice settings surface: expected 8 but was 0` in run `35079882308`; four inherited tests passed. It executes actual production ownership methods but does not simulate Android composition.

Production fix `a90fbaa7c8e52f4d015934caafd924a9cf28f694` adds only direct, null-guarded child-surface visibility propagation using the existing modal ownership decision. Artwork, animation, voice backend, controls and Home/media code remain untouched. Independent review: `docs/handoffs/2026-09-16-v201-voice-surface-review.md`.

## Current build checkpoint

Candidate: versionCode 201 / `1.2.201-voice-surface-ownership`.
Source under build: `478372242171d9df77e00897a64bad0e3de240e4`.
Full signed GitHub run: `35080668407`, job `104743636102`.
The first focused regression step is green. At this checkpoint the remaining full build is still running; no completed v201 build, installation or physical acceptance is claimed.

The workflow retains inherited checks and permanent signing, adds exact source/materialized comparison for BoopCanonicalFaceView, and publishes `BOOP-Unified-v201-Voice-Surface-Ownership` only after APK identity/signer checks.

## Next step

Inspect the current run summary, retrieve only relevant failure logs if needed, then stage the successful signed candidate. Verify artifact commit, APK hash and permanent signer before the already-authorized in-place Shield update. Confirm installed version/hash, reopen Voice settings and capture the actual display. Keep runtime visibility verification separate from Ryan's listening tests of TEST VOICE, pitch/rate and cross-device propagation. Private screenshots/raw diagnostics must not be published.
