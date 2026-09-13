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
