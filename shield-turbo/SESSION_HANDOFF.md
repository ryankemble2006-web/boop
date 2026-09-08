# SHIELD TURBO handoff

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.

## Current physical evidence

CLEAN START remains physically accepted on Ryan's real Shield.

- v0.5.7 proved the brightness-style transparent full-screen CLEAN START notice is physically visible.
- v0.5.8 measured the accepted cleanup job at `notice=62ms • adbReady=113ms • resumed=56ms • stops=332ms • slowest=com.fork2.app:268ms • total=573ms`.
- v0.5.10 compact ANALYSE report is physically accepted. Ryan's Shield screenshot showed the full 9sp monospace report in one readable frame.
- The v0.5.10 real-Shield performance probe established trusted local ADB (`ADB TURBO`), Android thermal status support, no reviewed CPU/GPU stock sysfs controls, no allowlisted performance-write path, and no Android fixed-performance command.
- v0.5.11 Processor Mode Trace is now physically accepted. Ryan captured Optimized, manually changed NVIDIA Processor Mode to Max performance, then captured Max. The real before/after diff showed exactly:
  - `system:nv_power_mode`: `1 -> 0`;
  - `property:persist.vendor.sys.phs.cpufreq.boost`: `0 -> 5`;
  - `property:persist.vendor.sys.phs.gpufreq.boost`: `0 -> 5`;
  - `property:persist.vendor.sys.phs.frt.boost`: `0 -> 5`;
  - `property:persist.vendor.sys.phs.frt.min`: `15 -> 20`.

Interpretation locked from physical evidence:
- `system:nv_power_mode` is the leading user-facing actuator candidate for SHIELD Processor Mode on this firmware;
- observed GUI mapping is `1 = Optimized`, `0 = Max performance`;
- the four `persist.vendor.sys.phs.*` properties are downstream NVIDIA effects and are **not** direct write targets;
- the trace proves state meaning, but the app still has not proven it can safely write/restore `nv_power_mode` and trigger NVIDIA's dependent changes.

## Current machine-verified physical-test candidate

**v0.5.11 / versionCode 18**, exact release source:
`8ba8402fdcc970adddcb1acc97b758f5fce76db4`

Final release workflow:
- run `34261526060`;
- job `102180473668`;
- conclusion **success**;
- JVM tests: **82 passed**, 0 failures/errors/skips;
- source/API/security contracts: all passed, including processor-trace no-write/no-resident-component guards;
- lint: **0 errors / 26 warnings**;
- package `com.boop.shieldturbo`, versionCode 18, versionName 0.5.11;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- signer DN `CN=BOOP Development,O=BOOP`;
- APK ZIP integrity passed;
- APK SHA-256 `8b452d1acb6210b6af0c400500edd609026d166a74b71e89b64a27baf180dd6b`;
- APK size `2391774` bytes;
- signed artifact `SHIELD-TURBO`: ID `10070181954`, ZIP `788607` bytes, artifact SHA-256 `0fbf8aafd5a236463babb6ff90e060e72381eec37ac4a1a560eadb4ba2094f46`;
- test artifact `SHIELD-TURBO-TESTS`: ID `10070244960`, ZIP `102653` bytes, artifact SHA-256 `316d4182c68bffe0b995eafc3d84d067a942c51a7121cfeb194f4585c8c57bda`;
- nonvisual emulator install/launch smoke passed through completion;
- no screenshot/golden/layout/UI-hierarchy visual automation ran.

v0.5.11 is machine green and its **read-only Processor Mode Trace is physically accepted**.

## v0.5.11 Processor Mode Trace

Purpose: discover the real no-root interface behind NVIDIA SHIELD `Settings > System > Processor mode` before performance writes.

Trace reads exactly:
- `settings list global`;
- `settings list secure`;
- `settings list system`;
- `getprop`.

