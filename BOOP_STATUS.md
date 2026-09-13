# BOOP Lyrics Lab v158 status

Updated 2026-09-13. Branch: `boop-lyrics-lab-side-by-side-v157`.
Package: `com.boop.lyricslab`; `158 / 0.1.158-lyrics-session-sync`.

**Current user boundary:** LOCAL ONLY. The Shield is busy and must not be queried,
controlled or installed on. Neither physical Pixel is a target. No Unified/main merge.

**User acceptance:** initial real lyrics and lab controls work; emulator design
approved. User reported automatic lyrics refresh failing until exit/reopen.
That failure supersedes any blanket working inference from the first report.

**Fix:** retain the active session's metadata/playback subscription through
transient states, instead of treating display eligibility as observer lifetime.
App source `5970f59aa9173fd8171d2d370b856349d0cf4f17`; renderer/artwork untouched.

**Local proof:** the signed old v157 reproduces loss of lyrics after Previous in
a real Android session test. Exact signed v158 passes all 38 runtime assertions
twice on the dedicated TV emulator, including no-reopen track changes, seeking,
pause/resume, session/metadata recovery, rapid changes and reopening.
Synthetic provider/words, actual installed production app and Android callbacks.

**GitHub:** signed run `34766477538` SUCCESS; artifact `BOOP-Lyrics-Lab` /
`10320107611`. APK SHA256:
`c95bb9cc343ec9752fb2be610d0c61c0e4d01eb4faa3c62b8c6b28713db00c09`.
Signer/package/version/source checked; local installed base APK matches exactly.
73 session, 19 identity, 61 timed/gate, 35 transport, two incomplete-timing and seven
entry assertions passed. Test fixture build `34767793652` also passed compilation/
signing only on GitHub. Runtime testing remained exclusively local.

**Still pending:** post-fix real Deezer/Shield refresh acceptance, real-device
missing-lyrics/offline and longer-session cases. No claim of universal catalogue
availability. No post-fix Shield deployment has occurred in this continuation.
The temporary test fixture was removed only from the dedicated emulator; lab
v158 remains installed. See SESSION_HANDOFF.md and the detailed local-test receipt.
