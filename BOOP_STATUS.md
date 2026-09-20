# BOOP status

## In progress: v237 approved Queue panel

Ryan approved Queue beside Lyrics, hidden during Flow. The physical native album queue jump is now confirmed, preserving album order. The v237 source candidate adds the charcoal remote list, real current-track indication, same-queue selection and Flow exclusion with stale-row/late-metadata guards. Twelve focused tests pass, including34 native queue assertions; full CI and the new installed panel remain pending. No working heart/Lyrics/audio code was altered. Details: docs/handoffs/2026-09-20-shield-queue-v237.md. The earlier investigation below is historical and its layout/jump questions are superseded by this approval and probe.

## Queue investigation, not implemented: 2026-09-20

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

2026-09-20. Shield v235 is signed, downloaded, hash/signature verified and automatically installed on the intended physical Shield. Owning branch: boop-shield-weather-focus-v221. No Wall update installed.

Source d6a7957d57a94fb7fc2a25266478e7c4d376f220; successful build35531643785/job106133185445; artifact10611029896. Favourite check35531643786 also passed. APK160534893 bytes, SHA25631d0161a2b3a534c0858ccde3b2aeb0f67474379352b366dc21a8f3cd9119d43. Permanent signer unchanged; 16 native libraries and frozen art preserved.

Lyrics left is outlined dislike+skip; Lyrics right and HOME are confirmed favourites toggles filled using the current slider accent. Invisible source-built native offscreen bridge uses the existing authenticated local HA/ADB route; no new login/permission or laptop runtime dependency.

Installed app logged read OK saved0 and toggle OK saved1, plus further successful reads. An initial hardware-binding IOException occurred before those successes; exact cause unknown. A separate native probe confirmed a reversible saved/unsaved/saved round trip. Ryan subsequently accepted favourite add/remove and cross-app state propagation; dislike+skip and a fresh accent-slider change were not explicitly reported. One normal media-next ready signal was sent after install and live receipts.

Six focused tests and the full signed CI pipeline passed. Emulator-only UI fixture installation did not complete, so no emulator visual pass is claimed; the bounded read-only lab emulator was stopped. No live recording or repeated UI polling on the real Shield. Raw evidence stays private. Full current details: SESSION_HANDOFF.md and docs/handoffs/2026-09-20-invisible-deezer-hearts-v235.md.
