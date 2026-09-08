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

The floating-square Apps drawer is physically accepted. HOME geometry/chrome 0.9.4 is physically good. Ryan owns visual acceptance; GitHub must not run screenshot/golden/layout/animation judging.

## Physically proven Now Playing authority

The real Shield settled the permission path:

- v0.10.1 settings routing failed physically;
- v0.10.2 Accessibility media delivery failed physically, including after service toggle and app restarts;
- Ryan manually enabled **Shield Settings -> Apps -> Special app access -> Notification access -> BOOP Now Playing**;
- Deezer Now Playing appeared immediately after HOME refresh.

Therefore Notification Listener access is the proven Shield authority. Accessibility is HOME-only again. `Media access` reflects the Notification Listener grant and tries the exact Android TV Notification Access activity first, then generic/detail listener fallbacks.

## v0.10.3 physical result

Version 18 / `0.10.3-now-playing-layout`, build source `db7aadc18c872b75da8dfa2713c520c7e39d993b`, workflow `34284059958`, was physically tested by Ryan.

Result: **much better**. The large headphones BOOP collision was solved by the reserved/clipped 230dp mascot bay and `Launcher Settings` stayed on one line.

Remaining physical issues from Ryan's screenshot:

- transport buttons were still a little too large and visually overlapped/crowded;
- Apps / Launcher Settings / Settings needed moving down a touch;
- album-art square remained grey even though Deezer title/artist/playback data was present.

Favourite apps looked intact and were not targeted for redesign.

## Album-art root cause

`ShieldNowPlayingManager` already checked MediaMetadata ART, ALBUM_ART, DISPLAY_ICON and their local URI forms. The live Deezer session supplied no usable artwork through those fields, so the square stayed grey.

The physically-enabled Notification Listener was still discarding posted notification payloads entirely. v0.10.4 therefore adds one narrow fallback: for **media notifications only**, BOOP may extract artwork from the notification large icon / picture fields and match it to the active player package. Notification title, body, messages and actions remain ignored.

MediaMetadata artwork still wins whenever present; notification artwork is fallback only.

## Current candidate: v0.10.4 artwork + spacing

Version code 19 / `0.10.4-artwork-spacing`.

Physical-layout candidate changes:

- transport buttons reduced from 78x48dp / 16sp to **70x42dp / 15sp**;
- controls row height 52 -> 46dp and top margin 10 -> 8dp;
- only the top navigation row is translated down **8dp**;
- Now Playing card remains 182dp high;
- mascot reservation remains 230dp;
- Favourite apps geometry/order/grab behavior is unchanged.

Artwork fallback changes:

- listener processes only notifications containing `Notification.EXTRA_MEDIA_SESSION`;
- reads artwork only from large-icon / picture bitmap or icon fields;
- no notification text/body/action parsing was added;
- manager stores fallback artwork by player package and uses it only if MediaMetadata art is null;
- fallback is removed when that media notification is removed or listener observation stops.

## TDD / fast CI evidence

Artwork boundary RED:

- test commit `dc8d969875766b0d926a808216f7ee5a47951e4d`;
- workflow `34284805898`;
- **69 functional tests, exactly 1 failure**, `managerAcceptsArtworkOnlyNotificationFallback`, because the required manager boundary did not exist yet.

Production artwork boundary: `8052b830430ba90cdb5ac67ce8f2dc0a023067fb` plus notification extractor `a9cd75eec01e8da7df0564d4997e6dd66a66afa6`.

Manual-layout commits: compact controls `0ff23b74d9fa80ccec21c36bee67e338a2ae44cc`; top-nav nudge `bdba8d1cf32d9e53813080f7cb3eb81fe86c7347`.

GitHub fast lane still sets `BOOP_SKIP_MANUAL_VISUAL_TESTS=1`. It does **not** run screenshot, golden-image, emulator appearance, layout, focus-scale or animation visual acceptance. It does run functional compilation/tests, signed assembly, exact package/version, protected manifest/service/resource presence, permanent signer verification, APK archive integrity and artifact upload.

## v0.10.4 release receipt

- Final APK source: `184974d97126d4adbf9f4c573bac04d109065c59`
- Workflow: `34285227706` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10079223274`
- Version: code 19 / `0.10.4-artwork-spacing`
- APK SHA-256: `fe7007f29b36b69b43f90f692906da1ac55e2b1b26c8d53fcbcf7eb0775a1541`
- Artifact ZIP SHA-256: `047b8475578fb0265b86e48042bbab30f8292c00a537cf23bba9e3f9c818e377`
- Permanent BOOP signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

The downloaded artifact was independently unpacked after CI. Its APK SHA matched the workflow receipt exactly; `badging.txt` confirmed `com.boop.shieldhome`, code 19 / `0.10.4-artwork-spacing`; `signer.txt` confirmed the established BOOP signer.

**v0.10.4 is functional/build/sign/package green only. Album art and spacing are physically pending Ryan's Shield.**

## Next physical check

Install/update v0.10.4 over the existing launcher, then:

1. Deezer -> HOME: check whether album art now fills the left square.
2. Check all five transport buttons for clean separation and remote focus/use.
3. Check the top three navigation buttons now sit comfortably below the screen edge.
4. Confirm headphones BOOP remains inside his right-hand bay.
5. Confirm Favourite apps remains unchanged.
6. Preserve single Home -> BOOP and double Home -> native Recent Apps.

Real Shield behavior is the authority. Do not merge into unified until Ryan explicitly approves the standalone behavior.
