# BOOP status: v161 Lab scale-zero fix built; colour remains accepted

Updated 2026-09-13. Current scope: GitHub source/tests/build/permanent signing only. No laptop, emulator or physical-device operations ran in this continuation.

Engineering/build commit `0b6ee6f91e05f00138a94ec2c9fd846117020754` on `boop-unified-eye-sync-safe-v159` is version161 / `1.2.161-lab-scale-independent`. It retains v160 speed and changes only the embedded Lab's Android-scale freeze condition plus the APK version. No artwork, authored motion, hue, permissions or signer changes.

Test-first checkpoint `fc9e6633ca7b578b7729cc5ee0e134294969075a`: standalone timing `34770049893` had the expected zero-scale Lab sign failure with five other tests passing. Full build `34770049896` reproduced it in raw AND materialized callbacks and stopped before signing. Red branch: `wip/boop-lab-scale-zero-red-fc9e663`.

At the repair commit, timing `34770388848`, appearance `34770388845` and full build `34770388933` all succeeded. All six timing functions passed; new Lab checks passed 20920 assertions per source path. Receipts report 235 Unified and 68 Shield functional tests with zero failures/errors/skips. All required preservation, colour, permanent-signing, package and archive checks passed.

Artifact `10321956422` / `BOOP-Unified`:
- APK SHA256 `c68b81be9b7d3e10883d2aea52c05dc7b8c187fcd835eaddf73e25e91cb4acd6`.
- ZIP SHA256 `623805381b04c2ce61a8a5bfcb56e663a9a2fc8eddbc1b9eb9b98ed987ccdf3b`.
- Same permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Artifact hashes/receipts were checked in the chat sandbox; all 13 packaged asset entries are byte-identical to v160. The APK itself differs because this is a real Lab repair/version bump, not another identical v160 rebuild.

Wall -> Shield eye colour remains physically accepted by Ryan. Prior captures demonstrated both directions with Pixel7. Last recorded physical state remains v159, user hue2, sharing on; no fresh device query or mutation here. Physical Pixel10 stays excluded.

Remaining: actual v161 runtime/visual speed checks on permitted laptop emulators, then authorized physical-device validation. No v161 installation or on-screen acceptance is claimed, and physical deployment remains held. The source-level Lab concern is now reproduced and repaired, not still an uninvestigated hypothesis. Two-device colour offline/reconnect is separate outstanding coverage.

Current details: `SESSION_HANDOFF.md` and `docs/handoffs/2026-09-13-lab-scale-zero-verification.md`. Preserve the v160 and colour receipts as historical evidence. No local synchronization, unrelated merge, permission change or bypass of the prior laptop block occurred.
