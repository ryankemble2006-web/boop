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

﻿# BOOP v148 single-face ownership checkpoint

Updated 2026-09-13. Owner: `boop-unified-animation-v148`.

Scoped source commit `80cd81c1` enforces one visible BOOP face owner across Voice Settings, Developer Menu and notification preview without changing any of the 26 canonical animation definitions or locked artwork. Four regression assertions were run red against untouched v147 and all four failed for the expected ownership gaps; the same assertions pass on v148. `BoopFacePresentationHarness` passes eight ownership scenarios, including delayed animation visibility not resurrecting an occluded face.

A clean detached worktree at `80cd81c1` materialized successfully. Android Java compilation and the focused notification/dev-menu unit tests completed with `BUILD SUCCESSFUL`; local APK assembly correctly stopped at the absent private BOOP signing key, so no locally signed release is claimed. Three unrelated dirty Shield files in the working tree remain excluded from this commit.

v148 is an intermediate checkpoint only. Unified's canonical face still consults Android animator scale, so the next isolated version will transplant the already-proven Animation Lab independent-motion policy before device installation/acceptance.
# BOOP v147 duplicate-render cleanup: signed build published

Updated 2026-09-13. Owner/worktree: `boop-unified-v146-integration`.

Ryan supplied screenshots showing repeated BOOP eye pairs on phone notification/dev/voice surfaces and two BOOP eye layers inside the Shield Now Playing headphones. The duplicate sources were traced without changing the canonical animation catalogue or approved animation timings.

- Phone portrait rendering no longer draws the entire approved eye atlas. `BoopEyeLayout` now supplies a single portrait eye-pair geometry and `BoopFaceView` draws only the two canonical cropped eyes.
- Shield Now Playing retains the approved headphones artwork but masks its legacy baked-in eye slots before the canonical GLES eye surface is drawn. No animation clip, canonical eye renderer, headphone artwork file, permission, signing setting, or unrelated feature logic was removed.
- Unified version candidate is `147 / 1.2.147-duplicate-cleanup`.
- Regression guard: `tests/test_unified_duplicate_puppet_renderers.py`. A deliberate red run against the pre-fix sources failed for both duplicate paths; restoring the fix made both tests pass.
- Fresh app unit tests and Java compilation passed locally. Full APK signing remains GitHub-owned. Visual acceptance is still manual and must not be inferred from source/CI checks.
- Three unrelated Shield overlay files became dirty during existing materialisation patches. They are deliberately excluded from this scoped change and must be preserved as concurrent work.

## v147 signed build receipt

- App source commit: `35bf096d4efe5bf40990dbc12d493cd2372e7c33`.
- GitHub Actions run: `34732219690`, conclusion SUCCESS.
- Artifact: `BOOP-Unified`, artifact ID `10309931232`.
- Package/version verified by CI: `com.boop.alpha1`, `147 / 1.2.147-duplicate-cleanup`.
- Signed APK SHA256: `2f56fc31645b34c69670d1b3abc4d3d63382d8bc762bf339151e43d59faa5db9`.
- Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- CI reports 235 focused Unified tests and 68 focused Shield tests, zero failures/errors/skips. Signed APK package identity, signature and ZIP integrity were verified.
- Physical Shield/Pixel and visual acceptance remain pending. This task did not install the APK, grant permissions, clear data or alter signing configuration.

## v147 device installation receipt

- Explicit `adb install -r` returned `Success` on Nvidia Shield (`SHIELD Android TV`) and Pixel 10 Pro XL.
- Both devices report package `com.boop.alpha1`, `versionCode=147`, `versionName=1.2.147-duplicate-cleanup`.
- Each installed `base.apk` SHA256 is `2f56fc31645b34c69670d1b3abc4d3d63382d8bc762bf339151e43d59faa5db9`, exactly matching the signed GitHub artifact.
- BOOP was launched on both with the normal launcher intent after installation. No uninstall, data clear, permission grant, signing change or unrelated device setting change was performed.
- Visual acceptance of the duplicate-render cleanup remains Ryan-owned and pending; install/package/hash evidence does not claim the screens look correct.

The v146 delivery record follows unchanged below.
# BOOP v146 combined delivery: installed on Pixel and Shield

Updated 2026-09-12. Owner/worktree: `boop-unified-v146-integration`.
Ryan confirmed source work had ceased, authorized all three feature lines to
merge, and explicitly requested installation on Pixel 10 Pro XL and Shield.
This supersedes the inputs' former isolation restrictions for this delivery.
Original branches/checkpoints and the standalone Animation Lab remain untouched.

## Signed build

