# SHIELD TURBO durable decisions

Updated 2026-09-08. `SESSION_HANDOFF.md` owns exact receipts; `STATUS.md` is the concise view. Fresh physical evidence overrides stale pending-test notes.

## Identity and continuity

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`; not unified BOOP. Check live Turbo and `main` before publication, preserve concurrent work, never force-push, and verify remote HEAD afterward.

Use only established secret-backed signer `boop-dev`. Certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## CLEAN START physical decision

The real force-stop/read-back CLEAN START core is physically positive. Stale Recents/task-manager cards are history and may remain while target apps are stopped; apps reload only when deliberately focused/launched. Do not change the accepted stop/read-back core merely to solve presentation.

## Static notice lock

Required text remains `SHIELD TURBO · CLEAN START` / `Tidying startup apps`. Notice must be static, non-focusable and non-touchable. No spinner, pulse, fade, slide, countdown, moving dots, animation, repeated layout animation or focus effect. Presentation failure must fail open into cleanup.

Physical history:
- v0.5.1 flashed only at the end;
- v0.5.2 invisible;
- v0.5.3 invisible and slow, almost eight seconds;
- v0.5.4 invisible but fast navigation restored;
- v0.5.5 invisible, `DRAWN` ~54ms exposed false-positive draw signal;
- v0.5.6 invisible despite `FRAME_COMMITTED` ~103ms; navigation quick; Home micro-refresh;
- v0.5.7 uses a brightness-style transparent full-screen host; physical result pending.

`FRAME_COMMITTED` is not physical visual acceptance. Do not add another timing hack based on draw/commit callbacks.

## v0.5.7 architecture decision

The next evidence-driven variable was surface geometry because `BrightnessService` is physically visible on Ryan's Shield with a full-screen application-overlay surface.

v0.5.7 therefore uses:
- transparent `FrameLayout` host;
- `MATCH_PARENT x MATCH_PARENT` overlay window;
- `TYPE_APPLICATION_OVERLAY`;
- `FLAG_NOT_FOCUSABLE`, `FLAG_NOT_TOUCHABLE`, `FLAG_LAYOUT_IN_SCREEN`, `FLAG_LAYOUT_NO_LIMITS`, `FLAG_HARDWARE_ACCELERATED`;
- unchanged small static card as a top-centre child;
- host-level attach-gated frame-commit diagnostics;
- unchanged 500ms maximum fail-open.

This change is presentation geometry only. CLEAN START targets, force-stop/read-back, current-app skip, trusted loopback ADB and 30/60/120-second max-three scheduler remain unchanged.

## Safety retained

AUTO CLEAN START remains opt-in and bounded. Boot cleanup uses only already-trusted loopback ADB, never a new RSA approval. No periodic/resident cleaner. Only eligible non-system user apps are targets; preserve BOOP/Android/NVIDIA/Google-core/system exclusions. HARD BLOCK remains separate. Preserve old StartupLedger undo records.

No `pm clear`, uninstall, cache/login/data deletion, broad kill-all, rooting, device-owner/bootloader work, third-party re-signing, overclocking or fake RAM score. Keep ADB key private in `noBackupFilesDir`.

Keep physically proven brightness behavior, APPS direct launch, labels, Cancel/Back and accepted controls unchanged. Display & Sound and Accessibility remain parked.

## Testing boundary and latest receipt

Ryan owns all real-device visuals and remote acceptance. Never add GitHub screenshots, UI hierarchy dumps, golden/image/layout/focus/appearance/motion judgment. Allowed gates are functional/API/protocol/security tests, compilation, lint, signer/package/archive integrity and basic nonvisual crash smoke.

Latest candidate: **v0.5.7/code 14**, source `5f3b18fca5921e2a47f132c1a149d03d59d7f091`, release run `34236299335`, job `102094767133`, success. 68 JVM tests passed; source/API/security contracts passed; lint 0 errors/24 warnings. Signed artifact `10060096819`, ZIP SHA-256 `6c2098ff9ac4135ad105020567c098dd66deb4223c73da2ee6db51135fa6779e`; tests artifact `10060152208`, SHA-256 `902228e7d291dd16efdba06a6936efe8db674e67bc92567a98690361ee7437f2`. APK `Shield-Turbo-v0.5.7.apk`, 2330782 bytes, SHA-256 `289db308bd387d5cf2249e44dd99a92a00cfae5e73b0e3ee54dca07e154321c0`. Permanent signer as above. Nonvisual install/cold/warm launch/no-fatal smoke passed. No visual tests ran.

v0.5.7 TDD: RED `87a7fd89fc58f83e8cee7072701dba66cbd81cdc` (run `34234888441`, job `102089929246`); production host `a3b3589700741b08358261a0aab352da29016307`; corrected structural guard/full GREEN `607ac8f1177bfdfd98a1ba2309d34ccf63b66447` (run `34235514995`, job `102092069734`); final atomic release source `5f3b18fca5921e2a47f132c1a149d03d59d7f091`.

An accidental empty root scratch file created during release prep was immediately deleted in normal history; the tree returned exactly to the green tree before the atomic version stamp. It is not a checkpoint and no app content survived it.

Historical v0.5.6 and earlier receipts remain in Git; never repoint historical checkpoints. Temporary branch `shield-turbo-v01-stamp-temp` is noncanonical and may be safely deleted later if normal branch deletion tooling becomes available.

## Next physical evidence required

Install v0.5.7 and reboot. Record sign visibility, navigation responsiveness, exact startup diagnostic, and target stopped state if convenient. If the sign is visibly correct and motionless, freeze the notice architecture and record physical acceptance.
