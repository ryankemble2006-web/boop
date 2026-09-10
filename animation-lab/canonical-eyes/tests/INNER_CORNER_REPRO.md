# Inner-corner regression (manual visual review)

Ryan reported and circled the v5 Thinking lid ends stopping abruptly at the inner
corners. Before repair, reproduce in the signed v5 lab with:

`adb -s emulator-5554 shell am start -W -n com.boop.animationlab/com.boop.alpha1.BoopDevMenuActivity --es clip thinking --ei freeze_ms 1100`

The screenshot remains local: BOOP-corner-before-v5.png. The existing shader
returns immediately on transparent original pixels and always writes original
alpha, even though it stretches lid RGB. Thus the open lid's end silhouette
cannot move with the skin. This is the failing case before implementation.

Repeat the same pose after repair, then inspect both winks, partial/full blinks,
open/reset and reopening at slow and normal speed. The neutral master and all
catalogue/timing bytes must remain identical. No automated screenshot/golden,
pixel or appearance acceptance tests: Ryan judges the new preview. Existing
nonvisual state, build, signature and integrity checks remain required.
