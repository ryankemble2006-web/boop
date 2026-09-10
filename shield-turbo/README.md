# SHIELD TURBO

Remote-first NVIDIA Shield utility for read-only diagnostics, reversible startup cleanup, stock processor-mode control, picture brightness, local ADB setup, and safe power tools.

## Current development state

- Package: `com.boop.shieldturbo`
- Branch: `shield-turbo-v01`
- v0.6.0 / code 21 remains the physically accepted persistent TURBO rollback point.
- v0.6.1 / code 22 is machine-green for the one-time SILENT STARTUP note and silent CLEAN START boot behaviour.
- v0.6.2 / code 23 adds the read-only TURBO+ HEADROOM physical discovery screen and is pending full CI/physical acceptance.

## TURBO

Persistent TURBO uses NVIDIA's physically proven stock Processor Mode actuator only: `system:nv_power_mode`, with `1=Optimized` and `0=Max performance`. The downstream NVIDIA CPU/GPU/FRT properties are evidence only and are never direct write targets. The app saves the exact pre-TURBO setting, verifies Max after enabling, restores the saved stock setting on disable, and uses an Android thermal watchdog to fall back to NORMAL at SEVERE or higher.

No root, custom kernel, bootloader, boot image, voltage changes, above-stock clocks, arbitrary sysfs writes, or thermal-limit bypass are part of TURBO.

## TURBO+ HEADROOM

TURBO+ HEADROOM is deliberately read-only. From the TURBO page, move Right from the normal TURBO button to `TURBO+ HEADROOM TEST` and press OK once. It opens a full-screen black result designed to be photographed from the TV.

It checks readable evidence for:

- CPU online state, current/max frequency and governor;
- GPU devfreq current/max/min frequency, governor and available frequencies;
- memory/EMC clock or devfreq clues;
- thermal zones, cooling devices and fan/thermal/cooling properties;
- `nv_power_mode` plus any other surfaced NVIDIA/processor/performance/fan/power/EMC setting names.

Each section says `FOUND` or `BLOCKED`. ADB/setup failures use a large `TURBO+ • STOP` page with `ADB NOT READY` and the recovery path. The footer is `PHOTOGRAPH THIS • BACK TO CLOSE`.

Discovery does not promote a newly visible setting into a Turbo write target. Any candidate must be separately proven with save, change, read-back and restore before it can join persistent TURBO.

## CLEAN START

Automatic CLEAN START is opt-in, bounded and silent after reboot. The old boot banner/overlay was removed. A one-time first-real-launch note explains that saved startup work is silent and may cause a brief apparent startup pause.

CLEAN START only force-stops user-selected eligible apps and verifies the result. It does not uninstall apps, clear data/logins, chase arbitrary free-RAM scores, or touch NVIDIA/BOOP core packages.

## Evidence rules

Machine-green, signed and physically accepted are separate states. Physical Shield evidence wins over generic Tegra assumptions. See `SESSION_HANDOFF.md`, `STATUS.md`, and `MEMORY.md` for exact receipts and current boundaries.
