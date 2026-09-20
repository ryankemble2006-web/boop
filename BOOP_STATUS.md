# BOOP status

## In progress: v235 invisible-heart candidate

The latest user instruction prefers invisible operation and authorizes using the existing emulator/laptop, automatic installation of the verified Shield APK, then a normal next-track ready signal. A bounded native offscreen-display feasibility test now confirmed favourite saved -> unsaved -> saved without retaining a display or changing the final TV remote focus. Original favourite restored. No live recording or repeated UI polling. Source-owned v235 implementation and six focused tests are prepared; signed CI and final app route/install are not yet verified. Correct roles: Lyrics left outlined dislike+skip, Lyrics right and HOME toggles filled only when saved, using runtime accent-slider colour. See `docs/handoffs/2026-09-20-invisible-deezer-hearts-v235.md` for exact evidence and limits. This supersedes the earlier visible-handoff proposal below.

Updated 2026-09-20. Owner lineage: `boop-wall-shield-split-v207`. Live Shield branch: `boop-shield-weather-focus-v221`.

v234 remains signed but its native Deezer favourite route FAILED physical use. No replacement route is yet implemented, built, installed or signalled ready.

Corrected approved target: Lyrics LEFT dislike-and-skip, Lyrics RIGHT favourites toggle; HOME one favourites toggle. ONLY the two favourites toggles fill with the selected launcher accent when saved. Dislike remains outlined with ordinary focus highlighting. No hardcoded orange. These are new requirements, not a description of the unchanged v234 APK.

RDC/ADB recovered after the reported outage. Installed and cached provider APK hashes match. Offline inspection with existing tools located the normal heart's track-specific add/remove network/data path and state feedback; no callable external favourite/dislike entry was established. Partial decompilation had 585 errors, so no exhaustive absence claim is made.

Next proposal needs Ryan's UX agreement: a bounded on-demand native Deezer UI handoff, then return to BOOP, instead of an invisible background command. No live recording or repeated UI polling. No new account authorization, provider changes, permissions or app installs were performed.

Ryan authorizes automatic installation once a verified Shield candidate exists, followed by a normal next-track attention signal. Do not use dislike as the signal. Not performed yet.

Unchanged app source `ccbd42cf3c4dc77614425f675e80fa8a3f146d20`.
Historical signed run `35525392054`, artifact `10609691434`.
Shield APK `BOOP-Shield-v234.apk`, 160518509 bytes, SHA-256 `58d151cd8a91142a3372e7efacdf59edc771dad8a7eddd69aa892b7d4f3876b9`.
Permanent signer unchanged; Wall remains version207. No new application tests/build or Windows BOOP source synchronization claimed.

Current decisions and inspection evidence: `docs/handoffs/2026-09-20-deezer-heart-static-and-ui-scope.md`.
Prior live evidence: `docs/handoffs/2026-09-20-deezer-heart-live-diagnosis.md`.
Existing artifact/test receipt: `docs/handoffs/2026-09-20-shield-favourite-hearts-v234.md`.
