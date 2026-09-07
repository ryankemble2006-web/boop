# BOOP shared context

Reconciled 2026-09-06; Wall candidate evidence updated 2026-09-07.
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
- Next Shield UI question: revisit settings and the rest of the living-room
  inventory, after reading current source/HA evidence; it has not been diagnosed.

## Wall Free Chat and v32 polish, 2026-09-07

Owning branch: `boop-wall-free-chat-wip`; current signed test APK v32, built
`6e48e3bc05f7269376d54179ab32025e1a0b72b9`. Read its SESSION_HANDOFF.md for exact verification.
The three-second OpenCode/Free Chat menu remains reversible and persistent.
Immediate house/media stay local first. Browser chat requires manual paste/send
and has its own login/limits; no invisible backend or direct API key.

Ryan physically confirmed v31's query-copy/new-chat/paste-instruction handoff.
He approved bigger bold two-line instructions and gentle 3-7 second awake-idle
blinks, never resetting sleep. Both are implemented in v32 with no extra taps,
screens or permissions. Full latest CI is NOT green: compile/signing/wake/menu
checks pass, but the new natural-blink capture fails. Text spans/callbacks pass;
actual screenshots do not verify text visibility/size. Earlier partial portrait
blink/sleep evidence is not a complete release pass. v32 physical visuals and
landscape remain pending. Do not promote a checkpoint or hide this test gap.

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
