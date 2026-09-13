# Native Deezer lyrics v157: runtime verification in progress

Updated 2026-09-13. Owner: `boop-unified-native-lyrics-v157`.
GitHub is the source authority. Ryan explicitly selected accepted v156 at
`a901c1e9f31e55c710e31ac7ff4f5924c9769d56` as the base. Do not import the unfinished
local v157 worktree or rebase onto an unrelated later experiment. Current main's
v156 acceptance supersedes the inherited v156 handoff's old pending wording.

## Approved behavior

BOOP owns a borderless full-screen music/lyrics presentation. Deezer keeps playing
in the background; no notification launch, UI clicking or foreground macro remains
in the lyrics entry path. Exact recording identity comes from native Deezer's
published MediaSession metadata. Timed text follows pause/resume/seek and automatic
track changes while the lyrics screen is open. Missing/unknown lyrics never paint
the previous song. Preserve artwork, canonical animation/hue, favourites, artist/
album browsing, existing transport controls, 10-second seek and long-Back behavior.
No new permissions, account credentials or permanent lyric cache. Neither phone
is a deployment/test target. No GitHub visual checks; runtime checks happen locally.

## Evidence

- Fresh anonymous Deezer probes returned text, millisecond timing and provider
  credit for public recording IDs 3135556 and 3135553. Both standard and word-only
  formats were checked. This is live technical evidence, not a supported API or
  catalogue/licensing guarantee. The Cast receiver implementation was not copied.
- GitHub `34756703301` recorded the genuine missing-document assertion before
  implementation. An earlier checksum-URL 404 was setup failure, not TDD red.
- Parser/request ownership: 61 checks. Timed transport: 35. Incomplete-timing
  handling: 2. All 98 passed on application source `8186447a1e47fdcded2ac2d259f250024cdccca1`.
- Original signed build run `34757490259`, artifact `10318200099`, passed 235 Unified
  and 68 Shield tests plus the preserved non-visual pipeline. APK identity/signature/
  hash/source were independently checked after download. **That initial APK is not
  a deliverable:** local runtime testing found window-sized children due to applying
  layout constraints after measurement. Do not install it on the Shield.
- The layout failure was reproduced on the dedicated TV emulator with actual
  screenshots/hierarchy: artwork and transport children retained viewport dimensions.
  Source `f98d2a3b5f3c4b3a58868f1a9ee6856a5f750850` moves constraints before child measurement.
  Its corrected isolated fixture build `34758695802` is green; reinspection is pending.
- Entry control-flow test run `34758695805` correctly failed because a completed
  hidden-host lookup left Lyrics entry busy. The current source clears that completed
  request before checking focus, while still rejecting a late launch. Seven dedicated
  real-entry control-flow assertions cover the correction; green rerun pending.

## Local runtime isolation

The existing Android TV emulator had concurrent v159 installed. Android refused
our v157 downgrade; no override, uninstall or data clear was attempted. A separate
AVD `BOOP_Lyrics_157_8186447` was created from the already-installed API36 TV image.
It booted and accepted v157 plus the separate `com.boop.lyricspreview` fixture.
The fixture copies the exact production renderer with invented text and synthetic
artwork. It has no production test hooks and must never be installed on the Shield.
Initial Unified entry launches to its ordinary fresh-install profile chooser.
Neither physical Pixel was touched. Shield remained on accepted v156 at the last
check and was playing native Deezer. Recheck its version before any installation.

## Next gates

1. Confirm entry regression green and inspect the corrected fixture on the dedicated
   TV emulator. Exercise pause/resume, seeking, track changes, blank states and Back.
2. Run the complete existing signed Unified workflow against the corrected source.
3. Verify exact downloaded signer/package/hash/source, then emulator launch and
   Shield-only upgrade after a fresh installed-version check. Test actual native
   Deezer with BOOP lyrics foreground, including leaving/reopening and track changes.
4. Record final source/run/artifact/hash receipts, reconcile branch status/memory,
   and verify the live feature branch. No accepted/main branch merge is authorized.

Prior protected delivery history remains in the parent v156 `SESSION_HANDOFF.md`
at `a901c1e9f31e55c710e31ac7ff4f5924c9769d56`. See
`docs/superpowers/plans/2026-09-13-native-lyrics.md` for scope and research references.
Private local captures, raw diagnostics, downloaded APKs and credentials stay out
of GitHub. Review/test success must never be relabelled as Ryan's visual acceptance.
