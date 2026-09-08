# BOOP unified status

Updated 2026-09-08. Canonical branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged.

## Current green candidate

Shield clean HOME implementation is complete in the unified lineage and the latest full non-visual build is green.

- Code: `e2c938ed0a035913b6fb8499aad1c3b89eb3aaac`
- Version: 46 / `1.2.0-unified-shield-home`
- Workflow: `34215725283` SUCCESS
- Artifact: `BOOP-Unified`, ID `10051749294`
- APK SHA-256: `94f0046a93797606176fdd247c328aa189adb161cb6468346d26f69b8f71cb54`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Artifact ZIP SHA-256: `e3fb91691c0f828edba8469e15677814fb48ce1fc2955693eb793343356e5cd4`

Fresh evidence: integration contracts passed; Launcher lint passed; Shield 58/58, Shield HOME 26/26 and unified 74/74 focused functional tests passed with zero failures/errors/skips; signed APK assembly, package/version, manifest HOME/internal launcher presence, permanent signer and ZIP integrity passed. Physical/device/visual acceptance is pending Ryan.

## Shield HOME contract

Default: favourite apps only, plus Apps and launcher Settings. Play Next and app content rows are optional and OFF by default. Disabled optional rows do not instantiate/fetch providers. Ads, sponsored content, Shop and Discover have no implementation/restore path. Stock launcher remains installed as recovery. BOOP uses local focus/page animations only and does not change Android global animation scales.

Routing: `UnifiedEntryActivity` remains the only exported HOME/LAUNCHER entry. Shield HOME intent -> `com.boop.shieldhome.ShieldLauncherActivity`; ordinary Shield BOOP launch -> existing puppet.

## LOCKED Shield muscle-memory rule

**Remove the crap, preserve Shield behavior.** BOOP HOME must not intentionally replace Nvidia/system functionality.

Physical acceptance must preserve at least: double-tap Home -> Recent Apps/task switcher; normal Back behavior; volume/CEC; Nvidia/Android Settings; system remote shortcuts; app switching; system animations. Single Home should return to BOOP HOME when BOOP is selected as launcher.

If one of these breaks on hardware, repair that narrow break later. Do not answer a shortcut failure by globally intercepting remote keys, disabling the stock launcher, changing secure settings or reimplementing Shield OS behavior.

## Other protected state

Approved paired black-lidded eyes are now in the unified materialized build path; preserve approved geometry/alpha, iris-only hue, blink timing/gates, headphones/puppetry and five-digit hands. HA names/Home controls were physically accepted earlier and must stay intact. Shield scaling remains idempotent/non-cumulative. Assistant remote invocation/audio remains a separate physical acceptance boundary. Custom wake five-utterance enrolment is now materialized but still requires real-device acceptance.

Protected historical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881` until Ryan explicitly promotes a newer physically accepted checkpoint. No screenshots/golden/aesthetic-source checks, emulator device acceptance, automatic installs/grants or signer/package changes.
