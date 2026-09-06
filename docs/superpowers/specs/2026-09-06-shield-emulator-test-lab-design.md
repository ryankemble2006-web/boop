# BOOP Shield emulator test lab design

## Purpose

Provide a quick Android TV development target for BOOP Shield while retaining
the physical NVIDIA SHIELD as the only authority for Shield-specific behaviour
and release acceptance.

## Decision

Use a standard Android TV AVD at 1080p with hardware graphics rendering on the
host NVIDIA GPU. The AVD is a development instrument, not a virtual NVIDIA
SHIELD and not an acceptance substitute.

The host emulator cannot receive exclusive GPU passthrough on Windows. Its
supported accelerated path is host GPU rendering through the Android Emulator's
graphics backend. Configuration must prefer the NVIDIA GPU where Windows and
the NVIDIA driver expose that choice, but must not install drivers, alter global
GPU policy, or change security/virtualization settings without separate consent.

## Test boundaries

The AVD covers rapid, repeatable checks:

- D-pad navigation, focus order, and Back/Home behaviour.
- TV layout at 1920x1080 and basic density/overscan inspection.
- BOOP overlay startup, visual placement, and ordinary input pass-through where
  Android's stock TV environment supports it.
- Debug APK install, launch, and log collection.

The physical NVIDIA SHIELD remains required for:

- NVIDIA launcher and firmware-specific behaviour.
- Deezer media/notification listener behaviour and H1 play/pause puppetry.
- Overlay access and service lifecycle on the real device.
- HDMI/HDR/display-mode effects, performance, long-running stability, and
  final remote behaviour.

## Components and data flow

1. Android SDK's Emulator and a system-image-compatible Android TV AVD run BOOP
   debug APKs.
2. The Windows graphics preference routes the emulator process to the discrete
   NVIDIA GPU when supported; the emulator reports its active renderer for
   verification.
3. A local ADB target installs and launches debug builds.
4. The physical SHIELD receives only deliberately selected builds and completes
   the short acceptance checklist before any claim of physical verification.

No emulator configuration changes the BOOP app, its permissions, or its
protected overlay boundaries: no microphone, no focus capture, no touch
capture, and no new Home Assistant socket.

## Error handling and verification

- If an Android TV image or emulator component is absent, report the exact
  missing package and request approval before downloading it.
- If Windows denies access to SDK or GPU settings, request only the scoped
  permission needed to inspect or configure the relevant setting.
- If the emulator falls back to software rendering, label it unaccelerated and
  do not claim NVIDIA use.
- Verify the AVD's resolution/device profile, boot success, GPU renderer, ADB
  connection, and BOOP debug APK launch.
- Record emulator checks as local/automated evidence only. Record physical
  SHIELD checks separately and never promote one into the other.

## Out of scope

- GPU passthrough, virtualized NVIDIA SHIELD firmware, and spoofing a physical
  Shield.
- Installing applications, granting Android permissions, enabling notification
  access, or modifying BOOP source without a specific follow-up request.
- Replacing the physical Shield's existing verified H1 acceptance evidence.
