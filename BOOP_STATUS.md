# BOOP status: colour accepted; v160 GitHub speed verification passed

Updated 2026-09-13. Latest scope is GitHub work only. No laptop/device execution or deployment occurred in this continuation.

Wall -> Shield eye colour is physically accepted by Ryan. Prior captures verified visible delivery in both directions with Pixel 7. Preserve the last recorded user hue2 and enabled sharing; do not restore old test values. Physical devices were last verified on v159. Physical Pixel 10 remains excluded.

Engineering commit `1d8bf3d39a0858aa0c4f2b435fc92b1b16ef486a` adds a mandatory timing/materialized-source gate before APK signing. The app itself remains the unchanged published v160 implementation. Tests were expanded without changing artwork, motion, hue, settings permissions or signing.

Test-first timing run `34768979364` at `ca6cd34af50c42dfb5606053b5d27fde7128eaea`: expected missing-gate failure, four other tests passed. Five-line workflow repair then passed in full build `34769075927`. Appearance run `34769075918` passed. Permanent-signed artifact `10321686042` / `BOOP-Unified`, built from `1d8bf3d39a0858aa0c4f2b435fc92b1b16ef486a`, is available.

APK SHA256 `a584381d10ebb220ab686acec22f7668384e240f39ade052e4177583aa4adc7b` matches the existing v160 candidate exactly. Build receipts report 235 Unified and 68 Shield functional tests with zero failures/errors/skips, plus timing and preservation gates.

Speed remains pending runtime/visual acceptance and physical deployment. Next coverage is the existing emulator-first speed plan when permitted. Raw embedded Lab scale-zero handling needs a materialized-path check; the existing scale-independence contract is Wall-only. Do not report all-surface runtime success from green CI.

Details: `SESSION_HANDOFF.md` and `docs/handoffs/2026-09-13-speed-github-verification.md`. Owner remains `boop-unified-eye-sync-safe-v159`. No local synchronization, no new app repair and no bypass of the prior laptop block are claimed.
