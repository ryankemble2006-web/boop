# SHIELD TURBO handoff

Owner branch: `shield-turbo-v01`. Package: `com.boop.shieldturbo`.
Candidate: `0.1.0`, versionCode `1`.

## Current work, 2026-09-07

Ryan requested GitHub-only chat-mode development, existing signing keys only, and a separately installable utility in this repository. No BOOP runtime or unified-app changes are authorised by this utility task.

The initial candidate `5f9e636b7851f0512792b6296aacc12dcd007f53` compiled and passed its four initial tests, but run `34123083174` failed Android lint on hard-coded view IDs. Signing was not reached; that run did not produce a signed candidate.

Regression commit `2d0d5776d5e83ee5c15eb8999b272390a0af3591` added real edge-case tests, source guards and installed-release remote-input verification. A concurrent worker then pushed `b7a9ee279be97d9b10f744f3c899ff8468b23065`, fixing numeric IDs with generated IDs. That change is preserved in ancestry; stable resource IDs now identify persistent controls and generated IDs identify result cards.

Red-test evidence: run `34123950576`, job `101748196618`, executed 15 Kotlin tests with seven failures (invalid CPU value, temperature units/non-finite values, isolated probe exceptions, privilege evidence failure) and four source guards with three failures. This repair addresses those reproduced cases, moves scanning off the UI thread, removes unnecessary screen retention and makes result cards D-pad focusable. It keeps the original public app permission boundary.

Next: inspect the new GitHub Actions run for this repair. Require unit tests, safety guards, lint, release assembly, established signer fingerprint, package/archive integrity and installed-release remote-input checks before delivering a signed candidate. Do not mark these as passed merely because this source is saved.

## Product and verification limits

v0.1 is read-only diagnostics, not an optimisation release. ADB setup/helper and actual tuning are deferred. Tier detection reports app authority, not guessed device root status. Only network-state permission is requested. No app installations or permission changes on Ryan's physical devices are part of this task.

Physical NVIDIA Shield acceptance is pending. The planned CI emulator is API 30 with a handheld profile plus D-pad simulation, not physical Tegra validation.

The existing GitHub `BOOP_DEV_KEYSTORE_B64`, `BOOP_DEV_STORE_PASSWORD`, `BOOP_DEV_KEY_PASSWORD` secrets and alias `boop-dev` are used inside the runner. Private signing material is never published; temporary key material is removed after assembly. No relay or other BOOP credentials are used.
