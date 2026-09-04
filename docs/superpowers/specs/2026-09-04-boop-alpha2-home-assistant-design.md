# BOOP Alpha 2 — Home Assistant Control Design

Date: 2026-09-04
Status: Approved design awaiting implementation plan

## Goal

Turn the working BOOP Alpha 1 puppet interaction into the first useful BOOP Wall behavior:

**tap BOOP → speak naturally → Home Assistant performs a real local action → BOOP acknowledges it**

The first physical acceptance target is the spare Pixel 7 Pro acting as **BOOP Wall / Living Room** and controlling the existing Govee fan exposed to Home Assistant Assist as `Fan` in the `Living Room` area.

Alpha 2 must preserve the core rule that **Home Assistant is the authority for the house**. BOOP is the puppet interface, not a second automation platform.

## Locked principles

- BOOP is a puppet, not a fake person or autonomous co-pilot.
- Ordinary Home Assistant controls execute immediately.
- BOOP does not maintain its own device database, aliases, services, scenes, or permissions list.
- If Home Assistant exposes an entity to Assist, BOOP may control it. If Home Assistant does not expose it, BOOP does not bypass that boundary.
- The local control path must continue to work when OpenAI/cloud services are unavailable.
- Cloud AI may add wording and charm after an action, but must never be required to switch a device.
- Failures are reported plainly. BOOP never guesses that another same-named device is a suitable substitute.
- Creating, changing, or deleting an automation is a later capability and must require a plain-English confirmation before writing anything.

## Device identity and room context

Each BOOP device has one saved Home Assistant area that represents where that physical BOOP lives.

For this first Pixel:

- Device role: `BOOP Wall`
- Home area: `Living Room`

Future Android devices use the same application and save a different area, for example `Bedroom`.

### Command resolution rule

BOOP must preserve explicit room information spoken by the user. If the user does not name an area, BOOP qualifies the request with its saved home area before sending it to Home Assistant.

Examples for Living Room BOOP:

- `turn on the fan` → send an equivalent request for `turn on the fan in the living room`
- `turn on the bedroom fan` → preserve `Bedroom`; do not rewrite it to Living Room
- `turn on both fans` → preserve the explicit multi-device request and do not inject a room

The room-context step must be conservative: explicit room or multi-target wording always wins. BOOP is not a general natural-language parser and must not reinterpret device intent beyond supplying its own room when the request is otherwise unqualified.

## One-time connection experience

There is no traditional setup wizard.

### Home Assistant discovery

On first use of Home Assistant control, BOOP looks for a Home Assistant instance on the local network using Home Assistant's documented native-app discovery path.

User-facing flow:

1. BOOP discovers the local Home Assistant server.
2. BOOP shows a minimal prompt: **“I found your house”** with a Connect action.
3. BOOP opens Home Assistant's normal authorization/sign-in flow.
4. The user authorizes BOOP once.
5. BOOP stores the resulting refresh credential securely and obtains/renews short-lived access tokens itself.

No token copying, YAML, manual endpoint entry, or settings maze is part of the normal path. A manual URL fallback may exist later for networks where discovery is unavailable.

### Area identity

The first Alpha 2 build does **not** add an area picker. This Pixel is deliberately fixed to `Living Room` for the first physical control milestone.

The later reusable-device setup prompt is one minimal question:

**“Where am I?”**

At that point BOOP will present Home Assistant areas and store the chosen area locally, allowing the same app build to become `BOOP Bedroom`, `BOOP Kitchen`, and so on without a traditional setup wizard.

That prompt is explicitly deferred so it cannot block the first real Home Assistant device command.

## Home Assistant command handoff

The speech recognizer produces a transcript as proven in Alpha 1.

Alpha 2 command flow:

1. User taps BOOP.
2. BOOP listens using the existing working speech path.
3. BOOP receives the transcript.
4. BOOP adds its saved area only when the request is unqualified; explicit area or multi-target wording is preserved.
5. BOOP sends the resulting text to Home Assistant's conversation system.
6. Home Assistant resolves the entity/area and executes the action.
7. BOOP evaluates Home Assistant's result.
8. BOOP acknowledges success or reports failure.

Home Assistant remains responsible for entity resolution, exposed entities, areas, integrations, state, and execution.

## Authority model

BOOP gets no separate per-device permission system.

The rule is:

**Exposed to Home Assistant Assist → BOOP may control it.**

BOOP must not create a second permissions list that can drift from Home Assistant.

This also means future BOOP devices inherit the same Home Assistant authority model automatically; their saved home area only changes how unqualified room-local requests are resolved.

## Success replies: machinery in, puppetry out

Home Assistant's machine-oriented result is not spoken verbatim when a normal action succeeds.

### Online/cloud-enhanced reply

After the Home Assistant action has already completed successfully, BOOP may ask OpenAI for a fresh, very short puppet-style acknowledgement.

Only the minimum result context needed for the wording is sent, for example:

`living room fan turned on successfully`

