# BOOP Lyrics Lab v158 status

Updated 2026-09-13. Branch: `boop-lyrics-lab-side-by-side-v157`.
Package: `com.boop.lyricslab`; `158 / 0.1.158-lyrics-session-sync`.

**Current boundary:** Ryan's "do it" released the busy-Shield hold for this specific
separate-lab test. Neither phone or Unified is an install/edit target. No merge.

**Shield deployment: VERIFIED.** Exact tested v158 installed with `adb install -r`;
physical version and installed base APK hash match the signed candidate below.
Already-enabled notification access retained without any new grant. Lab launch
returned Status ok and logged a real timed-document load with 50 lines.

**Physical refresh check: INTERRUPTED, NOT PASSED.** Desktop Commander timed out
as initial/Next captures were requested; no process ID or output was returned.
That command may or may not have sent Next. Session-list/ping calls also timed out.
Read owned sessions/private captures after reconnection before repeating input.
Do not reinstall just to resume; v158 is already present. Registry "online" did
not establish live responsiveness. No app fault or permission change was proved.

**Fix:** retain the active session's metadata/playback subscription through
transient states, instead of treating display eligibility as observer lifetime.
App source `5970f59aa9173fd8171d2d370b856349d0cf4f17`; renderer/artwork untouched.
**Local proof:** signed v157 reproduced the fault; signed v158 passed 38 actual
Android-session/production-presentation assertions twice. Synthetic provider/words,
not a post-fix real Deezer pass. GitHub functional and packaging checks passed.

**Signed candidate:** run `34766477538`, artifact `BOOP-Lyrics-Lab` / `10320107611`.
APK SHA256: `c95bb9cc343ec9752fb2be610d0c61c0e4d01eb4faa3c62b8c6b28713db00c09`.
Signer: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Physical and emulator installed hashes were each verified in their own stage.

**Preserved:** user-approved design, original positive lyrics/controls acceptance,
separate Unified installation (v159 baseline), native Deezer and both phones.
No new app code/build, permission/signing change or merge in this install stage.
Detailed current receipt: `docs/handoffs/2026-09-13-lyrics-shield-v158-install.md`.
Earlier local receipt: `docs/handoffs/2026-09-13-lyrics-local-refresh-tests.md`.
