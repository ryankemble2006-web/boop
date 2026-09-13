# Unified v164: music bounce candidate from accepted v162

Updated 2026-09-13. Task branch: `boop-unified-v164-music-bounce`. Signed candidate is delivered for Ryan's manual test. NOT installed or merged by this task. The accepted v162 branch remains `boop-unified-native-lyrics-v162@112d09b5b446d6582954a6d89b3700fe16298ecb`.

## Current request and starting point

Ryan reported that v163 did not move and broke his new Lyrics button, then said he rolled back himself. His latest instruction is to start again from confirmed v162, leave its confirmed features alone, make v164 with actual music bounce, sign it and give him the APK to test rather than hosted visual acceptance. The reported rollback is user evidence; this task did not inspect or change the installed app. The exact cause of the reported v163 lyrics regression was not established here.

This branch starts directly from the accepted v162 commit above. It does not merge the v161 Music Lab or v163 tree. The existing Unified owner, v162, main and lab branches were not written by this task. New v164 uses the normal Unified package, not a side-by-side lab.

## Exact signed candidate

- Package/version: `com.boop.alpha1`, `164 / 1.2.164-music-bounce`.
- Build/source commit: `f9f65569250b9dc02602101ef4d56195824e0380`.
- Signed GitHub run `34778178916`, job `103780068294`: SUCCESS.
- Artifact `BOOP-Unified-v164-Music-Bounce`, ID `10323694771`.
- APK `BOOP-Unified-v164-Music-Bounce.apk`, 155290450 bytes.
- APK SHA256 `d7ae61fc956dddc064211b7e5b3c5197b1ce4e55800acd558cee4b9e622eb5c8`.
- Artifact ZIP SHA256 `0a08fcbd7ebb4144bd1ea275b33e92b4be42534981c1953671b39295eb856aca`.
- Existing permanent signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The connector-downloaded ZIP and extracted APK were hash checked in the artifact sandbox. The embedded build commit, badging, public signing receipt and native lyrics/bounce class descriptors matched. Artifact extraction did not run the app or build laptop sources.

## Implemented now

`MusicBounceSource` uses Android Visualizer output-mix waveform data on a short-lived worker. DC-rejected loudness drives `MusicBounceEnvelope`: quick rise, softer fall, bounded vertical height. `MusicBounceRenderer` offsets the Now Playing GL viewport, translating the whole existing puppet without changing the shared eye renderer or shaders. Sampling starts only while the puppet is visible, playing and permitted; it stops/releases on pause/hide/detach. Stale, missing and constant audio data produce no fabricated bounce.

Saved animation speed still drives the unchanged canonical blink/expression clock. Audio level controls the added whole-puppet bounce independently. No BPM lookup, beat inference, physical-microphone fallback, recorder, media projection, audio-focus change, playback command or volume setting is added by the bounce implementation.

The earlier conditional permission Activity and flow were reused as exact source blobs, not a branch merge. Missing access is offered once from the foreground music screen; it never opens from a floating service. Ryan operates Continue and Android's permission choice. Manual retry remains Launcher Settings > Now Playing > Music audio access. Existing uses-permission declarations are unchanged. Permission UI, native audio availability and visual bounce remain for Ryan to test.

## Protect v162

The only modified existing app/build files are the version fields, one private permission-Activity manifest line, six settings-entry lines and the Now Playing puppet adapter. All v162 lyrics files, Lyrics-button callback, media-session manager, launcher routing, startup code, voice code, existing materialization scripts, dependencies, artwork, shaders, shared renderer and animation engine remain unchanged in source. The v162 ShieldLyricsActivity manifest registration is retained and was found in the actual APK. The accepted half-size passive licence footer is not changed. No retired Lyrics Lab dependency is introduced.

Historical accepted v162 APK: `cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171`; build source `1e0136ffa9732353035f88ca7a7cb131f7481557`. Its accepted Lyrics/Skip report and earlier v161 speed/colour acceptance remain in `docs/handoffs/2026-09-13-unified-v162-accepted-lab-retired.md` and the full base root handoff at `112d09b5b446d6582954a6d89b3700fe16298ecb`. Do not reopen those accepted repairs absent a new report.

## Verification level and next step

Read `docs/handoffs/2026-09-13-v164-music-bounce.md` for exact red/green, regression and package results. New tests passed with 332 numerical envelope checks. Native lyrics entry/data/transport/lifecycle checks, existing timing/materialization and 10 canonical-owner/bay checks passed. Two inherited one-time v162 source-freeze tests deliberately skip future versions; the new v164 source-preservation gate passed instead. Full signed assembly and package/signature/integrity/packaged-class checks passed. Existing nonfatal deprecation warnings remain. No complete historical test-suite rerun or independent reviewer is claimed.

No Shield, phone or emulator operation, installation, permission grant, reset, data clear or playback input was performed. Useful real Deezer/Shield levels and visible timing are NOT established by these checks. Deliver the APK in chat for Ryan to test. Do not silently install, merge, advance an accepted owner or call v164 physically accepted. Any later requested install must retain package/signature/hash checks and user data; actual OS permission choices remain Ryan's.

Source, nonvisual checks, builds, permanent signing and handoffs stay on GitHub. Laptop continuity files were read but no source worktree was changed or synchronized. Physical Pixel 10 remains excluded. Fetch the live task branch before further writes; preserve other operations. This final handoff/status/memory update changes documentation only, leaving the signed source commit above unchanged.
