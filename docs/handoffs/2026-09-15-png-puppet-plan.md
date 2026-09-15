# PNG puppet implementation plan
Goal: use the approved photographic PNG for the open face, photo felt for blink coverage, vivid cloth colours and existing shared controls.
Spec: docs/handoffs/2026-09-15-png-puppet-rig-proposal.md, approved by user15September.
Workflow: GitHub source/tests/build/signing; joint Shield/Pixel7 only. Preserve v180 rollback and accepted sharing. No visual CI or local build.
1. Add approved PNG unchanged and hidden felt photo as new assets. Never overwrite old masters.
2. Write failing actual-image rig/palette tests; run on GitHub.
3. Add PngPuppetRig to derive an exterior transparency mask without punching out black pupils, and a column lid boundary from white eye pixels. Upload raw RGBA rig bytes to avoid premultiplying encoded geometry.
4. Replace procedural material/fringe draw with original photographic open image and separate fixed-coordinate photo-felt coverage during closure. Keep original clock/poses, viewport ownership and hue binding. Maintain image aspect and exclude smaller variants from study.
5. Use HSV vivid fabric colours and an independent recolour amount: zero leaves photographic charcoal exact. Preserve shared payload0..359.
6. Verify GLSL, actual asset hashes, rig/palette behavior, preserved animation/control/sharing and actual APK contents. Independent review.
7. Install verified signed candidate on authorized Shield/Pixel7; document exact identity and leave aesthetic acceptance to user.
