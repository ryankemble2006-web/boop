# PAUSED: concurrent publisher on the colour feature branch

This isolated WIP snapshot preserves the local draft from this session, not a
build or runtime acceptance. The installed Wall app was not changed here.
While this session was writing v159, another publishing pass advanced the same
feature branch to 04f7c10cd6c1c1efb07d915eb223c7b544b6c89c using an earlier
incomplete snapshot. Its handoff says it will complete the same feature.
Do not run two writers or deployments for the same feature.

Original v158/v159 local branches and indexes are preserved. This is a separate
snapshot, not a force-push over the concurrent branch. Local tests pass: 397
colour/state checks, 25 HA-flow checks, 16 protected file hashes, and wiring.
Android compile, emulator runtime and live HA sharing remain unverified.
Animation speed is not started. Nothing was installed. Do not deploy this WIP.
Next: establish one owning session, reconcile this local colour draft with the
remote recovery workflow, complete build/runtime gates, then animation speed.
Pixel 10 remains excluded; no physical devices were accessed in this session.

## Active work: shared eye colour, then animation speed (2026-09-13)

Owner: `boop-unified-eye-sync-safe-v159`, based on accepted v156 at
`a901c1e9f31e55c710e31ac7ff4f5924c9769d56`. The Shield polish sweep,
favourites selector and artist navigation are already user-accepted.
The v158 draft contained overlapping writes with incompatible binding APIs;
it remains preserved. This isolated branch owns the reviewed continuation.

Colour implementation: all existing canonical eye surfaces observe the same
`boop_eyes/hue_degrees` key. Working Wall hue classes, master, shaders and all
26 authored clips are pinned unchanged. Sharing is OFF on upgrade. Explicit
opt-in reuses paired HA OAuth and one marked persistent input_text helper;
ordinary startup cannot create helpers or overwrite a stale shared value.
No UDP receiver, new service, permission, voice or artwork change is included.

Verification so far: tests were red before implementation; 397 colour/state
checks and 25 HA-flow checks now pass, alongside protected-file integrity and
lifecycle/source wiring checks. No Android compile or runtime pass is claimed
at this checkpoint. Full signed CI build and local emulator tests are next.
Animation speed is NOT implemented yet. No physical device has been queried
or changed in this work. Pixel 10 remains excluded. Do not deploy before gates.
Research/design: docs/superpowers/plans/2026-09-13-shared-colour-and-speed.md.

## Current continuation: v156 Now Playing artist link (2026-09-13)

Owner: `boop-unified-artist-link-v156`, based on v155 at
`d168925998c77d59875d15e3402b01e55ec41bc8`. Ryan accepted the favourites
selector: "your favs button is lush ta". Preserve it and all earlier approvals.
Artist text now accepts D-pad focus and turns cyan only while focused. From
Pause, Up reaches progress and Up again reaches the artist; Right from album
art also reaches it. OK opens that exact artist in the installed Deezer app.
Artist Down returns to the progress bar; bar Down still goes to Pause.

Browsing uses public recording metadata only on click, preferring Deezer's
validated recording ID. Otherwise exact title/artist/album matching must yield
one artist ID, never a name-only guess. No autoplay or transport command.
Cancelled, stale-track or background-host requests cannot open a late page.
No artwork, animation definitions, corner geometry, seek deltas, 250 ms hold,
favourites, permissions or signing policy are changed.

Three feature tests were red on v155, then green. Eighteen artist identity
cases and the existing seventeen favourite behaviour checks passed locally.
GitHub signed build, local TV runtime checks and Shield delivery are pending.
Both physical Pixels remain untouched; Pixel 10 remains on hold.

## Current continuation: v155 Add favourites selector (2026-09-13)

Owner: `boop-unified-add-favourites-v155`. Signed source `dbb00edff3e9d7cd46e8a80fa0152678e1e5a179`.
Package/version: `com.boop.alpha1`, `155 / 1.2.155-add-favourites`.
GitHub run `34749730386` completed SUCCESS; artifact `10315522619` (`BOOP-Unified`).
APK SHA256: `603ef36c811c101b9137f8010a2126d532e4680e3b6d9452b9d53e9667912fe7`.
Permanent signer: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Source, actual package/version, APK ZIP integrity, digest and signature were verified.

