# Shield v208 artist text focus: signed candidate delivered for Ryan's test

Date: 2026-09-17. Owner: `boop-wall-shield-split-v207`; started from live `1b6816f611f88db67abf548eebe67057c01f5bab`. Shared workflow: main `9808322212b4953d3fb4831fd05e9ffa1de806c6`. Installation and visual/physical acceptance remain pending.

## Approved scope and implementation

Ryan supplied a Shield screenshot and approved removing only the Now Playing artist's grey box/outline: white normally, progress-bar cyan while focused, preserving size, spacing, artist click and remote navigation. The shared TV decorator previously restyled every clickable/focusable TextView, overriding the artist's focus colour.

Only the artist opts into `BoopTvChrome.useTextOnlyFocus`. A weak per-view exemption prevents repeated layout decoration and queued generic focus callbacks from overriding its state-list colours. Its background is null, Android default focus highlighting is disabled, and focus uses the same `accentColor` as progress (`#4DB8FF`), with white otherwise. All layout values and existing key/click callbacks are retained. Generic buttons and Voice editor chrome retain their existing behavior.

Reviewed production diff: 22 added/2 replaced lines in BoopTvChrome and 2 added/3 removed in ShieldNowPlayingView. The latter is precisely the artist initialization/listener hunk. Only Shield's version advances to `208` / `1.2.208-shield`. No other application source was changed.

## Regression and CI evidence

Regression-first `c98e69fc1db73f481112e8b8c56c09fdb13865f7`: run `35216418039`, job `105185850590`, correctly failed both new tests before Android build/signing. The production-method Java harness compiled and then asserted `Artist acquired the unwanted button box`; the wiring test reported the missing artist-only opt-in.

Implementation `b4600ccfc72fb44d04322662b75eee1f4458cdee`: the artist regressions passed (2 tests), but signed run `35216758867` stopped in the inherited v169 changed-file guard after its 1444 native/playback behavior assertions passed. That historical allowlist did not yet recognize the deliberately edited Now Playing view. No APK was produced by that failed run.

Maintenance/build source `477f199b6ad8bcf54e9d0eb6256bbb9f9633682c` adds an exact byte comparison against the pre-change view with only the two approved artist substitutions. The historical guard allows that file only after this exact comparison succeeds; no unrelated media edits or failed behavior tests were waived. No further app-source change was needed.

Full signed run `35217237866`, job `105188496531`: SUCCESS, including early artist regressions, all 18 inherited non-visual stages, split input audit/materialization, original plus generated source tests, both shell builds, actual APK validation, artifact upload and signer cleanup. Focused split run `35217237801`, job `105188496105`: SUCCESS. Property-recording doubles/source/numeric checks are code evidence only, not Android rendering or human acceptance.

## Exact delivery

- Source: `477f199b6ad8bcf54e9d0eb6256bbb9f9633682c`.
- Artifact: `10495244358`, `BOOP-Shield-v208-Wall-v207-Signed`.
- Archive SHA-256: `12a7121b05b921b199709df358e048e7d601f429d50600229c3cafb8803d9177`.
- Deliverable: `BOOP-Shield-v208.apk`, 160420201 bytes.
- Package/version: `com.boop.shieldoverlay`, `208`, `1.2.208-shield`.
- APK SHA-256: `6503557057c1661a37cf4c65f91c63e149808dbde000645ceaea04e3d1eba5a9`.
- Existing signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The actual signed APK passed package/entry/manifest/signer/asset checks. All 16 native libraries match the permanent accepted-v206 checkpoint. The downloaded archive matched GitHub's digest; the extracted Shield APK matched the receipt's hash and byte count. No rebuild or re-signing was performed during extraction.

The existing pipeline also compiles Wall v207 for shared-library integrity. Its rebuilt APK is not being delivered as a Wall update and is not claimed byte-identical to the earlier installed Wall. Provide the actual Shield APK, not the multi-app artifact ZIP, as the user's primary download.

## User testing and continuity

Ryan drags the APK into Shield scrcpy and checks box-free artist text, white unfocused/cyan focused, and the retained click/navigation. No automatic installation, RDC/ADB operation, screenshot, device driving, emulator or visual acceptance sweep occurred. The screenshot supplied before the change is not evidence of the new candidate's behavior. Visual/physical acceptance and installed v208 identity remain pending Ryan's report.

Frozen Voice, accepted character/animation, permissions, app data/setup, models, signing keys, other applications and concurrent work remain unchanged. Earlier real-remote microphone acceptance is still pending, not reopened or certified by this build. Prior root handoff/status/context/memory were archived byte-for-byte under `docs/handoffs/2026-09-17-before-artist-focus/`; current roots retain the newest receipt and pending checks. Documentation publication is separate from the built source, with CI skipped for documentation-only changes.
