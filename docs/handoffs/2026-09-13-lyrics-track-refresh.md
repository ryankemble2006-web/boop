# Lyrics Lab track-change regression

Ryan tested the lab controls: all work, but lyrics do not refresh until leaving
and reopening. This supersedes any blanket working inference from his initial
positive-path confirmation. No merge is authorized while this regression remains.

Base: b8ee9168cdb5c330e7a072c748d47e0adb200fb4, lab source 9a52f1c6.
Target remains com.boop.lyricslab only; preserve Unified and both phones.

Fresh Shield reproduction: the native session moved from one recording to the
next after a Next command while the lab was foreground. The Android session was
still PLAYING with new metadata, but the lab showed its empty Play something in
Deezer prompt. Registered media controller count decreased from 8 to 7. An initial
read-only baseline also showed that reopening restores current timed lyrics.
A later attempted transition probe was aborted when the lab was no longer
foreground; no commands from that aborted test were sent. Do not count it as proof.

Code trace: LyricsLabSession.refresh selects only four presentation-eligible
states, then unregisters its sole per-session callback if none is selected.
A still-active session entering NONE/STOPPED/SKIPPING loses its only route to
later PLAYING/metadata updates. The active-session-list observer does not replace
per-session playback/metadata callbacks. Android documents these as distinct APIs.

Regression tests compile the real observer, recording policy and snapshot against
controlled Android service/controller boundaries. They emit temporary states then
new metadata and PLAYING for the same active token without any active-list event
or Activity restart. Fix observation lifetime, not screen reloads or polling.
Preserve presentation eligibility for stale/empty content and existing transport
capability checks. Detach on actual session removal, revoked access or lab pause.

References: https://developer.android.com/reference/android/media/session/MediaController
and https://developer.android.com/reference/android/media/session/MediaController.Callback

This note records investigation and planned regression coverage, not a fix claim.
Private device diagnostics remain off GitHub; no lyrics or account data published.
