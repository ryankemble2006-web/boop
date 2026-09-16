# BOOP Wall and Shield v207: signed clean-install receipt

Date: 2026-09-16. Owner: `boop-wall-shield-split-v207`. Ryan explicitly authorized unattended completion, permanent signing and fresh installation on Shield and Pixel 7. Voice was explicitly frozen and removed as a prerequisite. Physical Pixel 10 was never targeted.

## Source, architecture and checks

Installed build source: `aa8fd9f6d79f28b441a48df31138a75d38420118`, based on live combined owner `841458b8bbc53773d16a18bb0359de1c0550f5e2` and the accepted v206 app at `9d57019d9370dbe3f47061b6e8b0ce8ed5134715`. No old standalone app implementation was restored or merged.

Two application shells consume one shared assistant source/resource tree and the existing shared animation, launcher and TV libraries. Installed identity determines the body, eliminating profile choice. Real HA, room, Voice, access and Android Home confirmations remain. Setup completion is persisted only by the user-facing Continue action; failed persistence does not advance it. Wall keeps the built-in launcher. The current Johnny consumer and provider contract were audited; Shield owns the legacy authority and a new alias without modifying Johnny or weakening its caller guard.

Full signed run `35110823569`, job `104843751519`: SUCCESS, all steps. Focused run `35110823530`, job `104843751768`: SUCCESS. The full build executed all 18 existing v206 verification steps in their original order, followed by split integration checks, both application builds and actual APK validation. Focused coverage includes executable installed identity, production body/setup policies, user-only completion, genuine permission/Home routing, provider ownership/caller restrictions, component qualification, source/art preservation and generated repository self-filtering.

The shell materialization report covers 444 generated main Java/asset inputs. Only the package-independent self-filter in the generated phone launcher changes in that step. The natural Voice source, tuning/controller, MainActivity, natural manifest/download path, accepted Home/card/puppet code and photographic assets remain unchanged. Both actual APKs contain exactly the same 16 native libraries as the hash-verified, permanently signed v206 APK. No audible Voice test or latency fix is claimed.

RED/GREEN history: initial test-first run `35106866646` had three expected missing split/chooser/identity failures and passed protected-source preservation. First implementation run `35109154640` exposed a nonexistent Context-field assumption in AppRepository; reading the actual class led to an executable UID-based fix. Full run `35109154846` reached 1,444 passing music-worker assertions but stopped at the inherited production-change allowlist; only the seven intentional split paths were added. Run `35110162034` passed all inherited/focused checks and both signed compiles, then correctly stopped when its new verifier compared raw AAR native bytes to packaged APK bytes. Comparison against the actual verified v206 APK established all 16 packaged libraries unchanged in the successful final run. No functional check was deleted to make the builds green.

## Signed artifacts and exact installed identities

GitHub artifact `10451993779`, `BOOP-Wall-Shield-v207-Signed`, ZIP SHA-256 `21b61cdeaa5c358cdbf505ef818ff5fdd351286f4acaf8f3f6419c25bc075d74`, 152365350 bytes. The downloaded ZIP digest and structure were verified before staging.

| Target | APK | Package | Version | APK SHA-256 |
| --- | --- | --- | --- | --- |
| Pixel 7 Pro | `BOOP-Wall-v207.apk` | `com.boop.alpha1` | `207` / `1.2.207-wall` | `05436dc79441b69d2cd5c328b809dce22464b6ba0fb120cd302d7cd5084c81dd` |
| Nvidia Shield | `BOOP-Shield-v207.apk` | `com.boop.shieldoverlay` | `207` / `1.2.207-shield` | `d4cff18acc85e7360601de042c37281ae6f262018b428525834ed70188574a0f` |

