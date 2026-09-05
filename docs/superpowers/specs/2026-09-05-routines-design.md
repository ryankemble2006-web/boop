# BOOP Shield Routines v1 Design

Date: 2026-09-05
Branch: `boop-shield-home-implementation`
Base checkpoint: `f8e81359f27b62877849596e8da99d4ecaa1bb63`

## Goal

Add a first useful Routines page to the BOOP Shield Home app while preserving the working Home dashboard and its checkpointed behaviour.

Routines v1 is deliberately narrow: discover whole-house Home Assistant scripts and scenes, show them in one sofa-friendly list, run them from the Shield remote, and provide truthful, plain-English execution feedback.

## Scope

Included:

- Home Assistant `script.*` entities.
- Home Assistant `scene.*` entities.
- Whole-house discovery; no room filter.
- One case-insensitive alphabetical list.
- A small visible type label: `Script` or `Scene`.
- Big, chunky, remote-first cards.
- D-pad Up/Down navigation with focus-following vertical scrolling.
- D-pad Left returns focus to the Routines rail item.
- Select runs the highlighted routine.
- Per-row execution feedback.
- Plain-English failure handling.
- Local Home Assistant control only; no cloud dependency for routine execution.

Explicitly excluded from v1:

- Home Assistant automations.
- Editing or creating routines.
- Voice control or spoken routine creation.
- Room filtering.
- Execution history or “last run” timestamps.
- Routine favourites or paging layers.
- Sensor data.

## Architecture

Routines is a separate vertical slice beside the existing Home dashboard rather than another responsibility inside `HomeAssistantRepository` or `BoopHomeActivity`.

### `RoutineItem`

Small immutable model containing:

- entity ID
- display name
- type: `SCRIPT` or `SCENE`

It represents only routines BOOP is prepared to show and run.

### `RoutinesRepository`

Owns Home Assistant protocol details for routine discovery and execution.

Responsibilities:

- discover available whole-house scripts and scenes
- filter unusable registry entries
- map them to `RoutineItem`
- sort them case-insensitively by display name
- call `scene.turn_on` for scenes
- call `script.turn_on` for scripts
- subscribe to state changes before starting a script so completion can be observed truthfully
- expose callbacks/results to the controller without leaking Home Assistant jargon into the UI layer

It reuses the already-authenticated Home Assistant WebSocket and the existing state-change subscription mechanism.

### `RoutinesController`

Owns Routines page state and execution state.

Responsibilities:

- load the routine list
- expose an unavailable state when Home Assistant cannot be reached
- track execution state independently per routine row
- ignore repeated Select on the same routine while it is already running
- leave other rows usable while one routine is running
- convert repository outcomes into BOOP states: normal, `Running…`, `Done`, `Didn’t run`
- reset `Done` or `Didn’t run` back to the normal row after about two seconds
- apply a bounded script-completion timeout so a missed finish event never leaves a row stuck forever

### `TvRoutinesView`

Owns TV presentation and remote interaction only.

Responsibilities:

- render the page heading and flat routine list
- render each routine as a chunky `FocusCardView`
- show the routine name and small type label
- render temporary execution text for only the affected row
- keep focused rows visible while scrolling
- handle D-pad Up/Down naturally through the list
- route D-pad Left back to the rail
- route Select to the controller callback

The view does not know Home Assistant protocol details.

### `BoopHomeActivity`

Only wires the page together using the existing authenticated WebSocket, consistent with the current Home dashboard composition pattern.

It must not absorb routine discovery, filtering, execution state, or protocol logic.

## Discovery and filtering

Routines v1 is whole-house.

The repository reads Home Assistant entity metadata and current states, then keeps only entities whose domain is `script` or `scene` and which are intended for ordinary user interaction.

Exclude:

- hidden entities
- disabled entities
- config-category entities
- diagnostic-category entities

Entries without a usable display name are excluded.

Display name preference follows the existing Home approach: registry/display name first, then state `friendly_name` fallback where available.

The final list combines scripts and scenes and sorts by display name using case-insensitive alphabetical order.

No room membership test is applied.

## Execution behaviour

### Scene

When Select is pressed on a scene:

1. That row changes to `Running…`.
2. BOOP calls `scene.turn_on` targeted at that exact scene entity.
3. If Home Assistant accepts the service call, the row becomes `Done`.
4. `Done` remains visible for about two seconds.
5. The normal routine name/type returns.

A scene is considered done when Home Assistant accepts the service call. BOOP does not pretend to infer downstream physical completion of everything contained in a scene.

### Script

When Select is pressed on a script:

1. That row changes to `Running…`.
2. BOOP subscribes to Home Assistant state changes before sending the start command.
3. Only after the subscription is active does BOOP call `script.turn_on` for that exact script entity.
4. The row stays `Running…` while the script is active.
5. BOOP listens only for state changes belonging to that exact script entity.
6. When Home Assistant reports that the script has finished, the row becomes `Done`.
7. `Done` remains visible for about two seconds.
8. The normal routine name/type returns.

