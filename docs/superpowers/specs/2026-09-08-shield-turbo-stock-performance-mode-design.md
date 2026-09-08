# SHIELD TURBO stock performance mode design

Date: 2026-09-08
Owning branch: `shield-turbo-v01`
Package: `com.boop.shieldturbo`
Status: design approved in chat; implementation pending user review of this written spec

## Goal

Add a persistent, no-root performance mode for NVIDIA Shield that pushes the device as hard as the stock firmware safely permits for workloads such as Dolphin emulation, while preserving NVIDIA/Android thermal protection and providing exact rollback to the user's previous settings.

This is intentionally **stock-envelope tuning**, not overclocking in the kernel-modification sense.

## User decisions

The approved behavior is:

- TURBO persists across reboot until the user turns it off.
- TURBO includes NVIDIA's own Max Performance processor mode when that control is genuinely exposed and writable on the current Shield firmware.
- A thermal watchdog runs whenever TURBO is active.
- At Android thermal status **SEVERE**, TURBO immediately restores NORMAL, persists NORMAL, and does not automatically turn itself back on.
- A quiet foreground-service notification while TURBO is active is acceptable.
- TURBO should use every **proven** stock performance lever available on the device, but should not blindly enable settings merely because their names sound performance-related.

## Explicit safety boundary

The old blanket "no overclocking" boundary is replaced for this feature by the narrower rule below:

> Stock-envelope performance tuning is allowed. SHIELD TURBO must not request or set a frequency above firmware-exposed limits, modify voltage, bypass or weaken thermal protection, unlock the bootloader, require root, flash or replace a kernel, modify boot images, or disable NVIDIA/Android throttling.

The feature may use the app's existing trusted local ADB transport and `WRITE_SECURE_SETTINGS` access where required, but only for settings/controls that the stock OS exposes and that can be verified by read-back.

## Why Android fixed-performance mode is not the TURBO engine

Android's fixed-performance mode is intended to hold CPU/GPU at a device-specific repeatable operating point. It is not guaranteed to represent maximum dynamic performance and can therefore reduce peak game/emulator performance on some devices.

For v1:

- it may be detected and shown as diagnostic information;
- it is **not** automatically enabled by TURBO;
- it can only become part of TURBO later if real Shield measurements demonstrate that it improves the target workload without defeating the thermal/rollback contract.

## Architecture

### 1. `PerformanceCapabilityProbe`

A read-only capability layer discovers what the current Shield firmware actually exposes.

It should report, without changing anything:

- current NVIDIA processor/performance mode if discoverable;
- whether that mode has a verifiable stock writable control;
- exposed CPU frequency/governor/min/max attributes that are readable;
- exposed GPU/devfreq/governor/min/max attributes that are readable;
- whether any candidate attribute is writable by the existing ADB shell identity;
- Android thermal-status availability;
- current thermal status;
- optional Android fixed-performance support as diagnostic-only information.

Discovery must be bounded to a reviewed allowlist of known settings and kernel/sysfs attributes. There is no recursive `/sys` crawler and no generic "write anything writable" behavior.

A candidate performance lever is usable only when the implementation can define all of:

1. how to read the original value;
2. the stock value TURBO intends to request;
3. how to write it through an approved stock interface;
4. how to read back and verify the requested result;
5. how to restore the original value;
6. how to verify restoration.

If any item is missing, that lever is diagnostic-only.

### 2. `PerformanceProfileController`

This is the transactional apply/restore engine.

Entering TURBO from NORMAL:

1. Probe capabilities.
2. Require a trustworthy Android thermal-status source before allowing persistent TURBO.
3. Capture a complete NORMAL baseline for every control that is about to change.
4. Persist that baseline atomically before the first write.
5. Apply supported stock controls one at a time.
6. Read back each write immediately.
7. If every requested control verifies, persist `TURBO_VERIFIED` and start/retain the watchdog.
8. If any write or read-back fails, restore every already-changed control from the saved baseline, verify the restoration, remain NORMAL, and report the rejected control in plain English.

Returning to NORMAL, whether manual or thermal:

1. Load the saved NORMAL baseline.
2. Restore every changed control to its exact saved value.
3. Verify every restoration.
4. Persist NORMAL only after restoration succeeds or record an explicit recovery-needed state if verification is incomplete.
5. Keep the saved NORMAL baseline intact until every required restoration has been verified. A failed restore must not discard the only known-good original values.
6. Stop the thermal watchdog once NORMAL is verified.

The controller never substitutes guessed "default" values for a saved original value.

### 3. Persistent state model

The persistent state should distinguish at least:

- `NORMAL`
- `ENABLING`
- `TURBO_VERIFIED`
- `RESTORING`
- `RECOVERY_REQUIRED`

