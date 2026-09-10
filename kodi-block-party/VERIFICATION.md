# BOOP Block Party 1.0.0 — verification

2026-09-10. Standalone Kodi add-on; no Android app changes.

## Completed checks

- 21 deterministic Python engine/controller tests passed, including walls, four rotations, wall kick, collision rejection, ghost/hard drop alignment, one/four-line clears, top-out, lock delay, gravity, seven-bag, progression, random-play invariants, pause/resume, replay and exit.
- Independent code review found no actionable correctness/lifecycle/input/package issues in the reviewed implementation; the reviewer independently reran the same 21 tests.
- Tested in an isolated portable **Kodi 21.2 x64** on Windows, with its own disposable profile. The user's normal Kodi profile was not used or modified.
- Kodi accepted/enabled the add-on. Native Kodi actions successfully started, rotated, moved and dropped pieces, cleared four rows, paused, resumed and exited.
- The actual shipped `RunScript(script.boop.blockparty)` entry point launched, survived input/play, paused/resumed and returned to Kodi Home. No add-on traceback in the test log.
- Local best score survived closing and restarting Kodi.
- Local presentation inspected in Kodi. The gameplay preview uses a deterministic board fixture to show the cube geometry and landing guide; it is not a claimed human high score.
- Original approved BOOP eye master copied without byte changes; SHA-256 verified.
- Packaging checks validate XML, Python 3.8-compatible syntax, ZIP integrity, safe member paths and byte equality of every install ZIP member against its source.

## Limits

- No Nvidia Shield/Forki hardware installation or physical remote acceptance yet. Kodi 19+ is the API target; only Kodi 21.2 Windows was exercised here.
- 3D cube artwork is pre-rendered on a classic 2D board (2.5D); no depth-axis movement or rotating 3D well.
- Sound files are bundled; acoustic acceptance on the target audio setup is unverified.
- Runtime/smoke checks do not claim Ryan has physically or visually accepted this game.
- No GitHub CI workflow was added or triggered for appearance testing. Checks were local.

The first hidden-window screenshot was black; visible-window inspection was used instead. Initial drawing was moved out of Kodi's window-initialisation callback. Title, pause and game-over cards use pre-rendered text and native state-driven visibility after dynamic labels failed to display reliably. The revised pause card was checked in a fresh Kodi run. Runtime smoke screenshots are not screenshot-comparison or automated visual acceptance tests.
