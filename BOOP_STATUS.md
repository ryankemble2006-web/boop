# BOOP Launcher Alpha 2 status

## Current

Launcher Alpha 2 now has a physically confirmed pure-black fullscreen HOME and swipe-up app drawer. Ryan confirmed the `0.2.2` fullscreen fix removed the persistent Pixel status-bar clock.

A signed `0.2.3` / code `6` follow-up adds the requested inverse navigation gesture: swipe down from the top of All Apps to return HOME.

Current application source: `5b18bb983de284a7773f30cc1a5897a7cb002c35`.
GitHub Actions run: `34082550615`.
Signed artifact ID: `10004259850`.
APK SHA-256: `8fc6c4b212d3fca7cddd98ec420724b8f38c7bb0cf79afdf57fc325d3c6ec05e`.

## Signed / ready for physical test

- Package `com.boop.launcher`, version `0.2.3` / code `6`.
- Existing permanent BOOP signing identity unchanged.
- Normal signer workflow completed its compile/lint gate and uploaded the signed APK.
- Downward gesture from the top of All Apps returns to HOME using the existing close animation.
- A downward gesture while the list is already scrolled remains available for normal list navigation.

## Physical state

Physically confirmed:
- pure-black HOME;
- swipe-up app drawer;
- stronger fullscreen suppression removes the persistent Pixel clock/status bar.

Awaiting Ryan's physical check:
- swipe-down from the top of All Apps closes the drawer naturally.

## Still incomplete

- Drawer motion is not yet full Launcher3 direct-finger/spring physics.
- Widget host flow exists but widget rendering/move/resize is incomplete.
- Dynamic multi-page workspace behavior is incomplete.
- Rotation/process-death and real widget flows still need current physical checks.

## Next

Install `0.2.3` / code `6`, swipe up into All Apps, then swipe down from the top to return HOME. Record the physical feel rather than spending time on emulator animation validation.

## Protect

- Preserve `boop-launcher-alpha1` as historical fallback until Alpha 2 is accepted.
- Keep package `com.boop.launcher` and existing BOOP signing identity.
- Do not publish signing keys/private certificates.
- Keep Wall and Shield app lineages independent.
- Signed and physically accepted are separate states.
