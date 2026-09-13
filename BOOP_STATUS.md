# BOOP status: v161 speed and two-way colour physically accepted

Updated 2026-09-13. Ryan tested the installed v161 and confirmed animation-speed changes work on BOTH Shield and Pixel 7, and colour changes automatically travel in BOTH directions between them. This supersedes the previous awaiting-test status. The reported speed and live colour behaviours are USER-ACCEPTED ON PHYSICAL DEVICES, not merely installed or CI-green.

Installed and previously identity-verified on both: `com.boop.alpha1`, versionCode161, `1.2.161-lab-scale-independent`. App/build commit `0b6ee6f91e05f00138a94ec2c9fd846117020754`; full build `34770388933`, artifact `10321956422`. APK SHA256 `c68b81be9b7d3e10883d2aea52c05dc7b8c187fcd835eaddf73e25e91cb4acd6`. Same permanent signer. Both preceding ordinary installations succeeded and installed APK hashes matched GitHub.

Acceptance scope: speed changes on each device; automatic colour delivery Pixel 7 / Wall -> Shield and Shield -> Pixel 7 / Wall. Speed stays device-local; do not describe speed as synchronized. Ryan's report does not enumerate every surface/rate or a deliberate offline/reconnect cycle. No such additional outcomes are claimed, and they are not blockers to the reported acceptance.

This continuation records the result in documentation only. No app source, tests, workflow, artwork, signing, installation, permissions or settings changed; no device or emulator was operated. Physical Pixel10 remains excluded. No colour fixtures were restored. The v160 speed implementation, v161 Lab repair, exact original1x and accepted Shield polish remain intact.

Workflow remains GitHub source/non-visual tests/build/permanent signing, requested installation, then testing together with Ryan. No autonomous emulator requirement or hosted visual acceptance. Shared rule: `main@24a260b6e7cdd5aed792ccfbb683e8e495eb5f80:BOOP_START_HERE.md`.

Next: move on from speed and colour; await Ryan's next task or a new specific problem rather than reopen completed repairs. Current owner: `boop-unified-eye-sync-safe-v159`. Read `SESSION_HANDOFF.md`, `BOOP_UNIFIED_MEMORY.md` and `docs/handoffs/2026-09-13-v161-speed-colour-accepted.md`. The installation and Lab regression receipts remain preserved as provenance.