- Package `com.boop.alpha1`; version `146 / 1.2.146-phone-lyrics-startup`.
- App merge `34ca306dd16ec1575f1c809741e728f610a5fba1`.
- Signed build source `c868421e021ab4e0d3775f71c4fd1b484c1a88c4`.
- GitHub Actions run `34683100171`: SUCCESS; artifact `10294242943`, BOOP-Unified.
- APK SHA256 `4ee558ddf81be95371f07bf5025f581b397e6f16bc47895727fc91e45f576adc`.
- Permanent signer SHA256
  `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The signed download's source/hash/package/version/ZIP and signature were verified.
First run 34682994462 stopped on missing historical provenance text. Original
Unified source pins were restored, not the assertion weakened; no app change.
The subsequent complete run passed all gates.

## Exact merged inputs

- Phone iris/menu/crash fixes: `boop-v144-devmenu-hue-experimental`
  at `2c3c69cd8bc0dd9d251cce281dad2ec28370c38d`.
- Lyrics: `boop-v142-lyrics-fastfail`
  at `0d2c39863ed63d790ec5a2bb4e45d767867229ff`.
- Startup Manager/defaults: `boop-shield-defaults`
  at `503cdb63d64716c9c1a568aadca97ba1d24680cd`.

All three heads are actual ancestors. Shared local bridge changes combined
additively; the independent feature source matches its frozen input. No redraw,
renderer redesign, new permission or silent package/profile change was introduced.

## Tests

Fresh materialization and Android compilation passed. Full local Unified:
295 tests across 85 suites, zero failures/errors/skips, including 18 Lyrics tests.
Twelve phone/menu/constructor/shared-library contracts, 15 Startup Manager suites,
27 defaults coordinator scenarios, eight safety scenarios, profile/journal,
Android 11 linkage, canonical media and listener-seeding checks all pass.
CI: 235 focused Unified and 68 Shield tests, zero failures/errors/skips, plus
separate defaults/Startup suites. Review was in-session, not an independent
reviewer. No visual CI acceptance is claimed; existing deprecation warnings remain.

## Installation and preserved state

Both explicit adb install -r operations returned Success. Fresh device queries
report v146 and the same versionName; each installed base APK's on-device SHA256
matches the signed artifact above. No uninstall, data clear, grant or signer swap.

Private rollback APKs were pulled and verified against their original device hash:
Pixel v145: af10e94b40572904111278004be215dfa0fd93639e8babe1a8ecf68abbf0fddd.
Shield v145: 5b28036f6a3b7114f752bdbdc5273486394fbbafb7e8ca55c56caf7bd37ef6b5.

Shield preferences/no-backup files, disabled packages, paired background limits,
font scale, accessibility and notification listeners matched across installation.
Pixel eyes, voice, wake, HA, launcher/profile and other preference hashes matched,
except boop_notifications.xml changed during the interval. Its exact cause is not
established from hash-only evidence. Pixel no-backup and OS settings also matched.
Do not claim every preference is identical. No defaults Apply/Undo was executed.

## Physical checks and limits

Shield Home rendered with the integrated media puppet and Lyrics button. A first
black capture did not persist after explicit Home re-entry. On the current native
Deezer track without lyrics, BOOP showed `No lyrics for this track.` and remained
foreground. A screenshot and toast record agreed; subsequent D-pad Down moved
visible focus. No UIAutomator or Deezer launch was used on that negative path.
The display has 3840x2160 screenshots but a 1920x1080 input override. An initial
physical-coordinate tap missed; corrected logical coordinates hit the button.
Resolution/density settings were never changed.

Normal Home settings > Startup Manager opened the Overview. Both Use BOOP defaults
and Undo BOOP defaults plus all four cards were visible. No preset was applied.
Shield was returned to BOOP Home after the checks.

After Pixel was unlocked, MainActivity launched and its canonical GLES2 renderer
reported ready. A normal tap woke the intentional idle-black face; the screenshot
showed its saved non-default coloured irises. No new current-process fatal appeared
in the inspected post-launch logs on either device. No lock setting was changed.

The combined positive Lyrics flow, offline timeout, full phone developer-menu/
slider interaction, long-session and preset Apply/Undo/reboot were not exercised
in this final device check. User visual/acoustic acceptance remains separate.

## Continue safely

Use this combined branch and fresh remote heads, not an older isolated v142/v145
branch as the latest app. Current source is committed; future work must retain all
three fixes. Private screenshots/logs, hash snapshots, rollback APKs, probe helpers
and generated build/cache outputs remain ignored and unshared. No credentials or
third-party inspection artifacts were published. No completion music skip sent.


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
