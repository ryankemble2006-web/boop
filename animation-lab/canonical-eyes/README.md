# BOOP canonical eye code library

All 26 motions share `EyeMotion`, `catalogue.json`, `eyes.frag`, and the byte-locked
master. Runtime samples/deforms existing lid/eye material. No frame playback,
whole-PNG movement, regenerated art, alternate iris drawing, or bottom eyelid.

`tools/prepare.py --out <directory>` validates the master/catalogue and produces
Java catalogue data plus a smooth packed lid-edge lookup. Python/Pillow are
build-time tools only. Android runtime has no third-party dependencies.

Integration units:

- `java/com/boop/eyes/EyeMotion.java`: pure state/clock/transition controller;
- generated `EyeCatalogue.java`: the single JSON catalogue compiled as data;
- `android/com/boop/eyes/CanonicalEyeRenderer.java`: GLES2 renderer;
- generated assets: original master, lid-rig lookup, GLSL shader, catalogue;
- `android/com/boop/alpha1/BoopDevMenuActivity.java`: test host only, not a production dependency.

Supply monotonic milliseconds, select a clip with a blend duration, sample a
Pose, and pass that pose to the renderer. Pause/resume the host's clock and GL
surface with lifecycle. Finite clips hold their final state; the application
decides the next logical state. Gaze values normalize to [-1,1]; lids to [0,1].
Neutral samples the master directly. Placement/scaling fits the view once and
does not animate the character rectangle. The displayed pupil/iris moves by
deforming the existing eye texture within a stationary support region.

The existing Animation Lab package is the v9 test host (permanent signer).
This focused lab shelf covers eyes and the new SIGN_SHOW.md notification demo;
old v0.4 accessory/notification shelves
remain preserved in the reference APK/source archive and are not claimed ported.
No production Unified/clean HOME integration is performed by this library change.

CI checks state math, compilation, package/no-permission boundary, exact master,
permanent signer and ZIP integrity. Local Android TV tests the actual shader.
Ryan owns all appearance and animation acceptance. v3 MP4 rhythm received positive
user feedback; these new code-rendered versions require their own review.

v8 adds SignMotion and NotificationSignView for four local sign-holder fixtures.
The host arranges the same eye renderer above independent exact hand layers and
a vector sign. Share the logical clock and pass SignMotion.Pose.eyes to the eye
renderer, and the full pose/style to NotificationSignView.show. The sign stage
uses a 1000x680 logical canvas; the example host reserves 64% height for eyes.
v9 replaces the rejected flat open-hand overlay with four individually sampled
finger meshes per hand in front and palm/thumb material behind the board. This
is a restrained 2D grip from the same artwork, not a full 3D rig. Normal eye clips
remain selectable; no live notification semantics or production wiring exist.

v7 inner-corner repair: the opaque rig texture has TWO rows, source material
edge and continued destination contour. Ship the generated rig and shader as
one versioned unit. The destination no longer flattens into the old source cap.
Only the moving boundary changes; skin UV sampling and original face alpha stay
anchored. The failed v6 alpha-only experiment is not the final repair.
