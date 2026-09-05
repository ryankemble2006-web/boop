# BOOP

BOOP is a local-first smart-home puppet interface.

The project is deliberately split between a visible puppet surface and the systems that let it control the house. BOOP should feel simple, physical and friendly without pretending to be a person. The core rule is that basic Home Assistant control must keep working locally even when cloud or AI services are unavailable.

For the current working rules and project state, read:

- [`BOOP_RULES.md`](BOOP_RULES.md)
- [`BOOP_STATUS.md`](BOOP_STATUS.md)

## Current prototypes

### BOOP Shield Home

The Nvidia Shield prototype is the current smart-home control surface.

Working features include:

- local Home Assistant connection and remembered house pairing
- room discovery and room selection
- real Home Assistant favourite discovery
- remote-first navigation with large, chunky TV controls
- physical on/off control for supported favourites
- truthful state confirmation from real Home Assistant `state_changed` events
- a 10-second maximum confirmation window without optimistic state guessing
- preserved Home state during socket loss with plain-English stale/offline behaviour
- protected, non-interactive BOOP eye overlay kept separate from Home Assistant control code

The physically verified Home checkpoint is:

```text
checkpoint-shield-home-f8e8135
```

That checkpoint is not to be moved or rewritten.

### Shield Routines v1

Routines is being added as a separate vertical slice rather than being bolted into the working Home favourite path.

Implemented so far:

- whole-house discovery of usable `automation.*`, `script.*` and `scene.*` entities
- hidden/config/diagnostic filtering
- one case-insensitive alphabetical list
- one BOOP-facing type label: `Routine`
- automation execution through `automation.trigger`
- scene execution through `scene.turn_on`
- script execution through `script.turn_on`
- script completion only after the exact target is observed going `on -> off`
- isolated `off` events do not falsely count as completion
- `Running...`, `Done` and `Didn't run` row states
- 120-second script timeout
- roughly two-second result hold before returning the row to normal
- duplicate Select suppression on the same running routine while other routines remain usable
- scrollable remote-first TV list without focus reordering

Routines is composed in the Activity on the existing authenticated Home Assistant WebSocket. The current correction adds Home Assistant automations to the same BOOP-facing Routines list; fresh CI and Shield sofa verification remain required before the Routines checkpoint is physically green.

The approved design and implementation plan live under:

```text
docs/superpowers/specs/2026-09-05-routines-design.md
docs/superpowers/plans/2026-09-05-routines-v1-implementation.md
```

## Pixel puppet-body Alpha

The spare Pixel 7 Pro is the physical BOOP puppet-body prototype.

The latest Alpha behaviour is substantially further along than the original Alpha 1 described by the old README. The puppet interaction work is largely there; the next Alpha job is not to start over, but to rebuild the current Alpha around the newer QR-based local pairing/authentication flow that was developed for the Shield path.

The intended direction is:

1. keep the current puppet interaction behaviour
2. replace the older connection/setup path with the newer QR pairing flow
3. keep credentials out of QR payloads
4. reuse the local-first Home Assistant authentication model
5. preserve plain-English, minimal setup rather than exposing Home Assistant jargon

That QR rebuild should be treated as its own reviewed change rather than mixed into the Shield Routines work.

## Project principles

BOOP is a puppet interface, not a chatbot personality.

Core rules:

- local Home Assistant first
- no cloud dependency for basic smart-home control
- remote-friendly TV navigation
- large, chunky controls
- plain-English errors and status messages
- no touch requirement for Shield/TV control
- no microphone or Home Assistant logic in the protected overlay runtime
- preserve working behaviour unless a tested change explicitly requires otherwise
- test before claiming something works

See [`BOOP_RULES.md`](BOOP_RULES.md) for the short operational version.

## Repository map

```text
.github/workflows/
    GitHub Actions builds and CI checks

docs/superpowers/specs/
    Approved feature designs

docs/superpowers/plans/
    Task-by-task implementation plans

scripts/
    Build/signing/helper scripts

shield-overlay/
    Shield Android application
    Home dashboard, pairing, overlay, routines and Java unit tests

tests/
    Python source-regression tests

BOOP_RULES.md
    Project guardrails for humans and coding agents

BOOP_STATUS.md
    Current whiteboard: green state, active task, next work and protected checkpoints
```

## Protected areas

For current Shield Routines work, treat these as protected unless a failing regression proves a change is necessary:

```text
shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantRepository.java
shield-overlay/app/src/main/java/com/boop/shieldoverlay/FocusCardView.java
shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopOverlayService.java
shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopOverlayView.java
shield-overlay/app/src/main/java/com/boop/shieldoverlay/OverlayGeometry.java
shield-overlay/app/src/main/java/com/boop/shieldoverlay/OverlayWindowSpec.java
```

Do not move or overwrite:

```text
checkpoint-shield-home-f8e8135
```

The active implementation branch is:

```text
boop-shield-home-implementation
```

## Development workflow

BOOP development uses small evidence-backed steps:

1. identify the root cause or exact behaviour being changed
2. add a failing regression test first
3. make the smallest production change
4. get the test green
5. run the full Shield CI pipeline for the exact commit
6. physically verify on the Shield before declaring real-world behaviour fixed
7. create a new checkpoint only after physical green

For architectural changes, design and approval come before implementation.

## Shield build

The Shield project is Java-based Android code and currently uses the Gradle/Android toolchain defined in `shield-overlay` and CI.

Useful local gates:

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
gradle -p shield-overlay :app:assembleDebug --stacktrace
```

GitHub Actions builds the Shield APK through:

```text
.github/workflows/build-shield-overlay-poc.yml
```

The workflow also checks source regressions, package/permission boundaries, stable BOOP development signing and artifact upload.

## Alpha build

The repository also retains the Pixel Alpha build workflow:

```text
.github/workflows/build-apk.yml
```

The original Alpha 1 contract was the first physical BOOP face: true-black eyes, tap-to-speak, local speech recognition where available and no Home Assistant control. That description is now historical context, not the current project roadmap.

## What is next

Current order of work:

1. run fresh Shield CI for the automation-compatible Routines build and produce the installable APK
2. physically verify Routines and re-check the existing Home favourite control
3. create the Routines functional checkpoint only after physical green
4. then return to the Pixel Alpha and rebuild its setup around the newer QR pairing/authentication flow

When in doubt, `BOOP_STATUS.md` is the current whiteboard and `BOOP_RULES.md` is the guardrail file.
