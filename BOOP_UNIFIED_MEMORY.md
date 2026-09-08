# BOOP unified memory

Updated 2026-09-08. Read fresh main authority and SESSION_HANDOFF.md before work. All normal app work belongs on `boop-unified`, not a stale primary or historical app checkout.

## Durable approved rules

- BOOP forever. One extra spoken wake name in `boop_voice/wake_name`; BOOP is always the fallback. No app/package/class/branding/HA/pairing/signing/repository identity rename. Real-model token tests do not establish acoustic success.
- Shield Home is Room -> actual supported controllable devices. No Favourites. Devices do not belong in Settings; Settings is configuration-only. Keep cyan chunky TV controls, fixed geometry on focus, usable D-pad navigation and focused-row retention during state updates.
- HA discovery/room membership is read-only. Trust explicit entity area and inherited device area through HA target expansion; filter locally, fail closed, never silently reveal or operate the whole house. Hide helpers, diagnostics/configuration and unsupported/unavailable controls. HA compact category lookup can be a keyed object, not just an array.
- Locked eye artwork is never regenerated. User hue changes only the blue/cyan iris ring, preserving sclera/pupil/reflections and original default blue. Reuse tint buffers instead of allocating a full image for every slider tick.
- Ryan performs visual acceptance on hardware. Never add or run GitHub screenshot/golden-image, appearance/layout/animation judgement or source-string aesthetic gates unless he explicitly reverses this rule. Preserve functional tests, build/signature/package checks and security checks. Non-visual crash/entry smoke is not visual approval and follows signed-APK upload.
- Undocked Wall is tap-to-talk. Foreground wireless charging permits continuous wake. Preserve mic ownership around settings/TTS/tap/resume. No always-on handheld mic workaround.
- Hands/2.5D puppetry, later inside/outside HA sensor rails and AC teaching remain separate deferred work. Existing media/blink/launcher/permissions/signing stay untouched in this repair.

## Evidence to preserve

Delivered `6cd9c67` was CI-green but had a known custom tokenizer defect and now has Ryan's failed custom plus fallback wake test. The tokenizer repair and fallback creation are integrated in subsequent source, not in that old APK. The exact reason for the physical wake failure is not yet proven. Pixel 7's Android 17 update did not immediately break the tested media/blink/colour features; that is partial observation, not full OS certification.

Current source continues from live `848a997`, with fresh build/artifact and physical acceptance pending. Protected accepted rollback `e746affbb82b577cef2f1cf6e731dff186c8f881` remains. Original receipts and longer product history remain in `docs/history/unified-v43/`. GitHub publication does not imply the Windows worktree is synchronized or a device is installed.
