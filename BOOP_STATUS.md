# Current voice repair acceptance — 14 September 2026

v166 on `boop-voice-ack-v166` is signed, installed and command/spoken-reply USER-ACCEPTED on Shield and the explicitly substituted Pixel 10 Pro XL. Final tested source `982a2ddeed73d673be6fe0fe2251a74d290c07c5`, successful run `34861777001`, artifact `10355475599`. Preserve the first Shield NO_SPEECH_DETECTED attempt as historical failed input; the coordinated retest succeeded and the user explicitly confirmed hearing speech. Voice choices, colour, HOME/assistant and permission settings were preserved. Scope is the tested command/reply path, not long-running microphone reliability. Exact receipts: `docs/handoffs/2026-09-14-v166-voice-startup.md`.

---

## Prior branch history (historical)

# Unified v162: native lyrics user-accepted; standalone Lab removed

Updated 2026-09-13. Owner: `boop-unified-eye-sync-safe-v159`.
Package/version: `com.boop.alpha1`, `162 / 1.2.162-native-lyrics`.

**Physical acceptance:** Ryan replied "perfection" to the integrated Now Playing
-> Lyrics / Skip confirmation. The merged native feature is USER-ACCEPTED ON
SHIELD, not awaiting another confirmation. The approved footer/presentation and
all earlier accepted v161 colour/speed behavior remain protected.

**Conditional cleanup completed:** after that confirmation, only the separate
`com.boop.lyricslab` was uninstalled on Shield. ADB returned Success/exit 0 and a
fresh package query verified absence. Unified v162's version and installed APK
hash were identical before/after. No phone/emulator operation, playback input,
app launch, new install, manual permission/settings change or app-code edit.
The standalone source branch and historical test/build evidence are retained.

**Exact installed APK:** SHA256
`cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171`.
Built source `1e0136ffa9732353035f88ca7a7cb131f7481557`, signed run `34773509395`,
artifact `BOOP-Unified` / `10322553107`. Permanent signer unchanged. PR #10 merge
`0908d6955c90978e97dcbae9031f3f1de638bd9d`. Earlier source/functional/package checks
passed; they were not rerun for this acceptance record. User acceptance is distinct
from the previous CI evidence and today's package-only cleanup verification.

**Next:** keep the accepted native feature inside current Unified. Do not reinstall
the retired Lab or reopen the confirmed integration gate. Unenumerated catalogue/
offline cases remain unclaimed coverage, not a new automatic testing backlog.
See `SESSION_HANDOFF.md`, `BOOP_UNIFIED_MEMORY.md`, and
`docs/handoffs/2026-09-13-unified-v162-accepted-lab-retired.md`.
