# Scoped canonical rebuild

User-approved scope: items 1, 3, 6, 7, 8 and 10 of the recovered overhaul. Eyes/blink/animation art are owned by another task and will be transplanted later. Turbo is excluded.

Base: boop-unified 99474d141e7affad17cdbe854e94dd3986076980; v91 APK source 11650313221ae5bf997dbb93b6a905bfdc7da1ed. Ryan confirmed natural selection, demonstrations and normal routed speech on 2026-09-10. Preserve the exact natural subsystem.

## Design and inventory

- The app assembles Wall, launcher and shield libraries using scripts/materialize-unified.sh. Keep adapters at the end of materialization to avoid breaking prior patches.
- Add a small Android-independent shared state in the Shield library, accessible from the app and imported Shield Home source. It holds speech, room, media and presentation ownership; reserve eye/animation data without modifying eye implementations.
- Existing room controls discover HA entities in Shield. Wall currently hardcodes Living Room in setup and direct media/colour clients. Make room lookup configurable and refreshed per command; preserve default migration and HA local Assist.
- Existing Now Playing is on boop-shield-clean-launcher, not unified. Reuse its source and metadata/artwork/controls without merging its history or replacing its standalone package. Integrate a library in this requested rebuild; do not change default Android HOME or grant permissions.
- Existing Shield HA Back finishes to the fullscreen puppet, while onStop shows the overlay. Replace the automatic fullscreen entry with the existing Home surface and ensure Back has a deterministic destination.
- One profile authority continues to decide automatic defaults, with explicit first-run choice and a settings route for later changes.

## Ordered implementation

- [x] Shared state: immutable snapshots, immediate subscriptions, reentrant-safe publication, speech/room/media updates, one owner at a time; behavioral JVM tests. Wire lifecycle and speech adapters.
- [x] Room and discovery: current room store, user configuration, generic exposed HA domain/name/room resolver for lights/fans/switches; ambiguity fails closed; preserve Assist for unsupported requests. Behavioral tests for newly appearing entity, ambiguous names, offline/hidden devices and room isolation.
- [x] Now Playing: import the exact existing Shield Home sources, retain original assets unchanged as temporary renderers; share media state between Now Playing and corner owner. Home releases corner before acquiring its own view; leaving Home reclaims corner only during playback; stop removes it. No new notification access or overlay permissions granted automatically.
- [ ] Media commands: reuse Android media sessions and HA direct controls; explicit Deezer search/play intent to the installed provider, never Google fallback or false success. Preserve pause/resume. Test parsing and deterministic routing/failure behavior.
- [x] Back and profiles: first run/manual profile UI, TV HOME route to Shield launcher, HA controls back to preceding Home without starting headphones fullscreen. Test route matrix and escape policy.
- [x] Integration: enable existing non-visual signed GitHub workflow for rebuild branch, version candidate, run focused tests, inspect logs, reuse signed artifact for local phone/TV emulator inspection. Keep exact user device acceptance separate.
- [x] Review scoped diff and reconcile/publish handoff/status/memory; verify live branch SHA. No merge into boop-unified until the candidate is reviewed/tested.

## Constraints

Package com.boop.alpha1; permanent signer unchanged. Basic control local-first. No provider secrets. No extra microphone. No visual/golden/pixel/appearance tests. No eye asset or blink changes. No Turbo changes. Existing APK and physical v91 baseline are preserved. Check live remote before publication and never overwrite concurrent work.

## Test execution

Use failing focused JVM tests before production behavior changes. GitHub performs compilation, functional tests and signing. Manual emulator inspection checks navigation and visible UI; Ryan owns device/acoustic acceptance. Missing provider capabilities must produce a plain-English failure rather than inventing playback.

## Completion evidence

Selected implementation, review, signed CI and manual emulator checks completed for source e683b26e04a3bc2bb8ba5a94ee23eeb2380d1ec0 (v94). GitHub run 34437732145 passed 170 Unified and 58 Shield tests. See SESSION_HANDOFF.md for exact receipts, manual checks and pending physical/provider acceptance. Candidate remains unmerged; eyes transplant is separate.

Physical follow-up: Pixel 7 commands the remote Shield. The v94 same-device Deezer search route is insufficient; item 7 is reopened for remote artist playback. Preserve the accepted HA transport and Natural Voice results while verifying the Shield integration and playback capabilities. See the latest handoff.

## v96 follow-up: native artists, tracks and Flow

Approved by Ryan: repair repeat/switch artist requests, exact songs and default
play music to native Deezer Flow; add HA ADB integration. Resolve metadata,
verify the selected exposed TV against ADB hardware, restart Deezer, then read
semantic native focus before one remote selection. Preserve transport and voices.
The installed launcher reconnect bug required a scoped compatibility fix on its
existing branch, reviewed/built/signed and explicitly approved for installation.
No new app lineage, permission, eye work or visual CI. See current handoff for receipts.

Final v96 source 8a6c2bdb4a01149f86ce464505b2d354d1dde60f passed run34447810258
(200 Unified, 58 Shield tests); signed APK and manual Pixel emulator checks complete.
Native track/Flow/artist component checks observed playing indicators on the Shield.
Pixel end-to-end and acoustic acceptance remain pending; candidate stays unmerged.


## v97 supersedes v96 screen navigation

Ryan reports working v96 commands but unacceptable force-close/restart delay.
He accepts navigation while existing music keeps playing. Standard Deezer native
MediaController Play-from-URI was physically observed for Flow, artist and exact
song links without killing the process. Replace screen walking with the short-lived
source-built ADB media helper; keep identity/room/exposure/nonce/epoch guards.
Cold preparation never plays; recheck the request before dispatch. No provider
secrets, installed daemon, new permissions, changed eyes or visual CI. Local focused
tests and generated-helper emulator probe passed; signed build/APK acceptance pending.
See current handoff for exact verification levels and preserved artifacts.
