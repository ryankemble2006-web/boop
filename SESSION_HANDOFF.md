# BOOP Shield clean launcher handoff

Updated 2026-09-08. Authoritative branch: `boop-shield-clean-launcher`.

## Product boundary

Standalone Nvidia Shield clean-HOME validation app. Package `com.boop.shieldhome`. Unified/AIO `com.boop.alpha1` remains separate and untouched. Do not merge into unified until Ryan explicitly approves after physical Shield testing.

## Protected physical baseline

Preserve the v0.8 HOME mechanism: single Home -> BOOP; double Home -> native Nvidia/Shield Recent Apps; reboot rearm; stock Android TV Home remains installed/enabled for recovery/trigger. Preserve accepted banners, grab/reorder, floating-square Apps drawer, 0.9.4 HOME geometry/chrome and normal Shield system behavior.

Ryan owns visual acceptance. GitHub must not run screenshot/golden/layout/animation judging.

## Physically proven Now Playing authority

Notification Listener access is the proven Shield media-session authority. Ryan manually enabled **Shield Settings -> Apps -> Special app access -> Notification access -> BOOP Now Playing**, and Deezer Now Playing appeared immediately. The Accessibility media experiment failed and Accessibility is HOME-only again.

`Media access` reflects the Notification Listener grant and tries Android TV's exact Notification Access activity first, with generic/detail listener fallbacks.

## Physical layout progress

v0.10.3 removed the large headphones-BOOP collisions and Ryan reported it **much better**.

v0.10.4 / code 19 (`184974d97126d4adbf9f4c573bac04d109065c59`, workflow `34285227706`) reduced transport controls, moved the top navigation row down 8dp and added a notification-artwork fallback. Ryan physically reported **awesome spacing**. Remaining requests/results:

- lift the transport row upward by 4dp;
- album art still absent / grey.

## v0.10.5 remote-artwork candidate

Version code 20 / `0.10.5-remote-artwork`.

Visual change, manually judged only:
- transport controls row top margin is 4dp instead of 8dp, lifting Prev / Rew / Play-Pause / Fwd / Next by exactly 4dp;
- no other accepted layout geometry is intentionally changed.

Artwork investigation/fix:
- prior manager accepted MediaMetadata bitmap artwork and only local `content`, `file`, or `android.resource` artwork URIs;
- HTTPS MediaMetadata artwork URIs were therefore discarded;
- v0.10.5 adds a background `NowPlayingArtworkResolver` that accepts HTTPS ART_URI / ALBUM_ART_URI / DISPLAY_ICON_URI, caches results and republishes the selected snapshot when the image arrives;
- network artwork is HTTPS-only, off the UI thread, with 4s connect / 6s read timeout, image MIME checking and a bounded cache;
- manifest adds ordinary `android.permission.INTERNET` solely for optional remote cover artwork;
- notification artwork fallback now accepts either a media-session token or Android `CATEGORY_TRANSPORT`, plus legacy/current large-icon and picture artwork forms;
- MediaSession bitmap art remains first choice, then local/remote MediaMetadata URI art, with notification art as fallback;
- notification title, body, messages and actions remain ignored.

## TDD / CI evidence

Remote-artwork policy RED:
- test commit `1536be0b51a7f6e0db9d0cb71ae1aaf5e5fffc59` required local/HTTPS/unsupported URI classification and transport-notification classification;
- production policy began at `337c43f86fb51c3dc731b9e9492ac7f18b6d87a5`;
- async resolver `90afc531af2080f400f8adca2267d3b465ca059d`;
- manager integration `c34472f96824aef258b22549d75087f981218874`;
- 4dp transport lift `7d5503f1d94e66fb68d760044a89990fd958a6c7`;
- transport notification artwork `99d973147685fc8331221d536109503af90e9fd6`;
- INTERNET permission `00debabab01091031b511badad52996a116315f0`;
- JVM Android-URI stub exposed one test-only mismatch; URI policy was made pure Java in `771682ce05eb5f7353a1560f8751465f8fa5a611`.

The corrected functional suite then passed. Final release source:
- `102ea618935bb5a8c7b0fef14657ff3e75756262`
- workflow `34286575434` SUCCESS
- artifact `BOOP-Shield-Clean-Launcher`, ID `10079722116`
- APK SHA-256 `01e4e81538f2c8048aeff930b7ef8b5838d85f7ea0d6e701b6d1e193b3b98363`
- artifact ZIP SHA-256 `df875000340b08e936f8f867d9f59c6ed88224e808695283c3c6753796758458`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Final CI passed functional tests, signed assembly, exact package/code20/version, INTERNET manifest presence, protected HOME/Accessibility/Now Playing manifest checks, permanent signer verification, APK integrity and artifact upload. `BOOP_SKIP_MANUAL_VISUAL_TESTS=1` remained active; no visual acceptance was run.

The downloaded artifact was independently unpacked: APK SHA matched CI exactly; `badging.txt` confirmed `com.boop.shieldhome`, code 20 / `0.10.5-remote-artwork`; signer matched the permanent BOOP certificate.

## Next physical gate

Install/update v0.10.5 over the current launcher. No new special-access grant is needed for INTERNET. Skip to a fresh Deezer track and return HOME. The album-art square may populate shortly after HOME because HTTPS artwork is asynchronous. Check the exact 4dp-higher transport row.

If album art is still grey, do not add more blind artwork fallbacks. Add only sanitized diagnostic evidence for MediaMetadata artwork fields and URI scheme/host, without logging notification text or personal content.

Real Shield behavior is authority. Do not merge into unified until Ryan explicitly approves the standalone behavior.
