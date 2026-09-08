# SHIELD TURBO handoff

Updated 2026-09-08. Owner branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.
Current signed candidate: **v0.4.0 / versionCode 5**, machine-verified; Startup Manager physical acceptance pending.

## Latest physical feedback and user goal

Ryan reports four Kodi forks start themselves after Shield boot and later need force-closing. He approved a Startup Manager that prevents selected user apps from waking in the background while preserving manual launch, with an explicit stronger HARD BLOCK mode and exact rollback. He also reconfirmed that GitHub must not perform visual acceptance; Ryan owns real-Shield visual/remote testing.

Earlier physical evidence remains valid: bedroom brightness works, corrected STANDARD maintenance controls became selectable, and Developer Options opens correctly. Display & Sound and Accessibility remain unresolved firmware-route issues and were deliberately sidestepped for this pass.

## Exact signed v0.4.0 receipt

- Built source: `1358925716cf2c834171b767dd94f08d0c49e013`.
- Workflow: `Build SHIELD TURBO`, run `34201159209`, job `101980019981`, conclusion **success**.
- JVM tests: **51 passed**, 0 failures/errors/skips.
- Source/security contracts: **10 passed**.
- Android lint: **0 errors, 22 warnings**.
- Signed artifact: `SHIELD-TURBO`, ID `10045926945`, ZIP size `744061` bytes.
- Artifact ZIP SHA-256: `239a58277ed5eed513da6c23a334a11ece67271470ec004b5bb668c834ef8380`.
- Test artifact: `10045969922`, ZIP size `98932` bytes, SHA-256 `faf31d577df7708fe5f3871b8a044c4dbb8d2c7da6b86a98b0201503320f0e4b`.
- Delivered APK: `Shield-Turbo-v0.4.0.apk`, `2281902` bytes.
- APK SHA-256: `cf12ccdfec929424ad89f6f5302c86f7b1331ef809ceef336fc0bda5d344657a`.
- Established signer certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Package/version/non-debuggable/Leanback/archive checks: passed.
- Nonvisual emulator smoke: install, cold launch, process check, Back, warm relaunch and no package fatal exception passed.
- Visual checks: **none**. No screenshot, UI hierarchy, golden/image/layout or appearance assertions were used.

## Startup Manager implementation

ADVANCED now includes **STARTUP MANAGER**.

### BLOCK STARTUP / KEEP LAUNCHABLE

This is the normal/default recommendation for Ryan's Kodi forks. For one selected non-system app, Turbo first captures the current Android background app-op state and package enabled state, then applies:

- `RUN_IN_BACKGROUND -> ignore`
- `RUN_ANY_IN_BACKGROUND -> ignore`

The package remains enabled so its normal launcher icon can still be used manually. Turbo reads both app-op values back after the write. If the Shield rejects the change, Turbo attempts to restore the saved original state rather than silently claiming success.

### HARD BLOCK / DISABLE APP

This is deliberately separate and requires confirmation. It disables only the selected package for the current Android user with `pm disable-user`. It does not clear data, logins, files or caches. A hard-blocked app will not launch until restored.

### Exact rollback ledger

Before the first Turbo mutation for a package, `StartupLedger` records the original background app-op modes and enabled state in private app preferences. **First original wins**: later Turbo changes do not overwrite the rollback point.

Per-app UNDO restores the exact recorded values and verifies read-back before removing the ledger entry. **UNDO ALL TURBO STARTUP CHANGES** restores each recorded app independently; any app whose restore cannot be verified keeps its ledger entry so the failure stays visible.

Turbo adds no boot receiver or startup background service for this feature. The Android app-op/package enabled states are persistent system state, so Turbo does not need to become another app that wakes at boot merely to reapply the policy.

## Safety boundaries retained

Startup Manager lists safe user apps, including ledger-managed apps that may currently be disabled. Existing system/NVIDIA/Android/Google-core/BOOP exclusions remain. No QUERY_ALL_PACKAGES permission was added.

There is no bulk RAM cleaner, speed score, `pm clear`, uninstall, cache purge, app-data clearing, root, bootloader work, CPU/GPU clock/governor change or automatic hard-block action. The goal is reducing unwanted background/startup activity, not maximizing a free-RAM number.

The existing one-button local ADB flow, diagnostics, selected-app restart, sleep/reboot and animation controls remain. Brightness implementation is unchanged from the physically accepted bedroom behavior.

## TDD / review evidence

- Startup policy RED: source `eea152e4bf2e7770191f1792746a450c02d82176`, run `34199708521`, failed because `StartupPolicy` did not exist while existing safety contracts remained green.
- Undo-ledger RED: source `f698552ab0503bd7cbd33b94dc66f9e0f7ade56b`, run `34200071110`, failed because ledger classes did not exist.
- Startup-manager contract RED: source `0b9f177318da99a4abd05cbc61fddb2eeea8cd6f`, run `34200548808`; unit tests passed, then the new safety contract failed because the Startup Manager activity/wiring did not yet exist.
- Final v0.4.0 run `34201159209` is fully green at the machine level described above.

The published runtime changes are confined to SHIELD TURBO and its dedicated workflow. No other BOOP body, signer, checkpoint tag or main-branch ownership contract changed.

## First physical Startup Manager test

Use **one Kodi fork first**:

1. ADVANCED -> ENABLE ADB TURBO if access is not already authorised.
2. ADVANCED -> STARTUP MANAGER.
3. Select one Kodi fork -> **BLOCK STARTUP / KEEP LAUNCHABLE**.
4. Reboot the Shield normally.
5. Confirm that fork does not wake itself after boot.
6. Launch the same fork manually from its normal icon and confirm it still works.
7. Only after that works, repeat for the other forks.

HARD BLOCK is not required for this initial test. If BLOCK STARTUP cannot read the original Shield state or Android reports an unsupported app-op mode, Turbo should fail closed before mutation rather than guess. Record the exact on-device result.

## Historical receipts / rollback

- v0.3.0 pre-Startup-Manager: source `ac5f79cd9138553a27df776e6b47e685f2cbf0ff`, run `34196792381`, artifact `10044276954`, APK `fff6b791235b05938dcb34a886c99a97d4563132cd46db79493dd422b0f25846`.
- v0.2.1: source `0e00f7c44758aa4192e7664b61d477b11494942d`, run `34192942800`, artifact `10042893415`, APK `0572aa2e66481703a91490078f6291350824bd266cbd44a834bfc4015cfada27`; native Display & Sound failed physically.
- v0.2.0: source `81f448c417e3b0b122df55edc6ea6886d1bde5b2`, run `34191343078`, artifact `10042388530`, APK `7e5a769e68c88cf44749f7887c35e64d41369c3b76fe11a35a9473b350582636`; Developer Options physically worked, Display & Sound fallback rejected.
- Corrected STANDARD: source `d277ebe713cdbe5298f6205ef34fa4d493ea2114`, run `34189880390`, artifact `10041897001`, APK `f86ed5b9aac5926d98c09d9fa69b83a8d41e0cd8992ecd7b5bcdebccdbc60cf1`; maintenance selectability physically confirmed.
- Original bedroom brightness: source `192879ba87082b9daf5275c89a706bfd5f1106d2`, run `34129557124`, artifact `10021629767`, APK `3ad1a87f2d007a972d66aa6a3f1ee687596e3903e7038db2b252f5eaf9075a6d`; brightness physically confirmed.

Use only the established secret-backed `boop-dev` signer. Documentation commits after built source `1358925716cf2c834171b767dd94f08d0c49e013` do not identify a different APK. Distinguish branch documentation HEAD from the exact built source.