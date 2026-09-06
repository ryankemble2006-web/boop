# Shield Deezer puppet checkpoint

Date: 2026-09-06. Status: written integration design approved by Ryan ("happy").
He selected helper-agent implementation with reviews between parts. Implementation
is complete through task-scoped review; final audit/whole-change review is pending.
Only a local debug candidate exists, not an installable update or physical checkpoint.
Ryan approved committing
and pushing only reviewed Deezer changes plus required approved H1 artwork/motion
to the separate animation branch, then running existing signed CI after checks and
reviews pass. No main-branch merge, installation or Android grant is authorized.

## Outcome and scope

Ryan chose: "lets do deezer first so we get a nice checkpoint". The first integrated
APK uses the approved H1 Soft groove headphones and the accepted gentle nod. Keep
the 1.2x preview pace: one cycle every three seconds, not beat synchronisation.
Paused headphones rest; resume continues the nod. Stopped playback or lost access
returns the original eyes. Track updates do not continually restart the animation.

Deezer is selected by the observed exact package `deezer.android.app`. Playback
may continue in the background; foreground-app detection is not part of this slice.
Do not reinterpret opening another app as Deezer stopping. A paused Deezer session
keeps resting headphones until its state changes or the session disappears.

Excluded: Kodi/Forki detection and popcorn runtime, Armin and Queen/Freddie Easter
eggs, artwork generation, new gestures, audio/beat analysis, metadata display or
storage, playback commands, Home Assistant changes, voice changes and boot startup.
Existing H1/P1 preview assets and uncommitted work are preserved.

## Chosen approach and evidence

Use Android media-session callbacks inside the Shield app, with a separate observer
feeding a small local state holder and the renderer. No HA socket or internet is
in this animation path. Compared with routing state through HA, this avoids adding
HA availability and another hop to the puppet's reaction. Compared with combining
Deezer and foreground-driven cinema now, it keeps the first checkpoint on the signal
already proven on the physical Shield.

The separate disposable probe physically received play, pause, resume and next
callbacks, including short background delivery. It did not render BOOP. Its
temporary notification access was revoked and is not permission for this app.
The standard notification-access settings activity was absent on the tested Shield.
The first checkpoint therefore permits a separately approved developer-assisted
grant; it is not a claim that consumer permission setup is solved.

## Components and change boundary

1. A dedicated notification-listener service observes only Deezer controllers after
   Android connects it. Register session-list and per-controller callbacks before
   reading initial state. Detach missing/destroyed controllers and reject stale
   callbacks using session identity and a registration generation. Release all
   observers on disconnect, access denial, opt-out or destruction. Do not copy the
   probe's metadata collection or log history into working BOOP.
2. An application-scoped, main-thread state holder exposes immutable snapshots and
   subscription/unsubscription. It retains no Activity or View. A pure Java policy
   maps eligible session states to the three visual states below. Android callbacks
   are adapters; policy and timekeeping are independently testable.
3. A pure motion clock accumulates only visible, enabled, playing time. It passes
   elapsed time at 1.2x to the existing `MediaPuppetMotion.music` sampler. Repeated
   equivalent snapshots do nothing. Pausing/hidden intervals never become a jump
   when rendering resumes. Entering ordinary eyes resets the next music cycle.
4. A dedicated H1 renderer draws the existing transparent music asset. The existing
   eye renderer remains the fallback. The view owns animation-frame scheduling and
   stops it when resting, hidden, detached or the display is off. No new animation
   timer or polling loop is added to `BoopOverlayService`.
5. A small Settings addition provides the feature switch, access status and a
   user-initiated access-settings action. Only Settings composition in
   `BoopHomeActivity` may change; pairing, authentication, favourite control and
   Routines logic are out of scope.

Expected touched areas: new observer/state/policy/clock/H1 renderer and geometry
helpers, tests and one drawable; narrow changes to the manifest, overlay service
and view for state subscription/render selection, and Settings wiring/copy.
Keep `HomeAssistantRepository`, `FocusCardView`, `OverlayGeometry`'s ordinary-eye
calculation and `OverlayWindowSpec` unchanged. Any necessary change to a protected
runtime integration point starts with a failing regression, not deletion or
weakening of its existing contracts.

## State and timing contract

