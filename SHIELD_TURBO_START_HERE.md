# SHIELD TURBO: start here

This is a separate Android TV utility authorised by Ryan on 2026-09-07. It shares this repository, not BOOP's application package or runtime.

- Owning branch: [`shield-turbo-v01`](https://github.com/ryankemble2006-web/boop/tree/shield-turbo-v01).
- Source project: [`shield-turbo/`](https://github.com/ryankemble2006-web/boop/tree/shield-turbo-v01/shield-turbo).
- Current implementation/build evidence: [`shield-turbo/SESSION_HANDOFF.md`](https://github.com/ryankemble2006-web/boop/blob/shield-turbo-v01/shield-turbo/SESSION_HANDOFF.md).
- Package: `com.boop.shieldturbo`.
- Workflow: `.github/workflows/shield-turbo.yml` on the owning branch.
- Approved design: `docs/superpowers/specs/2026-09-07-shield-turbo-design.md`.
- Approved plan: `docs/superpowers/plans/2026-09-07-shield-turbo.md`.

## Working agreement

Ryan wants this project developed through connected GitHub tools and GitHub Actions in chat mode. Work mode is for a later optimisation pass, not a prerequisite for this project. Do not instruct him to switch modes for ordinary development. Do not claim automatic background chat monitoring or control over his client sleep settings.

Use the repository's already established secret-backed `boop-dev` signer. Never create or publish a replacement key, expose secrets, use BOOP relay credentials, or install/change permissions on a physical device merely because a build was requested.

Fetch the live owning branch before edits and again before pushing; preserve concurrent work. Main is the cross-project context hub. BOOP's own canonical application remains on `boop-unified`; never merge a stale BOOP app snapshot from this independent utility branch into that app lineage.

## v0.1 scope and evidence limits

The approved first release is read-only capability and telemetry analysis. It is not a claim of boosted performance. ADB setup/helper and tuning actions remain deferred pending actual Shield capability evidence. Ordinary permission grants are not permanent shell authority; a root-related file is not working root access.

Tests, a signed APK, emulator remote-input verification and physical Shield acceptance are distinct states. The owning handoff records which ones have actually passed. Consult it rather than assuming an app is ready because a plan or commit exists.
