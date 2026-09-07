# BOOP unified status

Updated 2026-09-07. Branch `boop-unified`; package `com.boop.alpha1`.

## Available candidate

Built code: `6cd9c67a03c639a20acde892e2d57186652e13d5`.
Green run: `34125882296`; artifact `BOOP-Unified` / `10020439707`.
APK SHA-256: `603e72b6f3a83eca429e90a11559454ca2d9c140eee69bff9bcd537d9a276a4e`.
Version metadata remains 43 / `1.1.0-unified-dock-mirror-shield-settings`.
Permanent signer and original BOOP keyword asset preserved.

Verification: CI tests/lint/build, signing and package identity, Shield-entry emulator smoke, downloaded artifact checksum, APK archive and required settings/name DEX markers all passed. Physical Shield Settings navigation and real HA inventory have NOT been verified.

## Important incomplete work

Custom wake-name preference, settings and verbal parsing exist, and custom call prefixes are now removed before normal command routing. However the built custom-name tokenizer applies BPE merging to a UNIGRAM model. Treat custom-name waking as defective/unverified, despite green existing CI.

Repair commit `1c63dac26e2fb0f84a1bf4ee1a445869aff31462` exists separately but its branch publication was blocked by the connector's safety-status check. It is NOT in the above APK or branch. Its local tokenization comparison passed 5032/5032 reference cases; full CI for it remains pending. Documentation changes do not integrate that application repair.

The signed candidate is suitable for checking the requested Shield settings presentation using BOOP as fallback. It is not the completed two-feature release Ryan requested.

## Device evidence

Last accepted rollback: `e746affbb82b577cef2f1cf6e731dff186c8f881`.
Ryan reported unchanged Shield UI on earlier `950611d`; cause remains unproven. That binary did contain its newer settings class. Confirm unified `com.boop.alpha1` is actually launched rather than standalone `com.boop.shieldoverlay`. Do not automatically uninstall or modify grants/defaults.

Portable handheld remains tap-to-talk; wireless dock permits foreground wake listening. Real camera/dock/thermal, remote-focus, custom acoustic accuracy and persistence tests remain outstanding.

See `SESSION_HANDOFF.md` and `docs/BOOP-UNIFIED-WAKE-NAME-RECHECK.md` for exact failures and next steps. Original pre-recheck status is preserved verbatim at `docs/history/unified-v43/BOOP_STATUS.md`; no historical checkpoint was repointed.
