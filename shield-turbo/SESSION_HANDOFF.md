# SHIELD TURBO handoff

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.

## Current physical evidence

CLEAN START remains physically accepted on Ryan's real Shield.

- v0.5.7 proved the static full-screen CLEAN START notice is physically visible.
- v0.5.8 measured the accepted cleanup job at `notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`.
- v0.5.10 compact ANALYSE report is physically accepted as a readable one-screen 9sp monospace evidence sheet.
- v0.5.11 Processor Mode Trace physically mapped NVIDIA Processor Mode Optimized to Max performance:
  - `system:nv_power_mode`: `1 -> 0`;
  - downstream CPU/GPU/FRT evidence: `0/0/0/15 -> 5/5/5/20`.

The four `persist.vendor.sys.phs.*` values remain read-only NVIDIA evidence and must never be direct write targets.

## Physical NVIDIA actuator acceptance

v0.5.12 first exposed an `Unexpected ADB stream` transport failure. That was reproduced in `AdbWireStreamLifecycleTest` and fixed narrowly in commit `1b632e09706652d2f2802c6ca4bb28963d9e7685`: only stale `OKAY`, `WRTE`, or `CLSE` packets for older positive stream IDs are ignored. Current, future, and invalid stream IDs still fail closed.

Ryan then physically ran the v0.5.13 one-shot actuator proof on the real Shield. It PASSED:

- baseline Optimized: `mode=1 cpu=0 gpu=0 frt=0 min=15`;
- request Max using only `settings put system nv_power_mode 0`;
- Max verified: `mode=0 cpu=5 gpu=5 frt=5 min=20`;
- restore using only `settings put system nv_power_mode 1`;
- final Optimized verified: `mode=1 cpu=0 gpu=0 frt=0 min=15`;
- `DIRECT VENDOR WRITES • NONE`.

Durable result: `system:nv_power_mode` is a physically accepted stock performance actuator. Mapping is `1=Optimized`, `0=Max performance`. Vendor boost properties remain evidence only.

## Persistent TURBO v1 implementation

Canonical design: `docs/superpowers/specs/2026-09-08-shield-turbo-stock-performance-mode-design.md`.
Implementation plan: `docs/superpowers/plans/2026-09-08-shield-turbo-persistent-stock-performance-mode.md`.

v0.6.0 implements the approved narrow design:

- the only writable performance lever is `system:nv_power_mode`;
- exact pre-TURBO stock processor state is saved before the first performance write and preserved until verified restore succeeds;
- enable writes Max, verifies the full NVIDIA state shape, and rolls back on failure;
- failed restore preserves the baseline and marks recovery required rather than guessing;
- state is persisted as one private encoded snapshot;
- controller operations are serialized process-wide;
- a private foreground thermal watchdog exists only while verified TURBO is active;
- Android thermal status is checked before boot-time reapply;
- Android `SEVERE` or higher restores NORMAL, persists the fallback, and requires manual re-arm;
- a private boot receiver starts reapply only when saved state is `TURBO_VERIFIED` with desired TURBO true;
- the TURBO page has a large remote-first `TURBO MODE: OFF|ON` control plus Processor mode, Thermal state, Watchdog, and Last change readouts;
- first enable explains reboot persistence, stock-controls-only behavior, thermal watchdog, and SEVERE fallback;
- the physically proven one-shot NVIDIA proof remains available beside persistent mode;
- no root, custom kernel, bootloader, boot image, voltage changes, above-stock clocks, arbitrary sysfs writes, or thermal/throttling bypass were added.

## UI lifecycle safety fix

Post-build review found that closing the TURBO page could cancel the panel executor while a performance transaction was still in flight. That could interrupt a write/verify/restore sequence.

TDD receipt:

