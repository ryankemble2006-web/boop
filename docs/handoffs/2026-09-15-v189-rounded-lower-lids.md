# v189: rounded lower photographic lids

Date: 2026-09-15. Owner: `boop-rounded-lids-v189`.
Parent delivery: v188, docs `02cb67a99fdaf391b482606a400d969dcfa9e347`.
Candidate source: `470bb4b471e7e452b29b6a3a437bea0fa4a0904e`.

## User request and boundary

Ryan accepted the v188 whole-lid movement and asked for a gently rounded lower edge when blinking. His held near-closed frame showed a straight lower edge. This records acceptance of the movement, not blanket acceptance of every pose.

Only the closed target contour changes: the same photographed cap stretches to a lower oval (centres470/1066, horizontal radius320, vertical curve410+226*sqrt(1-dx*dx), maximum636). A missing-edge column retains its prior642 endpoint. The original PNG, rig bytes, hue/felt palette, brightness, fibres, shared settings, animation clocks and187lab remain unchanged. V188's shared RGB/transparency sample and premultiplied replacement remain unchanged.

## Evidence

- Red run34943905047 at206702fd6a058fe908e1625e0522b43af6af47f9 reproduced the previous642-pixel endpoint at the centre and both sides.
- New nonvisual tests evaluate the actual shader expression, curvature, continuous closure, crop clearance and sustained opaque eye coverage from the actual PNG rig.
- Existing four-end regression now evaluates the actual oval target at50%,77% and100%; open-pose arithmetic, central colour, replacement compositing and gap checks retained.
- Independent exact-source review found no blockers.
- Exact-source GitHub run34944002348 passed. Lower contour centre636, sides519.4118 at280px offset; minimum feather-adjusted full-eye coverage margin7.232px. All four ends passed at50/77/100%. Shader linking, APK source identity, native lyrics, colour sharing,160720 timing checks and both20956 lab callback harnesses passed. Two historical source-freeze skips remain; no hosted visual tests.
- Permanent signer verified locally and in CI: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- APK SHA256: `9df0c1727099ca71b806438755a272c4afe89b9992ef76454678d50127ae0a8f`. Version189/1.2.189-png-puppet installed successfully on Shield and Pixel7Pro; actual installed APK hashes and versions matched.
- Shield16/16 preference files unchanged; phone10/11 unchanged, only notification bookkeeping differed. Eye/appearance hashes rechecked unchanged after lab use; phone rotation remains automatic1/user0.
- Actual phone held comparison: previous188 Blink87.9ms/99%, new189 Blink87.8ms/99%. Rounded lower edges visible and no exposed white in new held frame. Near-matched times, not an identical-time capture. Shield returned Home; no separate full Shield animation inspection by assistant. Captures remain private.
- Ryan then requested inspection of his selected95.6ms/95% paused frame. Actual capture shows a thin white crescent beneath each rounded lid at this partial closure. Slider left untouched; this is consistent with a partially open eye, not the fully closed coverage test.
- **Ryan physically accepted the result:** he confirmed the eyes now morph like an eye beneath the felt. This is the accepted photographic eyelid movement/rounding checkpoint. Keep broader device and feature acceptance separate.

## Preserved rollback

V188 APK SHA256: `5dd6059325b76fbc7f079158babd348c6f6d479f0bd8bfe27355ddc26688709c`.
Existing rollback artifact retained. No merge to main.
