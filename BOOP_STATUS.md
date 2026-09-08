# BOOP unified status

Updated 2026-09-08. Branch `boop-unified`; package/permanent signer unchanged.

## Signed v44 candidate available

Code `4044ee55b5a39e2a220ee897de0393b796c29a5f`, version 44 / `1.1.1-unified-eyes-wake-home`. Run `34192698906` completed successfully. Artifact `BOOP-Unified` ID `10042812867`, downloaded and checked. APK SHA-256 `5c60b904d06d8a94ad3ab117e2da86c5726c91ff8f2ca40845a0f2266e9966f6`.

Passed: 52 Shield functional tests, 59 unified/wake tests (zero failures/errors/skips), non-visual contracts, Launcher lint, compilation, permanent signing, package/archive checks. Download ZIP digest, APK hash and built-commit receipt match. Exact provenance: `docs/BOOP-V44-BUILD-RECEIPT.md`.

Includes modern Room -> Devices Home, configuration-only Settings, Favourites removal, robust HA category filtering, stable focus, cached iris-only colour, accumulated wake-model/fallback repairs and canonical phone eyes/blink on Shield. Concurrent `dcc7acf` work is preserved as parent.

No GitHub visual/aesthetic-source/emulator/device acceptance ran. Ryan owns appearance, installation/launch, D-pad use, permission screens, acoustic wake and dock/mic testing. No physical acceptance is claimed. Protected rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`.

Earlier v43 `6cd9c67` had failed custom and BOOP wake after rename by Ryan's report; dock state/cause unknown. Media/blink/colour still worked after his Android 17 update. Test v44 voice wake foreground and wirelessly charging; undocked intentionally stays tap-to-talk. Enable/access-screen behaviour remains physically unverified.

No automatic install, grants, target/signing changes or Windows synchronization. Documentation-only updates do not rebuild the verified code. See `SESSION_HANDOFF.md` for next steps.
