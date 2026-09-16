## Current Unified handoff - v205 installed; Home spacing visually aligned

Updated 2026-09-16. Owner remains `boop-hand-colour-v191`. Current receipt: `docs/handoffs/2026-09-16-v205-home-visual-spacing.md`.

Signed v205 (`205` / `1.2.205-home-visual-spacing`) is installed on the Shield. Source/build head `b529eceaa70b3d6140eeb41d5d35cd5e41ecd8ef`; focused visual-spacing gate was RED first at run `35095495224`, then GREEN at `35096388830`; full signed run `35096667354` succeeded with artifact `10446686887` / `BOOP-Unified-v205-Home-Visual-Spacing`.

Fresh ADB install/pull-back verification matched APK SHA-256 `633f0d70cff6db5823909f13f1231818fa1a7ae041088975b4cfe776bfe3b088` byte-for-byte. A fresh live Shield screenshot during playback shows the four top buttons unchanged, the visible Apps-row-to-Now-Playing gap matching the Now-Playing-to-Kodi artwork gap, and uniform gaps across Kodi, YouTube, Deezer, Casualty and EastEnders. The `Favourite apps` heading remains absent. v205 removes hidden Home-card top inset/centering and keeps banner focus size fixed so source-level 16dp spacing is also visually consistent.

Acceptance boundary: active-playback Home spacing is visually verified from the live screenshot. This v205 check did not stop the user's playback to re-exercise the no-media transition, so the parked favourites position and large bottom-right idle BOOP remain preserved by source/tests but await a fresh no-media physical screenshot if needed. No phones, permissions, app data, voice models or unrelated branches were touched.

---

## Current Voice handoff - v201 installed; entry screen clear

Updated 2026-09-16. Owner remains `boop-hand-colour-v191`. Current receipt: `docs/handoffs/2026-09-16-v201-voice-verified-build.md`. Preserve the collaborating install handoff and independent review alongside it.

**Fresh Shield verification: v201 is installed and the Voice entry screen is unobstructed.** Direct package/hash reads confirm `com.boop.alpha1`, 201 / `1.2.201-voice-surface-ownership`, installed APK SHA-256 `527907407d60787860cf9e1c1cd13b464db91f015f657eec92590f995d735c8e`. The latest targeted hierarchy shows both the canonical face wrapper and child GLSurfaceView GONE. A fresh screenshot visibly shows Voice, BOOP name/training controls, Pitch, Cadence and TEST VOICE with no giant eyes. The collaborating session performed the update; this reviewing recovery did not reinstall it or change settings.

Build source `eff4b004176747bfa489f566a34357b52e8c0557`; full signed run `35080910395` SUCCESS; artifact `10439409429`, `BOOP-Unified-v201-Voice-Surface-Ownership`. Independent APK inspection confirms the permanent signer, matching source/hash and unchanged permission declarations versus v200. The regression failed before the five-line visibility correction and passed afterward, including against materialized source. Artwork, voice backends/tuning, Home/media behavior and music assertions were not changed by that correction.

**Acceptance boundary:** this proves the visible obstruction is absent in the captured Voice entry state; it is not blanket physical acceptance. This recovery has not pressed TEST VOICE, adjusted Pitch/Cadence, tested exit/re-entry restoration, heard the natural voice output or tested cross-device sharing. Continue those joint checks with Ryan before marking the broader voice task finished. Screenshots/raw dumps stay private. Do not reinstall again merely because an older note says v200 or awaiting deployment.

The earlier v200 source-provenance gap is also closed: its installed and downloaded APK hashes matched `bd0b80ab96e858ed12bcaa1c43b132f4b55a43c078630a7cebb80a9db34d5bf5` from source `572e8d6cb4726951df0604e18a9a8e614d93a543`. Keep source/builds and durable handoffs on GitHub; use the already-connected bridge without Work-mode/reconnect prompts. This publication is documentation-only. Physical Pixel 10, permissions, app data, voice models, signing keys and unrelated branches remain outside this recovery's changes.

---

# BOOP Unified durable memory - current only

Updated 2026-09-16. Live GitHub branch `boop-hand-colour-v191` is authoritative for this branch's source/build receipts; dated receipts retain detailed history. Fresh device identity and source/build provenance are separate evidence.

## Current Voice continuation: human-testing phase

