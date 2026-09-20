# BOOP current handoff

## In progress: v239 Lyrics Queue and music-reply correction

Ryan accepted v238 Queue as perfect. The requested Lyrics Queue now reuses the same panel/controller, appears below Play/Pause, and remains hidden on Flow. The whole left music column moves up38 design pixels together; old internal gaps and right-hand lyrics stay unchanged. A deterministic native music test reproduced playback being sent once but reported Failed when a concurrent heart lookup overwrites HA's latest ADB response. The code now reads the exact service-call receipt first and never replays an uncertain command. This is a reproduced music-path defect; the precise phone utterance/version and a physical phone re-test are still needed.

Source candidate: Shield239 and companion Wall208 for the sending-phone client. No phone installation is being performed.26 focused tests plus47 Lyrics Activity boundary assertions passed; signed CI and physical final Lyrics layout remain pending. Current details: docs/handoffs/2026-09-20-lyrics-queue-and-voice-receipt-v239.md. Preserve accepted v238/v236/v235 results below.

## Current: Shield v238 Queue installed and physically verified

The approved Queue button appears between Lyrics and Flow for recognised native album/playlist playback only. It opens a charcoal remote-friendly list of the tracks Deezer actually publishes, marks the current track with the chosen accent, and selects within the existing queue. Flow, radio, unknown context and Cast do not show Queue. Back returns to BOOP. No automatic opening, queue editing or claim that long playlists are exposed completely.

Source/build a42b6bbf00f920a3cc0867f1f4115541fa9090d8. Successful signed run35535859962/job106144644088 and Deezer checks35535859984/job106144643967. Artifact10612647184, BOOP-Shield-v238-Wall-v207-Signed. ZIP SHA256171617fe56520a2793ea29881e7c24a10617c3bbde9bcce759757a88faf6270a. Shield APK160567661 bytes, SHA256510e558c48ac30b07097c5798ac883fa517956718a4d243693add7c6d7cb0ade. Permanent signer f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde unchanged. Downloaded source/CRC/hash/size and cryptographic signature were independently verified, with all16 native libraries and18 assets byte-identical to accepted v236.

The exact APK was copied to Desktop/APKBOOP and installed over237 preserving data; installed238/1.2.238-shield read back. Accepted v236 rollback retained. On the actual v238 app, its real Flow button confirmed native Flow and Queue absent. Its real Queue button then opened a31-track native album, and clicking an upcoming row confirmed the chosen track while preserving native session, album context and queue order. A final still screenshot verifies the v237 garbled separator and clipped artist-line issues are fixed. Back returned to the BOOP HOME activity, with primary display0 focus. The helpers disconnected and their temporary on-device JARs were removed. No live recording or repeated UI polling; no extra attention skip, new grants, provider changes or Wall install. No emulator was started.

Thirteen focused tests pass, including34 native queue assertions plus regression checks for the accepted hearts, Flow and alignment; full signed CI passed. Review was self-review, not an independent reviewer. Album jump, final panel and Flow hiding have physical evidence; playlist-mode logic has automated coverage but no separate physical long-playlist acceptance is claimed. Ryan subsequently accepted v238 Queue as working perfectly, including the corrected clipping. Exact implementation/verification chronology: docs/handoffs/2026-09-20-shield-queue-v237.md. Historical v237 issues and older v236/v235 accepted checkpoints remain below; current state is this v238 section.

## Historical Queue investigation before implementation: 2026-09-20

Ryan is interested in a normal album/playlist Queue, explicitly excluding Flow. Live read-only inspection found an actual published Flow marker (`com.deezer.METADATA_KEY_STREAM_CONTEXT_TYPE=flow_partner`, listen type SMART_RADIO), so hiding it need not rely on queue length. Matched-provider mapping also defines album_partner and playlist_partner; those modes have not yet been captured live in this task. The current Flow list is listening history through the current item, not upcoming suggestions. Queue titles/artists and unique numeric queue IDs are available; standard item media IDs are absent. Long playlists use a limited published window.

The provider implements a queue-jump handler but does not advertise SKIP_TO_QUEUE_ITEM in the inspected live state. A direct normal-album row selection still needs a physical test before shipment. Proposed Queue panel/entry is not yet implemented or layout-approved. Hide during Flow/radio/unknown context, use event-driven native metadata/queue callbacks, and preserve v236 Flow/hearts/Lyrics. No playback, app source, APK, permissions, recording or emulator changes were made. Details: docs/handoffs/2026-09-20-deezer-queue-flow-exclusion-investigation.md.

## User-accepted v236 Flow and album-to-Flow use case: 2026-09-20

Ryan reports that the installed Flow button works perfectly. His stated use case is to browse to an album by selecting the Now Playing artwork, listen to the album, then press Flow when it finishes without digging through Deezer's menus. Preserve the direct Flow shortcut alongside album-art browsing and the accepted favourite controls; this is the reason Flow replaced Close player.

This records user acceptance of the installed Flow feature and the described listening workflow. It does not request automatic Flow at album completion, establish cold-start/Cast support, or add a separate measured end-of-queue test. Source5bfb8d91f3351cbe1c089fb5e0a2566334db2553 and the verified v236 APK are unchanged. This feedback update is documentation-only: no build, install, permissions, recording, device input or playback change.

## Current: v236 Flow physically tested and user-accepted

The Now Playing Close player slot is now Flow, with the same130x44dp geometry, margins, focus and current accent. It starts the existing native Deezer Flow URI directly through the selected player's advertised playFromUri command, without leaving BOOP. Separate Close media remains. Accepted v235 hearts, dislike and Lyrics implementation are untouched. Native Deezer playback must already be active; no Cast or visible cold-launch fallback is claimed.

Source5bfb8d91f3351cbe1c089fb5e0a2566334db2553; signed run35533035925/job106137012994 SUCCESS; Deezer checks35533036024 also SUCCESS. Artifact10612281598. Installed236/1.2.236-shield after full hash/signature verification; copy in Desktop/APKBOOP; v235 rollback retained. APK SHA25693608fae1f3566943fe11e37147a37b5d0bb48bf6938ff711c5ecd054bee06ec,160534893 bytes. Permanent signer unchanged;16 native libraries and18 assets byte-identical to v235. Wall not installed.

One exact-labelled Flow-button click on the physical installed app started a new playing native Deezer queue with active item0. The same BOOP HOME activity stayed onscreen and remote focus stayed on display0. No separate attention skip, recording or repeated UI polling. Ten focused tests passed locally, with18 native Flow assertions, and the full signed pipeline passed. Ryan subsequently confirmed that Flow works perfectly and supplied the album-to-Flow use case recorded above; the earlier instrumented runtime test remains separate evidence. Details: docs/handoffs/2026-09-20-shield-flow-button-v236.md.
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
