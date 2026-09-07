# BOOP shared context

Reconciled 2026-09-07; Wall chat-mode ownership added against live main.
Current user instructions and fresh device evidence win.
This compact file is shared across active app branches; detailed product history
remains in BOOP_MEMORY.txt where present. Use BOOP_START_HERE.md for branch routing.

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

## Wall chat fallback, approved 2026-09-07

OpenCode credit exhaustion must not disable local house/media commands. Ryan
approved holding the eyes for three seconds, past the existing playful hold,
to open Chat mode: OpenCode / Free Chat / Cancel. OpenCode is the default;
the selection persists and the same menu reverses it. No permanent settings
button. Keep Wall-to-Launcher swipe and other existing interactions intact.

Local processing always comes first. Only a genuine NO_MATCH may use the selected
conversation route. Device/auth/offline failures must not become web questions.
Free Chat is a visible browser-backed ChatGPT session, with its own login and
limits; it is not an embedded consumer API or unlimited free service. The current
candidate copies the question for manual paste/send and discloses this in its
menu. Do not add hidden scraping, injection, credential extraction, or a direct
OpenAI key. Native spoken answers behind the eyes are not claimed for this route.

Implementation/test authority: boop-wall-free-chat-wip, SESSION_HANDOFF.md,
BOOP_STATUS.md and BOOP_CHAT_MODE_MEMORY.txt. This isolated candidate does not
replace the preserved Wall app branch or the accepted physical checkpoint.
Wall v30's reviewed launcher swipe is in boop-wall-resurrection; its handoff
records emulator evidence, not a new physical acceptance.

## Native conversation relay, approved 2026-09-07

Ryan approved the stored OpenAI conversation relay design/plan, followed by full
implementation, tests and signed APK delivery. This is a separate Native Chat
choice, not a replacement for OpenCode or the browser Free Chat fallback. Local
processing still runs exactly once first, and only NO_MATCH may call the selected
assistant. Successful native replies reuse BOOP's existing TTS/follow-up/puppetry.
Conversation failures must not clear Home Assistant credentials or imply that a
house command failed. No new device permissions or timed routines are implied.

The provider API key stays only in Worker secrets. Android uses a separate relay
token from private build configuration. Such a token is extractable from an APK:
configured artifacts must remain private, and this variant's public CI refuses
token-bearing builds. Empty configuration remains buildable but means setup is
required. The relay uses separately billed API usage, not consumer ChatGPT credits.
No Worker deployment or live provider call is established by local mock tests.

The phone-chat implementation is on **boop-relay-reviewed-v34**, isolated from a
concurrent implementation on **boop-wall-free-chat-wip**. Both descend from the
plan at 7aa871f. Do not overwrite either session, silently merge them, or mix one
variant's APK/Worker with the other. Use exact commit/artifact receipts; matching
v34 labels alone prove nothing. The isolated branch's SESSION_HANDOFF.md and
BOOP_STATUS.md own its current CI/runtime evidence. Preserve accepted checkpoints;
no physical or end-to-end deployment acceptance is implied by this shared map.

## Unified BOOP direction and release discipline, approved 2026-09-07

After the current puppet update is finished, Ryan wants Wall, Launcher and Shield
merged from their absolute latest live GitHub heads into one canonical BOOP app/APK
that selects the correct body/profile for the installed device. Do not start that
merge from remembered APK filenames, stale local folders or older checkpoints.
Fetch and reconcile the live heads at merge time.

The target release model is intentionally boring and auditable:
- one canonical BOOP APK/package lineage after unification;
- one intentional functional change per update whenever practical;
- every physically accepted build records the exact Git commit/tag, workflow run,
  signed artifact and physical result;
- if a new build breaks, rollback uses the last physically accepted Git
  checkpoint/artifact, never a guessed filename or merely the last numerically
  higher version;
- GitHub history/artifacts are the archive. Local deployment folders are working
  surfaces only;
- after a replacement build is physically accepted, local deployment storage
  should contain only the current signed APK and, if desired, one clearly named
  last-good APK. Superseded random build files should be removed rather than kept
  as an informal backup system;
- prefer a stable deployment name such as `BOOP.apk`; provenance belongs in Git,
  not in an accumulating sequence of ambiguous filenames.

Until unification is complete, existing Wall/Launcher/Shield package identities
and branch ownership remain in force. This direction does not authorize silently
merging the apps early or discarding accepted checkpoints.

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
the Pixel 7 for easier screenshot testing. GitHub is the code/context handoff.
Before edits fetch/check the owning branch and read its handoff; before stopping
record results and publish reviewed scoped work. No blind overwrites/force pushes.
Shared context is not automatic chat-history replication, and an unattached or
offline task must say what it cannot access. "Update memory" means reconcile and
publish documentation only, unless Ryan explicitly also requests implementation.
