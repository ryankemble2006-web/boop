# BOOP durable project memory

Updated 2026-09-17.

The current consumer apps are split shells around the shared BOOP implementation: Wall uses `com.boop.alpha1`; Shield uses `com.boop.shieldoverlay`.

Shield v209 repaired native Close player / Close media ownership after the package split. Shield v210 introduced a weather panel in the existing 182dp idle Now Playing slot, but Ryan's first physical test found that no weather information loaded and the panel surface did not match Now Playing.

Shield v211 repairs that weather candidate without changing the approved layout. Open-Meteo remains the keyless weather source; the Shield shell now declares `android.permission.INTERNET`, allowing the existing repository to fetch it. The weather surface is locked to the same card chrome as Now Playing: RGB 16/16/16 fill, 14dp corner radius, and RGB 48/48/48 1dp stroke. Weather refreshes every 30 minutes, supports a bounded stale cache, is non-focusable, and disappears whenever eligible Now Playing media is present. The favourites row remains parked.

Latest signed Shield source: `3d1c62b965fbe02aee58e8d39dd6649721611fef`. Signed run `35228355008`; artifact `10500275652` (`BOOP-Shield-v211-Wall-v207-Signed`). Target APK `BOOP-Shield-v211.apk`, SHA-256 `cd059052f7dfe1b398b8d1aeba21f49d333d4e637b455e87776367578ea6874d`. Automated build, signer, package, inherited functional, split integration, packaged INTERNET-permission, native-library and frozen-art checks passed. Manual installation and visual/physical acceptance of v211 remain pending with Ryan. Wall remains on v207.

The earlier GitHub workflow-rule/context experiment was retired on 2026-09-17. The active branch no longer carries the root AGENTS, BOOP_START_HERE, BOOP_CONTEXT, BOOP_RULES or BUILD_ON_GITHUB files, and the current exact-file/source-preservation allowlists were removed. Functional regression coverage, package verification and signer/integrity checks remain.

Historical receipts under `docs/` remain history rather than active instructions.
