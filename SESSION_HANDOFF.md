# Wall -> Launcher swipe — WIP snapshot, 2026-09-06

Preserves the exact five-file draft from the other laptop task at Wall base619ba5d.
Original uncommitted files remain in that task's worktree; do not overwrite them.
This branch is a backup/reference for cross-device development, not a finished
Wall release or a change to the accepted checkpoint595e1da.

Draft:MainActivity/manifest changes plus BoopLauncherSwipeGesture and two tests.
A96dp horizontal displacement with1.5 directional confidence launches
com.boop.launcher; missing package produces a plain-English toast.
Fresh123 source tests and the four-case Java gesture harness PASS.
No verified signed APK, install or physical acceptance for this draft.
Review full touch/hold/voice behaviour, cancellation and multipointer handling
before any promotion. Tiny/vertical movement must not accidentally launch.
Do not treat a source-string check as proof of Android touch-event integration.

Next: coordinate against current boop-launcher-alpha1, preserve independent apps,
and review/build/test the Wall change before merging it into the Wall app branch.
No changes to the Launcher source, auth, signing or HA were made by this snapshot.
