# Felt eyelids implementation plan

Approved visual: charcoal felt lids with crown highlight, glossy eyes, iris-only colour. User approved without revision. Base: v176 f64d49b3. Owner: boop-felt-eyelids-v177.

1. Publish non-visual neutral-pose regression and shader compile/link check; observe failure before material change.
2. Change only shared eyes.frag lid material using deterministic spatial felt detail and crown/rim lighting. Preserve original bitmap, rig silhouette, gaze, hue math, premultiplied alpha and all timing. No procedural replacement eyes. Material is a real-time interpretation of the approved study; joint device review determines appearance.
3. Bump candidate to177; retain v176 voice/audio/lyrics/HA source. Run inherited signed build and material checks on GitHub. No hosted visual tests.
4. Independent source review; verify artifact package/version/signer and packaged shader. Deliver candidate for joint testing, keep v176 rollback. Update owning handoff/status/memory.

Fake notification app and approved new hand grip are separate following work, not silently included. Original masters remain unchanged. This material change applies shared Unified Wall/notification/NowPlaying; standalone lab and static banners remain separate.
