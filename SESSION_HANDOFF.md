# Standalone Lyrics Lab retired after Unified v162 acceptance

Updated 2026-09-13. This branch, `boop-lyrics-lab-side-by-side-v157`, is retained
as source/test/recovery history. It is not the current app delivery line.

Ryan replied "perfection" to the requested confirmation of integrated Unified
Now Playing -> Lyrics and Skip. The Unified native feature is USER-ACCEPTED ON
SHIELD. His earlier instruction to bin the Lab once confirmed is now fulfilled.

## Verified cleanup

Only `com.boop.lyricslab` was uninstalled on Shield. The existing Lab was first
verified as `159 / 0.1.159-lyrics-footer`, APK SHA256
`fb373b09572379976dcf6b15dd54ce307f7153933ca61cd3d7f8a9204d716503`.
Ordinary ADB uninstall returned Success/exit0. A fresh package-list query verified
its absence. Unified v162's version and installed APK hash were checked unchanged
before and after:
`cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171`.
No other Lab, Unified, Deezer, phone or emulator was uninstalled or changed.
No launches, playback inputs, manual settings/permission changes, new installs,
app-source edits or builds accompanied this cleanup/documentation update.

Do NOT reinstall this helper or keep the old conditional retirement gate open.
Its private runtime role has been replaced by Unified's internal native screen.
Do not delete this source branch, historical APK/build receipts or test provenance.

## Current owner and accepted feature

Continue from LIVE `boop-unified-eye-sync-safe-v159`, whose branch suffix does
not determine its version. Acceptance/cleanup docs were published at
`112d09b5b446d6582954a6d89b3700fe16298ecb`; read its SESSION_HANDOFF.md and
`docs/handoffs/2026-09-13-unified-v162-accepted-lab-retired.md`.

Unified `com.boop.alpha1`, `162 / 1.2.162-native-lyrics`, was built from
`1e0136ffa9732353035f88ca7a7cb131f7481557` by signed run `34773509395`, artifact
`10322553107`. PR #10 merged at `0908d6955c90978e97dcbae9031f3f1de638bd9d`.
Its Lyrics button opens its own private native Activity, not this Lab or a
Deezer menu macro. Shared renderer/parser/loader and half-size bottom-right
licence credit were copied exactly. Main lyric size remains unchanged.
Unified retains its own session manager and accepted v161 colour/speed/artwork.

## History and boundaries

The previous pending-retirement docs remain at `bcd9ed52dcda071fcf71e7e8ab751e52f19c7e3d`.
Complete standalone UI/build/acceptance history remains at
`5ce581be6f1da10eb47640c4c11636a4bfa9e330`, the dated docs/handoffs receipts, and
`docs/history/lyrics-lab-pre-user-confirmation/`.
Ryan's original Lab Skip/control/design acceptance and its genuine v157 refresh
failure/fix remain historical evidence. His new Unified acceptance is separate.
Neither means all catalogue/offline cases were individually tested. Do not make
those unreported cases a reason to repeat the confirmed integration gate.
GitHub development and joint device tests remain current; no local builds or
automatic emulator/visual gates. Further work belongs on the live Unified owner.
