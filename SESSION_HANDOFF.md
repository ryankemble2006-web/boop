# Unified v165: doubled music lift, signed candidate ready

Updated 2026-09-13. Task owner remains `boop-unified-v164-music-bounce`; branch name does not determine APK version. No merge into an accepted owner, main or another task is authorized.

## Latest user observation and change

After the verified v164 Shield installation, Ryan said: "ha it kinda works :P make him bounce more obviously". Record partial positive user feedback on v164 motion and the request for stronger movement, not blanket acceptance of every audio source, permission flow or v164 lyrics behavior.

v165 doubles only the visible vertical lift in `MusicBounceRenderer.setHeightFraction`. The production diff from installed v164 is one `* 2f` multiplier and the two version fields in `unified/app-build.gradle`. Audio sampling, MusicBounceEnvelope and its rise/fall timing, permission flow, blink clock, puppet size, viewport dimensions, artwork and native Lyrics code remain byte-identical to v164. The maximum visible offset is now 0.28 of surface height instead of 0.14; silence/reset still restores the original viewport. Actual appearance and clipping at the larger offset remain for Ryan's test.

## Exact signed candidate

- Package/version: `com.boop.alpha1`, `165 / 1.2.165-music-bounce-stronger`.
- Source/build commit: `fe8c04274064643342734baaafb46077832f335c`.
- Signed run `34779552167`, job `103783924085`: completed SUCCESS.
- Artifact `BOOP-Unified-v165-Music-Bounce-Stronger`, ID `10325220435`.
- APK `BOOP-Unified-v165-Music-Bounce-Stronger.apk`, 155290450 bytes.
- APK SHA256 `bbce760abc5f50fa68663c344fa9b75edfd9feb76559fe35c328c507af09bda2`.
- ZIP SHA256 `a57a43525c072739a35296512dd24bce33cdeb73d034f1a552ae31d8f3341483`, 71282006 bytes.
- Permanent signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The connector-downloaded ZIP and extracted APK match those hashes and the embedded build receipt. Packaged version, public signer receipt, native Lyrics registration, archive integrity and music/lyrics class descriptors were inspected without running the APK. Full build/signature verification ran in the existing GitHub pipeline. No replacement key or signing change.

## Verification and preserved features

Test-first RED `71942f8282c618e260864e9ff46a110906e63304`, run `34779468840`, had three expected failures for missing v165 version and doubled lift, including the actual renderer reporting expected 2 pixels but receiving 1. Three existing groups passed. Logs were read before the one-line implementation.

GREEN run above passed all six music test groups: 332 unchanged envelope assertions and 27 production-renderer amplitude/delegation assertions. New guards restrict changes from v164 to the renderer multiplier and version only, while retaining the v162 source-preservation gate. Native lyrics route/data/transport/entry/lifecycle checks, six existing timing functions, materialized source/art checks, 10 canonical-owner/bay contracts and three source-to-materialized comparisons passed. Full signed assembly completed with 104 tasks; APK identity, signature, native lyrics/music class presence and archive integrity passed. Two inherited one-time v162-only source freezes still skip later versions; these skips are not claimed as passes. No complete historical-suite rerun, independent reviewer or hardware/visual acceptance is claimed. Existing nonfatal warnings remain.

Accepted v162 source lineage remains `boop-unified-native-lyrics-v162@112d09b5b446d6582954a6d89b3700fe16298ecb`, rechecked unchanged. Preserve its internal Lyrics button/screen, Skip observation and passive half-size licence footer. The retired Lyrics Lab is not a dependency. v161 blink-speed/colour acceptance remains inherited evidence; speed is local, colour shared. Do not restart from the old v161 lab or v163 tree.

## Delivery and next step

v165 is signed and supplied for Ryan to test, NOT installed by this tuning task. No ADB/device input, permission grant, settings reset, data clear, phone/emulator operation or local source edit/build occurred. Installed v164 was left untouched by this task; its last verified identity and partial user feedback remain the physical baseline. Do not infer that the newly built v165 has run on Shield.

Give Ryan the direct APK in chat. A subsequent requested installation targets the identified Shield and updates Unified in place with hash/package/signature checks and no new grants or data clearing. Runtime/visual testing is joint with Ryan. Do not merge or advance an accepted branch automatically. Leave physical Pixel 10 and other tasks alone.

Source/build/signing/handoffs remain on GitHub. Laptop continuity files were read only; no checkout synchronization is claimed. Keep working Desktop Commander 0.2.47 unchanged. Fetch LIVE task HEAD before further writes and preserve concurrent work. This final handoff/status/memory update is documentation only.

Full receipt: `docs/handoffs/2026-09-13-v165-stronger-bounce.md`. Prior full root handoff: `95e2d153ac6e6b319f50284d4fbdb53bcb83c294`; historical v164 build/install receipts remain unchanged under `docs/handoffs/`.
