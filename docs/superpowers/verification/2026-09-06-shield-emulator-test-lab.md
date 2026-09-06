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

## Evidence boundary

All results in this document are local/automated emulator evidence. Physical
NVIDIA SHIELD validation remains required for Deezer H1, overlay
access/lifecycle, HDR/display, extended stability, and release acceptance.
