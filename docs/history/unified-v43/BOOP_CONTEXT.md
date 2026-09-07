# BOOP shared context

Reconciled 2026-09-07 for the unified v43 dock/mirror + Shield settings pass.
Current user instructions and fresh device evidence win.
This compact file is shared across active app branches; detailed product history
remains in BOOP_MEMORY.txt where present. Use BOOP_START_HERE.md for branch routing.
Fetched main is the authority for newer shared context across other app branches.

## Identity and decisions

BOOP began as a useful smart speaker in a cute, ownable plastic robot. Pixel
Wall, Shield and Launcher are different bodies/prototypes in the same family,
not reasons to discard the robot vision. BOOP is an honest expressive puppet,
not a claim of sentience or emotional dependence. Blue/cyan eyes, humour,
manners, accessibility and repairability matter. Yellow articulated hands come
from the early ASL/BSL direction; eating pantomime is not validated sign language.

Home Assistant is the local authority. Immediate house and media control must
remain local; ordinary open-ended conversation may use HA's OpenCode agent named
BOOP. No direct OpenAI secret belongs in Android. Never invent entity IDs or
quietly control a similarly named device in another room.

Automations, scripts and scenes are all called **Routines** in user-facing UI,
while their execution semantics remain distinct. Voice routine creation requires
an explicit creation request, a plain-English proposal and confirmation (including
polite wording such as yes please). Timed voice routines were removed pending
redesign. Routine-authoring capability evidence is not proof of a finished feature.

## Unified v43 dock, mirror and Shield-room decisions — 2026-09-07

Owning branch: `boop-unified`. Ryan physically tested unified commit
`e746affbb82b577cef2f1cf6e731dff186c8f881` and said the APK was fine to modify;
that remains the last physically accepted unified rollback point until v43 is
physically tested.

Current built candidate: versionCode 43 / `1.1.0-unified-dock-mirror-shield-settings`,
code commit `950611df0235d3943bf9958153efa470a342036b`. GitHub Actions run
`34117631109` is green; artifact `BOOP-Unified` ID `10017287954`; APK SHA-256
`95ba6292c04edaa4db2f1028337f0b3009a7c5006ee9b423e1bc40a9addef4fb`.

Durable behavior:

- Undocked handheld Wall mode is tap-to-talk; continuous local wake capture stays
  disarmed. Wireless charging is the physical dock signal that permits foreground
  wake-word listening.
- Docked eyes may sleep independently of wake listening. A cheap proximity nudge
  may trigger a brief front-camera presence peek; continuous idle image recognition
  is deliberately avoided because heat still matters while wirelessly charging.
- “BOOP mirror” / “Hey BOOP mirror” and natural polite/open variants explicitly
  open the front-camera mirror. Natural stop/close/exit/back-to-BOOP variants close
  it. Mirror is the deliberate continuous-camera exception because the user asked
  to see the live feed.
- Horizontal mirror layout reserves `INSIDE` and `OUTSIDE` sensor rails. Actual
  Home Assistant sensor entity selection is deferred until those sensors exist;
  do not invent mappings.
- Shield settings are television-first: large grouped cards, generous spacing,
  BOOP black/cyan identity, obvious focus state and D-pad/Enter navigation. Do not
  turn it into a stretched phone settings page or add decorative settings motion.
- Shield controls shown in Home are fail-closed to the selected HA area. HA first
  expands the selected area target so device-level area membership is respected;
  BOOP then filters the resulting cards locally as defense in depth. BOOP may read
  area/device/entity membership but must not move, rename or reassign HA objects.
  If room membership cannot be confirmed, hide controls instead of exposing the
  house. Legitimate controls remain actionable only after surviving this filter.

The v43 CI run covers preserved Wall guards, Launcher tests/lint, Shield unit tests
including room-scope and navigation-model coverage, unified dock/wake/mirror unit
tests, permanent signer setup, signed APK build, real unified Shield-entry emulator
smoke, package/version/manifest/signer verification and artifact upload. This is
CI/signer/emulator green, not physical acceptance. Real handheld/tablet dock,
camera/thermal/mirror behavior and real Shield remote/HA inventory still require
Ryan's device test. See `docs/BOOP-UNIFIED-V43-MEMORY.md`, `BOOP_STATUS.md` and
`SESSION_HANDOFF.md`.

## Current evidence, not a blanket release claim

