# BOOP Alpha 3 — Home Assistant device identity and raw speech

Date: 2026-09-04
Branch: `alpha2-local-ha-control`

## Goal

BOOP must stop owning a command vocabulary. If a device is exposed to Home Assistant Assist, BOOP should be able to control it without BOOP learning the vendor, entity name, domain, or sentence form.

Core rule:

**HA works for BOOP. BOOP works for the user.**

For the current Pixel 7 Pro build, BOOP is permanently treated as **BOOP Wall** in **Living Room**.

## Scope

Alpha 3 covers actuator control only. Sensors and state queries are deferred.

Any Home Assistant entity/domain that Assist can already control is in scope automatically, including future Govee, Sonoff, and other devices, provided those entities are exposed to Assist.

Out of scope:
- BOOP-maintained entity/device catalogs
- vendor-specific integrations or aliases
- sentence packs or command grammar
- sensor questions/state queries
- automation creation/editing
- cloud AI wording or capability
- multi-room setup UI

## Architecture

### 1. BOOP has a Home Assistant device identity

After the existing Home Assistant OAuth connection succeeds, BOOP registers itself with Home Assistant using the native `mobile_app` registration endpoint.

Registration identity:
- app/device name: `BOOP Wall`
- current physical room: `Living Room`
- registration is created once and persisted

BOOP then obtains Home Assistant's actual device registry ID for that registration and stores only that device ID locally alongside the existing HA connection data.

BOOP does not download or persist the Home Assistant entity registry.

### 2. BOOP assigns itself to Living Room once

BOOP resolves the Home Assistant area whose human-readable name is `Living Room`, then sets the BOOP Wall device's `area_id` to that area through Home Assistant's authenticated device registry API.

The area assignment is performed only when BOOP's HA device identity is first created or when the stored identity is missing/invalid.

If the authorised HA account cannot perform the device-registry update, BOOP must not compensate by rewriting speech or inventing room rules. It reports a plain setup failure once and leaves command text untouched.

### 3. Speech is passed through unchanged

The current `RoomContext` grammar layer is removed from the command path.

For every recognised command, BOOP sends Home Assistant:
- `text`: the exact recognised transcript, unchanged
- `language`: the current BCP-47 language tag
- `device_id`: BOOP Wall's stored HA device ID

BOOP must not:
- change word order
- add `in the Living Room`
- normalise `turn`, `switch`, `on`, `off`, or any other verb/state
- inspect the sentence for known devices or areas
- retry with a rewritten sentence

Home Assistant Assist is solely responsible for understanding the utterance and resolving an exposed target.

### 4. Device growth is automatic

The desired user flow is:

1. Add a new device to Home Assistant.
2. Give it a sensible Home Assistant name/alias/area as needed.
3. Expose it to Assist.
4. Speak to BOOP.
5. Home Assistant resolves and controls it.

No BOOP APK update or BOOP-side teaching is required.

## Data flow

`tap BOOP -> Android speech recogniser -> exact transcript -> HA conversation API + BOOP device_id -> Home Assistant Assist -> exposed entity action -> HA result -> BOOP reply`

Home Assistant remains the source of truth for:
- entity names
- aliases
- rooms/areas
- device types/domains
- supported actions
- vendor integrations

BOOP remains responsible only for:
- capturing speech
- identifying itself to HA
- sending the raw transcript
- converting HA success/failure into a short puppet reply

## Failure behaviour

- HA reports a successful action: `Done.`
- HA cannot resolve a valid exposed target: `I can't find that.`
- HA endpoint cannot be reached: `I can't reach the house right now.`
- HA authentication expires/rejects: use the existing reconnect path
- HA rejects/fails an otherwise resolved action: `That didn't work.`
- BOOP device registration/room assignment fails: plain one-time setup failure; do not rewrite speech as fallback

The existing universal `Which room?` mapping for `no_valid_targets` is removed because `no_valid_targets` is broader than room ambiguity.

## Code changes

Expected production changes:
- remove `RoomContext` from `MainActivity` command handling
- delete `source/RoomContext.java`
- add a small HA device-identity/setup component responsible for registration, device-ID retrieval, area lookup, and one-time area assignment
- extend local secure/persistent connection storage with BOOP's HA device ID and any minimal registration identifiers required for re-use
- extend `HomeAssistantClient` so `/api/conversation/process` includes `device_id` and receives the raw transcript
- change `LocalReply` `NO_TARGET` wording to `I can't find that.`
- bump Android version metadata for the first permanent-signer over-install test

No vendor/entity hardcoding is permitted.

## Testing

### Unit/regression tests

Tests must prove:
- arbitrary raw transcripts arrive at the HA request builder unchanged
- `device_id` is included in the conversation request
- no production code normalises or appends room text to commands
- registration/setup runs when BOOP identity is absent and is skipped when a valid identity is already stored
- `Living Room` is resolved by name to its HA area ID before assignment
- target lookup failure maps to `I can't find that.`
- existing auth, unreachable-house, malformed-response, speech-language-tag, and signing regressions remain green

The old `RoomContextTest` tests are removed because they enforce behaviour Alpha 3 explicitly forbids.

### CI/device checks

The existing GitHub Actions checks remain mandatory:
- source regressions
- JVM/Android unit tests
- permanent signing guard
- signed APK build and inspection
- Android 16 emulator install/launch/no-crash check
- APK artifact upload

### Physical acceptance

On the Pixel 7 Pro:
- install Alpha 3 directly over the current permanently signed Alpha 2 without uninstalling
- existing HA auth/app data should remain intact
- confirm BOOP Wall appears in HA and is assigned to Living Room
- verify multiple natural command forms without adding BOOP grammar rules
- add/expose a different HA actuator and confirm BOOP can control it with no BOOP code change

Physical behaviour is not considered proven until tested on the real Pixel.

## Locked principles

- BOOP does not learn the house; Home Assistant does.
- BOOP never carries a private device dictionary.
- Exposed-to-Assist means controllable by BOOP without app changes.
- Raw speech goes to HA unchanged.
- The current build's fixed physical home is Living Room.
- BOOP's cloud dependency remains zero for basic Home Assistant control.