| Observed condition | BOOP result |
| --- | --- |
| Feature off, no grant, disconnected, no Deezer session, null/unknown/error/NONE/STOPPED state | Original eyes; no repeating motion |
| Deezer PLAYING | H1 headphones; gentle repeating nod |
| Deezer PAUSED | H1 headphones; freeze the current pose |
| BUFFERING or CONNECTING | H1 resting pose; never pretend playback is confirmed |
| SKIPPING_TO_NEXT / PREVIOUS / QUEUE_ITEM, FAST_FORWARDING or REWINDING | Keep H1 and hold its current pose until the next confirmed state; no new gag |
| BOOP Home visible, overlay hidden/detached, or display off | No overlay animation frames; retain latest state for return |
| System animations disabled | Static H1 when otherwise appropriate; original eyes in fallback states |
| Public Android Power Saver active | Conservatively hold static H1; resume held phase after it ends |

On an initial non-playing headphone state, use the neutral time-zero H1 pose.
No elapsed song time, speed, `active` flag or metadata change is treated as proof of
PLAYING. The probe saw speed 1.0 while PAUSED. A track change within the same live
session does not reset the clock. Session loss returns ordinary eyes; attaching a
later session starts fresh rather than reviving stale motion.

Do not apply a blanket 300-500 ms debounce: the observed explicit next transition
lasted about 59 ms. State changes are handled on the next available frame without
starting separate skip choreography. No polling and no animation restart for
duplicate PLAYING callbacks. If a transitional state persists, remain at rest;
do not invent a return to PLAYING on a timeout.

If more than one Deezer controller exists, prefer PLAYING controllers; retain the
current selection among equally eligible controllers. Otherwise retain a current
controller with a supported headphone state, then choose the first such controller
in Android's supplied list. With no eligible controller use ordinary eyes. Reconcile
the complete controller list before publishing selection changes so temporary map
updates do not flash the fallback. No other app's session is a substitute.

## Rendering and protected behaviour

Preserve transparent, upper-right, non-focusable and non-touchable overlay behaviour,
hide-on-Home/show-on-leave, the one-time ordinary-eye wake and display-mode recovery.
No microphone, network client or HA dependency enters the overlay classes.

Use the cleaned H1 music PNG without repainting it. Match the accepted corner study:
approximately 14% of display width for the visible eye pair, with room for the larger
headphones. The H1-only geometry helper reserves the full alpha/motion envelope
(including tilt), with at least 3% top and right clearance. The ordinary-eye geometry
remains exactly its existing calculation. Reuse the same overlay view/window and
update layout only for mode or actual display-geometry changes, not every frame.
Verify the motion envelope mathematically and visually; no clipped ear cups, opaque
rectangle or oversized full-screen overlay window is acceptable.

Review refinement: read the authoritative animator-duration setting rather than a
separately refreshed ValueAnimator cache. Also observe public Power Saver changes and
hold motion while it is active, without private APIs or polling. This conservative
choice can hold BOOP still even on a vendor device that permits animations in Power
Saver. Unexposed vendor policies and physical delivery remain unverified.

Decode the asset once, not per frame. No media observations or network work in draw
methods. Observe system animator enablement and display state through lifecycle or
change callbacks; resume from the held phase, without polling. Unregister all such
callbacks when their owner is destroyed. Do not widen the existing wake animation
into a repeating idle animation.

## Consent, Settings and failure behaviour

Default the new feature preference to OFF, including existing installations. It is
stored separately from credentials. Remote-selecting On explains that Android's
notification access is broader than playback access: BOOP will ignore notification
contents and observe only Deezer playback. The app never grants access itself.

Settings shows Off, Access needed, Connecting or On using real preference, grant and
listener state, not a hopeful toggle. An explicit access action attempts Android's
settings screen and catches an unavailable screen safely. On this Shield, show a
short explanation that one-time computer setup is needed and ordinary eyes still
work. Do not open unrelated screens, loop rebind attempts, run shell commands from
the app, or suggest clearing BOOP data.

Switching Off immediately releases media observers and restores ordinary eyes. It
does not erase pairing or pretend to revoke Android's separate grant. Keep the
access status visible; the access action explains how to remove that grant where
the device permits it. Turning back On with an existing grant can explicitly
request reconnection and must take a fresh snapshot. A Settings resume refreshes
status after the user returns from Android settings. Left/Back navigation and focus
order must remain predictable, including when status text changes.

