# Rally Shield verification

Date: 2026-09-12. Status: signed build installed; device testing incomplete.

## Built and installed identity

- App: BOOP Rally, package `com.boop.rally`, version 1 / 1.0, ARM64.
- Source: `71c71157fd9b6931b3c7d320409fa32d94f72197`.
- Successful GitHub Actions run: `34688081241`.
- Artifact: `10296216335`, named `BOOP-Rally-Shield`.
- APK size: 1,602,630 bytes.
- APK SHA-256: `cb3b37efd9dfe4b5ea1850c4fe3ff9155e3c319ecefa1855ee1ae6f408e192d8`.
- Permanent BOOP certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Actions completed compilation, 12 behavioral tests, package/content checks and permanent signing. The downloaded APK passed independent size/hash and v2/v3 certificate checks before installation. The artifact was downloaded again through the GitHub connector; its extracted APK matched the same hash. The 12 behavioral tests were rerun successfully from that artifact's corresponding source.

The first Actions run failed on the upstream NDK working directory. The corrected build invokes ndk-build from the upstream jni directory. No game executable or proprietary asset is included in the public APK or source artifact.

## Behavioral coverage

Bundle preparation: deterministic output, original-file preservation, required executable, controlled boot configuration, accepted Championship configuration, duplicate names, symbolic links and unsafe output location. Java bundle policy: valid pack acceptance, traversal rejection and wrong-game rejection. Java controls: 22 assertions covering key mapping, overlapping input sources and release/reset. Native input queue: short taps survive between frames. Native audio queue: capacity/wrap/discard plus 200,000 concurrent samples.

These are nonvisual tests. They do not establish game physics, controller feel, subjective sound quality or user visual acceptance.

## Device evidence

The installation target was positively identified as SHIELD Android TV before ADB installation. Installation succeeded. Both private packs were copied and their device hashes matched the local receipts; original desktop folders were verified unchanged during preparation.

BOOP Rally's two-game launcher was opened. RAC Rally booted its original intro at 320x200; Championship booted at 640x480 and its keyboard/options/back navigation responded. Native diagnostics showed advancing video frames, a configured 30 Hz output, nonzero PCM samples and no AAudio error in the observed intervals. Some background intervals correctly reported paused state and stationary frame counters. This is not a measured gameplay frame-rate or audible-speaker acceptance claim.

A separate package, `local.networkq.rally`, was also present and sometimes foreground. Its different controls/text-entry dialogs are not evidence about this APK. It and the Johnny Castaway app were left installed and unmodified. Read foreground package immediately before and after future test actions; do not infer game identity from a screenshot filename or a launch intent alone.

Remote file/command requests and a connectivity ping later timed out. Hardware testing stopped rather than claiming an unobserved result.

## Not yet verified

Actual driving in both games; steering/accelerator/brake response with a physical gamepad; audible engine/co-driver/music quality; in-game save, clean return and reopen persistence; repeated game switching and background/resume under exclusive device control. Do not mark these passed or publish this candidate as fully accepted until tested.

## Rebuild and review notes

The public repository and artifact contain the launcher/host and GPL notices, not commercial game data. Rebuild from the recorded Git commit using the existing Actions workflow. The corresponding-source archive includes the pinned vendor sources without their .git directory, while the current build script checks the vendor Git revision. For a local rebuild from that archive, preserve the provided vendor sources, move that vendor directory aside, and let the script clone the pinned upstream commit. Signing still requires the existing approved private setup; do not create/export a replacement key.

A focused source review checked package separation, permissions, input lifetimes, asset validation, native queues and shutdown paths. An independent reviewer was not available. Repeated/overlapping GameActivity launches and shutdown/relaunch timing remain specifically on the follow-up lifecycle test list. No application code was changed after the installed build; subsequent commits are documentation only.
