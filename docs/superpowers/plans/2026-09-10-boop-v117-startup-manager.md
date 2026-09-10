# BOOP v117 Startup Manager / Clean Start

Date: 2026-09-10
Owning branch: `boop-canonical-rebuild`
Turbo reference: `shield-turbo-v01@c001d591aecedc2ab065850b8241953f3563068f`

## Scope

Keep the user-approved v116 Home layout unchanged. Add one `Startup Manager` entry to Launcher Settings.

The page reuses Turbo's superseding Clean Start design: at most five eligible non-system launchable apps, nothing selected by default, one cleanup pass after boot, no heartbeat, foreground app skipped, and every force-stop verified. BOOP/system/NVIDIA/Google services plus the Netflix/Plex/Deezer warm path are excluded.

## Local authority

Clean Start needs ADB-shell authority to stop another package. Unified owns a new app-private loopback ADB identity and therefore requires its own one-time Shield approval. The old Turbo private key cannot be copied or assumed trusted.

Turbo's old `StartupLedger` remains authority for historic app-op/enable-state changes made by `com.boop.shieldturbo`. Unified does not recapture those states and does not claim to migrate their Undo. Restore any old Turbo startup blocks in Turbo before removing it.

## Deferred / untouched

v114 tablet recipe physical/provider testing, eye/blink/animation branches, EastEnders private shortcut/artwork, NVIDIA processor/headroom experiments, broad dependency/service disabling and recurring process killers remain untouched.

## Verification

Pure-Java policy checks cover protected packages, warm-media exclusions, five-target bound, force-stop command construction and stopped-state verification. CI must compile the integrated Android path, preserve package `com.boop.alpha1` and the permanent signer. Device approval and real boot behaviour remain physical tests.
