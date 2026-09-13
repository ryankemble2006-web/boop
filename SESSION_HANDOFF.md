# BOOP Lyrics Lab: Shield user test succeeded

Updated 2026-09-13. Owning branch: `boop-lyrics-lab-side-by-side-v157`.
App: **BOOP Lyrics Lab**, package `com.boop.lyricslab`, version
`157 / 0.1.157-lyrics-lab`. GitHub owns source and build evidence.

## Latest user result

Ryan reported: "i tested for you and enabled notification.. it worked".
Record this as USER-TESTED WORKING on the Shield after Ryan enabled the lab's
notification access. It is a real-device user result, not merely the synthetic
emulator preview or an API-only probe. Ryan enabled access himself; this report
is not permission to change or import permissions in another app.

Ryan previously said: "keep going i like the design ya did in the emulator".
The emulator presentation is user-approved. Preserve that design; do not
redraw it, regenerate artwork or replace it with a modal text box.

The feedback confirms the successful tested lyrics path. It does not enumerate
pause/resume, both seek directions, automatic track changes, no-lyrics tracks,
leaving/reopening or long-session behavior individually. Keep those checks
separate rather than treating this message as blanket acceptance of every case.

## Exact source and live build evidence

- Lab application source: `9a52f1c66a6322584e99350056fb4b9bcb645a2b`.
- Shared native-lyrics source: `9bb64285d3a3fb8d3cd1f4890931d7afdf74dfb9`.
- Requested Unified base: accepted v156 at
  `a901c1e9f31e55c710e31ac7ff4f5924c9769d56`.
- GitHub run `34763076696`, **Build side-by-side BOOP Lyrics Lab**:
  completed SUCCESS for the exact lab source above.
- Artifact: `BOOP-Lyrics-Lab`, ID `10319767160`, not expired when read.
- Artifact archive digest reported by GitHub:
  `sha256:6f52e2fa4521980b8a06cc994b971c042684957b09f671d2cebfb818d2a9a29d`.
  This is the artifact archive digest, NOT the installed APK hash.

The branch, source and CI artifact were checked live when recording this result.
No fresh ADB/package-hash check was performed in this documentation update;
do not manufacture an independent installed-artifact verification receipt.
No new APK, app-code edit, permission change or deployment was performed.

## Isolation and merge boundary

Ryan requested a side-by-side fork after other work collided, with merging
later when the feature is complete. That fork already exists. Do not create a
second fork, restart from a local draft, or install a v157 Unified downgrade.
This lab is a separate music application, not a replacement HOME launcher.
Only `com.boop.lyricslab` is the target for subsequent lab installs and testing.
Do not install `com.boop.alpha1` or the synthetic `com.boop.lyricspreview` on the
Shield in this task. Neither physical Pixel is a target. Preserve the existing
Unified app and the other task's work regardless of their version numbers.
No merge into Unified/main is authorized by this success report.

## Next continuation

Keep this working lab and approved presentation. When work resumes, fetch the
live lab branch and continue any remaining focused playback/lifecycle checks
on the dedicated local TV emulator and this separate Shield package. Do not
rebuild the macro. Later integration must merge the native-lyrics feature into
the then-current Unified source, not replace newer Unified with the old base.
Physical confirmation remains Ryan-owned; non-visual GitHub checks remain
separate from local runtime testing. No GitHub visual checks.

## Preserved context

The inherited pre-confirmation handoff, status and full historical memory were
preserved byte-for-byte under `docs/history/lyrics-lab-pre-user-confirmation/`.
Their old instructions to install a Unified candidate are superseded by the
side-by-side boundary above. Earlier failed layout/API/macro experiments remain
historical evidence, not the current tested lab state. The feature plan remains
at `docs/superpowers/plans/2026-09-13-native-lyrics.md`; lab packaging and isolation
are described in `lyrics-lab/README.md`. Keep private captures, diagnostics,
account data and downloaded third-party material outside the public repository.
