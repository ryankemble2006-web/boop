# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Package: `com.boop.shieldhome`
- Unified/AIO `com.boop.alpha1`: separate and untouched
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Physically accepted Apps drawer: 0.9 floating square icons
- Physically-good HOME geometry/chrome: 0.9.4
- 0.9.5 stronger focus pop remains a physical visual candidate
- 0.10.1 Notification Access route: **physical FAIL**
- Current candidate: version 17 / `0.10.2-accessibility-media`
- Build head: `6f72deafad2c050f8b3f6e283b3b65f45a7cf230`
- Workflow: `34280253602` SUCCESS
- Artifact ID: `10077375611`
- APK SHA-256: `9be4f47ca4f20307ca51d5d93d810a9f7f843efde633edb3256f859602d60923`
- Artifact ZIP SHA-256: `2059dc47b5abd41351fc3c97eba64e068001b4b5c28abf527d84dca7b75d383b`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- CI/signer/package green; real-Shield Accessibility media delivery is pending Ryan

## 0.10.2 change

The Shield no longer needs a separate Notification Access setup for the intended Now Playing path.

- Existing BOOP Home Override Accessibility keeps its protected Home-window behavior.
- It now also receives notification-state events.
- BOOP extracts only `Notification.EXTRA_MEDIA_SESSION` when it contains a `MediaSession.Token`.
- Notification text/content/actions are not read or stored; `canRetrieveWindowContent=false` remains.
- The token feeds the existing `MediaController` Now Playing path for metadata, artwork, playback state and transport controls.
- Launcher Settings `Media access` now follows the already-enabled BOOP Home Override Accessibility service and opens Accessibility settings.
- The Notification Listener service remains only as an optional compatibility fallback, not the intended Shield setup route.

TDD RED `62af0ecea0e787b767025ca25f06ad5e678f6c57` failed on the intentionally missing accessibility event policy. Final run `34280253602` passed focused functional tests, signed build, exact code17/version/package checks, HOME/Accessibility and Now Playing manifest/resource checks, permanent signer verification, archive integrity and artifact upload.

No GitHub screenshot/golden/appearance/layout/animation acceptance was run. The downloaded APK independently matched the CI SHA exactly.

## Locked behavior

Preserve:

- single Home -> BOOP;
- double Home -> native Recent Apps;
- BOOP Home Override across reboot;
- stock Android TV Home installed/enabled as recovery/trigger;
- real wide HOME banners and grab/reorder;
- accepted floating-square Apps drawer;
- no HOME black focus plate or normal favourite stars;
- artwork-only HOME focus scaling;
- single Back -> favourite item 1;
- long Back/top-right Settings -> real Shield Settings;
- volume/CEC/system shortcuts and animations.

## Next gate

Install/update 0.10.2. With BOOP Home Override already ON, Launcher Settings should report **Media access: ON** without separate Notification Access. Start Deezer, return HOME and test Now Playing. Recheck single Home and double Home. Real Shield behavior is the authority.

Do not merge into unified until Ryan explicitly approves the standalone behavior.
