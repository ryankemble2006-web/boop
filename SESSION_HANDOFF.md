# Unified v162 native lyrics: merged and installed on Shield

Updated 2026-09-13. Current Unified owner remains `boop-unified-eye-sync-safe-v159`.
The branch name is not the APK version. Integration branch:
`boop-unified-native-lyrics-v162`. PR #10 was merged into the current owner at
`0908d6955c90978e97dcbae9031f3f1de638bd9d` after GitHub checks passed.

## Latest request and delivered change

Ryan requested integrating the tested Lyrics Lab feature into v161 as v162,
routing the existing Now Playing Lyrics button to it, and binning the lab once
confirmed. The source integration and Shield installation are complete.

Now Playing's existing Lyrics action uses BOOP's private internal native lyrics
Activity, not the old Deezer foreground/menu macro and not com.boop.lyricslab.
The shared renderer/parser/client/loader and footer are exact source copies from
lab lineage `5ce581be6f1da10eb47640c4c11636a4bfa9e330`.
The Back to Now Playing footer is absent; its area contains the passive licence/
copyright credit at half its old size. Song lyric font size is unchanged.
Unified's existing manager, state bus and launcher remain byte-for-byte v161.
Its active-token observation already survives temporary non-displayable states;
the separate Lab observer/notification listener/application was NOT imported.

## Exact build and installation receipt

Application/build source: `1e0136ffa9732353035f88ca7a7cb131f7481557`.
Version/package: `162 / 1.2.162-native-lyrics`, `com.boop.alpha1`.
GitHub full signed run `34773509395`: SUCCESS.
Artifact `BOOP-Unified` / `10322553107`.
APK SHA256: `cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171`.
Permanent signer: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
GitHub artifact ZIP digest: `112b5ba62aa365227f419a0e9e825ca648991f6fab936e8e90b1a083ad453bc9`.
The ZIP digest is GitHub's reported archive identity, not the APK digest.

Downloaded APK source receipt, package/version, signature, hash and private
native-Activity registration were checked. Shield baseline was exactly accepted
v161 with APK hash `c68b81be9b7d3e10883d2aea52c05dc7b8c187fcd835eaddf73e25e91cb4acd6`.
Ordinary `adb install -r` returned Success. Installed version/name and base APK
SHA256 matched the v162 candidate above. No app launch, playback input, permission
change, data clear, phone or emulator command was issued in this delivery.

## Lab retention and next joint check

The separate Lab remains installed as `159 / 0.1.159-lyrics-footer`.
Its APK hash was the same before and after the Unified update:
`fb373b09572379976dcf6b15dd54ce307f7153933ca61cd3d7f8a9204d716503`.
Do not remove it because CI or installation passed. Wait for Ryan to confirm that
Now Playing -> Lyrics works in Unified v162, including the desired Skip refresh.
Then remove ONLY `com.boop.lyricslab` from Shield and verify its absence. Preserve
its source history/branch and the current Unified app. No further permission or
settings changes, phone deployment or blanket cleanup are implied.

Ryan already confirmed the Lab's real-Shield Skip refresh and controls; do not
reopen that Lab gate. Unified's new integration is awaiting his joint physical
confirmation. No new visual/audio/runtime acceptance was performed by the assistant.

## Verification and preserved acceptance

New native route/transplant tests were red on v161 in run `34773211530`, then green
on the implementation in `34773471412`. Latest test-only revision run
`34773700715` passed four integration/source guards and 61 timed-data/ownership,
35 transport, two incomplete-timing, seven entry and 28 internal Activity/state/
lifecycle/transport assertions. Android/service/renderer boundaries in the new
Activity harness are controlled doubles, not real-device evidence.
Full signed CI also passed 235 Unified and 68 Shield functional tests with zero
failures/errors/skips, plus the existing colour, speed, artwork-integrity and other
non-visual checks. Scoped in-session review recorded on PR #10; not independent.

After the APK build, `fad4ab24bc234e383f7b9ba22a22080eaf0f616d` changed only a test
helper so v162's one-time byte-preservation guard does not freeze later releases.
The merged tree differs from the built source only by those 12 test lines before
this documentation update; no APK input changed. All v162 guards still ran.

Accepted v161 base: `593ad609ff87f651d5273bd17f5a2c0ca3ef5198` (build `0b6ee6f9`).
Ryan's speed-on-both-devices and automatic two-way colour acceptance is retained.
Speed is local, colour shared. All unrelated v161 source and assets are unchanged.
Neither phone was touched; their earlier acceptance is not a new v162 device test.

Current workflow remains GitHub development/build/signing and joint device testing.
No automatic emulator/hosted visual gate, local source edits or claimed checkout
sync. Main already routes to this owner, so no ordinary-progress main edit needed.
Detailed receipt: `docs/handoffs/2026-09-13-unified-v162-lyrics.md`.
Prior v161 and Lab receipts remain at their pinned input branches/commits.
