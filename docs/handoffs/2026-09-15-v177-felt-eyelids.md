# v177 felt eyelids — signed candidate

Updated 2026-09-15. Owner: `boop-felt-eyelids-v177`.
Base: accepted v176 tree `f64d49b39bd292ffa08011fcec8c61a50113f426`.
Built source: `55feb6e3e83d16d8c27b20cad225c24b2ab472f3`.

## Approved direction and implementation

Ryan approved the charcoal felt eyelid study without revision, especially its soft crown light. The shared GLES2 eye shader now applies deterministic felt pile and crossed fibre detail to resting and closing upper lids, with neutral charcoal lighting. This is a real-time interpretation of the approved study, not a replacement generated eye bitmap. The study SHA256 is `9f2ec16f3aac0e443130b34127a16dcde8489d5a23440399b76a5295800d6f03`.

The original eye master, alpha silhouette, lid rig, gaze equations, hue math, renderer and animation clocks are unchanged. Iris colour still follows the existing sharing path; cloth stays neutral. Production changes versus v176 are restricted to `eyes.frag` and app version. Shared Unified Wall, notification eyes and Now Playing receive the material; standalone apps and static banners are outside this change.

The approved yellow hand master remains unchanged. Required anatomy is exactly four fingers and one opposable thumb per hand. The approved sign grip shows four fingers in front and the thumb behind the sign; hidden does not mean missing. Production integration of that new grip and the Shield-triggered fake phone-notification diagnostic remain subsequent work, not completed by v177.

## Verification

- Test-first run [34928761171](https://github.com/ryankemble2006-web/boop/actions/runs/34928761171) failed only because neutral-open eyes bypassed material shading.
- Run34928830226 passed the new shader/art/motion checks, then the historical music test's production allowlist rejected the authorized shader change. The exception is now narrow, with a separate v176-to-v177 production scope check.
- Final [run34928908920](https://github.com/ryankemble2006-web/boop/actions/runs/34928908920) succeeded. Five material/compiler/preservation checks; inherited voice startup, music/worker, audio routing, HA, lyrics, timing and face-ownership checks passed. Two inherited one-time v161/v162 freeze tests remain skipped, not passed.
- Independent read-only source review found no critical or important issue. Optional duplicate material work during closure is a possible optimization only if device performance warrants it.
- Actual APK contains the exact shader and original eye master bytes and retained lyrics/music classes.
- Downloaded APK package/version, hash and permanent signer verified locally. No local build, emulator or hosted visual test.

## Artifact and rollback

Artifact `10381345601`: `BOOP-Unified-v177-Felt-Eyelids`.
Package: `com.boop.alpha1`; version `177 / 1.2.177-felt-eyelids`.
APK SHA256: `e8d082480e63fd02f6b391e6a362a1b71fb529061614c36ed821770347a68c8c`.
Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Accepted v176 rollback downloaded and hash/signer verified: `f686ca98e3f58d84d5adab3d7bd0647136e9117c290330c6a473c0223b59977e`, run34877467478. Previous accepted behavior remains the rollback baseline.

## Device status and next action

Not installed in this task. Approved mockup is distinct from physical acceptance of the rendered material. Next joint check: felt/crown appearance at normal viewing distance, smooth open/blink/wink/gaze, and iris colour changes on the device Ryan selects. Keep settings and permissions intact. No notification fix or natural-voice feature is claimed.

Separate Johnny HA branch has later v14 acceptance; historical pending Johnny notes below the new root handoff are superseded by its current owning-branch receipt.
