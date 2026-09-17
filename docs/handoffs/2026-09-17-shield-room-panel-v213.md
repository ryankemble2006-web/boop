# Shield room panel v213

## Scope and starting point
Ryan approved the room-derived, remote-first lower Home panel, expanding into BOOP's lower-right space when he is absent and contracting when he returns. It uses the existing "Set this device room" selection and existing HA credentials. A persistent Home settings switch hides it completely. New UI is BOOP charcoal/cyan, not a stock Android menu. Sensors remain a later task.

After repeated interrupted sessions Ryan requested a fresh implementation, autonomous completion, and a signed APK link. This work starts directly from the live v212 owner commit `2ab0db655368089b19f9c705fba2cd404a1cd4e7`, not from the incomplete v211 panel branch or its uncommitted trees. Work branch: `boop-shield-room-panel-v213`.

## Implementation
- New pure-Java RoomPanelController, with cancellable connection/load/action deadlines, bounded reconnection and generation-checked callbacks. A click revalidates room membership immediately before using the existing HA repository's confirmed on/off service path. Pending requests cannot double-toggle, and late confirmations cannot overwrite newer observed state.
- RoomPanelSession adapts the existing BoopPreferences, HomeAssistantSession, HomeAssistantWebSocket and HomeAssistantRepository. All view callbacks return on the main thread. Connections, subscriptions, retries and pending actions stop outside launcher Home or when disabled. No duplicate room keys or authentication flow.
- Native charcoal ShieldRoomPanelView retains stable entity tiles and focus on state updates. Buttons expand with available width; overflow scrolls horizontally. D-pad Down enters from favourites, Up returns to the previous favourite; Left/Right stop at row ends. No new stock dialog or popup.
- Layout measures the visible favourite captions, then fills the available lower area. Existing hero slot, favourite positions and BOOP corner geometry remain intact. The panel animates its reserved right margin over 200 ms, respecting disabled system animations.
- Existing supported physical device controls are lights, switches/smart plugs and fans. The existing repository's one-primary-control-per-device and hidden/config/diagnostic filtering are preserved. Thermostat detail pages, cameras and sensor readings are not claimed.

## Verification approach
New deterministic controller tests cover no-room setup, confirmed commands, rejection, duplicate clicks, passive state changes, unavailable/unknown devices, stop/room-change stale callbacks, fresh membership, wrong-room replies, snapshot races, reconnection, reauthentication, request deadlines, cancellation and late command confirmation. Numerical layout tests cover expanded/contracted width, minimum space, overflow and many screen bounds. No emulator, image generation, screenshot or physical device was used.

The current v212 weather network permission, column-height repair and approved card chrome are retained. Voice, audio, artwork and native libraries are not edited. Wall remains v207 and is built for compatibility only, not requested for installation. Signed build and actual APK verification are recorded below. Ryan retains manual installation and physical/visual acceptance.

## Final verified result

Feature source `cfcb627348c5fde2bc4be86553f8a9648192a51b`; signed build source `7cb211b2a4f2b0307b500cc7ec718effa609ffd5`.
GitHub run `35244156761`, job `105279913277`: SUCCESS through build, APK verification, artifact upload and signer cleanup.
Artifact `10506423552`, `BOOP-Shield-v213-Wall-v207-Signed`.
Shield APK `BOOP-Shield-v213.apk`, package `com.boop.shieldoverlay`, 213 / `1.2.213-shield`, 160485741 bytes.
APK SHA-256 `cb21540979161b31ebebd756fbfea40ab1217ed8781ca8073094059c2286f783`.
Permanent certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256 `4dc5cb1ce6b6385d2f575ad04ec06d9cb95e2a35a3b49c52088765e2813eebfb`.

96 local focused tests passed. CI passed 70 initial focused checks, all 18 inherited stages, 95 materialized integration checks and 13 HA registry/room-filter unit tests; suites overlap rather than forming a unique summed count. The new 20-case controller suite includes a regression that was seen failing when a late service confirmation overwrote a newer external state; per-entity observation versions now preserve that latest state. Both application shells compiled and passed actual APK identity/version/certificate and frozen native/art checks.

The downloaded artifact ZIP matched GitHub's SHA-256 and passed CRC. The extracted Shield APK matched the exact source receipt, size and SHA-256 and passed its own CRC. New feature classes were present. All 16 native-library hashes and frozen artwork bytes were independently compared against the baseline/source and matched. No re-signing occurred outside GitHub.

First CI run `35243778496` stopped before the full APK build: Gradle configured all split shell projects while the newly added unit-test step ran before the existing required signer environment was prepared. The next commit moved the existing signer preparation ahead of Gradle configuration and supplied the same password environment to the test step. No permanent key, signer check, dependency or device permission was changed to resolve this.

Physical installation, live Home Assistant command behavior, D-pad ergonomics and visual acceptance remain pending for Ryan. No device control, emulator or screenshot was used. Only the Shield APK is delivered; the compatibility Wall v207 build is not an installation request. Root handoff/status/memory were updated with this result. The incomplete older panel branch remains historical and was not merged.
