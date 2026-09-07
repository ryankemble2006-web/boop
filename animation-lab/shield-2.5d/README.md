# Shield 2.5D puppetry and official five-digit hands

Saved on 2026-09-07 at Ryan's request. This is an art/design archive, not a working animation or a separate app. Application implementation remains on `boop-unified`.

## Saved direction

Explore deliberately using the Shield's graphics hardware for rich real-time 2.5D puppetry of the existing headphones BOOP: layered depth, restrained mesh deformation, gaze-led acting, independent hands and headphones that follow movement with weight, lag and controlled recoil. The aim is a better performance, not maximum GPU load. No measured headroom or guaranteed frame rate is claimed.

The complete engineering/evidence brief lives at [docs/animation/SHIELD_2_5D_HANDS.md on boop-unified](https://github.com/ryankemble2006-web/boop/blob/boop-unified/docs/animation/SHIELD_2_5D_HANDS.md). Profile the actual device and drawing path before choosing layered Canvas or a small OpenGL ES renderer. Preserve media playback, remote pass-through, lifecycle, screen-off, idle and reduced-motion safeguards. Real beat synchronisation is a separate capability, not supplied by extra GPU power. No microphone, root, overclocking, new permissions or audio-focus changes are authorised by this archive.

## Five digits, always

Every hand has four fingers plus one thumb. Retain thumb, index, middle, ring and little fingers as separate rig controls in all poses/transitions. Fingers can curl or be naturally hidden by a grip, but cannot disappear, fuse into a permanently four-digit design, duplicate or change count. Inspect open/neutral and extreme poses before acceptance. Earlier four-digit studies are not approved references.

`BOOP_YELLOW_HANDS.md` remains the visual authority: bright yellow plush/felt, rounded fingers and palms, short rounded cuffs, floating hands without arms. Pose the existing hands; do not redesign them. Keep BOOP's approved eyes/headphones unchanged. A generated headset in a grip study is not replacement artwork. No animal transformations, mouths, tongues or extra faces. Preserve the exact transparent master unchanged, and label all crops, layers and previews as derivatives.

Retain the illustrated gesture ideas: open palms, a wave, pointing, curved gripping fingers, gripping/adjusting an earcup and a later lift-one-cup/listen/settle gesture. Independent wrists/fingers should eventually transition into those poses. These are visual ideas, not verified rigging or sign-language poses. Ryan accepted the work as a promising start; no physical Shield test followed in this session.

## Actual archived image

![Corrected five-digit open-hand study, lossy display preview only](../shared-assets/boop-yellow-hands/pose-studies/five-digit-open-pose-preview.webp)

This is one 448 x 448 WebP display preview, 8230 bytes, SHA-256 `d1209ef642d722ff17f1c3d334121d9ac5cfa82ea43f3f50788a1098fdf65192`, Git blob `854fd36b870f1c6e647c5fdce853003cd05e5e2f`. It is lossy and has the study's baked black background. It is NOT the official transparent master, a full-resolution archive or a runtime resource. [Source/transfer manifest](../shared-assets/boop-yellow-hands/pose-studies/manifest.json).

The unchanged official master from the uploaded ZIP was reverified: 1774 x 887 RGBA PNG, 1541931 bytes, SHA-256 `74e3b162d8fa750491b9a1577d51d043e1f7fdcc3940cbdf22f742bc58c9f556`. Its GitHub binary transfer remains pending. The corrected open-pose full-resolution source is 1254 x 1254 RGB PNG, 1199789 bytes, SHA-256 `7e9f0dea25283626b004f4dbf0c0633544f2310bbe94ce50b0463bc7f6cffdee`; it also remains pending transfer. Both are retained in the downloadable handoff bundle.

Only one new pose-study source image was available as a file. Other grip/pointing/headphone examples remain conversation references and saved written ideas, not archived image files. Do not claim that the complete illustrated set is stored here.

Next safe step: upload the exact PNGs to their documented branch paths, verify checksums and update transfer manifests. Preparing the actual jointed layers/mesh and integrating or benchmarking it requires a separate request. No app code, build/workflow, package/version, permissions, signer, deployment or accepted checkpoint changed.
