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
