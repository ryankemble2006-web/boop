# Active continuity

Shared eye colour first, then speed. GitHub feature owner `boop-unified-eye-sync-safe-v159`. Accepted v156 source is preserved independently; its Shield sweep, favourites and artist navigation are already signed off.

Recovery snapshots: `wip/boop-colour-v158-recovery@484d292f` and `wip/boop-colour-v159-recovery@7ea7d26d`, not releases. The runtime was not actually truncated at recovered `04f7c10c`; startup/settings wiring was missing. Recovery tests `9e452b43` were retained by concurrent implementation `dcebdedd`; do not overwrite it with the superseded unreferenced `c5016ef7` implementation.

Signed v159 source `0a4134ebfe8049254378b4706d2ee1df73cd7e87`, GitHub full build `34757337845`, artifact `10317198102`. APK SHA-256 `80e86119d4771624ff47617373df2cebc03c0454aa47f10a0996a6d507b68353`. Existing permanent signer retained and locally verified. All full-build gates succeeded; report records 235 Unified and 68 Shield focused tests without failures.

The recovery window installed/verified v159 on laptop emulators only: emulator-5570 Pixel_10_Pro_XL_API_36 and emulator-5572 BOOP_Android_TV_API_36. Migration fixtures 73 and 288 survived the upgrade; cold launch into existing voice settings succeeded. Those are deliberate test colours, not user preferences. Private rollback/artifact receipts: `%TEMP%/boop-colour-v159-checks`. No physical-device install or permission change by that window.

Use existing `boop_eyes/hue_degrees`, 0..359/default 190. Keep original slider, shaders, master, EyeMotion, EyeCatalogue and sign choreography intact. HA sharing is opt-in and authenticated, never trusted-LAN-only UDP. Never push startup defaults over another device's colour. Device-local animation speed must preserve 1x exactly; it is not implemented yet.

Do not equate source contracts/build success with real HA or visual acceptance. Appearance UI interaction, real two-device sharing and Android reconnect/server-change gates remain. Coordinate one emulator test owner: concurrent branch changes occurred and an unexpected phone foreground transition needs attribution. A final read/log request was blocked before execution; no safety bypass was attempted. GitHub reads/writes continued to work.

All source/build/signing work stays on GitHub. Laptop emulators precede authorized physical Shield/Pixel 7 checks. Pixel 10 physical deployment is excluded. Native lyrics remains separately owned. Read SESSION_HANDOFF.md and the approved colour/speed plan before continuing.