The cloud reply must be:

- one short line
- playful and unpredictable
- clearly puppet-like rather than faux-human
- free of emotional claims or invented state
- unnecessary to the action itself

Examples of the desired flavor include short acknowledgements such as `Boop. Wind acquired.` or similarly compact variations.

### Offline/local reply

If OpenAI is unavailable, slow, or the device has no internet, BOOP uses a deliberately plainer local response bank.

Examples:

- `Fan on.`
- `Done.`
- `Bedroom fan off.`

The reduced cheekiness is intentional. It acts as a subtle cloud-status signal without banners, status lights, or technical error messages.

The Home Assistant action must never wait for OpenAI. Cloud wording happens only after the local action result is known.

## Failure behavior

Failures prioritize clarity over puppetry.

### Home Assistant unreachable

BOOP says:

**“I can't reach the house right now.”**

No cloud request is required to formulate this.

### Room-local target unavailable/offline

If Living Room BOOP was asked for `the fan` and the Living Room fan is unavailable, BOOP reports that device as offline and stops.

Example:

**“The living room fan is offline.”**

BOOP must never silently try the Bedroom fan merely because it shares the name `Fan`.

### Ambiguous request

If Home Assistant cannot safely determine the intended target and BOOP's saved room does not resolve it, BOOP asks a short clarification such as:

**“Which room?”**

### Action rejected or failed

BOOP reports the failure plainly, for example:

**“That didn't work.”**

BOOP must never claim success unless Home Assistant reports that the command succeeded.

## Confirmation boundary

Normal device controls execute immediately without confirmation.

Examples:

- turn a fan on/off
- turn a light on/off
- run a scene
- media control

Future requests that create, modify, or delete Home Assistant automations/routines require a clear read-back and explicit confirmation before BOOP writes the change.

Example future flow:

User: `Every night at 11 turn everything off.`

BOOP: `I'll make that run every night at 11. Want me to save it?`

Only an affirmative answer authorizes the write.

This automation-writing capability is **not part of the first Alpha 2 implementation slice**.

## Audio and puppet polish intentionally deferred

The following behaviors remain locked for later but are outside the first Home Assistant control slice:

- music/audio ducks to roughly half volume while BOOP listens/speaks, then restores
- idle true-black state and quick wake animation
- capacitive finger-follow eyes using Henson-style eased puppetry motion
- one-time local speech-model prompt: **“BOOP needs her ears”**
- richer camera-based eye tracking
- reusable-device **“Where am I?”** area selection

These must not block the first real Home Assistant command.

## First physical acceptance test

Target device: spare Pixel 7 Pro, fixed to Home Assistant area `Living Room` for Alpha 2.

Target entities currently available:

- `Fan` in `Living Room` — Govee
- `Fan` in `Bedroom` — Govee

The first acceptance sequence is:

1. Install the Alpha 2 build on the Pixel 7 Pro.
2. BOOP discovers the local Home Assistant instance.
3. User taps **Connect** after **“I found your house.”**
4. Complete Home Assistant's normal authorization once.
5. BOOP retains its Alpha 2 home area as `Living Room` without asking another setup question.
6. Tap BOOP and say **“turn on the fan.”**
7. Only the Living Room Govee fan turns on.
8. BOOP gives a short cloud-enhanced puppet acknowledgement when OpenAI is reachable.
9. Say **“turn off the fan.”** and verify only the Living Room fan turns off.
10. Say **“turn on the bedroom fan.”** and verify the explicit Bedroom request overrides BOOP's Living Room default.
11. Disable internet/cloud access while leaving the LAN and Home Assistant available.
12. Repeat a Living Room fan command.
13. The fan still changes state successfully and BOOP gives a plain local acknowledgement.
14. Make Home Assistant unreachable while BOOP remains running.
15. Issue another command.
16. BOOP says **“I can't reach the house right now.”** and does not claim success.
17. Restore Home Assistant, make only the Living Room fan unavailable, and say **“turn on the fan.”**
18. BOOP reports the Living Room fan as offline and does not operate the Bedroom fan.

Passing this sequence proves the first Alpha 2 milestone:

- existing tap-to-speech interaction
- persistent Home Assistant authentication
- BOOP room identity
- area-aware duplicate-name handling
- real device control
- Home Assistant authority
- local-first operation
- cloud-only reply flavor
- explicit safe failure behavior

## Non-goals for this slice

Do not add these during the first implementation slice:

- automation creation/editing
- BOOP-maintained entity databases
- separate BOOP permissions
- sensor dashboards
- widget UI
- camera tracking
- wake-word listening
- music ducking
- setup/settings wizard
- reusable-device area picker
- multi-Home-Assistant support
- cloud-required device control

## Implementation boundary

The first implementation should be the smallest end-to-end path that passes the physical acceptance test. Existing Alpha 1 speech behavior should remain intact except where integration requires routing a recognized transcript into the new Home Assistant client and speaking the resulting acknowledgement.