Approved feature: a persistent + Add favourites tile after the last favourite.
OK opens BOOP's own installed-app selector. Saved and duplicate components are
omitted; choosing one app appends it without launching it, preserves the existing
order, returns Home and focuses the added app. Back/Cancel makes no saved change
and returns focus to +. The + tile stays last after reordering, is not saved as
an app, and owns the right-edge stop. Empty/all-added states remain usable.
No Nvidia launcher, database or external picker is required.

Verification: new model/wiring checks were red on v154 and green on v155.
Seventeen Java favourite behaviour checks and the focused source regression
suite passed, followed by the complete non-visual GitHub build/test/sign pipeline.
The exact signed APK was exercised on the local Android TV AVD before deployment:
reaching +, repeated Right stopping there, opening the selector, cancellation
with unchanged preferences, adding one app, focusing the new favourite, excluding
it afterward, all-added messaging, and reordering while + remains last passed.
The emulator fixture order was restored through the app UI. Restart persistence
passed. An initial rapid post-restart key batch did not reach +; that attempt
was not counted. A repeat checked first-card focus before navigating and passed.
No additional production changes were made for that test sequencing.

Shield-only `adb install -r` returned Success after emulator checks. Installed
version and base APK SHA256 match the signed artifact above. All 23 existing
Shield favourites and their order match the pre-install values exactly. BOOP
Home launch returned Status ok. Final physical selector/visual acceptance remains
Ryan-owned and PENDING. Both physical Pixels were left untouched. No permission,
lock, signing, data-clear or stock-launcher restoration changes were made.
The desktop connection briefly rate-limited an install request before execution;
the later successful retry was verified rather than assumed.

Preserve accepted v153 thin cyan corners, v154 seek-flash bay, all animation
designs, 10-second seek, Down-to-Pause and 250 ms hold. Media/art paths were not
edited. Next: Ryan inspects + and the selector on Shield. Private logs, dumps,
emulator fixture backup and the signed artifact cache remain ignored.

## Current continuation: v154 seek-layout flash (2026-09-13)

Owner: `boop-unified-seekflash-v154`; signed source `66e93bc196ce5111321318c715ecb8fd5e8b6914`.
Package/version: `com.boop.alpha1`, `154 / 1.2.154-seek-layout`.
GitHub run `34748063810` completed SUCCESS; artifact `10315325483`, BOOP-Unified.
APK SHA256 `d4e8d6f349405617ee2f66e5486d788b642972310c8719aee42b4d0794faeba0`.
Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Source receipt, APK ZIP integrity, actual package/version, digest and signature
were checked before Shield install. `adb install -r` returned Success; installed
version 154 and on-device base APK digest matched the signed artifact exactly.

Ryan signed off 10-second seeking, Down from the bar to Pause, the 250 ms hold,
and v153 thin corners. Preserve them. v154 changes only the media row's parent
for the existing puppet: a persistent non-focusable bay keeps its width when
media ownership hides the child. No animation/ownership/seek/focus logic changes.
The pre-fix, progress-focused real-Shield seek reproduced the reported jump:
control width 1082 -> 1450 -> 1082; Lyrics/Close player shifted right by the same
368 pixels. The activity and views were not recreated. Two structural ownership
checks were red before the fix and green after; 10 existing focused guards passed.
Full GitHub compilation/tests/signing succeeded; no visual CI checks were added.

Shield launch returned to BOOP Home. The initial post-install width was 1082.
The seek recheck was ABORTED when Johnny Castaway MainActivity became foreground;
The automated post-fix seek check was not completed. Ryan subsequently confirmed
the fix on Shield: "sweet fix.. nice" (2026-09-13). The v154 seek-flash fix is now
USER-ACCEPTED; do not relabel the interrupted automated check as a pass. Preserve
this bay fix alongside the accepted thin corners, 10-second seeking, Down to
Pause and 250 ms settings hold. Continue only with the next user-directed item.
Neither Pixel nor any emulator was queried or changed in this task. Pixel 10
remains on hold. No permission changes, lock bypass, data clear or signer changes.
Private captures/dumps and scratch checks remain ignored, not published.

