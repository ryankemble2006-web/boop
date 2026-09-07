# BOOP Wall Eye Hue Memory

Date: 2026-09-07
Branch: `boop-wall-eye-hue-wip`
Base: `boop-wall-resurrection` at `3a702f89b7f317649d267f25c34c6c9655edcff8`

This is the durable memory for the scoped Wall eye-colour experiment. Read it
with `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, fetched `main:BOOP_CONTEXT.md`, and
the repository startup rules before changing this branch.

## Product decision

BOOP Wall may expose exactly one user-selectable eye hue control beside the
existing voice settings. It is hue only. Do not grow this into RGB channels,
brightness, saturation, opacity, effects, themes, replacement eye artwork, a
mouth, or a background setting.

The accepted cyan/blue eyes remain the default. The slider's absolute hue anchor
is 190 degrees. At the default, the rendering code deliberately returns a null
ColorFilter, leaving the existing `boop_eyes` bitmap pixels untouched.

Non-default hues recolour the existing bitmap through a hue-rotation
`ColorMatrixColorFilter` on the same Paint used by every eye draw path. That is
intentional: both eyes change together while crops, layout, geometry, animations
and gestures remain owned by the existing Wall implementation.

The selected integer hue is stored in SharedPreferences file `boop_eyes` under
key `hue_degrees`. Slider motion saves it and immediately calls the face hue
setter for live preview. A newly constructed `BoopFaceView` reloads the value,
which is the persistence path across app/process restart.

## Scope boundary

This branch is an experiment layered on the reviewed Wall v30 launcher-swipe
head. It does not overwrite or redefine the physically accepted v29 checkpoint
`checkpoint-boop-wall-595e1da`. Do not merge Free Chat, relay, launcher draft,
or other candidate lineages into this branch merely because they exist.

Files intentionally introduced for this feature:
- `source/BoopEyeHueMath.java`
- `source/BoopEyeHue.java`
- `source/BoopEyeHueSettings.java`
- `scripts/patch-wall-eye-hue.py`
- hue-specific tests and workflow

`source/BoopFaceView.java` has only the saved-hue load and shared-Paint filter
hook. `MainActivity.java` remains the v30 source; the one settings control is
inserted deterministically during Android project materialization.

## Verification memory

GitHub Actions run `34089134515` passed the implementation at
`fc7391740c2d4e04b8fd357bcfe93dcb83e120b8`.

Representative hue checks: default 190, orange 30, green 120, pink 330,
purple 270, cyan 180. Existing source regression coverage for wake/sleep,
thinking, shake, Member Berry, voice settings and launcher swipe remained green,
along with Java motion/gesture harnesses, bridge regression and Android unit
tests. The signed candidate uses the permanent BOOP development signer.

Candidate APK from that run:
- versionCode 31
- versionName `0.4.11-wall-eye-hue`
- SHA-256 `39d91cd46cfbff815abf4ec4681f5fb3a3be971d6cec6f1ccf2e3bb9398a028d`
- signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Do not call this a new physical checkpoint yet. The Wall Pixel still needs the
human-visible acceptance pass: default blue, live orange/pink/green, restart
persistence, and wake/sleep/thinking/shake/Member Berry/tap/hold/swipe regression.
