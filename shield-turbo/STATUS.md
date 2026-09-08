# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Physical state

CLEAN START remains physically accepted:
- v0.5.7 full-screen static notice visible on the real Shield;
- v0.5.8 job timing `notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`.

v0.5.10 compact ANALYSE presentation is physically accepted. The real-Shield screenshot fit the complete report in one frame at 9sp monospace and remained readable.

Physical Stage-1 performance evidence from v0.5.10:
- trusted local ADB works, tier `ADB TURBO`;
- Android thermal status is exposed;
- reviewed CPU/GPU stock sysfs controls are not exposed;
- no allowlisted performance-write path is exposed;
- Android fixed-performance command is not exposed;
- generic power-key clues are not evidence of NVIDIA Processor Mode control.

## Current candidate

**v0.5.11 / code 18**, exact release source `8ba8402fdcc970adddcb1acc97b758f5fce76db4`.

Release run `34261526060`, job `102180473668`, conclusion **success**:
- **82 JVM tests passed**, 0 failures/errors/skips;
- all source/API/security contracts passed, including processor-trace read-only guards;
- lint **0 errors / 26 warnings**;
- permanent signer/package/version/archive checks passed;
- nonvisual emulator install/launch/no-fatal smoke passed;
- APK SHA-256 `8b452d1acb6210b6af0c400500edd609026d166a74b71e89b64a27baf180dd6b`;
- APK size `2391774` bytes;
- `SHIELD-TURBO` artifact ID `10070181954`, artifact SHA-256 `0fbf8aafd5a236463babb6ff90e060e72381eec37ac4a1a560eadb4ba2094f46`;
- `SHIELD-TURBO-TESTS` artifact ID `10070244960`, artifact SHA-256 `316d4182c68bffe0b995eafc3d84d067a942c51a7121cfeb194f4585c8c57bda`.

Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`, DN `CN=BOOP Development,O=BOOP`.

v0.5.11 is machine green. **Processor Mode Trace physical acceptance is pending.**

## Processor Mode Trace

New `PROCESSOR MODE TRACE` flow on TURBO:
1. SHIELD Processor mode = Optimized.
2. `CAPTURE OPTIMIZED` saves a private local baseline.
3. Ryan manually changes SHIELD Processor mode = Max performance.
4. `CAPTURE MAX` reads current state and shows the compact before/after diff.

Read sources only: `settings list global`, `settings list secure`, `settings list system`, `getprop`.

No Shield setting/property/performance value is written. The only mutation is SHIELD TURBO's own private baseline storage. No service, receiver, foreground permission, watchdog or boot component was added. CLEAN START and brightness are untouched.

## Approved persistent stock TURBO design

Written spec:
`docs/superpowers/specs/2026-09-08-shield-turbo-stock-performance-mode-design.md`

Locked future behavior after a genuine writable stock lever is proven:
- persistent across reboot;
- NVIDIA Max Performance only with verified state/read-back/restore;
- additional stock controls only when allowlisted, within firmware limits, reversible and verified;
- foreground thermal watchdog only while active;
- boot-time thermal check before reapply;
- auto-restore NORMAL at SEVERE or higher and stay NORMAL until manual re-arm;
- exact original NORMAL snapshot preserved through reboot and failed restore;
- no root, custom kernel, bootloader unlock, voltage modification, above-stock clocks or thermal bypass.

**No performance write is implemented yet.**

## Locked existing behavior

Freeze the physically proven CLEAN START overlay/core/timing path, trusted loopback ADB, target exclusions, bounded 30/60/120s max-three scheduler, brightness 10–100%, APPS direct launch/remote navigation, and v0.5.10 compact ANALYSE layout. Display & Sound and Accessibility remain parked.

## Next step

Physical v0.5.11 test: capture Optimized, manually switch NVIDIA Processor mode to Max performance, capture Max, and return the compact diff screenshot. No performance write until that evidence identifies a real interface with unambiguous read-back and restore semantics.
