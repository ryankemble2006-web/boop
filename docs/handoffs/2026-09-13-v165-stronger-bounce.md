# v165 stronger music bounce: signed candidate receipt

Updated 2026-09-13. Ryan tested installed v164 and reported "ha it kinda works :P make him bounce more obviously". Record partial positive motion feedback and a request for more visible travel, not general physical acceptance. Task branch stays `boop-unified-v164-music-bounce`.

## Scope and source preservation

Started from live `95e2d153ac6e6b319f50284d4fbdb53bcb83c294`, the v164 installation handoff. Production change is one `* 2f` multiplier in MusicBounceRenderer's clamped visible-height assignment and version165 /1.2.165-music-bounce-stronger. MusicBounceSource, MusicBounceEnvelope, permission handling, blink engine, renderer delegation, size, horizontal placement, native lyrics and all other application inputs are unchanged from v164. The visible offset reaches at most 0.28 of the current surface height; shape/timing of the envelope and the zero-lift resting viewport are unchanged. Appearance and clipping at the larger offset need Ryan's test.

Accepted v162 branch `boop-unified-native-lyrics-v162` was rechecked at unchanged `112d09b5b446d6582954a6d89b3700fe16298ecb`. This task does not merge, repoint an accepted owner or import the old Music Lab/v163 tree. Existing native Lyrics-button route, internal Activity, Skip observer and passive half-size licence footer are protected by the baseline checks.

## Test-first record

RED source `71942f8282c618e260864e9ff46a110906e63304`, run `34779468840`, job `103783705756`: three of six test groups failed as expected for absent v165 version/gain. The compiled production renderer, with controlled Android/GL-boundary doubles, reported expected 2 pixels but actual 1. Three existing groups passed, including 332 envelope assertions. Failure logs were read before implementing the multiplier. Signing/build stages were skipped.

GREEN source `fe8c04274064643342734baaafb46077832f335c`, run `34779552167`, job `103783924085`, completed SUCCESS; full completed job log read:

- All six music test groups passed, including the unchanged 332 real-envelope assertions and 27 compiled production-renderer amplitude/delegation checks. Inputs include zero, small/medium/max/over-limit values, negative/NaN/infinity, resize and reset. Size and horizontal placement remain unchanged.
- The v165 guard permits only two previously tracked app-file changes from v164: renderer multiplier and build-version fields. An exact-text comparison confirms the one-line renderer replacement. The existing v162 baseline guard also passes.
- Native lyrics routing and private-Activity/no-Lab source checks passed; two inherited one-time v162-only freezes skip later versions. Native harness assertions passed: 61 data/timing/ownership, 35 transport, 2 incomplete timing, 7 entry control-flow, 28 Activity/state/lifecycle/transport. The skips are not counted as executed passes.
- Six existing timing functions passed, with 160720 timing checks, 1157272 edge checks, and 20920 callback checks each for raw and materialized source. All 26 authored clips remain unchanged. Materialized colour/speed/master checks passed.
- Ten canonical-owner/notification/face/bay pytest contracts passed. Source-to-materialized comparisons passed for MusicBounceSource, ShieldNowPlayingPuppetView and MusicBounceRenderer.
- Full signed assembly succeeded with 104 tasks. Actual APK package/version, native Lyrics and permission Activity registrations, music/lyrics class descriptors, permanent signature and archive integrity passed. Artifact upload and temporary signer removal succeeded.

Scoped in-session review confirmed the narrow production diff and preserved existing behavior. No independent reviewer, complete historical-suite rerun, real audio input test or hardware visual acceptance is claimed. Existing nonfatal action/Android/Gradle warnings were not changed.

## Exact candidate and independent artifact inspection

Package `com.boop.alpha1`; version `165 / 1.2.165-music-bounce-stronger`.
Source/build commit `fe8c04274064643342734baaafb46077832f335c`.
Run `34779552167`, artifact ID `10325220435`, name `BOOP-Unified-v165-Music-Bounce-Stronger`.
APK `BOOP-Unified-v165-Music-Bounce-Stronger.apk`, 155290450 bytes.
APK SHA256 `bbce760abc5f50fa68663c344fa9b75edfd9feb76559fe35c328c507af09bda2`.
ZIP 71282006 bytes, SHA256 `a57a43525c072739a35296512dd24bce33cdeb73d034f1a552ae31d8f3341483`.
Permanent signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The ZIP was downloaded through the GitHub artifact connector and its digest matched the GitHub artifact metadata. Extracted APK digest matched apk-sha256.txt; built-commit.txt matched the exact source above. Badging, public signer receipt, native Lyrics registration and actual APK music/lyrics class descriptors were inspected. Archive integrity passed. No APK execution or app build occurred in the artifact sandbox.

## Delivery and physical boundary

Provide the signed APK directly in chat. v165 was NOT installed or run on Shield by this tuning task. No ADB input, permission grant, settings reset, uninstall/data clear, phone/emulator operation or local source edit/build occurred. The last verified installed candidate remains v164, which Ryan now says partially works. Do not present v165's numeric tests as his visual approval or assume an installation occurred.

Normal requested installation can later update the identified Shield's Unified in place with verified package/signature/hash and preserved data/grants. Test the stronger movement with Ryan, not a hosted visual run. Main, accepted owner/v162 and other labs were not written. Laptop continuity files were read only; no checkout synchronization is claimed. Final root handoff/status/memory and this receipt are documentation-only changes after the signed source commit. Historical v164 receipts remain unchanged.
