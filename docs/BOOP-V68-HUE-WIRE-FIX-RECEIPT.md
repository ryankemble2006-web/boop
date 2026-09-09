# BOOP Unified v68 hue wire fix receipt

Updated 2026-09-09.

## Physical report that opened this fix

Ryan physically tested signed v67 and reported that the eye hue control was not working. v67 must therefore not be treated as physically accepted.

## Root cause

The v67 procedural-eye transplant correctly replaced `BoopFaceView.setEyeHueDegrees()` with a procedural hue setter, but `scripts/patch-unified-shield-dashboard.py` later invoked the legacy bitmap-era `scripts/patch-unified-iris-cache.py` again. `materialize-unified.sh` runs the Shield dashboard patch after the v65 procedural-eye stages and later reruns it against the copied Shield tree, so that late helper invocation silently replaced the procedural setter with the obsolete bitmap-tint setter.

The rendered eyes are procedural in v67, so changing a bitmap tint no longer changes the displayed iris. This exactly matches the physical symptom.

## Test-first fix

Regression test commit:

`8e4527e99f835acc4bf4000d2fa9a9c53a32e9d7`

Workflow `34313965830` failed at the non-visual integration-contract step as expected because `patch-unified-shield-dashboard.py` still referenced the legacy iris-cache patch.

Production fix:

- removed the late legacy iris-cache invocation from `scripts/patch-unified-shield-dashboard.py`;
- kept the historical baseline iris-cache pass in `scripts/materialize-android.sh`, where it runs before the procedural-eye replacement;
- added a non-visual regression contract preventing the late Shield dashboard pass from restoring the legacy bitmap setter;
- no eye geometry, sclera feathering, artwork, blink, notification behavior, wake behavior, Home behavior or signer/package identity was changed.

## Signed v68 evidence

Built code head:

`91e562754be31745a5ee76538ebef50e6c6a9b2d`

Release identity:

- versionCode `68`;
- versionName `1.2.22-unified-hue-wire-fix`;
- workflow `34314023763` SUCCESS;
- artifact `BOOP-Unified`, ID `10089480166`;
- artifact ZIP digest `sha256:f1b546f52800260a878940db0b8f074b1a8a07fec9df4370b2843acfc59c1699`;
- APK SHA-256 `571f0a501e5a3df921fc1e521ae235810860df3f493c223af491f974cd30e352`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Fresh artifact verification:

- non-visual integration contracts PASS;
- canonical materialization PASS;
- notification presenter contracts PASS;
- seamless wake handoff PASS;
- Launcher preservation PASS;
- Shield focused functional tests 58/58, zero failures/errors/skips;
- Unified focused functional tests 136/136, zero failures/errors/skips;
- signed APK assembly PASS;
- package/version/permanent-signer/APK ZIP integrity PASS;
- artifact upload PASS.

The extracted APK was re-hashed locally from the downloaded GitHub artifact and matched the CI receipt exactly: `571f0a501e5a3df921fc1e521ae235810860df3f493c223af491f974cd30e352`.

## Acceptance boundary

GitHub performed no visual checking. v68 is CI/signer green only. Ryan must physically confirm that moving the eye hue slider changes the procedural iris live while leaving sclera, pupil, catchlights and black lids unchanged. Do not create a v68 physical checkpoint until that exact signed APK is explicitly accepted on real hardware.
