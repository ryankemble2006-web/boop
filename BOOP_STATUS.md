# BOOP status

Updated 2026-09-17. Owner: `boop-wall-shield-split-v207`. Implementation: `boop-shield-room-panel-v213`.

Shield v213 / `1.2.213-shield` is signed and ready for Ryan's manual test. It adds the adaptive charcoal room-control panel below favourites, populated from the existing Set this device room selection. No duplicate room setup. Home settings has a persisted Smart home panel ON/OFF switch; disabled and non-Home states stop the panel's network session. Room controls support existing physical lights, switches/plugs and fans. Sensors remain deferred.

The fresh implementation starts from v212 owner `2ab0db655368089b19f9c705fba2cd404a1cd4e7`, not the interrupted v211 panel work. The v209 close-media repair, approved weather chrome, v211 network permission and v212 weather column-height repair are preserved. Voice/audio and BOOP's character rig/artwork were not edited.

Signed source `7cb211b2a4f2b0307b500cc7ec718effa609ffd5`; successful run `35244156761`, job `105279913277`.
Artifact `10506423552`, `BOOP-Shield-v213-Wall-v207-Signed`.
Shield file `BOOP-Shield-v213.apk`, 160485741 bytes, SHA-256 `cb21540979161b31ebebd756fbfea40ab1217ed8781ca8073094059c2286f783`.

Verification passed: 96 local focused checks; CI 70 initial checks, 18 inherited stages, 95 materialized checks, 13 HA registry/filter unit tests, both shell builds, and actual APK identity/certificate/native/art checks. Suites overlap, not a summed unique count. Downloaded ZIP/Shield APK CRC and receipt hashes independently matched, with all 16 native-library hashes and frozen art unchanged. The first CI signer-ordering failure was repaired without changing the permanent key or dropping checks.

Manual installation, live room-device control, remote focus and visual acceptance remain PENDING. No automatic installation, emulator, screenshot, device driving or permission changes occurred. Wall remains v207 and is not to be installed for this Shield feature.

Prior v212 receipt and physical-acceptance distinctions are retained in SESSION_HANDOFF.md. Retired root workflow-rule/context files and source-preservation allowlists remain retired. Functional tests, package checks and signer/integrity verification remain active.
