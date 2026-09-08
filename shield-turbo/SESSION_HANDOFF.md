# SHIELD TURBO handoff

Updated 2026-09-08. Owner branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.
Current signed candidate: **v0.3.0 / versionCode 4**, not yet physically accepted.

## Latest physical feedback and explicit request

Ryan reports v0.2.1 Accessibility says no app installed; Display & Sound first bounced Home, and subsequent clicks did nothing. Its guessed AOSP component is a failed hardware route, not a fix. Earlier v0.2.0 general Android Settings fallback was also rejected. Developer Options was physically confirmed working; brightness was confirmed on the bedroom Shield; corrected STANDARD maintenance controls became selectable. None of these reports prove the new ADB transport or power tools work on the physical Shield.

Ryan explicitly requested one-button ADB setup, useful advanced tools, web research, no GitHub visual confirmation, and a signed APK. This authorises the scoped self-ADB design below, not root, bulk process killing, other-app data deletion, unrelated BOOP changes or automatic physical deployment.

## Exact delivered v0.3.0 receipt

- Built source: `ac5f79cd9138553a27df776e6b47e685f2cbf0ff`.
- Build workflow run: `34196792381`, job `101966198551`, final conclusion **success**.
- Signed artifact: `SHIELD-TURBO`, ID `10044276954`, ZIP size `731773` bytes.
- Artifact ZIP SHA-256: `a279948eb1cf626955814d215c99642ba8cb655d5c7a230834d1b04aa8c5e6ce`.
- APK inside artifact: `shield-turbo/app/build/outputs/apk/release/app-release.apk`.
- Delivered filename: `Shield-Turbo-v0.3.0.apk`, size `2245278` bytes.
- APK SHA-256: `fff6b791235b05938dcb34a886c99a97d4563132cd46db79493dd422b0f25846`.
- Established signer certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Test artifact: `10044314369`, ZIP SHA-256 `dc416161f41e347977888d3c121654a63e75e636cca851064cc6385700ba99b3`.

Downloaded archive and extracted APK were verified against archive digest, inner checksum, built-source receipt, package/version/release metadata, signer receipt and ZIP/APK CRC integrity. The actual APK was supplied, not merely a renamed ZIP or an invented link. Candidate upload preceded the slower nonvisual smoke; the latter completed successfully before final handoff.

## Machine verification and review

43 JVM unit tests passed: zero failures, errors or skips. This includes the actual loopback fake-daemon authentication/command exchange, packet integrity/bounds/fragmentation, RSA token/public-key format, persisted identity, command status receipts, silent-peer timeout, package-injection exclusions, single-app restart policy and firmware routing policy. Nine source/security contracts passed. Android lint passed with **0 errors and 21 warnings**, not a warning-free claim. Signed release assembly, permanent signer/package/version/non-debuggable/archive checks and API 30 install/launch/relaunch/no-fatal smoke passed.

Initial transport frame test was locally RED on a stub before implementation. Expanded Java 17-targeted transport self-test was locally GREEN and then exercised in Android project's JVM CI. This is protocol/machine evidence, not a connection to Ryan's Shield. No screenshot, UI hierarchy, golden-image, layout or appearance judgement was run.

Published diff was reviewed: changes are confined to shield-turbo and its existing workflow. MainActivity's runtime diff is the Advanced handoff and its explanatory sleep/reboot footer. The picture/brightness methods and Brightness.kt/BrightnessService.kt have no diff. No other BOOP body, signing identity, workflow permissions or checkpoint tags changed.

## Implemented power tools

ADVANCED now opens private `power.PowerActivity`.

- **ENABLE ADB TURBO:** creates a per-install authentication identity in app-private `noBackupFilesDir`, connects only to loopback port 5555, handles standard Android RSA approval, verifies ADB shell UID, and explicitly grants only this package's existing WRITE_SECURE_SETTINGS permission. The actual permission is checked after the command. No commands need pasting into a laptop.
- **Network Debugging first-use boundary:** a normal app cannot enable a disabled ADB daemon or approve its own RSA trust prompt. On connection refusal, Turbo opens the known-working Developer Options route. Ryan enables Network Debugging and returns; Turbo retries setup. Android's Allow/Always allow prompt still belongs to Ryan. No bypass is claimed.
- **Diagnostics:** bounded on-demand shell CPU activity, memory, thermal-service and data-storage readings, displayed on-device. Missing firmware services remain visible as unavailable output, not fake readings/scores.
- **Restart a stuck app:** user chooses and confirms ONE installed non-system app. Package and resolved launch component are validated/rechecked before force-stop plus launch. BOOP, NVIDIA, Android/system services and Google core services are excluded. No app data is cleared or disabled.
- **Sleep / reboot:** explicit confirmation warns that playback/running work is interrupted. Sleep is keyevent 223. Reboot uses svc power reboot; a dropped connection during reboot is explicitly uncertain and requires looking at the TV, not asserted as a proven restart.
- **Animation 0.5x / off / normal:** only the three Android animation-scale keys. Original values are saved before changing anything; writes are read back; partial failure attempts rollback. UNDO restores saved originals, unlike the older RESTORE 1x normal preset.

