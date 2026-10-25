# BOOP Shield v405 Release Receipt

- **Snapshot**: `shield-v405`
- **Version Code**: `405`
- **Version Name**: `1.2.405-shield`
- **Summary**: Direct LAN pairing, zero QR/browser dependency, and Deezer Flow transport/lifecycle cleanup.
  - **Zero-Nav Direct LAN Pairing**: Completely eliminated the phone camera QR code scan and mobile Chrome OAuth redirect loop (`ERR_TOO_MANY_RETRIES`). Added local LAN TCP pairing server on port `42123` with Android `NsdManager` mDNS service registration (`_boop-pair._tcp.`), broadcast receiver `com.boop.PAIR_HA` (`--es url <url> --es token <token>`), and direct Long-Lived Access Token verification (`GET /api/` Bearer check) without requiring IndieAuth browser workflows.
  - **Direct Token Pairing UI**: Updated `TvPairingView` to show "Ready to Pair / Auto-Discovery Active on LAN", replaced QR code display with LAN status, and added direct token entry dialog for manual token fallback.
  - **Deezer Flow Transport Cleanup**: In `BackgroundDeezerFlow`, updated `found.getTransportControls().playFromUri` to pass an empty `Bundle` instead of `null` extras, matching `DeezerFlowController` so cold Deezer doesn't drop playback intents.
  - **Deezer Flow Lifecycle Reset**: In `BackgroundDeezerFlow.releaseNow()`, canceled all pending callbacks (`handler.removeCallbacksAndMessages(null)`), reset `pending = false`, `mode = Mode.IDLE`, and cleared `flowRequested` so force-stops or session ends never leave background flow stuck in pending state.
