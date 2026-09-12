# BOOP Rally implementation plan

Goal: one signed, offline, two-game Shield launcher, installed with the user's local files.
Architecture: Android Java launcher + JNI/libretro host + pinned native DOSBox Pure; local bundles are separate from public code/artifacts.
Spec: DESIGN.md. Execute inline for the user's requested complete build/install/test task.

- [ ] Write and run failing bundle preparation tests: preserve bytes, choose correct executable, deterministic ZIP output, reject missing games, reject symbolic links, reject output inside source.
- [ ] Implement `tools/prepare_games.py`; repeat the behavioral tests.
- [ ] Write and run failing pure-Java controls tests, then implement `Controls.java`: key mapping, multiple simultaneous sources, release-on-pause, deadzone/trigger boundaries.
- [ ] Implement launcher, validated local/SAF bundle loading, pause menu and native host. Keep controls, file policy, UI and emulation ownership in separate files.
- [ ] Build native core from pinned source and APK with existing SDK/JDK on GitHub, use existing development signer and verify fingerprint. Publish no proprietary files or local diagnostics.
- [ ] Prepare private bundles from the existing desktop copies, verify originals unchanged, download exact CI APK, verify signer/hash, install only on positively identified Shield and provision bundles.
- [ ] Exercise each game to a race where possible; record exact launch/input/menu/return/save results and any performance or sound uncertainty. Keep screenshots private.
- [ ] Review all changes and repeat tests. Update owning branch handoff/status/memory, publish reviewed work and compare local HEAD to live GitHub. Add a narrow source-routing note on main without changing app contracts.
