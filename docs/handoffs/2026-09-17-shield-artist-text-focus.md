# Shield v208 artist text focus: implementation awaiting signed CI

Date: 2026-09-17. Owner: `boop-wall-shield-split-v207`; started from live `1b6816f611f88db67abf548eebe67057c01f5bab`. Main workflow at `9808322212b4953d3fb4831fd05e9ffa1de806c6` applies.

Ryan approved removing only the Now Playing artist's grey box/outline: white normally, cyan matching the progress bar while focused. Preserve size, spacing, artist click and remote navigation. The shared TV decorator restyled all clickable/focusable TextViews, overriding the artist's existing focus colour.

## Reproduction and implementation

Regression-first commit `c98e69fc1db73f481112e8b8c56c09fdb13865f7`, signed-workflow run `35216418039`, job `105185850590`: both new tests failed as intended before any Android build/signing. The compiled production-method harness reported `Artist acquired the unwanted button box`; the source contract reported the missing artist-only opt-in. This was a real assertion failure, not a compilation failure.

Only the artist opts into `BoopTvChrome.useTextOnlyFocus`. A weak per-view exemption prevents both repeated layout decoration and queued generic focus callbacks from overwriting its state-list colours. Default Android focus highlighting is disabled for that label, its background is null, and its focus colour uses the same `accentColor` as the progress bar (`#4DB8FF`), with white otherwise. No focus listener replaces the state list. All existing remote/click callbacks and layout values are retained.

Shield alone advances to versionCode 208 / `1.2.208-shield`. The existing shared build still checks both application shells; Wall's version/configuration is not advanced and no new Wall installation is requested. APK identity verification is updated for those explicit versions; permanent signer, packaged native-byte/artwork checks and every inherited non-visual gate remain.

Next: verify signed CI, review the exact diff, publish the receipt in handoff/status/context/memory, and deliver the actual signed Shield APK. No successful build or new installation is claimed by this in-progress note.

Ryan installs by dragging the actual downloaded APK into his Shield scrcpy window and owns visual/physical acceptance. No RDC, ADB, assistant screen capture, emulator or automated visual test is authorized by this task. Voice, permissions, app data, accepted artwork, unrelated apps and concurrent work remain outside this change.
