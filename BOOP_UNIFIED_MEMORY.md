# Scoped canonical rebuild status

Updated 2026-09-10. Owning branch/worktree: `boop-canonical-rebuild` at `C:/Users/ryank/Documents/Codex/BOOP/.worktrees/boop-canonical-rebuild`. Base `boop-unified@99474d141e7affad17cdbe854e94dd3986076980`. Canonical `boop-unified` is unchanged; no merge/physical acceptance of this candidate. Current candidate commit is the commit containing this document (resolve with git and verify live remote).

## Accepted physical baseline

Ryan confirmed v91 natural voices are selectable, demos speak, and a normal BOOP reply speaks with the selected voice on 2026-09-10. This lifts the Natural Voices gate for the selected rebuild. Exact APK source `11650313221ae5bf997dbb93b6a905bfdc7da1ed`; version 91; package `com.boop.alpha1`. APK SHA256 `42dc50d12031a674aa751918f6bfd6b4deab8b6ced95332a437f4068124fe53d`; permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Successful source workflow `34433115316`, artifact `10135283428`. This acceptance covers voices, not every previous appearance/remote-mic concern. Protected voice checkpoint `checkpoint-boop-unified-v91-natural-voices-accepted` points to the exact v91 source. Preserve v88 checkpoint `f5f086fc4f67712b5746be067aff852331299bb0` as historical rollback too.

## Requested scope and current implementation

Items 1, 3, 6, 7, 8 and 10 only: shared speech/room/media state; current-room exposed generic HA discovery; one media corner/Home owner; explicit Deezer and session transport; HA Back escape; manual/automatic profiles. Eyes are being handled in another task for later transplant. No eye/blink mechanics or Turbo work is authorized in this branch.

Candidate version 93 uses the existing permanent signer/package. Existing Shield Home/Now Playing sources are integrated as an internal library under the approved rebuild; the standalone source branch remains preserved. No OS default-HOME or permission is changed automatically.

## Verification and next action

Initial candidate v92 source `a3eb768641e339ed59a6e2e86cb74f64bccf5979` passed GitHub run `34436377162`: 168 Unified and 58 Shield focused tests, package/signature/archive validation. Artifact `10136361414`; ZIP SHA256 `fcc418dc2244e48d84e82c26e82525e8f2189a1db9fffc2f27615f192026e76c`; APK SHA256 `6d8d5a797b8b9aba862f2bc0e5903e7d5e2260e94b6a1e88d75b613a6559cc38`. Download independently verified; installed on local API36 TV and Pixel emulators.

Manual TV inspection confirmed first-run automatic Shield routing, readable Home/settings, Back from unpaired HA returning to caller, and a simulated media session producing one corner over its player then one puppet inside Home Now Playing. The fixture provides metadata only, no real audio/provider acceptance. Review fixes include room identity reassignment, ambiguous-name clarification, group-command preservation, shared dashboard room settings, and lifecycle-gated Home ownership.

Manual inspection also found fresh-entry reopening old Settings and Android16 bypassing legacy Back handlers. v93 fixes fresh entry to Home and uses documented `enableOnBackInvokedCallback=false` compatibility for existing Back/long-Back handling; adds explicit media-overlay permission settings and foreground permission recheck. v93 build and final emulator checks are pending. No GitHub visual tests. Candidate physical/real Deezer/HA acceptance remains pending.

Plan: `docs/superpowers/plans/2026-09-10-scoped-canonical-rebuild.md`. Source provenance: `unified/SOURCE_HEADS.md`. Historical v89/v90 failures and v91 repair evidence remain in git history of the canonical handoffs.
