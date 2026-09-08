# BOOP unified status

Updated 2026-09-08. Canonical app branch `boop-unified`; package and permanent signer unchanged.

Current source candidate: based on `848a99725b6d64d4fb80d74fc466bdff807dd41e`, pending fresh CI and signed artifact. It includes the accumulated wake-name repair, modern Room -> Devices Home, configuration-only Settings, Favourites removal, robust HA category filtering, stable D-pad focus, and cached iris-only hue.

Last delivered artifact: `6cd9c67a03c639a20acde892e2d57186652e13d5`, run `34125882296`, artifact `10020439707`. It is not an accepted wake-name release. Ryan reports failed custom and fallback waking after rename, but continuing media/blink/colour functionality after his Android 17 upgrade. Shield layout/control defects from that candidate are the reason for this pass.

Local checkpoint: adapter Python syntax, workflow YAML parse and four synthetic colour fixture methods passed. Full Android/CI and physical-device acceptance remain pending for this candidate. Earlier dashboard functional test failures are not visual failures and must not be skipped to make a build green.

CI policy: no automated visual acceptance. Focused functional tests and signer/package/archive verification remain. Upload signed test APK before the slower non-visual launch smoke. Ryan performs appearance/real-device acceptance. Rule is durable in BOOP_RULES.md.

Protected rollback: `e746affbb82b577cef2f1cf6e731dff186c8f881`. No user-device install, grant, reset, signing change or Windows synchronization performed. See SESSION_HANDOFF.md for exact evidence and next step.
