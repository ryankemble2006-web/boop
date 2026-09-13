# BOOP Lyrics Lab v159: footer update installed, no Unified merge

Updated 2026-09-13. Owner: `boop-lyrics-lab-side-by-side-v157`.
Package: `com.boop.lyricslab`; `159 / 0.1.159-lyrics-footer`.
GitHub owns source, non-visual tests, builds, permanent signing and handoffs.

## Latest request and result

Ryan asked to stay in the lab while the other Unified branch finishes: remove
"Back to Now Playing", put the lyrics licence in its place and halve its font.
The credit (not the lyric lines) now uses half its previous size, right/bottom
aligned at the former footer inset. Its licence/copyright content remains intact.
The footer no longer consumes navigation focus or has an exit click action.
The remote's existing Back path is not altered. No other design change is made.

Signed lab v159 was installed on Shield ONLY as com.boop.lyricslab. Exact source,
package/version, permanent signature and downloaded/installed APK digest match.
The separate Unified app's APK hash was identical before and after the lab update;
it reported v161 / 1.2.161-lab-scale-independent in that check. Neither phone nor
any emulator was operated. No app launch, playback key, permission change or data
clear was issued. Appearance of the new footer is for Ryan to inspect, not inferred
from the successful package verification.

## Exact candidate

Source: `e6f9bbb736ac90287815bea3a9c48496505675ed`.
GitHub signed run: `34772352902`, SUCCESS.
Artifact: `BOOP-Lyrics-Lab` / `10322900224`.
APK SHA256: `fb373b09572379976dcf6b15dd54ce307f7153933ca61cd3d7f8a9204d716503`.
Signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Non-visual footer-wiring regression was red before implementation and green after.
Existing 73 session, 19 identity, 61 timed-data/ownership, 35 transport, two incomplete
and seven entry assertions passed, as did packaging, source-preserving materialization,
build, signing and archive checks. In-session scoped diff review, no independent
reviewer or new runtime/visual acceptance claim. Detailed receipt:
`docs/handoffs/2026-09-13-lyrics-footer-v159.md`.

## Acceptance carried forward

Ryan enabled notification access himself and confirmed the original lab lyrics
and player controls. He approved the full-screen emulator presentation. His v157
report that lyrics only refreshed after exit/reopen was genuine and was fixed in
v158 by retaining the active media-token subscription through transient states.
He then explicitly confirmed: lyrics reload when Skip is pressed inside the lab.
That real-Deezer/Shield manual-Skip result is USER-CONFIRMED PASS. Do not ask him
to repeat it merely because an older handoff calls it pending. The observer, clock,
provider, loader and main lyric renderer have not changed in this footer update.

The exact v158 passed 38 actual-Android-session/production-presentation assertions
twice locally with synthetic provider/words. Keep that separate from Ryan's physical
Skip confirmation. Natural completion, offline/no-lyrics, both skip directions and
longer-session physical coverage were not individually established by that message.

Ryan also said ADB is fine. Earlier connector timeouts are historical and are not
a current blocker or a reason to reinstall/upgrade the working connection.
An earlier capture/Next command had unknown outcome; do not retroactively call it
an automated pass. The dated installation receipt preserves that history.

## Current workflow and merge boundary

Current main BOOP_START_HERE now specifies GitHub development and joint testing,
not an automatic emulator-first or autonomous visual gate. Do not use local app
source/builds or start/reconfigure AVDs automatically. Keep Desktop Commander
0.2.47 and its known-good command unchanged. Later lab delivery targets only the
separate lab; neither phone nor Unified is an install target in this task.

No merge into main or Unified is approved now. Ryan explicitly wants to wait for
the other branch and polish here. Later integration must use the live then-current
Unified successor, not install the original v156 base over newer work. Its session
manager is separate from the lab-specific observer. Test the integrated build
with Ryan when that merge is agreed. Keep approved animation/artwork/other features.

Earlier evidence: `docs/handoffs/2026-09-13-lyrics-local-refresh-tests.md`,
`docs/handoffs/2026-09-13-lyrics-shield-v158-install.md`,
`docs/handoffs/2026-09-13-lyrics-track-refresh.md`.
The preceding user-acceptance handoff is preserved at commit `820c1621f3340b96a915b353b4ef8cc0d217bc97`.
Historical product memory remains under `docs/history/lyrics-lab-pre-user-confirmation/`.
No private addresses, raw device dumps, screenshots, tokens or APKs in source control.
