# BOOP Routine Authoring v1 Design

Date: 2026-09-05

## Purpose

BOOP should let the user create one small class of Home Assistant automation by
voice without turning the Android app into an automation engine or giving
ordinary OpenCode conversation unrestricted tools.

The first version is intentionally narrow. The user begins with the explicit
spoken phrase `create a routine`, BOOP inspects the real Home Assistant instance,
asks for missing details, reads back one exact proposal, and writes only after
the user replies `yes please` in the same live conversation.

Home Assistant remains the sole authority for entities, automations, execution
and persistence. BOOP calls automations, scripts and scenes “Routines” in its
interface, but this authoring slice creates Home Assistant automations only.

## Protected baseline

This work starts after the physically verified BOOP Wall checkpoint:

    checkpoint-boop-wall-595e1da

The existing behaviour remains protected:

- natural wake and tap-to-talk;
- local-first Home Assistant control;
- direct media control;
- ordinary BOOP/OpenCode conversation;
- conversation continuation through Home Assistant `conversation_id`;
- Shield QR pairing and room selection;
- Shield Routines discovery and execution; and
- the absence of the discarded Android timed-routine flow.

The Android app already carries the Home Assistant conversation id across
OpenCode turns. It should not change unless a failing integration test proves a
specific Android gap. Routine parsing, proposal state and writing belong in the
server-side BOOP OpenCode bridge.

## User contract

### Entering authoring mode

Only a normalized transcript that begins with the words `create a routine`
enters authoring mode. Case and trailing punctuation do not matter. A normal
command, a hypothetical question, a plain `yes please`, or an implied schedule
must never create or prepare an automation.

This explicit opening is important because BOOP may eventually be used by the
user's father. Ambiguous speech must preserve the safer interpretation.

### Supported first slice

V1 may create a new, enabled Home Assistant automation with:

- one clock-time trigger or one sun trigger;
- an optional sun offset when the user states it;
- no conditions; and
- one or more ordinary actions limited to the `light`, `switch`, and
  `media_player` domains.

The exact V1 service allowlist is:

- `light.turn_on` and `light.turn_off`;
- `switch.turn_on` and `switch.turn_off`; and
- `media_player.media_play`, `media_player.media_pause`,
  `media_player.media_next_track`, and `media_player.media_previous_track`.

Brightness is allowed only with `light.turn_on` and is expressed as an integer
percentage from 1 through 100. Clock times use Home Assistant's configured local
timezone. Sun triggers are sunrise or sunset with an optional signed offset that
Home Assistant can validate. No delay action or additional service data is
accepted in V1.

The action must target real entities discovered from Home Assistant. V1 may use
ordinary domain services appropriate to the request, such as turning a light or
switch on/off or a basic media action. It must not invent an entity id or infer
a same-named target when more than one candidate remains.

V1 does not create scripts or scenes. It does not edit, delete, enable, disable
or trigger existing automations. It does not support templates, webhooks,
location triggers, device/state/event triggers, arbitrary conditions, security
devices, locks, doors, alarms, cameras, shell commands or file operations.

Future work may add richer triggers after separate design and physical proof.
The requested example “when Forki is running on the Shield, run a Govee
tap-to-run” is explicitly future work because it needs a trustworthy Shield
application-state entity and exact Govee action discovery.

### Clarification and proposal

BOOP inspects the relevant entities and existing automations before proposing a
change. When device, time, sun event, offset, action or intended behaviour is
unclear, BOOP asks one short question at a time.

Before any write, BOOP reads back:

- the proposed Routine name;
- whether the trigger is a time, sunrise or sunset and its exact value/offset;
- every target using both its friendly name and exact entity id;
- every action and important parameter, including brightness when present; and
- that the new Routine will be enabled but will not run immediately.

The read-back ends by asking whether BOOP should create that Routine.

### Confirmation and cancellation

The bridge stores a validated structured proposal against the current Home
Assistant conversation id. The proposal expires after two minutes.

Only the normalized reply `yes please` confirms the currently pending proposal.
The confirmation is valid only in the same conversation, before expiry, and
when the stored proposal has not changed. A plain `yes`, a delayed confirmation,
a confirmation in another conversation, or `yes please` without a proposal does
nothing.

`No thank you`, `cancel`, `stop`, expiry, a new `create a routine` request, or an
unrelated turn clears the pending proposal. BOOP reports cancellation briefly.

### Success response

After creation, BOOP verifies that Home Assistant exposes the new automation,
that its trigger/actions match the proposal, and that it is enabled. BOOP then
reports success in one short spoken response. Creation must never trigger the
automation.

## Architecture

### Existing paths remain isolated

The Android command router remains local-first:

1. direct media commands remain local;
2. ordinary Home Assistant commands remain local;
3. only an exact local `NO_MATCH` reaches the BOOP OpenCode conversation agent.

Ordinary OpenCode conversation keeps every tool disabled exactly as it does at
the protected checkpoint.

### Bridge state machine

The OpenCode Wyoming bridge adds per-Home-Assistant-conversation state:

    CHAT -> AUTHORING -> PENDING_CONFIRMATION -> CREATING -> CHAT

- `CHAT` has no enabled tools.
- `AUTHORING` permits only the proven read capabilities needed to inspect Home
  Assistant entities and existing automations.
