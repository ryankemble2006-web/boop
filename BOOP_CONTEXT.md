# BOOP shared context

## Latest shared eye decision, 2026-09-09

Ryan re-confirmed the exact glossy `boopApprovedEyes.png` / original `glossy_cartoon_eyes_with_black_eyelids.png` as BOOP's perfect permanent default across phone/Wall, tablet, Shield, Launcher references and all animation work. The blue/cyan iris remains the default and the lids remain glossy black. Accessories are separate additions, not regenerated versions of the base character.

Canonical master: `boop-unified/unified/assets/boop-eyes/boopApprovedEyes.png`.

Exact identity:
- 1774 x 887 RGBA, supplied alpha preserved;
- 936,803 bytes;
- SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`;
- Git blob `f95375356b20297fa2b27ab8887f65d4cce5c7fd`.

The exact PNG is present in GitHub. Byte-identical copies are deliberately retained in current Unified, Shield, Launcher and animation source/reference areas with `DO_NOT_TOUCH_BOOP_EYES.md` and checksum receipts. Never edit, flatten, recompress, regenerate, recreate, crop, resize on disk, recolor/recolour, threshold/flood-fill, reconstruct transparency or substitute this master. Runtime posing, scaling, masking, blinking and animation must remain non-destructive to the source bytes. Replacing the permanent default requires Ryan's explicit approval of a new exact master and recorded identity.

User eye-colour control may affect the iris at runtime only, without rewriting the master or tinting the sclera, pupil, highlights or black lids.

Source preservation is not the same as renderer uniformity. Ryan separately observed on 2026-09-09 that Shield can present flatter/cartoon-coloured eyes rather than the glossy default. That renderer mismatch remains an implementation task. Do not solve it by making a new eye image.

Ryan also clarified that blink had been turned off by him and is working. Preserve the working blink implementation, timing/curve/delay and motion/power safeguards. Do not repair a non-fault or change system animation settings.

## Earlier shared context and historical receipts

Reconciled 2026-09-07. Current user instructions and fresh device evidence win. Use BOOP_START_HERE.md for branch routing. Normal app development now belongs on `boop-unified`; older app branches are retained for rollback/reference.

## Identity and decisions

BOOP began as a useful smart speaker in a cute, ownable plastic robot. Pixel Wall, Shield and Launcher are different bodies of the same BOOP, not different products. BOOP is an honest expressive puppet, not a claim of sentience or emotional dependence. Blue/cyan eyes, humour, manners, accessibility and repairability matter.

Home Assistant is the local authority. Immediate house/media control remains local. Ordinary open-ended conversation may use the selected assistant route. No direct OpenAI provider secret belongs in Android. Never invent entity IDs or quietly control a similarly named device in another room.

Automations, scripts and scenes are called **Routines** in user-facing UI while execution semantics remain distinct. Voice routine creation requires an explicit creation request, a plain-English proposal and confirmation. Timed voice routines remain removed pending redesign.

## Wall chat and eye contracts retained

OpenCode credit exhaustion must not disable local house/media commands. Holding the eyes for three seconds opens Chat mode choices; local processing always runs first, and only a genuine NO_MATCH may use a conversation route. Device/auth/offline failures must not become web questions.

Native Chat remains conversation-only and reuses existing BOOP TTS/follow-up/puppetry on successful replies. Provider keys stay outside Android. Relay tokens in configured APKs are extractable and must be treated accordingly; public repository CI must not publish private credentials.

The v38 local eye-colour intent/slider UX was physically accepted and must not regress. Default cyan/blue is 190 degrees. The permanent glossy master now governs source identity; any runtime colour treatment remains iris-only and non-destructive.

Ryan physically reported the v40 landscape geometry as much better because BOOP now reads as the same character turning around rather than changing face. Preserve the rule that orientation changes framing, not character geometry.

## Unified BOOP, implemented 2026-09-07

Ryan chose one canonical APK/branch because maintaining separate Wall, Launcher and Shield build lineages and many local filenames had become too confusing. The unified candidate is implemented on **`boop-unified`**.

Exact live source heads fetched at first integration time:
- Wall `boop-wall-native-chat-eye-hue@a28364f98fba1b3a5dbab7e66075c0fb166e08e3`;
- Launcher `boop-launcher-alpha2@953ad6d5fe48df104a1f74bdcf4b448f5a6d04f2`;
- Shield puppet `boop-shield-fullscreen-deezer-wip@1a487a4aaecd098d6854905ee64d60253ad6b4b7`.

The unified package is `com.boop.alpha1`, retaining Wall's permanent BOOP signer. Wall is the app core; Launcher and Shield sources are compiled as internal modules. One exported `UnifiedEntryActivity` selects the body automatically. Current profile contract:
- explicit persistent recovery/debug override first;
- Android TV / Leanback / television UI mode -> Shield;
- Pixel 7 Pro -> Wall;
- other non-TV Android with `smallestScreenWidthDp >= 600` -> Wall;
- sub-600dp handheld Android, including Pixel 10 Pro XL -> Launcher.

The 600dp tablet rule was added on 2026-09-09 so the Xiaomi Pad 7 Pro takes the Wall path without a Xiaomi-specific model hardcode. Canonical tablet-compatible app/test head `bd878606809302de1b871e6c62d8ce905346e766` is CI/signer green from workflow `34395085823`, artifact `BOOP-Unified` ID `10121327367`.

Ryan physically tested that exact V70 tablet candidate on the Xiaomi Pad 7 Pro and confirmed correct Wall-body routing, touch operation and local Home Assistant control. Landscape BOOP is currently larger than desired. A later explicit tablet-layout pass may scale BOOP down in the centre so weather can sit on one side and sensor data on the other. Full Pad shakedown remains in progress, so this is positive partial physical evidence rather than blanket acceptance.

Wall-to-Launcher and Launcher-to-Wall are internal activity hops in one APK. The unified build reproduces Shield's approved eye-artwork materialization before Shield compile/tests, but the current renderer-uniformity mismatch remains physically unresolved.

First unified candidate: versionCode 41 / `1.0.0-unified-alpha1`.
Green build source head: `bb4797de5005952d0d27a6647ea17c15781b76f7`.
Workflow run: `34104002238`.
Artifact: `BOOP-Unified`, ID `10011710184`.
APK SHA-256: `62ccac0b767fc7005bfeb0eae013f0bad42ad7db7eeeecf054ad42da949f9aba`.

CI-green, locally built, physically tested and design-only are different states. Never manufacture a physical checkpoint from a CI pass.

## Migration boundary

Keeping `com.boop.alpha1` means a current Wall install has the cleanest in-place upgrade path. Existing standalone Launcher (`com.boop.launcher`) and Shield (`com.boop.shieldoverlay`) are distinct Android packages, so their private state, HOME/default-launcher selection and Shield special-access grants cannot migrate automatically to the unified package. Preserve historical branches and accepted artifacts until unified BOOP is physically accepted.

## Canonical release discipline

- Normal BOOP app work starts from the live `boop-unified` head.
- One intentional functional change per version whenever practical.
- Physical acceptance creates the rollback point: exact Git commit/tag, workflow run, signed artifact and physical result.
- If a new candidate breaks, return to the exact last physically accepted checkpoint/artifact. Do not guess from version labels, timestamps or filenames.
- GitHub history/artifacts are the archive. Deployment folders are working surfaces only.
- Historical Wall/Launcher/Shield branches remain provenance/rollback references; do not delete or repoint old checkpoints.

## Current evidence, not blanket release claims

- Protected Wall checkpoint `checkpoint-boop-wall-595e1da` remains historical physical rollback evidence.
- v38 eye-colour UX is physically accepted.
- v40 landscape geometry received positive physical confirmation from Ryan.
- Shield corner-H1 puppetry/placement has older physical acceptance evidence on `boop-shield-media-puppetry`; latest Shield presentation remains lineage-specific and requires physical verification.
- Xiaomi Pad 7 Pro V70 has positive physical evidence for correct Wall routing, touch and local HA control; further shakedown remains.

## Cross-device working agreement

GitHub is the code/context handoff. Before edits fetch/check `boop-unified` and main; before stopping record results and publish reviewed scoped work. No blind overwrites or force pushes. Shared context is not automatic chat-history replication. When Ryan says `update memory`, reconcile and publish documentation only unless he explicitly also requests implementation.

## Official yellow hands, locked across BOOP on 2026-09-07

Ryan approved the exact side-by-side, hands-only yellow plush pair as BOOP's official hands everywhere: unified Wall, Launcher, Shield and all animation work. Read `BOOP_YELLOW_HANDS.md`. Preserve five digits per hand (four fingers plus a thumb), the approved material/proportions/rounded cuffs, floating hands without arms, and genuine PNG transparency. Posing is allowed; redesign is not. The established eyes/headphones remain unchanged.

Exact master: `boop-yellow-hands-approved.png`, 1774 x 887 RGBA, 1541931 bytes. SHA-256: `74e3b162d8fa750491b9a1577d51d043e1f7fdcc3940cbdf22f742bc58c9f556`.
App reference folder: `unified/assets/boop-yellow-hands/` on `boop-unified`.
Animation reference folder: `animation-lab/shared-assets/boop-yellow-hands/` on `animation-freddie-mercury`, which remains a separate art lab, not an app lineage.
