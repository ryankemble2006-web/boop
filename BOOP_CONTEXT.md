# BOOP shared context

## Latest shared eye decision, 2026-09-08

Ryan approved the final `glossy_cartoon_eyes_with_black_eyelids.png` as BOOP's
permanent default across phone/Wall, Shield and all animation work. The blue/cyan
eyeLID accent lines have been blackened; the blue/cyan IRIS remains the default.
All animations must take this same form. Accessories come later as separate
additions, not regenerated versions of the base character. This explicit approval
supersedes older eye-bitmap locks below only for this exact master. No further
redesign, regeneration per pose or return to the old torn-alpha artwork.

Read `BOOP_EYES_MASTER.md` on `boop-unified` for the exact asset and integration
state. Master: 1774 x 887 RGBA, 936803 bytes; SHA-256
`ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`.
Ryan has backed it up and explicitly requests replacement on phone and Shield.
Preserve its supplied alpha and proportions, accepted iris-only colour controls,
existing headphones/puppetry and locked five-digit yellow hands. Do not feed the
new master through brightness-threshold/flood-fill transparency reconstruction.

Ryan also clarified that blink had been turned off by him and is working.
Preserve the working blink implementation, timing/curve/delay and motion/power
safeguards. Do not repair a non-fault or change system animation settings.

Approval and local backup are not APK integration: the PNG transfer into GitHub
is still pending at this documentation checkpoint. The current chat connector
has no mounted local-file upload argument, and direct container GitHub access
failed. Ryan was asked to add the saved PNG to the root of `boop-unified`; the
branch handoff owns subsequent transfer/build/physical evidence. No app-code,
workflow, signing, permission, device or Windows-synchronization change is claimed.

## Earlier shared context and historical receipts

Reconciled 2026-09-07. Current user instructions and fresh device evidence win.
Use BOOP_START_HERE.md for branch routing. Normal app development now belongs on
`boop-unified`; older app branches are retained for rollback/reference.

## Identity and decisions

BOOP began as a useful smart speaker in a cute, ownable plastic robot. Pixel Wall,
Shield and Launcher are different bodies of the same BOOP, not different products.
BOOP is an honest expressive puppet, not a claim of sentience or emotional
dependence. Blue/cyan eyes, humour, manners, accessibility and repairability matter.

Home Assistant is the local authority. Immediate house/media control remains local.
Ordinary open-ended conversation may use the selected assistant route. No direct
OpenAI provider secret belongs in Android. Never invent entity IDs or quietly
control a similarly named device in another room.

Automations, scripts and scenes are called **Routines** in user-facing UI while
execution semantics remain distinct. Voice routine creation requires an explicit
creation request, a plain-English proposal and confirmation. Timed voice routines
remain removed pending redesign.

## Wall chat and eye contracts retained

OpenCode credit exhaustion must not disable local house/media commands. Holding the
eyes for three seconds opens Chat mode choices; local processing always runs first,
and only a genuine NO_MATCH may use a conversation route. Device/auth/offline
failures must not become web questions.

Native Chat remains conversation-only and reuses existing BOOP TTS/follow-up/
puppetry on successful replies. Provider keys stay outside Android. Relay tokens in
configured APKs are extractable and must be treated accordingly; public repository
CI must not publish private credentials.

The v38 local eye-colour intent/slider UX was physically accepted and must not
regress. Default cyan/blue is 190 degrees with no ColorFilter; non-default hues tint
the existing shared `boop_eyes` paint. No mouth, replacement eye artwork, RGB
channels, brightness/saturation/opacity controls, themes or effects.

Ryan physically reported the v40 landscape geometry as much better because BOOP
now reads as the same character turning around rather than changing face. Preserve
the rule that orientation changes framing, not character geometry.

## Unified BOOP, implemented 2026-09-07

Ryan chose one canonical APK/branch because maintaining separate Wall, Launcher and
Shield build lineages and many local filenames had become too confusing. The first
unified candidate is now implemented on **`boop-unified`**.

Exact live source heads fetched at integration time:
- Wall `boop-wall-native-chat-eye-hue@a28364f98fba1b3a5dbab7e66075c0fb166e08e3`;
- Launcher `boop-launcher-alpha2@953ad6d5fe48df104a1f74bdcf4b448f5a6d04f2`;
- Shield puppet `boop-shield-fullscreen-deezer-wip@1a487a4aaecd098d6854905ee64d60253ad6b4b7`.

The unified package is `com.boop.alpha1`, retaining Wall's permanent BOOP signer
for the cleanest update path. Wall is the app core; latest Launcher and Shield
sources are compiled as internal modules. One exported `UnifiedEntryActivity`
selects the body automatically:
- Android TV / Leanback / television UI mode -> Shield;
- Pixel 7 Pro -> Wall;
- other handheld Android, including Pixel 10 Pro XL -> Launcher.
An internal persistent override exists for recovery/debugging.

