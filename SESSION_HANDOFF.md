# Current task handoff: music audio permission prompt

Updated 2026-09-13. This is the isolated `boop-unified-music-audio-prompt` task branch, based on LIVE Unified `593ad609ff87f651d5273bd17f5a2c0ca3ef5198`. The accepted combined app remains owned by `boop-unified-eye-sync-safe-v159`; this task branch has NOT replaced or merged into it.

## Requested scope and implementation

Ryan first requested read-only feasibility for a simple VU-style music bounce, then explicitly asked: "if he dont ask permission, code a prompt to ask like before,". This task implements that conditional permission prompt only. It does not implement a Visualizer sampler, change bouncing, or alter blink timing.

Production changes are confined to the current `unified/shield-home` module:
- Added `MusicAudioPermissionActivity.java` and pure `MusicAudioPermissionFlow.java`.
- Added a private activity registration in `unified/shield-home-manifest.xml`.
- Added **Launcher Settings > Now Playing > Music audio access** in `ShieldHomeSettingsView.java`.

The entry checks the actual RECORD_AUDIO grant. Already allowed returns without an Android permission request. Missing access gets a cancellable explanation, then Continue opens Android's own request. Not now/Back cancels. Denial does not retry automatically; Open settings is a separate explicit choice. Returning from settings rechecks the grant and closes rather than reopening a request. Pending request and dialog phase are saved across activity recreation.

This is intentionally not routed through the existing voice activity or voice permission callback. It creates no audio recorder/visualizer, starts no voice/service and makes no network call. RECORD_AUDIO and MODIFY_AUDIO_SETTINGS were already declared; no additional uses-permission was added. Android permission labels are not proof that the physical microphone is being used by this entry.

Implementation commit: `c1c53e84f20e2211b729dc2f5a541018fc2908b8`. Non-visual compile workflow at `746a7d3ab0c8601c174ca690c2b27397266e673e`. Read `docs/handoffs/2026-09-13-music-audio-permission.md` for exact red/green and compilation evidence. Do not substitute source inspection or a CI pass for real Android prompt acceptance.

## Music behaviour retained as the next design, not implemented

Animation speed continues to control blinks. Actual music loudness will separately control bounce height, with a quick rise and smoother fall. No BPM lookup, beat detection or full-animation restarts are wanted. Use Android Visualizer, not a physical-microphone fallback. Usable readings from Deezer on the Shield remain unproven. Preserve all approved artwork, coded animations, single-face ownership and exact original 1x timing.

## Accepted installed baseline remains unchanged

The base handoff recorded Ryan's physical acceptance of v161 speed on BOTH Shield and Pixel 7, and automatic eye-colour delivery in BOTH directions. Speed remains device-local; colour is shared. Do not reopen these accepted repairs.

Accepted app/build commit `0b6ee6f91e05f00138a94ec2c9fd846117020754`; package `com.boop.alpha1`; version `161 / 1.2.161-lab-scale-independent`; prior signed build `34770388933`, artifact `10321956422`. Accepted APK SHA256 `c68b81be9b7d3e10883d2aea52c05dc7b8c187fcd835eaddf73e25e91cb4acd6`. Permanent signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Those are inherited installation/acceptance facts, not new device checks in this task. Original acceptance and installation receipts remain at `docs/handoffs/2026-09-13-v161-speed-colour-accepted.md` and `docs/handoffs/2026-09-13-v161-installed-joint-testing.md`. The complete pre-task root handoff/status/memory remain available at base commit `593ad609ff87f651d5273bd17f5a2c0ca3ef5198`.

## Workflow and next safe step

GitHub owns source edits, non-visual checks and durable handoffs. This task did not edit/build laptop sources, operate emulators or devices, install an APK, grant permission, reset settings, access physical Pixel 10 or touch signing keys. Local worktrees are not claimed synchronized. No release/version bump was made, and no new signed APK is presented as accepted v161.

For integration, fetch the LIVE Unified owner and reconcile this small feature against any concurrent changes; do not copy this branch over a newer Unified checkout. Preserve the current owner's handoff/acceptance record when merging. Use the existing permanent signed-build workflow before any requested delivery. Device permission/remote-navigation acceptance must be tested together with Ryan, not automatically granted. Actual music-driven bouncing needs its own implementation request. Main's workflow/ownership did not change.
