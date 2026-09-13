# Lyrics Lab: integrated into Unified v162; retirement awaits confirmation

Updated 2026-09-13. This branch remains `boop-lyrics-lab-side-by-side-v157`.
Ryan explicitly requested integrating the tested feature into Unified v161 as
v162, changing the existing Lyrics route, and binning the Lab once confirmed.
The former wait/no-merge instruction is superseded by that request.

## Current result and owner

PR #10 merged the native lyrics feature into the current Unified owner,
`boop-unified-eye-sync-safe-v159`, at `0908d6955c90978e97dcbae9031f3f1de638bd9d`.
Its current delivery receipt is at `978bf7df5e0759512b8a69807d9badeee7a8bb95`:
`docs/handoffs/2026-09-13-unified-v162-lyrics.md` and `SESSION_HANDOFF.md`.
Fetch the live Unified owner for subsequent integration work, not this older base.

Unified `162 / 1.2.162-native-lyrics` was installed and identity-verified on Shield.
Application/build source `1e0136ffa9732353035f88ca7a7cb131f7481557`;
signed run `34773509395`; artifact `10322553107` / BOOP-Unified;
APK SHA256 `cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171`.
The Now Playing Lyrics button now uses its private internal Activity, not this
Lab or the Deezer menu macro. Shared renderer, timing, loader and footer were
copied by exact Git blobs. Unified keeps its own unchanged session manager and
all unrelated v161 colour/speed/artwork source. No Lab observer/listener imported.

## Conditional cleanup, not yet performed

The installed Lab remains `com.boop.lyricslab`, `159 / 0.1.159-lyrics-footer`.
Its before/after APK hash during Unified installation stayed identical:
`fb373b09572379976dcf6b15dd54ce307f7153933ca61cd3d7f8a9204d716503`.
Wait for Ryan to confirm the merged Unified Now Playing -> Lyrics route and
refresh behavior. Then uninstall ONLY com.boop.lyricslab from Shield and verify
absence. Do not delete this source branch/history, Unified, Deezer, another Lab
or any phone application. Do not remove the Lab merely because CI/install passed.
Neither phone was touched in this integration delivery. No permissions, settings,
playback inputs, launches or emulator operations were performed.

## Preserve previous acceptance and history

Ryan confirmed real-Deezer Skip refresh inside Lab, the player controls, and the
emulator design. The latest footer removes Back to Now Playing, with passive
bottom-right licence/copyright text at half its previous size, not smaller lyrics.
Those source files are preserved in Unified. Lab acceptance is not automatically
physical acceptance of the new integration; do not ask to repeat the old Lab test.

Full previous Lab handoff/status/memory remain at
`5ce581be6f1da10eb47640c4c11636a4bfa9e330`. The source itself is unchanged by this
routing update. Dated footer, local refresh and installation receipts remain in
`docs/handoffs/`. Historical product memory remains under
`docs/history/lyrics-lab-pre-user-confirmation/`.

GitHub source/tests/build/signing and joint testing remain the current workflow.
No local source builds, autonomous emulator/visual gates or connection upgrades.
The Unified owner holds the current implementation and deployment evidence.
