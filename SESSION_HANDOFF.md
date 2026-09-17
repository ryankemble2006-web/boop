# BOOP current handoff

Updated 2026-09-17. Owner branch: `boop-wall-shield-split-v207`. Room-panel implementation branch: `boop-shield-room-panel-v213`.

## Current Shield: v213, signed and ready for Ryan's test

Package `com.boop.shieldoverlay`, version `213` / `1.2.213-shield`.
Ryan approved an adaptive, room-derived Home panel and then explicitly requested a fresh implementation after the earlier interrupted attempts. This version starts from the live v212 owner `2ab0db655368089b19f9c705fba2cd404a1cd4e7`; the incomplete `boop-shield-home-panel-v211` branch was not merged.

The lower Home area now contains native charcoal/cyan device controls from the existing **Set this device room** selection and existing Home Assistant pairing. No second room preference or pairing flow. The panel fills the available area below favourite captions, expands when lower-right BOOP disappears, and contracts with a 200 ms margin animation when he returns. The hero slot, favourites and BOOP's corner geometry remain parked. If optional content leaves insufficient space, the panel hides rather than covering it.

Home settings contains **Smart home panel: ON / OFF**, default ON. Off removes the panel and stops its Home Assistant session. D-pad Down enters from favourites; Left/Right traverse controls and stop at the ends; Up returns to the prior favourite. Device tiles retain identity and focus during live state updates. Existing supported physical lights, switches/smart plugs and fans appear; sensor readings, camera views and thermostat detail controls remain later work.

The separate RoomPanelController/RoomPanelSession reuse the existing HA repository and canonical room adapter. Old room callbacks and stale tile presses are rejected, membership is rechecked before commands, pending actions cannot double-toggle, and actual state is not replaced with optimistic success. Connections/subscriptions/retry timers stop when disabled or outside launcher Home. Legacy HomeDashboardController and voice/audio code were not changed.

## Verified signed artifact

Build source: `7cb211b2a4f2b0307b500cc7ec718effa609ffd5`.
Production feature commit: `cfcb627348c5fde2bc4be86553f8a9648192a51b`.
Successful GitHub run `35244156761`, job `105279913277`.
Artifact `10506423552`: `BOOP-Shield-v213-Wall-v207-Signed`.
Deliver **BOOP-Shield-v213.apk**, 160485741 bytes.
APK SHA-256: `cb21540979161b31ebebd756fbfea40ab1217ed8781ca8073094059c2286f783`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256: `4dc5cb1ce6b6385d2f575ad04ec06d9cb95e2a35a3b49c52088765e2813eebfb`.

Verification: 96 local focused tests; CI initial 70 focused tests, all 18 inherited stages, 95 materialized integration checks, and 13 HA registry/room-filter unit tests passed. These suites overlap and must not be added into a unique test total. Both app builds and actual package/version/certificate checks passed. The downloaded artifact ZIP and extracted Shield APK were independently checked against metadata/receipt; CRC, new feature classes, all 16 native-library hashes and frozen artwork matched. No rebuild or re-sign took place outside GitHub.

The first v213 run `35243778496` stopped before the full APK build because a new Gradle unit-test step ran before the existing required signer environment was prepared. The workflow now prepares that same permanent signer before Gradle configuration and supplies the existing password environment to the test step. No signer check was disabled and no replacement key was created.

## Physical acceptance and next step

Ryan downloads the Shield APK, drags it into his Shield scrcpy window, and tests the room devices, D-pad navigation, panel On/Off and expansion with/without media/BOOP. Physical installation and visual/live-device acceptance are pending. No device was driven, no emulator or screenshot was used, no permissions were granted, and no daily Pixel was touched. The earlier model's unfinished files remain historical, not the current deliverable.

## Preserved baseline and Wall

v209 repaired native Close player / Close media; Ryan reported it working. v211's weather card colour was physically approved and its footer proved Open-Meteo data was arriving. v212 repaired the three zero-height current/hourly/daily columns with MATCH_PARENT, retaining their 3:4:3 widths. All those fixes remain in v213, including Shield INTERNET, the 182dp hero slot and RGB 16/16/16 card fill, 14dp corners and RGB 48/48/48 stroke. v212's separate full visual acceptance was not recorded and is not invented here.

Prior signed v212 source `aef9b05605b2d271d7df9f2698f8431dd42fb97e`, run `35229524975`, artifact `10500159898`, APK SHA-256 `01cd1a53a2fc9b9eedffcc6b6601390ab204af6369dbcdd754a4eb9f1c7a3ff1`.
Wall remains v207 / `com.boop.alpha1`; it was built for shared compatibility only and is not requested for installation. Voice/provider/pitch investigation remains untouched.

The retired root workflow-rule/context files and changed-file allowlists were not restored. Detailed implementation and verification record: `docs/handoffs/2026-09-17-shield-room-panel-v213.md`.
