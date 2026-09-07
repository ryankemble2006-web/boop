# BOOP shared context

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
