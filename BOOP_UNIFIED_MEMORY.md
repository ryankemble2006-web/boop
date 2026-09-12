# BOOP Launcher v1: signed standalone candidate

Updated 2026-09-12. Owner `boop-shield-launcher-standalone`; project `shield-launcher/`.
Package `com.boop.shieldlauncher`, version `1 / 1.0.0-launcher-tools`.
Signed source `72dde252f4f73487d9b99af52ea189777822a795`; Actions run `34684440925`
succeeded, artifact `10295068406` (BOOP-Launcher), 7,250,154-byte APK.
SHA256 `5293f65d53299bf49b45c10911693f1e3b97c6effd1a87a3734c2ee88037bb7b`.
Permanent signature, downloaded package/version/hash, actual APK dependency closure,
Android build and focused startup/defaults/close tests verified. No physical install
or acceptance on the other Shield. Failed integration work was not imported.
Read docs/verification/2026-09-12-shield-launcher-v1.md for the full receipt and limits.
Shared main product exception published at `af0db837bf9dde26d16c632fe75d8b5bca1f7fc0`.


## Inherited history below, not this branch routing

## Current v145 delivery, 2026-09-12

The user-approved Overview row repair is signed and installed. Source
`8fbe84411ec211f3adfec4409e41f3d49b2607fa`, run `34679678333`, artifact `10293642500`.
Manual Shield review confirms both BOOP defaults buttons and all four Overview
cards are visible; Use BOOP defaults was left with visible cyan focus, unpressed.
The settings/receipts, disabled list, background limits and 130% text setting were
unchanged across installation. Only the six flexible Overview child heights and
app version changed; preset logic, artwork and other branches remain untouched.
Local and signed gates passed. See SESSION_HANDOFF.md for full hashes and evidence.
Earlier v143 not-installed/failed-visibility entries below are historical. The
preset's full Apply/Undo/reboot and user acceptance are still separate.

## Live v143 Overview defect confirmed, 2026-09-12

The device reports installed `143 / 1.2.143-boop-shield-defaults`. A fresh live
screen inspection found both defaults buttons AND the four action cards collapsed
to one-pixel-high rows below the status boxes. The Android view tree confirms
one-pixel heights; this is not a navigation-location or missing-build problem.
Focus was moved onto **Use BOOP defaults**, but the collapsed control cannot show
a usable visible focus treatment. No Apply, Undo or package action was selected.
The scrollable Overview uses MATCH_PARENT-height weighted children in
WRAP_CONTENT-height rows. Repair that scoped sizing behavior on this branch before
claiming the buttons are visible or promoting v143 as physically accepted.
The current user request was recheck/highlight only. No app-code edits, build,
install, permission changes or package mutations were made in this investigation.
Previous source/signing evidence below remains valid, but is not visual acceptance.

# BOOP defaults continuation memory

The requested feature is BOOP-branded: Use BOOP defaults and Undo BOOP defaults.
Work only in boop-shield-defaults, branched from accepted v135 plus its docs at
582bd0d404a3d4ca61abd718551f7af5ef20aabe. Do not disturb ongoing lyrics/animation
or integration work. The user approved implementation with go; deployment is
separate. Candidate143 is not a merge of other later-numbered app branches.

Preset v1 is frozen: 14 exact IDs / nine disables / nine boot selections / six
paired background limits. See StartupDefaultsProfile.java and the spec. Do not
promote broad NVIDIA/Android categories to dynamic disable rules. Exclude the
three older/unattributed disables and Kodi's prior single app-op setting. A new
profile revision must preserve compatibility with older Undo journals; do not
silently edit the frozen list in place.

Share requested actions, not private Restore snapshots. Each receiving Shield
records its own exact pre-preset state before writes. Group Undo must not call
global Restore blindly: doing so would erase earlier customizations. Preserve
original individual ledger bytes and untouched dimensions. Keep newer-state or
ledger conflicts, interrupted journals and the first baseline. Keep current
settings is an explicit record-only escape; it is not a device reset.

The native review is required before Apply. Active input/accessibility providers,
foreground packages and recovery components are skipped. Require NVIDIA TV,
completed setup/current user/local control and an available BOOP HOME before
changing the stock launcher last. No new grants or platform text-size changes.

Tests and Android compilation passed. This is not real-device acceptance of the
preset or its new review layout. No physical device changes have been performed.
The original accepted v135 app is the source baseline; later installed device
state belongs to concurrent user-authorized work and must be rechecked before
any deployment. Read SESSION_HANDOFF.md for current build/provenance.

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
