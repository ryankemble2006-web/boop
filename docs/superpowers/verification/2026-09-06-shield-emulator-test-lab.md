# BOOP Shield emulator test lab — local verification

## Prerequisites

- Android Emulator: 37.1.11.0, gfxstream backend.
- Selected system image: `system-images;android-36;android-tv;x86_64`.
- Existing AVDs observed and preserved: `Pixel_10_Pro_XL_API_36` and
  `Pixel_7_Pro_API_36`.
- Status: Android TV system image installed locally. No BOOP APK installation,
  Android permission/access change, physical Shield interaction, or GPU-policy
  change has occurred.

## Android TV AVD boot

- AVD: `BOOP_Android_TV_API_36` using the 1080p Television profile.
- Graphics configuration: `hw.gpu.enabled=yes`, `hw.gpu.mode=auto`.
- Boot and ADB: passed locally on `emulator-5554`.
- Renderer: hardware-accelerated Android Emulator OpenGL translation through
  the NVIDIA GeForce RTX 3050 Laptop GPU.

## Host graphics environment

- Adapters observed: AMD Radeon(TM) Graphics and NVIDIA GeForce RTX 3050 Laptop
  GPU.
- The emulator renderer identifies the NVIDIA adapter directly, so no Windows
  per-app graphics-preference change was necessary.
- Hyper-V optional-feature state could not be read without an elevated Windows
  administrator token. No virtualization setting was changed.

## BOOP debug smoke test

- Build: `:app:assembleDebug` passed from `shield-overlay/` with the verified
  local Android SDK supplied only to the Gradle process.
- Install/launch: passed on `emulator-5554` only. No physical-device serial was
  used.
- Remote behaviour: Android's overlay-access settings opened as expected, with
  no access automatically granted. D-pad/Select/Back returned control to the
  underlying stock TV launcher.
- Visual overlay placement and live input pass-through are unsupported in this
  stock-AVD run because overlay access was intentionally not granted. This is
  not a BOOP defect claim and is not physical-Shield evidence.

## Evidence boundary

All results in this document are local/automated emulator evidence. Physical
NVIDIA SHIELD validation remains required for Deezer H1, overlay
access/lifecycle, HDR/display, extended stability, and release acceptance.