## Current continuation: v153 artwork corners (2026-09-13)

Owner: `boop-unified-artframe-v153`; signed source `cb61f62a6c3a249fed82abfd9e629ffea0e70ea2`.
Version: `153 / 1.2.153-artwork-corners`, package `com.boop.alpha1`.
GitHub run `34745915929` succeeded; artifact `10313822926` (`BOOP-Unified`).
APK SHA256: `61394f68a9ce06ae552be199fb67a95c5a1ee67048bff5385bca392492742dae`.
Permanent signer: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Package/version, source receipt, ZIP
integrity and signature independently verified before installation.

Shield `adb install -r` returned Success; installed base APK hash matches exactly.
BOOP Shield Home launch succeeded and resumed activity was verified. No emulator
was used under the cosmetic-change override. Neither Pixel was queried, changed
or installed in this task. No permission, lock, data-clear or signer changes.

v151 and v152 did NOT fix the corner bleed in Ryan's inspection. v153 restores
artwork frames from 8 dp to 4 dp and subtracts half the stroke width from the
stroke-centre radius so the OUTER arc matches the image clip. Only FocusChrome
changes at runtime; media seeking, 250 ms hold, normal chrome and animations remain.
The regression failed before correction; 12 Java geometry cases and 10 focused
source contracts passed locally, then the entire GitHub workflow passed.

Fresh physical Shield screenshots were inspected locally: the white crescent
visible outside the v152 cyan corner was absent in the captured v153 wide-icon
and album-art corners. First post-launch unfocused captures were not used as
proof. Screenshots stay private/ignored; no visual CI checks were added.
Ryan accepted the v153 Shield corner fix: 'awesome onto next'. Preserve this geometry.
Next on the existing polish list is acceptance of the already-implemented progress
bar navigation: Up from transport controls focuses the bar, Left/Right request
minus/plus 10 seconds, and Down returns to Play/Pause. Seeking is enabled only
when the selected session advertises seek support and has a known duration.
These controls landed in v151 and remain in installed v153; do not rebuild them
as a new feature. Favourite right-edge stop and 250 ms settings hold are also
already present. This continuation verified source wiring and Shield version,
not a new physical seek/navigation pass. No new APK or device changes; Pixel 10
remains on hold and Pixel 7/emulators were not accessed in this continuation.

﻿# BOOP v148 status

- Branch: `boop-unified-animation-v148`
- Source checkpoint: `80cd81c1`
- Scope: exclusive face ownership for Voice Settings, Developer Menu and notification preview
- Red proof: four ownership tests fail on untouched v147
- Green proof: four ownership tests pass on v148; Java ownership harness passes 8 scenarios
- Clean detached build: materialization + Android compile + focused unit tests green
- Local release APK: not produced because stable BOOP signing remains GitHub-owned
- Preserve: three unrelated dirty Shield files remain local and excluded
- Next: v149 independent BOOP animation timing, then signed install/physical checks on Pixel 10 + Shield
# BOOP v147 current status: installed on both devices

Updated 2026-09-13. Owner/worktree: `boop-unified-v146-integration`.

## Current build

- Package `com.boop.alpha1`; version `147 / 1.2.147-duplicate-cleanup`.
- App source commit `35bf096d4efe5bf40990dbc12d493cd2372e7c33`.
- GitHub Actions run `34732219690`: SUCCESS; artifact `10309931232`, `BOOP-Unified`.
- Signed APK SHA256 `2f56fc31645b34c69670d1b3abc4d3d63382d8bc762bf339151e43d59faa5db9`.
- Permanent signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Duplicate-render cleanup

- Phone notification/dev/voice portrait surfaces draw one canonical cropped eye pair instead of the whole approved eye atlas.
- Shield Now Playing masks the legacy baked-in headphone eyes before the canonical GLES eye surface draws.
- No animation clip, approved master artwork file, signing setting, permission or unrelated feature logic was removed.
- Regression guard `tests/test_unified_duplicate_puppet_renderers.py` was red against pre-fix sources and green after restoration.

