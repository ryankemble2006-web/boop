# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Package: `com.boop.shieldhome`
- Unified/AIO `com.boop.alpha1`: separate and untouched
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Physically accepted Apps drawer: 0.9 floating square icons
- Physically-good HOME geometry/chrome: 0.9.4
- v0.10.1 Notification Access routing: physical FAIL
- v0.10.2 Accessibility media path: physical FAIL
- Notification Listener manually enabled on real Shield: **physical PASS, Now Playing appeared immediately**
- v0.10.3 collision layout: **physical result MUCH BETTER**, remaining small control/nav/artwork issues
- Current candidate: code 19 / `0.10.4-artwork-spacing`
- Build source: `184974d97126d4adbf9f4c573bac04d109065c59`
- Workflow: `34285227706` SUCCESS
- Artifact ID: `10079223274`
- APK SHA-256: `fe7007f29b36b69b43f90f692906da1ac55e2b1b26c8d53fcbcf7eb0775a1541`
- Artifact ZIP SHA-256: `047b8475578fb0265b86e48042bbab30f8292c00a537cf23bba9e3f9c818e377`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Functional/build/signer/package green; v0.10.4 artwork/spacing pending Ryan's Shield

## Physically proven media path

`Shield Settings -> Apps -> Special app access -> Notification access -> BOOP Now Playing` is the authority path. `Media access` uses the Notification Listener grant; BOOP Home Override Accessibility is HOME-only.

## v0.10.3 physical follow-up

Ryan's v0.10.3 screenshot confirmed the major mascot collision was solved and described it as **much better**. Remaining issues:

- transport buttons still slightly too large/crowded;
- top Apps / Launcher Settings / Settings row needs moving down a touch;
- Deezer title/artist/playback worked but album-art square stayed grey.

## v0.10.4 candidate

Spacing:

- media controls 78x48dp -> **70x42dp**;
- control font 16sp -> **15sp**;
- controls row 52 -> 46dp, top margin 10 -> 8dp;
- top navigation row translated down **8dp** only;
- existing 182dp Now Playing card, 230dp mascot bay and Favourite apps geometry remain unchanged.

Album art:

- MediaMetadata art remains first choice;
- if absent, BOOP may use artwork only from a **media notification** matched by player package;
- supported fallback sources are notification large-icon/picture bitmap/icon fields;
- notification title/body/messages/actions remain ignored;
- fallback clears when the media notification is removed or listener observation ends.

## Fast CI rule

Ryan owns real-device visual acceptance. CI sets `BOOP_SKIP_MANUAL_VISUAL_TESTS=1`; no screenshot, golden, emulator appearance, layout, focus-scale or animation visual acceptance runs.

CI still verifies functional Java/Android logic, signed assembly, exact package/version, protected manifest/service/resource presence, permanent signer, APK archive integrity and artifact upload.

Artwork TDD RED: `dc8d969875766b0d926a808216f7ee5a47951e4d`, workflow `34284805898`, 69 tests / exactly 1 expected failure on the missing artwork-fallback boundary.

Final v0.10.4 workflow `34285227706` passed the fast functional/build/sign/package lane. The downloaded APK independently matched CI SHA and signer.

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

Install/update v0.10.4 and physically check album art, transport spacing, top-nav vertical position, mascot confinement and unchanged favourites. Then recheck single/double Home.

Real Shield behavior is the authority. Do not merge into unified until Ryan explicitly approves the standalone behavior.
