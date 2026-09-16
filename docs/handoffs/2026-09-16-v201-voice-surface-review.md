# v201 Voice surface correction - independent continuation review

Date: 2026-09-16. Owner: `boop-hand-colour-v191`.

The voice-recovery session completed the documentation-only memory update at `e8abd41ddccbe5dc209661fd107c6fb41a90cb33`, then continued read-only device/source diagnosis. Concurrent implementation arrived from the collaborating session. It was reviewed rather than replaced with a competing patch.

## Reproduced failure

Fresh Shield screenshot and targeted MainActivity hierarchy read agree: the canonical face wrapper is GONE while its child GLSurfaceView retains VISIBLE and the large felt eyes remain over Voice Settings. Raw output and captures stay private. The view is the current canonical renderer, not the old BoopFaceView or an inferred unrelated service.

The new test commit `1759fd76516bca357472d6af82cccf37ee47bcff` ran before the corrective production change. Focused run `35079882308`, job `104741067108`, failed exactly at `voice settings surface: expected 8 but was 0`; four existing tests passed. Java compilation succeeded. This is a real failing execution of the extracted production ownership methods, not a test syntax error or a missing-JDK failure.

## Reviewed correction

Production commit `a90fbaa7c8e52f4d015934caafd924a9cf28f694` adds a null-guarded direct surface visibility write inside `applyPresentationVisibility`, using the existing `BoopFacePresentationState.effective()` value before updating the wrapper. It does not replace modal ownership, add an overlay service, change artwork/animation/voice controls, alter permissions, or change saved settings.

The regression exercises the actual source methods and, when present, their final materialized copies. It covers direct surface/wrapper agreement, delayed wake while Voice settings is open, another modal retaining ownership, intentional idle black, reopening, requested GONE, and construction-time null state. It explicitly does not simulate Android composition or claim physical acceptance.

No blocking issue was found in this narrow production diff. The separate eye surface now receives the same existing modal decision as its wrapper. A broader GL lifecycle rewrite is not required by this source-level finding and was not added by the reviewing session. Existing Home/Now Playing code already explicitly manages its own surface; it remains unchanged.

Candidate identity was bumped to versionCode 201 / `1.2.201-voice-surface-ownership` at `8f13808cb31db6a4c989993cc440ce22dc55799d`; identity assertions were reconciled at `7b603b267c568181ad178a77f23ccf635e4152ea`. At this review checkpoint, final workflow metadata/green build evidence is still pending. Do not mistake the earlier intentional RED run for a completed candidate result.

## Remaining acceptance

Require the focused regression and complete signed GitHub build to pass for the actual candidate source. An explicit later in-place installation and fresh joint Shield check must prove that the eyes disappear, remain absent during TEST VOICE, and restore appropriately on leaving settings. Audible natural pitch/rate, selector readability and cross-device propagation remain Ryan's human checks.

This reviewing session performed no production edit, build rerun, installation, phone operation, permission change or signing change. Preserve the collaborating implementation and current installed v200 until an explicitly requested verified replacement. The user requested continuation in chat with the already-connected ADB bridge; no reconnect or Work-mode prompt is needed.
