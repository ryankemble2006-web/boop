# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Package: `com.boop.shieldhome`
- Unified/AIO `com.boop.alpha1`: separate and untouched
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Physically accepted Apps drawer: 0.9 floating square icons
- Physically-good HOME geometry/chrome: 0.9.4
- 0.9.5 stronger focus pop remains a physical visual candidate
- Current functional candidate: version 16 / `0.10.1-media-access-route`
- Build head: `df4445e6a0c6488355002d5ed99ebfb88ca4e9c1`
- Workflow: `34278312090` SUCCESS
- Artifact ID: `10076663643`
- APK SHA-256: `1f8f9f82871ca80f6e5496a3047068171042edfdc3502f829faa3ebc2fe31ff5`
- Artifact ZIP SHA-256: `7c54e96b4d9ca5de4e6690f68cccff87b4ee70499631467aa52f02986bb8d9a3`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- CI/signer/package green; physical verification of the corrected Settings destination is pending Ryan

## 0.10.1 scoped fix

Physical 0.10.0 report: **Media access: OFF opened general Shield Settings instead of Notification Access.**

Root cause: API 30+ attempted the per-listener `DETAIL` Settings action before Android TV's generic Notification Listener settings action. Shield accepted the detail intent into the wrong Settings surface, preventing the TV route from being tried.

Fix:

- modern route order is now `GENERIC, DETAIL`;
- older Android remains `GENERIC` only;
- no UI/layout/artwork/animation source changed;
- no HOME override, launcher visuals, permissions or media-session behavior changed.

TDD:

- RED `65385a8078d8a734b7556514091077e4eeaed566`: 79 tests, exactly 1 failure, the new generic-first route assertion.
- GREEN production fix `5c549474c9f87400a5a55a94eecb7a6288ff27dd`.
- Final run `34278312090`: all 79 focused tests passed, signed build passed, exact code16/version/package passed, protected HOME/Accessibility and Now Playing manifest/resource checks passed, permanent signer passed, APK archive integrity passed and artifact uploaded.

No GitHub screenshot/golden/appearance/layout/animation acceptance was run. Visual acceptance remains Ryan's real-device call.

## Locked behavior

Preserve:

- single Home -> BOOP;
- double Home -> native Recent Apps;
- BOOP Home Override across reboot;
- stock Android TV Home installed/enabled as recovery/trigger;
- real wide HOME banners and grab/reorder;
- accepted floating-square Apps drawer;
- no HOME black focus plate or normal favourite stars;
- artwork-only HOME focus scaling;
- single Back -> favourite item 1;
- long Back/top-right Settings -> real Shield Settings;
- volume/CEC/system shortcuts and animations.

## Next gate

Install/update 0.10.1 and check only the reported fault first: Launcher Settings -> **Media access: OFF** should land on Android TV Notification Access / Notification Listener special access, not general Shield Settings. Enable BOOP there and confirm the launcher reports `Media access: ON` on return.

Continue the wider 0.10 Now Playing physical tests after that. Do not merge into unified until Ryan explicitly approves the standalone behavior.
