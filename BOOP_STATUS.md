# BOOP Launcher Alpha 2 status

## Current

Clean-sheet Launcher Alpha 2 physical-feel baseline is built and signed on `boop-launcher-alpha2`.

Tested app source: `844301bfe264a52b202787e8224fb43452dd3ff6`.
GitHub Actions run: `34077665229` — success.
Signed artifact ID: `10002643933`.
APK SHA-256: `89a5cd50f97dd87e513d86d79bfae71b5f962d86f760190c57527a92fd289b96`.

## CI green

- Package remains `com.boop.launcher`, version `0.2.0` / code `3`.
- Android API 29 minimum, target/compile 36.
- Unit tests pass.
- Android lint passes.
- Release assemble passes.
- Existing permanent BOOP signer verifies with APK Signature Scheme v2.
- Pure-black edge-to-edge Home baseline implemented.
- No permanent clock, At a Glance, search pill or dock/hotseat.
- All Apps local enumeration and contextual local search implemented.
- App pin, persistence, basic move/remove and Home/Back state implemented.

## Not physically green

Ryan has not yet installed/accepted this Alpha 2 baseline on Pixel 10 Pro XL. Do not promote it as the authoritative accepted Launcher or update the main app map yet.

Known incomplete before full Alpha 2 acceptance:
- drawer transition is not yet full Launcher3 direct-finger spring physics;
- widget host flow exists but widget views are not yet rendered/movable/resizable;
- dynamic multi-page workspace behavior is incomplete;
- rotation/process-death and real widget flows have not had current physical checks.

## Next

Install `BOOP-Launcher-Alpha2.apk` on Pixel 10 Pro XL and collect a short physical feel/bug list. Prioritize any blocking launch/gesture issues and true Pixel-like drawer motion before adding deferred launcher furniture or BOOP-specific flourishes.

## Protect

- Preserve `boop-launcher-alpha1` as historical fallback until Alpha 2 is physically accepted.
- Keep package `com.boop.launcher` and the existing BOOP signing identity.
- Do not publish signing keys or private certificates.
- Keep Wall and Shield branches/source independent.
- CI-green is not physical acceptance.
