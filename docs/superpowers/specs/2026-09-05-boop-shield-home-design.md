# BOOP Shield Home — TV Dashboard, Local Pairing, and HA Control Design

Date: 2026-09-05
Status: Approved in chat; awaiting written-spec review before implementation planning
Target branch: `boop-shield-overlay-poc`
Known-good baseline: `37c8c22f69c37908b1fd68b07ed1f6b638be93cf`

## Goal

Evolve the existing known-good `shield-overlay` APK into BOOP's Nvidia Shield / Android TV body without changing its package identity or forcing an uninstall.

The Shield app must keep the proven omnipresent eye overlay, add a proper remote-first BOOP Home experience for a large TV, pair to Home Assistant without typing on the Shield, and grow toward simple Home Assistant control and routine creation while keeping Home Assistant as the authority for the house.

The first implementation slice ends with one real local Home Assistant binary control working end-to-end from the TV after QR pairing and one-click room selection.

## Product rules

- Keep the existing `shield-overlay` project and package identity `com.boop.shieldoverlay` so upgrades install over the known-good build and retain overlay permission/state where Android permits.
- Preserve the known-good overlay behavior and HDR/display-mode redraw fix.
- BOOP is a puppet interface, not a second smart-home platform.
- Home Assistant owns devices, areas, scenes, state, automations, permissions, and execution.
- BOOP may cache presentation state and local UI preferences, but must not create a second device database or automation runtime.
- LAN-first is a product rule. Pairing, normal HA control, room state, favourites, and routine writes must not require a BOOP cloud service.
- Cloud services may add optional intelligence later but may never become a required toll gate for controlling the house.
- Shield interaction is D-pad, Select, Back, and confirmation. Avoid typing with the remote as a standing rule.
- Do not add accessibility-service hacks solely to steal hardware buttons from Google Assistant.
- Sensor-heavy dashboards are explicitly out of scope for Shield BOOP; that belongs to BOOP Wall / screensaver-style surfaces.

## Existing overlay boundary

The current eye overlay is a protected subsystem.

It retains:

- the existing approved eye artwork and renderer
- existing size and upper-right placement
- `TYPE_APPLICATION_OVERLAY`
- `FLAG_NOT_FOCUSABLE`
- `FLAG_NOT_TOUCHABLE`
- foreground-service persistence
- the `DisplayManager.DisplayListener` redraw on display changes that physically passed HDR10, resolution switching, Kodi/Home switching, pause/resume, and Shield sleep/wake testing

The dashboard must not reach into or redesign the eye renderer.

The overlay service gains only a small explicit visibility interface so BOOP Home can request `hide` while the full-screen TV activity is foreground and `show` when BOOP Home exits. Hiding must not destroy the foreground service or discard its known-good runtime behavior.

Dolby Vision remains unverified and must not be claimed as tested until a physical DV test passes.

## Application shell

There is one installed BOOP Shield APK, not two cooperating Shield packages.

It contains two intentionally separate UI worlds:

1. **Overlay world** — the small omnipresent, non-interactive eyes.
2. **BOOP Home world** — a normal full-screen Android TV activity with real D-pad focus and navigation.

BOOP Home is launchable from both:

- the Android TV / Shield launcher
- the physical Netflix button through the user's existing Button Mapper setup

BOOP itself does not intercept the Netflix key globally.

Opening BOOP Home hides the little overlay eyes. Back behaves normally through nested screens; Back from the root Home page exits to the previous app and restores the little overlay eyes.

Later, BOOP Home may render a much larger 55-inch puppet face as part of the activity. That future full-screen puppet is separate from the protected overlay renderer.

## Launcher identity

Keep the current application/package identity but give the Shield app fresh TV-specific launcher artwork:

- a new Shield/Android TV launcher icon
- a new Leanback banner
- artwork consistent with BOOP's existing eye-led visual language
- no change to the approved overlay eye asset as part of the launcher-art pass

The launcher presentation must read clearly from sofa distance and remain visually distinct from Kodi/Forki artwork.

## First-run pairing screen

Before Home Assistant is paired, BOOP Home shows almost nothing:

- BOOP branding / eyes
- **“I found your house”**
- one very large QR code
- one obvious **Connect** action

No dashboard clutter appears behind this state.

The Shield discovers a local Home Assistant instance via Home Assistant's documented `_home-assistant._tcp.local.` mDNS/Zeroconf discovery path. The normal successful path must not ask the user to type a Home Assistant URL on the Shield.

