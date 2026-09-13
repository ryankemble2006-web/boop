# v161 physical acceptance: speed and automatic bidirectional eye colour

Date: 2026-09-13. Repository `ryankemble2006-web/boop`. Owner branch `boop-unified-eye-sync-safe-v159`.

## User result

Following verified installation of the signed v161 APK on Nvidia Shield Android TV and Pixel 7 Pro, Ryan reported:

> both devices, speed change working also colour is automagically changing from device to device, to and from both in both directions.

This records USER-REPORTED PHYSICAL ACCEPTANCE of:
- Animation-speed changes on Shield.
- Animation-speed changes on Pixel 7 / Wall.
- Automatic eye-colour propagation Pixel 7 / Wall -> Shield and Shield -> Pixel 7 / Wall.

The evidence is Ryan observing his devices during joint testing. It is not an inference from CI, a slider number, a connection-ready message or installation success. Do not reopen accepted speed or colour repair from historical recovery notes without a new specific failure report.

## Accepted artifact identity

The report follows the preceding verified installations, not a new device query in this continuation:

- Package/version: `com.boop.alpha1`, `161 / 1.2.161-lab-scale-independent`.
- App/build commit: `0b6ee6f91e05f00138a94ec2c9fd846117020754`.
- Full build: `34770388933`; artifact: `10321956422` / `BOOP-Unified`.
- APK SHA256: `c68b81be9b7d3e10883d2aea52c05dc7b8c187fcd835eaddf73e25e91cb4acd6`.
- ZIP SHA256: `623805381b04c2ce61a8a5bfcb56e663a9a2fc8eddbc1b9eb9b98ed987ccdf3b`.
- Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Both preceding ordinary updates returned Success/exit0, and installed package/version/base-APK hashes matched the GitHub candidate. Detailed evidence remains in `docs/handoffs/2026-09-13-v161-installed-joint-testing.md`. Build/regression details remain in `docs/handoffs/2026-09-13-lab-scale-zero-verification.md` and the earlier speed/colour receipts. Those files retain their historical state; this newer receipt supersedes their awaiting-acceptance text.

## Scope and limits

Speed remains device-local. Ryan confirmed shared colour in both directions, not shared speed. His report does not enumerate each individual animation surface or all four rates, a deliberate offline/reconnect cycle, mid-clip changes, sleep/wake or the device's Android animation-scale setting. No additional scenario-specific physical results are invented. These limits are not a reason to withhold the accepted checkpoint, reopen the repair, or create an autonomous testing backlog.

The approved artwork, shaders, all coded animations, original1x timing, working Wall hue controls, accepted Shield polish and single-face ownership remain protected. No new application change is required to record this successful result.

## Documentation publication and next step

Starting LIVE owning-branch HEAD checked: `734cfa71be100ecee2664240a6c66ddaf8528f16`. LIVE main checked: `24a260b6e7cdd5aed792ccfbb683e8e495eb5f80`. Update the current handoff/status/memory with this acceptance and publish a documentation-only successor. The final documentation commit does not change the APK's build identity above.

No app source, test, build workflow, asset, permission, signer, installation or device setting was changed in this continuation. No local command, device/emulator input, capture, code-test run or new build was performed. Physical Pixel10 remained untouched. No private addresses, serials, credentials, raw dumps or private screenshots are included.

Continue with GitHub development, requested delivery and testing together with Ryan, as recorded in current `main/BOOP_START_HERE.md`. Main needs no new ordinary-progress edit. Speed and live bidirectional colour are accepted; await Ryan's next task or specific new observation. No queued device work or scheduled monitoring exists.
