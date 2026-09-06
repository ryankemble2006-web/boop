# BOOP Launcher handoff — 2026-09-06

Owner: Ryan's Android Work task. Authoritative branch: boop-launcher-alpha1.
Project: launcher/; package com.boop.launcher. Current task checkout:
/workspace/scratch/e630624baa67/boop-launcher-fix (local branch boop-launcher-drag-fix
tracks the authoritative app branch). Root source is inherited historical BOOP,
not the current Wall app.

## Current delivery

- Alpha 1.1: versionCode 2, versionName 0.1.1, same package and permanent BOOP key.
- Exact tested source: 41f250d4ca0c791df7a83dd1c083aba7840670b7.
- Build/verification run: 34062128943. Build, lint, 10 unit tests and all 15 emulator checks passed.
- Existing installed layouts remain in the same private preferences on update.
- Ryan confirmed Alpha 1 installed and app pinning worked on his phone. Alpha
  1.1 physical acceptance remains pending; emulator evidence is a separate level.

## Reported bug and fix

Ryan held a pinned icon to drag upward for removal. An instruction menu opened
before release; he reported Android's Home selector on letting go. The old signed
APK (run 34058128961) reproduced the premature menu in regression run 34060777148.

Icons now own one touch stream from DOWN through release. Long hold picks up the
item and shows a temporary top removal area, without a dialog or mid-touch layout
rebuild. Upward release removes it; ordinary drag moves it without overlap. A
stationary hold enters editing, and a separate short edit tap opens size/page
options. Widgets keep ordinary short touches; drag takeover explicitly cancels
the widget child. Cancel, focus loss, Back and HOME preserve the chosen mode and
do not turn release into a toolbar click. Multi-pointer input cancels pickup.

The first signed fix (8606a1d, run 34061237800) passed 8 gesture checks but exposed
Back restoring editing. Android sends CANCEL before detaching the old root;
render() now cancels active pickup without restoring stale mode before replacing
views. The final test releases directly over the measured Bail out button.

## Verification and next step

The CI pipeline first runs launcher/scripts/drag_smoke.py on the signed APK with
the system Home default unchanged. It then clears only the emulator's launcher
data and runs the general smoke.py checks. Tests cover continuous hold/removal,
persistence, stationary release, size options, movement, release over Bail out,
focus-loss cancellation, Back during a hold, startup, drawer, pages and rotation.
Current verification: 9 gesture regressions and 6 general emulator checks passed.
No AndroidRuntime errors were recorded. Signed artifact: 9997817013; emulator
evidence: 9997900126. APK SHA256:
6cbe27e26859f7b9f8e37c3a83088389f21fd3b4294d4d2cbd988708bebc6e02.
Download filename: BOOP-Launcher-Alpha1.1.apk. Content is the exact signed APK
from run 34062128943; only its filename changes for delivery.

Next: install the signed Alpha 1.1 update over the existing app and repeat Ryan's
physical hold/upward-drag/release gesture. Real Home Assistant widget binding,
configuration/cancellation and widget touch controls still need phone checks.
BOOP Wall handoff/optional return strip also retain their own phone test gate.

## Download handoff

The first Android download route flashed closed; a direct Library download URL
then showed a cross-site refusal. After an APK reattachment with explicit Android
package MIME type and a GitHub backup were supplied, Ryan proceeded to installed-
app testing; he did not report which download route worked. Use the normal APK
attachment with application/vnd.android.package-archive; avoid that failed direct
URL route. The GitHub signed artifact is a ZIP backup containing the same APK.

## Cross-app boundaries

Wall: boop-wall-resurrection, com.boop.alpha1; preserved voice/wake code595e1da.
Wall eyes -> Launcher swipe has separate work on boop-wall-launcher-handoff-wip;
consult its live handoff before inferring current build/device acceptance.
Shield: boop-shield-media-puppetry; consult its own handoff. Neither Wall nor
Shield source was changed for this Launcher fix. Main owns the shared map and
contracts; app progress belongs here.
