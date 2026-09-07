# Shield handoff — 2026-09-07

## Settings remote-scroll fix — signed candidate

Owner: Ryan's Shield task. Branch: `boop-shield-media-puppetry`.
Application change commit: `7cd636086ea7991659550cf8d5b87da321eab5f3`.
Build/workflow commit: `6b87b682e22bbeac09635d7f340b3c328a700507`.
Signed GitHub Actions run: `34083898275`.
Artifact: `BOOP-Shield-Overlay-POC-debug` / artifact `10004571953`.
Extracted APK SHA-256: `0eac8ea8bc52925998f0ac862a5acb68a5e1314c2f383cf90ef13f94e86852ef`.
Package remains `com.boop.shieldoverlay`; existing permanent BOOP development signer verified by CI.

Ryan reported that the Shield Settings/My Home area showed only one item and could
not be scrolled with the remote. The scoped fix is in `TvSettingsView`: the existing
ScrollView is now remote-focusable and D-pad Up/Down explicitly prefers the next
focusable settings card; when no further card can take focus but more content is
off-screen, the page itself smooth-scrolls. Left still returns to the Settings rail.
Settings contents, Deezer behavior, Home Assistant, overlay runtime, permissions,
artwork and puppet placement are unchanged.

The authoritative Shield branch previously did not contain a literal `My Home`
settings label, so this fix targets the underlying Settings scrolling/navigation
container rather than inventing or removing inventory rows. Record Ryan's physical
Shield result next; CI success is not physical acceptance.

Verification for run `34083898275`: source regression suite PASS, Shield unit tests
PASS, APK build PASS, package/permission inspection PASS, stable signer continuity
PASS, artifact upload PASS. No emulator or physical Shield interaction was performed
by this chat.

The build workflow now includes the authoritative `boop-shield-media-puppetry`
branch in its existing push branch list so future scoped Shield changes on the
owning branch use the same signer/test pipeline. No signing secrets were changed.

## Preserve

- checkpoint-shield-home-f8e8135
- checkpoint-shield-routines-3fa18c6
- H1 physically accepted lower placement from application commit `4fe28a4`
- Existing Deezer observer/puppet behavior and Android access state
- No Google Assistant fallback and no cancelled setup-heavy Deezer Cast bridge

## Next safe step

Install the signed settings-scroll candidate over the existing Shield app and test
D-pad Up/Down through all Settings rows. If a specific `My Home` inventory page is
still clipped after this container fix, capture that screen and continue from this
branch without changing the protected Home/Routines checkpoints.
