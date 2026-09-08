# SHIELD TURBO control-centre design

Date: 2026-09-08
Owner branch: `shield-turbo-v01`
Package: `com.boop.shieldturbo`
Status: design only; no application code changed by this document

## Goal

Grow the physically proven brightness utility into a remote-first NVIDIA Shield control centre without pretending Android can do things it cannot. Build useful STANDARD-mode tools first, preserve the exact working brightness behaviour, then add an explicitly enabled ADB TURBO layer only for capabilities that are proven on real Shield hardware.

## Product shape

SHIELD TURBO stays a separate app from unified BOOP. It remains landscape, chunky, D-pad first and understandable from the sofa. The home screen becomes a small set of large cards instead of a dense settings list:

1. **TURBO** — honest performance/health snapshot plus safe maintenance actions.
2. **PICTURE** — the already proven 10–100% brightness control and display information.
3. **APPS** — launch/relaunch helpers and fast routes into Android's own app-management surfaces.
4. **NETWORK** — connection, address/transport and simple connectivity diagnostics.
5. **SHIELD** — device, storage, display/HDMI and system shortcuts.
6. **ADB TURBO** — hidden/locked until explicitly configured and positively detected.

The existing `ANALYSE SHIELD` path may be folded into these cards, but its truthful diagnostics remain available.

## Approach choice

### Rejected: fake optimiser

Do not add RAM-clean scores, placebo cache cleaners, governor claims, generic process killing or a big `BOOST` button whose effect cannot be measured. Modern Android aggressively owns process lifecycle, and SHIELD TURBO must not manufacture performance theatre.

### Rejected: jump directly to root/shell architecture

Do not require root, ship a permanent local ADB daemon, bundle private keys or silently depend on a shell bridge. That increases attack surface and makes the simple utility fragile.

### Chosen: staged STANDARD + proven ADB TURBO

Phase 1 uses only ordinary app capabilities and Android-owned settings surfaces. Phase 2 adds a one-time, user-approved ADB setup for specific elevated grants/actions. Each ADB capability is independently detected after setup. If a capability is absent, the UI says so and falls back to the standard route.

## Phase 1: STANDARD control centre

### TURBO card

Show real values, not a synthetic score:

- available/total memory;
- internal storage free/total;
- CPU frequency sources already exposed by the current diagnostic path, labelled as frequency rather than load;
- thermal sources only with their actual kernel/source labels unless their identity is proven;
- uptime and basic device/build information;
- a refresh action for a new snapshot.

Safe actions:

- **Free space** opens Android's storage-management surface;
- **Manage apps** opens Android's application-management surface;
- **Restart SHIELD TURBO** cleanly recreates/relaunches this app only;
- **Quick check** reruns the diagnostic snapshot and highlights concrete conditions such as critically low storage rather than inventing a percentage boost.

No programmatic killing of arbitrary third-party apps in STANDARD mode. Where Android owns Force Stop, SHIELD TURBO should open the correct system App Info page instead of pretending it can force-stop another package itself.

### APPS card

Provide a remote-friendly app picker based on launchable installed apps. For a selected app:

- Launch app.
- Open Android App Info.
- Open its storage/settings page when Android exposes one.
- Offer a relaunch helper only when it can be implemented honestly; otherwise direct the user through App Info/Force Stop and then Launch.

Do not clear another app's private data/cache programmatically.

### SHIELD card

Large shortcuts into useful Android/Shield system panels, subject to intent availability on the actual firmware:

- Display & Sound;
- Apps;
- Storage;
- Network & Internet;
- Accessibility / special access relevant to SHIELD TURBO;
- Developer options when available;
- system About/device information.

Show detected Shield model/build and uptime. Sleep/reboot controls are not advertised in STANDARD mode unless the platform exposes a legitimate route that works on the target firmware.

### NETWORK card

Show transport and current local network facts already available to the app. Add a simple on-demand connectivity probe with explicit labels such as local network / internet reachability. Do not call latency a broadband speed test. A full throughput benchmark is deferred until separately requested.

### PICTURE card

Preserve the physically working brightness implementation exactly as the baseline contract:

- 10–100%;
- 100% means no overlay;
- below 100% uses the private non-exported overlay service;
- no focus interception;
- persisted value;
- immediate undo by returning to 100%.