The stored NORMAL baseline is created only when transitioning from verified NORMAL into TURBO. It **must not be replaced on reboot while TURBO is already active**.

This prevents reboot-time reapplication from accidentally recording Turbo values as the new normal baseline.

Persistence should include:

- desired/verified mode;
- saved original values keyed by reviewed control identifier;
- the exact set of controls successfully applied;
- last change reason/time;
- last thermal fallback reason/time;
- enough schema/version information to reject stale incompatible snapshots after future app changes.

If state is ambiguous after process death, app update, failed write, or corrupted/incomplete persistence, the safe recovery direction is NORMAL. SHIELD TURBO must not blindly reapply TURBO from an unverified state.

### 4. Boot reapply

A private boot receiver handles persistent TURBO.

On boot:

- if stored state is not `TURBO_VERIFIED`, do not apply performance changes;
- if TURBO was verified before reboot, start the thermal watchdog and re-probe capabilities;
- obtain and verify the **current thermal status before any performance write**;
- if current status is SEVERE or higher, do not reapply TURBO; restore/verify NORMAL from the saved baseline where needed, persist NORMAL, record thermal fallback, and notify the user;
- verify that the saved control identifiers are still supported on the current firmware;
- reapply only the previously approved stock controls;
- never overwrite the stored NORMAL baseline;
- verify every reapplication by read-back;
- if capability changed or any reapply step fails, restore NORMAL where possible, persist NORMAL or `RECOVERY_REQUIRED`, and notify the user.

Boot reapply must not request a new ADB RSA approval. It uses trusted local ADB only, matching CLEAN START's existing background trust boundary.

### 5. Thermal watchdog foreground service

The watchdog exists only while TURBO is active.

Responsibilities:

- run as a small foreground service with a quiet persistent notification;
- register Android's thermal-status listener on supported API levels;
- obtain the current thermal status immediately when starting, rather than waiting for a future callback;
- show the current thermal state to the UI through stored/shared state;
- on status below SEVERE, take no performance-control action;
- on **SEVERE or higher**, invoke the same transactional NORMAL restore path used by the manual button;
- after successful thermal fallback, persist NORMAL, record the reason, post a plain-English notification, and stop the watchdog;
- never auto-reenable TURBO after thermal fallback.

If Android thermal status is unavailable or cannot be trusted on the current device/API, persistent TURBO is unavailable. The UI may still show capability diagnostics, but it must not claim active thermal protection.

The watchdog does not disable, alter, mask, or compete with Android/NVIDIA thermal throttling. Those protections remain authoritative underneath SHIELD TURBO.

### 6. Stock performance controls

#### NVIDIA Max Performance

The first preferred performance control is NVIDIA's own processor/performance mode, because that is the intended stock maximum-performance policy.

Implementation must discover the actual supported read/write interface on the target Shield firmware before adding a write. No guessed setting key or undocumented value is accepted merely from naming similarity.

Once a valid interface is proven:

- save the user's original processor mode;
- request Max Performance;
- read back the result;
- include it in the transactional rollback set.

#### CPU/GPU governors and frequency bounds

Additional stock controls may be used only when the existing ADB shell identity can access a reviewed firmware-exposed interface and the requested value is within the firmware's own exposed supported range.

The controller may request a more performance-oriented governor or the firmware's own exposed maximum/minimum bounds only when all apply/verify/restore requirements are satisfied.

It may **not**:

- invent a frequency;
- write above a reported stock maximum;
- modify voltage tables;
- replace thermal governors;
- disable cooling/throttling controls;
- use arbitrary sysfs writes outside the reviewed allowlist.

The first physical capability build should prefer probing over writing. New writable controls should be enabled one at a time with real-Shield evidence.

## UI

The existing TURBO page remains the home for this feature.

Add a large remote-first state control near the top:

- `TURBO MODE: OFF`
- `TURBO MODE: ON`

First enable shows a concise confirmation explaining:

- TURBO persists across reboot;
- it uses stock performance controls only;
- a watchdog remains active while TURBO is on;
- SEVERE thermal status automatically restores NORMAL and leaves it off.

Under the switch, show four chunky readouts:

- `Processor mode: Max performance | <original/current> | Unavailable`
- `Thermal state: Normal | Light | Moderate | Severe | ...`
- `Watchdog: Active | Off | Unavailable`
- `Last change: <plain-English reason>`

The notification while active should be compact, for example:

`SHIELD TURBO • Performance mode active`

Thermal fallback notification should be explicit, for example:

`SHIELD TURBO switched to NORMAL because the Shield reached SEVERE thermal status.`

No v1 temperature slider, thermal-threshold selector, "ignore heat" control, voltage control, clock-frequency input, or hidden expert bypass.

