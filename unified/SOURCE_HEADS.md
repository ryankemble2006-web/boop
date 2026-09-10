# BOOP unified source heads

Initial all-in-one merge inputs, fetched live on 2026-09-07:

- Wall: `boop-wall-native-chat-eye-hue@a28364f98fba1b3a5dbab7e66075c0fb166e08e3` (v40 landscape eye-match lineage)
- Launcher: `boop-launcher-alpha2@953ad6d5fe48df104a1f74bdcf4b448f5a6d04f2` (0.3.7 widget-picker candidate; physically reported better)
- Shield puppet: `boop-shield-fullscreen-deezer-wip@1a487a4aaecd098d6854905ee64d60253ad6b4b7` (v3 friendly Deezer access/full-screen puppet lineage)

The unified app keeps `com.boop.alpha1` and the existing permanent BOOP signer so current Wall installs have the cleanest upgrade path. Launcher and Shield packages remain preserved on their historical branches for rollback/reference; the unified APK contains their latest source as internal modules.

## Scoped canonical rebuild, 2026-09-10

Ryan selected overhaul items 1, 3, 6, 7, 8 and 10. The dedicated candidate branch is `boop-canonical-rebuild`, based on canonical `99474d141e7affad17cdbe854e94dd3986076980`.

Shield Home/Now Playing source and resource input: `boop-shield-clean-launcher@9888fbef444d1e0647fdd8ae90acb7197acfb813`. Its metadata, artwork, browsing and controls are reused as an internal library. The standalone package/branch remains untouched. Existing renderers are temporary; eyes/blink and Turbo changes are excluded and owned by other work. No automatic default-HOME, accessibility or notification-access grants are introduced.