Wall-to-Launcher and Launcher-to-Wall are internal activity hops in one APK. The
unified build reproduces Shield's own approved eye-artwork materialization before
Shield compile/tests.

First unified candidate: versionCode 41 / `1.0.0-unified-alpha1`.
Green build source head: `bb4797de5005952d0d27a6647ea17c15781b76f7`.
Workflow run: `34104002238`.
Artifact: `BOOP-Unified`, ID `10011710184`.
APK SHA-256: `62ccac0b767fc7005bfeb0eae013f0bad42ad7db7eeeecf054ad42da949f9aba`.

CI passed pinned-source checks, preserved Wall guards, latest Launcher tests/lint,
latest Shield tests, unified Android tests, stable signing, package/version/entry
validation, manifest presence for all three bodies and APK integrity. This is
**CI/signer green, not yet physically accepted as a unified app**.

## Migration boundary

Keeping `com.boop.alpha1` means a current Wall install has the cleanest in-place
upgrade path. Existing standalone Launcher (`com.boop.launcher`) and Shield
(`com.boop.shieldoverlay`) are distinct Android packages, so their private state,
HOME/default-launcher selection and Shield special-access grants cannot migrate
automatically to the unified package. Expect one-time setup/reselection during the
first physical unified tests on those bodies. Preserve historical branches and
accepted artifacts until unified BOOP is physically accepted.

## Canonical release discipline

- Normal BOOP app work now starts from the live `boop-unified` head.
- One intentional functional change per version whenever practical.
- Physical acceptance creates the rollback point: exact Git commit/tag, workflow
  run, signed artifact and physical result.
- If a new candidate breaks, return to the exact last physically accepted
  checkpoint/artifact. Do not guess from version labels, timestamps or filenames.
- GitHub history/artifacts are the archive. Deployment folders are working
  surfaces only; after a replacement is accepted, keep current signed `BOOP.apk`
  and optionally one clearly identified last-good APK, then remove superseded
  local clutter.
- Historical Wall/Launcher/Shield branches remain provenance/rollback references;
  do not delete or repoint old checkpoints.

## Current evidence, not blanket release claims

- Protected Wall checkpoint `checkpoint-boop-wall-595e1da` remains historical
  physical rollback evidence.
- v38 eye-colour UX is physically accepted.
- v40 landscape geometry received positive physical confirmation from Ryan.
- Shield corner-H1 puppetry/placement has older physical acceptance evidence on
  `boop-shield-media-puppetry`; the latest full-screen/friendly-access Shield input
  used by unified BOOP remains lineage-specific and requires physical verification.
- Unified v41 is signed/CI-green but requires real Wall, handheld and Shield tests.

CI-green, locally built, physically tested and design-only are different states.
Record them separately. Never manufacture a physical checkpoint from a CI pass.

## Cross-device working agreement

GitHub is the code/context handoff. Before edits fetch/check `boop-unified` and
main; before stopping record results and publish reviewed scoped work. No blind
overwrites or force pushes. Shared context is not automatic chat-history
replication. When Ryan says `update memory`, reconcile and publish documentation
only unless he explicitly also requests implementation.

## Official yellow hands, locked across BOOP on 2026-09-07

Ryan approved the exact side-by-side, hands-only yellow plush pair as BOOP's
official hands everywhere: unified Wall, Launcher, Shield and all animation work.
Read `BOOP_YELLOW_HANDS.md`. Preserve five digits per hand (four fingers plus a
thumb), the approved material/proportions/rounded cuffs, floating hands without
arms, and genuine PNG transparency. Posing is allowed; redesign is not. The
established eyes/headphones remain unchanged. The Blah Blah Blah talking-hand
idea uses fingers and thumb, not animal heads, extra eyes, tongues or mouths.

Exact master: `boop-yellow-hands-approved.png`, 1774 x 887 RGBA, 1541931 bytes.
SHA-256: `74e3b162d8fa750491b9a1577d51d043e1f7fdcc3940cbdf22f742bc58c9f556`.
App reference folder: `unified/assets/boop-yellow-hands/` on `boop-unified`.
Animation reference folder: `animation-lab/shared-assets/boop-yellow-hands/` on
`animation-freddie-mercury`, which remains a separate art lab, not an app lineage.

Documentation and checksum records are published; PNG binary upload is still
pending manual transfer from the supplied checked ZIP. No editable original
layers were supplied. Do not confuse visual approval or a saved note with a PNG
in GitHub, runtime integration, a new APK or physical-device acceptance. This
request changes no app behavior, permissions, signing, workflow or deployment.
