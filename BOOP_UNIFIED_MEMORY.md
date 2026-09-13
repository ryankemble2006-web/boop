# Active continuity

Shared eye colour first, then speed. GitHub feature owner `boop-unified-eye-sync-safe-v159`. Accepted v156 source is preserved independently; its sweep, favourites and artist navigation are already signed off.
Recovery snapshots: `wip/boop-colour-v158-recovery@484d292f` and `wip/boop-colour-v159-recovery@7ea7d26d`. They are not releases. The truncated runtime still needs completion and app/settings wiring.
Use the existing `boop_eyes/hue_degrees` range 0..359/default 190. Keep original slider, shaders, eye master, EyeMotion, EyeCatalogue and sign choreography intact. HA sharing is opt-in and authenticated, never a trusted-LAN-only UDP receiver. Do not push startup defaults over another device's colour. Device-local animation speed must preserve 1x exactly.
All source/build/test/signing work goes through GitHub after this recovery. Emulators and Shield/Pixel 7 are runtime/visual gates. Pixel 10 is untouched and excluded. No evidence of completed colour runtime or speed yet. See plan and handoff for references and next step.