If discovery cannot find an instance, the TV explains the problem plainly and offers phone handoff rather than exposing a Shield keyboard. Manual/fiddly connection settings belong on BOOP Wall / phone, not the TV.

## QR pairing philosophy

Pairing is local, temporary, and not permanently pokeable.

When the pairing screen is visible, the Shield creates a short-lived local pairing session. The QR contains only information needed for BOOP Wall on the phone to reach and authenticate that temporary session. It must not contain a Home Assistant password, long-lived access token, or refresh token.

Rules:

- pairing session lifetime: 120 seconds
- one successful use invalidates it immediately
- leaving/cancelling the pairing screen invalidates it
- retrying creates fresh session material
- after pairing finishes, the temporary pairing endpoint is shut down completely
- there is no permanent BOOP HTTP/HTTPS server listening after setup
- no BOOP cloud account or relay is involved

User-facing stale-session wording: **“That one went stale.”** with a simple retry action.

## Local pairing transport

The phone-to-Shield pairing channel is direct over the LAN and uses temporary pinned TLS.

For each pairing session the Shield creates fresh ephemeral TLS identity/session material. The QR carries the information BOOP Wall needs to pin that exact temporary Shield identity. BOOP Wall must reject a peer that does not match the QR-pinned identity.

This requirement must not silently fall back to unauthenticated LAN HTTP. If Android platform constraints prevent the pinned temporary channel from being implemented cleanly, implementation stops at that boundary and the pairing design is revisited while preserving the LAN-only rule.

No long-lived listener or reusable pairing key remains after pairing.

## Home Assistant authorization handoff

BOOP owns the setup experience, but Home Assistant owns credential entry and authorization.

Phone flow:

1. Scan the Shield QR.
2. BOOP Wall opens the pinned local pairing session.
3. BOOP Wall explains that the Shield BOOP wants to join the house.
4. User continues into Home Assistant's real authorization/sign-in UI.
5. Home Assistant performs authentication and returns a one-time authorization code/result.
6. BOOP Wall passes only that short-lived authorization result to the waiting Shield through the pinned local pairing channel.
7. The Shield completes its own Home Assistant token exchange using the same client ID used for authorization.
8. The Shield stores its own refresh credential in Android Keystore-backed secure storage and uses short-lived access tokens for API calls.
9. The temporary pairing session shuts down.
10. BOOP Shield gives a small puppet reaction and says **“Found it.”**
11. BOOP automatically continues to **“Where am I?”**

The Shield must end up with its own Home Assistant credential. Do not copy BOOP Wall's existing refresh token onto the Shield.

Home Assistant's IndieAuth/OAuth validation requires the authorization-code token exchange to preserve the client ID used during authorization. The implementation must use a local/same-origin authorization callback strategy so normal pairing does not depend on a BOOP-hosted public website or relay merely to validate redirects.

The intended first approach is for BOOP Wall's controlled auth surface to intercept the one-time callback and relay the authorization code to the Shield over the already pinned pairing channel, while the client ID and redirect origin remain same-origin for Home Assistant validation. This exact callback flow must be proven in a small implementation spike before broad dashboard work. If current Home Assistant or Android behavior blocks the local callback cleanly, stop and revise the design rather than introducing a cloud dependency.

## BOOP Wall companion boundary for the first slice

The existing BOOP Wall/Pixel application remains a separate app and keeps its current interaction behavior.

For this first Shield slice, BOOP Wall gains only the minimum companion behavior required for pairing:

- open a BOOP Shield pairing QR/deep link
- connect to the QR-pinned temporary Shield session over LAN
- host/launch the Home Assistant authorization handoff
- return the one-time authorization result to that Shield session
- show plain success/failure and then close the temporary pairing context

The friendly routine editor is a later BOOP Wall slice and must not be bundled into this first Shield milestone.

## Room identity

Immediately after pairing, BOOP asks:

**“Where am I?”**

It loads Home Assistant areas and presents them as very large one-click D-pad cards such as:

- Living Room
- Bedroom
- Kitchen

Select saves that Shield's home area locally.

The same APK can therefore be installed on another Shield/TV and assigned a different room with one click.

`Settings -> Where am I?` allows changing the area later with the same no-typing card picker.

The saved room is BOOP's default context for future unqualified voice/control requests. Explicitly named rooms must always override the local default.

## BOOP Home navigation

The main TV activity uses a permanent left-hand vertical navigation rail:

- **Home**
- **Routines**
- **Settings**

