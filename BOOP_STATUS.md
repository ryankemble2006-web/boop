# BOOP Launcher Alpha 2 status

## Current

Launcher Alpha 2 has a physically useful baseline and a new immersive follow-up build.

Ryan physically installed the earlier `0.2.0` / code `3` baseline and confirmed a black Home screen plus functioning app drawer. Physical evidence then showed Android status/navigation bars remained visible and the bottom app row could be clipped by navigation.

Ryan approved immersive Home + drawer.

Current application source: `92c34d47b19e6d2191891e9eb9ffe5a329394cf1`.
GitHub Actions run: `34080350338` — success.
Signed artifact ID: `10003492030`.
APK SHA-256: `1dfcd87412b8308704325db9d1045f08294942e01ff53d62cddd20cf21d8435c`.

## CI / smoke green

- Package `com.boop.launcher`, version `0.2.1` / code `4`.
- Android API 29 minimum, target/compile 36.
- Unit tests pass, including immersive-mode regression checks.
- Android lint passes.
- Release assemble and permanent BOOP signer pass.
- Signed APK installs and survives launch on Android 16 smoke emulator.
- Status and navigation bars are hidden with transient edge-swipe recovery.
- System bars are re-hidden when launcher focus returns.
- All Apps bottom padding now accounts for navigation-bar size and scrolls safely within padding.
- Pure-black Home, no permanent clock/At a Glance/search pill/dock remains the intended visual baseline.

## Physical state

Physically confirmed on the previous build: black Home screen and app drawer work.

Not yet physically confirmed on `0.2.1`: immersive status/nav hiding, transient recovery gesture and corrected final drawer row. Do not mark this specific fix physically green until Ryan installs it.

## Still incomplete

- Drawer motion is not yet full Launcher3 direct-finger/spring physics.
- Widget host flow exists but widget rendering/move/resize is incomplete.
- Dynamic multi-page workspace behavior is incomplete.
- Rotation/process-death and real widget flows still need current physical checks.

## Next

Install the signed `0.2.1` / code `4` APK and verify: uninterrupted black Home, no persistent clock/Wi-Fi/battery/nav buttons, edge-swipe temporary system-bar recovery, and no clipped bottom drawer row. Continue the physical feel/bug list from there.

## Protect

- Preserve `boop-launcher-alpha1` as historical fallback until Alpha 2 is accepted.
- Keep package `com.boop.launcher` and existing BOOP signing identity.
- Do not publish signing keys/private certificates.
- Keep Wall and Shield app lineages independent.
- CI-green, smoke-green and physical acceptance are distinct states.
