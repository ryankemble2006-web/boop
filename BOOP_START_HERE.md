# BOOP Unified: current startup and testing workflow

Updated 2026-09-15. Current owner: `boop-hand-colour-v191`; rollback owner: `boop-felt-preview-v178`. Main is the shared-context hub, not the latest combined app. Fetch the LIVE owning branch and main, then read this branch's `SESSION_HANDOFF.md`, `BOOP_STATUS.md` and `BOOP_UNIFIED_MEMORY.md` for implementation, installed version and acceptance state. Read the current receipt for the exact built APK version.

## Latest user rule: GitHub development, testing together

Ryan explicitly requested the ready signed APK be installed for his own testing and set the ongoing workflow to development on GitHub plus device testing together with him. This supersedes older emulator-first rules, the cosmetic/substantive distinction, and the previous deployment hold in historical handoffs/context. Shared workflow is recorded in `main/BOOP_START_HERE.md` at `24a260b6e7cdd5aed792ccfbb683e8e495eb5f80`.

Use GitHub for source edits, non-visual code tests, builds, permanent signing and durable documentation. After the existing code/package/signer checks pass, provide the signed APK and perform the installation Ryan requests. No autonomous emulator run, emulator-first delivery gate or GitHub visual test. Test visible device behaviour with Ryan, guided by his observations and agreed next checks. Do not report CI, changed sliders or installation success as physical acceptance.

Desktop Commander/ADB may stage the GitHub artifact, perform an explicitly requested installation on authorized devices and verify package/version/hash. Use further device inputs, captures or diagnosis only as part of the joint test. No local app-source edits/builds. Preserve dirty/concurrent work and existing AVDs; use an emulator only if Ryan explicitly requests it.

Device operations remain scoped to Ryan's current joint-test request and current handoff. Historical device choices are not a new installation request. Preserve settings, permissions, approved artwork, coded animations, exact1x, single-face ownership, working hue controls and accepted Shield polish.

## Current delivery and evidence

## Next session: puppet personality and silly voice controls

Ryan's next requested work after the usage reset is the voice/personality side of Build a Boop: playful, deliberately silly voice controls, with chosen voice settings carrying to the Shield like the existing colour controls. Explore the control range with him when work resumes; optional natural-language/voice downloads remain part of the earlier direction. This is a recorded next task, not a completed voice feature.

Boop is a genderless, raceless felt puppet intended to belong in any household. Colour is customizable felt and character styling. Preserve the strict five-digit hand standard: four fingers and one thumb, with the coherent connected anatomy already accepted. Future ASL/BSL remains a separate implementation and validation task; the current fixed sign grip is a foundation, not completed language support.

Ryan credits the tiny material details with bringing Boop forward as a performer. He specifically values the pause/scrub slider and embedded lab because they let both collaborators inspect the same animation instant and experiment without revising the accepted production character. Preserve those review tools and the accepted v189/v191 checkpoints.


## Latest: v191 hand colour accepted — 2026-09-15

Owner `boop-hand-colour-v191`; reviewed/built `32f7d34bd58e1f0ae58134326e2881ba0456f75c`, signed run34946949843 passed. Version191 installed and actual APK hash/version verified on Shield and Pixel7Pro.

Build a Boop now has independent hand colour below felt, an exact Original yellow reset, a fixed canonical-eye/sign preview and separate optional hand sharing. V189's twelve-file accepted default lock and v190's artwork/anatomy/scale remain intact. All code/package/signer checks passed; two historical skips remain explicit.

Ryan confirms hand sharing worked first time and, to his eyes, no stray pixels are visible. **V191 hand appearance and cross-device sharing are physically accepted.** Preserve this checkpoint; further character changes require his new direction.

Source, CI, signer and both installed APK identities were previously verified. This acceptance is Ryan's subsequent real-device verdict, not an inference from those checks.

See [v191 receipt](docs/handoffs/2026-09-15-v191-hand-colour.md). Preserve v190 rollback. Fake-notification diagnostic remains later work. Earlier notes below are historical.


## Latest: v190 felt signs accepted; hand colour requested (2026-09-15)

Owner `boop-felt-signs-v190`; reviewed/built97ac4f54bd38ea385622ce7f97df85fd5593e0cc, run34945220479 passed. Permanent-signed190 installed and actual APK hash/version verified on Shield and Pixel7Pro. Ryan accepted the connected felt signs and hand scale. V189 remains the permanent default under tag `boop-felt-default-v189`, twelve exact-file build checks and materialized route checks. Saved colour choices remain editable.
Next explicit request: independent hand colour in Build a Boop, preserving these accepted shapes/materials. See [v190 receipt](docs/handoffs/2026-09-15-v190-accepted-felt-signs.md). Earlier notes are historical.

## Continuity

Current user instructions and freshly fetched main decisions override inherited historical root maps/context. Do not restore the old separate-package roadmap over the current Unified lineage. Standalone Launcher, standalone Animation Lab and unrelated projects remain separate unless Ryan explicitly requests integration.

After material results, update the current handoff/status/memory, review the exact diff, publish only relevant files and verify live GitHub HEAD. A GitHub-only session must not claim a laptop checkout was synchronized. Do not force-push or overwrite concurrent changes. Keep credentials, private addresses, raw device dumps and private captures out of this public repository. Preserve all previous acceptance/research receipts as history rather than current blockers.

Known-good laptop transport remains `npx.cmd -y @wonderwhy-er/desktop-commander@0.2.47 remote`; do not change it while it works. Explicit tool denials are not permission to bypass them. The current requested ordinary v161 installations succeeded without changing the transport or permissions.
