# BOOP durable project memory

Updated 2026-09-17.

The consumer apps remain split shells around shared BOOP code: Wall `com.boop.alpha1` on v207; Shield `com.boop.shieldoverlay` now on v213. The owning app branch remains `boop-wall-shield-split-v207`.

## Accepted room-panel design and implementation

Ryan wants the entire empty lower Shield Home area to be useful, not just a small item beside BOOP. The native smart-home panel fills that area under favourite captions. It expands across BOOP's lower-right space when he is absent and contracts when he returns, without moving the hero, favourites or his character rig. Surfaces use BOOP charcoal with cyan focus. The existing **Set this device room** selection is the only room source; no duplicate picker, room keys or pairing flow.

**Home settings > Smart home panel: ON / OFF** is persisted, default ON. OFF hides the panel and stops its HA work. D-pad Down from favourites enters devices; Left/Right navigate and stop at row ends; Up restores the previous favourite. Live state updates retain tile identity and focus. Existing HA physical lights, switches/smart plugs and fans are shown through the existing room/device filtering. Sensor values, camera views and thermostat detail controls remain future presentation work rather than fabricated buttons.

After multiple interrupted attempts Ryan requested a fresh autonomous implementation and a signed APK link while he was away. v213 starts directly from v212 owner `2ab0db655368089b19f9c705fba2cd404a1cd4e7`. The earlier `boop-shield-home-panel-v211` implementation and orphan trees were not merged. The new state machine is isolated from legacy HomeDashboardController, reuses the HA transport/repository and pairing, gates stale room/actions, rechecks membership before a command and does not invent success before confirmation. It stops subscriptions, connections and retry timers off Home or when disabled.

## Latest verified artifact

Signed source `7cb211b2a4f2b0307b500cc7ec718effa609ffd5`; feature commit `cfcb627348c5fde2bc4be86553f8a9648192a51b`.
Successful run `35244156761`, job `105279913277`; artifact `10506423552`, `BOOP-Shield-v213-Wall-v207-Signed`.
Deliver `BOOP-Shield-v213.apk`, SHA-256 `cb21540979161b31ebebd756fbfea40ab1217ed8781ca8073094059c2286f783`.
Permanent certificate `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde` unchanged. Focused/controller/numerical tests, inherited checks, HA registry tests, materialized integration, both app builds and packaged identity/native/art checks passed; downloaded ZIP/APK were independently verified. No build or re-sign outside GitHub. A first-run CI signer-ordering error was corrected without removing checks.

Ryan owns APK drag-and-drop installation through Shield scrcpy and all physical/visual acceptance. v213 has not been installed or tested against his live devices by this session. No RDC, device driving, emulator, screenshots, permission changes or daily Pixel access. Wall remains v207, built only for compatibility and not requested for installation.

## Preserved history

v209 fixed native Close player / Close media identity after the split; Ryan reported it working. v210 introduced idle weather in the 182dp Now Playing slot. v211 added missing Shield INTERNET and matched the weather card to Now Playing: RGB 16/16/16, 14dp corners, RGB 48/48/48 1dp stroke. Ryan approved that colour; his footer data proved the Open-Meteo path worked. The three main columns were still blank because their heights were zero.

v212 changed those current/hourly/daily columns to MATCH_PARENT while preserving their 3:4:3 widths. No new weather setup is needed. Keyless Open-Meteo, 30-minute refresh/cache, non-focusable weather, media priority and parked favourites are preserved in v213. Full v212 visual acceptance was not separately recorded. Prior v212 source `aef9b05605b2d271d7df9f2698f8431dd42fb97e`, run `35229524975`, artifact `10500159898`, APK hash `01cd1a53a2fc9b9eedffcc6b6601390ab204af6369dbcdd754a4eb9f1c7a3ff1` remain historical receipts.

The root AGENTS/START_HERE/CONTEXT/RULES/BUILD_ON_GITHUB experiment and changed-file allowlists were retired on 2026-09-17 and were not restored. Functional regression, package and permanent-signer/integrity checks remain. Dated docs are historical context, not automatic instructions to roll back current source.