## Error and recovery behavior

Plain-English outcomes are required.

Examples:

- `TURBO NOT APPLIED • Shield rejected processor mode change`
- `TURBO NOT APPLIED • Thermal protection is not exposed on this firmware`
- `NORMAL RESTORED • Original processor settings verified`
- `TURBO OFF • Shield reached SEVERE thermal status`
- `CHECK PERFORMANCE SETTINGS • Restore could not be fully verified`

Any failed enable operation rolls back already-applied changes before returning control to the user.

A failed restore never claims NORMAL is verified. It records `RECOVERY_REQUIRED`, preserves the saved NORMAL baseline, keeps the user informed, and must not automatically reapply TURBO on reboot.

## Interaction with existing SHIELD TURBO features

This subsystem is independent of CLEAN START.

Do not change:

- CLEAN START target selection, trusted ADB behavior, timing, static notice, or force-stop/read-back mechanism;
- brightness overlay behavior;
- APPS direct launch behavior;
- animation undo behavior;
- existing signer/package identity.

The thermal watchdog is not a general resident cleaner and must not acquire unrelated responsibilities.

## Testing strategy

Implementation follows TDD.

### Pure/unit tests

Cover:

- state-machine transitions;
- baseline is captured once and survives reboot reapply;
- no write occurs before baseline persistence succeeds;
- partial apply failure restores earlier changes;
- read-back mismatch is treated as failure;
- NORMAL restores exact saved values;
- failed restore preserves the saved NORMAL baseline;
- boot checks current thermal state before any reapply write;
- SEVERE or higher triggers one-way fallback to NORMAL;
- lower thermal states do not trigger fallback;
- thermal fallback persists NORMAL and prevents reboot re-enable;
- ambiguous/corrupt state prefers NORMAL/recovery, never TURBO;
- fixed-performance mode remains excluded from automatic TURBO v1;
- unsupported/unapproved sysfs attributes are never writable candidates.

### Source/security contracts

Require:

- no `su`/root execution path;
- no bootloader/kernel/boot-image operation;
- no voltage writes;
- no thermal-disable/throttle-disable command;
- only allowlisted performance controls can be written;
- boot path uses trusted ADB only and cannot trigger fresh approval;
- watchdog exists only for active TURBO;
- CLEAN START implementation is unchanged by this feature.

### Machine verification

Run existing JVM/source/lint/signing/package/archive gates and nonvisual launch/crash smoke.

Machine tests must not claim visual quality, thermal effectiveness, Dolphin performance gain, or physical safety.

### Physical Shield rollout

Physical testing is staged:

**Stage 1: capability build**
- read-only display of discovered NVIDIA mode, CPU/GPU controls, writability, and Android thermal status;
- no performance writes beyond already accepted app behavior.

**Stage 2: NVIDIA mode**
- enable only the proven NVIDIA Max Performance control with transaction/undo/read-back;
- verify physical behavior and rollback.

**Stage 3: extra stock controls**
- add one additional proven stock control at a time only if Stage 1 shows it exists and Stage 2 remains stable.

**Stage 4: persistence/watchdog**
- verify boot reapply retains the original NORMAL baseline;
- deliberately exercise manual NORMAL restore;
- thermal fallback logic should be tested with a safe mocked/injected status path in code, not by intentionally overheating the Shield.

Performance success should ultimately be judged by real workload measurements such as Dolphin frame pacing/FPS and by absence of unwanted thermal fallback during normal use, not by synthetic "Turbo scores".

## Definition of done

The feature is complete only when:

- TURBO can be enabled and disabled entirely with the Shield remote;
- all changed controls are stock firmware controls and read back correctly;
- the exact pre-Turbo NORMAL state is restored on manual disable;
- TURBO persists across reboot without overwriting the original baseline;
- watchdog runs only while TURBO is active;
- boot reapply checks thermal status before any performance write;
- SEVERE or higher thermal status restores NORMAL and prevents automatic re-enable;
- failed/ambiguous state prefers NORMAL or explicit recovery while preserving known-good baseline data;
- no root, kernel, bootloader, voltage, over-limit clock, or thermal-bypass path exists;
- existing CLEAN START and brightness behavior remain intact;
- permanent BOOP signing identity remains unchanged;
- CI is green;
- Ryan physically accepts behavior on the real Shield.

## Deferred ideas

Not part of v1:

- per-app automatic TURBO profiles;
- auto-launch Dolphin with TURBO;
- automatic NORMAL restore when Dolphin exits;
- user-adjustable thermal thresholds;
- custom kernel/root overclocking;
- Android fixed-performance mode as an automatic performance lever;
- benchmark scoring or fake "RAM boost" metrics.
