# BOOP Shield v208: signed candidate; Ryan's artist-focus test pending

Updated 2026-09-17. Application owner remains `boop-wall-shield-split-v207`. Start from its LIVE HEAD, not the old primary laptop checkout or historical standalone apps. Main owns the shared workflow; the current app branch owns implementation evidence.

## Latest delivery

Ryan approved a narrowly scoped Now Playing change after supplying a Shield screenshot: remove the artist label's grey box and outline, keep white text normally, and use the progress bar's cyan (`#4DB8FF`) on focus. Only that label opts out of generic TV button decoration. Its size, spacing, click callback, D-pad routes and availability rules are retained. Other button chrome is unchanged. Voice and approved artwork/animation sources were not changed.

Signed build source: `477f199b6ad8bcf54e9d0eb6256bbb9f9633682c`.
Signed CI: run `35217237866`, job `105188496531`, SUCCESS. Focused split run `35217237801` also succeeded. All 18 inherited non-visual stages and the new source/production-method artist regressions passed, including checks of the materialized sources.

Deliver `BOOP-Shield-v208.apk`: package `com.boop.shieldoverlay`, versionCode `208`, versionName `1.2.208-shield`, 160420201 bytes. SHA-256 `6503557057c1661a37cf4c65f91c63e149808dbde000645ceaea04e3d1eba5a9`. Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. All 16 native libraries match accepted v206 and packaged frozen art matches source. Artifact `10495244358`, `BOOP-Shield-v208-Wall-v207-Signed`, contains the APK and verification receipt. The downloaded archive and extracted Shield APK hashes were verified without rebuilding or re-signing.

The shared pipeline also built Wall for compatibility checks, still version 207. That rebuilt binary is not claimed identical to the previously delivered Wall APK and is not a new Wall delivery request. Leave the user's installed Wall alone.

## Next user check and working boundary

Ryan downloads the actual Shield APK and drags it into his Shield scrcpy window. He checks no artist box in either state, white when unfocused, matching cyan when focused, and the retained artist action/navigation. Installation and visual/physical acceptance are PENDING. Nothing was installed or driven by this session, and no screen capture, emulator or autonomous visual test was run. CI is not physical acceptance.

The 2026-09-17 manual-delivery policy supersedes older joint-testing/assistant-install defaults: GitHub source/review/non-visual tests/builds/permanent signing, then actual APK download; Ryan installs and supplies screenshots/errors. Routine work must not depend on RDC or a mode change. `BOOP_START_HERE.md` has been synchronized to the shared main policy, not an older fallback.

## Preserved continuity

Shield's earlier v207 ASSISTANT-role repair remains: BOOP activity-based assistant, empty voice interaction, original Katniss recognizer. Real Bluetooth microphone capture/command acceptance remains pending; this UI task did not retest or change that state. Preserve subsequent user setup, microphone permission and Home ownership. Never restore historical first-setup/access snapshots automatically.

The last separately verified installations were Wall/Shield v207 from `aa8fd9f6d79f28b441a48df31138a75d38420118`. Their original receipt and the full microphone recovery history remain in the dated v207 handoffs. Voice latency/provider/pitch investigation is frozen for later; all current natural voice behavior, downloads and tuning stay intact. Physical Pixel 10, HA itself, other apps and dirty/concurrent work were untouched.

Detailed current receipt: `docs/handoffs/2026-09-17-shield-artist-text-focus.md`. The four previous root context/handoff/status/memory files are preserved byte-for-byte in `docs/handoffs/2026-09-17-before-artist-focus/`; their historical delivery claims are not new v208 acceptance.
