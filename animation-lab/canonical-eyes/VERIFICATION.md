# Canonical eye library verification — 2026-09-10

## v11 felt joins and hidden arrow tip

Built source 348daff76367ccab892e8feaa56cc3e8e3e0b816; Actions 34442085173
success. APK SHA-256
1797b653908c1cac92a79d138a5fe303a28dbae61864c7f1a414a68856691f25 matches
the downloaded receipt. Permanent signer and exact eye/hand source intact.
Existing 30,636 eye-state and 53,508 sign-state checks pass; CI compilation,
package and integrity checks pass. Static review found no drawing-state issue.
Signed APK installed and cold-launched on dedicated emulator5558. Fixed pose
captured, sign performance recorded, renderer-ready logged without renderer or
fatal errors. Local evidence: animation-work/sign-spinner/evidence-v11 and
record-grip-v11.ps1. Root-only alpha feather and original-palm overlap soften
joins and hide the tip; no whole-hand blur or source repaint. Ryan owns final
appearance and 55-inch testing. No production install or physical acceptance.

## v10 closed fingers

Final built source 0da499aea8907a6a73976ab186f62454937f95b5; signed Actions
34441436283 success. APK SHA-256
304449f94066aa99e14a57611cfecc3913b86f554fb8226d9939fe8cdd7c24ca matches
the downloaded receipt. Same permanent signer and immutable source art.
30,636 eye-state and 53,508 sign-state tests pass; build/integrity checks pass.
Scoped static review found no functional issue in the initial closing change.
Manual inspection then tightened the right knuckle line; final build cold
launched on dedicated emulator5558, fixed pose captured, show recorded, and
renderer-ready logged without a renderer/fatal error. Local evidence is under
animation-work/sign-spinner/evidence-v10 with record-grip-v10.ps1. The earlier
db9f589 v10 build is superseded by this exact source, not a second final APK.
No physical-device test or production integration. Ryan requested this final
refinement; appearance acceptance remains his. V7 eye acceptance is unchanged.

## v9 original-art grip candidate

Built source d220933572ed6ea91db4841ae03f512c138860ec; Actions 34440698857
succeeded. Package com.boop.animationlab versionCode 9, same permanent signer.
APK SHA-256 cd44e29520e75dcbd3d14b16d4e17782916b9afc87b2ab12468378673320a96b
matches the downloaded build receipt. CI checks exact eye/hand bytes, archive,
package boundary and signer. 30,636 eye-state and 53,508 sign-state tests passed
locally and in CI. Scoped independent read-only review found no blocking issue.

The dedicated API36 TV emulator5558 installed and cold-launched the signed APK.
Fixed WhatsApp 3000ms pose captured; WhatsApp and Gmail performances recorded.
All four fixtures launched, Home/Back/reopen and return to Thinking completed.
Renderer-ready logged with no renderer/fatal error in the tested process.
Receipts/script/raw video: animation-work/sign-spinner/evidence-v9 and
record-grip-v9.ps1. User-facing MP4 is a 60fps-compatible encoding of that actual
runtime capture. No automated visual acceptance, soak guarantee, physical-device
test, live notifications or production integration. Ryan reviews appearance.
V8's flat open-hand grip was rejected; v7 eye-corner preview remains accepted.

## v8 five-digit notification sign show

Source 3a7feea9f4e1706fb873e21a3fd7675ebe16197b; Actions 34439276646 success;
artifact 10137328681; package com.boop.animationlab versionCode 8. APK SHA-256
210e960b9bca468d4476cd05b741f7b2bd57b2e99dfdafee35c51843a0580bec.
Permanent signer unchanged. Exact eyes and hands checked inside the APK.
53,508 sign-state checks passed after an observed failing lift assertion;
30,636 existing eye-state checks passed. No automated visual assertions.
Read-only code review found no important functional issue.

Runtime evidence belongs to a dedicated BOOP_Sign_Show_API36 TV emulator on
port5558, not the other tasks' devices. The initial default AVD had GPU disabled;
this new profile was corrected to the working TV graphics setting. Existing
TV/phone profiles were not changed. The signed v8 cold launch succeeded and
the first fixed sign pose was captured.

All four sign performances were then recorded in the actual Android runtime.
Approximate video chapter starts: WhatsApp 0s, Gmail 14.5s, Facebook 28.6s,
X 44.5s. Home/return, Back/reopen, replay and returning to the normal Thinking
eye clip completed. The renderer initialized and no renderer/fatal error was
logged for the tested process. Exact script, RESULT, recording, screenshots
and package receipts remain local under animation-work/sign-spinner.
This short test is not a frame-rate/long-soak/memory-leak guarantee.

No physical installation, notification access, production integration or v8
visual acceptance. Ryan accepted the earlier v7 corner preview only.

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
