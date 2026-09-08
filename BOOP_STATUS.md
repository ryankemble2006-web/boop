# BOOP unified status

Updated 2026-09-08. Canonical branch `boop-unified`; package/permanent signer unchanged.

Current code `4044ee55b5a39e2a220ee897de0393b796c29a5f`, v44 / `1.1.1-unified-eyes-wake-home`. Run `34192698906` is in progress. Initial non-visual contracts and materialization passed. No finished APK or physical acceptance is claimed yet.

Includes accumulated wake-model/fallback repairs, modern Room -> Devices Home, configuration-only Settings, Favourites removal, robust HA category filtering, stable D-pad focus, cached iris-only hue, and the NEW shared canonical phone eye geometry/blink on Shield. Concurrent `dcc7acf` room/iris work was preserved as parent.

The unified delivery workflow has no visual, aesthetic-source, emulator launch/install or real-device acceptance tests. Focused functional tests, compilation, Launcher lint, signer/package/archive checks remain. Documentation-only updates do not trigger another APK.

Local shared-eye adapter syntax and exact-source patch anchors passed; Android compilation/tests/signing require the current CI result. Visual eye/iris/blink, focus/room controls, permission screens and acoustic wake need Ryan's hardware tests.

Last delivered `6cd9c67`, run `34125882296`, artifact `10020439707`, is not an accepted wake-name release. Pixel 7's Android 17 media/blink/colour checks worked by Ryan's report; custom and BOOP wake failed after rename, dock state unknown. Test wake while foreground and wirelessly charging. Handheld is intentionally tap-to-talk.

Protected physical rollback: `e746affbb82b577cef2f1cf6e731dff186c8f881`. No automatic install, grant, target/signing change or Windows sync. See `SESSION_HANDOFF.md` for precise provenance and next safe step.
