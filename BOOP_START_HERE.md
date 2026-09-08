# BOOP — start here

Updated 2026-09-08. Repository: [ryankemble2006-web/boop](https://github.com/ryankemble2006-web/boop).

## Canonical app branch and explicit standalone exception

**Normal BOOP app development starts on `boop-unified`.**

Ryan chose one APK/one branch after separate Wall, Launcher and Shield lineages became operationally confusing. The unified AIO keeps package `com.boop.alpha1` and the permanent BOOP signer.

**Explicit current exception:** the clean Nvidia Shield HOME replacement is deliberately being developed and physically tested as a standalone app first. It belongs to `boop-shield-clean-launcher`, package `com.boop.shieldhome`, and must not be merged into or used to update `com.boop.alpha1` until Ryan explicitly approves the later merge after Shield hardware testing.

| Work | Branch | State |
| --- | --- | --- |
| Unified BOOP AIO: Wall + phone Launcher + existing Shield body | [boop-unified](https://github.com/ryankemble2006-web/boop/tree/boop-unified) | Canonical AIO lineage, package `com.boop.alpha1`. Read `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, `BOOP_UNIFIED_MEMORY.md`, `unified/SOURCE_HEADS.md`. |
| Shield clean HOME standalone experiment | [boop-shield-clean-launcher](https://github.com/ryankemble2006-web/boop/tree/boop-shield-clean-launcher) | Standalone package `com.boop.shieldhome`; green signed v1 `0.1.0-standalone`; physical Shield acceptance required before later merge into AIO. Read that branch's `SESSION_HANDOFF.md` and `BOOP_STATUS.md`. |
| Cross-project rules/context | main | Read `AGENTS.md`, `BOOP_CONTEXT.md`, this file. Main is the context hub, not the built app. |

## Unified routing contract

One AIO package, `com.boop.alpha1`, contains the established unified bodies:

- Android TV / Leanback / television mode -> existing Shield BOOP body.
- Pixel 7 Pro -> Wall body.
- Other handheld Android devices, including Pixel 10 Pro XL -> Launcher body.
- An internal persistent override exists for recovery/debugging; normal use is automatic.

The standalone clean Shield HOME is **not** an internal AIO route at this stage. Current unified `ShieldEntryRoute` keeps Shield HOME and ordinary Shield launches on `com.boop.shieldoverlay.MainActivity`. The standalone package becomes a HOME candidate only when separately installed and explicitly selected through Android's supported HOME chooser.

Wall remains the AIO app core so the unified APK keeps Wall's package/signing lineage. Launcher and existing Shield body are compiled as internal modules. Wall-to-Launcher and Launcher-to-Wall are internal activity transitions rather than separate-package launches.

Initial unified build receipt:
- build head `bb4797de5005952d0d27a6647ea17c15781b76f7`;
- GitHub Actions run `34104002238`;
- artifact `BOOP-Unified`, ID `10011710184`;
- APK SHA-256 `62ccac0b767fc7005bfeb0eae013f0bad42ad7db7eeeecf054ad42da949f9aba`.

CI/signer green is not physical green. Do not mark unified BOOP or the standalone Shield launcher physically accepted until Ryan tests the relevant real device.

## Current standalone Shield clean launcher receipt

- Branch: `boop-shield-clean-launcher`
- Package: `com.boop.shieldhome`
- Green build head: `d6e775de68f0f19661736abff6c0432e25320196`
- Version: 1 / `0.1.0-standalone`
- Workflow: `34217924617` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10052578743`
- APK SHA-256: `374d86419abbc3aca367f79d2a13e36667ab41e8f975dc3f9b1b12f89ca26c68`

The locked product rule is: **remove the crap, preserve Shield behavior**. Physical acceptance must preserve double-tap Home -> Recent Apps/task switcher, Back, volume/CEC, Nvidia/Android Settings, system remote shortcuts, app switching and system animations. Stock launcher remains installed as recovery.

## Exact source heads used for first unification

Fetched live immediately before initial integration:

- Wall: `boop-wall-native-chat-eye-hue@a28364f98fba1b3a5dbab7e66075c0fb166e08e3`.
- Launcher: `boop-launcher-alpha2@953ad6d5fe48df104a1f74bdcf4b448f5a6d04f2`.
- Shield puppet: `boop-shield-fullscreen-deezer-wip@1a487a4aaecd098d6854905ee64d60253ad6b4b7`.

The exact receipt also lives in `boop-unified/unified/SOURCE_HEADS.md`.

## Historical/reference branches

Old branches are deliberately retained for rollback, provenance and comparison. They are not the normal starting point for new features after unification unless Ryan explicitly asks to work on a historical lineage.

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

The AIO APK keeps package `com.boop.alpha1`. The standalone clean Shield launcher uses `com.boop.shieldhome`, so it installs separately and does not overwrite AIO. Its private state and HOME selection are also separate. A later merge is an intentional source/product merge after physical acceptance, not an Android in-place update from the standalone package.

Existing historical standalone Launcher (`com.boop.launcher`) and Shield (`com.boop.shieldoverlay`) packages remain different from AIO as well; their private app data and special-access grants do not automatically transfer to `com.boop.alpha1`.

## Release discipline

- AIO remains the canonical BOOP product lineage on `boop-unified`.
- The user-approved `boop-shield-clean-launcher` branch is a temporary standalone validation lane, not a second AIO lineage.
- One intentional functional change per version whenever practical.
- Physical acceptance creates the rollback checkpoint: exact Git commit/tag + workflow run + signed artifact + physical result.
- If a candidate breaks, return to the exact last accepted checkpoint/artifact. Never guess from a filename or merely choose the previous version number.
- GitHub is the archive. After a replacement build is accepted, deployment folders keep current signed APKs and optionally one last-good APK; remove superseded local clutter.

## Starting any BOOP task

1. Read `main/AGENTS.md`, `main/BOOP_CONTEXT.md`, and this map.
2. For normal AIO work fetch the live `boop-unified` head and read its `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, `BOOP_UNIFIED_MEMORY.md` and `unified/SOURCE_HEADS.md`.
3. For the clean Shield HOME experiment use live `boop-shield-clean-launcher`, read its handoff/status, and keep package `com.boop.shieldhome` separate until Ryan explicitly approves merge.
4. Preserve concurrent/dirty work and fetch again before pushing. No force pushes or silent overwrites.
5. Current user instructions and fresh physical-device evidence override stale dated notes.
6. CI-green, signed and physically accepted are separate verification levels.

When Ryan says `update memory`, treat it as documentation synchronization only unless he separately asks for code changes.
