# BOOP Unified v43 durable memory

Date: 2026-09-07
Owning branch: `boop-unified`
Built code commit: `950611df0235d3943bf9958153efa470a342036b`
Version: versionCode 43 / `1.1.0-unified-dock-mirror-shield-settings`

This note is the current durable memory for the dock/mirror and Shield settings pass. It supplements the older historical `BOOP_MEMORY.txt`; current user instructions, `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, and fresh physical evidence still win.

## Physically accepted starting point

Ryan physically tested unified commit `e746affbb82b577cef2f1cf6e731dff186c8f881` and reported the app was fine to modify. Preserve that commit/artifact as the last physically accepted unified rollback point until v43 is tested on real bodies.

## Docked handheld behavior

- Undocked handheld BOOP does not keep the wake-word `AudioRecord` loop armed. It remains tap-to-talk.
- Wireless charging is the physical mode switch: docked BOOP may arm local wake-word listening while foregrounded.
- TTS, tap recognition, settings, lifecycle pause, and teardown continue to suspend/release wake capture through the existing coordinator.
- Docked eyes may sleep black independently of the wake microphone.
- While docked and asleep, a cheap proximity nudge may trigger a brief front-camera presence peek. The camera is not intended to run continuous room recognition in idle state; this keeps thermal cost below continuous vision while wireless charging.
- If the device lacks the required proximity/front-camera capability, fail quietly rather than inventing presence.

## Mirror mode

- Natural variants of “BOOP mirror”, including polite/wake prefixes such as “Hey BOOP, mirror”, open the front-camera mirror.
- Natural close/stop/exit/back-to-BOOP variants close it.
- Mirror is the deliberate continuous-camera exception because the user explicitly asked to see the camera feed.
- Horizontal mirror layout reserves slim `INSIDE` and `OUTSIDE` side rails. Sensor entity mappings are deliberately not invented yet; Home Assistant sensor selection comes later.

## Shield settings and room scope

- Shield settings are TV-first rather than a phone settings page stretched across a television: large grouped cards, BOOP black/cyan visual language, generous spacing, strong cyan/white focused state, and D-pad/Enter navigation.
- Do not add decorative settings animation.
- The Shield’s selected Home Assistant area is authoritative for what controls are displayed.
- Discovery remains read-only with respect to HA configuration. BOOP does not move, rename, or reassign entities/devices to areas.
- `HomeAssistantRepository` asks HA to expand the selected `area_id` target first; this allows HA to account for device-level area membership. BOOP then applies a local room-scope filter as defense in depth.
- A missing/mismatched room or unconfirmed entity is hidden rather than falling back to whole-house controls.
- Existing legitimate on/off actions remain permitted only for cards that survived the room filter, and confirmation must return the same room-scoped card.

## v43 verification

GitHub Actions run `34117631109` completed green for built commit `950611df0235d3943bf9958153efa470a342036b`.
Artifact: `BOOP-Unified`, ID `10017287954`.
APK SHA-256: `95ba6292c04edaa4db2f1028337f0b3009a7c5006ee9b423e1bc40a9addef4fb`.

The run passed preserved Wall guards, materialization, Launcher unit tests/lint, Shield unit tests (including room-scope and existing navigation-model tests), unified unit tests for dock/wake/mirror behavior, permanent signer setup, signed APK assembly, unified Shield-entry emulator smoke, package/version/manifest/signer checks, archive integrity, and artifact upload.

This is CI/signer/emulator green, not physical acceptance. Real-device checks still required: wireless dock detection and mic disarm/rearm on the target handheld/tablet, camera peek behavior and thermals, mirror orientation/rails, and the actual Shield remote/settings layout plus real HA room inventory filtering.

No laptop synchronization is claimed from this chat session. The connected GitHub branch is the published authority for this work.