Ryan resumed BOOP Voice Ideas after the chat malfunctioned. The work had already reached human testing. His requested screenshot on the existing Shield ADB connection confirms large photographic felt eyes covering the Voice name controls and much of the Pitch/Cadence sliders; TEST VOICE remains visible below. Fresh read-only identity confirms `com.boop.alpha1/.MainActivity`, versionCode 200, `1.2.200-uniform-tv-chrome-voice-demo`.

Preserve the prior requirement to clear the full-screen eyes before continuing other voice work, use readable Home-style selectors across Shield/Android TV menus, and expose TEST VOICE with the tuning controls. None of those behavior checks is newly accepted here. This is a failing visual checkpoint, not proof of the renderer/service responsible or of input interception.

Continuation/context receipt: `docs/handoffs/2026-09-16-voice-human-test-overlay-blocker.md`. Locate the actual v200 source/build receipt before a correction; the historical v198 receipt below does not supply v200 provenance. Do not restart design, downgrade, ask for an already-connected bridge or push this task into Work mode. The continuation performed captures/identity reads and documentation updates only, with no app source, settings, permissions, installs, signing or phone operations. Screenshots and raw device output remain private.

## Latest Shield observation: storage cleanup

Ryan requested old Shield clutter be removed after a storage-blocked install. The cleanup removed 103 obsolete downloaded APK copies, three hash-identical duplicate Forki installers, 84 old BOOP/Johnny/Rally captures and 14 temporary diagnostic files. Approximately 1.92 decimal GB was recovered; the final measurement was 3,784,840 KiB free, approximately 3.88 decimal GB (3.61 GiB), with 69% used. All 21 third-party package names and the Home/screensaver defaults were unchanged. Five unclassified ZIP backups and one original Forki installer were retained on the Shield.

The live Shield already had **BOOP v200** (`1.2.200-uniform-tv-chrome-voice-demo`) and Johnny versionCode 15 (`1.14-reborn-ha-fast-poll`) when inspected; their version/update metadata remained unchanged through cleanup. This maintenance task did not build, sign, install or physically accept v200, and did not alter app data, permissions, phones or local source/worktrees. The blocked installer was not retried.

Receipt: `docs/handoffs/2026-09-16-shield-storage-cleanup.md`.

This direct device observation supersedes the earlier v197/v198 statements below for current installed identity only. Do not downgrade using an old receipt's next step. Read the newest relevant feature/source receipt before a later explicitly requested install; do not invent v200 source/hash provenance from its version string.

## Earlier voice/personality build receipt
v198 (`1.2.198-voice-profile-controls`) was signed and CI-green but not installed or physically accepted at the earlier build-session receipt. Built source `7a333039d127324d4de69d2a4b84e203fb61bbbc`; final run `35071278286`; artifact `BOOP-Unified-v198-Voice-Profile-Controls` id `10436755628`.

Build a Boop exposes Voice pitch and Voice speed using the existing `BoopVoiceTuning` ranges. Optional voice-profile sharing uses its own bounded `BOOP_VOICE_V1` Home Assistant helper and carries backend, Emma/Isabella/George/Fable natural voice key, pitch and speech rate. Voice model files stay local to each device and are never transferred through Home Assistant.

Natural Kokoro rate follows the shared speech-rate value. Natural Kokoro pitch is intentionally not applied to the PCM playback path until that Android audio path is physically proven. Preserve this boundary rather than pretending the natural pitch slider is audible today.

Receipt: `docs/handoffs/2026-09-16-v198-voice-profile-controls.md`.

## Preserved device and character checkpoints
- v197 is the earlier verified Shield behavior checkpoint for Home/Now Playing ownership. It hides/releases the large Home GL puppet while Now Playing owns the media puppet and restores it when media disappears. Receipt: `docs/handoffs/2026-09-16-v197-home-media-handoff.md`. This historical checkpoint is not a claim that v197 is still installed; see the v200 observation above.
- v191 hand appearance and cross-device hand sharing remain physically accepted.
- The photographic felt default remains locked at tag `boop-felt-default-v189`; unrelated work must not regenerate or replace it.
- Preserve current lyrics, playback controls, music/bass behavior, animation speed and shared eye/felt/hand choices unless Ryan explicitly changes them.

## Working evidence rule
Keep BOOP source edits, non-visual tests, builds, signing and durable handoffs on GitHub. Use the smallest relevant regression first, then the full GitHub Actions build. Installation identity is separate from runtime behavior, and both are separate from Ryan's physical acceptance. Do not install a candidate until Ryan explicitly requests it.
