# BOOP unified memory

Updated 2026-09-07 after build and wake-name recheck.

Canonical app lineage remains `boop-unified`, package `com.boop.alpha1`, with Wall core plus internal Launcher/Shield modules and permanent BOOP signer. TV/Leanback routes to Shield, Pixel 7 Pro to Wall, other handhelds to Launcher; preserve the recovery override. Historical app branches/checkpoints remain rollback/reference. The original full memory is retained verbatim at `docs/history/unified-v43/BOOP_UNIFIED_MEMORY.md`, including exact initial source/build receipts and all official yellow-hand/2.5D design contracts.

## New durable decision: call-name customization

BOOP forever. Only an additional user-chosen spoken wake/call name is configurable, under `BOOP's name` in Voice Settings. Default and permanent fallback are BOOP. One preference: `boop_voice/wake_name`. Natural typed names and local verbal rename/reset commands must use it without changing app/package/classes/branding/HA/pairing/signing/repository identity, normal controls/chat, wake sensitivity or microphone lifecycle. No name allowlist and no parallel wake architecture.

## Current verification memory

Ryan accepted unified `e746affbb82b577cef2f1cf6e731dff186c8f881` as the last physical rollback. He reported no visible Shield UI change on `950611d`; binary inspection found the newer class present, so the cause remains unknown. Future testing must identify the real package/activity, not assume the artifact excluded the source. Do not remove the standalone package or silently alter Android permissions/defaults.

Latest available signed code is `6cd9c67a03c639a20acde892e2d57186652e13d5`, run `34125882296`, artifact `10020439707`, APK SHA-256 `603e72b6f3a83eca429e90a11559454ca2d9c140eee69bff9bcd537d9a276a4e`. It contains the new uppercase Shield settings, HOME ASSISTANT section, name field and custom-prefix routing repair. All current CI checks passed, but they missed the custom-name tokenizer defect; do not equate green CI with this request being completed.

The pinned file named bpe.model is actually a UNIGRAM model. Greedy BPE tokenization disagreed with native SentencePiece on 23/32 initial name phrases. A model-type-aware repair matched 5032 reference cases locally and exists in commit `1c63dac26e2fb0f84a1bf4ee1a445869aff31462`; its branch publication was blocked by the tool safety-status check. The repair is NOT integrated, NOT in the APK and NOT fully CI/physically tested. This docs-only update does not apply that blocked code.

Use exact commit/artifact hashes because version 43 metadata was reused. Keep BOOP fallback for testing this candidate, and require full repaired-build plus real acoustic/dock/persistence and Shield settings/focus/room tests before marking the feature complete.

No ongoing monitoring or laptop synchronization was verified in this recheck. See `SESSION_HANDOFF.md`, `BOOP_STATUS.md` and `docs/BOOP-UNIFIED-WAKE-NAME-RECHECK.md` for the current safe continuation.
