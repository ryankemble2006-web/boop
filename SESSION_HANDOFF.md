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

Visual baseline remains the physically-good 0.9.4 HOME geometry/chrome plus the 0.9.5 stronger focus-pop candidate. The floating-square Apps drawer is physically accepted. Ryan owns visual acceptance; GitHub must not run screenshot/golden/layout/animation judging.

## Now Playing baseline

0.10.0 introduced generic Android MediaSession Now Playing, Launcher Settings media/player controls and the approved independent headphones BOOP layer. Build `f06cee260c98b2b03ddaa67ed19e505078bf3ac1`, workflow `34277141969`, artifact `10076198619`.

The player-supplied metadata/artwork/progress/actions come from Android `MediaController`. The headphones layer remains launcher-owned, non-focusable and non-clickable.

## Physical result: 0.10.1 route hotfix failed

0.10.1 / code 16 attempted to fix `Media access: OFF` by routing to Android Notification Listener settings. Ryan physically tested it and reported **nope**: the Shield still did not provide a usable Notification Access destination.

Do not keep iterating hidden Notification Access Settings routes as the primary Shield setup path.

## Current candidate: 0.10.2 Accessibility media bridge

Version 17 / `0.10.2-accessibility-media` removes the separate Notification Access requirement from the intended Shield flow.

Primary behavior:

- the already-working `BOOP Home Override` Accessibility service still handles `TYPE_WINDOW_STATE_CHANGED` using the protected Home override logic;
- it additionally requests `TYPE_NOTIFICATION_STATE_CHANGED`;
- for notification-state events it extracts only Android's `Notification.EXTRA_MEDIA_SESSION` value when it is a `MediaSession.Token`;
- notification title, body, actions and screen content are not read or stored;
- the token is handed to `ShieldNowPlayingManager`, which creates a `MediaController` and obtains the normal Now Playing metadata/artwork/playback state/actions from the media session;
- `Media access: ON/OFF` now reflects whether BOOP Home Override Accessibility is enabled;
- selecting the Media access row opens the same Accessibility settings route already used for BOOP Home Override;
- the existing Notification Listener service remains packaged only as a compatible fallback if that permission was already granted. It is no longer the intended primary setup path.

Privacy/scope remains narrow: `canRetrieveWindowContent=false`; no notification text parsing, typing, gestures or remote-key interception was added.

## TDD and release verification

RED:

- test commit `62af0ecea0e787b767025ca25f06ad5e678f6c57` required the accessibility policy to preserve Home window handling, add media-notification handling and ignore unrelated events;
- workflow `34279753997` failed only because the new `ShieldAccessibilityEventPolicy` did not yet exist.

GREEN/release:

- final build head: `6f72deafad2c050f8b3f6e283b3b65f45a7cf230`;
- workflow: `34280253602` SUCCESS;
- artifact: `BOOP-Shield-Clean-Launcher`, ID `10077375611`;
- version: 17 / `0.10.2-accessibility-media`;
- APK SHA-256: `9be4f47ca4f20307ca51d5d93d810a9f7f843efde633edb3256f859602d60923`;
- artifact ZIP SHA-256: `2059dc47b5abd41351fc3c97eba64e068001b4b5c28abf527d84dca7b75d383b`;
- permanent BOOP signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Final workflow passed the full focused standalone launcher test suite, signed assembly, exact package/version checks, protected HOME/Accessibility and Now Playing manifest/resource checks, permanent signer verification, APK archive integrity and artifact upload. No screenshot/golden/appearance/layout/animation acceptance was run.

The downloaded release artifact was independently unpacked after CI. Its APK SHA-256 matched the workflow receipt exactly, and `badging.txt` confirmed `com.boop.shieldhome`, code 17, `0.10.2-accessibility-media` with the established BOOP signer.

**0.10.2 is CI/signer/package green only. Whether Shield actually delivers the expected media notification Accessibility event/token must be proven on Ryan's real Shield.**

## Next physical check

Install/update 0.10.2 over the current BOOP Shield Home.

1. If BOOP Home Override is already ON, Launcher Settings should show **Media access: ON** without any separate Notification Access setup.
2. Start Deezer playback and return HOME.
3. Verify the Now Playing panel receives title/artist/artwork/progress/actions and the headphones BOOP responds to playing/paused state.
4. Recheck single Home -> BOOP and double Home -> native Recent Apps. The Accessibility Home behavior must be unchanged.
5. If no Now Playing data appears, report that exact physical result. Do not infer success from CI.

Do not merge into unified until Ryan explicitly approves the standalone behavior.