## Verification and installation

- CI: 235 focused Unified tests + 68 focused Shield tests, zero failures/errors/skips; package/signature/ZIP integrity verified.
- Explicit `adb install -r` returned `Success` on Nvidia Shield and Pixel 10 Pro XL.
- Both devices report v147 and the installed `base.apk` on each device hashes exactly to the signed artifact above.
- BOOP was launched on both after install. No uninstall, data clear, permission grant or signer change.
- Ryan owns visual acceptance; duplicate appearance is not yet claimed physically green.

## Preserve

Three unrelated dirty Shield overlay source files remain local and excluded from this delivery: `BoopHomeActivity.java`, `HomeDashboardController.java`, `TvSettingsView.java`. Preserve them as concurrent work.

Use this v147 combined branch as the current Unified app. See `SESSION_HANDOFF.md` for the complete v146 history and exact v147 receipts.


# v149 signed/install verification - 2026-09-13

Source commit `23b4646ceb465a5438fcf8dc980651651616e1b5`, branch `boop-unified-animation-v149`.
GitHub Actions run `34738750812` completed SUCCESS. Artifact `BOOP-Unified` / ID `10311787700` was built from that exact commit. CI passed exclusive-face ownership, Android-scale-independent BOOP motion, canonical animation transplant, notifications/developer/Natural Voice, Shield control, wake/lifecycle, signing/package/archive and remaining workflow gates.

Signed APK package/version: `com.boop.alpha1`, `149 / 1.2.149-independent-motion`. APK SHA256 `1fea7893c4a26e190d064ab5f758daf822fac2ebdd1b649f163a3dd59e916847`. Permanent signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Explicit `adb install -r` returned Success on Nvidia Shield and Pixel 10 Pro XL. Both report v149 and both installed `base.apk` hashes exactly match the signed artifact. No uninstall/data clear/permission grant/signing change was performed.

Physical Shield independent-motion verification: Android window, transition and animator scales were all 0. Unified routed to `com.boop.alpha1/com.boop.shieldhome.ShieldLauncherActivity`; the resumed activity was explicitly confirmed before capture. Three screenshots 350 ms apart produced three different hashes, proving live Unified motion continued with Android animation scales disabled. Johnny Castaway can take the dream foreground later; any hierarchy captured while it owned foreground was discarded.

Phone duplicate visual acceptance is still pending. The physical Pixel is securely locked and normal `wm dismiss-keyguard` did not remove the keyguard; no PIN was entered or bypass attempted. A fresh Pixel 10 emulator installed v149, but its Android System UI entered the already-known ANR state, so its UI hierarchy was also discarded. Source red/green ownership tests and CI are green, but they are not substituted for Ryan's real-device visual acceptance.

## Release workflow rule - 2026-09-13

Ryan's standing BOOP rule: do not stop to ask for approval on routine build/release gates after source intent is already approved. Push the reviewed source, let GitHub perform materialization, tests, signing, packaging and integrity checks, then install the resulting signed APK on both Nvidia Shield and Pixel 10 Pro XL automatically. Ryan owns final visual inspection only. Do not substitute local unsigned builds for this pipeline.

## v150 signed device install - 2026-09-13

GitHub Actions run `34741902983` completed SUCCESS from source `28a2d92c58ecbab4fc42f8c84206d383ea03a997`. Signed artifact `BOOP-Unified` is `com.boop.alpha1`, version `150 / 1.2.150-canonical-notifications-nowplaying`, APK SHA256 `a13957ff8c02e2d5062d3993ed8113d005d6ad1831b9649994240eb9e8a54ba0`, permanent signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

`adb install -r` returned Success on Nvidia Shield and Pixel 10 Pro XL. Both devices report v150 and both installed `base.apk` hashes exactly match the signed artifact. Shield launch resolves to `com.boop.alpha1/com.boop.shieldhome.ShieldLauncherActivity` and was left foreground for Ryan's visual inspection. Pixel launch was requested successfully but the secure keyguard remains showing; no PIN entry or bypass was attempted. Visual acceptance remains Ryan-owned.
