# Wall and Shield split implementation plan

Goal: derive two permanently signed applications from the accepted v206 combined source, install Wall only on Pixel 7 Pro and Shield only on Nvidia Shield, and stop at genuine first setup.

Architecture: retain the existing materialization chain and accepted implementations. Compile the common assistant once as an Android library, with two small application shells and the existing animation, launcher and TV libraries. Resolve the body from the installed application identity, never a user profile choice. Keep the current Voice sources and materialization patches byte-identical.

Spec: the user's approved split instructions plus `docs/handoffs/2026-09-16-voice-deferred-split-authorized.md`. The user explicitly authorized unattended execution and installation on both targets; additional design/voice approval rounds are not required.

## Constraints

Base: live owner `841458b8bbc53773d16a18bb0359de1c0550f5e2`, built v206 source `9d57019d9370dbe3f47061b6e8b0ce8ed5134715`. Scope branch: `boop-wall-shield-split-v207`. Wall applicationId `com.boop.alpha1`; Shield applicationId `com.boop.shieldoverlay`. Preserve the accepted UI, photographic felt, sharing, HA, notifications and applicable media/launcher features. Voice latency and tuning work are frozen. Source edits/tests/builds/signing stay on GitHub. No reset, force push, local source build, unrelated app removal, Pixel 10 command, pre-grant or forced default HOME.

## Tasks

- [x] Audit the fully materialized current source, manifests, explicit package references, shared BuildConfig assumptions and Johnny consumer. Run `bash scripts/materialize-unified.sh` then `python scripts/audit-split-input.py` on GitHub. Retain findings and unchanged source provenance.
- [x] RED: publish `tests/test_wall_shield_split.py` before implementation and run its routing, shared-shell and preservation contracts on GitHub. Missing split modules and the profile chooser must fail.
- [x] Add `split/assistant-lib.gradle`, `split/wall/`, `split/shield/` and `scripts/materialize-split.py`. Shared source/resources/native dependencies have one owner, while labels, application IDs and launcher filters belong to the shells. Existing voice and visual source remain unchanged.
- [x] Implement fixed identity routing in `unified/BoopDeviceProfile.java` and remove the chooser from `unified/BoopProfileActivity.java` / `unified/UnifiedEntryActivity.java`. Reuse the real setup/settings actions, mark first setup complete only following a user's button press, and preserve room, voice, access and genuine Android HOME confirmations.
- [x] Resolve each audited hard-coded package use by intent (class namespace versus installed application ID). Preserve Johnny's currently consumed contract and signer restriction without migrating credentials. Add focused executable/package regression tests before fixes.
- [x] GREEN: run the focused split regressions plus the full inherited v206 functional/build gates on GitHub. Compile both shells, verify the same permanent certificate, both manifests, class targets, assets and APK identity. Do not interpret this as physical acceptance.
- [x] Review final source diff and source/Voice preservation, verify no concurrent accepted source advance was omitted, then stage both verified APKs on Yoga and confirm the retained recovery APKs. Check current targets, installed versions, HOME ownership and a usable ADB/Settings recovery route before either uninstall.
- [x] Uninstall only current Unified on each approved target without retaining app data. Install only Wall on Pixel 7 and only Shield on Nvidia Shield, without `-g` or old settings/data. Read back package/version/hash/signature and inspect genuine setup foreground state. No setup button press, sign-in, voice test, permission grant or forced HOME selection.
- [x] Publish reviewed source and exact build/install receipts; update current handoff/context/status/memory and main's ownership pointer when ownership changes. Verify live GitHub HEAD. Record outstanding human acceptance and the two first-setup screens separately from build/install success.

Recovery is not optional: existing v206 Shield and v191 Pixel 7 APKs are retained privately. No clean install is allowed until both replacements pass and recovery is usable. If a genuine blocker prevents installation, preserve current installations and publish accurate unfinished status rather than claiming completion.

## Completion and observation boundary

Both signed v207 apps were clean-installed and initially verified at genuine first setup without a setup-button press. Pixel 7 remained there in the latest read. Shield later reported setup complete and YouTube foreground; that subsequent progress was left unchanged, not reset to satisfy the stopping-point wording. The deployment receipt records the BOOP-only listener/overlay revocations and the unresolved origin of intervening state changes. Human acceptance of onboarding and post-setup behavior remains pending.

Delivered source/run: `aa8fd9f6d79f28b441a48df31138a75d38420118` / `35110823569`. Verification-only follow-up `5f9dbc4e86c0ec92eba728e8b3fbdf56818c2ece` passed focused `35114639152` and full signed `35114639135`. No follow-up installation was performed. Current handoff/status/memory/context and main routing carry the results. Full receipt: `docs/handoffs/2026-09-16-v207-signed-clean-install.md`.
