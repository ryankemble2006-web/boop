# BOOP Launcher v1: signed standalone candidate

Updated 2026-09-12. Owner `boop-shield-launcher-standalone`; project `shield-launcher/`.
Package `com.boop.shieldlauncher`, version `1 / 1.0.0-launcher-tools`.
Signed source `72dde252f4f73487d9b99af52ea189777822a795`; Actions run `34684440925`
succeeded, artifact `10295068406` (BOOP-Launcher), 7,250,154-byte APK.
SHA256 `5293f65d53299bf49b45c10911693f1e3b97c6effd1a87a3734c2ee88037bb7b`.
Permanent signature, downloaded package/version/hash, actual APK dependency closure,
Android build and focused startup/defaults/close tests verified. No physical install
or acceptance on the other Shield. Failed integration work was not imported.
Read docs/verification/2026-09-12-shield-launcher-v1.md for the full receipt and limits.
Shared main product exception published at `af0db837bf9dde26d16c632fe75d8b5bca1f7fc0`.

# BOOP Launcher standalone handoff

Updated 2026-09-12. Owner: `boop-shield-launcher-standalone`.
Project `shield-launcher/`; generated dependency closure `work/shield-launcher-src/`.
Package `com.boop.shieldlauncher`, version 1 / `1.0.0-launcher-tools`.
User explicitly approved splitting the launcher and its advanced tools, excluding
failed uncommitted integration work. The subsequently published v146 integration
is still not an input. Unified and historical standalone installs are untouched.

## Frozen input and product boundary

Base `boop-shield-defaults@503cdb63d64716c9c1a568aadca97ba1d24680cd`, with signed
v145 source `8fbe84411ec211f3adfec4409e41f3d49b2607fa`. Includes corrected visible
Startup Manager Overview/defaults controls and accepted v135 startup foundations.
Retains Home/favourites/drawer/Now Playing/album browsing/audio handling, Startup
Manager, Clean Start, package controls and device-specific exact defaults Undo.
The existing lyrics branch, phone/Wall, HA dashboard, voice/wake/models, overlay
media puppet and Animation Lab are excluded. Home Now Playing puppet remains.
New label BOOP Launcher; new package does not replace com.boop.alpha1 or the
historical com.boop.shieldhome package. No new artwork; inherited launcher PNGs
are byte-checked and unchanged, with an XML app icon using the existing eyes.

## Extraction and review

83 Java files currently form the standalone dependency closure. The original
Unified sources remain unchanged; generation applies narrow standalone adapters.
Settings open the existing Shield firmware router and OS Home chooser, not HA or
Unified profiles. Close Player uses this app's previously authorized local ADB
bridge and exact session checks, never HA credentials. Native targets remain
Deezer/YouTube; Cast uses the existing session Stop and is never force-stopped.
Back, pause, session change and timeout cancel work; actual session disappearance
requires a fresh Android observation. The local ADB identity and Restore journal
are app-private and not backed up or imported from the source Shield.

## Verification to this point

Observed test-first RED before implementation for packaging and local close.
Five packaging/routing contracts, local-close adversarial Java tests, 15 existing
Startup Manager suites, 27 defaults coordinator cases, eight defaults safety
cases and profile/journal checks are the focused gate. Local unsigned Android
build and actual APK ZIP/DEX-boundary checks pass after the final settings-router
manifest repair. Five packaging tests, local-close scenarios, all 15 startup
suites and all defaults scenarios were freshly rerun and passed. Unsigned local
artifact is 7,675,699 bytes; it is not the permanent-signed delivery.
Self-review only; no independent reviewer or physical acceptance claimed.
Permanent signing is required through build-shield-launcher.yml and the existing
GitHub BOOP secrets. Never distribute the unsigned local compile artifact.
No physical device install, permission/role grant, package action, Apply/Undo or
media action has been performed. Real second-Shield setup/launch/remote/close/
reboot and visual acceptance remain pending.

## Final signed receipt and next safe step

Build source `72dde252f4f73487d9b99af52ea189777822a795`; run `34684440925`
completed successfully; artifact `10295068406`. The signed APK is 7,250,154 bytes,
SHA256 `5293f65d53299bf49b45c10911693f1e3b97c6effd1a87a3734c2ee88037bb7b`.
Independent downloaded apksigner/aapt/hash/ZIP/DEX checks passed. Permanent signer
unchanged. Full receipt: docs/verification/2026-09-12-shield-launcher-v1.md.
Shared main owns the explicit standalone exception at `af0db837` and preserves
the separately delivered v146 combined app. No changes to either installed app.
Next safe step is user-authorized installation/setup on the other Shield and
physical launcher/media/local-tools/recovery checks. Do not auto-grant access or
apply the default preset. Keep this source branch separate from Unified.
