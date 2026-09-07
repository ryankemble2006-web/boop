# BOOP Wall Native Chat v34: isolated setup candidate

Updated 2026-09-07. Owning branch: boop-relay-reviewed-v34.
**Code/build/CI green; Worker undeployed; real native chat and physical acceptance unverified.**

- App/build commit `453bda9ec5bcd12f44fe50d63c7db5ce7a718295`; com.boop.alpha1, versionCode 34 /0.4.14-wall-native-chat.
- Full successful run `34084002048`, job `101624466469`, APK artifact `10004727824`.
- Existing permanent signer verified; stable-signed debug variant.
- APK SHA-256 `1bb448d6f458cf4b527a69f7137f65ef3dd5a1a1da7dfab958b5cfe8efaeba97`; 139485306 bytes.
- Configuration receipt: setup-required. Installing this APK alone does not enable
  live native conversation. Use the matching Worker and private configuration.

PASS: 177 local source/Python/JVM and 13 mock Worker tests; CI bridge/Android units,
materialization, assembly, signature/package/archive, real wake microphone,
three-mode menu/persistence/revert/cancellation, natural portrait+landscape blink,
notice formatting, original sleep deadline, background cancellation, pairing return.
The last UI run uses a 720x1560 @280 dpi emulator (same dp/aspect as 1440x3120 @560),
not a physical Pixel or a full-panel performance claim. Earlier exact failed
captures and the unknown first wake failure remain recorded in SESSION_HANDOFF.md.

Local-first routing is unchanged: only NO_MATCH may reach conversation. OpenCode
and browser Free Chat stay reversible. Native replies/errors reuse existing voice.
No provider key in Android; public CI rejects configured bearer-token APKs.
Worker deployment/private billing/secrets, real provider success and physical phone
verification are absent. No install, permission grant or secret change occurred.

The concurrent boop-wall-free-chat-wip implementation was preserved, not merged.
Do not interchange the two v34 variants by version label. Main 85d08eb maps both.
Protect accepted Wall 595e1da and Home/Routines checkpoints; no tag was promoted.
Exact APK fingerprints/provenance: docs/BOOP-WALL-V34-BUILD-RECEIPT.md.
Method/scoped review: docs/BOOP-RELAY-V34-REVIEW.md. Prior v33 history is archived.
