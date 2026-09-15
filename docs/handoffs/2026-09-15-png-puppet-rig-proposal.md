# Proposed PNG puppet rig — 15 September 2026

Status: design proposal, not implemented or installed. Current app remains v180.

## User evidence
Felt colour sharing worked on the first attempt (user report). Current colour range is too dark: the requested range includes vivid green and blue fabric, not charcoal with a hue shift. User reports no visible hairs. Preserve the successful slider/preview/sharing interaction.

## Diagnosis
The current renderer loads the original approved eyes PNG and replaces lid appearance with procedural felt. It does not use the generated felt colour-study PNG. v180 reduced strand density and multiplies ribbon width and alpha by min(1, pixelScale/0.65), reducing visibility in small views. Palette normalizes colour to the original charcoal luminance, deliberately preventing bright fabric. These are implementation choices, not evidence of inadequate device hardware.

## Recommended method
Keep the existing GLES renderer, animation clock, pose catalogue, shared colour preferences and live preview. Replace procedural felt/ribbon approximation with prepared PNG material layers:
- Existing eye/gaze layer below independently deformable left and right felt lids.
- Transparent soft edges retaining fibres and tangles from the approved study.
- Sufficient felt artwork behind the resting lid to cover a full blink, without stretching the open-pose strip across the eye.
- Stable material coordinates so the same worn patches and tufts persist during blinks and expressions.
- Separate fabric base colour from baked light/shadow detail: vivid full-colour fabric with retained soft highlights, shadows and fibre contrast. Keep original charcoal as an explicit reset.
- At small sizes use a deliberately prepared texture level that retains a few grouped tufts; do not enlarge every fine hair or increase light globally.

The study PNG contains a hero pair plus smaller variants on a black background. It is a flattened reference, not a ready-to-drop-in rig: hidden closed-lid material and transparency must be prepared. A single whole-image warp would stretch eyes, fibres and baked lighting together. A pre-rendered frame atlas would preserve individual frames but complicate continuous independent blinks/gaze and combinatorial recolouring. Layered PNGs best fit existing controls.

## First reviewable candidate
One canonical blink/look rig using the prepared felt artwork, with charcoal, vivid green and vivid blue available through the existing felt slider. Preserve all animation timings and eye-colour behaviour. Test geometry/masks, colour independence, packaging and signing on GitHub, then joint viewing at phone widescreen and Shield's actual small face size. User judges likeness and surviving texture; do not claim every subpixel fibre can be retained.

## Acceptance
- Recognizably the approved scruffy material, not a generated line pattern.
- Bright green/blue fabric without blown-out stage lighting.
- Full closures expose consistent felt with no holes, smeared pile or eye texture leaks.
- Existing shared settings and immediate preview still work.
- Original assets and v178/v180 rollback artifacts retained.

No migration, new graphics engine or display setting change is presently justified. PNG/Bitmap texture loading is already used by this app; Android GLUtils supports this directly: https://developer.android.com/reference/android/opengl/GLUtils .
