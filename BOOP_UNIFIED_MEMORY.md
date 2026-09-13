# Unified memory: v162 native lyrics integrated, Lab retirement pending confirmation

## Current delivery and ownership

2026-09-13. Ryan requested: roll the tested Lyrics Lab into Unified161 as162,
replace the Now Playing Lyrics route, and bin the Lab once confirmed.
PR #10 merged `boop-unified-native-lyrics-v162` into the existing Unified owner
`boop-unified-eye-sync-safe-v159` at `0908d6955c90978e97dcbae9031f3f1de638bd9d`.
Main remains the context hub and already points to that owner. Do not mistake
its v159 branch suffix for the installed application version.

Shield is installation-verified on com.boop.alpha1 / 162 / 1.2.162-native-lyrics.
Build source 1e0136ffa9732353035f88ca7a7cb131f7481557; signed run34773509395;
artifact10322553107; APK cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171.
Permanent signer f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde.
An ordinary adb install -r upgraded the exact accepted v161; resulting package,
version and base-APK hash matched. Neither phone was queried or updated.
No launch, media key, permission grant, data clear or emulator operation occurred.

## Native lyrics, not the old macro

The existing Now Playing button enters the internal unexported ShieldLyricsActivity
through exact-ID native preflight. It does not launch Deezer's menus or another app.
Shared data/renderer/loader and the footer are byte-identical copies from the lab
input 5ce581be6f1da10eb47640c4c11636a4bfa9e330. The passive licence/copyright credit
sits bottom-right at half its previous size; main song lyrics remain unchanged.
No Back to Now Playing footer control. Remote Back follows the Activity path.

Unified retains its own unchanged manager/state bus, with callbacks for active
tokens independently of displayable playback state. Do not import the separate
Lab observer, listener or package. The Lab's old skip regression was caused by
dropping that callback during temporary states; it was fixed and user-confirmed.
There is no new permission, account credential, dependency or persistent lyric store.

## Conditional Lab removal is still pending

com.boop.lyricslab remains installed on Shield at159 / 0.1.159-lyrics-footer,
APK fb373b09572379976dcf6b15dd54ce307f7153933ca61cd3d7f8a9204d716503,
verified unchanged before/after the Unified update. Ryan's earlier confirmation
was for Skip inside the Lab, NOT this new integrated route.
Wait for Ryan to confirm Unified's Now Playing -> Lyrics and refresh behavior.
Then uninstall ONLY the separate Lyrics Lab on Shield and verify it is absent;
do not remove Unified, other Labs, Deezer or source history. The user's conditional
cleanup request does not authorize early removal on the strength of a CI pass.

## Verification boundaries

New routing/transplant regression red:34773211530. Native green:34773471412 and
latest test-only head fad4ab24 in34773700715. Four source/route tests plus61 timed,
35 transport,2 incomplete,7 entry and28 Activity/state/lifecycle assertions pass.
Full pipeline also passed235 Unified and68 Shield functional tests with no failures,
errors or skips, plus preserved colour/speed/source-asset checks. This is not hosted
visual acceptance. New Android/service/view boundaries are controlled doubles.
In-session scope review is recorded on PR10, not an independent reviewer.

The merge differs from app build1e0136ff only by12 test lines allowing later version
bumps beyond162 to evolve source. No compiled input changed after the signed build.
Documentation updates do not change the APK. Separate deployment identity from
Ryan's pending physical confirmation; do not manufacture all-catalogue/offline or
natural-track-completion outcomes from the earlier Lab Skip confirmation.

## Accepted v161 foundation remains intact

Base593ad609 / build0b6ee6f9 carries Ryan's accepted speed changes on BOTH Shield
and Pixel7, and automatic colour delivery in BOTH directions. Speed remains local;
colour is shared. All unrelated v161 app source/artwork is unchanged by integration.
Do not reopen those accepted repairs or reset his preferences. Previous acceptance
and installation receipts remain under docs/handoffs and at the base commit.

## Workflow and history

GitHub source/non-visual tests/build/permanent signing, requested installation,
then joint testing with Ryan. No automatic emulator gate or local source/build loop.
Physical Pixel10 remains excluded; this lyrics delivery did not touch Pixel7 either.
Keep Desktop Commander0.2.47 unchanged while it works; ADB is fine per Ryan.
No private addresses, raw device data, screenshots, keys or APKs in public source.
No local source checkout was changed or claimed synchronized.
Read SESSION_HANDOFF.md and docs/handoffs/2026-09-13-unified-v162-lyrics.md.
Lab acceptance/provenance remains in boop-lyrics-lab-side-by-side-v157; do not erase it.
