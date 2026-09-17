# BOOP durable project memory

Updated 2026-09-17.

The current consumer apps are split shells around the shared BOOP implementation: Wall uses `com.boop.alpha1`; Shield uses `com.boop.shieldoverlay`.

Shield v209 repaired native Close player / Close media ownership after the package split. Shield v210 adds a weather panel to the existing 182dp idle Now Playing slot. Open-Meteo is the current weather source; it requires no API key. Weather refreshes every 30 minutes, supports a bounded stale cache, is non-focusable, and disappears whenever eligible Now Playing media is present.

Latest signed Shield source: `a3618c613fd53e75577e8d9a608bb1b2b732e007`. Signed run `35226569308`; artifact `10499332037` (`BOOP-Shield-v210-Wall-v207-Signed`). Wall remains on v207.

The earlier GitHub workflow-rule/context experiment was retired on 2026-09-17. The active branch no longer carries the root AGENTS, BOOP_START_HERE, BOOP_CONTEXT, BOOP_RULES or BUILD_ON_GITHUB files, and the current exact-file/source-preservation allowlists were removed. Functional regression coverage, package verification and signer/integrity checks remain.

Historical receipts under `docs/` remain history rather than active instructions.
