# Unified memory: v162 native lyrics accepted, standalone Lab retired

## Latest result and completed conditional cleanup

2026-09-13. Ryan replied "perfection" after the request to confirm Now Playing ->
Lyrics and Skip in integrated Unified v162. Treat this as USER ACCEPTANCE of the
integrated native lyrics feature on Shield. It supersedes the earlier awaiting-
confirmation state; do not ask him to repeat that already-accepted gate.
This is Ryan's physical report, not a new assistant-run UI/audio/lyrics test.

Ryan had explicitly said to bin the Lab once confirmed. That condition is now
satisfied and cleanup was performed: only com.boop.lyricslab was uninstalled from
Shield. ADB returned Success/exit0 and a fresh package-list read verified absence.
Before removal the Lab was159 /0.1.159-lyrics-footer with APK
fb373b09572379976dcf6b15dd54ce307f7153933ca61cd3d7f8a9204d716503.
Unified was162 with the exact signed hash before AND after; it was not changed.
No other Lab/Deezer/Unified/source history was removed. No phones, emulators,
playback keys, app launches, new installs, manual permission/settings changes,
app-source edits or builds accompanied this cleanup/documentation continuation.

## Current delivery and ownership

Owner boop-unified-eye-sync-safe-v159. Integration branch boop-unified-native-lyrics-v162.
PR10 merged the approved feature at0908d6955c90978e97dcbae9031f3f1de638bd9d.
Main is the shared-context hub and already points to this owner. The v159 suffix
is NOT the app version. Do not restart from the older standalone Lab base.

Shield: com.boop.alpha1 /162 /1.2.162-native-lyrics.
Build source1e0136ffa9732353035f88ca7a7cb131f7481557; signed run34773509395;
artifact10322553107; APK cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171.
Signer f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde.
The prior install upgraded exact accepted v161 and checked source/package/signature/
hash. The cleanup above independently rechecked its installed identity unchanged.
Neither phone was queried or updated in this lyrics task.

## Native lyrics, retained UI and observation

Now Playing's existing Lyrics button enters internal unexported ShieldLyricsActivity,
not Deezer menus or another app. It has no runtime dependency on the retired Lab.
Shared data/renderer/loader and footer came from exact reviewed Lab source blobs.
The passive licence/copyright credit sits bottom-right at half its previous size;
main song lyrics retain their size. Back to Now Playing footer action is removed;
physical Back remains the Activity's path. Keep this accepted design.

Unified's own unchanged manager/state bus retains active-token callbacks separately
from displayable playback state. The Lab-specific observer/listener/package was not
imported. Its earlier skip failure was real, fixed, and user-confirmed before the
integration; retain that provenance, not the obsolete physical package.
No new permission, account credential, dependency or persistent lyric store.

## Prior test provenance, not rerun for acceptance

Routing/transplant red34773211530; green34773471412 and latest test-only fad4ab24
in34773700715. Four source/route checks plus61 timed,35 transport,2 incomplete,
7 entry and28 Activity/state/lifecycle assertions passed. The full pipeline passed
235 Unified and68 Shield tests with no failures/errors/skips, preserving colour,
speed and artwork source checks. Android/view boundaries in new tests were doubles.
Scoped PR10 review was in-session, not independent. No hosted visual acceptance.
The merge differed from built app1e0136ff only by12 later test lines permitting
future releases beyond162 to evolve source; no compiled inputs changed.

Ryan's new acceptance must not be expanded into individually unreported catalogue,
offline, natural-track-completion or all-hardware test results. Those limits do not
justify re-opening the integrated feature he just accepted or resurrecting the Lab.

## Accepted v161 foundation and workflow

Base593ad609 / build0b6ee6f9 retains speed acceptance on both Shield and Pixel7 and
automatic colour both ways. Speed is local, colour shared. Unrelated v161 source/
artwork was preserved during integration. Do not reset choices or reopen repairs.

GitHub owns source/non-visual tests/build/permanent signing and documentation;
device tests are joint with Ryan. No autonomous emulator gate/local source-build
loop or connection upgrades. Desktop Commander0.2.47 remains pinned; ADB is fine
per Ryan. Physical Pixel10 remains excluded. Keep other tasks/worktrees intact.

Receipt: docs/handoffs/2026-09-13-unified-v162-accepted-lab-retired.md.
Earlier integration: docs/handoffs/2026-09-13-unified-v162-lyrics.md.
Pre-acceptance docs remain at978bf7df5e0759512b8a69807d9badeee7a8bb95.
Standalone branch boop-lyrics-lab-side-by-side-v157 remains history/recovery source,
not an app to reinstall. No private captures, raw dumps, addresses, keys or APKs
belong in the public repository. No local checkout synchronization is claimed.
