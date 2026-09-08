# BOOP unified status

Updated 2026-09-08. Canonical branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged.

## Current green candidate

The latest signed unified candidate adds five-sample local custom wake-name training while preserving the clean Shield HOME implementation.

- Code: `aad1e20aae1bbd15423a7bf0f307d364039cf506`
- Version: 47 / `1.2.1-unified-wake-training`
- Workflow: `34216093167` SUCCESS
- Artifact: `BOOP-Unified`, ID `10051904537`
- APK SHA-256: `b99a83873a44a5dd3a4ac2fea8e32633db71a14ef38fac8bcc7b6cbf6970b6b2`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Artifact ZIP SHA-256: `12140acd259093ed4c1a48b6a3be169d46635b7e613f08f0afc589de00b1c104`

Fresh evidence: integration contracts passed; Launcher lint passed; Shield 58/58, Shield HOME 26/26 and unified 74/74 focused functional tests passed with zero failures/errors/skips; signed APK assembly, package/version, manifest HOME/internal launcher presence, permanent signer and APK archive integrity passed. The downloaded artifact and extracted APK re-hashed to the same GitHub receipts. Physical/device/visual/acoustic acceptance remains Ryan's job.

## Custom wake training

BOOP remains the permanent fallback wake name. A non-BOOP name can be selected verbally or in Voice Settings and is trained locally by saying it five times. Training reuses the existing `BoopWakeWordController` / single 16 kHz `AudioRecord` ownership, stores only a compact amplitude-normalised pronunciation profile, and does not retain raw training PCM. Changing the name clears its previous profile.

The trained matcher is additive to Sherpa rather than a replacement. Custom names now receive all 33 established natural wake forms, including the existing `hey`, `oi`, `ok`, `morning`, `wake up`, `listen`, `excuse me` and prefix/suffix variants. A matcher/profile failure must leave BOOP/Sherpa available.

CI proves deterministic training/profile/codec and routing contracts only. Real recognition quality, miss rate and false-wake rate are not accepted until Ryan trains and tests a name such as `Steve` on the Pixel 7 Pro.

## Shield HOME contract

Default: favourite apps only, plus Apps and launcher Settings. Play Next and app content rows are optional and OFF by default. Disabled optional rows do not instantiate/fetch providers. Ads, sponsored content, Shop and Discover have no implementation/restore path. Stock launcher remains installed as recovery. BOOP uses local focus/page animations only and does not change Android global animation scales.

Routing: `UnifiedEntryActivity` remains the only exported HOME/LAUNCHER entry. Shield HOME intent -> `com.boop.shieldhome.ShieldLauncherActivity`; ordinary Shield BOOP launch -> existing puppet.

## LOCKED Shield muscle-memory rule

**Remove the crap, preserve Shield behavior.** BOOP HOME must not intentionally replace Nvidia/system functionality.

Physical acceptance must preserve at least: double-tap Home -> Recent Apps/task switcher; normal Back behavior; volume/CEC; Nvidia/Android Settings; system remote shortcuts; app switching; system animations. Single Home should return to BOOP HOME when BOOP is selected as launcher.

If one of these breaks on hardware, repair that narrow break later. Do not answer a shortcut failure by globally intercepting remote keys, disabling the stock launcher, changing secure settings or reimplementing Shield OS behavior.

## Other protected state

Approved paired black-lidded eyes are in the unified materialized build path; preserve approved geometry/alpha, iris-only hue, blink timing/gates, headphones/puppetry and five-digit hands. Blink is user-confirmed working and is not a current defect. HA names/Home controls were physically accepted earlier and must stay intact. Shield scaling remains idempotent/non-cumulative. Assistant remote invocation/audio remains a separate unresolved physical acceptance boundary.

Protected historical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881` until Ryan explicitly promotes a newer physically accepted checkpoint. No screenshots/golden/aesthetic-source checks, emulator device acceptance, automatic installs/grants or signer/package changes.
