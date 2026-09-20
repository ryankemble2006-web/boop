# Shield v237: native album and playlist Queue

2026-09-20. Ryan approved the proposed Queue button beside Lyrics and a remote-friendly charcoal panel. Flow must not expose or browse a queue. Base live branch34d5970703cdc0578dab0d4d728c281b5b748537, with accepted v236 Flow and v235 favourites retained. Task worktree remains .worktrees/boop-deezer-invisible-v235; primary checkout and concurrent work are preserved.

## Physical feasibility before implementation

The normal installed Deezer301000101 album context was observed as album_partner. A direct numeric skipToQueueItem changed the active album track and retained the same session, album context and 31-item queue order. This confirms the previously located handler despite its missing advertised ACTION_SKIP_TO_QUEUE_ITEM bit. A first probe timed out because its expected title came from an old Flow list while the new album metadata had already arrived. A follow-up read established the jump happened, and the corrected coherent-queue probe then passed. This metadata/queue race is now a production guard, not ignored evidence.

These probes used the existing native media connection, with a temporary private shell helper removed afterward. A public catalogue lookup selected the currently playing song's album for the test. No new login, account/token extraction, recording, screenshots, permissions or emulator runs. The current album was left playing for the new Queue test. Raw listening metadata stays private.

## Implementation

A Queue button sits between Lyrics and Flow, visible only for a recognised native album_partner or playlist_partner context with nonempty context ID and a coherent published queue/current track. Flow/radio/unknown/Cast do not qualify. While hidden, the accepted v236 layout is exactly restored; when present, the extra78dp button and8dp gap use title-row space, without moving the artwork, progress or transport row or the right-edge Flow button. Lyrics moves left only to make room for the approved new adjacent control.

The charcoal Dialog lists the actual supplied native tracks, marks the current track with the selected accent, allows remote selection and closes with Back. It never claims the published window is the entire long playlist. No editing, reordering, full library browser or automatic opening. Every clickable row carries the immutable state/row it was rendered from; a recycled or queued input cannot silently act on newer row data.

The existing manager feeds queue/metadata/playback events to a scoped controller. It reads a full queue at session binding, on queue callbacks, and again immediately before selection, not on progress updates. Flow dispatch hides Queue immediately; provider context handles Flow started outside BOOP. Duplicate IDs, missing titles, incoherent metadata, revoked access and wrong sessions fail closed. Queue generation includes context and published row content. A changed album/order rejects old row clicks. Selection uses native numeric IDs, not a track URI that would replace album order. The unadvertised handler fallback is pinned to physically verified provider301000101; other versions require the advertised queue-skip capability, checked again at action time. Four-second pending confirmation requires the native active ID and matching track metadata. Timeouts/exceptions never fabricate a current-track indicator.

Dialog subscription is lifecycle-bound. Native playback and BOOP's existing notification grant provide the data; no new HA/ADB/offscreen route is required. Working favourite/dislike helper, Flow dispatch itself, Lyrics source, audio/voice/HA, signer and frozen art are unchanged.

## Verification before source publication

Both queue tests were first observed failing because the controller and entry did not exist. The production controller now passes34 assertions for albums/playlists, Flow/radio/unknown/Cast exclusion, no queue polling, duplicate IDs, provider version/capability changes, late metadata, stale rows/context, selection confirmation, timeout, exception and observer cleanup. UI/source contracts check placement, runtime accent, row-bound identities, callbacks and teardown. Twelve focused queue/Flow/favourite/alignment tests passed locally. Diff/whitespace review passed. Self-review only; no independent reviewer is claimed.

Full signed CI and actual installed v237 panel/action/focus checks are pending this source publication. Successful shell feasibility is not a final APK UI test. Shield version237 /1.2.237-shield; Wall remains207 and is not requested for installation. Retain v236 rollback. Automatic verified Shield installation remains authorized in this conversation; no v237 installation or extra ready signal has happened yet.
