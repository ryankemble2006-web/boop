# BOOP unified handoff

Updated 2026-09-07 after Ryan requested a live build recheck.
Owner: `boop-unified`. Package: `com.boop.alpha1`. Permanent BOOP signer unchanged.
Main owns shared contracts; this handoff owns current implementation evidence.

## Current result: signed candidate, known wake-name defect

Latest built code: `6cd9c67a03c639a20acde892e2d57186652e13d5`.
GitHub Actions run `34125882296` completed successfully at 13:21 UTC.
Artifact `BOOP-Unified`, ID `10020439707`.
APK SHA-256: `603e72b6f3a83eca429e90a11559454ca2d9c140eee69bff9bcd537d9a276a4e`.
Artifact ZIP SHA-256: `455cd406beb111ac6d5d1d974b20bd54735a65222aa702612476929efd4ad285`.
APK size: 142032549 bytes. Version metadata remains code 43 / `1.1.0-unified-dock-mirror-shield-settings`; use exact build commit and checksum, not that reused version label.

This is a signed Shield-settings test candidate, NOT completion of the requested wake-name feature. The custom-name tokenizer in the built code incorrectly applies BPE merging to the pinned UNIGRAM model. Some custom phrases therefore receive incorrect token sequences. Original BOOP keywords remain unchanged and available. Do not advertise custom acoustic waking as verified.

A correction with real-model regression tests was created as commit `1c63dac26e2fb0f84a1bf4ee1a445869aff31462`, parent `6cd9c67a03c639a20acde892e2d57186652e13d5`, tree `58dd78003f34503850c79a91bce6af7677b4f5ab`. The connector blocked the requested branch update because it could not determine the request's safety status. That correction is NOT on the branch and NOT in the APK. Do not report it as pushed, built or fully CI-tested. This documentation-only update does not integrate that blocked application change.

## What the recheck repaired and verified

- Run `34122652048` at `33549400992ae844e2940a55d273b05cef3f4e62` failed Shield compilation: missing static imports for MATCH_PARENT and WRAP_CONTENT.
- Commit `d1a91be4e4bf854e60e09adb350f3fe563266c4c` added only those imports. Its run passed compilation/tests and signed assembly but was cancelled during emulator smoke, before artifact publication.
- Commit `6cd9c67a03c639a20acde892e2d57186652e13d5` fixes custom-call removal before command routing, reusing the generated natural phrases and preserving the original BOOP normalizer overload. Added mirror, rename, multiword, default/fallback and word-boundary tests.
- Run `34125882296` passed preserved Wall source guards, materialization, Launcher tests/lint, Shield unit tests, unified unit tests, permanent signing, signed assembly, Shield-entry emulator smoke, package/signer checks and artifact upload.
- Downloaded artifact ZIP and APK checksums and built-commit receipt matched. APK archive integrity passed. DEX contains `BOOP SETTINGS`, `HOME ASSISTANT`, `BOOP's name`, TvSettingsView and BoopWakeNameStore. Original BOOP keywords are byte-identical to the previously delivered v43 artifact. These checks do not prove actual settings navigation or acoustic behavior.
- Local reference comparison of the unshipped tokenizer repair matched SentencePiece 0.2.1 on 5032 cases. The existing tokenizer disagreed on 23 of the initial 32 name/phrase cases. See `docs/BOOP-UNIFIED-WAKE-NAME-RECHECK.md`.

## Physical evidence and next safe step

Last physically accepted unified rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`.
Ryan reported that the Shield UI appeared unchanged after installing candidate `950611d`; do not promote it or the newer candidate to physically accepted. Inspection of the exact old APK confirmed its newer settings class was present. The reason for the unchanged real screen has not been proven. Verify the actually launched package/activity: unified BOOP is `com.boop.alpha1`, distinct from historical standalone `com.boop.shieldoverlay`. Do not uninstall apps, transfer private data, change HOME defaults or grant special access automatically.

The new candidate's Settings page should visibly identify `BOOP SETTINGS`, `HOME ASSISTANT` and `BOOP's name`. Real Shield remote focus/scroll/activation and actual HA room inventory still need device tests. Existing CI checks launch the Shield body; they do not navigate and visually verify Settings.

After the publication block is resolved through an authorized path, inspect the correction commit, reconcile any new branch changes, run full unified CI and verify its exact artifact before claiming the complete wake-name request finished. Do not blindly repoint a branch or bypass a tool restriction. Physical tests must cover custom phrase accuracy, persistence, verbal rename/reset, BOOP fallback, microphone release/re-arm and thermal behavior. Undocked handheld wake remains disarmed; test voice wake while wirelessly docked.

## Durable scope and preserved history

BOOP forever: user-selected spoken call name only, one `boop_voice/wake_name` preference, BOOP permanent fallback. No package, branding, class, HA, pairing, signer or repository identity changes. No replacement wake engine, cloud dependency or microphone lifecycle change in this recheck.

Dock/mirror, deferred sensor rails, local-first HA, protected artwork, separate animation studies and original unification/migration receipts remain as recorded in `docs/history/unified-v43/SESSION_HANDOFF.md`, `docs/history/unified-v43/BOOP_CONTEXT.md`, `BOOP_UNIFIED_MEMORY.md` and `unified/SOURCE_HEADS.md`. The original root documents are preserved verbatim in that history folder. Current user evidence and this handoff override their dated candidate-status wording.

No laptop checkout synchronization, user-device deployment or background monitoring was verified from this chat.
