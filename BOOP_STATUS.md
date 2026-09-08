# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Package: `com.boop.shieldhome`
- Unified/AIO `com.boop.alpha1`: separate and untouched
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Physically accepted Apps drawer: 0.9 floating square icons
- Physically-good HOME geometry/chrome: 0.9.4
- Notification Listener manually enabled on real Shield: **physical PASS, Now Playing appeared immediately**
- v0.10.3 major collision fix: physical **much better**
- v0.10.4 spacing: physical **awesome spacing**; album art still absent
- Current candidate: code 20 / `0.10.5-remote-artwork`
- Build source: `102ea618935bb5a8c7b0fef14657ff3e75756262`
- Workflow: `34286575434` SUCCESS
- Artifact ID: `10079722116`
- APK SHA-256: `01e4e81538f2c8048aeff930b7ef8b5838d85f7ea0d6e701b6d1e193b3b98363`
- Artifact ZIP SHA-256: `df875000340b08e936f8f867d9f59c6ed88224e808695283c3c6753796758458`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Functional/build/signer/package green; remote artwork and 4dp control lift physically pending Ryan

## Physically proven media path

`Shield Settings -> Apps -> Special app access -> Notification access -> BOOP Now Playing` is the authority path. `Media access` uses the Notification Listener grant. BOOP Home Override Accessibility remains HOME-only.

## v0.10.4 physical result

Ryan reported **awesome spacing**. Requested one final visual tweak: lift the Prev / Rew / Play-Pause / Fwd / Next row by 4dp. Album art remained grey after the notification-artwork fallback.

## v0.10.5 changes

- transport row top margin 8dp -> **4dp**; all other accepted spacing remains unchanged;
- MediaSession bitmap artwork still wins first;
- local MediaMetadata artwork URIs remain supported;
- HTTPS MediaMetadata ART_URI / ALBUM_ART_URI / DISPLAY_ICON_URI can now be downloaded asynchronously, never blocking launcher UI;
- HTTPS only, no cleartext; 4s connect / 6s read timeout, image MIME check, bounded cache;
- normal `INTERNET` permission added only for optional remote cover artwork;
- notification fallback now accepts Android media-session notifications **or** `CATEGORY_TRANSPORT`, and checks large-icon/picture artwork forms;
- notification title/body/messages/actions remain ignored.

Artwork-source TDD RED: `1536be0b51a7f6e0db9d0cb71ae1aaf5e5fffc59`, then production policy/resolver. A JVM-stub issue in the pure URI-policy test was corrected by making the scheme parser pure Java. Final v0.10.5 workflow `34286575434` passed the fast functional/build/sign/package lane.

## Fast CI rule

Ryan owns real-device visual acceptance. CI sets `BOOP_SKIP_MANUAL_VISUAL_TESTS=1`; no screenshot, golden, emulator appearance, layout, focus-scale or animation visual acceptance runs. Functional logic, signed assembly, exact package/version, manifest/service/resource presence, permanent signer, APK integrity and upload remain checked.

## Locked behavior

Preserve single Home -> BOOP; double Home -> native Recent Apps; reboot rearm; stock Android TV Home recovery; banners/grab/reorder; accepted Apps drawer; HOME focus behavior; Back behavior; Shield Settings access; volume/CEC/system shortcuts and animations.

## Next gate

Install/update v0.10.5. Skip to a fresh Deezer track, return HOME and allow a moment for asynchronous HTTPS artwork. Check the 4dp-higher controls row and whether album art appears. If art is still grey, stop blind fallbacks and instrument only sanitized artwork-source diagnostics (field presence + URI scheme/host, no notification text).

Real Shield behavior is the authority. Do not merge into unified until Ryan explicitly approves the standalone behavior.
