# BOOP Animation Lab system-scale POC

Updated 2026-09-13. Branch: `animation-lab-system-scale-poc`.

Goal: prove BOOP's existing coded animations can keep running when Android/Shield UI animation scales are disabled, without changing any existing animation artwork, poses, timing keys or clip definitions.

Source commit `b19318d92d267145c1df3029f5b63c2840ca335e` builds Animation Lab v12, package `com.boop.animationlab`, versionName `0.12-independent-motion-poc`.

GitHub Actions run `34730986443` completed successfully with catalogue/state tests, MotionPolicyTest, Android compile, permanent BOOP signing and APK integrity checks.

Physical Shield test: signed v12 installed successfully. `window_animation_scale`, `transition_animation_scale` and `animator_duration_scale` were all set to `0`. The Thinking loop remained animated; three captures 300 ms apart produced changing frame hashes while the app stayed foreground.

The POC removes Android animator-scale-zero as a BOOP reduced-motion trigger. Power Saver remains allowed to reduce BOOP motion. At 1.0x the BOOP clock delta is unchanged from the previous lab baseline.

Clickable lab controls now expose 0.5x, 1.0x, 1.5x and 2.0x. Physical Shield UI hierarchy verified the 2.0x click updated the live label to `Animation speed 2.0x | Android UI scale 0.0x`, then 1.0x restored it to `Animation speed 1.0x | Android UI scale 0.0x`.

No existing EyeMotion clip definitions, catalogue timing keys, eye master, shader rig, notification hands or sign choreography were changed.

Shield was left with Animation Lab open at 1.0x and Android animation scales at 0.

Next: user visually reviews the behaviour. If accepted, transplant only the independent timing policy + shared speed control concept into Unified before creating more animations.
