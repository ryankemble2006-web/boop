# BOOP Wall handoff — 2026-09-07

Owner: Wall application; separate from com.boop.launcher and Shield.
Working physical code checkpoint: 595e1da, version 29 / 0.4.9-alpha6.5.6-wall,
package com.boop.alpha1. Existing signed build 33992704568 and physical Pixel 7 Pro
acceptance are recorded in BOOP_STATUS.md. **Do not replace that checkpoint.**

## Current eye-colour experiment

Active experiment branch: `boop-wall-eye-hue-wip`.
It was forked exactly from live `boop-wall-resurrection` head
`3a702f89b7f317649d267f25c34c6c9655edcff8` (Wall v30 launcher-swipe candidate).
No resurrection/free-chat/relay lineage was blindly merged and the protected
physical Wall checkpoint remains untouched.

Candidate identity: versionCode 31, versionName `0.4.11-wall-eye-hue`, package
`com.boop.alpha1`.

The only user-facing addition is one `Eye colour` hue slider inserted alongside
the existing voice settings. It spans 0–359 degrees. The accepted cyan/blue
appearance is anchored at 190 degrees. At exactly 190 the render path installs
**no colour filter**, so the default still draws the original `boop_eyes` bitmap
through the pre-existing Paint path with no pixel colour transformation.

Non-default hues use a hue-rotation `ColorMatrixColorFilter` on the same shared
Paint already used to draw both eyes. No replacement artwork is introduced.
The same filter therefore follows portrait, normal landscape and shake draw
paths without changing eye source crops, geometry, layout or transforms.
There is no mouth and the black background remains unchanged.

The slider previews live while moving and writes only `hue_degrees` to Android
SharedPreferences file `boop_eyes`. `BoopFaceView` reloads that preference in its
constructor, so the selected hue is restored after app/process restart. No RGB,
brightness, saturation, opacity, effect or theme controls were added.

Runtime source protection deliberately keeps `MainActivity.java`, voice routing,
wake behavior, gesture helpers, layout, shake motion and wake assets byte-for-byte
at the v30 base. A materialization patch inserts exactly one hue settings control
before the existing Done button. `BoopFaceView` differs from v30 only by loading
the saved hue and applying the colour filter to its existing Paint.

## Verification

GitHub Actions run `34089134515` is green for code commit
`fc7391740c2d4e04b8fd357bcfe93dcb83e120b8`.

Passed gates include:
- protected v30 runtime-source scope guard;
- 125 Python source/regression tests, including thinking, wake/sleep lifecycle,
  voice settings, shake puppet, Member Berry and launcher-swipe integration;
- hue harness at default 190 plus representative orange 30, green 120,
  pink 330, purple 270 and cyan 180;
- existing launcher-swipe, shake-detector and shake-eye-motion Java harnesses;
- bridge regression and Android unit tests;
- materialized-app check proving one slider, original `R.drawable.boop_eyes`,
  shared Paint tint path and no mouth;
- signed Android build, package/version inspection, ZIP integrity and stable
  signer verification.

Signed candidate from that run:
- APK SHA-256: `39d91cd46cfbff815abf4ec4681f5fb3a3be971d6cec6f1ccf2e3bb9398a028d`
- signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- artifact: `BOOP-Wall-Eye-Hue-v31`

CI verifies the save/load/restart wiring and preserves the existing animation and
gesture code, but **physical acceptance is still pending**. On the Wall Pixel,
verify default blue visually, drag through orange/pink/green, force-stop/restart
with a non-default hue, and exercise wake/sleep, thinking, shake, Member Berry,
tap/hold and Wall-to-Launcher swipe. Do not promote or move the physical
checkpoint until that device pass is green.

## Earlier Wall-to-Launcher handoff

The separate laptop task's original uncommitted five-file left-swipe draft was
preserved on `boop-wall-launcher-handoff-wip`. It is not the authority for this
experiment. The reviewed swipe was promoted into the resurrection line as v30.
It opens separately installed `com.boop.launcher` only after a deliberate
single-finger left swipe of at least 96dp with 1.5x horizontal confidence.
Movement cancels the hidden Member Berry hold; deliberate swipe is consumed
before tap-to-speak. Its emulator evidence remains valid, but physical acceptance
was still pending when this hue branch was created.
