# BOOP // BLOCK PARTY

Standalone Kodi Python 3 program add-on, not a BOOP Android lineage. Ryan requested a Tetris-style game with 3D presentation, Enter rotation, BOOP theme and full artistic licence.

Use classic 10 x 20 falling-block play, seven tetrominoes, a shuffled seven-bag, clockwise rotation with simple kicks, ghost landing, hard/soft drop, delayed lock, progressive speed, line-clear scoring, local best score, pause and replay. Present extruded 3D cube artwork on a straight-on board (2.5D rendering, not a volumetric 3D well). All resources ship locally. No network, media transport, device permissions or third-party Python modules at runtime.

Native Kodi WindowXML and image controls use a fixed 1280 x 720 coordinate layout. Background and cube assets are generated ahead of time using Pillow, so Kodi needs only its built-in Python APIs. Approved eye master is copied byte-for-byte and scaled only at display time. Keyboard/remote: Left/Right move; Down soft drop; Enter/OK rotate/start/resume; Up hard drop; Back/Escape pause, then Back exits; Play/P pauses. No global keymap changes.

Implement locally in this task; deliver a self-contained add-on ZIP, source archive, install guide and preview. Test core game behaviour with deterministic unit tests, then smoke-test using an isolated Kodi runtime if available. Ryan owns appearance and target-device acceptance.

## Execution plan

1. Write deterministic engine tests; see them fail, then implement and pass them.
2. Generate local arcade assets and copy verified approved eyes without edits.
3. Implement Kodi UI, action queue, lifecycle handling and local best-score storage.
4. Test controller behaviour and package integrity; run isolated Kodi launch/action smoke.
5. Package source and installable ZIP; record exact test evidence and remaining device acceptance.
