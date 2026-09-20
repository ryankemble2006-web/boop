# BOOP status

## User-accepted v235 favourites: 2026-09-20

Ryan physically tested the installed v235 and reports that the favourites button works exactly as desired, including Android added/removed confirmation messages. He favourited a track in native Deezer, returned to BOOP HOME and saw the heart fill; he then unfavourited it on BOOP's Lyrics screen and verified that the removal was reflected in native Deezer. This is acceptance of real cross-app favourite-state propagation and add/remove operation on the installed build, not merely CI success or the earlier standalone probe. Preserve this working checkpoint and its invisible operation.

Scope of acceptance: favourites add/remove, confirmation messages, Deezer-to-HOME filled state, and Lyrics-to-Deezer removal. The separate dislike-and-skip button and a fresh change of the accent slider were not explicitly tested in this report; do not infer those results or an unrestricted every-app/window guarantee. The selected-accent requirement remains unchanged. The earlier hardware-binding failure remains historical evidence; this report does not diagnose it. No new app code, build, install, permission change, recording, input or ready-track skip was performed to record this feedback. Existing source d6a7957d57a94fb7fc2a25266478e7c4d376f220 and the verified v235 APK are unchanged.

2026-09-20. Shield v235 is signed, downloaded, hash/signature verified and automatically installed on the intended physical Shield. Owning branch: boop-shield-weather-focus-v221. No Wall update installed.

Source d6a7957d57a94fb7fc2a25266478e7c4d376f220; successful build35531643785/job106133185445; artifact10611029896. Favourite check35531643786 also passed. APK160534893 bytes, SHA25631d0161a2b3a534c0858ccde3b2aeb0f67474379352b366dc21a8f3cd9119d43. Permanent signer unchanged; 16 native libraries and frozen art preserved.

Lyrics left is outlined dislike+skip; Lyrics right and HOME are confirmed favourites toggles filled using the current slider accent. Invisible source-built native offscreen bridge uses the existing authenticated local HA/ADB route; no new login/permission or laptop runtime dependency.

Installed app logged read OK saved0 and toggle OK saved1, plus further successful reads. An initial hardware-binding IOException occurred before those successes; exact cause unknown. A separate native probe confirmed a reversible saved/unsaved/saved round trip. Ryan subsequently accepted favourite add/remove and cross-app state propagation; dislike+skip and a fresh accent-slider change were not explicitly reported. One normal media-next ready signal was sent after install and live receipts.

Six focused tests and the full signed CI pipeline passed. Emulator-only UI fixture installation did not complete, so no emulator visual pass is claimed; the bounded read-only lab emulator was stopped. No live recording or repeated UI polling on the real Shield. Raw evidence stays private. Full current details: SESSION_HANDOFF.md and docs/handoffs/2026-09-20-invisible-deezer-hearts-v235.md.