Declare the new listener as non-exported and protected by
`android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`, with its required service
intent filter, as in the successfully bound disposable probe. No accessibility,
usage-history, privileged media-control, audio-recording or boot permission is added.
Notification callbacks are no-ops; no notification enumeration, cancellation or
payload parsing. No playback control calls. No title, artist, token or notification
contents in diagnostics or persistent storage. Only fixed state/status labels are
needed for this checkpoint.

Any developer-assisted grant is a later explicit approval for the exact BOOP
component and Android user after checking the target and existing listener baseline.
Never reuse the probe grant, replace the whole listener list or alter other apps'
access. Agree cleanup/retention with Ryan and verify the resulting access state.

## Verification and checkpoint gate

Start with a failing pure-Java regression: PLAYING advances motion, PAUSED holds it
even if upstream speed is 1.0, and resumed/duplicate PLAYING continues from the held
phase rather than restarting. Then cover every state-table row, off/grant loss,
controller selection and stale callbacks, reconnect snapshots, hidden-time exclusion,
short skip transitions, large/negative clock inputs and geometry bounds.

Run the existing Shield unit and source regression suites, H1 motion/export tests,
build and Android lint. Keep the working Home/Routines/authentication contracts
passing. Audit the packaged manifest, H1 alpha asset, package identity, version and
certificate. Label local tests, CI results and physical evidence separately.

Use `com.boop.shieldoverlay` and the existing stable BOOP signer, checked against
`shield-overlay/signing/boop-dev-cert-sha256.txt` and the installed package. Do not
install a locally debug-signed build over working BOOP, uninstall it, clear its data
or introduce another signing identity. The repository's existing stable-signing
workflow is the known route; local stable signing availability must be checked
without printing secrets. If obtaining the signed APK needs a branch push or CI
dispatch, confirm the publication boundary before doing it. Preserve a known-good
signed APK and record a data-preserving recovery route before any update.

One physical step at a time on the Shield:

1. Install an approved, correctly signed update without data clearing. Pairing and
   room remain; with the feature Off, ordinary eyes and Home/Routines still work.
2. Enable only after the agreed permission step. Start Deezer: H1 is correctly sized,
   transparent and gently nodding at the approved pace. Remote still controls Deezer.
3. Pause, resume and skip: rest, continuous nod and no flashing/repeated reset.
4. Open BOOP Home and return: overlay hides, then returns correctly. Exercise a real
   Home favourite and a Routine; verify room/auth/navigation have not regressed.
5. Stop playback/session, switch the feature Off, then restore it: correct ordinary-eye
   fallback and fresh recovery, with no re-pairing. Test permission revocation and
   reconnection separately with explicit approval and documented cleanup.
6. Exercise display-off/on, available resolution/HDR transitions and a BOOP process
   restart; check clipping and recovery. Observe a ten-minute playback period and a
   paused interval for runaway animation work or visible stutter. Automated counters
   must show no repeating frames while paused/hidden, not merely a still screenshot.

Only seal a new uniquely named Deezer physical checkpoint after the applicable gate
passes, recording source commit, APK hash, signing fingerprint, device, permission
state, test results and any explicitly accepted limitations. Missing HDR coverage or
consumer permission UX remains labelled unverified; do not silently call it green.
Never move the existing Home, Routines or Wall checkpoint tags. This design approval
alone does not create, install or physically verify a checkpoint.

## Supporting references

- Approved preview: `2026-09-06-shield-media-motion-preview.md` in this directory.
- Probe evidence: task output `outputs/boop-shield-concepts-2026-09-06/BOOP_ANIMATION_MEMORY.txt`.
- Android requires waiting for listener connection and cleaning up listener resources:
  [NotificationListenerService](https://developer.android.com/reference/android/service/notification/NotificationListenerService).
- The system animation-enabled flag is available from API 26:
  [ValueAnimator.areAnimatorsEnabled](https://developer.android.com/reference/android/animation/ValueAnimator#areAnimatorsEnabled()).

## Self-review

Checked for placeholders, contradictory states, hidden permission grants, scope
growth, frame work while hidden, stale observer ownership and signing/checkpoint
claims. Production-friendly Shield access setup is deliberately excluded from this
developer-assisted checkpoint, not left as an implied implemented capability.
Written-spec approval received; implementation planning and reviewed execution follow.
