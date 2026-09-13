# BOOP Lyrics Lab v158 status

Updated 2026-09-13. Branch: `boop-lyrics-lab-side-by-side-v157`.
Package: `com.boop.lyricslab`; `158 / 0.1.158-lyrics-session-sync`.

**Physical Skip refresh: USER-CONFIRMED PASS.** Ryan clarified that ADB is fine
and lyrics reload when Skip is pressed inside the lab. The reported manual-Skip
regression is confirmed fixed on his Shield without leaving/reopening the lab.
Do not request the same confirmation again or require reconnection/reinstallation.

**Evidence boundary:** this is Ryan's real-device result, not a newly observed
automated assertion. The earlier capture/Next command timed out with unknown
execution outcome; that history remains true. No new ping or device checks were
performed in this documentation update. Natural end-of-track advance, both skip
directions, offline/no-lyrics and longer sessions were not separately confirmed.

**Current boundary:** separate lab only. Neither phone nor Unified is an install/
edit target. No merge authorized by this result. Approved design stays unchanged.

**Shield deployment: previously VERIFIED.** Exact tested v158 installed with
`adb install -r`; version/base APK hash matched the signed candidate. Existing
notification access retained. Launch succeeded and logged a real 50-line document.

**Fix:** retain active-session metadata/playback observation through transient
states, separately from presentation eligibility. App source
`5970f59aa9173fd8171d2d370b856349d0cf4f17`; renderer/artwork untouched.
**Local proof:** signed v157 reproduced the fault; signed v158 passed 38 actual
Android-session/production-presentation assertions twice. Synthetic provider/words;
keep these checks distinct from the later real-Deezer user test. CI checks passed.

**Signed candidate:** run `34766477538`, artifact `BOOP-Lyrics-Lab` / `10320107611`.
APK SHA256: `c95bb9cc343ec9752fb2be610d0c61c0e4d01eb4faa3c62b8c6b28713db00c09`.
Signer: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Physical and emulator installed hashes were verified in their own earlier stages.

**Preserved:** approved design and controls, separate Unified (v159 at deployment),
native Deezer and both phones. No new code, build, permissions, signing, install or
merge performed while recording Ryan's clarification. The old pending/reconnect
instructions in `docs/handoffs/2026-09-13-lyrics-shield-v158-install.md` are historical
and superseded by this user result. See SESSION_HANDOFF.md for the current handoff
and `docs/handoffs/2026-09-13-lyrics-local-refresh-tests.md` for local evidence.
