# BOOP Wall Free Chat handoff

Updated 2026-09-07. Owning branch: `boop-wall-free-chat-wip`.
Status: **signed and CI/emulator green; physical acceptance pending**.
This supersedes the earlier unresolved UI-gate notes, not the accepted physical
Wall checkpoint. Keep the candidate isolated until Ryan tests it on a phone.

## Exact verified build

- App: `com.boop.alpha1`, versionCode 31, `0.4.11-wall-free-chat`.
- Built commit: `0ceb97bc7c258835ce292391d483849398016020`.
- Base: `boop-wall-resurrection@3a702f89b7f317649d267f25c34c6c9655edcff8`.
- GitHub Actions run: `34071614834`; build job: `101589892017`.
- Artifact: `10000728933`, `BOOP-Wall-Free-Chat-candidate`.
- Downloaded APK: `BOOP-Wall-v31-Free-Chat.apk`, 139485298 bytes.
- APK SHA-256: `2d8c858da399c2e6f1f9e7cbbed8199de6bcb9ca3d207442fd49252bb3626d29`.
- Artifact ZIP SHA-256: `3d2d7e818413640c0a9ed887fac90e5123336e5c8fe858f56ba402476d7e52d7`.
- Effective materialized MainActivity SHA-256:
  `64605902f7198dc9f6596d852ada7aa995e5febfa2486dc3dd35f9c2e10bfef0`.
- Existing signer SHA-256:
  `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The completed job-step report was reread: all steps passed, including source and
bridge regressions, preserved Java harnesses, Android unit tests, materialization,
all 33 wake mappings, APK assembly, package/version/archive checks, full apksigner
verification, clean Android 16 Pixel 7 Pro emulator startup, the real wake-microphone
armed gate, actual menu/persistence/revert/cancellation interaction, and the Shield
pairing-return route. The exact successful artifact was downloaded; its build
receipt, ZIP digest/integrity, APK digest and inner archive integrity were checked.
This remains the stable-signed debug variant, not an optimized release.

The actual UI gate proves: a 1.6-second playful hold does not open the menu; a
3.3-second hold does; fresh default is OpenCode; selecting Free Chat saves it;
force-stop/restart retains the selection; the same menu restores OpenCode; a
vertical drag and backgrounded/cancelled hold do not open the menu later.
Fourteen focused capture/bounds/onboarding tests also pass in the Linux scratch
review. No physical phone, browser login, in-place v31 update, or real-house
acceptance was performed here. Do not extrapolate CI to physical verification.

## Approved behaviour

Hold the face for three seconds, past the existing MemberBerry animation, for
Chat mode: OpenCode / Free Chat / Cancel. OpenCode remains default. Selection
persists separately from house credentials and is reversed through the same menu.
No permanent settings button. Existing Wall-to-Launcher swipe remains in the base.

Local processing always runs first. Only genuine NO_MATCH can use the selected
conversation route. Local successes, missing targets, offline devices and auth
failures must not be sent to the web. No timed voice routines were reintroduced.
Free Chat is a visible browser-backed ChatGPT session: the question is copied
for manual paste/send. Its own login and usage limits apply. The menu discloses
this. No hidden webpage injection, scraping, extracted credentials, direct OpenAI
key, embedded consumer API, or native spoken answer behind the eyes is claimed.
Browser Back is the intended return route; verify that on Ryan's actual phone.

## Build and source boundaries

Always use `scripts/materialize-android.sh`. The repository already applies wake
and toast patches; `scripts/patch-wall-chat-mode.py` follows those during the same
materialization pipeline. It rejects changed/ambiguous anchors and checks
idempotency. Raw checked-in MainActivity remains frozen; the feature is present
in the effective materialized source. Face drawing, wake assets, HA clients,
companion source and the existing stable signer remain protected.

## Resolved CI diagnosis and history

- `5c717c8`: existing implementation found on this branch; run 34069260038 built
  and signed but failed emulator readiness.
- `c1a5e9`: bounded recognizer readiness without weakening the real microphone gate.
- Concurrent `7e06da7` added responsive/scrollable choices and non-all-caps labels;
  its source and subsequent concurrent documentation were preserved.
- `e5c5598` / run 34070255788 exposed Android's first-run fullscreen tutorial
  covering the actual BOOP input surface. `d9a271b` acknowledged only that exact
  disposable-emulator tutorial, guarded by package/resource IDs and unit tests.
- `d9a271b` / run 34070906442 then passed the initial menu/default/Free Chat save,
  but UIAutomator produced no XML just after process restart. The failure was a
  missing capture file, not a failed mode assertion. Focused evidence is in
  diagnostic run 34071469075, artifact 10000594439.
- `0ceb97b` retries only failed fresh UI captures, at most four attempts, deleting
  stale dumps each time. Actual UI assertions are unchanged and never retried
  into a pass. Tests first reproduced missing/invalid capture failures, then
  passed after the bounded fix. The final full run above is green.
- `boop-wall-chat-ci-diagnostics@80f507a` is a historical CI-log helper only,
  not an application branch. It uses read-only Actions permission, no signer.

## Cross-device handoff and preservation

Shared main map/context at `2912a198e3f9f2b67f89a39739159c8172c654f2` directs
Work to this candidate. Main remains the authority for shared decisions; this
handoff owns current candidate evidence. Already-open Work tasks must fetch and
reread it. Ryan's "update memory" instruction means documentation-only commits
and pushes, not changes to app code, permissions, signing or physical installs.
This session used GitHub and a Linux scratch review, not the Windows laptop or
its LAN. No laptop-filesystem synchronization is claimed. Reviewed work is in
GitHub; temporary downloads/test logs are not project changes to commit.

Accepted physical Wall stays `595e1daa43393882a0e5de43967545ac526b8b66`, v29,
run 33992704568, Pixel 7 Pro accepted 2026-09-05. Its annotated checkpoint was
reread live and still resolves there. Preserved Wall branch remains `3a702f8`;
v30 swipe has inherited emulator evidence only. Shield Home/Routines checkpoints,
Launcher, existing workflows and signing credentials are untouched.

Next safe step: Ryan tests the signed v31 update on Pixel 7, including hold/menu,
mode persistence/revert, one local media command, and general-question browser
paste/send/Back. Record actual results before promoting a physical checkpoint.
See BOOP_STATUS.md, BOOP_CHAT_MODE_MEMORY.txt and docs/BOOP-WALL-FREE-CHAT.md.