Add read-only display information where Android exposes it: active resolution, refresh rate/mode and HDR capability/status without changing those settings in Phase 1.

## Phase 2: ADB TURBO

ADB TURBO is an explicit advanced layer, not a promise that `USB debugging = turbo`.

### Setup

The app presents a one-time setup page containing the exact package-specific commands needed for the capabilities we choose to test. The setup is performed from an external authorised ADB host. SHIELD TURBO never stores an ADB private key, never silently pairs itself and never bundles a generic shell bridge.

After setup, the app probes each intended elevated capability and marks it individually as AVAILABLE or NOT AVAILABLE. The badge is evidence-driven. Reboot/disabling debugging behaviour is tested on real Shield hardware before any persistence claim.

### First ADB targets

The first ADB pass should concentrate on useful, reversible capabilities and discovery:

- richer diagnostics that Android normally withholds;
- selected secure/global settings only where an explicit grant permits SHIELD TURBO itself to read/write them;
- app-ops/usage visibility where a one-time grant supports it;
- direct settings toggles only after proving the exact setting key and rollback behaviour on Shield firmware;
- identify whether any safe restart/sleep/reboot path can be exposed without retaining a shell daemon.

Do not assume an ADB grant gives the app shell UID. Operations that fundamentally require a live shell remain unavailable unless a separately reviewed architecture is approved later.

### ADB safety model

Every setting-changing ADB action must have:

- current value read before change;
- a single clear new value;
- an immediate restore/default action;
- persistence behaviour documented from real hardware;
- no boot-loop-risking, overclocking, thermal-limit, SELinux, filesystem or package-disable experiments in the normal UI.

Experimental Shield poking belongs behind a clearly labelled lab/debug surface and only after a known-good rollback path exists.

## State and permissions

Keep ordinary permissions minimal. Phase 1 should add permissions only when a specific feature requires them. Continue to avoid microphone, camera, BOOP relay credentials and unrelated background receivers.

ADB-granted capabilities are recorded as detected state, never assumed from a stored boolean. If a grant disappears, controls fall back to locked/read-only safely.

## UI behaviour

- D-pad focus must always be visible.
- No horizontal control may trap Left/Right focus; brightness keeps its proven behaviour.
- Back always returns one level before leaving the app.
- Destructive or setting-changing actions require a clear confirmation unless they are trivially reversible from the same control.
- Plain English labels, no `developer-speak` on the main screen.
- STANDARD and ADB TURBO status should be visible, but ROOT is never inferred from the presence of `su`.

## Testing

### Automated

Preserve all current brightness regression tests. Add unit tests for capability detection, intent/fallback routing, app filtering, state persistence and any setting-write restore logic. Android lint remains blocking on errors. CI must build with the established signer and repeat installed-release D-pad smoke checks.

### Emulator

Smoke-test card navigation, app/system intent fallbacks, Back behaviour, brightness regression and safe behaviour when ADB capabilities are absent.

### Real Shield

Phase 1 acceptance requires the bedroom Shield to confirm remote navigation, brightness non-regression, real settings shortcuts, app picker/launch behaviour, network/device facts and no playback/focus interference.

Phase 2 acceptance is capability-by-capability. Record exact Shield firmware, exact ADB command/grant, detected result, action result, rollback result and reboot persistence before promoting an ADB control.

## Delivery sequence

1. Record the current bedroom-Shield brightness result as the physical baseline.
2. Implement the STANDARD shell/navigation and TURBO/APPS/SHIELD/NETWORK cards without changing brightness internals.
3. Build/sign/test and physically verify that candidate.
4. Add read-only PICTURE/display facts and any Phase-1 polish that does not require ADB.
5. Build/sign/test again.
6. Add the ADB TURBO setup/detection framework.
7. Probe one elevated capability at a time on Shield hardware, starting with richer diagnostics and reversible settings access.
8. Only expose controls that have a demonstrated rollback path.

## Non-goals for this design

- root requirement;
- overclocking or governor forcing;
- thermal-limit bypass;
- arbitrary third-party data/cache deletion;
- fake RAM boosting;
- persistent bundled ADB/private keys;
- automatic device deployment;
- merging SHIELD TURBO into unified BOOP.