Navigation must be deterministic with D-pad focus, large spacing, large type, strong focus treatment, and no tiny touch-oriented controls.

## Home dashboard

The Home page uses a hybrid layout.

### Favourites

The top row contains a small number of very large favourite control/scene cards.

Favourites are per-BOOP-device presentation preferences, not Home Assistant configuration.

BOOP initially suggests:

- useful devices from that BOOP's selected room
- obvious whole-house scenes/routines when appropriate

The user can pin/unpin favourites with the Shield remote.

A Living Room BOOP and Bedroom BOOP may therefore naturally have different favourites.

### Rooms

Below favourites are large room cards.

Opening a room presents:

1. the most useful controls first
2. then large grouped sections such as **Lights / Fans / Climate / Media / Scenes / Other**

Do not expose `entity_id`, service names, domains, YAML, or Home Assistant implementation jargon in normal TV UI.

### Control behavior

Use a smart hybrid:

- simple binary devices: one Select press performs the obvious action
- richer devices: Select opens a large remote-friendly detail panel

Examples of rich controls include brightness, fan speed, thermostat setpoint, and media controls.

Back closes the detail panel and returns focus to the originating card.

## Offline / HA-unreachable behavior

BOOP Home remains alive if Home Assistant is temporarily unavailable.

- keep the cached dashboard structure visible
- show cached last-known states clearly as stale/not current
- disable controls that require Home Assistant
- never claim a command succeeded without an HA success result
- keep BOOP-local navigation, eye settings, room identity display, and other local settings usable
- reconnect automatically when HA becomes reachable again without requiring re-pairing

Plain failure wording should remain human, e.g. **“I can't reach the house right now.”**

## Voice design

Voice is not required for the first implementation slice, but the architecture must leave a clean path for it.

Preferred input order:

1. Shield remote microphone button when Android exposes it cleanly to BOOP
2. a large visible **Talk to BOOP** fallback button

If Google Assistant owns the hardware mic key and Android does not provide a clean supported handoff, BOOP must not add an accessibility-service interception hack just to steal it.

Future listening presentation:

- quick device commands: compact listening layer over the current page
- longer routine creation: full-screen 55-inch BOOP puppet/listening state

## Routines design

Routine creation is deferred beyond the first implementation slice, but its UX and authority model are locked.

Home Assistant remains the automation runtime. BOOP is a plain-English author/editor.

The Shield Routines page shows:

1. BOOP-friendly routines first
2. an **Other automations** section below for advanced HA automations BOOP cannot fully translate

BOOP-friendly language:

- **When this happens** instead of Trigger
- **Only if** instead of Conditions
- **Do this** instead of Actions
- **Then** for chained steps
- **Wait** for delays
- ordinary **Every / At / After** schedule language
- **Rooms / Things / Scenes** rather than entity-centric jargon

Routine creation flow:

1. **Create Routine**
2. **Talk to BOOP**
3. BOOP produces a plain-English draft
4. user chooses **Save / Edit on phone / Start again**
5. meaningful/destructive changes require a plain-English read-back and explicit confirmation before BOOP writes to Home Assistant

The first editor intentionally targets the common 90% of household routines. Advanced HA-only constructs are not forced into the simple UI.

If BOOP cannot safely round-trip an existing HA automation, it must say so and avoid pretending it understands it. Advanced automations remain visible in **Other automations** with safe operations such as Run / Enable / Disable / Open on phone where technically supported.

## BOOP Wall phone editor handoff

For advanced editing within BOOP's normal language, **Edit on phone** presents a QR code and opens the BOOP Wall app.

BOOP Wall owns the friendly routine editor rather than dropping the user into Home Assistant's native automation editor by default.

Home Assistant continues to store and execute the automation.

The phone editor uses the same simple concepts as the Shield and may later expose a clearly separated Advanced path for HA-specific constructs.

## Settings

Shield Settings is intentionally small and remote-friendly:

- **Where am I?**
- **Home Assistant** — connection status, reconnect, unpair
- **Voice** — mic-button preference/status and Talk to BOOP fallback
- **Eyes** — overlay visibility and later dashboard-eye options
- **Favourites** — reset suggestions / clear local pins
- **Edit on phone** — QR handoff for fiddly configuration

Do not expose tokens, YAML, URLs, or keyboard-heavy configuration in ordinary Shield settings.

## First implementation slice

The first implementation is deliberately smaller than the full design.

It must add:

