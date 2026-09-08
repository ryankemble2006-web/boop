# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

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
- Current candidate: same-version code 22 / `0.10.7-puppet-bay` **fill +10% refresh**
- Build source: `242ad467d2cbe91d2de9d4cc5e44cf278ec20fa5`
- Workflow: `34291502544` SUCCESS
- Artifact ID: `10081530720`
- APK SHA-256: `26e623be00928c263053427c9f07bd01540c2cf964a06b3f1bf55610fd00219d`
- Artifact ZIP SHA-256: `177bab9198ddece6bd6bbfc0ebef40e47cebc860302c5376b40ac39a3be146f0`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Functional/build/signer/package green; visual size/placement pending Ryan

## Current +10% visual pass

Ryan reported the card-owned BOOP was correctly inside the Now Playing bay, then asked for him to be 10% bigger after the first full-bay fill pass.

Current visual-only change:
- puppet ImageView is 110% of the existing stage width and height;
- stage remains the same size and clips the enlarged child;
- gravity remains centered;
- `FIT_CENTER` preserves the approved artwork proportions;
- controls, focus treatment, album-art path, HOME behavior and animation policy are unchanged;
- no GPU/OpenGL/skeletal renderer was added.

## Fast CI rule

Ryan owns real-device visual acceptance. CI sets `BOOP_SKIP_MANUAL_VISUAL_TESTS=1`; no screenshot, golden, emulator appearance, layout, focus-scale or animation visual acceptance runs. Functional logic, signed assembly, exact package/version, manifest/service/resource presence, permanent signer, APK integrity and upload remain checked.

## Next gate

Install the v0.10.7 fill +10% APK and physically judge BOOP's size/perch inside the right-hand Now Playing bay. Confirm clipping and remote navigation remain unchanged. Minor sizing/placement tweaks can continue after use. Defer full Tegra/GPU puppetry until the stage placement is worth locking.

Real Shield behavior is authority. Do not merge into unified until Ryan explicitly approves the standalone behavior.