The app adds INTERNET permission only for its local client socket. There is no app-owned listening server, off-device address input, Internet endpoint, root command, startup receiver, extra service or permanent ADB session. Operations are bounded/cancellable and close the connection. Cancellation is not a promise to undo a command already sent.

Network Debugging itself exposes the Shield's device-managed listener on the network. Use a trusted LAN and disable it in Developer Options when finished. A retained secure-settings grant and current shell connectivity are different capabilities; every shell operation verifies its own connection. Never imply a settings grant alone grants reboot/process control.

## Display & Sound and Accessibility

The failed hardcoded activity and generic Accessibility route are removed from automatic routing. `FirmwarePages` inspects the installed system settings packages and their declared activity handlers, restricted to enabled/exported system activities. Scoped manifest queries permit discovery; QUERY_ALL_PACKAGES was not added.

First use lists actual firmware entries, prioritises combined-page candidates and marks related subpages. Launches use the firmware-declared action where available, not a guessed ACTION_MAIN. On return, Ryan confirms whether that was the wanted native page. Only a confirmed installed route is saved for subsequent direct shortcuts. General Settings is an explicitly separate manual option, never called a successful Display & Sound fix. Route details are available locally for troubleshooting.

**The exact NVIDIA destinations are still unresolved until Ryan tests this discovery flow.** This build provides discovery/confirmation instead of asserting another unverified class-name fix. If there is no matching public activity, record that limitation and obtain minimal private firmware evidence; do not add random classes, publish device dumps, or quietly substitute general Settings.

AppCatalog also stops filtering launcher entries with MATCH_DEFAULT_ONLY, which incorrectly excluded valid launchers without CATEGORY_DEFAULT. Normal APPS click remains direct launch; no package-name modal returns.

## Next physical test

Install this signed v0.3.0 over the existing package. ADVANCED -> ENABLE ADB TURBO, enable Network Debugging only if asked, return and approve the Android prompt. Verify reported connection/grant success, read diagnostics, test 0.5x and UNDO, then explicitly test sleep/reboot or selected-app restart when playback can be interrupted. Test native-page discovery and confirmation separately. Recheck brightness without changing its established service behavior.

Do not equate CI success with successful Shield firmware navigation, visual acceptance, actual ADB authorisation, thermal behavior or hardware power control. Record exactly which operation Ryan reports working.

## Historical receipts / rollback evidence

- v0.2.1: source `0e00f7c44758aa4192e7664b61d477b11494942d`, run `34192942800`, artifact `10042893415`, APK `0572aa2e66481703a91490078f6291350824bd266cbd44a834bfc4015cfada27`. Machine checks passed; native Display & Sound failed physically. Docs head was `4c881ea401fa4b481229c039c2239050bf910148`.
- v0.2.0: source `81f448c417e3b0b122df55edc6ea6886d1bde5b2`, run `34191343078`, artifact `10042388530`, APK `7e5a769e68c88cf44749f7887c35e64d41369c3b76fe11a35a9473b350582636`. Developer Options physically worked; general Settings was rejected as Display & Sound.
- Corrected pre-ADVANCED STANDARD: source `d277ebe713cdbe5298f6205ef34fa4d493ea2114`, run `34189880390`, artifact `10041897001`, APK `f86ed5b9aac5926d98c09d9fa69b83a8d41e0cd8992ecd7b5bcdebccdbc60cf1`. Maintenance selectability physically confirmed.
- Original bedroom brightness: source `192879ba87082b9daf5275c89a706bfd5f1106d2`, run `34129557124`, artifact `10021629767`, APK `3ad1a87f2d007a972d66aa6a3f1ee687596e3903e7038db2b252f5eaf9075a6d`. Brightness physically confirmed.
- Old pre-brightness `shield-turbo-v0.1.0` prerelease remains historical and must not be repointed. Do not promise an in-place downgrade to older versionCode APKs.

See `docs/superpowers/plans/2026-09-08-local-adb-power.md` for research, design and scope. Startup main was checked at `dd38cfc72eb5d00bc42121f88c633cb237805009`. No Windows checkout or physical device was modified/synchronised in this chat. App-specific handoff/status/memory are the applicable implementation context; shared BOOP ownership is unchanged. Documentation commits after the built source do not replace the APK receipt.
