# BOOP Wall and Shield v207: signed and clean-installed

Updated 2026-09-16. Current application owner: `boop-wall-shield-split-v207`. This branch continues the accepted v206 combined implementation, not historical standalone apps. Main is the routing/context hub. Fetch LIVE heads before editing and preserve concurrent work.

## Delivered identity and build

Both applications were built together on GitHub using the existing permanent BOOP key, verified, staged with the known-good recovery APKs, and clean-installed on their corresponding targets only.

| Target | Installed package | Version | APK SHA-256 |
| --- | --- | --- | --- |
| Pixel 7 Pro | `com.boop.alpha1` | `207` / `1.2.207-wall` | `05436dc79441b69d2cd5c328b809dce22464b6ba0fb120cd302d7cd5084c81dd` |
| Nvidia Shield | `com.boop.shieldoverlay` | `207` / `1.2.207-shield` | `d4cff18acc85e7360601de042c37281ae6f262018b428525834ed70188574a0f` |

Installed build source `aa8fd9f6d79f28b441a48df31138a75d38420118`; signed run `35110823569`, job `104843751519`, SUCCESS. Artifact `10451993779`, `BOOP-Wall-Shield-v207-Signed`. Focused run `35110823530` also succeeded. The installed base APK hashes match the signed staged files exactly.

## Actual stopping point: do not rewind Shield progress

Both replacements were opened through their ordinary launcher entry and inspected at genuine first setup, without this sequence pressing Continue, signing in or writing a completed-setup flag. Pixel 7's latest read remains `Set up BOOP Wall`, setup incomplete.

Shield initially showed `Set up BOOP Shield`, setup incomplete. During subsequent checks, its three BOOP notification listeners and overlay access were found enabled. This sequence revoked only those three BOOP listener grants and set only BOOP Shield overlay access to ignored, preserving unrelated listeners, to satisfy the requested unconfigured-access test. No grants or default-Home assignment were issued.

Later readback showed Shield's `setup_intro_completed=true` and YouTube foreground. The actor responsible for that intervening change was not established; no Continue, sign-in or flag-write command was sent by this sequence. Do not claim that both devices are still at setup step one. Do not reset, reinstall, clear flags, change permissions again or force Shield back to setup. Preserve that subsequent progress. At the last access check, Shield listeners/accessibility/overlay and microphone/camera access were off.

## Architecture and preserved implementation

Two thin application shells consume one shared assistant library and the existing animation, launcher and TV libraries. The installed package determines the body; no device/profile chooser. Room, Voice, access and genuine Android Home/permission confirmations remain available. Wall retains its built-in launcher. Shield owns Johnny's existing `com.boop.alpha1.johnny_states` compatibility authority and a canonical Shield alias; the current consumer and unchanged signer/caller guard were audited.

Voice is frozen in BOTH apps. The approximately 12-second latency investigation, other voices/providers and further pitch/cadence work are deferred for Astra, not claimed fixed. No voice implementation, native runtime, model/download manifest or accepted photographic Home/artwork source was replaced. All 16 packaged native libraries matched accepted v206 exactly. The generated-source audit covers 444 shared Java/asset inputs with only the self-package app-list filter changed by shell materialization.

## Next work and continuity

Ryan's newcomer/onboarding and subsequent HA, media, appearance/sharing, notifications and Johnny checks remain human acceptance; CI and installed identity do not prove them. Preserve the v206 UI sign-off. No extra Voice investigation gate, automatic emulator run or fresh reset is implied.

Full receipt: `docs/handoffs/2026-09-16-v207-signed-clean-install.md`. Package audit: `docs/handoffs/2026-09-16-v207-package-integration-audit.md`. Build-verification maintenance after delivery replaces dependence on an expiring v206 artifact with its exact permanent native-hash checkpoint; it changes no application source or installed APK.

The former combined owner `boop-hand-colour-v191` remains at `841458b8bbc53773d16a18bb0359de1c0550f5e2`. Its dirty local v203 worktree was not modified. Full previous root documents are preserved byte-for-byte in `docs/handoffs/2026-09-16-v207-before-split/`; their pending-split/voice-gate statements are historical. Physical Pixel 10, Home Assistant itself, unrelated apps and signing keys were left untouched.
