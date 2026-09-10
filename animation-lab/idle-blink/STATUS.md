# BOOP idle blink cleanup status

Current successor: ../canonical-eyes/ contains the shared 26-motion code library
and signed/emulator-tested lab v5. Read its VERIFICATION.md. The notes below
describe the preserved earlier blink workshop, not the latest runtime status.

- Branch: `animation-idle-blink-cleanup`.
- Workspace: `animation-lab/idle-blink/` only, plus its dedicated GitHub Actions workflow.
- Source art is copied unchanged from Git blob `b2112ec156668cc165747d8778a8e564e268b184`.
- Physical evidence: Ryan's 2026-09-09 Shield video identifies two flat black Photoshop leftovers above the genuine eyelids.
- Current phase (2026-09-10): canonical master recovered/hash verified; v3 source-pixel blink and separate headphone alpha-mask candidates saved locally with previews and exports. Ryan review pending. See WORKSHOP_20260910.md.
- Approved final behavior: upper eyelids descend from above, fully cover, hold very briefly and reopen; no whole-eye squash or character redraw; natural random idle interval; occasional double blink.
- Destination intent: reusable Animation Lab default idle blink package for later manual BOOP Air transplant.
- Production app code, builds, signing, permissions, packages and deployments are unchanged.
- Durable laptop workshop exists at `C:/Users/ryank/Documents/Codex/BOOP/animation-work`; large media/APKs remain local. Publication receipts are recorded after live-head verification.