- RED commit `5603246265d30993177a83be649318d25bfc4887` added `test_closing_panel_never_interrupts_an_inflight_performance_transaction`;
- run `34279811675`, job `102241588162`, failed exactly because `TurboModePanel.close()` still contained `operation?.cancel(true)`;
- GREEN implementation commit `87feccaeba1c2c5fa2044aeeb572fad947aa985c` removed operation cancellation and `shutdownNow()`;
- `close()` now stops UI delivery and performs orderly `worker.shutdown()`, allowing an already-running safety transaction to finish.

Durable rule: UI lifecycle must never interrupt an in-flight performance transaction.

## Physically accepted v0.6.0 checkpoint

**v0.6.0 / versionCode 21**

Exact release source:
`87feccaeba1c2c5fa2044aeeb572fad947aa985c`

Machine verification:

- run `34280026842`, job `102242293277`, conclusion success;
- JVM tests: 101 passed, 0 failures/errors/skips;
- source/API/security contracts: 62 passed;
- Android lint completed successfully with warnings only;
- package `com.boop.shieldturbo`, versionCode 21, versionName 0.6.0, Leanback launchable;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`, DN `CN=BOOP Development, O=BOOP`;
- APK ZIP integrity passed;
- APK SHA-256 `db0ca06c03a51e7985ca11c479b88becfd1471ca8ad5a7218a0710f7348deb43`;
- signed artifact `SHIELD-TURBO`: ID `10077342574`, ZIP SHA-256 `0f63b6ecc36df91731d1f3ab91041142fa1f58ae740a1d2f4cc949f73b88aced`;
- test artifact `SHIELD-TURBO-TESTS`: ID `10077384107`, ZIP SHA-256 `3aa92aad10e4bcd5f0906cfa54fe4fcf38d7cd2699eb87fcecb0bbace539ea4f`;
- emulator API 30 install succeeded;
- cold launch `1541ms`, warm launch `387ms`, process remained alive with PID `2186`;
- no package `FATAL EXCEPTION` was found;
- no screenshot, golden-image, UI hierarchy, or visual acceptance automation ran.

Physical acceptance on the real Shield:

1. Ryan started with NVIDIA Processor Mode Optimized and enabled TURBO from the app. Shield settings switched to Max performance.
2. The app physically displayed `TURBO MODE: ON`, `Processor mode: Max performance verified`, `Thermal state: NONE`, and `Watchdog: ON`.
3. Ryan rebooted the Shield with TURBO armed. Without pressing the TURBO button again, the app returned showing TURBO ON and `Last change: TURBO retained after reboot`. This proves the persisted desired state, boot receiver, foreground watchdog service, thermal pre-check, trusted local ADB path, and retain/reapply flow executed successfully on hardware.
4. Ryan then turned TURBO OFF and confirmed NVIDIA Processor Mode returned to Optimized.

**Persistent TURBO v0.6.0 is physically accepted for its normal enable, reboot persistence/watchdog startup, and manual NORMAL restore path.**

## Thermal fallback boundary

The SEVERE-or-higher watchdog fallback remains machine-tested, not physically heat-tested. Do not intentionally overheat the Shield to validate it. If further evidence is needed, add a safe injected/test path that exercises the same controller transition without thermal stress.

## Existing locks

Preserve the physically accepted CLEAN START current-user force-stop plus verification core, bounded opt-in 30/60/120s max-three boot scheduler, trusted loopback ADB/private key, static notice host, brightness 10-100%, APPS direct launch/remote behavior, compact diagnostic sheet, no-data-deletion rules, and permanent signer.

Never direct-write `persist.vendor.sys.phs.cpufreq.boost`, `gpufreq.boost`, `frt.boost`, or `frt.min`. Never add root, voltage, thermal-disable, above-stock clocks, arbitrary sysfs performance writes, or bootloader/kernel changes without an entirely new approved design and physical evidence boundary.

## Next safe step

Treat v0.6.0 source `87feccaeba1c2c5fa2044aeeb572fad947aa985c` and artifact `10077342574` as the persistent TURBO rollback checkpoint. Any next performance expansion must preserve this exact accepted path and add one independently proven stock control at a time.

`main` remains separate at `4b0ab90abbad9c48dabd25b6a9ea002cdad18375`.