# Shield voice to Deezer Cast

Date: 2026-09-07. Status: architecture approved by Ryan; implementation is not
approved until this written design is reviewed. Music Assistant installation and
Deezer authorization are an external, user-run prerequisite through OpenCode.

## Outcome

BOOP replaces Google Assistant for the supported music path. It does not hand a
request to Google Assistant, require Google Assistant to be enabled, or use it as
a fallback. A user can say ordinary variants of a request for music to BOOP; BOOP
asks the local Home Assistant/Music Assistant stack to resolve Deezer and play it
on the Cast receiver that belongs to the Shield running BOOP. The Shield overlay
then reacts to the resulting Deezer media session as it already does.

This solves the Deezer Android-TV curtain problem by using the calm Deezer Cast
receiver surface while retaining BOOP's music puppetry.

## Boundaries and prerequisites

- The protected transparent overlay stays non-focusable, non-touchable and has no
  microphone. Voice capture belongs to a separately owned Shield voice component;
  it must not be added to `BoopOverlayService` or its overlay window.
- Basic command routing remains local through Home Assistant. OpenCode may
  interpret an unrecognised request but is never a required dependency for the
  direct music command.
- Music Assistant must be installed in the user's Home Assistant, with the Deezer
  provider authorized by the user and its Google Cast provider enabled. BOOP never
  receives, persists or logs Deezer credentials.
- The user must explicitly allow the microphone permission for the separate voice
  component. No accessibility, usage-history or privileged permission is added.
- The branch's existing Deezer notification-listener access is observation-only;
  this feature does not broaden it, repurpose it for control, or modify its
  puppetry policy.

## Command contract

The voice layer normalizes direct, local music requests into one command shape:

`PLAY_MUSIC(query?, destination?)`

The matcher accepts the single-word request `music` and ordinary natural variants
such as "play music", "put music on", "start some tunes", "I want music", and
polite/filler-word forms. It does not claim an impossible exhaustive list of all
spoken language. Explicit musical queries become `query`; explicit destination
phrases become `destination`. The existing conversation route receives requests
that do not meet the direct command grammar, but it must return the structured
command or a clear failure--never secretly invoke Google Assistant.

If a destination is absent, BOOP uses the discovered Cast player mapped to the
current Shield. It identifies the receiver by a stable provider/player identity,
not an IP address, package name, or Ryan-specific friendly name. At first setup
BOOP compares locally available Shield identity/network evidence with Music
Assistant's discovered Cast players. It asks the user to choose only when the
match is ambiguous. An explicit destination always wins; no plausible but
different speaker is silently substituted.

## Local service contract

The Shield voice component calls the existing authenticated Home Assistant path
with a dedicated BOOP music command. Home Assistant is responsible for resolving
the request through Music Assistant and issuing playback to the selected Cast
player. The exact Music Assistant API/action and required provider/entity
identifiers are discovered during setup and stored as a user-visible binding.

The client has four outcomes: started, setup-required, unavailable, and failed.
It reports each in plain language and waits for actual provider/player feedback
before calling playback started. It does not open Deezer's Android-TV activity,
send shell commands, alter Android animation scales, or fall back to a random
media player.

Music Assistant, rather than BOOP's Android process, supplies the authenticated
Deezer playback mechanism. Generic Google Cast APIs only load direct media URLs;
they are not used to bypass Deezer authentication or imitate Deezer's private Cast
protocol.

## Flow

1. A user speaks to the separate BOOP voice surface.
2. The local matcher produces `PLAY_MUSIC`, with an optional request and target.
3. The target resolver selects an explicit user target, otherwise the saved
   current-Shield Cast binding, otherwise shows setup-required.
4. Home Assistant forwards the structured command to Music Assistant.
5. Music Assistant searches/chooses Deezer content and starts it through its Cast
   provider.
6. BOOP receives a success only after the local route reports playback started.
   Existing Android media-session observation independently animates H1 once
   Deezer playback is actually seen.

Failures remain attributable: microphone denied, no recognized music request,
Music Assistant/Deezer unconfigured, Shield Cast target missing/ambiguous, no
matching Deezer item, or playback failure. Nothing is retried indefinitely and no
other player is chosen without consent.

## Verification

Automated tests cover phrase normalization, explicit-target precedence, Shield
binding persistence/absence/ambiguity, service request formation, and every
failure outcome. Existing overlay, Home, Routines, pairing, authentication and
Deezer-puppet tests must remain green.

Physical acceptance happens after the user completes Music Assistant setup:

1. Disable/remove Google Assistant and confirm BOOP has no fallback path.
2. On a clean user setup, discover the local Shield Cast receiver without a
   hard-coded IP/name; verify ambiguous discovery asks rather than guessing.
3. Say `music`, a common request variant, a content-specific request and an
   explicit destination request. Confirm each applicable one resolves correctly.
4. Verify Deezer uses its Cast surface, BOOP dances only on observed playback,
   remote input still passes through, and pause/resume works.
5. Verify Deezer authorization loss, unavailable Cast target and unknown request
   yield a clear BOOP response without invoking Google Assistant or disturbing
   existing pairing/settings.

No checkpoint is created until the signed build passes the applicable physical
steps. This design does not authorize an app install, permission grant, deletion
of Google Assistant, or a Home Assistant configuration change by Codex.

## Durable decision

Ryan chose to remove Google Assistant and retain its useful household music
experience through BOOP's local Home Assistant route. BOOP must not depend on, or
silently delegate this functionality to, Google Assistant.

## Self-review

Checked for a hidden Google fallback, hard-coded device ownership, direct Deezer
credential handling, microphone expansion into the overlay, unconfirmed playback
claims, and silent target substitution. Music Assistant setup remains explicitly
external and user-operated.
