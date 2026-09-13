# Primary continuation: shared eye colour and animation speed

2026-09-13. Ryan explicitly confirmed this work is primary; old recovery threads are not blockers. GitHub source owner remains `boop-unified-eye-sync-safe-v159`; next app version is 160. The v156 Shield sweep remains accepted.

## Verified colour delivery from the preceding continuation
Signed v159 source `0a4134ebfe8049254378b4706d2ee1df73cd7e87`, full GitHub run `34757337845`, APK SHA256 `80e86119d4771624ff47617373df2cebc03c0454aa47f10a0996a6d507b68353`. Run passed 235 focused Unified and 68 Shield tests plus configured integration/asset/colour checks and permanent signing.
That artifact was installed and hash-readback verified on both laptop emulators, Shield and Pixel 7. Pixel 10 physical phone remains excluded. Emulator checks covered preserved hue 73/288, settings entry, Cancel staying off, local slider changes, TV D-pad hue adjustment and persistence after reopening. Phone Wall awakened/rendered; crash buffer was empty.
Shield actual HA sharing reached ready, published hue 195, then rejoined and restored shared 195 after a disconnected local edit to 289. The shared setting was restored to original 190. This proves Shield-to-HA state persistence and rejoin, NOT two-device delivery. Pixel 7 was locked; no unlock or credential bypass was attempted. End-to-end two-device sharing remains to verify when accessible.

## Current speed implementation awaiting GitHub and runtime gates
Recovered staged GitHub tree now adds device-local 0.5x/1x/1.5x/2x settings, a monotonic delta clock and lifecycle speed listeners for Wall, Now Playing, notifications and embedded Lab. Authored motion, shaders, hue code and masters remain unchanged. Notification hands and eyes share one timeline; sleep-hide follows the remaining scaled duration. Existing Lab slow-review remains separate and no controller double-scaling is used.
The test-first baseline at `3806cd34` failed the two expected missing-speed tests (run `34759118959`). The harness compares every authored clip and state/blend/trigger transitions to v156 at four rates, including exact 1x pose bits. No passing speed test or build is claimed until the new runs complete.

## Boundaries and next
No changes to Android global settings on physical devices, permissions, signing, voices, media transport or native-lyrics branch. Pixel 10 is untouched. GitHub owns source/build/logic tests; laptop emulators and Shield own runtime/visual checks. Preserve canonical visuals and current Wall hue. Follow the existing research plan; primary docs rechecked: Android SystemClock and SharedPreferences, HA input_text state restoration. Build and speed CI must both pass before deployment. Update this handoff with exact outcomes, not old pending statuses.
