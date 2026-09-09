# BOOP Shield clean launcher status

Updated 2026-09-09. Standalone branch `boop-shield-clean-launcher`.

- Package: `com.boop.shieldhome`
- Unified/AIO `com.boop.alpha1`: separate and untouched
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Physically accepted Apps drawer: 0.9 floating square icons
- Physically-good HOME geometry/chrome: 0.9.4
- Notification Listener: physical PASS
- v0.10.4 spacing: physical **awesome spacing**
- v0.10.5 album art: physical **PASS**
- v0.10.6 focus outline: CI/signer green, physical judgement pending
- +10% puppet size retained
- Playback dance: after animations were re-enabled Ryan reported BOOP **"looks awesome"**
- Paused DJ-sulk: implemented/signed; explicit physical acceptance not yet recorded
- Current candidate: code 22 / `0.10.7-puppet-bay`, dance + sulk + natural blink
- Exact APK source: `b5221b842aff959d283e561aeac1f8b32e25a5ca`
- Workflow: `34293856616` SUCCESS
- Artifact ID: `10082387913`
- APK SHA-256: `5078e83e1763d92b2d58cb7748e50693d12f7b4407ecdc2c3f5a531c5f6a0ccd`
- Artifact ZIP SHA-256: `69a4588e41fd724e9493afd3956cd4783992ad12e95b159993559877041811ff`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Functional/build/signer/package green; blink appearance physically pending Ryan

## Natural blink pass

Ryan approved a bounded blink experiment while preserving the dance/sulk renderer.

- blink duration 183ms using BOOP's proven soft close/reopen curve;
- random interval 3–7 seconds;
- 18% double-blink chance, 110ms gap;
- existing `boop_headphones` artwork is unchanged;
- near-black eyelid masks are drawn over the two eye ovals by the puppet ImageView;
- dance/sulk transforms, +10% size, clipping, controls, album-art path, HOME behavior and focus are unchanged;
- blink obeys the same animator-disabled and Power Saver safeguards.

TDD RED: `c435f28e659c583b13cffd3aad0da8d245157226`, workflow `34293639208`, 77 tests with exactly 3 expected blink-contract failures and no signing. GREEN: `b5221b842aff959d283e561aeac1f8b32e25a5ca`, workflow `34293856616` SUCCESS.

## Fast CI rule

Ryan owns real-device visual acceptance. CI sets `BOOP_SKIP_MANUAL_VISUAL_TESTS=1`; no screenshot, golden, emulator appearance, layout, focus-scale or animation visual acceptance runs. Functional logic, signed assembly, exact package/version, manifest/service/resource presence, permanent signer, APK integrity and upload remain checked.

## Next gate

Install the natural-blink APK and judge the eyelid placement/timing on the real Shield. Confirm the blink feels natural and does not disturb dance, sulk, clipping, album art, controls or remote navigation.

Real Shield behavior is authority. Do not merge into unified or begin full Tegra/GPU puppetry until Ryan explicitly approves.
