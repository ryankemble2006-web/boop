# Unified v162 status

Updated 2026-09-13. Owner `boop-unified-eye-sync-safe-v159`; PR #10 merged the
native lyrics integration at `0908d6955c90978e97dcbae9031f3f1de638bd9d`.

**Delivered on Shield:** `com.boop.alpha1`, `162 / 1.2.162-native-lyrics`.
The existing Now Playing Lyrics button opens BOOP's private native screen, not
a Deezer menu macro or the separate Lab. Includes the half-size bottom-right
licence/copyright footer and no Back to Now Playing footer action.

**Verified:** full signed run `34773509395` SUCCESS, app source
`1e0136ffa9732353035f88ca7a7cb131f7481557`, artifact `10322553107` / BOOP-Unified.
APK SHA256 `cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171`.
Downloaded signature/package/source and installed version/hash all matched.
Shield was on the exact accepted v161 before the ordinary update. No downgrade,
permissions, data reset, playback input, app launch, phone or emulator operation.

**Preserved:** v161's accepted colour/speed source, artwork, manager, launcher and
other code; exact source-scope guards pass. New native checks and the original
235 Unified / 68 Shield tests pass. New Activity tests use controlled boundaries;
CI/install success is not new physical lyrics acceptance.

**Pending user confirmation:** open Lyrics inside Unified and confirm the merged
screen/Skip behavior. The Lab remains unchanged at v159 with its before/after APK
hash verified. Remove ONLY com.boop.lyricslab from Shield after that confirmation,
not now. Its source branch/history remains recovery provenance.

Handoff/memory and detailed receipt: SESSION_HANDOFF.md, BOOP_UNIFIED_MEMORY.md,
`docs/handoffs/2026-09-13-unified-v162-lyrics.md`.
