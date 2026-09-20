# BOOP current handoff

## Current: v236 Flow installed and physically checked

The Now Playing Close player slot is now Flow, with the same130x44dp geometry, margins, focus and current accent. It starts the existing native Deezer Flow URI directly through the selected player's advertised playFromUri command, without leaving BOOP. Separate Close media remains. Accepted v235 hearts, dislike and Lyrics implementation are untouched. Native Deezer playback must already be active; no Cast or visible cold-launch fallback is claimed.

Source5bfb8d91f3351cbe1c089fb5e0a2566334db2553; signed run35533035925/job106137012994 SUCCESS; Deezer checks35533036024 also SUCCESS. Artifact10612281598. Installed236/1.2.236-shield after full hash/signature verification; copy in Desktop/APKBOOP; v235 rollback retained. APK SHA25693608fae1f3566943fe11e37147a37b5d0bb48bf6938ff711c5ecd054bee06ec,160534893 bytes. Permanent signer unchanged;16 native libraries and18 assets byte-identical to v235. Wall not installed.

One exact-labelled Flow-button click on the physical installed app started a new playing native Deezer queue with active item0. The same BOOP HOME activity stayed onscreen and remote focus stayed on display0. No separate attention skip, recording or repeated UI polling. Ten focused tests passed locally, with18 native Flow assertions, and the full signed pipeline passed. User preference acceptance remains for Ryan; the runtime test was performed by this session. Details: docs/handoffs/2026-09-20-shield-flow-button-v236.md.
## User-accepted v235 favourites: 2026-09-20

Ryan physically tested the installed v235 and reports that the favourites button works exactly as desired, including Android added/removed confirmation messages. He favourited a track in native Deezer, returned to BOOP HOME and saw the heart fill; he then unfavourited it on BOOP's Lyrics screen and verified that the removal was reflected in native Deezer. This is acceptance of real cross-app favourite-state propagation and add/remove operation on the installed build, not merely CI success or the earlier standalone probe. Preserve this working checkpoint and its invisible operation.

Scope of acceptance: favourites add/remove, confirmation messages, Deezer-to-HOME filled state, and Lyrics-to-Deezer removal. The separate dislike-and-skip button and a fresh change of the accent slider were not explicitly tested in this report; do not infer those results or an unrestricted every-app/window guarantee. The selected-accent requirement remains unchanged. The earlier hardware-binding failure remains historical evidence; this report does not diagnose it. No new app code, build, install, permission change, recording, input or ready-track skip was performed to record this feedback. Existing source d6a7957d57a94fb7fc2a25266478e7c4d376f220 and the verified v235 APK are unchanged.

Updated 2026-09-20. Owning Shield branch: boop-shield-weather-focus-v221 (long-lived cache-hot branch, not app version). Source worktree for this task is .worktrees/boop-deezer-invisible-v235, isolated from the old primary checkout. Wall remains v207 and was not installed.

## Retained checkpoint: v235 favourites physically accepted

Source/build commit: d6a7957d57a94fb7fc2a25266478e7c4d376f220.
Signed workflow35531643785, job106133185445: SUCCESS. Favourite checks35531643786, job106133185094: SUCCESS.
Artifact10611029896: BOOP-Shield-v235-Wall-v207-Signed.
APK BOOP-Shield-v235.apk, 160534893 bytes, SHA256 31d0161a2b3a534c0858ccde3b2aeb0f67474379352b366dc21a8f3cd9119d43.
ZIP SHA256 37d99acc99912d5f306b1f2aa58367583e2581a0d19f2e4cc22e98023c600ba4.
Permanent signer SHA256 f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde.

The exact APK was downloaded and independently hash/size/CRC checked. All 16 native libraries and 8 frozen PNG/shader assets match v234 byte-for-byte, plus CI's accepted baseline checks. Cryptographic apksigner verification passed both in CI and on the laptop with the permanent certificate. The APK was copied to Desktop/APKBOOP and installed by adb install -r on the verified physical Shield. Installed version235 / 1.2.235-shield was read back. Existing app data retained; no permission grants or provider modifications. No Wall install.

BOOP Shield HOME was brought foreground. Installed-app logs subsequently show native read OK saved0 and toggle OK saved1 at stage confirmation, followed by further successful reads. This verifies a real installed BOOP -> existing Home Assistant -> offscreen native Deezer -> matching result path, not only a fake boundary test. One earlier read failed at hardware-binding with IOException; its root cause was not established. Do not claim a completely error-free first connection.

An earlier reversible native probe separately verified saved -> unsaved -> saved on the same track and restored its initial favourite. Do not present that as an observed v235 UI unfavourite test. Ryan subsequently accepted favourite add/remove and cross-app state propagation; separate dislike+skip and a fresh accent-slider change remain unreported, as recorded above. A normal KEYCODE_MEDIA_NEXT was sent once as his requested ready signal, after installation and successful native receipts. Never use dislike for an attention signal.

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
