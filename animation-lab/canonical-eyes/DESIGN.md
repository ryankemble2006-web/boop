# Canonical eye motion library — 2026-09-10

Ryan authorized all other eye animations to match the default, using code rather
than moving an entire PNG, and authorized new motions where that is simpler.
He described the v3 MP4 blink as gorgeous and reported edge artifacts in the GIF.
This records approval of that preview's feel, not an Android/device acceptance.

One exact master texture, one shader, one data catalogue, one state controller.
The shader deforms existing lid skin below fixed roots and moves the sampled
iris/sclera field inside an anchored aperture. No whole-face jiggle, source-art
replacement, bottom lid, or image generation. Neutral state samples the original
master directly. No procedural substitute eyes. Colour controls are not changed.

The v3 edge bug came from per-column alpha-speck bottom bounds, integer coverage,
and unsmoothed edge segmentation. Use a continuous traced-edge lookup, a stable
closure destination and fractional edge coverage instead. Preserve 183ms blink,
73.2ms close, 8ms hold, 101.8ms reopen, 3–7s idle spacing, 18% double/110ms gap.

Catalogue covers all recovered eye families, plus new winks/attention/emotion
cues. Accessories/hands remain separate; the catalogue supplies their eye state.
JSON is the only clip definition. Java data is generated from it. Tests cover
time/state transitions, clamping, interruption, pause/resume and clip completion;
no pixel/golden/appearance assertions. Ryan judges previews.

Existing Animation Lab package com.boop.animationlab is the runtime test host.
This v5 lab replaces only that sidecar for testing and preserves v0.4 as rollback.
Production Unified and the concurrently owned canonical rebuild are untouched.
GitHub builds/signs the focused dependency-free Android lab; local Android TV
emulator tests its actual shader and catalogue. Library is then exportable for
later deliberate application integration.
