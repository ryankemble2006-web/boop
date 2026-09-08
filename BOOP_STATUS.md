# BOOP Shield clean launcher status

Updated 2026-09-09. Standalone branch `boop-shield-clean-launcher`.

- Package: `com.boop.shieldhome`
- Unified/AIO `com.boop.alpha1`: separate and untouched
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Physically accepted Apps drawer: 0.9 floating square icons
- Physically-good HOME geometry/chrome: 0.9.4
- Notification Listener: physical PASS
- v0.10.3 collision layout: physical **much better**
- v0.10.4 spacing: physical **awesome spacing**
- v0.10.5 album art: physical **PASS**
- v0.10.6 focus outline: CI/signer green, physical judgement pending
- +10% puppet size source: `242ad467d2cbe91d2de9d4cc5e44cf278ec20fa5`, physical judgement pending
- Current candidate: code 22 / `0.10.7-puppet-bay`, playback dance + paused DJ sulk
- Build source: `cc54657a3ef5e97114e4b753c23b8801d56b2bda`
- Workflow: `34292426967` SUCCESS
- Artifact ID: `10081873605`
- APK SHA-256: `1d7607c5b8830b98d9d2235ec77a50f5b246875a67e583799c6281d3f5350a06`
- Artifact ZIP SHA-256: `cd52b6100b14f6b4890d9ca1f141b1be23bb0854fe297ad2a00a0fa1d7e1808f`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Functional/build/signer/package green; animation feel physically pending Ryan

## Dance / DJ sulk candidate

Ryan approved: **dance during playback; upset at the DJ when paused**.

- playing -> continuous 2.4s dance loop with lean, double bounce, tilt and gentle squash/stretch;
- paused -> 5.2s upset loop with sulked lean/lower stance and periodic irritated head shakes;
- buffering/other eligible non-playing states remain neutral REST;
- track/session changes keep the existing acknowledgement hop layered over the current pose;
- +10% puppet size and existing clipped 230dp bay are preserved;
- controls, media artwork path, HOME behavior, focus, permissions and package identity are unchanged;
- animator-disabled and power-saver safeguards remain.

TDD RED: commit `64ea1be637092738942e92bb5ed5f0e3201607bc`, workflow `34292204559`, 74 tests with exactly 2 expected failures for missing UPSET state and insufficient dance amplitude. GREEN: source `cc54657a3ef5e97114e4b753c23b8801d56b2bda`, workflow `34292426967` SUCCESS.

## Fast CI rule

Ryan owns real-device visual acceptance. CI sets `BOOP_SKIP_MANUAL_VISUAL_TESTS=1`; no screenshot, golden, emulator appearance, layout, focus-scale or animation visual acceptance runs. Functional logic, signed assembly, exact package/version, manifest/service/resource presence, permanent signer, APK integrity and upload remain checked.

## Next gate

Install the dance/DJ-sulk APK. Confirm BOOP dances continuously while playing and visibly sulks/shakes his head when paused. Judge amplitude/timing manually on the Shield, and confirm clipping, album art, controls and remote navigation remain unchanged.

Real Shield behavior is authority. Tune motion parameters only unless physical evidence requires a wider change. Do not begin full Tegra/GPU puppetry or merge into unified until Ryan explicitly approves.
