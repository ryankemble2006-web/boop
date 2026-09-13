# Unified memory: stronger v165 candidate after v164 music feedback

Updated 2026-09-13. Task owner remains `boop-unified-v164-music-bounce`.

## Latest user evidence

Ryan tested installed v164 and said "ha it kinda works :P make him bounce more obviously". This is positive but qualified feedback about movement. It does not establish perfect beat matching, every audio source, lyrics acceptance of v164, or appearance of the newly built v165.

Keep the task narrow: actual music controls whole-puppet bounce; saved animation speed still controls blinks and expressions independently. No BPM analysis, synthetic beats, per-hit clip restarts or physical-microphone fallback. Keep the accepted art and native Lyrics button intact.

## v165 change and exact candidate

v165 is v164 plus a `* 2f` visible-lift multiplier in MusicBounceRenderer and the two version fields only. MusicBounceSource, MusicBounceEnvelope, rise 28 ms/fall 140 ms, permission flow, blink engine, viewport width/height and all native lyrics/launcher/voice/materialization inputs are unchanged. Silence and reset restore zero lift. Visible maximum offset rises from 0.14 to 0.28 of surface height. Stronger travel and clipping require Ryan's visual test; the bounds alone are not visual acceptance.

Package/version `com.boop.alpha1`, `165 / 1.2.165-music-bounce-stronger`.
Source `fe8c04274064643342734baaafb46077832f335c`.
Signed run `34779552167`, job `103783924085`, SUCCESS; artifact `10325220435`.
APK `BOOP-Unified-v165-Music-Bounce-Stronger.apk`, SHA256 `bbce760abc5f50fa68663c344fa9b75edfd9feb76559fe35c328c507af09bda2`.
Existing signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Downloaded artifact hashes, embedded source and package/signing receipts matched; no APK execution in the artifact sandbox.

## Preserved baseline and installation history

The previous v164 is the last verified Shield installation, source `f9f65569250b9dc02602101ef4d56195824e0380`, APK `d7ae61fc956dddc064211b7e5b3c5197b1ce4e55800acd558cee4b9e622eb5c8`. Its standard in-place install preserved HOME/UID/first-install timestamp and existing audio/media grants. No device was operated or updated during v165 tuning. Full prior root records remain at `95e2d153ac6e6b319f50284d4fbdb53bcb83c294`, and dated v164 receipts are retained.

This lineage starts from accepted v162 at `boop-unified-native-lyrics-v162@112d09b5b446d6582954a6d89b3700fe16298ecb`, not old v161 Music Lab or v163. v162 native Lyrics and Skip were user-accepted with "perfection". Preserve internal ShieldLyricsActivity, button route, track observer, main lyric size and passive half-size licence footer. No retired Lyrics Lab dependency. Earlier v161 speed/colour acceptance remains inherited evidence; speed is local and colour shared. The v162 branch was live-checked unchanged; this task did not merge or write main, the accepted owner or other labs.

## Verification and delivery boundary

Test-first red71942f8282c618e260864e9ff46a110906e63304 had expected missing-version/gain failures, including the production renderer yielding 1 pixel rather than the requested 2. Final six music test groups passed with 332 unchanged envelope assertions and 27 renderer amplitude/size/lifecycle checks. Source guards permit only the multiplier/version change from v164. Native lyrics routing/data/timing/transport/lifecycle, original timing/materialization and 10 owner/bay checks passed. Signed assembly, packaged classes/version, permanent signature and integrity passed. Two inherited one-time v162 freezes skip later versions; new guards provide current preservation evidence. No full historical-suite rerun or independent reviewer claimed.

v165 is signed for Ryan to test, not installed or physically accepted. Deliver the APK in chat; only install following a request, keeping normal package/hash/signature checks and data. No shell grants, permission reset, Home replacement or autonomous visual/emulator loop. No new main-phone access; leave physical Pixel 10 alone.

Keep development, nonvisual tests, builds, signing and handoffs on GitHub. Desktop Commander0.2.47 stays unchanged. Laptop continuity reads are not source synchronization; no local app checkout was edited or built. Preserve other tasks and private artifacts. Fetch LIVE task HEAD before continuation and update handoff/status/memory after material results. Receipt: `docs/handoffs/2026-09-13-v165-stronger-bounce.md`.
