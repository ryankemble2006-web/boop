# BOOP permanent eye master

Approved by Ryan on 2026-09-08. This decision supersedes the older eye-source requirement only for this explicitly approved replacement.

## Locked appearance

The final paired-eye PNG with black eyelids and no blue/cyan eyelid accent lines is BOOP's permanent default. Ryan explicitly approved this exact result, said all animations must take this form, and then requested replacement on both phone/Wall and Shield. Future accessories are separate additions to this base. Do not regenerate the character for poses, restore the blue eyelid lines, or substitute an earlier generated image.

Preserve the approved shapes, proportions, spacing, pupils, highlights, shading and supplied alpha. The blue/cyan iris remains the default. Preserve the already accepted user-selectable iris-only colour behaviour without tinting the whites, pupils, highlights or eyelids. Existing headphones, five-digit yellow hands and puppetry are not being redesigned in this replacement pass.

## Exact file identity

- Chat/source filename: `glossy_cartoon_eyes_with_black_eyelids.png`.
- Dimensions: 1774 x 887.
- PNG mode: RGBA; decoded alpha range 0..255.
- File size: 936803 bytes.
- SHA-256: `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`.

The bytes and format above were checked locally. Visual approval came from Ryan, not an automated appearance test. An RGBA header alone is not a silhouette check. Do not apply the rejected brightness/row-span transparency converter to this master; retain its existing alpha unchanged.

## Blink correction

Ryan subsequently reported that he had turned blink off himself and that it is working. The earlier missing-blink report is no longer an unresolved defect. Preserve the current blink code, timing, curve, delay and system-motion/power safeguards. Do not change system animation settings or rewrite blink while replacing the bitmap.

## Integration state

APPROVED MASTER, NOT YET INTEGRATED. The exact PNG is present in the chat sandbox and Ryan has downloaded a backup. This documentation commit does not contain the PNG binary, change either renderer or produce an APK.

The exposed GitHub actions accept text/API content but have no mounted local-file upload argument, and direct GitHub networking from this chat container failed. A Canva asset upload succeeded as a transfer attempt, but returned only metadata/a thumbnail; that is not the original file in GitHub and must not be used as the app texture.

Next transfer: Ryan may upload the saved PNG, with its existing filename, to the root of `boop-unified`. Fetch live HEAD again, verify the exact bytes/hash, and retain a canonical copy under `unified/assets/boop-eyes/` before wiring phone and Shield. Adapt source rectangles to this new image rather than reusing the old portrait-atlas coordinates or guessing alpha from RGB. Preserve the approved character geometry when framing either device.

No image regeneration, new accessories, unrelated repairs, new signer/package, device installation, permission change, or visual/device CI acceptance is authorised by this asset replacement alone.
