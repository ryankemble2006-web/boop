# BOOP Rally for Shield

Independent offline Android TV launcher for the user's existing Network Q RAC Rally and Network Q RAC Rally Championship DOS files. Package `com.boop.rally` is separate from BOOP Unified and Android HOME. No Internet, storage, microphone or accessibility permission is requested.

## Build

GitHub workflow `.github/workflows/build-rally-shield.yml` builds the pinned ARM64 DOSBox Pure core, compiles the Java/JNI host, checks behavior and package identity, and signs with the existing permanent BOOP development key in Actions. No new key is generated. API 26 minimum; compile/target API 36; JDK 17; SDK build-tools 36.0.0; NDK 26.3.11579264.

Run `python -m unittest discover -s rally-shield/tests -v`. To build on Linux with the specified SDK/NDK installed, set `ANDROID_HOME` and run `python rally-shield/tools/build_apk.py`. The unsigned aligned output is `rally-shield/build/BOOP-Rally-unsigned.apk`; signing is performed separately by the workflow. The `corresponding-source.tar.gz` CI artifact contains launcher/host/build sources and the exact emulator source.

## Private game preparation and installation

The APK contains no game files. Preserve original PC folders, and run:

```text
python rally-shield/tools/prepare_games.py --rac93 "path/to/original/RALLY" --rac96 "path/to/accepted/Browser-Test" --output "path/outside/originals/games"
```

This produces deterministic `rac93.zip` and `rac96.zip` packs plus a private integrity receipt. It verifies the original files remain unchanged. Championship's accepted `Browser-Test` copy is intentional: do not substitute its older unverified RALLY folder.

Install the signed APK on the positively identified Shield, open BOOP Rally once, then copy the packs to `/sdcard/Android/data/com.boop.rally/files/games/` using ADB. The in-app Import option also accepts prepared packs through Android's file picker. Do not commit or publicly upload these packs, raw device logs or screenshots.

## Controls and saves

D-pad or left stick sends the game's arrow keys. A/remote OK sends Enter, B sends Escape, X sends Space, Y sends F1, R2 sends Up, L2 sends Down, and L1/R1 send Z/A. USB/Bluetooth keyboard keys also work. Back/Start opens the host pause menu with Resume, Enter, Escape, keyboard keys and Return to collection. The original game's control options remain authoritative; controller mapping still requires physical racing checks.

Each title has an isolated save directory below app-owned external storage `saves/<game-id>`. Return to collection cooperatively unloads the core, writing changed game files, before closing the separate game process. This is ordinary DOS game-file persistence, not a suspended RAM snapshot. Home pauses input and emulation; Android may terminate background apps, so use Return to collection after saving in the game.

Both titles initially use 30 FPS emulator output. This is presentation pacing, not a promise that the original simulation is exactly 30 Hz. Classic uses its recovered 12000-cycle setting; Championship uses max/dynamic, 16 MB and SB16 220/7/1/5. Save, sound and race performance must be recorded from the exact physical APK separately from CI success.

See SESSION_HANDOFF.md and BOOP_STATUS.md on this branch for actual verification, build and installation receipts. Licensed under GPL-2.0-or-later; see LICENSE and NOTICE.txt.
