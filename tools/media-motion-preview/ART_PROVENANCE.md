# Art provenance

Built-in image generation was used, not a paid CLI/API fallback. Ryan approved local image cleanup on 2026-09-06. H1 source/music.png is the original approved H1 Soft groove render, preserved without regeneration. P1 source layers were generated from the approved Quiet nibble reference. Cleanup uses deterministic OpenCV segmentation, neutral-checker exclusion for the isolated yellow hand/cream kernel, and a subpixel alpha edge. Source RGB is retained. Original files are not overwritten.

## Layer generation prompts

### music

Prepare this approved BOOP H1 design as a production animation sprite. Change ONLY the background: remove the entire charcoal background to genuine alpha transparency, including the open space inside the headphone arch, gaps between the headphones and eyes, and between the eyes. Keep all existing headphones, two eyes, cyan accents, tilt, proportions, materials, shading, exact placement and framing UNCHANGED. Canvas stays 1536x1024. Do not move, enlarge, restyle or crop the subject. Preserve black pupils and black headphone material as opaque, not cut out. No background colour, no checkerboard drawn into the image, no floor, no shadow outside the object, no text. Output a clean RGBA PNG sprite with genuine transparent background.

### cinema

Make an animation BASE LAYER from this approved BOOP P1 image. Keep the two eyes, striped popcorn tub, popcorn IN the tub, and the right-hand-side yellow hand holding the tub exactly unchanged, same pixel placement and scale in a 1536x1024 canvas. Remove the entire LEFT pinching hand and the single popcorn kernel it is holding. Also remove all loose airborne popcorn outside the tub. Remove the entire charcoal background to genuine alpha transparency, including all spaces between eyes and props. Do not move, resize, restyle or reframe any retained object. Preserve dark pupils and eyelids as opaque. The result is ONLY eyes floating above the held striped tub, on genuine transparent background. No drawn checkerboard, no head, no mouth, no body, no text. Clean RGBA PNG.

### hand

Extract a single animated prop layer from this approved BOOP P1 image. Retain ONLY the yellow LEFT pinching hand, unchanged in its exact pixel position, scale, orientation and materials within the same 1536x1024 canvas. REMOVE the popcorn kernel between its thumb and index; the thumb and index now pinch empty space. Remove the eyes, popcorn tub, other hand, all popcorn and all charcoal background. Everything except that single pinching yellow hand must be genuinely alpha transparent. Reconstruct clean fingertips behind the removed kernel if needed but keep the existing five-digit human hand pose and geometry. Do not enlarge or center the hand; retain its original lower-left placement. Preserve the smooth rounded yellow puppet finish. No added objects, no drawn checkerboard, no text. Output genuine transparent RGBA PNG.

### kernel

Extract a single animated prop layer from this BOOP image: retain ONLY the ONE popcorn kernel pinched between the left yellow hand's thumb and index finger (the isolated kernel around x665 y505). Keep this kernel at its original position and size in the 1536x1024 canvas. Reconstruct any tiny occluded edge as a natural fluffy popcorn kernel, preserving the cream-white lobes and toasted centre. Remove ALL hands, eyes, tub, other popcorn, and background to genuine alpha transparency. Do not center, enlarge or move the one kernel. The canvas is almost entirely empty transparent space with exactly ONE tiny popcorn kernel at its original coordinate. No text, no drawn checkerboard, no shadow beyond the popcorn. Output genuine transparent RGBA PNG.

## Transparency retry

The cinema, hand and kernel layers received a targeted built-in image edit requesting removal of the painted checkerboard, preserving artwork/scale and an actual RGBA alpha channel. The result still contained painted checkerboards. The user then explicitly approved local cleanup; those untouched retry outputs are preserved in source/.