- Shield Home and Routines: protected physically verified functional checkpoints
  checkpoint-shield-home-f8e8135 and checkpoint-shield-routines-3fa18c6.
- Wall: Alpha 6.5.5 is the correct preserved voice/wake lineage. Later-numbered
  6.6 timed branches were not its superset. Restored Wall code 595e1da was signed
  and physically tested: natural wake, conversation, immediate lights/media,
  QR pairing and current firm shake threshold. Protect checkpoint-boop-wall-595e1da.
- Shield H1: approved 3D graphite/cyan headphones; gentle 1.2x preview pace.
  Latest functional placement code 4fe28a4 was signed by GitHub run34014467071.
  Ryan confirmed dancing, pause response and then "awesome placement".
  Do not extrapolate to all HDR/lifecycle/layout/long-soak cases.
- P1 popcorn (red/cream tub, yellow hands) has an approved local motion preview,
  not Kodi/Forki production integration. Armin hand puppets and any-Queen-track
  Freddie gag are saved ideas, not implemented features.
- Fanart-aware relocation: MediaSession artwork-presence checks did not distinguish
  the user-labelled fanart/curtain pair. A narrow private asset-reference inspection
  found an internal nullable background model. Its proposed background-picture
  label was absent from the paused test's external UI tree. No usable external
  signal or automatic positioning implemented. See Shield handoff.
- Foreground-aware minimise/return and minimal-button first-start setup remain
  separate, unimplemented follow-ups. Shell visibility does not prove normal
  app access. Do not silently grant Android access.

## Wall Free Chat and faster blink, 2026-09-07

Owning branch: `boop-wall-free-chat-wip`; v33 application/build commit
`e3507bde3f296dcb419a1dcef0faf735c7243525`. Exact artifact and signing receipt:
`docs/BOOP-WALL-V33-BUILD-RECEIPT.md`. Read SESSION_HANDOFF.md for evidence limits.
The three-second OpenCode/Free Chat menu remains reversible and persistent.
Immediate house/media stay local first. Browser chat requires manual paste/send
and has its own login/limits; no invisible backend or direct API key.

Ryan physically confirmed v31's query-copy/new-chat/paste-instruction handoff.
v32 implements 1.5x bold two-line instructions and irregular 3-7 second awake-idle
blinks without extra taps/screens/permissions or resetting sleep. Ryan now reports
v32 "he blinks just fine". Do not treat the old failed automated frame capture as
proof of broken physical blinking. Text readability, sleep and every orientation
were not confirmed by that comment. Exact old CI gaps remain archived in
`docs/BOOP-WALL-V32-HANDOFF.md`.

At Ryan's explicit request, v33 changes only the blink duration 220 -> 183 ms
(1.2x speed, rounded) plus version metadata. Blink gaps and all other app behavior
remain unchanged. No tests/emulator runs for this update. Existing signing workflow
has an opt-in [boop-build-only] marker; use it only on an explicit no-tests request.
Normal builds retain full checks. Build/sign-only is not runtime acceptance.

Last full-green v31 run34071614834, built0ceb97b; its history is archived in
`docs/BOOP-WALL-V31-HANDOFF.md`. Protect accepted physical Wall595e1da and separate
preserved branch3a702f8. Shared ownership/main and other app branches unchanged.

## Protect the working puppet

Shield overlay remains non-focusable, non-touchable, no microphone and no HA
socket. Keep ordinary remote input passing through. Shared Home/Routines use the
existing authenticated socket. Preserve manual control, auth, signing and
checkpoints. User chose to keep Android animations enabled after an old animator
scale tweak stopped motion; do not silently undo that decision.

CI-green, locally built, physically tested and design-only are different states.
Record them separately. Never manufacture a physical checkpoint from a CI pass.

## Cross-device working agreement

Ryan develops Shield on the laptop and Launcher/Wall through Android, including
Pixel 7 for easier screenshot testing. GitHub is the code/context handoff. Before
edits fetch/check the owning branch and read its handoff; before stopping record
results and publish reviewed scoped work. No blind overwrites/force pushes.
Shared context is not automatic chat-history replication. Already-open Work tasks
must reread fetched main and the owning handoff. "Update memory" remains docs-only
unless Ryan explicitly requests code, permissions, installs or signing changes.
This GitHub session does not imply access to or synchronization of laptop files.
