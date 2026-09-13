# Lyrics Lab v159: passive bottom-right licence footer

Date: 2026-09-13. Branch: `boop-lyrics-lab-side-by-side-v157`.
Package: `com.boop.lyricslab`; version `159 / 0.1.159-lyrics-footer`.

## Request and exact scope

Ryan asked to keep working in the separate lab while the other Unified branch
finishes, remove "Back to Now Playing", put the lyrics licence in that location,
and halve its font size. The assistant explicitly interpreted half-size as the
credit text, not the actual lyrics. No merge was requested or performed.

The shared ShieldLyricsView on THIS lab branch now removes the footer navigation
label, click/focus listeners and its fallback focus reference. The existing credit
retains the licence/copyright content and clears with its document. It is passive,
right/bottom-aligned at the former footer's right/bottom inset. The credit uses
5 instead of 10 at both construction and measured sizing. Its container extends
left within the lyrics column to retain room for attribution. The main lyrics,
artwork, track details, transport controls, session observer, loader, timestamps,
remote Back handling, manifests and signer are unchanged.

## Source and checks

Application source: `e6f9bbb736ac90287815bea3a9c48496505675ed`.
Accepted starting checkpoint: `820c1621f3340b96a915b353b4ef8cc0d217bc97`.
App changes: ShieldLyricsView footer only and the lab's version fields.
Tests add a non-visual source-wiring contract: removed navigation action, passive
attribution, unchanged document-credit binding, no new Back-key interception.
No screenshot, rendering comparison or visual acceptance runs on GitHub.

Recorded test-first red: `34772090734` at `a1aa97799d6bb9a9b30faf1c9440d5d6ca5c82be`.
The footer test failed because the old action still existed; the other three
packaging/source/identity tests passed. An initial test-only fixture argument typo
was corrected before this clean red run. No existing assertion was weakened.

Signed build: `34772352902`, completed SUCCESS for the exact source above.
Four packaging/source-wiring tests pass, including 19 exact-recording identity
assertions. Existing checks also pass: 73 session continuity, 61 timed-data/owner,
35 transport, two incomplete timing and seven entry-control-flow assertions.
Package identity, permanent signer and ZIP integrity pass in the existing workflow.
Scoped GitHub diff reviewed in-session; no independent reviewer or visual pass claimed.

Artifact: `BOOP-Lyrics-Lab`, ID `10322900224`.
Artifact archive SHA256 (not the APK):
`2c33ee9bb568fea52f30c609ba6b0b0226cdd32863b1a43c9651e99688e4b951`.
APK SHA256:
`fb373b09572379976dcf6b15dd54ce307f7153933ca61cd3d7f8a9204d716503`.
Permanent signer SHA256:
`f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Shield delivery and limits

Downloaded the GitHub artifact to a private temporary artifact cache only. Checked
its source receipt, package/version, APK digest and signature independently.
Before updating, the Shield lab matched the confirmed v158 APK digest
`c95bb9cc343ec9752fb2be610d0c61c0e4d01eb4faa3c62b8c6b28713db00c09`.
`adb install -r` succeeded for ONLY `com.boop.lyricslab`. Read-back showed v159 and
its installed base APK SHA256 exactly matched the candidate above.

Separate Unified reported `161 / 1.2.161-lab-scale-independent`; its APK hash was
identical before and after the lab-only installation. No other app installation,
app launch, playback input, permission change, data clear, phone or emulator
operation was performed. No local app-source edit/build was used.

The current main BOOP_START_HERE workflow is GitHub development and joint device
testing. It supersedes earlier automatic emulator-first rules. Accordingly no
new emulator/visual gate was imposed. Appearance of the new footer awaits Ryan's
inspection; package verification is not visual acceptance.

## Preserved acceptance and next integration

Ryan already confirmed Skip reloads the lyrics inside lab v158 on real Deezer/
Shield. Do not reopen that accepted bug or require ADB reconnection. The previously
approved design is preserved except the explicit footer changes requested here.
Keep the lab separate while the other branch finishes. A later merge must carry
the feature into the then-current Unified source, retaining its own session manager
and concurrent changes, then test the integrated build together. No merge yet.
Natural completion, offline/no-lyrics and longer-session coverage remain distinct
from the confirmed manual-Skip result. Earlier receipts remain in the dated
local-refresh and Shield-v158-install handoffs and the v158 user-confirmation
checkpoint `820c1621`. Private data and raw device captures stay off GitHub.
