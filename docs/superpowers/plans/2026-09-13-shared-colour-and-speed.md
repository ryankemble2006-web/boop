# Shared colour, then animation speed implementation plan

Goal: extend accepted v156 without replacing working Wall hue or animation artwork.
Base: a901c1e9f31e55c710e31ac7ff4f5924c9769d56. Shield polish is user-accepted.
Isolated owner: boop-unified-eye-sync-safe-v159. Native lyrics stays separate.

## Boundaries
Keep BoopEyeHue/Math/Overlay, CanonicalEyeRenderer, shaders, EyeMotion, EyeCatalogue,
SignMotion and the approved master bytes unchanged. Retain boop_eyes/hue_degrees,
range 0..359 and default 190. No voice, transport, permissions or signing changes.
Pixel 10 is excluded. Substantive changes require local emulator checks first.

## Researched design
1. Lifecycle-scoped colour listeners make every existing canonical renderer read
   the same existing local hue. Register only after setRenderer; keep strong
   listener references and unregister on detach. Never reset hue during upgrade.
2. Cross-device colour uses the existing paired Home Assistant OAuth/WebSocket
   client. Sharing starts OFF. Explicit opt-in can create one marked input_text
   helper; ordinary startup/reconnect cannot create or overwrite unknown helpers.
   Discover actual helper/entity identity, validate its marker and 0..359 payload.
   No unauthenticated LAN receiver, invented sensor persistence or new HA plugin.
   HA retains the accepted shared value. Offline local hue remains usable; on
   reconnect shared state is read before any write. Never publish stale defaults.
3. Speed is device-local, default 1x, selectable 0.5x/1x/1.5x/2x. Use the Lab's
   elapsed-delta multiplier with continuous phase, not Android animator scale.
   Keep all authored motion and durations unchanged; adjust the input clock only.
   Test 1x pose equivalence and sleep/one-shot completion, not just a moving image.

## Work sequence
- [ ] Colour protocol/model and binding tests red, then implementation and green.
- [ ] Add opt-in HA adapter and settings entry; test errors, reconnect, identity,
      coalescing, no echo, cancellation, and preserve local hue while disabled.
- [ ] Commit/test colour before adding speed.
- [ ] Add clock tests against the unchanged baseline, then speed implementation.
- [ ] Run full existing GitHub build/test/sign/integrity and local TV/phone checks.
- [ ] Deploy only after gates pass, to Shield/Pixel 7; leave Pixel 10 alone.
- [ ] Publish exact evidence, limits, status and handoff. No visual CI judgement.

## Primary references checked 2026-09-13
- https://developer.android.com/reference/android/content/SharedPreferences
- https://developer.android.com/reference/android/content/SharedPreferences.OnSharedPreferenceChangeListener
- https://developer.android.com/reference/android/opengl/GLSurfaceView
- https://developer.android.com/reference/android/os/SystemClock
- https://developers.home-assistant.io/docs/api/websocket/
- https://www.home-assistant.io/integrations/input_text/
- https://github.com/home-assistant/core/blob/dev/homeassistant/components/input_text/__init__.py
- https://github.com/home-assistant/core/blob/dev/homeassistant/helpers/collection.py
- https://github.com/home-assistant/core/blob/dev/tests/components/input_text/test_init.py

HA helper creation is admin-only; ordinary set_value uses entity permissions.
The create/list API is checked against official implementation, not a promise
of a permanently versioned public interface. Unsupported/denied setup must fail
closed with local hue intact. Omit initial so HA restores the last shared value.
The old puppet-integration LAN register is explicitly unauthenticated and also
changes blink speed/voice; it is research material only, not a merge candidate.
The Lab MotionPolicy.scaledDelta proves the intended rate multiplier, but its
v13 LAN notification tester is not a secure production sync transport.

The v158 draft had overlapping writes with incompatible binding interfaces.
It is preserved untouched; v159 owns this reviewed continuation.
