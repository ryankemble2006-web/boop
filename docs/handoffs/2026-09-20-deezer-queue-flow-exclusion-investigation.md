# Deezer Queue feasibility: keep Flow out

2026-09-20. Read-only feasibility investigation against accepted Shield v236. Owning branch and isolated worktree were live-verified at a70798fda8f29b91285fb47089ff8dd23b30964b before recording these results. No production code or APK change.

## User intent

Ryan is interested in Queue / Up Next for deliberate album listening and says not to bother with it during Flow: let Flow choose what it serves. Proposed initial scope is a remote-friendly charcoal list for recognised albums/playlists, with current-track indication, upcoming tracks, selected accent and a straightforward Back return. Exact entry-point/layout and implementation still need agreement; this investigation does not claim a finished queue feature. No automatic opening, autoplay-at-album-end, queue editing or library browser requested.

## New live evidence

A private one-shot read-only helper used the public Android MediaController fields on the existing native Deezer session. It never called a transport method, opened an Activity, used accessibility, recorded or captured a screen. Its temporary on-device JAR was removed afterward. Two bounded reads were used, the second to inspect only the newly discovered context fields. Raw output remains private.

The live provider publishes `com.deezer.METADATA_KEY_STREAM_CONTEXT_TYPE=flow_partner` and `com.deezer.METADATA_KEY_LISTEN_TYPE=SMART_RADIO`. This is an actual mode marker, not a guess from queue length. Do not publish the context ID value; it is not needed for the public record.

The inspected Flow queue had 11 then 13 entries, with the active item last and zero entries after it. All entries had title and subtitle; numeric queue IDs were unique. The inspected active description also had description/artwork URI. Queue title was null, standard description media IDs were absent, and session/description extras offered no additional mode values. A null-equals-null comparison is not evidence of identity: the final read correctly reports no usable standard media-ID match.

## Matched-provider code evidence

The already-inspected installed-version-matched Deezer 301000101 metadata publisher deliberately exposes stream context and a custom current playable identifier. Its context mapper distinguishes `flow_partner`, `album_partner`, and `playlist_partner` (plus other types). Only the Flow value was physically read in this investigation; album/playlist values are from targeted source inspection, not live album tests.

The queue publisher distinguishes InfiniteQueueList from BasicQueueList. Infinite queues export up to 100 earlier entries plus the current one, with zero future entries. Basic queues use a bounded window of 10 earlier and 90 later entries, adjusting near the ends. Therefore the earlier 101-item observation is NOT evidence of 101 upcoming Flow tracks. Long playlists are not guaranteed to be fully exposed in one response. Show the actual supplied window, not fabricated completeness.

The provider's skip-to-queue handler exists and maps the supplied numeric queue ID to its internal queue index. However the live session does NOT advertise ACTION_SKIP_TO_QUEUE_ITEM, and the inspected capability builder does not establish that advertised support. Selecting a real album row still needs a controlled physical test before shipping; no queue jump was sent here. Do not replace an in-queue jump with a track URI that could discard album order. Check current session token, context/generation, published numeric queue ID and row identity immediately before dispatch; reject stale rows and duplicate IDs.

## Recommended first implementation boundary

Use native queue/metadata callbacks in BOOP's existing media connection, not the offscreen-heart helper or a polling loop. Whitelist recognised album/playlist context; hide Queue for Flow/radio and unknown or missing context. Missing mode metadata must not be treated as an album. A Flow request must invalidate old visible queue state immediately, with metadata callbacks handling Flow started outside BOOP. Keep accepted album-art browsing, Flow, hearts, Lyrics, spacing, audio, voice and HA unchanged.

Next: agree the small Queue entry and panel, then validate a direct album queue jump before building a candidate. No new build/install, device permission changes, playback commands, track skips, live recording, repeated UI polling or emulator run occurred in this investigation. Accepted v236 source remains 5bfb8d91f3351cbe1c089fb5e0a2566334db2553 and its APK remains unchanged. Read-only probe code is throwaway/private, not a shipped component.