- BOOP Home full-screen Android TV activity inside the existing `shield-overlay` APK
- launcher/Leanback entry with new Shield-specific icon and banner
- explicit hide/show bridge to the protected overlay service
- first-run **“I found your house”** pairing screen
- local Home Assistant discovery
- 120-second LAN QR pairing handoff to BOOP Wall using QR-pinned temporary TLS
- the minimum BOOP Wall pairing companion change described above
- secure storage of the Shield's own HA credential
- **“Found it.”** puppet success moment
- one-click **“Where am I?”** area picker
- basic Home shell with left rail, favourite/room skeleton, and large D-pad focus behavior
- one real binary Home Assistant control from a favourite card to prove the end-to-end local path
- cached/offline presentation sufficient to prove that loss of HA does not kill BOOP Home

The first binary control is selected from a real Home Assistant entity exposed in the chosen room during physical testing; it is not hard-coded as a permanent BOOP device model.

It must not add yet:

- routine creation/editing
- full routine list translation
- BOOP Wall routine editor
- voice control
- mic-button ownership work
- rich dimmer/fan/climate/media control panels beyond what is needed to scaffold the navigation boundary
- sensor dashboards
- full-screen giant puppet implementation
- BOOP cloud relay/account service
- permanent local server
- accessibility-service button interception

## First physical acceptance test

Target: existing Shield running the known-good overlay, then a second Shield/bedroom install after the first passes.

Acceptance sequence:

1. Install the new APK over the current `com.boop.shieldoverlay` build without uninstalling.
2. Confirm the existing overlay permission remains usable and the little eyes still appear over normal Shield/Kodi content.
3. Launch BOOP Home from the launcher.
4. Confirm the little overlay eyes hide without stopping/destroying the known-good service.
5. Confirm first-run shows **“I found your house”** and a very large QR.
6. Scan with BOOP Wall on the phone.
7. Complete Home Assistant authorization on the phone; do not type credentials or codes on the Shield.
8. Confirm BOOP Shield says **“Found it.”** with a small puppet reaction.
9. Confirm **“Where am I?”** appears automatically.
10. Select **Living Room** with one D-pad click.
11. Confirm BOOP Home appears with large TV-scale cards and deterministic D-pad focus.
12. Use the single implemented binary favourite control and verify the intended Home Assistant device changes state.
13. Exit BOOP Home with Back and confirm the previous app returns and the little overlay eyes reappear.
14. Reopen BOOP Home and confirm pairing plus Living Room identity persist.
15. Play the previously tested HDR10 material and perform Kodi/Home resolution switches; confirm the little overlay retains the known-good display-change behavior when BOOP Home is not foreground.
16. Disable internet access while keeping the LAN and Home Assistant available; confirm the implemented HA control still works.
17. Make Home Assistant unreachable; confirm the dashboard remains visible with stale state clearly indicated, HA controls disabled, and BOOP-local navigation/settings still usable.
18. Restore Home Assistant; confirm live control resumes without re-pairing.
19. Install the same APK on the bedroom Shield/TV and choose **Bedroom** during the one-click room step.
20. Confirm its local room identity is independent of the Living Room Shield.

Passing this slice proves the core architecture before adding routines, voice, and richer device controls.

## Testing strategy

Protect the known-good overlay with regression tests before changing runtime behavior.

Automated coverage should include:

- overlay window type/flags and HDR display-change redraw remain present
- hide/show does not destroy the overlay service or replace its renderer
- activity lifecycle restores the overlay when BOOP Home leaves foreground through normal Back flow
- pairing sessions expire after 120 seconds, are single-use, and are destroyed on cancellation/success
- BOOP Wall verifies the QR-pinned temporary TLS identity before passing authorization data
- QR/session material never serializes HA passwords or persistent HA tokens
- HA authorization result is accepted only for the active authenticated pairing session
- the Shield stores its own refresh credential and does not copy BOOP Wall's refresh token
- persisted room identity is per installation/device
- favourites are local presentation data rather than copied HA entities
- offline state prevents action dispatch and cannot report false success
- D-pad focus paths for Home/rail/room/favourite control are deterministic
- forbidden additions remain absent: no BOOP cloud endpoint dependency, no accessibility service, no permanent pairing listener

CI must still build and inspect the Android TV APK and must continue to run the existing BOOP regression suite.

## Definition of done for this design cycle

This design is ready for implementation planning when the user approves this written spec. Implementation planning must then decompose the first slice into small test-driven steps while treating the current overlay baseline as protected behavior.