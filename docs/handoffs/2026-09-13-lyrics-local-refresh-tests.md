# Lyrics refresh regression: local-only continuation

Owner: `boop-lyrics-lab-side-by-side-v157`. Latest user report: all player controls
work, but lyrics do not reload until exiting and refreshing. Initial lyrics and
the approved design remain accepted; automatic refresh is NOT accepted.

Latest instruction: continue using locals and stay off the busy Shield. Do not
query, install, issue transport commands to, or otherwise touch the Shield or
either phone. No Unified/main merge.

Recovered live source is `5970f59aa9173fd8171d2d370b856349d0cf4f17`, already containing
the targeted v158 session-observation fix. Do not restart or duplicate it. It keeps
the registered active Deezer token through temporary NONE/STOPPED/SKIPPING states
while still clearing ineligible presentation data. Android's active-session-list
listener is not a substitute for a per-session metadata callback.

Existing red proof: GitHub run `34766399230` on `5ad0746a` fails at the first
NONE-to-next-track transition, expected ID 456 and got null. Existing green proof:
run `34766477538` on `5970f59a` passes 73 session assertions, 19 identity assertions,
61 timed-document/gate assertions, 35 transport checks, two incomplete-timing and
seven real-entry control-flow checks, plus signed APK compilation/integrity.
These tests use Android boundary doubles. They do not independently reproduce the
physical Deezer app or prove its timing behavior.

The dedicated local TV AVD `BOOP_Lyrics_157_8186447` is reachable and has lab v157.
It has no Deezer APK. Its lab notification consent is not enabled. Use normal
Android consent on THIS emulator before the local tests; do not copy physical
permissions. The other emulator with concurrent Unified work remains untouched.

Next test is real Android Binder/session updates into the actual installed lab
activity. `tests/runtime/lyrics-session/` builds a separate emulator-guarded
synthetic player/instrumentation fixture. It uses invented cached lyric documents
without modifying production code and never needs a Deezer login/network/audio.
Run baseline v157 to attempt reproduction, then exact signed v158 for continuity,
pause/resume, seek/highlight, missing metadata, replacement session, rapid changes
and reopening. GitHub only compiles/signs the fixture; runtime execution is local.
Record baseline and fixed outcomes separately, including inability to reproduce.

Research: https://developer.android.com/reference/android/media/session/MediaController
and https://developer.android.com/reference/android/media/session/MediaSessionManager
were checked against their current documentation. No universal playback/catalogue
or future-endpoint guarantee is claimed. Keep the user-approved renderer unchanged.
