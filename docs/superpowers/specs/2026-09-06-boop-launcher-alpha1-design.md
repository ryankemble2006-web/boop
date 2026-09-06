# BOOP Launcher Alpha 1 approved design

User authorized implementation of the previously approved design on 2026-09-06.
A self-contained `launcher/` Android application beside BOOP, application ID
`com.boop.launcher`, label BOOP Launcher. Existing BOOP and Shield code stays untouched.
One APK for Pixel 7 Pro and Pixel 10 Pro XL, Android 10+ with responsive insets.

## Experience
Pure black home canvas, original app icons, no clock, dock, folders or permanent add
button. Search retained in drawer. Swipe up opens searchable apps; long press an app
adds it. Long press home edits; freely position icons without overlap, select size,
move between home/widget pages, upward removal and downward normal return. Back
returns to previous page, first closing editing/drawer. Home returns main canvas.
Swipe left moves to HA/native Android widget page; optional second widget page.
Widgets are real AppWidgetHost widgets with Android binding/configuration consent.
Positions and sizes persist locally. Layout must remain in bounds and nonoverlapping
on rotation and screen-size changes. Big chunky English UI and accessible labels.
Bail out in edit/first-run opens Android Home settings; never trap user as default.

## BOOP companion
Open installed BOOP (`com.boop.alpha1`) from launcher. After explicit Android overlay
permission, a narrow right-side swipe strip returns to launcher on leftward swipe.
Do not intercept BOOP taps, voice or full face. Service notification has return/stop
controls; remove strip on launcher resume, screen off and service stop. No microphone,
HA credentials, network permission or changes inside BOOP. Explain that strip stays
active until return/stop/screen off if BOOP opens another app. Launching BOOP is a
user action, not an automatic recurring Home loop. Companion entry is on initial
screen and editor. Without BOOP installed show plain English install-needed message.

## Build and verification
Native Java/platform views chosen to match existing project and minimize dependencies;
Compose was a prior implementation suggestion, not locked user behavior.
Isolated branch `boop-launcher-alpha1` from origin/main. Separate workflow builds and
signs release using existing BOOP_KEYSTORE_BASE64, BOOP_KEYSTORE_PASSWORD,
BOOP_KEY_ALIAS, BOOP_KEY_PASSWORD secrets; never fetch their contents. No debug-key
fallback. Unit tests cover layout collision/clamping and page/back decisions.
Emulator smoke covers installation, HOME resolution, drawer/search, add/edit, rotation,
process restart persistence and bail out; real widget provider and real BOOP phone
integration remain explicitly identified if not exercised by emulator.
