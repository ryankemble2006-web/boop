# BOOP wake-name and Shield recheck receipt

2026-09-07. This document records a partial result, not full feature completion.

## Published and built

Code `6cd9c67a03c639a20acde892e2d57186652e13d5`; run `34125882296`; artifact `10020439707`.
APK SHA-256 `603e72b6f3a83eca429e90a11559454ca2d9c140eee69bff9bcd537d9a276a4e`.
ZIP SHA-256 `455cd406beb111ac6d5d1d974b20bd54735a65222aa702612476929efd4ad285`.
APK 142032549 bytes; code 43 / `1.1.0-unified-dock-mirror-shield-settings`.

GitHub CI passed source guards, Launcher tests/lint, Shield and unified tests, signing, assembly, Shield-entry emulator smoke, package/signer checks and upload. Downloaded ZIP digest, built-commit receipt and APK checksum matched; archive integrity passed. DEX contains BOOP SETTINGS, HOME ASSISTANT, BOOP's name, TvSettingsView and BoopWakeNameStore. Original BOOP keyword asset exactly matches the preceding signed v43 APK. The tokenizer correction below is absent from this binary, as expected.

Run `34122652048` had failed compilation from missing MATCH_PARENT/WRAP_CONTENT imports. Commit `d1a91be4e4bf854e60e09adb350f3fe563266c4c` fixed only those imports; its run passed tests/assembly but was cancelled before smoke completed and before upload. Commit `6cd9c67` then fixed custom-call stripping before command routing and added focused tests while retaining the original BOOP normalization path.

## Known remaining fault and unshipped correction

Actual model audit: run `34122171661`, artifact `10018615090`; audit ZIP SHA-256 `4b0ba705684103730542d3f281e1fc549ded1c12a10c47f5438d98fa2db0606e`.
Model bpe.model SHA-256 `c8a2a0129c4ab8e463164c142f82d25649661b122c8cd0b7aab5c9e80b90ad24`.

The model's TrainerSpec declares UNIGRAM despite its filename. Draft Java code applied greedy BPE, differing from SentencePiece 0.2.1 on 23/32 named phrase fixtures. The correction reads the model type, uses best-path unigram segmentation and native candidate/stored-score precision, and retains BPE behavior for BPE models. Reference comparison passed 5032/5032 cases: 32 named phrases plus 5000 deterministic randomized ASCII inputs. This proves text encoding in that test set, not acoustic wake accuracy or arbitrary language support.

Correction commit `1c63dac26e2fb0f84a1bf4ee1a445869aff31462` has parent `6cd9c67a03c639a20acde892e2d57186652e13d5` and tree `58dd78003f34503850c79a91bce6af7677b4f5ab`. It also adds actual-model digest/token/vocabulary JUnit tests. The connector blocked publishing its branch update because it could not determine the request's safety status. It is not on the branch or in the delivered APK. This separate documentation-only checkpoint preserves the issue without integrating that code. Full CI for the correction remains pending.

## Physical and scope limits

The older rejected-by-appearance `950611d` binary did contain its newer settings class and BOOP Settings heading, with the old subtitle absent. The unchanged real Shield UI report has no proven cause yet. Check actual launched package/activity; unified `com.boop.alpha1` differs from historical `com.boop.shieldoverlay`. No automatic uninstall or permission/default changes.

Use this built candidate only with the known custom-name limitation, primarily to inspect Shield settings and room inventory. BOOP fallback remains. Actual D-pad rendering/scrolling, HA area membership, name persistence, verbal reset, acoustic names and dock/mic behavior still need device tests. Last physical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`.

No identity, signer, microphone lifecycle, model weights or unrelated animation changes in the recheck. No laptop synchronization or background monitor verification is claimed. Original context/handoff/status/memory documents are preserved verbatim under `docs/history/unified-v43/`.
