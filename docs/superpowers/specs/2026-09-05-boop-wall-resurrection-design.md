# BOOP Wall Resurrection Design

**Status:** Approved direction; awaiting final user review  
**Date:** 2026-09-05  
**Target:** Pixel 7 Pro BOOP Wall (`com.boop.alpha1`)

## Background

BOOP Wall's correct pre-timed resurrection line survives on
`alpha6.5.5-shake-muppet`. It contains the working 33-phrase natural wake
collection, local Home Assistant command path, direct media path, Home Assistant
conversation path that selected BOOP's OpenCode agent, wake sensitivity fixes,
and the later eye/puppet work.

The apparent newer Alpha 6.6.x line forked from Alpha 6.5 before the natural-wake
6.5.3, customization 6.5.4, and shake/puppet 6.5.5 work. Its higher version
number therefore does not make it the correct superset. It contains the timed
routine experiments but omits the later natural-wake lineage. This branch split
caused the earlier reconstruction mistake.

The current Shield development branch builds the same Android package with an
older echo-only `MainActivity`. Installing that stable-signed APK updated BOOP
Wall in place and replaced the smart activity with the echo shell. The missing
behaviour was not erased from Git.

OpenCode separately stopped answering because its authentication token expired.
The user re-authenticated it on 2026-09-05 and confirmed it answers normally
again. The current Home Assistant conversation agent is named `BOOP`.

## Decision

Resurrect BOOP Wall from Alpha 6.5.5 and modernise it with the current secure QR
pairing companion and stable BOOP development signing. Do not rebuild the smart
behaviour on the echo shell or ship the old Alpha unchanged. Preserve all Shield
checkpoints and Alpha history.

Timed voice routines are deliberately excluded. Their automatic follow-up flow
was the unstable part of the otherwise useful Alpha voice system and needs a
separate redesign. Its history remains in Git, but it must not be reachable in
this build.

## Goals

- Restore all 33 established natural BOOP wake phrases and continuous wake
  readiness.
- Restore ordinary conversation through Home Assistant's `BOOP` OpenCode agent.
- Restore immediate local Home Assistant control without depending on OpenCode
  or WAN availability.
- Restore direct media controls and safe target selection.
- Preserve BOOP's eyes, listening/speaking states, voice output, and honest
  plain-English failures.
- Preserve tap-to-talk and the established voice-selection controls as reliable
  fallbacks and regressions.
- Bring forward secure QR pairing and stable signing so normal updates retain
  BOOP's app data.
- Create a checkpoint only after physical Pixel testing proves the complete path.

## Non-goals

- Timed, delayed, scheduled, recurring, or follow-up-confirmed voice routines.
- Changes to the protected Shield overlay or Shield Home/Routines code.
- A new cloud provider or direct OpenAI API integration in Android.
- A second Home Assistant database, permission system, or automation engine.
- New visual design, personality work, or wake phrases beyond the preserved set.
- Treating CI or emulator success as physical verification.

## Source and Integration Strategy

Create an isolated resurrection branch from Alpha 6.5.5, not from a protected
Shield checkpoint. Bring forward only the modern pieces BOOP Wall needs:

1. Stable BOOP development signing and certificate verification.
2. The QR/deep-link Shield pairing companion, manifest entry, and pairing tests.
3. Build hardening needed to compile and exercise the restored source without
   changing protected Shield runtime files.

The package remains `com.boop.alpha1`. A correctly signed APK must install as an
update over the echo build and retain stored app state. Secrets, tokens,
keystores, and private certificates remain outside Git.

## Runtime Architecture

### Wake and capture

The preserved Sherpa spotter, wake controller, audio session, coordinator,
trigger gate, transcript normaliser, and recognition intent form one lifecycle.
A recognised BOOP phrase opens one speech capture. BOOP visibly changes state
while listening and rearms after the request completes or fails.

The source of truth is Alpha 6.5.5's inherited
`wake-assets/boop-kws/keywords_raw.txt`, introduced by
`alpha6.5.3-natural-wake-cook`. It contains exactly 33 phrases, including
`BOOP`, `HEY BOOP`, `EY BOOP`, `HI BOOP`, `HELLO BOOP`, `OI BOOP`,
`OKAY BOOP`, `WAKE UP BOOP`, `ARE YOU THERE BOOP`, `GOOD MORNING BOOP`,
and `EXCUSE ME BOOP`. Every tokenized variant maps to the same `@BOOP` event.
Restore that asset intact rather than recreating the collection from memory.

### Command routing

Each completed transcript is processed once:

1. Existing on-device interceptions such as BOOP's voice controls retain their
   established priority.
