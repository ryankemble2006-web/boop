# BOOP current handoff

Updated 2026-09-20. Owning Shield branch: boop-shield-weather-focus-v221 (long-lived cache-hot branch, not app version). Source worktree for this task is .worktrees/boop-deezer-invisible-v235, isolated from the old primary checkout. Wall remains v207 and was not installed.

## Current: Shield v235 signed and installed; native route responding

Source/build commit: d6a7957d57a94fb7fc2a25266478e7c4d376f220.
Signed workflow35531643785, job106133185445: SUCCESS. Favourite checks35531643786, job106133185094: SUCCESS.
Artifact10611029896: BOOP-Shield-v235-Wall-v207-Signed.
APK BOOP-Shield-v235.apk, 160534893 bytes, SHA256 31d0161a2b3a534c0858ccde3b2aeb0f67474379352b366dc21a8f3cd9119d43.
ZIP SHA256 37d99acc99912d5f306b1f2aa58367583e2581a0d19f2e4cc22e98023c600ba4.
Permanent signer SHA256 f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde.

The exact APK was downloaded and independently hash/size/CRC checked. All 16 native libraries and 8 frozen PNG/shader assets match v234 byte-for-byte, plus CI's accepted baseline checks. Cryptographic apksigner verification passed both in CI and on the laptop with the permanent certificate. The APK was copied to Desktop/APKBOOP and installed by adb install -r on the verified physical Shield. Installed version235 / 1.2.235-shield was read back. Existing app data retained; no permission grants or provider modifications. No Wall install.

BOOP Shield HOME was brought foreground. Installed-app logs subsequently show native read OK saved0 and toggle OK saved1 at stage confirmation, followed by further successful reads. This verifies a real installed BOOP -> existing Home Assistant -> offscreen native Deezer -> matching result path, not only a fake boundary test. One earlier read failed at hardware-binding with IOException; its root cause was not established. Do not claim a completely error-free first connection.

An earlier reversible native probe separately verified saved -> unsaved -> saved on the same track and restored its initial favourite. Do not present that as an observed v235 UI unfavourite test. Ryan's final acceptance of both-screen state/colour, unfavourite and dislike+skip is still pending. A normal KEYCODE_MEDIA_NEXT was sent once as his requested ready signal, after installation and successful native receipts. Never use dislike for an attention signal.

## Approved UI and actual native mechanism

Lyrics LEFT: crossed-out dislike-and-immediate-skip icon, outlined with ordinary focus highlighting only.
Lyrics RIGHT: favourites toggle. HOME Now Playing: same favourites toggle after Next.
ONLY those two favourites toggles fill, and only when confirmed saved. Colour is resolved from FocusChrome.accentColor(context) when drawn, following the user's slider, not hardcoded orange. Three transport positions, artwork, text, progress and accepted v233 lyric fallback remain unchanged.

Native Deezer301000101 has no advertised rating/custom heart commands. A source-built, short-lived shell helper creates its own destroy-on-removal virtual display and runs the real signed-in Deezer player there. It invokes actual accessibility ACTION_CLICK on verified offscreen nodes, with no synthetic touch/key input and no visible fallback. Version, capability, track title/artist/album/duration/mediaID/session and native control geometry are checked. A small in-memory glyph crop provides the native fill receipt; it is not a recording and writes no screenshot files. The user's normal accessibility services remain enabled via FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES.

The existing authenticated HA ADB route is reused. An own-app private nonce marker proves the target hardware and supports cancellation; credentials from Deezer are never read. No new login, permissions, permanent service or laptop dependency. State discovery runs once per track/screen entry, not every progress callback. Single-flight actions; helper12s/app30s deadlines; stale/ambiguous/unconfirmed state fails closed without optimistic fill.

## Verification and limits

Six focused tests pass, including 70 retained favourite assertions, 27 native-rule assertions, runtime-accent/UI contracts and actual-controller async boundary tests for shared results, query coalescing, toggle/dislike separation and cancellation. Source helper compilation/dex passed. Full inherited v206, split integration, HA unit-test, both APK builds and package/signature/native/art CI stages passed. Review was self-review, not an independent reviewer.

The existing TV API36 emulator ran in a read-only data overlay with snapshot saving disabled, 2 cores and 2GB RAM. It lacks secondary-display activity support; the physical SDK30 Shield supports it. A separate emulator-only synthetic playback fixture was built, but its installation did not complete before the unresponsive lab instance was stopped. No emulator visual/UI pass is claimed. The task-owned emulator was closed; laptop had about20GB RAM free afterward. Do not treat an emulator capability failure as proof the physical Shield lacks the feature.

No live recording or repeated UI polling on the real Shield: Ryan stopped the earlier capture because it made navigation unusable. Keep diagnostics bounded and preferably post-action. Raw dumps, provider APK/reconstruction, images, lab signing material and local authoring scripts remain private. One standalone manual production-helper diagnostic was safety-blocked and was not retried; normal authorized signed-APK installation and the installed app's own route supplied later evidence.

Details: docs/handoffs/2026-09-20-invisible-deezer-hearts-v235.md.
Historical v234 failure, static investigation and v233 rollback remain in preceding dated handoffs. v233 rollback source9e319d7336e7b52d54c080ed8d3bd596c805ae3d. Preserve accepted voice/audio/HA/artist browsing and all frozen art. The obsolete v234 remove-left/add-right design must not be restored.