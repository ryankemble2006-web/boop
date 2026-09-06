# BOOP Launcher handoff — 2026-09-06

Owner: Ryan's Android Work task. Branch:boop-launcher-alpha1.
Project is launcher/, package com.boop.launcher; build guidance in launcher/README.md.
Root Alpha1 README/source describe an inherited historical app, NOT current Wall.
No Launcher application source was changed by the cross-device synchronization.

Downloaded initial snapshot763d77b. The Android task is actively advancing this
branch: fetch/check live HEAD at startup and again before any push. A documentation
sync commit does not prove a new Launcher build or physical acceptance.
Use existing launcher build/verify workflows for exact commit evidence. Physical
device tests listed in launcher/README.md remain a separate gate.

Current Wall reference:boop-wall-resurrection, com.boop.alpha1, accepted code595e1da.
Proposed Wall eyes -> left swipe -> Launcher is saved separately on
boop-wall-launcher-handoff-wip; source tests/harness pass but signed APK and
physical acceptance are not verified. Do not replace Wall with the echo-only
source inherited on this Launcher branch. Keep the two apps separate.

Shield reference:boop-shield-media-puppetry. Read its SESSION_HANDOFF.md when
needed; no need to merge Shield into Launcher to consult its latest source.


## Active Launcher drag regression
Ryan confirmed Alpha 1 installs after its download attachment type was corrected.
A pinned app's long press opens an instruction menu and release can reach Android's
Home selector. Reproducing against signed run 34058128961 before changing app code.
A continuous DOWN/hold/MOVE/UP emulator regression is being added. Existing APK
remains the delivered build; no drag fix is claimed yet.

## Alpha 1.1 touch fix — verification in progress
- Old delivered APK reproduced the held-finger modal bug in GitHub run
  34060777148 (job 101560788856), assertion: long press opened a modal before UP.
- Icons now own the original touch stream through release. Long hold shows a
  temporary overlay removal target without opening a dialog or rebuilding home.
  Widgets receive normal short touches; takeover cancels their child explicitly.
- Short tap in pre-existing editing still opens size/page options. Focus loss,
  cancellation and detach clear drag state without saving or deleting.
- VersionCode 2 / versionName 0.1.1 keeps package/signature and stored layout.
- Added live gesture regression for hold, upward removal, persistence, stationary
  release, size options, movement, toolbar crossing and interrupted touch.
- New signed build and emulator verification pending. Do not distribute as fixed
  until those checks finish. HA widget/provider device checks remain outstanding.

### First signed fix validation
Build/lint/10 unit tests passed for source 8606a1d, run 34061237800.
Eight continuous-touch emulator checks passed, including the reported removal
gesture and release across the Bail out toolbar area. The additional Back-during-
hold check failed. Android sends CANCEL while removing the old root before detach;
that cancellation restored the previous editing flag. render() now cancels the
active item without restoring its old mode before replacing views. Rebuild pending.
