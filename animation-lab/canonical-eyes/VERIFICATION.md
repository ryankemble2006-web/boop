# Canonical eye library verification — 2026-09-10

## v7 inner-corner correction

Built source: `31735d1395ab1c5167bd5bdbcb0c48c6f77a752c`.
Actions 34437977312 succeeded; artifact 10136874223. Package unchanged,
versionCode 7. APK SHA-256:
`2d383d6c089605dd70424d768c302bb495b1208dd3ebc412a7772b41d8ef97eb`.
Permanent signer and exact master unchanged. 30,636 state checks passed again.
Code review found no blocking maths/lookup issue. There are no visual CI tests.

The v5 cutoff was reproduced at Thinking 1100ms before edits. v6 alpha-only
candidate c8f6b60 / run34437615447 did not remove it and extended the outer cap;
that experiment is retained as failed evidence, with the alpha change reverted.
The source rig had a plateau at its inner caps. v7 separates material lookup
from the continued moving boundary in a two-row rig. The correction ramps in
near open and converges at shut. Neutral, source master and all clips/timings
remain unchanged. The same fixed-pose v7 capture no longer shows the horizontal
shelves; that is a diagnostic observation, not Ryan's visual acceptance.

Local receipts/captures: animation-work/library-v2. No production app change or
physical test. Current shared main records v91 Natural Voice acceptance; the
older v88-only acceptance wording below belongs to the earlier snapshot.

Actual GLES2 initialization and all 26 fixed-time clip launches completed with
no renderer failure logged. Thinking 1100ms, reset, partial/shut blink captures
and a 30-second runtime review were saved. Final Home/resume was NOT verified:
another BOOP test held foreground and the lab's waiting am-start was stopped,
leaving that test alone. The script reported `Clip failed thinking` at this final
step. Do not describe the whole smoke script as passing. Earlier v5 lifecycle
evidence remains historical; this is not a new physical/lifecycle acceptance.

## Historical v5 library verification

- Owning branch: animation-idle-blink-cleanup. Built implementation:
  `73dfa160649d9462839db7e47cbef939f8ec894f`.
- Actions run 34435432070: success; artifact 10135991882.
- APK: BOOP-Animation-Lab-v5.apk, com.boop.animationlab, versionCode 5.
- APK SHA-256: `7841fb99b4fc42449d361c849e3368030b35af3350ef3008708e4cce34271f69`.
- Existing permanent signer SHA-256:
  `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Exact master SHA-256:
  `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`.

30,636 nonvisual state assertions passed (bounds, finite values, loop seams,
terminal holds, transitions, pause/resume, blink timing and reset). CI compiled,
signed and checked the package, absence of permissions, signer, master and ZIP.

Installed only on emulator-5554, Android TV API36. Actual GLES2 shader initialized.
All 26 catalogue clips were launched, followed by 18 extra state switches and
Back/reopen plus Home/resume. Renderer failure flag false; process PID 4571
remained unchanged. Logs show clock suspension/resumption and renderer recreation.
PSS snapshots were 22,649 KiB before and 24,318 KiB after; these short-run samples
are not a leak test or a performance guarantee. No automated appearance scoring.

Local evidence: animation-work/library-v1/emulator-73dfa160649d, including RESULT,
runtime log, installation receipts, screenshots, memory samples and actual runtime
recording. Catalogue index places the final clip at approximately 64 seconds in
the 118.58-second source recording. The review copy ends after the catalogue.

Visual review of all new code motions is pending with Ryan. No physical-device
test/acceptance or Unified integration. The last recorded usable app rollback is
v88 f5f086fc4f67712b5746be067aff852331299bb0; natural voice remains unaccepted.
The separately owned canonical app/rebuild worktree was not changed.

The transplant exporter includes source, generated runtime assets/Java, individual
clip definitions, provenance and hashes. Existing accessory art and hands are not
claimed reimplemented. Large recordings/APKs remain local, not in public Git.