- `PENDING_CONFIRMATION` has no write capability. It retains one validated
  proposal, its digest and its expiry.
- `CREATING` is entered only by the exact context-bound `yes please`. It exposes
  exactly one proven automation-creation capability for one turn.
- Every success, refusal, cancellation, timeout or error returns to `CHAT` and
  revokes authoring capabilities.

Conversation state is memory-only for V1. Restarting BOOP, Home Assistant or the
OpenCode bridge cancels an unfinished proposal. No pending confirmation survives
a process restart.

### Structured response boundary

OpenCode must return a machine-readable envelope for authoring turns. The bridge
parses and validates the envelope before speaking its `speech` field or storing
a proposal. The proposal contains a version, name, trigger, targets, actions and
a digest of the canonical content.

Free-form model text is never treated as authorization and cannot directly
change state. An invalid, incomplete, unsupported or unparseable envelope is
rejected without writing.

### Capability preflight and allowlist

The implementation begins with a live inventory of the OpenCode tools available
in the installed Home Assistant add-on. Tool identifiers are matched against an
explicit allowlist; prefix or substring guesses are forbidden.

The design is viable only if the live environment provides separable:

1. read-only Home Assistant entity/automation inspection; and
2. a bounded native Home Assistant automation-creation operation.

The bridge must fail closed when those capabilities are absent, renamed or
ambiguous. It must never enable general shell execution, arbitrary file editing,
or every Home Assistant tool as a workaround. The
`home-assistant-configuration` skill supplies planning guidance, but a skill does
not override the bridge's capability allowlist or state machine.

If the installed tool surface cannot enforce this separation, implementation
stops after the capability report and returns to design. It does not weaken the
boundary.

### Native Home Assistant ownership

Automation creation uses Home Assistant's native configuration interface rather
than editing `automations.yaml` directly. Home Assistant assigns/persists the
automation, validates its configuration and remains responsible for execution.
The bridge performs a read-after-write verification and does not reload or
restart Home Assistant unless the native interface explicitly requires it.

Routine names are generated from the requested action and trigger. Existing
automations are inspected first; an exact equivalent or conflicting duplicate
causes BOOP to refuse creation and explain briefly.

## Failure behaviour

Every failure is bounded and spoken plainly:

- unavailable or unsafe tool separation: Routine creation is unavailable;
- missing or ambiguous entity: ask one clarification;
- unsupported trigger/domain: explain the V1 limit;
- duplicate: report that the Routine already exists;
- expired or mismatched confirmation: say the proposal expired and require a
  fresh `create a routine` request;
- OpenCode, Home Assistant or validation error: report failure without claiming
  creation; and
- uncertain write result: inspect Home Assistant before retrying so one spoken
  confirmation cannot create duplicates.

Existing automations and all local control paths remain untouched on failure.

## Security and privacy

- OpenCode credentials stay inside OpenCode.
- Home Assistant credentials stay inside the existing BOOP/Home Assistant
  integration boundary.
- OpenCode port 4096 remains internal to the add-on container.
- Ordinary chat cannot use tools.
- The bridge, not model prose, controls state transitions and capability maps.
- Entity ids come from live Home Assistant inspection.
- No secrets, tokens, full configuration dumps or unrelated entity inventory
  are included in spoken responses or logs.
- The V1 writer cannot edit or delete existing configuration.

## Testing

Pure tests cover transcript normalization, entry/cancel/expiry rules, state
transitions, proposal canonicalization/digests, schema validation, allowed
triggers/domains/services, duplicate handling and exact confirmation semantics.

Bridge tests use fake OpenCode and Home Assistant clients to prove:

- ordinary chat disables all discovered tools;
- authoring read turns receive only the exact read allowlist;
- write capability is absent before confirmation;
- `yes please` without the matching pending proposal cannot write;
- confirmation exposes one exact creation capability for one turn;
- creation is idempotent across an uncertain response;
- success requires read-after-write verification;
- no creation call executes the new automation; and
- all exit/error paths revoke capabilities and clear pending state.

Repository regression tests preserve BOOP Wall wake, Android conversation
continuation, local-first Home Assistant/media routing, Shield pairing, Shield
Routines discovery/execution, stable signing and the absence of Android timed
routines.

The live capability preflight records exact tool ids without secrets. CI tests
the bridge against fixtures; no CI job writes to the user's Home Assistant.

## Deployment and physical acceptance

The expected deployment replaces the single OpenCode startup hook and restarts
only the OpenCode add-on. No APK is built or installed unless testing discovers
an Android defect.

Physical acceptance uses one harmless scheduled automation:

1. Verify ordinary questions still work and ordinary chat has no tool access.
2. Say `create a routine` and request an allowed future clock/sun action.
3. Verify BOOP resolves the intended entity and asks any needed clarification.
4. Verify BOOP reads the exact enabled-but-not-run proposal.
5. Confirm with `yes please`.
6. Verify one enabled automation appears in Home Assistant and BOOP's Routines
   list, with the exact trigger and action, without executing during creation.
7. Verify a repeated request detects the duplicate.
8. Verify local lights/media and ordinary conversation still work.
9. Remove the harmless test automation manually in Home Assistant after the
   acceptance record is complete.

No new physical checkpoint is created until the complete acceptance sequence
passes on the real setup.
