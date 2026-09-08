# BOOP Shield clean launcher handoff

Updated 2026-09-08. Authoritative branch: `boop-shield-clean-launcher`.

## Product boundary

Standalone Nvidia Shield clean-HOME validation app. Do not merge into unified/AIO until Ryan explicitly approves it after physical Shield testing.

- Package: `com.boop.shieldhome`
- Unified/AIO package `com.boop.alpha1` is separate and untouched.
- Stock Android TV Home stays installed/enabled as recovery and as the Accessibility override trigger.
- Normal use must not require ADB, developer options, root or Shizuku.

## Protected physical baseline

Version 8 / `0.8.0-reboot-rearm`, build `af8ebe1147bd56cc952b874c2e4180bd6a44d15d`, remains the protected HOME mechanism:

- single Home -> BOOP;
- double Home -> native Nvidia/Shield Recent Apps;
- BOOP Home Override survives reboot;
- stock Android TV Home stays available for recovery/trigger;
- banners and grab/reorder work.

Visual baseline remains the physically-good 0.9.4 HOME geometry/chrome plus the 0.9.5 stronger focus-pop candidate. The floating-square Apps drawer is physically accepted. Ryan owns visual acceptance.

## Physically proven Now Playing authority

The real Shield settled the permission question on 2026-09-08:

- v0.10.1 settings-intent routing did not reach usable Notification Access;
- v0.10.2 Accessibility notification/media-token delivery produced no Now Playing, even after BOOP Home Override was toggled, Deezer was force-stopped/restarted and the launcher was restarted;
- Ryan manually opened **Shield Settings -> Apps -> Special app access -> Notification access**;
- `BOOP Now Playing` was listed there;
- enabling it and refreshing HOME immediately made the Deezer Now Playing panel appear.

Therefore Notification Listener access is the physically-proven Android authority for Shield Now Playing. Accessibility returns to its protected HOME-window job only.

Player metadata/artwork/progress/actions continue to come from Android `MediaController`; notification payload text is not used.

## Physical visual result from first live Now Playing

The first working screenshot proved the data path and exposed layout collisions:

- headphones BOOP covered the right side of the card and transport controls;
- `Next` was obscured;
- `Open player` competed with mascot space;
- title space was insufficient;
- top `Launcher Settings` wrapped/clipped;
- Favourite apps row itself remained the protected area and was not targeted for redesign.

## Current candidate: v0.10.3 Now Playing layout

Version 18 / `0.10.3-now-playing-layout` is the physical-layout candidate.

Permission/setup changes:

1. `Media access` status again reflects the actual BOOP Notification Listener grant.
2. Access routing tries the exact Android TV component first:
   `com.android.tv.settings/com.android.tv.settings.privacy.NotificationAccessActivity`.
3. Generic Notification Listener settings is the next fallback.
4. Modern per-listener detail settings is the final Notification Access fallback.
5. Only after those fail may the activity caller fall back to general Shield Settings.
6. BOOP Home Override Accessibility again requests only `typeWindowStateChanged`.

Layout candidate changes, intentionally awaiting Ryan's eyes:

- Now Playing keeps its existing 182dp card height.
- The card reserves a fixed 230dp right-hand mascot bay.
- The launcher-owned headphones view is itself constrained to a clipped 230x154dp bay aligned with that reserved region.
- Title/subtitle are single-line end-ellipsized.
- Five transport controls were compacted to fit in their own middle region.
- `Open player` is reduced slightly and remains outside the mascot reservation.
- `Launcher Settings` widened from 190dp to 220dp and is forced to one line.
- Favourite apps dimensions/order/grab behavior were not changed.

## Fast GitHub delivery lane

Ryan explicitly requested that GitHub stop doing visual checking because real-Shield iteration is faster.

CI now sets `BOOP_SKIP_MANUAL_VISUAL_TESTS=1` and excludes the selected historical appearance/layout contract classes from the CI test source set. GitHub does not run screenshot, golden-image, layout, focus-scale, animation or emulator appearance acceptance.

CI still runs functional Shield logic compilation/tests, signed assembly, exact package/version checks, protected manifest/service presence, permanent signer verification, APK archive integrity and artifact upload.

TDD receipt for the permission route:

- RED commit `1213094c165457b579578d220eb2eec0158656ae`, workflow `34283351013`: failed exactly because `TV_EXACT` and the Android TV component helpers did not yet exist.
- GREEN production route commit `ee6e445a6cf970b973ebac6ab3894da789013338`.
- Notification Listener authority restored in `024fea12efa73a79a505eb1a566614bc5856d1bf`.
- Accessibility HOME-only behavior restored in `7387cb414aaa01727a3fc41d8f3fedac339c7248` / `ef39fffb7bd20a272b56d462fec48fe9810e5d95`.

## v0.10.3 release receipt

- Final APK source: `db7aadc18c872b75da8dfa2713c520c7e39d993b`
- Workflow: `34284059958` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10078781887`
- Version: code 18 / `0.10.3-now-playing-layout`
- APK SHA-256: `a5400182fbf9364c0d60ae2b5f7148682e3c5eb78bb4ad6b9f4a0994974f20aa`
- Artifact ZIP SHA-256: `888231d6633864b50901e6f18461f654b17e6275a0ca22af0f0080ddf6e78edc`
- Permanent BOOP signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

The final fast workflow completed successfully. The downloaded artifact was independently unpacked; the APK SHA matched CI exactly, `badging.txt` confirmed `com.boop.shieldhome`, code 18 / `0.10.3-now-playing-layout`, and `signer.txt` confirmed the established BOOP signer.

**This proves build/package/signing/functionality gates only. The v0.10.3 layout is NOT visually accepted until Ryan tests it on the physical Shield.**

## Next physical check

Install/update v0.10.3 and test the quickest visible gates:

1. Launcher Settings -> `Media access` should open Android TV Notification Access directly.
2. With `BOOP Now Playing` enabled there, Deezer -> HOME should populate the panel.
3. Check whether headphones BOOP stays inside the reserved right-hand region and no longer covers title, `Open player`, progress or any transport control.
4. Confirm Prev / Rew / Play-Pause / Fwd / Next are all visible when supported.
5. Confirm `Launcher Settings` stays on one line.
6. Confirm Favourite apps still looks/behaves unchanged.
7. Recheck single Home -> BOOP and double Home -> native Recent Apps.

Real Shield behavior is the authority. Do not merge into unified until Ryan explicitly approves the standalone behavior.