The repository/controller implementation must account for the service-result and state-event callbacks arriving in either order.

The script completion wait is bounded. Initial v1 timeout target: 120 seconds. If completion is not observed inside that window, BOOP reports failure rather than leaving the card permanently in `Running…`.

## Failure behaviour

Failures are intentionally plain-English.

If the service call is rejected, the socket goes away during the operation, subscription setup fails, or a script completion times out:

- the affected row shows `Didn’t run`
- that result remains for about two seconds
- the normal routine name/type returns

No raw Home Assistant error string is shown on the routine card.

If Home Assistant is unavailable when the Routines page loads, the page shows one plain-English unavailable card:

`Routines unavailable right now`

Routines v1 does not use a cached stale routine list. A stale executable list would not provide enough value to justify implying those actions are currently available.

## Concurrency and input rules

- A running row ignores repeated Select presses on itself.
- Other routine rows remain selectable while one routine is running.
- Execution state is tracked per row/entity rather than freezing the entire page.
- The design does not require queueing repeated presses.
- BOOP must clean up subscriptions and timeout tasks after success, failure, cancellation, or socket loss.

## TV navigation and layout

The page follows the existing BOOP TV visual language:

- black background
- large white heading
- chunky focusable cards
- high-contrast focused state
- remote-first spacing
- no touch-first interaction assumptions

Navigation:

- Up/Down moves through the single routine list.
- The containing view scrolls as focus moves so the selected card remains visible.
- Left from a routine returns focus to the Routines item in the rail.
- Select runs the focused routine.

Focus visuals must not alter child ordering. In particular, the Routines implementation must preserve the Home regression rule introduced at checkpoint `f8e8135`: focused cards may use scale/Z/elevation effects, but must never call `bringToFront()` or otherwise reorder children in a vertical layout.

## Error-language policy

User-visible strings stay BOOP-style and plain English.

Examples:

- `Running…`
- `Done`
- `Didn’t run`
- `Routines unavailable right now`

Protocol details, entity-registry jargon, subscription wording, JSON errors, and raw Home Assistant failures stay below the UI boundary.

## Testing strategy

Development follows the existing BOOP rule: evidence first, red test first, minimal green fix, fresh Shield CI, then physical sofa verification.

Automated tests must cover at least:

1. Discovery keeps only `script.*` and `scene.*` entities.
2. Hidden entities are excluded.
3. Disabled entities are excluded.
4. Config/diagnostic entities are excluded.
5. Results are one case-insensitive alphabetical list.
6. Scene execution calls `scene.turn_on` for the exact entity.
7. A successful scene service result produces `Running…` then `Done`.
8. Script execution establishes the state subscription before `script.turn_on`.
9. Script completion ignores state events for every other entity.
10. Script completion succeeds only for the targeted script’s completion state.
11. Service-result and state-event ordering cannot race into a false result.
12. Duplicate Select on an already-running row is ignored.
13. Other rows remain runnable while one row is running.
14. Rejected service calls become `Didn’t run`.
15. Subscription setup failure becomes `Didn’t run` without firing the script.
16. Script completion timeout becomes `Didn’t run` and cleans up the subscription.
17. `Done` resets after about two seconds.
18. `Didn’t run` resets after about two seconds.
19. D-pad Left still returns from content to the Routines rail item.
20. Focus visuals never reorder routine cards.

## Physical verification

After fresh Shield CI is fully green, the first physical test should use at least one known scene and one known script.

Minimum sofa test:

1. Open Routines from the rail.
2. Confirm scripts and scenes appear together alphabetically.
3. Navigate enough rows to prove vertical scrolling follows focus.
4. Run a scene and observe `Running…` → `Done` → normal card.
5. Run a script that lasts long enough to observe `Running…` while active.
6. Confirm `Done` appears only when that script actually finishes.
7. Press Select twice rapidly on the same running script and confirm it is not started twice.
8. Confirm another routine can still be selected while the first is running.
9. Confirm Left returns to the Routines rail item.
10. Confirm the working Home page and favourite toggle path are unchanged.

Routines v1 is not called physically complete until these checks pass on the Shield.

## Preservation constraints

- Do not modify the checkpoint branch `checkpoint-shield-home-f8e8135`.
- Do not regress the existing Home dashboard, room selection, favourite discovery, physical binary control, real state confirmation, or focus ordering.
- Keep the existing `shieldoverlay` app/package naming.
- Keep core routine execution local to Home Assistant and independent of cloud/OpenAI availability.
- Do not add sensors, routine editing, automations, voice, history, or room filtering as incidental extras.

## Success criterion

From the Shield remote, the user can open Routines, browse a whole-house alphabetical list of usable Home Assistant scenes and scripts, press Select to run one, receive truthful `Running…` / `Done` / `Didn’t run` feedback, and return to the rail without disturbing the already-working Home dashboard.