2. The local Home Assistant client gets first refusal. Inside that client,
   established colour and direct-media handling run before the local Home
   Assistant conversation request.
3. A genuine local `NO_MATCH` is forwarded to general conversation.
4. The general client discovers Home Assistant conversation agents and selects
   the current `BOOP` agent, while retaining compatible BOOP/OpenCode matching
   for installations whose entity ID still contains `opencode`.

The router never sends a command to both paths after one succeeds.

### Local home control

Immediate home commands use Home Assistant locally and preserve room/entity
boundaries. Basic control remains useful if OpenCode or the internet is down.
BOOP reports authentication, connection, ambiguity, and unsupported actions
plainly and never invents success.

### Media control

Restore the direct media client and selector from the smart Alpha. Playback,
pause, stop, next/previous, and supported volume commands target the best valid
media player using established area/state evidence. Equal top candidates are
ambiguous: BOOP declines or falls back safely rather than guessing.

### Ordinary conversation

Unmatched ordinary speech goes through Home Assistant's conversation API to the
`BOOP` OpenCode agent. Android stores no OpenAI key and does not authenticate
directly with OpenAI. OpenCode authentication belongs to its Home Assistant
integration.

If the agent is unavailable or its external authentication expires, BOOP says
conversation is unavailable while local home and media control remain working.

### Pairing and credentials

Retain the current Shield QR pairing companion: credentials do not appear in the
QR payload, and the phone relays Home Assistant's short-lived authorization code
to the Shield over the pinned transport. This companion remains separate from
BOOP Wall's own existing `SecureTokenStore` and Home Assistant authentication.
It must not overwrite the Pixel's connection or become a second BOOP Wall
runtime.

## Removing Timed Routines

The build must not instantiate, route to, prompt for, or advertise the timed
routine flow. Tests prove timed phrases do not enter its old automatic follow-up
state machine. Historical classes may be omitted from assembled source or left
unreachable if physical omission adds needless integration risk; either way,
there is no runtime entry point.

This does not affect Shield Routines. Shield automations, scripts, and scenes
remain `Routine` under their protected checkpoint.

## Failure Behaviour

- Wake failure: keep eyes usable and show a plain error instead of pretending to
  listen.
- Speech no-match: rearm cleanly; never enter timed follow-up.
- Home Assistant unavailable: report that the home connection is unavailable.
- Home Assistant authorization rejected: direct the user toward pairing again.
- OpenCode unavailable or logged out: report conversation unavailable without
  disabling local commands.
- Ambiguous device or media target: do not guess.
- Every completion or failure releases microphone resources and rearms wake
  detection without overlapping capture sessions.

## Automated Verification

Tests must protect:

- all 33 preserved natural wake phrases, their shared `@BOOP` mapping, and
  transcript normalisation rules;
- trigger gating, one capture at a time, and wake rearming;
- local success stopping before OpenCode;
- local no-match reaching `BOOP` exactly once;
- discovery by current BOOP name and compatible historic BOOP/OpenCode IDs;
- OpenCode failure not breaking local home or media commands;
- direct media selection, ambiguity refusal, and service mapping;
- tap-to-talk and established voice-control interception;
- timed prompts and follow-up capture being unreachable;
- secure pairing deep-link and credential handoff;
- stable signer and update-compatible package identity;
- clean emulator launch without a fatal exception.

## Physical Acceptance Gate

The signed APK is installed as an update on the Pixel 7 Pro. BOOP earns a new
physical checkpoint only when all these pass:

1. Eyes launch and remain responsive.
2. Representative short, greeting, attention, and wake-up variants from the
   33-phrase collection wake her; `BOOP <command>` also works continuously; she
   rearms cleanly.
3. Ordinary speech receives a useful response from the Home Assistant `BOOP`
   OpenCode agent and speaks/displays it normally.
4. Immediate home commands change the correct devices, including a room-scoped
   command.
5. Media play/pause and another available media action control the intended
   player.
6. With OpenCode unavailable, local home control still succeeds and BOOP
   describes conversation failure honestly.
7. No timed-routine prompt or automatic timed follow-up appears.
8. The QR pairing companion still opens correctly when invoked by Shield.

CI-green and physically green remain separate labels. Failed physical checks are
recorded before further changes; protected Shield tags are never moved.

## Rollback and Checkpointing

The echo build, Alpha 6.5.5 and 6.6.3 branches, and Shield checkpoints remain recoverable.
No existing tag is rewritten. If the resurrection regresses the wall device,
repair forward from the isolated branch or reinstall the last appropriate signed
package.

After physical acceptance passes, create a new annotated BOOP Wall checkpoint at
the exact tested code commit and update `BOOP_MEMORY.txt` and `BOOP_STATUS.md`
with the physical evidence.
