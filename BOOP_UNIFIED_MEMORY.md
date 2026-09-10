# Scoped canonical rebuild status

Updated 2026-09-10. Owning branch/worktree: `boop-canonical-rebuild` at `C:/Users/ryank/Documents/Codex/BOOP/.worktrees/boop-canonical-rebuild`. Base `boop-unified@99474d141e7affad17cdbe854e94dd3986076980`. Canonical `boop-unified` is unchanged; no merge/physical acceptance of this candidate. Current candidate commit is the commit containing this document (resolve with git and verify live remote).

## Accepted physical baseline

Ryan confirmed v91 natural voices are selectable, demos speak, and a normal BOOP reply speaks with the selected voice on 2026-09-10. This lifts the Natural Voices gate for the selected rebuild. Exact APK source `11650313221ae5bf997dbb93b6a905bfdc7da1ed`; version 91; package `com.boop.alpha1`. APK SHA256 `42dc50d12031a674aa751918f6bfd6b4deab8b6ced95332a437f4068124fe53d`; permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Successful source workflow `34433115316`, artifact `10135283428`. This acceptance covers voices, not every previous appearance/remote-mic concern. Protected voice checkpoint `checkpoint-boop-unified-v91-natural-voices-accepted` points to the exact v91 source. Preserve v88 checkpoint `f5f086fc4f67712b5746be067aff852331299bb0` as historical rollback too.

## Requested scope and current implementation

Items 1, 3, 6, 7, 8 and 10 only: shared speech/room/media state; current-room exposed generic HA discovery; one media corner/Home owner; explicit Deezer and session transport; HA Back escape; manual/automatic profiles. Eyes are being handled in another task for later transplant. No eye/blink mechanics or Turbo work is authorized in this branch.

Candidate version 92 uses the existing permanent signer/package. Existing Shield Home/Now Playing sources are integrated as an internal library under the approved rebuild; the standalone source branch remains preserved. No OS default-HOME or permission is changed automatically.

## Verification and next action

Shared-state/parser Java 17 behavioral harness passes, and local source materialization passes. Independent review is in progress; room setup reassignment is being completed. Android compile, signed GitHub build, runtime/emulator inspection and candidate physical acceptance are pending. Use GitHub only for nonvisual builds/functional tests; do not run visual tests. Inspect the signed candidate locally on phone/TV emulators, then ask Ryan for device-only/provider/HA acceptance. Do not replace the accepted v91 artifact or merge to canonical until the candidate evidence is recorded.

Plan: `docs/superpowers/plans/2026-09-10-scoped-canonical-rebuild.md`. Source provenance: `unified/SOURCE_HEADS.md`. Historical v89/v90 failures and v91 repair evidence remain in git history of the canonical handoffs.
