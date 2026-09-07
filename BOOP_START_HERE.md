# BOOP — start here

Updated 2026-09-07. Repository: [ryankemble2006-web/boop](https://github.com/ryankemble2006-web/boop).

## Canonical app branch

**Normal BOOP app development now starts on `boop-unified`.**

Ryan chose one APK/one branch after separate Wall, Launcher and Shield lineages became operationally confusing. The first unified candidate was built from the exact latest live GitHub heads at merge time and keeps the permanent BOOP signer.

| Work | Branch | State |
| --- | --- | --- |
| Unified BOOP — Wall + Launcher + Shield | [boop-unified](https://github.com/ryankemble2006-web/boop/tree/boop-unified) | Canonical candidate, versionCode 41 / `1.0.0-unified-alpha1`; CI/signer green, physical unified acceptance pending. Read `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, `BOOP_UNIFIED_MEMORY.md`, `unified/SOURCE_HEADS.md`. |
| Cross-project rules/context | main | Read `AGENTS.md`, `BOOP_CONTEXT.md`, this file. Main is the context hub, not the built app. |

## Unified routing contract

One package, `com.boop.alpha1`, contains all three bodies:

- Android TV / Leanback / television mode -> Shield body.
- Pixel 7 Pro -> Wall body.
- Other handheld Android devices, including Pixel 10 Pro XL -> Launcher body.
- An internal persistent override exists for recovery/debugging; normal use is automatic.

Wall remains the final app core so the unified APK keeps Wall's package/signing lineage. Launcher and Shield are compiled as internal modules. Wall-to-Launcher and Launcher-to-Wall are internal activity transitions rather than separate-package launches.

Initial unified build receipt:
- build head `bb4797de5005952d0d27a6647ea17c15781b76f7`;
- GitHub Actions run `34104002238`;
- artifact `BOOP-Unified`, ID `10011710184`;
- APK SHA-256 `62ccac0b767fc7005bfeb0eae013f0bad42ad7db7eeeecf054ad42da949f9aba`.

CI/signer green is not physical green. Do not mark unified BOOP accepted until Ryan tests the relevant real devices.

## Exact source heads used for first unification

Fetched live immediately before integration:

- Wall: `boop-wall-native-chat-eye-hue@a28364f98fba1b3a5dbab7e66075c0fb166e08e3`.
- Launcher: `boop-launcher-alpha2@953ad6d5fe48df104a1f74bdcf4b448f5a6d04f2`.
- Shield puppet: `boop-shield-fullscreen-deezer-wip@1a487a4aaecd098d6854905ee64d60253ad6b4b7`.

The exact receipt also lives in `boop-unified/unified/SOURCE_HEADS.md`.

## Historical/reference branches

The old branches are deliberately retained for rollback, provenance and comparison. They are **not** the normal starting point for new features after unification unless Ryan explicitly asks to work on a historical lineage.

| Historical work | Branch | Important retained evidence |
| --- | --- | --- |
| Wall Native Chat + hue + sleepy close + landscape geometry | `boop-wall-native-chat-eye-hue` | Source input for unified build. v38 eye-colour UX physically accepted; Ryan physically preferred the v40 landscape geometry. |
| Wall preserved resurrection checkpoint | `boop-wall-resurrection` | Protected physical Wall checkpoint `595e1da`; keep as rollback/reference. |
| Launcher Alpha 2 | `boop-launcher-alpha2` | Source input for unified build. 0.3.7 widget picker physically reported better, later polish deferred. |
| Shield full-screen Deezer puppet | `boop-shield-fullscreen-deezer-wip` | Source input for unified build; branch-level CI green, physical acceptance lineage-specific. |
| Shield corner-H1 accepted puppetry | `boop-shield-media-puppetry` | Physically accepted older Shield puppetry/placement evidence; preserve. |
| Wall Free Chat / Native Chat history | `boop-wall-free-chat-wip` | Preserve concurrent Native Chat/hue history. |
| Isolated relay review | `boop-relay-reviewed-v34` | Separate reviewed relay lineage; historical/reference. |
| Older Wall hue experiment | `boop-wall-eye-hue-wip` | v31 historical experiment only. |
| Wall-to-Launcher draft | `boop-wall-launcher-handoff-wip` | Historical draft/reference. |
| Routine-authoring research | `boop-routine-authoring-v1` | Capability/design evidence only. |
| Older Shield Home | `boop-shield-home-implementation` | Historical/reference. |

Do not delete, force-update or repoint old checkpoints merely because unified BOOP exists.

## Migration boundary

The unified APK keeps package `com.boop.alpha1`, so current Wall installs have the cleanest in-place update path. Existing standalone Launcher (`com.boop.launcher`) and Shield (`com.boop.shieldoverlay`) are different Android packages. Their private app data, HOME/default-launcher selection and Shield special-access grants do not automatically transfer to `com.boop.alpha1`; expect one-time setup/reselection when unified BOOP is first physically tested on those devices.

## Release discipline

- One canonical APK lineage on `boop-unified`.
- One intentional functional change per version whenever practical.
- Physical acceptance creates the rollback checkpoint: exact Git commit/tag + workflow run + signed artifact + physical result.
- If a candidate breaks, return to the exact last accepted checkpoint/artifact. Never guess from a filename or merely choose the previous version number.
- GitHub is the archive. After a replacement build is accepted, deployment folders keep current signed `BOOP.apk` and optionally one last-good APK; remove superseded local clutter.

## Starting any BOOP task

1. Read `main/AGENTS.md`, `main/BOOP_CONTEXT.md`, and this map.
2. For normal app work fetch the live `boop-unified` head and read its `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, `BOOP_UNIFIED_MEMORY.md` and `unified/SOURCE_HEADS.md`.
3. Preserve concurrent/dirty work and fetch again before pushing. No force pushes or silent overwrites.
4. Current user instructions and fresh physical-device evidence override stale dated notes.
5. CI-green, signed and physically accepted are separate verification levels.

When Ryan says `update memory`, treat it as documentation synchronization only unless he separately asks for code changes.
