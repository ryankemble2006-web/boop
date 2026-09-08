# BOOP portable notification / lock-screen animation reference

Saved 2026-09-08 for later animation work. This is **reference-only** and changes no app code, permissions, lock-screen behaviour, notification access, signing, package, build or deployment.

## Approved visual target

The approved concept is the phone lock-screen composition where BOOP is represented only by the established paired eyes and floating yellow hands. BOOP holds an anonymous application-notification icon/card. No body, face shell, mouth or newly generated mascot is added.

For locked-phone presentation, notification content remains private: no sender name, message text, preview, photo or account detail is shown. The display communicates only that a notification exists, using the app/icon and optional count badge.

## Future animation direction

Animate the approved assets as separate puppet layers rather than generating replacement frames:

- gentle idle float;
- small eye darts and the existing blink language;
- slight hand grip/squeeze or grip adjustment;
- notification icon pop-in / small bounce;
- gaze toward the held icon;
- calmer or stopped motion when the screen settles;
- preserve battery/reduced-motion/lifecycle safeguards when implemented.

Movement, rotation, scaling and carefully controlled deformation are allowed for puppetry. Redesign is not. The approved eyes and five-digit floating hands remain the character source.

## Exact assets

### Eyes

`boopApprovedEyes.png` in this folder is copied by Git object identity from the latest canonical AIO branch `boop-unified` path `unified/assets/boop-eyes/boopApprovedEyes.png`.

- dimensions: 1774 x 887 RGBA
- bytes: 936803
- SHA-256: `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`
- Git blob SHA: `f95375356b20297fa2b27ab8887f65d4cce5c7fd`

Do not regenerate or restyle these eyes.

### Hands supplied in this chat

Reserved destination filename: `boop-yellow-hands-approved.png`.

- dimensions: 1774 x 887 RGBA
- local/current supplied file bytes: 1809990
- SHA-256: `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`
- appearance: user-confirmed approved floating yellow hands, four fingers plus thumb per hand

These exact chat-uploaded binary bytes are **not yet in GitHub** because the connected GitHub actions available in ChatGPT accept text/Git objects but do not expose a local-file binary upload parameter. Do not substitute a regenerated hand image. Upload this exact PNG later and verify the hash above before calling the transfer complete.

The older animation-lab documentation also records the earlier canonical hand master/hash; do not silently overwrite that historical record. This lock-screen reference records the exact current chat-supplied file separately.

### Approved lock-screen concept image supplied by the image generator

Reserved destination filename: `boop-notification-lockscreen-reference.png`.

- dimensions: 941 x 1672 RGB
- local/current generated file SHA-256: `cec1d22b6d3279ee882ef611e3d589a96fb6b3010d19303f6f66ad6968a022c4`

This exact binary is also pending transfer for the same connector limitation. It is a visual target only, not an app screenshot or proof of implemented behaviour.

## Non-negotiable visual rule

Future art/animation work must start from the approved eye master and approved five-digit floating hands. Pose them. Animate them. Do not ask an image model to invent BOOP again.
