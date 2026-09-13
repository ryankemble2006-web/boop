# Shared eye colour implementation, then animation speed

Updated 2026-09-13. GitHub feature owner: `boop-unified-eye-sync-safe-v159`.
Accepted device source remains v156 `a901c1e9`; its Shield sweep is complete. Both recovery branches remain preserved. No local draft is the source of truth for this continuation.

Current implementation completes appearance settings, private activity registration, Application startup and authenticated opt-in hue sharing. The runtime now guards invalid connection attempts and cancels stale authentication work. The existing local hue store, Wall hue controls, shader, approved eye master and all authored clips remain unchanged. Sharing remains OFF on upgrade.

GitHub's recovered baseline `04f7c10c` passed four tests and failed the missing Application/settings wiring contract. While completing it, live HEAD advanced to `9e452b43` with two additional settings tests only. Those tests were retained, not overwritten; this commit reconciles both changes. Fresh GitHub tests and full Android compilation/signing must now run. Nothing has been installed, and no runtime or visual pass is claimed.

Primary research retained in `docs/superpowers/plans/2026-09-13-shared-colour-and-speed.md`. Rechecked official Android SharedPreferences/GLSurfaceView and HA WebSocket/input_text/collection/entity-registry sources. The helper uses actual returned IDs, omits initial for restoration, and only explicit setup can create it. No unauthenticated UDP, replacement signer, new Android permission, voice changes or implicit native-lyrics merge.

Next: verify colour in GitHub and local emulators, then implement independent animation speed with exact 1x equivalence. Pixel 10 remains excluded. Shield/Pixel 7 deployment only after runtime gates; Ryan owns visual acceptance.