The Optimized baseline is stored only in SHIELD TURBO's private `SharedPreferences` under `processor_mode_trace` / `optimized_snapshot`. The trace does not issue `settings put/delete`, `setprop`, `cmd power set`, sysfs writes, fixed-performance enable commands, or any other performance mutation. It adds no service, receiver, foreground permission, watchdog, or boot component. It does not touch CLEAN START or brightness.

TDD receipts:
- first RED `45fe090d48da577c96e10bb7c6bb5c83b46e24f5` required the parser/diff model;
- model/policy/capture/codec/report slices followed through `0d1d3f549fa1f42fd074c481f5398122052cfc64` with intended RED→GREEN gates;
- final integration RED `d4b9b469a1e4c7a13a5b3b4fc6aea6be020fc494` had 82 JVM tests passing and failed only because the trusted probe/store/UI integration was intentionally absent;
- integration GREEN source `7babe9f8493c8a766ed9a1bc1b3cae8e838dc908` cleared JVM tests, source contracts, and lint;
- release stamp `8ba8402fdcc970adddcb1acc97b758f5fce76db4` changed only version assertions after that green tree.

## Approved persistent stock TURBO design

Canonical design:
`docs/superpowers/specs/2026-09-08-shield-turbo-stock-performance-mode-design.md`

Locked behavior once a real writable stock lever is proven:
- TURBO persists across reboot until manually disabled or thermally auto-disabled;
- NVIDIA Max Performance is included only when a genuine interface with read-back is proven on the target firmware;
- any additional CPU/GPU/governor controls must be stock-exposed, reviewed/allowlisted, inside firmware limits, and fully reversible;
- exact pre-Turbo NORMAL values are saved before writes and preserved until restore is verified;
- a foreground thermal watchdog exists only while TURBO is active;
- thermal state is checked before boot-time reapply;
- Android thermal status **SEVERE or higher** immediately restores NORMAL and requires manual re-arm;
- no root, custom kernel, bootloader unlock, voltage modification, above-stock clocks, or thermal/throttling bypass.

**No persistent TURBO write exists yet.** The next build is a bounded actuator proof only.

## Existing locks

Preserve unless Ryan explicitly changes the relevant feature:
- CLEAN START current-user `am force-stop` + process/stopped/enabled read-back verification core;
- target safety exclusions for BOOP/Android/NVIDIA/Google-core/system/updated-system packages;
- AUTO CLEAN START opt-in, bounded roughly 30/60/120s, maximum three attempts, no periodic cleaner;
- boot cleanup uses only already-trusted loopback ADB and cannot request fresh RSA approval;
- ADB key remains private in `noBackupFilesDir`;
- no `pm clear`, uninstall, cache/login/data deletion, broad kill-all or fake RAM scores;
- static CLEAN START notice remains physically locked;
- brightness 10–100% behavior remains physically proven and untouched;
- APPS direct launch, labels, Cancel/Back and remote-first navigation remain;
- compact ANALYSE report stays full-screen black, 9sp monospace, tight, screenshot-friendly, Back closes it;
- Display & Sound and Accessibility remain parked.

## Next safe step

Build a one-shot reversible `nv_power_mode` actuator proof using trusted local ADB:
1. require/read the expected Optimized baseline (`nv_power_mode=1` plus downstream `0/0/0/15`);
2. write only `settings put system nv_power_mode 0`;
3. read back `nv_power_mode=0` and verify NVIDIA itself changes downstream state to `5/5/5/20`;
4. restore only `settings put system nv_power_mode 1`;
5. read back and verify `1` plus downstream `0/0/0/15`;
6. on any mismatch, prefer restore-to-Optimized/recovery, keep exact diagnostics, and do not write vendor properties.

Only after that physical proof succeeds may `nv_power_mode` enter the persistent TURBO allowlist.

`main` remains separate and was last verified at `4b0ab90abbad9c48dabd25b6a9ea002cdad18375` during this feature cycle.