Both certificate SHA-256 digests equal the existing reference `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. No key was created, exported or substituted. Staged APKs were independently checked by existing Android build-tools apksigner and aapt. On-device installed base APK hashes match them exactly. Wall file size is 160420089 bytes; Shield is 160420201 bytes.

## Pre-uninstall recovery and actual fresh installation

Before either uninstall, BOTH replacements and BOTH existing recovery APKs were staged and identity/hash/signature checked on Yoga. Existing Shield v206 recovery hash: `b5f7b0570eccb171bcda7a2ad4e58e79cb397768ec12851e110f04b238713bca`. Existing Pixel 7 v191 recovery hash: `bfc1204c5da030a94991af021a0d0c4f85adb01b22d28825ce68fa7a065843f1`. These were APK-only backups, not BOOP data/model/credential backups. Current device identities, user 0, old installed hashes and sufficient free storage were checked. The restored Shield package did not already appear in the installed or uninstalled-package inventory.

Shield HOME initially resolved to Unified; the stock TV launcher was disabled. The existing Android Settings activity was opened and independently verified foreground with responsive ADB before removal. This provided a usable Settings/ADB/recovery route without enabling a different launcher, assigning HOME or treating FallbackHome as a proven launcher. The recovery APKs remain privately retained outside the checkout.

Pixel 7: ordinary `adb uninstall com.boop.alpha1` returned Success, with no keep-data option; old package absence was checked. Ordinary install of only Wall returned Success, without permission grants. The normal MAIN/LAUNCHER entry opened the genuine setup page.

Shield: ordinary uninstall of only old Unified returned Success and absence was checked. Ordinary install of only Shield was submitted without grants. Its client response timed out after 180 seconds during an intermittent bridge response failure. No blind reinstall/uninstall retry followed. Read-only queries showed the replacement installed, the old package absent, and the exact expected APK hash/version. Thus the completed installation was recovered by evidence, not by assuming a timeout meant failure. The normal MAIN/LAUNCHER entry then opened genuine Shield setup.

No old BOOP data, downloaded voice model, HA credential or completed-setup flag was restored. No factory reset, Home Assistant modification, unrelated app removal, Pixel 10 command or forced default-Home selection occurred. Existing local dirty v203 source/worktree documents were not modified or synchronized over.

## First screens and subsequent Shield change

Both first screens were observed through UI hierarchy and private screenshots: `Set up BOOP Wall` on Pixel 7 and `Set up BOOP Shield` on Shield. The setup-complete preference was absent/false. The sequence pressed no setup button, did not sign in, did not click Continue and did not write setup flags. Captures and raw diagnostics remain private.

During later access checks, three BOOP Shield notification listeners and overlay access were found enabled. This sequence revoked only the three listeners under `com.boop.shieldoverlay` using Android's disallow-listener command and set only that package's SYSTEM_ALERT_WINDOW operation to ignored. All unrelated listener components were checked unchanged. Accessibility was already off. These were revocations to honor the unconfigured-access brief, not pre-grants. The origin of the intervening enabled state was not determined.

A following read showed Shield's `setup_intro_completed=true` and YouTube foreground. No Continue, sign-in or flag-write command had been issued by this sequence. The actor responsible for that later progression was not established. No further device input was sent after noticing it, and no reset/rewind/reinstall was attempted. Pixel 7 remained at its first setup screen in the latest read.

Therefore: BOTH signed apps are installed and BOTH were initially verified at first real setup, but BOTH are NOT claimed to remain at step one at the end. Latest Shield access read: microphone/camera false, notification listeners false, accessibility false, overlay ignored. Pixel 7 microphone/camera/nearby-WiFi false, BOOP listeners/accessibility absent, overlay not allowed. Android versions without the relevant runtime permission entry are recorded as not applicable rather than a claimed grant. Do not overwrite any later human/concurrent access or onboarding changes.

## Acceptance and next safe step

Build/signing/installation identity and the initial setup screenshots are verified. The accepted v206 UI and prior natural Voice behavior remain protected, not reaccepted by CI. Newcomer onboarding and subsequent real HA, room, launcher/Home, media/lyrics/bounce, appearance/sharing, notifications and Johnny state responses still need Ryan's physical acceptance after his setup. The 12-second Voice latency investigation remains deferred for Astra. Do not create another Voice gate or reset either device to make the report look complete.

Package audit: `docs/handoffs/2026-09-16-v207-package-integration-audit.md`. Plan: `docs/superpowers/plans/2026-09-16-wall-shield-split.md`. Current handoff, status, memory and context supersede historical pending-split notes. Full previous root documents remain exact archive copies in `docs/handoffs/2026-09-16-v207-before-split/`.

## Verification durability after delivery

Commit `5f9dbc4e86c0ec92eba728e8b3fbdf56818c2ece` changes verification/tests only: the exact 16 native hashes extracted from the verified v206 APK are now stored in `split/v206-native-baseline.json`, eliminating a future dependency on an expiring Actions artifact. It changes no application code, model, artwork or installed package. Its follow-up workflow outcome is recorded separately; the delivered build/run and installed hashes above remain the deployment provenance. No follow-up rebuild authorizes silently reinstalling over the observed Shield progress.
