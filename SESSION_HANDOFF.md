# Unified v162 native lyrics: user-accepted, standalone Lab retired

Updated 2026-09-13. Current Unified owner remains `boop-unified-eye-sync-safe-v159`.
The branch name is not the APK version. Integration branch:
`boop-unified-native-lyrics-v162`. PR #10 merged the feature at
`0908d6955c90978e97dcbae9031f3f1de638bd9d`.

## Latest acceptance and completed cleanup

Ryan replied "perfection" after being asked to confirm the integrated Now Playing
-> Lyrics screen and Skip behavior in Unified v162. Record the integrated lyrics
feature as USER-ACCEPTED ON SHIELD. This supersedes the previous pending Unified
confirmation. Do not ask him to repeat that gate or confuse his acceptance with
the earlier Lab-only result. It is user evidence, not a new assistant-run UI test.

His prior instruction was to bin the Lab once confirmed. After that confirmation,
only `com.boop.lyricslab` was uninstalled from Shield. The ordinary ADB uninstall
returned Success/exit 0; a fresh package-list query verified its absence. Before
removal, its exact version159 and hash matched the known standalone Lab. Unified
was v162 with the exact signed APK hash both before and after cleanup. No other
Lab, Unified, Deezer, phone, emulator or source branch was removed or modified.
No app launch, playback input, new installation, manual permission/settings change,
rebuild or app-source edit was performed in this acceptance/cleanup continuation.
Receipt: `docs/handoffs/2026-09-13-unified-v162-accepted-lab-retired.md`.

## Accepted feature and retained design

Now Playing's existing Lyrics action uses BOOP's private internal native Activity,
not the Deezer foreground/menu macro and not the now-removed standalone Lab.
The shared renderer/parser/client/loader and footer are exact source copies from
lab lineage `5ce581be6f1da10eb47640c4c11636a4bfa9e330`.
The Back to Now Playing footer is absent. Its area contains the passive licence/
copyright credit at half its former size; main song lyrics keep their size.
Unified's existing manager, state bus and launcher remain byte-for-byte v161.
Its active-token observation already survives temporary non-displayable states;
the Lab observer/notification listener/application was NOT imported.

## Exact build and physical identity

Application/build source: `1e0136ffa9732353035f88ca7a7cb131f7481557`.
Version/package: `162 / 1.2.162-native-lyrics`, `com.boop.alpha1`.
GitHub full signed run `34773509395`: SUCCESS; artifact `BOOP-Unified` / `10322553107`.
APK SHA256: `cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171`.
Permanent signer: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP digest: `112b5ba62aa365227f419a0e9e825ca648991f6fab936e8e90b1a083ad453bc9`.
The ZIP digest is the archive identity, not the APK digest.

The earlier ordinary installation upgraded exact accepted v161, verified package/
version/source/signature/hash, and left the Lab for confirmation. The later cleanup
above independently rechecked Unified's version/base-APK hash unchanged. The removed
Lab was `159 / 0.1.159-lyrics-footer`, APK
`fb373b09572379976dcf6b15dd54ce307f7153933ca61cd3d7f8a9204d716503`.
Its source and receipts remain recoverable on `boop-lyrics-lab-side-by-side-v157`.
Do not reinstall that obsolete helper merely to resume native lyrics work.

## Preserved verification and acceptance

Native routing/transplant tests were red on v161 in run `34773211530`, then green
in `34773471412` and latest test-only revision `34773700715`. Four source/route
checks, 61 timed-data/ownership, 35 transport, two incomplete-timing, seven entry,
and 28 internal Activity/state/lifecycle/transport assertions passed. New Android/
service/renderer boundaries were controlled doubles, not physical UI evidence.
Full signed CI also passed 235 Unified and 68 Shield functional tests with zero
failures/errors/skips and the existing colour/speed/source-artwork checks.
Scoped in-session review was recorded on PR #10; no independent reviewer claimed.

After the APK build, `fad4ab24bc234e383f7b9ba22a22080eaf0f616d` changed only 12 test
lines so v162's one-time byte-preservation guard does not freeze later releases.
No compiled APK input changed after the signed build. These preserved test results
were not rerun for this documentation-only source update.

Accepted v161 base: `593ad609ff87f651d5273bd17f5a2c0ca3ef5198` (build `0b6ee6f9`).
Ryan's speed-on-both-devices and automatic two-way colour acceptance is retained.
Speed is local; colour shared. Neither phone was touched for this lyrics task.
Do not infer every catalogue/offline/natural-completion scenario from the new
acceptance, or make those unreported cases a reason to reopen his accepted result.

## Next and continuity

Continue from the live Unified owner for the next requested change. Native lyrics
and the conditional standalone-Lab cleanup are closed, not awaiting confirmation.
No automatic emulator/hosted visual gate, local source edits, connection upgrade,
settings resets or claimed checkout sync. Main still points to the same owner;
no ordinary-progress main edit is needed. GitHub development and joint tests remain.
Earlier integration receipt: `docs/handoffs/2026-09-13-unified-v162-lyrics.md`.
The pre-acceptance handoff/status/memory remain at `978bf7df5e0759512b8a69807d9badeee7a8bb95`.
Prior v161 and Lab evidence remains at its pinned input branches/commits.
