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

- [ ] Shared state: immutable snapshots, immediate subscriptions, reentrant-safe publication, speech/room/media updates, one owner at a time; behavioral JVM tests. Wire lifecycle and speech adapters.
- [ ] Room and discovery: current room store, user configuration, generic exposed HA domain/name/room resolver for lights/fans/switches; ambiguity fails closed; preserve Assist for unsupported requests. Behavioral tests for newly appearing entity, ambiguous names, offline/hidden devices and room isolation.
- [ ] Now Playing: import the exact existing Shield Home sources, retain original assets unchanged as temporary renderers; share media state between Now Playing and corner owner. Home releases corner before acquiring its own view; leaving Home reclaims corner only during playback; stop removes it. No new notification access or overlay permissions granted automatically.
- [ ] Media commands: reuse Android media sessions and HA direct controls; explicit Deezer search/play intent to the installed provider, never Google fallback or false success. Preserve pause/resume. Test parsing and deterministic routing/failure behavior.
- [ ] Back and profiles: first run/manual profile UI, TV HOME route to Shield launcher, HA controls back to preceding Home without starting headphones fullscreen. Test route matrix and escape policy.
- [ ] Integration: enable existing non-visual signed GitHub workflow for rebuild branch, version candidate, run focused tests, inspect logs, reuse signed artifact for local phone/TV emulator inspection. Keep exact user device acceptance separate.
- [ ] Review scoped diff and reconcile/publish handoff/status/memory; verify live branch SHA. No merge into boop-unified until the candidate is reviewed/tested.

## Constraints

Package com.boop.alpha1; permanent signer unchanged. Basic control local-first. No provider secrets. No extra microphone. No visual/golden/pixel/appearance tests. No eye asset or blink changes. No Turbo changes. Existing APK and physical v91 baseline are preserved. Check live remote before publication and never overwrite concurrent work.

## Test execution

Use failing focused JVM tests before production behavior changes. GitHub performs compilation, functional tests and signing. Manual emulator inspection checks navigation and visible UI; Ryan owns device/acoustic acceptance. Missing provider capabilities must produce a plain-English failure rather than inventing playback.
