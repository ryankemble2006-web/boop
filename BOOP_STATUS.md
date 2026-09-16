# BOOP status: v207 Shield assistant reassigned; real remote test pending

Updated 2026-09-16. Owner: `boop-wall-shield-split-v207`.

## Latest result: microphone-button recovery

Runtime configuration repair only. Before repair, v207 already had `use_boop` selected and a valid DEFAULT ASSIST activity, but Android's ASSISTANT role belonged to Google Katniss. Reassigned the role to the split Shield package. Live readback: `com.boop.shieldoverlay/com.boop.alpha1.BoopAssistantActivity`; voice-interaction setting empty; original Katniss recognizer unchanged. No system restart. Injected KEYCODE_ASSIST reached BOOP MainActivity/face instead of Google. Ryan's real remote, microphone capture and spoken-command acceptance remain pending.

The installed APK is unchanged: version 207 / `1.2.207-shield`, SHA-256 `d4cff18acc85e7360601de042c37281ae6f262018b428525834ed70188574a0f`. No new build/install, app source edit, voice change, signing change or reset was necessary. No code tests/builds were rerun for this device-setting repair and documentation-only publication. Pixel phones and local source worktrees were not changed.

Microphone permission remained denied immediately after role reassignment. A permission activity was observed during the injected-button check; later readback showed granted with USER_SET. This session issued no permission grant or dialog-selection input. Preserve the observed current state. Home already belonged to BOOP Shield before repair and was not changed.

Deezer attention cue: launch verified, Flow playback NOT verified. Recents/YouTube foreground changes occurred during navigation, so further input stopped. Detailed receipt: `docs/handoffs/2026-09-16-v207-shield-mic-role-recovery.md`.

## Split-delivery evidence levels (historical baseline)

**Build/signing:** source `aa8fd9f6d79f28b441a48df31138a75d38420118`, full signed run `35110823569` SUCCESS, focused run `35110823530` SUCCESS, artifact `10451993779` / `BOOP-Wall-Shield-v207-Signed`. The existing permanent certificate, both packages/versions/entry components, accepted artwork and all 16 native libraries were verified. All 18 inherited v206 verification steps and focused split tests passed.

**Installation:** clean uninstall of old Unified and installation of only the corresponding replacement completed on each target. Pixel 7 is `com.boop.alpha1`, `207` / `1.2.207-wall`, SHA-256 `05436dc79441b69d2cd5c328b809dce22464b6ba0fb120cd302d7cd5084c81dd`. Shield is `com.boop.shieldoverlay`, `207` / `1.2.207-shield`, SHA-256 `d4cff18acc85e7360601de042c37281ae6f262018b428525834ed70188574a0f`. On-device hashes matched the signed staged files. Shield's old `com.boop.alpha1` was absent; Wall was not installed on Shield and Shield was not installed on Pixel 7.

**Setup evidence:** both genuine first screens were captured and their completed flag was false/absent, with no Continue press or sign-in by the split sequence. Pixel 7 remained at first setup in the last split read. Shield subsequently reported setup complete and YouTube foreground. Do not claim both currently remain at step one or erase that progress.

**Acceptance:** accepted v206 UI and previously working natural Voice remain protected. Split onboarding and later device behavior have not been physically accepted by Ryan in these records. The Voice latency issue is deferred, not fixed or a prerequisite.

## Split access cleanup and recovery (historical)

Before uninstalling either app, both verified replacements and both known-good, permanently signed recovery APKs were staged; responsive Shield ADB and the existing Android Settings recovery screen were verified without changing Home ownership. No BOOP data, downloaded voice pack, credential or setup flag was restored. No permission was pre-granted, default Home forced, hardware reset or Home Assistant change performed by the split sequence.

During later split reads, three BOOP Shield notification-listener grants and overlay access were found enabled. That sequence revoked only those and preserved unrelated listeners. Their source was not determined. Its final access-off snapshot and no-further-input stopping point are historical, not instructions to undo subsequent setup or the authorized assistant repair. Pixel 10 was never targeted.

## Current source and durable evidence

The application implementation stays at the delivered source above. Subsequent verification-only maintenance at `5f9dbc4e86c0ec92eba728e8b3fbdf56818c2ece` stores exact v206 native hashes so later builds do not depend on an expiring Actions artifact. It is not a new app feature or another install instruction. Consult its actual run result separately from delivered build success.

Split receipt: `docs/handoffs/2026-09-16-v207-signed-clean-install.md`. Previous root status/context/memory are archived byte-for-byte in `docs/handoffs/2026-09-16-v207-before-split/`. The original owner and dirty local v203 documents remain unchanged. Live GitHub, not a version-like branch name or the local checkout, remains authoritative.
