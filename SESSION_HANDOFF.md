# BOOP Block Party — standalone Kodi game

Updated 2026-09-10. Owning branch: `boop-kodi-block-party`.

This branch owns the standalone Kodi game Ryan explicitly requested. It is not a parallel Android BOOP app and does not replace `boop-unified`, `boop-canonical-rebuild`, or the animation tasks. Inherited shared documents concern those projects; this handoff identifies this branch's scope.

Source: `kodi-block-party/script.boop.blockparty/`.
Installable ZIP: `kodi-block-party/releases/script.boop.blockparty-1.0.1.zip`.
Read `kodi-block-party/README.md`, `DESIGN.md` and `VERIFICATION.md`.

Implemented: classic falling-block gameplay with pre-rendered 3D cube artwork, original approved BOOP eye spectator, native Kodi controls, Enter rotation, Down tap/hold instant drop, Up hard drop, pause/replay, score/level/combo rules, ghost landing, seven-bag and local best score. Offline, native Python3 Kodi add-on; no browser, global keymaps or device permissions.

Verification: 1.0.1 hold timing is unit-tested; target remote acceptance remains pending. 27 unit tests and current package checks passed. Previous 1.0.0 evidence: independent code review; actual Kodi21.2 Windows launch/action/clear/pause/resume/exit tests passed in an isolated portable profile. Packaging/XML/Python3.8 syntax and approved eye hash checked. Screenshots are local manual inspection, not visual CI.

No physical Shield/Forki remote or acoustic acceptance yet. No game physical checkpoint exists. Android app/checkpoints are unchanged. Next safe step: Ryan installs the ZIP in Kodi and tries his remote/skin. Do not imply install permission from this handoff.

Exact release SHA is the commit containing this file; consult live GitHub HEAD and release SHA256SUMS before further work. Local task source is retained in the originating task workspace.
