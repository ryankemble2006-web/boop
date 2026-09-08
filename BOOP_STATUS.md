# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate, untouched)
- Version: 4 / `0.4.0-home-replacement`
- Green build head: `2e5dcca8fd2a7c635b0c54faa4b10fb07880f6fd`
- Workflow: `34226492186` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10055945427`
- APK SHA-256: `64c9c92603a481ffbc5d66df0a5826269823f6e9dbe94889a8294fdaa1d29b4f`
- Artifact ZIP SHA-256: `303ad4f81c1e47558fcf052908a14c0c5392f661201a3097cbbe827e9f26c7ab`
- Permanent BOOP signer reused and verified
- 42 focused Shield HOME tests green

This remains a standalone Shield experiment pending later AIO merge only after explicit real-device acceptance.

## Physical evidence

- Android TV banner presentation physically liked; preserve.
- 0.2 grab movement physically failed.
- 0.3 parent-level input-routing repair physically succeeded: Ryan confirmed the grabbed favourite moved ("the booger moved :)").
- 0.3 Back semantics are implemented but not yet explicitly recorded as physically accepted.

## 0.4 HOME replacement

Normal-user setup now requires **no ADB**. On first launch, if BOOP is not already HOME, BOOP requests Android's own HOME role once. Manual recovery/setup controls live under `Home rows`:

- `Make BOOP my Home` -> Android HOME-role/chooser flow.
- `Retire Android TV Home` -> dynamically finds the competing system HOME package and opens its Android App Info so the user can press OS-provided **Disable** if available.
- `Restore Shield Home` -> finds the stock HOME even when disabled; opens App Info for **Enable** when disabled, otherwise opens the HOME chooser.

BOOP does not programmatically disable another system package and does not require root, Shizuku, hidden APIs or ADB for the consumer flow. The stock launcher stays installed as emergency recovery. Its package name is not hard-coded.

Default HOME remains favourites-first. Banners, working grab/reorder, Back handling, real top-right Settings, optional-row defaults and the no-ad/Shop/Discover contract are preserved.

LOCKED: **remove the crap, preserve Shield behavior.** Double-tap Home -> Recent Apps/task switcher, volume/CEC, system Settings, system shortcuts, app switching and animations remain physical acceptance requirements.

0.4 is CI/signer green, **physical HOME-role/retire/restore acceptance pending Ryan on a real Shield**.
